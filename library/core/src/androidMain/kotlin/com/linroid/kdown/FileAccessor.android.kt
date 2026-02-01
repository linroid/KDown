package com.linroid.kdown

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

actual class FileAccessor actual constructor(private val path: String) {
  private val file = File(path)
  private var randomAccessFile: RandomAccessFile? = null
  private val mutex = Mutex()

  private suspend fun getOrCreateFile(): RandomAccessFile = mutex.withLock {
    randomAccessFile ?: run {
      file.parentFile?.mkdirs()
      RandomAccessFile(file, "rw").also { randomAccessFile = it }
    }
  }

  actual suspend fun writeAt(offset: Long, data: ByteArray) {
    withContext(Dispatchers.IO) {
      val raf = getOrCreateFile()
      mutex.withLock {
        raf.seek(offset)
        raf.write(data)
      }
    }
  }

  actual suspend fun flush() {
    withContext(Dispatchers.IO) {
      mutex.withLock {
        randomAccessFile?.fd?.sync()
      }
    }
  }

  actual fun close() {
    randomAccessFile?.close()
    randomAccessFile = null
  }

  actual suspend fun delete() {
    withContext(Dispatchers.IO) {
      close()
      file.delete()
    }
  }

  actual suspend fun size(): Long = withContext(Dispatchers.IO) {
    if (file.exists()) file.length() else 0L
  }

  actual suspend fun preallocate(size: Long) {
    withContext(Dispatchers.IO) {
      val raf = getOrCreateFile()
      mutex.withLock {
        raf.setLength(size)
      }
    }
  }
}
