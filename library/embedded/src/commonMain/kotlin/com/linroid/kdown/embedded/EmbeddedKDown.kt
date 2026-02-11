package com.linroid.kdown.embedded

import com.linroid.kdown.DownloadRequest
import com.linroid.kdown.KDown as KDownEngine
import com.linroid.kdown.SpeedLimit
import com.linroid.kdown.api.KDown
import com.linroid.kdown.task.DownloadTask
import kotlinx.coroutines.flow.StateFlow

/**
 * Embedded in-process implementation of [KDown] that delegates
 * to a [KDownEngine] instance directly. No HTTP involved.
 */
class EmbeddedKDown(
  private val kdown: KDownEngine
) : KDown {

  override val backendLabel: String = "Embedded"

  override val tasks: StateFlow<List<DownloadTask>> = kdown.tasks

  override suspend fun download(
    request: DownloadRequest
  ): DownloadTask {
    return kdown.download(request)
  }

  override suspend fun setGlobalSpeedLimit(limit: SpeedLimit) {
    kdown.setSpeedLimit(limit)
  }

  override fun close() {
    kdown.close()
  }
}
