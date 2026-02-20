package com.linroid.kdown.engine

import com.linroid.kdown.api.KDownError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for HTTP 429 (Too Many Requests) rate limit handling:
 * retryable classification, Retry-After propagation, and
 * FakeHttpEngine 429 support for downstream tests.
 */
class RateLimitTest {

  @Test
  fun fakeEngine_429Download_throwsRetryableHttpError() = runTest {
    val engine = FakeHttpEngine(httpErrorCode = 429)
    val error = assertFailsWith<KDownError.Http> {
      engine.download("https://example.com/file", null) {}
    }
    assertEquals(429, error.code)
    assertTrue(error.isRetryable)
  }

  @Test
  fun fakeEngine_429Download_includesRetryAfterSeconds() = runTest {
    val engine = FakeHttpEngine(
      httpErrorCode = 429,
      retryAfterSeconds = 60,
    )
    val error = assertFailsWith<KDownError.Http> {
      engine.download("https://example.com/file", null) {}
    }
    assertEquals(429, error.code)
    assertEquals(60L, error.retryAfterSeconds)
    assertTrue(error.isRetryable)
  }

  @Test
  fun fakeEngine_429Head_includesRetryAfterSeconds() = runTest {
    val engine = FakeHttpEngine(
      httpErrorCode = 429,
      retryAfterSeconds = 30,
    )
    val error = assertFailsWith<KDownError.Http> {
      engine.head("https://example.com/file")
    }
    assertEquals(429, error.code)
    assertEquals(30L, error.retryAfterSeconds)
  }

  @Test
  fun fakeEngine_429_withoutRetryAfter() = runTest {
    val engine = FakeHttpEngine(httpErrorCode = 429)
    val error = assertFailsWith<KDownError.Http> {
      engine.download("https://example.com/file", null) {}
    }
    assertEquals(429, error.code)
    assertNull(error.retryAfterSeconds)
    assertTrue(error.isRetryable)
  }

  @Test
  fun fakeEngine_500_noRetryAfter() = runTest {
    val engine = FakeHttpEngine(
      httpErrorCode = 500,
      retryAfterSeconds = 10,
    )
    val error = assertFailsWith<KDownError.Http> {
      engine.download("https://example.com/file", null) {}
    }
    assertEquals(500, error.code)
    // retryAfterSeconds is still passed through by FakeHttpEngine
    // regardless of code; real KtorHttpEngine only parses it for 429
    assertEquals(10L, error.retryAfterSeconds)
  }

  @Test
  fun connectionReduction_halvesWithMinimumOfOne() {
    // Verifies the reduction formula used by
    // DownloadCoordinator.reduceConnections
    val cases = mapOf(
      8 to 4,
      4 to 2,
      3 to 1,
      2 to 1,
      1 to 1,
    )
    for ((input, expected) in cases) {
      val reduced = (input / 2).coerceAtLeast(1)
      assertEquals(
        expected, reduced,
        "Expected $input connections to reduce to $expected",
      )
    }
  }

  @Test
  fun connectionReduction_chain() {
    // Simulates successive 429 reductions: 8 -> 4 -> 2 -> 1 -> 1
    var connections = 8
    val expected = listOf(4, 2, 1, 1)
    for (exp in expected) {
      connections = (connections / 2).coerceAtLeast(1)
      assertEquals(exp, connections)
    }
  }
}
