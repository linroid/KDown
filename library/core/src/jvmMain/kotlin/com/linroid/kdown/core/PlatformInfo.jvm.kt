package com.linroid.kdown.core

import com.linroid.kdown.api.StorageStatus
import com.linroid.kdown.api.SystemStatus
import java.io.File

internal actual fun currentSystemStatus(): SystemStatus {
  val runtime = Runtime.getRuntime()
  return SystemStatus(
    os = System.getProperty("os.name", "unknown"),
    arch = System.getProperty("os.arch", "unknown"),
    javaVersion = System.getProperty("java.version", "unknown"),
    availableProcessors = runtime.availableProcessors(),
    maxMemory = runtime.maxMemory(),
    totalMemory = runtime.totalMemory(),
    freeMemory = runtime.freeMemory(),
  )
}

internal actual fun currentStorageStatus(directory: String): StorageStatus {
  val dir = File(directory)
  return StorageStatus(
    downloadDirectory = dir.absolutePath,
    totalSpace = dir.totalSpace,
    freeSpace = dir.freeSpace,
    usableSpace = dir.usableSpace,
  )
}
