package com.linroid.ketch.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Suppress("UNUSED_PARAMETER")
internal actual fun createDefaultDispatchers(
  networkPoolSize: Int,
  ioPoolSize: Int,
): KetchDispatchers = WasmKetchDispatchers

private object WasmKetchDispatchers : KetchDispatchers {
  override val task: CoroutineDispatcher = Dispatchers.Default
  override val network: CoroutineDispatcher = Dispatchers.Default
  override val io: CoroutineDispatcher = Dispatchers.Default
}
