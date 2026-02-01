package com.linroid.kdown.model

import kotlinx.serialization.Serializable

@Serializable
data class DownloadMetadata(
  val taskId: String,
  val url: String,
  val destPath: String,
  val totalBytes: Long,
  val acceptRanges: Boolean,
  val etag: String?,
  val lastModified: String?,
  val segments: List<Segment>,
  val createdAt: Long,
  val updatedAt: Long
) {
  val downloadedBytes: Long
    get() = segments.sumOf { it.downloadedBytes }

  val isComplete: Boolean
    get() = segments.all { it.isComplete }

  fun withUpdatedSegment(segmentIndex: Int, downloadedBytes: Long, currentTime: Long): DownloadMetadata {
    val updatedSegments = segments.mapIndexed { index, segment ->
      if (index == segmentIndex) segment.copy(downloadedBytes = downloadedBytes)
      else segment
    }
    return copy(
      segments = updatedSegments,
      updatedAt = currentTime
    )
  }
}
