package com.linroid.ketch

import com.linroid.ketch.api.config.CoreConfig
import kotlin.test.Test
import kotlin.test.assertFailsWith

class CoreConfigTest {

  @Test
  fun invalidMaxConnections_throws() {
    assertFailsWith<IllegalArgumentException> {
      CoreConfig(maxConnections = 0)
    }
  }

  @Test
  fun negativeRetryCount_throws() {
    assertFailsWith<IllegalArgumentException> {
      CoreConfig(retryCount = -1)
    }
  }

  @Test
  fun negativeRetryDelay_throws() {
    assertFailsWith<IllegalArgumentException> {
      CoreConfig(retryDelayMs = -1)
    }
  }

  @Test
  fun zeroProgressInterval_throws() {
    assertFailsWith<IllegalArgumentException> {
      CoreConfig(progressUpdateIntervalMs = 0)
    }
  }

  @Test
  fun zeroBufferSize_throws() {
    assertFailsWith<IllegalArgumentException> {
      CoreConfig(bufferSize = 0)
    }
  }
}
