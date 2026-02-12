package com.linroid.kdown.endpoints

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

/**
 * Type-safe Ktor Resource definitions for the KDown REST API.
 *
 * These resources are shared between the server and remote client
 * to ensure endpoint paths are defined in a single place.
 *
 * ## Endpoints
 *
 * ### Server
 * - `GET  /api/status`       — server health and task counts
 * - `PUT  /api/speed-limit`  — set global speed limit
 *
 * ### Downloads
 * - `GET    /api/downloads`                  — list all tasks
 * - `POST   /api/downloads`                  — create a new download
 * - `GET    /api/downloads/{id}`             — get task by ID
 * - `POST   /api/downloads/{id}/pause`       — pause a download
 * - `POST   /api/downloads/{id}/resume`      — resume a download
 * - `POST   /api/downloads/{id}/cancel`      — cancel a download
 * - `DELETE /api/downloads/{id}`             — remove a task
 * - `PUT    /api/downloads/{id}/speed-limit` — set task speed limit
 * - `PUT    /api/downloads/{id}/priority`    — set task priority
 *
 * ### Events (SSE)
 * - `GET /api/events`       — SSE stream of all task events
 * - `GET /api/events/{id}`  — SSE stream for a specific task
 */
@Serializable
@Resource("/api")
class Api {

  @Serializable
  @Resource("status")
  data class Status(val parent: Api = Api())

  @Serializable
  @Resource("speed-limit")
  data class SpeedLimit(val parent: Api = Api())

  @Serializable
  @Resource("downloads")
  data class Downloads(val parent: Api = Api()) {

    @Serializable
    @Resource("{id}")
    data class ById(
      val parent: Downloads = Downloads(),
      val id: String
    ) {

      @Serializable
      @Resource("pause")
      data class Pause(val parent: ById)

      @Serializable
      @Resource("resume")
      data class Resume(val parent: ById)

      @Serializable
      @Resource("cancel")
      data class Cancel(val parent: ById)

      @Serializable
      @Resource("speed-limit")
      data class SpeedLimit(val parent: ById)

      @Serializable
      @Resource("priority")
      data class Priority(val parent: ById)
    }
  }

  @Serializable
  @Resource("events")
  data class Events(val parent: Api = Api()) {

    @Serializable
    @Resource("{id}")
    data class ById(
      val parent: Events = Events(),
      val id: String
    )
  }
}
