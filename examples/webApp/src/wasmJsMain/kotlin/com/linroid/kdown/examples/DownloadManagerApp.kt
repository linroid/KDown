package com.linroid.kdown.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linroid.kdown.examples.client.CreateDownloadRequest
import com.linroid.kdown.examples.client.KDownClient
import com.linroid.kdown.examples.client.ServerStatus
import com.linroid.kdown.examples.client.TaskResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerApp() {
  val client = remember { KDownClient() }
  val scope = rememberCoroutineScope()

  var tasks by remember {
    mutableStateOf<List<TaskResponse>>(emptyList())
  }
  var serverStatus by remember {
    mutableStateOf<ServerStatus?>(null)
  }
  var errorMessage by remember {
    mutableStateOf<String?>(null)
  }
  var showAddDialog by remember { mutableStateOf(false) }
  var connected by remember { mutableStateOf(false) }

  DisposableEffect(Unit) {
    onDispose { client.close() }
  }

  // Poll for task updates
  LaunchedEffect(Unit) {
    while (isActive) {
      try {
        val status = client.getStatus()
        serverStatus = status
        tasks = client.listTasks()
        connected = true
        errorMessage = null
      } catch (e: Exception) {
        connected = false
        errorMessage = "Cannot connect to server. " +
          "Is KDown daemon running on localhost:8642?"
      }
      delay(1000)
    }
  }

  MaterialTheme {
    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Column {
              Text(
                text = "KDown Web",
                fontWeight = FontWeight.SemiBold
              )
              val subtitle = if (connected) {
                val s = serverStatus
                if (s != null) {
                  "v${s.version} -- " +
                    "${s.activeTasks} active, " +
                    "${s.totalTasks} total"
                } else "Connecting..."
              } else {
                "Disconnected"
              }
              Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (connected) {
                  MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                  MaterialTheme.colorScheme.error
                }
              )
            }
          },
          actions = {
            Button(
              onClick = { showAddDialog = true },
              enabled = connected
            ) {
              Text("+ Add")
            }
          }
        )
      }
    ) { paddingValues ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      ) {
        if (errorMessage != null) {
          Card(
            colors = CardDefaults.cardColors(
              containerColor =
                MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Text(
              text = errorMessage ?: "",
              modifier = Modifier.padding(16.dp),
              style = MaterialTheme.typography.bodySmall,
              color =
                MaterialTheme.colorScheme.onErrorContainer
            )
          }
        }

        if (tasks.isEmpty() && connected) {
          EmptyState(
            modifier = Modifier.fillMaxSize(),
            onAddClick = { showAddDialog = true }
          )
        } else {
          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement =
              Arrangement.spacedBy(12.dp)
          ) {
            items(
              items = tasks,
              key = { it.taskId }
            ) { task ->
              TaskCard(
                task = task,
                onPause = {
                  scope.launch {
                    runCatching {
                      client.pauseTask(task.taskId)
                    }
                  }
                },
                onResume = {
                  scope.launch {
                    runCatching {
                      client.resumeTask(task.taskId)
                    }
                  }
                },
                onCancel = {
                  scope.launch {
                    runCatching {
                      client.cancelTask(task.taskId)
                    }
                  }
                },
                onRemove = {
                  scope.launch {
                    runCatching {
                      client.removeTask(task.taskId)
                    }
                  }
                }
              )
            }
          }
        }
      }
    }

    if (showAddDialog) {
      AddDownloadDialog(
        onDismiss = { showAddDialog = false },
        onSubmit = { url, dir, fileName, priority, conns ->
          showAddDialog = false
          scope.launch {
            runCatching {
              client.createDownload(
                CreateDownloadRequest(
                  url = url,
                  directory = dir,
                  fileName = fileName.ifBlank { null },
                  connections = conns,
                  priority = priority
                )
              )
            }.onFailure { e ->
              errorMessage = e.message
                ?: "Failed to create download"
            }
          }
        }
      )
    }
  }
}

@Composable
private fun EmptyState(
  modifier: Modifier = Modifier,
  onAddClick: () -> Unit
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = "No downloads",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = "Add a URL to start downloading",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))
      Button(onClick = onAddClick) {
        Text("Add download")
      }
    }
  }
}

