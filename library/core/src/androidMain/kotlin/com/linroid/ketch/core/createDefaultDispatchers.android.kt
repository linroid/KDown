@file:OptIn(DelicateCoroutinesApi::class)

package com.linroid.ketch.core

import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext

internal actual fun createDefaultDispatchers(
  networkPoolSize: Int,
  ioPoolSize: Int,
): KetchDispatchers = AndroidKetchDispatchers(networkPoolSize, ioPoolSize)

private class AndroidKetchDispatchers(
  networkPoolSize: Int,
  ioPoolSize: Int,
) : KetchDispatchers {
  override val task: CloseableCoroutineDispatcher =
    newSingleThreadContext("ketch-task")

  override val network: CloseableCoroutineDispatcher =
    newFixedThreadPoolContext(networkPoolSize, "ketch-network")

  private val ioPool: CloseableCoroutineDispatcher? = if (ioPoolSize > 0) {
    newFixedThreadPoolContext(ioPoolSize, "ketch-io")
  } else {
    null
  }

  override val io: CoroutineDispatcher = ioPool ?: Dispatchers.IO

  override fun close() {
    task.close()
    network.close()
    ioPool?.close()
  }
}
