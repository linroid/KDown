package com.linroid.ketch.core

/**
 * Creates platform-default [KetchDispatchers].
 *
 * @param networkPoolSize number of threads in the network dispatcher pool
 * @param ioPoolSize number of threads in the I/O dispatcher pool;
 *   `0` uses the platform default (e.g., `Dispatchers.IO`)
 */
internal expect fun createDefaultDispatchers(
  networkPoolSize: Int,
  ioPoolSize: Int,
): KetchDispatchers
