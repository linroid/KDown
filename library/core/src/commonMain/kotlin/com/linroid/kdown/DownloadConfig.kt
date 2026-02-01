package com.linroid.kdown

data class DownloadConfig(
  val maxConnections: Int = 4,
  val retryCount: Int = 3,
  val retryDelayMs: Long = 1000,
  val progressUpdateIntervalMs: Long = 200,
  val bufferSize: Int = 8192
) {
  init {
    require(maxConnections > 0) { "maxConnections must be greater than 0" }
    require(retryCount >= 0) { "retryCount must be non-negative" }
    require(retryDelayMs >= 0) { "retryDelayMs must be non-negative" }
    require(progressUpdateIntervalMs > 0) { "progressUpdateIntervalMs must be greater than 0" }
    require(bufferSize > 0) { "bufferSize must be greater than 0" }
  }

  companion object {
    val Default = DownloadConfig()
  }
}
