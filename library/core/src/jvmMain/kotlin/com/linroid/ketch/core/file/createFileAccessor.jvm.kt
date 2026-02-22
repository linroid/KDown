package com.linroid.ketch.core.file

import kotlinx.coroutines.CoroutineDispatcher
import java.io.RandomAccessFile

actual fun createFileAccessor(
  path: String,
  ioDispatcher: CoroutineDispatcher,
): FileAccessor {
  return PathFileAccessor(path, ioDispatcher) { realPath ->
    JvmRandomAccessHandle(RandomAccessFile(realPath, "rw"))
  }
}
