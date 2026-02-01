package com.linroid.kdown

expect class FileAccessor(path: String) {
  suspend fun writeAt(offset: Long, data: ByteArray)
  suspend fun flush()
  fun close()
  suspend fun delete()
  suspend fun size(): Long
  suspend fun preallocate(size: Long)
}
