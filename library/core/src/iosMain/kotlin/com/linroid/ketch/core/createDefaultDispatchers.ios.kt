package com.linroid.ketch.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual fun createDefaultDispatchers(
  networkPoolSize: Int,
  ioPoolSize: Int,
): KetchDispatchers = IosKetchDispatchers(networkPoolSize, ioPoolSize)

private class IosKetchDispatchers(
  networkPoolSize: Int,
  ioPoolSize: Int,
) : KetchDispatchers {
  override val task: CoroutineDispatcher =
    Dispatchers.Default.limitedParallelism(1)

  override val network: CoroutineDispatcher =
    Dispatchers.Default.limitedParallelism(networkPoolSize)

  override val io: CoroutineDispatcher = if (ioPoolSize > 0) {
    Dispatchers.IO.limitedParallelism(ioPoolSize)
  } else {
    Dispatchers.IO
  }
}
