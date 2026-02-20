package com.linroid.kdown.core

import com.linroid.kdown.api.StorageStatus
import com.linroid.kdown.api.SystemStatus
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSFileSystemSize
import platform.Foundation.NSProcessInfo

internal actual fun currentSystemStatus(): SystemStatus {
  val info = NSProcessInfo.processInfo
  return SystemStatus(
    os = "iOS ${info.operatingSystemVersionString}",
    arch = "arm64",
    javaVersion = "N/A",
    availableProcessors = info.activeProcessorCount.toInt(),
    maxMemory = info.physicalMemory.toLong(),
    totalMemory = info.physicalMemory.toLong(),
    freeMemory = 0L,
  )
}

internal actual fun currentStorageStatus(directory: String): StorageStatus {
  val fm = NSFileManager.defaultManager
  val attrs = fm.attributesOfFileSystemForPath(directory, null)
  val totalSpace = (attrs?.get(NSFileSystemSize) as? Number)
    ?.toLong() ?: 0L
  val freeSpace = (attrs?.get(NSFileSystemFreeSize) as? Number)
    ?.toLong() ?: 0L
  return StorageStatus(
    downloadDirectory = directory,
    totalSpace = totalSpace,
    freeSpace = freeSpace,
    usableSpace = freeSpace,
  )
}
