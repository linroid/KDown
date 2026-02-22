package com.linroid.ketch.core

import kotlinx.coroutines.Dispatchers

@Suppress("UNUSED_PARAMETER")
internal actual fun createDefaultDispatchers(
  networkPoolSize: Int,
  ioPoolSize: Int,
): KetchDispatchers = KetchDispatchers(
  task = Dispatchers.Default,
  network = Dispatchers.Default,
  io = Dispatchers.Default,
)
