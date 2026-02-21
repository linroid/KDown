package com.linroid.ketch.endpoints.model

import com.linroid.ketch.api.DownloadRequest
import com.linroid.ketch.api.DownloadState
import com.linroid.ketch.api.Segment
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class TaskList(
  val tasks: List<TaskResponse>
)

/**
 * Response for a single download task.
 */
@Serializable
data class TaskResponse(
  val taskId: String,
  val request: DownloadRequest,
  val state: DownloadState,
  val segments: List<Segment> = emptyList(),
  val createdAt: Instant,
)
