package com.linroid.kdown

import com.linroid.kdown.model.DownloadMetadata

interface MetadataStore {
  suspend fun load(taskId: String): DownloadMetadata?
  suspend fun save(taskId: String, metadata: DownloadMetadata)
  suspend fun clear(taskId: String)
}
