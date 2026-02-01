package com.linroid.kdown.model

data class ServerInfo(
  val contentLength: Long?,
  val acceptRanges: Boolean,
  val etag: String?,
  val lastModified: String?
) {
  val supportsResume: Boolean
    get() = acceptRanges && contentLength != null && contentLength > 0
}
