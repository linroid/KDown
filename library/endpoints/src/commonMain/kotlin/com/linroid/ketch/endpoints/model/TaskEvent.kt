package com.linroid.ketch.endpoints.model

import com.linroid.ketch.api.DownloadState
import kotlinx.serialization.Serializable

/**
 * Server-Sent Event payload for real-time task updates.
 */
@Serializable
data class TaskEvent(
  val taskId: String,
  val type: String,
  val state: DownloadState,
)
