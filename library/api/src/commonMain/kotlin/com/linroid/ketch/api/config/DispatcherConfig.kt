package com.linroid.ketch.api.config

import kotlinx.serialization.Serializable

/**
 * Configuration for the coroutine dispatchers used by the download engine.
 *
 * Controls the sizing of dedicated thread pools / dispatcher parallelism:
 * - A single-threaded **task** dispatcher handles scheduling, queue
 *   management, and state transitions.
 * - A **network** thread pool executes HTTP requests and segment downloads.
 * - An **I/O** pool handles blocking file operations (reads, writes,
 *   preallocate).
 *
 * @property networkPoolSize Number of threads (or parallelism level on
 *   platforms without real threads) for the network dispatcher.
 *   Defaults to [DownloadConfig.maxConnections].
 * @property ioPoolSize Number of threads for the I/O dispatcher.
 *   `0` (default) means use the platform default (e.g., `Dispatchers.IO`
 *   on JVM/Android/iOS).
 */
@Serializable
data class DispatcherConfig(
  val networkPoolSize: Int = 0,
  val ioPoolSize: Int = 0,
) {
  init {
    require(networkPoolSize >= 0) {
      "networkPoolSize must be non-negative"
    }
    require(ioPoolSize >= 0) {
      "ioPoolSize must be non-negative"
    }
  }

  companion object {
    val Default = DispatcherConfig()
  }
}
