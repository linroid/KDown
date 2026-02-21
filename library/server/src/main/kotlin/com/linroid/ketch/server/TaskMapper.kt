package com.linroid.ketch.server

import com.linroid.ketch.api.DownloadTask
import com.linroid.ketch.endpoints.model.TaskEvent
import com.linroid.ketch.endpoints.model.TaskSnapshot

internal object TaskMapper {

  fun toSnapshot(task: DownloadTask): TaskSnapshot {
    return TaskSnapshot(
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
