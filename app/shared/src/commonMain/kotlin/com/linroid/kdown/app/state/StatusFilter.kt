package com.linroid.kdown.app.state

import com.linroid.kdown.api.DownloadState

enum class StatusFilter(val label: String) {
  All("All"),
  Downloading("Downloading"),
  Paused("Paused"),
  Completed("Completed"),
  Failed("Failed");

  fun matches(state: DownloadState): Boolean = when (this) {
    All -> true
    Downloading -> state is DownloadState.Downloading ||
      state is DownloadState.Pending ||
      state is DownloadState.Queued ||
      state is DownloadState.Scheduled
    Paused -> state is DownloadState.Paused
    Completed -> state is DownloadState.Completed
    Failed -> state is DownloadState.Failed ||
      state is DownloadState.Canceled
  }
}
