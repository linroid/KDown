package com.linroid.kdown.core

import com.linroid.kdown.api.StorageStatus
import com.linroid.kdown.api.SystemStatus

/** Returns current system information (OS, arch, memory, etc.). */
internal expect fun currentSystemStatus(): SystemStatus

/** Returns storage information for the given [directory]. */
internal expect fun currentStorageStatus(directory: String): StorageStatus