@Composable
private fun TaskCard(
  task: TaskResponse,
  onPause: () -> Unit,
  onResume: () -> Unit,
  onCancel: () -> Unit,
  onRemove: () -> Unit
) {
  val fileName = task.fileName
    ?: extractFilename(task.url).ifBlank { "download" }
  val isActive = task.state == "downloading" ||
    task.state == "pending"
  val isPaused = task.state == "paused"
  val isTerminal = task.state == "completed" ||
    task.state == "failed" ||
    task.state == "canceled"

  Card(
    elevation = CardDefaults.cardElevation(
      defaultElevation = 2.dp
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
          Arrangement.spacedBy(12.dp)
      ) {
        StatusDot(task.state)
        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment =
              Alignment.CenterVertically,
            horizontalArrangement =
              Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = fileName,
              style =
                MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(
                1f, fill = false
              )
            )
            if (task.priority != "NORMAL") {
              Box(
                modifier = Modifier
                  .background(
                    MaterialTheme.colorScheme
                      .tertiaryContainer,
                    MaterialTheme.shapes.small
                  )
                  .padding(
                    horizontal = 6.dp,
                    vertical = 2.dp
                  )
              ) {
                Text(
                  text = task.priority,
                  style = MaterialTheme
                    .typography.labelSmall,
                  color = MaterialTheme.colorScheme
                    .onTertiaryContainer
                )
              }
            }
          }
          Text(
            text = task.url,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme
              .onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Progress and state details
      when (task.state) {
        "downloading" -> {
          val p = task.progress
          if (p != null) {
            LinearProgressIndicator(
              progress = { p.percent },
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = buildProgressText(p),
              style =
                MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme
                .onSurfaceVariant
            )
          } else {
            LinearProgressIndicator(
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
        "pending" -> {
          LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            text = "Preparing...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme
              .onSurfaceVariant
          )
        }
        "queued" -> Text(
          text = "Queued -- waiting for slot",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme
            .onSurfaceVariant
        )
        "scheduled" -> Text(
          text = "Scheduled",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme
            .onSurfaceVariant
        )
        "paused" -> {
          val p = task.progress
          if (p != null && p.totalBytes > 0) {
            LinearProgressIndicator(
              progress = { p.percent },
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = "Paused -- " +
                "${(p.percent * 100).toInt()}% " +
                "(${formatBytes(p.downloadedBytes)}" +
                " / ${formatBytes(p.totalBytes)})",
              style =
                MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme
                .onSurfaceVariant
            )
          } else {
            Text(
              text = "Paused",
              style =
                MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme
                .onSurfaceVariant
            )
          }
        }
        "completed" -> Text(
          text = "Completed",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.tertiary
        )
        "failed" -> Text(
          text = "Failed: ${task.error ?: "Unknown"}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        "canceled" -> Text(
          text = "Canceled",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme
            .onSurfaceVariant
        )
      }

      // Action buttons
      Row(
        horizontalArrangement =
          Arrangement.spacedBy(8.dp)
      ) {
        if (isActive) {
          FilledTonalButton(onClick = onPause) {
            Text("Pause")
          }
          TextButton(onClick = onCancel) {
            Text(
              "Cancel",
              color = MaterialTheme.colorScheme.error
            )
          }
        }
        if (isPaused) {
          FilledTonalButton(onClick = onResume) {
            Text("Resume")
          }
          TextButton(onClick = onCancel) {
            Text(
              "Cancel",
              color = MaterialTheme.colorScheme.error
            )
          }
        }
        if (isTerminal) {
          if (task.state == "failed" ||
            task.state == "canceled"
          ) {
            FilledTonalButton(onClick = onResume) {
              Text("Retry")
            }
          }
          TextButton(onClick = onRemove) {
            Text(
              "Remove",
              color = MaterialTheme.colorScheme.error
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StatusDot(state: String) {
  val bgColor = when (state) {
    "downloading", "pending" ->
      MaterialTheme.colorScheme.primaryContainer
    "completed" ->
      MaterialTheme.colorScheme.tertiaryContainer
    "failed" ->
      MaterialTheme.colorScheme.errorContainer
    "paused" ->
      MaterialTheme.colorScheme.secondaryContainer
    else ->
      MaterialTheme.colorScheme.surfaceVariant
  }
  val fgColor = when (state) {
    "downloading", "pending" ->
      MaterialTheme.colorScheme.onPrimaryContainer
    "completed" ->
      MaterialTheme.colorScheme.onTertiaryContainer
    "failed" ->
      MaterialTheme.colorScheme.onErrorContainer
    "paused" ->
      MaterialTheme.colorScheme.onSecondaryContainer
    else ->
      MaterialTheme.colorScheme.onSurfaceVariant
  }
  val label = when (state) {
    "downloading" -> "DL"
    "pending" -> ".."
    "queued" -> "Q"
    "scheduled" -> "SC"
    "paused" -> "||"
    "completed" -> "OK"
    "failed" -> "!!"
    "canceled" -> "X"
    else -> "--"
  }
  Box(
    modifier = Modifier
      .size(40.dp)
      .clip(CircleShape)
      .background(bgColor),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = fgColor
    )
  }
}

private val priorities = listOf(
  "LOW", "NORMAL", "HIGH", "URGENT"
)

@Composable
private fun AddDownloadDialog(
  onDismiss: () -> Unit,
  onSubmit: (
    url: String,
    directory: String,
    fileName: String,
    priority: String,
    connections: Int
  ) -> Unit
) {
  var url by remember { mutableStateOf("") }
  var directory by remember {
    mutableStateOf("downloads")
  }
  var fileName by remember { mutableStateOf("") }
  var priority by remember { mutableStateOf("NORMAL") }
  var connections by remember { mutableStateOf("4") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add download") },
    text = {
      Column(
        verticalArrangement =
          Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = url,
          onValueChange = {
            url = it
            if (fileName.isBlank()) {
              fileName = extractFilename(it)
            }
          },
          modifier = Modifier.fillMaxWidth(),
          label = { Text("URL") },
          singleLine = true,
          placeholder = {
            Text("https://example.com/file.zip")
          }
        )
        OutlinedTextField(
          value = directory,
          onValueChange = { directory = it },
          modifier = Modifier.fillMaxWidth(),
          label = { Text("Directory") },
          singleLine = true
        )
        OutlinedTextField(
          value = fileName,
          onValueChange = { fileName = it },
          modifier = Modifier.fillMaxWidth(),
          label = { Text("File name (optional)") },
          singleLine = true
        )
        OutlinedTextField(
          value = connections,
          onValueChange = { connections = it },
          modifier = Modifier.fillMaxWidth(),
          label = { Text("Connections") },
          singleLine = true
        )
        Text(
          text = "Priority",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme
            .onSurfaceVariant
        )
        Row(
          horizontalArrangement =
            Arrangement.spacedBy(8.dp)
        ) {
          priorities.forEach { p ->
            FilterChip(
              selected = priority == p,
              onClick = { priority = p },
              label = { Text(p) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (url.isNotBlank()) {
            onSubmit(
              url.trim(),
              directory.trim(),
              fileName.trim(),
              priority,
              connections.toIntOrNull() ?: 4
            )
          }
        },
        enabled = url.isNotBlank()
      ) {
        Text("Download")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

private fun buildProgressText(
  p: com.linroid.kdown.examples.client.ProgressResponse
): String {
  return buildString {
    append("${(p.percent * 100).toInt()}%")
    append(" -- ${formatBytes(p.downloadedBytes)}")
    append(" / ${formatBytes(p.totalBytes)}")
    if (p.bytesPerSecond > 0) {
      append(
        " -- ${formatBytes(p.bytesPerSecond)}/s"
      )
    }
  }
}

private fun extractFilename(url: String): String {
  return url.trim()
    .substringBefore("?")
    .substringBefore("#")
    .trimEnd('/')
    .substringAfterLast("/")
}

private fun formatBytes(bytes: Long): String {
  if (bytes < 0) return "--"
  val kb = 1024L
  val mb = kb * 1024
  val gb = mb * 1024
  return when {
    bytes < kb -> "$bytes B"
    bytes < mb -> {
      val tenths = (bytes * 10 + kb / 2) / kb
      "${tenths / 10}.${tenths % 10} KB"
    }
    bytes < gb -> {
      val tenths = (bytes * 10 + mb / 2) / mb
      "${tenths / 10}.${tenths % 10} MB"
    }
    else -> {
      val hundredths = (bytes * 100 + gb / 2) / gb
      "${hundredths / 100}.${
        (hundredths % 100).toString().padStart(2, '0')
      } GB"
    }
  }
}
