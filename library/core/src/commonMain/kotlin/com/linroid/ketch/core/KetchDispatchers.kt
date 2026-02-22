package com.linroid.ketch.core

import com.linroid.ketch.api.config.DispatcherConfig
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Dispatchers used by the Ketch download engine.
 *
 * Provides dedicated dispatchers for different workloads:
 * - [task]: Single-threaded dispatcher for task management (scheduling,
 *   queue operations, state transitions). Serialized execution eliminates
 *   Mutex contention for coordination logic.
 * - [network]: Thread pool for network operations (HTTP requests, segment
 *   downloads). Sized to match expected download parallelism.
 * - [io]: Thread pool for blocking file I/O (writes, reads, preallocate).
 *
 * Implement this interface to provide custom dispatchers, or use [Default]
 * for platform-appropriate defaults.
 */
interface KetchDispatchers {
  /** Single-threaded dispatcher for task coordination. */
  val task: CoroutineDispatcher

  /** Thread pool for network operations. */
  val network: CoroutineDispatcher

  /** Thread pool for blocking file I/O. */
  val io: CoroutineDispatcher

  /** Releases dispatcher resources (thread pools). Call on shutdown. */
  fun close() {}

  companion object {
    /**
     * Creates platform-default dispatchers with dedicated thread pools.
     *
     * @param networkPoolSize number of threads in the network pool;
     *   typically matches [com.linroid.ketch.api.config.DownloadConfig.maxConnections]
     * @param ioPoolSize number of threads in the I/O pool;
     *   `0` uses the platform default (`Dispatchers.IO`)
     */
    fun Default(
      networkPoolSize: Int = 4,
      ioPoolSize: Int = 0,
    ): KetchDispatchers =
      createDefaultDispatchers(networkPoolSize, ioPoolSize)

    /**
     * Creates dispatchers from a [DispatcherConfig].
     *
     * @param config dispatcher sizing configuration
     * @param maxConnections fallback network pool size when
     *   [DispatcherConfig.networkPoolSize] is `0`
     */
    fun fromConfig(
      config: DispatcherConfig,
      maxConnections: Int = 4,
    ): KetchDispatchers {
      val networkSize = if (config.networkPoolSize > 0) {
        config.networkPoolSize
      } else {
        maxConnections
      }
      return createDefaultDispatchers(networkSize, config.ioPoolSize)
    }
  }
}
