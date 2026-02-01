package com.linroid.kdown.internal

import com.linroid.kdown.HttpEngine
import com.linroid.kdown.model.ServerInfo

internal class RangeSupportDetector(
  private val httpEngine: HttpEngine
) {
  suspend fun detect(url: String): ServerInfo {
    return httpEngine.head(url)
  }
}
