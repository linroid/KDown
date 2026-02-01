package com.linroid.kdown.internal

import com.linroid.kdown.MetadataStore
import com.linroid.kdown.model.DownloadMetadata
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryMetadataStore : MetadataStore {
  private val mutex = Mutex()
  private val storage = mutableMapOf<String, DownloadMetadata>()

  override suspend fun load(taskId: String): DownloadMetadata? = mutex.withLock {
    storage[taskId]
  }

  override suspend fun save(taskId: String, metadata: DownloadMetadata) = mutex.withLock {
    storage[taskId] = metadata
  }

  override suspend fun clear(taskId: String) = mutex.withLock {
    storage.remove(taskId)
    Unit
  }

  suspend fun clearAll() = mutex.withLock {
    storage.clear()
  }
}
