package com.linroid.kdown

import com.linroid.kdown.error.KDownError
import com.linroid.kdown.model.DownloadState
import com.linroid.kdown.model.Segment
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.io.files.Path

class DownloadTask internal constructor(
  val taskId: String,
  val request: DownloadRequest,
  val createdAt: Long,
  val state: StateFlow<DownloadState>,
  val segments: StateFlow<List<Segment>>,
  private val pauseAction: suspend () -> Unit,
  private val resumeAction: suspend () -> Unit,
  private val cancelAction: suspend () -> Unit,
  private val removeAction: suspend () -> Unit
) {
  suspend fun pause() {
    pauseAction()
  }

  suspend fun resume() {
    resumeAction()
  }

  suspend fun cancel() {
    cancelAction()
  }

  suspend fun remove() {
    removeAction()
  }

  suspend fun await(): Result<Path> {
    val finalState = state.first { it.isTerminal }
    return when (finalState) {
      is DownloadState.Completed -> Result.success(finalState.filePath)
      is DownloadState.Failed -> Result.failure(finalState.error)
      is DownloadState.Canceled -> Result.failure(KDownError.Canceled)
      else -> Result.failure(KDownError.Unknown(null))
    }
  }
}
