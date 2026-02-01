package com.linroid.kdown

import com.linroid.kdown.model.DownloadMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class JsonMetadataStore(
  private val directory: File
) : MetadataStore {
  private val mutex = Mutex()
  private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
  }

  init {
    directory.mkdirs()
  }

  private fun fileFor(taskId: String): File {
    val safeTaskId = taskId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    return File(directory, "$safeTaskId.json")
  }

  override suspend fun load(taskId: String): DownloadMetadata? = mutex.withLock {
    withContext(Dispatchers.IO) {
      val file = fileFor(taskId)
      if (file.exists()) {
        try {
          val content = file.readText()
          json.decodeFromString<DownloadMetadata>(content)
        } catch (e: Exception) {
          null
        }
      } else {
        null
      }
    }
  }

  override suspend fun save(taskId: String, metadata: DownloadMetadata) = mutex.withLock {
    withContext(Dispatchers.IO) {
      val file = fileFor(taskId)
      val content = json.encodeToString(metadata)
      file.writeText(content)
    }
  }

  override suspend fun clear(taskId: String) = mutex.withLock {
    withContext(Dispatchers.IO) {
      val file = fileFor(taskId)
      if (file.exists()) {
        file.delete()
      }
      Unit
    }
  }

  suspend fun clearAll() = mutex.withLock {
    withContext(Dispatchers.IO) {
      directory.listFiles()?.forEach { file ->
        if (file.extension == "json") {
          file.delete()
        }
      }
    }
  }
}
