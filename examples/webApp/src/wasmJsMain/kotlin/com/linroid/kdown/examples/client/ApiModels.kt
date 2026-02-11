package com.linroid.kdown.examples.client

import kotlinx.serialization.Serializable

@Serializable
data class CreateDownloadRequest(
  val url: String,
  val directory: String,
  val fileName: String? = null,
  val connections: Int = 1,
  val headers: Map<String, String> = emptyMap(),
  val priority: String = "NORMAL",
  val speedLimitBytesPerSecond: Long = 0
)

@Serializable
data class TaskResponse(
  val taskId: String,
  val url: String,
  val directory: String,
  val fileName: String? = null,
  val state: String,
  val progress: ProgressResponse? = null,
  val error: String? = null,
  val filePath: String? = null,
  val segments: List<SegmentResponse> = emptyList(),
  val createdAt: String = "",
  val priority: String = "NORMAL",
  val speedLimitBytesPerSecond: Long = 0
)

@Serializable
data class ProgressResponse(
  val downloadedBytes: Long,
  val totalBytes: Long,
  val percent: Float,
  val bytesPerSecond: Long
)

@Serializable
data class SegmentResponse(
  val index: Int,
  val start: Long,
  val end: Long,
  val downloadedBytes: Long,
  val isComplete: Boolean
)

@Serializable
data class SpeedLimitRequest(
  val bytesPerSecond: Long
)

@Serializable
data class PriorityRequest(
  val priority: String
)

@Serializable
data class ErrorResponse(
  val error: String,
  val message: String
)

@Serializable
data class ServerStatus(
  val version: String,
  val activeTasks: Int,
  val totalTasks: Int
)

@Serializable
data class TaskEvent(
  val taskId: String,
  val type: String,
  val state: String,
  val progress: ProgressResponse? = null,
  val error: String? = null,
  val filePath: String? = null
)
