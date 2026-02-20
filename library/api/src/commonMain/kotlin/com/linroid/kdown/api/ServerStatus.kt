package com.linroid.kdown.api

import kotlinx.serialization.Serializable

/**
 * Comprehensive status snapshot of a KDown instance.
 *
 * Both [KDown][com.linroid.kdown.core.KDown] (embedded) and
 * [RemoteKDown][com.linroid.kdown.remote.RemoteKDown] (HTTP)
 * return this same type from [KDownApi.status].
 *
 * @property version library version string
 * @property revision build revision (git short hash)
 * @property uptime seconds since the instance started
 * @property tasks task count breakdown by state
 * @property config current download and (optionally) server configuration
 * @property system host system information
 * @property storage download directory storage information
 */
@Serializable
data class ServerStatus(
  val version: String,
  val revision: String,
  val uptime: Long,
  val tasks: TaskStats,
  val config: ConfigStatus,
  val system: SystemStatus,
  val storage: StorageStatus,
)

/**
 * Task count breakdown by state.
 */
@Serializable
data class TaskStats(
  val total: Int,
  val active: Int,
  val downloading: Int,
  val paused: Int,
  val queued: Int,
  val pending: Int,
  val scheduled: Int,
  val completed: Int,
  val failed: Int,
  val canceled: Int,
)

/**
 * Current download and (optionally) server configuration.
 *
 * [server] is `null` for a pure embedded instance that is
 * not backed by a daemon server.
 */
@Serializable
data class ConfigStatus(
  val download: DownloadConfigStatus,
  val queue: QueueConfigStatus,
  val server: ServerConfigStatus? = null,
)

/**
 * Download engine configuration.
 *
 * @property defaultDirectory default directory for saved files
 * @property maxConnections max concurrent segment downloads per task
 * @property retryCount max retry attempts for failed requests
 * @property retryDelayMs base delay between retries in milliseconds
 * @property bufferSize download buffer size in bytes
 * @property speedLimit global speed limit in bytes/sec (0 = unlimited)
 */
@Serializable
data class DownloadConfigStatus(
  val defaultDirectory: String,
  val maxConnections: Int,
  val retryCount: Int,
  val retryDelayMs: Long,
  val bufferSize: Int,
  val speedLimit: Long,
)

/**
 * Download queue configuration.
 *
 * @property maxConcurrentDownloads max simultaneous downloads
 * @property maxConnectionsPerHost max concurrent downloads per host
 * @property autoStart whether queued tasks start automatically
 */
@Serializable
data class QueueConfigStatus(
  val maxConcurrentDownloads: Int,
  val maxConnectionsPerHost: Int,
  val autoStart: Boolean,
)

/**
 * Server network configuration (sanitized — no secrets).
 *
 * @property host bind address
 * @property port listen port
 * @property authEnabled whether bearer-token auth is active
 * @property corsAllowedHosts allowed CORS origins
 * @property mdnsEnabled whether mDNS/DNS-SD registration is active
 */
@Serializable
data class ServerConfigStatus(
  val host: String,
  val port: Int,
  val authEnabled: Boolean,
  val corsAllowedHosts: List<String>,
  val mdnsEnabled: Boolean,
)

/**
 * Host system information.
 *
 * @property os operating system name
 * @property arch CPU architecture
 * @property javaVersion JVM version (or "N/A" on non-JVM platforms)
 * @property availableProcessors number of available CPU cores
 * @property maxMemory max heap / physical memory in bytes
 * @property totalMemory current heap size / physical memory in bytes
 * @property freeMemory free memory in bytes (0 when unavailable)
 */
@Serializable
data class SystemStatus(
  val os: String,
  val arch: String,
  val javaVersion: String,
  val availableProcessors: Int,
  val maxMemory: Long,
  val totalMemory: Long,
  val freeMemory: Long,
)

/**
 * Download directory storage information.
 *
 * @property downloadDirectory resolved download directory path
 * @property totalSpace total disk space in bytes
 * @property freeSpace free disk space in bytes
 * @property usableSpace usable disk space in bytes
 */
@Serializable
data class StorageStatus(
  val downloadDirectory: String,
  val totalSpace: Long,
  val freeSpace: Long,
  val usableSpace: Long,
)
