package com.linroid.ketch.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

internal actual fun createDefaultDispatchers(
  networkPoolSize: Int,
  ioPoolSize: Int,
): KetchDispatchers = JvmKetchDispatchers(networkPoolSize, ioPoolSize)

private class JvmKetchDispatchers(
  networkPoolSize: Int,
  ioPoolSize: Int,
) : KetchDispatchers {
  private val taskExecutor = Executors.newSingleThreadExecutor { r ->
    Thread(r, "ketch-task").apply { isDaemon = true }
  }
  private val networkExecutor = Executors.newFixedThreadPool(
    networkPoolSize,
  ) { r ->
    Thread(r, "ketch-network").apply { isDaemon = true }
  }
  private val ioExecutor = if (ioPoolSize > 0) {
    Executors.newFixedThreadPool(ioPoolSize) { r ->
      Thread(r, "ketch-io").apply { isDaemon = true }
    }
  } else {
    null
  }

  override val task: CoroutineDispatcher =
    taskExecutor.asCoroutineDispatcher()

  override val network: CoroutineDispatcher =
    networkExecutor.asCoroutineDispatcher()

  override val io: CoroutineDispatcher =
    ioExecutor?.asCoroutineDispatcher() ?: Dispatchers.IO

  override fun close() {
    taskExecutor.shutdown()
    networkExecutor.shutdown()
    ioExecutor?.shutdown()
  }
}
