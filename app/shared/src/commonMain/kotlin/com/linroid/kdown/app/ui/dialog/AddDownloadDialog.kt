package com.linroid.kdown.app.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linroid.kdown.api.DownloadPriority
import com.linroid.kdown.api.SpeedLimit
import com.linroid.kdown.app.util.extractFilename
import com.linroid.kdown.app.util.priorityLabel

private data class SpeedOption(
  val label: String,
  val limit: SpeedLimit
)

private val speedOptions = listOf(
  SpeedOption("Unlimited", SpeedLimit.Unlimited),
  SpeedOption("1 MB/s", SpeedLimit.mbps(1)),
  SpeedOption("5 MB/s", SpeedLimit.mbps(5)),
  SpeedOption("10 MB/s", SpeedLimit.mbps(10))
)

@Composable
fun AddDownloadDialog(
  onDismiss: () -> Unit,
  onDownload: (
    url: String,
    fileName: String,
    SpeedLimit,
    DownloadPriority
  ) -> Unit
) {
  var url by remember { mutableStateOf("") }
  var fileName by remember { mutableStateOf("") }
  var selectedSpeed by remember {
    mutableStateOf(SpeedLimit.Unlimited)
  }
  var selectedPriority by remember {
    mutableStateOf(DownloadPriority.NORMAL)
  }
  val isValidUrl = url.isBlank() ||
    url.trim().startsWith("http://") ||
    url.trim().startsWith("https://")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add download") },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = url,
          onValueChange = {
            url = it
            fileName = extractFilename(it)
          },
          modifier = Modifier.fillMaxWidth(),
          label = { Text("URL") },
          singleLine = true,
          placeholder = {
            Text("https://example.com/file.zip")
          },
          isError = !isValidUrl,
          supportingText = if (!isValidUrl) {
            {
              Text(
                "URL must start with " +
                  "http:// or https://"
              )
            }
          } else {
            null
          }
        )
        OutlinedTextField(
          value = fileName,
          onValueChange = { fileName = it },
          modifier = Modifier.fillMaxWidth(),
          label = { Text("Save as") },
          singleLine = true,
          placeholder = {
            Text("Auto-detected from URL")
          },
          supportingText = if (fileName.isBlank() &&
            url.isNotBlank()
          ) {
            { Text("Will be extracted from URL") }
          } else {
            null
          }
        )
        Text(
          text = "Priority",
          style = MaterialTheme.typography.labelMedium,
          color =
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          horizontalArrangement =
            Arrangement.spacedBy(8.dp)
        ) {
          DownloadPriority.entries.forEach { priority ->
            FilterChip(
              selected = selectedPriority == priority,
              onClick = { selectedPriority = priority },
              label = {
                Text(priorityLabel(priority))
              }
            )
          }
        }
        Text(
          text = "Speed limit",
          style = MaterialTheme.typography.labelMedium,
          color =
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          horizontalArrangement =
            Arrangement.spacedBy(8.dp)
        ) {
          speedOptions.forEach { option ->
            FilterChip(
              selected = selectedSpeed == option.limit,
              onClick = {
                selectedSpeed = option.limit
              },
              label = { Text(option.label) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val trimmed = url.trim()
          if (trimmed.isNotEmpty()) {
            onDownload(
              trimmed, fileName.trim(),
              selectedSpeed, selectedPriority
            )
          }
        },
        enabled = url.isNotBlank() && isValidUrl
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
