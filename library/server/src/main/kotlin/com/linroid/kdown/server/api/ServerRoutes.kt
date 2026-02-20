package com.linroid.kdown.server.api

import com.linroid.kdown.api.DownloadState
import com.linroid.kdown.api.KDownApi
import com.linroid.kdown.api.KDownVersion
import com.linroid.kdown.api.SpeedLimit
import com.linroid.kdown.core.DownloadConfig
import com.linroid.kdown.endpoints.Api
import com.linroid.kdown.endpoints.model.ConfigStatus
import com.linroid.kdown.endpoints.model.DownloadConfigStatus
import com.linroid.kdown.endpoints.model.QueueConfigStatus
import com.linroid.kdown.endpoints.model.ResolveUrlRequest
import com.linroid.kdown.endpoints.model.ResolveUrlResponse
import com.linroid.kdown.endpoints.model.ServerConfigStatus
import com.linroid.kdown.endpoints.model.ServerStatus
import com.linroid.kdown.endpoints.model.SourceFileResponse
import com.linroid.kdown.endpoints.model.SpeedLimitRequest
import com.linroid.kdown.endpoints.model.StorageStatus
import com.linroid.kdown.endpoints.model.SystemStatus
import com.linroid.kdown.endpoints.model.TaskStats
import com.linroid.kdown.server.KDownServerConfig
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import java.io.File

/**
 * Installs server-level endpoints: status, global speed limit,
 * and URL resolution.
 */
internal fun Route.serverRoutes(
  kdown: KDownApi,
  downloadConfig: DownloadConfig,
  serverConfig: KDownServerConfig,
  startedAt: Long,
) {
  get<Api.Status> {
    val tasks = kdown.tasks.value
    val taskStats = TaskStats(
      total = tasks.size,
      active = tasks.count { it.state.value.isActive },
      downloading = tasks.count { it.state.value is DownloadState.Downloading },
      paused = tasks.count { it.state.value is DownloadState.Paused },
      queued = tasks.count { it.state.value is DownloadState.Queued },
      pending = tasks.count { it.state.value is DownloadState.Pending },
      scheduled = tasks.count { it.state.value is DownloadState.Scheduled },
      completed = tasks.count { it.state.value is DownloadState.Completed },
      failed = tasks.count { it.state.value is DownloadState.Failed },
      canceled = tasks.count { it.state.value is DownloadState.Canceled },
    )

    val configStatus = ConfigStatus(
      download = DownloadConfigStatus(
        defaultDirectory = downloadConfig.defaultDirectory,
        maxConnections = downloadConfig.maxConnections,
        retryCount = downloadConfig.retryCount,
        retryDelayMs = downloadConfig.retryDelayMs,
        bufferSize = downloadConfig.bufferSize,
        speedLimit = downloadConfig.speedLimit.bytesPerSecond,
      ),
      queue = QueueConfigStatus(
        maxConcurrentDownloads = downloadConfig.queueConfig.maxConcurrentDownloads,
        maxConnectionsPerHost = downloadConfig.queueConfig.maxConnectionsPerHost,
        autoStart = downloadConfig.queueConfig.autoStart,
      ),
      server = ServerConfigStatus(
        host = serverConfig.host,
        port = serverConfig.port,
        authEnabled = serverConfig.apiToken != null,
        corsAllowedHosts = serverConfig.corsAllowedHosts,
        mdnsEnabled = serverConfig.mdnsEnabled,
      ),
    )

    val runtime = Runtime.getRuntime()
    val systemStatus = SystemStatus(
      os = System.getProperty("os.name", "unknown"),
      arch = System.getProperty("os.arch", "unknown"),
      javaVersion = System.getProperty("java.version", "unknown"),
      availableProcessors = runtime.availableProcessors(),
      maxMemory = runtime.maxMemory(),
      totalMemory = runtime.totalMemory(),
      freeMemory = runtime.freeMemory(),
    )

    val downloadDir = File(downloadConfig.defaultDirectory)
    val storageStatus = StorageStatus(
      downloadDirectory = downloadDir.absolutePath,
      totalSpace = downloadDir.totalSpace,
      freeSpace = downloadDir.freeSpace,
      usableSpace = downloadDir.usableSpace,
    )

    val uptimeSeconds =
      (System.currentTimeMillis() - startedAt) / 1000

    call.respond(
      ServerStatus(
        version = KDownVersion.DEFAULT,
        revision = KDownVersion.REVISION,
        uptime = uptimeSeconds,
        tasks = taskStats,
        config = configStatus,
        system = systemStatus,
        storage = storageStatus,
      )
    )
  }

  put<Api.SpeedLimit> {
    val body = call.receive<SpeedLimitRequest>()
    val limit = if (body.bytesPerSecond > 0) {
      SpeedLimit.of(body.bytesPerSecond)
    } else {
      SpeedLimit.Unlimited
    }
    kdown.setGlobalSpeedLimit(limit)
    call.respond(body)
  }

  post<Api.Resolve> {
    val body = call.receive<ResolveUrlRequest>()
    val resolved = kdown.resolve(body.url, body.headers)
    call.respond(
      ResolveUrlResponse(
        url = resolved.url,
        sourceType = resolved.sourceType,
        totalBytes = resolved.totalBytes,
        supportsResume = resolved.supportsResume,
        suggestedFileName = resolved.suggestedFileName,
        maxSegments = resolved.maxSegments,
        metadata = resolved.metadata,
        files = resolved.files.map { file ->
          SourceFileResponse(
            id = file.id,
            name = file.name,
            size = file.size,
            metadata = file.metadata,
          )
        },
        selectionMode = resolved.selectionMode.name,
      )
    )
  }
}
