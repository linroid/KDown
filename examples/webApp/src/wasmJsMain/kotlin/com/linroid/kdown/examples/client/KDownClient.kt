package com.linroid.kdown.examples.client

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * HTTP client for the KDown daemon server REST API.
 *
 * @param baseUrl the server base URL (e.g., "http://localhost:8642")
 * @param apiToken optional Bearer token for authentication
 */
class KDownClient(
  private val baseUrl: String = "http://localhost:8642",
  private val apiToken: String? = null
) {
  private val httpClient = HttpClient()
  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  suspend fun getStatus(): ServerStatus {
    val response = httpClient.get("$baseUrl/api/status") {
      applyAuth()
    }
    return json.decodeFromString(response.bodyAsText())
  }

  suspend fun listTasks(): List<TaskResponse> {
    val response = httpClient.get(
      "$baseUrl/api/downloads"
    ) {
      applyAuth()
    }
    return json.decodeFromString(response.bodyAsText())
  }

  suspend fun getTask(taskId: String): TaskResponse {
    val response = httpClient.get(
      "$baseUrl/api/downloads/$taskId"
    ) {
      applyAuth()
    }
    check(response.status.isSuccess()) {
      "Task not found: $taskId"
    }
    return json.decodeFromString(response.bodyAsText())
  }

  suspend fun createDownload(
    request: CreateDownloadRequest
  ): TaskResponse {
    val response = httpClient.post(
      "$baseUrl/api/downloads"
    ) {
      applyAuth()
      contentType(ContentType.Application.Json)
      setBody(json.encodeToString(
        CreateDownloadRequest.serializer(), request
      ))
    }
    return json.decodeFromString(response.bodyAsText())
  }

  suspend fun pauseTask(taskId: String): TaskResponse {
    val response = httpClient.post(
      "$baseUrl/api/downloads/$taskId/pause"
    ) {
      applyAuth()
    }
    return json.decodeFromString(response.bodyAsText())
  }

  suspend fun resumeTask(taskId: String): TaskResponse {
    val response = httpClient.post(
      "$baseUrl/api/downloads/$taskId/resume"
    ) {
      applyAuth()
    }
    return json.decodeFromString(response.bodyAsText())
  }

  suspend fun cancelTask(taskId: String): TaskResponse {
    val response = httpClient.post(
      "$baseUrl/api/downloads/$taskId/cancel"
    ) {
      applyAuth()
    }
    return json.decodeFromString(response.bodyAsText())
  }

  suspend fun removeTask(taskId: String) {
    httpClient.delete(
      "$baseUrl/api/downloads/$taskId"
    ) {
      applyAuth()
    }
  }

  suspend fun setTaskSpeedLimit(
    taskId: String,
    bytesPerSecond: Long
  ): TaskResponse {
    val response = httpClient.put(
      "$baseUrl/api/downloads/$taskId/speed-limit"
    ) {
      applyAuth()
      contentType(ContentType.Application.Json)
      setBody(json.encodeToString(
        SpeedLimitRequest.serializer(),
        SpeedLimitRequest(bytesPerSecond)
      ))
    }
    return json.decodeFromString(response.bodyAsText())
  }

  suspend fun setTaskPriority(
    taskId: String,
    priority: String
  ): TaskResponse {
    val response = httpClient.put(
      "$baseUrl/api/downloads/$taskId/priority"
    ) {
      applyAuth()
      contentType(ContentType.Application.Json)
      setBody(json.encodeToString(
        PriorityRequest.serializer(),
        PriorityRequest(priority)
      ))
    }
    return json.decodeFromString(response.bodyAsText())
  }

  suspend fun setGlobalSpeedLimit(bytesPerSecond: Long) {
    httpClient.put("$baseUrl/api/speed-limit") {
      applyAuth()
      contentType(ContentType.Application.Json)
      setBody(json.encodeToString(
        SpeedLimitRequest.serializer(),
        SpeedLimitRequest(bytesPerSecond)
      ))
    }
  }

  /** Returns the SSE events URL for all tasks. */
  fun eventsUrl(): String = "$baseUrl/api/events"

  fun close() {
    httpClient.close()
  }

  private fun io.ktor.client.request.HttpRequestBuilder.applyAuth() {
    if (apiToken != null) {
      header(HttpHeaders.Authorization, "Bearer $apiToken")
    }
  }
}
