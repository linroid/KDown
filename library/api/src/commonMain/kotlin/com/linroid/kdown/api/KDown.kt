package com.linroid.kdown.api

import com.linroid.kdown.DownloadRequest
import com.linroid.kdown.SpeedLimit
import com.linroid.kdown.task.DownloadTask
import kotlinx.coroutines.flow.StateFlow

/**
 * Service interface for managing downloads. Both embedded (in-process)
 * and remote (HTTP+SSE) backends implement this interface, allowing
 * the UI to work identically regardless of backend mode.
 */
interface KDown {

  /** Human-readable label: "Embedded" or "Remote · host:port". */
  val backendLabel: String

  /** Reactive task list updated on any state change. */
  val tasks: StateFlow<List<DownloadTask>>

  /** Create a new download and return the task handle. */
  suspend fun download(request: DownloadRequest): DownloadTask

  /** Set global speed limit (use [SpeedLimit.Unlimited] to remove). */
  suspend fun setGlobalSpeedLimit(limit: SpeedLimit)

  /** Release resources (HTTP client, SSE connection, etc.). */
  fun close()
}
