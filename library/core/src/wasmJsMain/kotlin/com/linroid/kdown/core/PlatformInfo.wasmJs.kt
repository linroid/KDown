package com.linroid.kdown.core

import com.linroid.kdown.api.StorageStatus
import com.linroid.kdown.api.SystemStatus

internal actual fun currentSystemStatus(): SystemStatus {
  return SystemStatus(
    os = "Browser",
    arch = "wasm",
    javaVersion = "N/A",
    availableProcessors = 1,
    maxMemory = 0L,
    totalMemory = 0L,
    freeMemory = 0L,
  )
}

internal actual fun currentStorageStatus(directory: String): StorageStatus {
  return StorageStatus(
    downloadDirectory = directory,
    totalSpace = 0L,
    freeSpace = 0L,
    usableSpace = 0L,
  )
}
