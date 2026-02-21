package com.linroid.ketch.server

import com.linroid.ketch.api.DownloadTask
import com.linroid.ketch.endpoints.model.TaskEvent
import com.linroid.ketch.endpoints.model.TaskResponse

internal object TaskMapper {

  fun toResponse(task: DownloadTask): TaskResponse {
    return TaskResponse(
      taskId = task.taskId,
      request = task.request,
      state = task.state.value,
      segments = task.segments.value,
      createdAt = task.createdAt,
    )
  }

  fun toEvent(
    task: DownloadTask,
    eventType: String,
  ): TaskEvent {
    return TaskEvent(
      taskId = task.taskId,
      type = eventType,
      state = task.state.value,
    )
  }
}
