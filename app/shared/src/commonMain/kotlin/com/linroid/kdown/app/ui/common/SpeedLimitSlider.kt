package com.linroid.kdown.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linroid.kdown.api.DownloadTask
import com.linroid.kdown.api.SpeedLimit
import com.linroid.kdown.app.util.formatBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val speedSteps = listOf(
  0L, 512 * 1024L, 1_048_576L, 2_097_152L,
  5_242_880L, 10_485_760L
)

@Composable
fun SpeedLimitIcon(
  active: Boolean,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  IconButton(
    onClick = onClick,
    modifier = modifier.size(28.dp),
    colors = IconButtonDefaults.iconButtonColors(
      contentColor = if (active || selected) {
        MaterialTheme.colorScheme.primary
      } else {
        MaterialTheme.colorScheme.onSurfaceVariant
      }
    )
  ) {
    Icon(
      Icons.Filled.Speed,
      contentDescription = "Speed limit",
      modifier = Modifier.size(16.dp)
    )
  }
}

@Composable
fun SpeedLimitPanel(
  task: DownloadTask,
  scope: CoroutineScope,
  modifier: Modifier = Modifier
) {
  val initial = task.request.speedLimit.bytesPerSecond
  val initialIndex = speedSteps
    .indexOfLast { it <= initial }
    .coerceAtLeast(0).toFloat()
  var sliderValue by remember { mutableStateOf(initialIndex) }

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = "Limit:",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Slider(
      value = sliderValue,
      onValueChange = { sliderValue = it },
      onValueChangeFinished = {
        val idx = sliderValue.toInt()
          .coerceIn(0, speedSteps.lastIndex)
        val bps = speedSteps[idx]
        val limit = if (bps == 0L) SpeedLimit.Unlimited
        else SpeedLimit.of(bps)
        scope.launch { task.setSpeedLimit(limit) }
      },
      valueRange = 0f..speedSteps.lastIndex.toFloat(),
      steps = speedSteps.size - 2,
      modifier = Modifier.weight(1f)
    )
    val idx = sliderValue.toInt()
      .coerceIn(0, speedSteps.lastIndex)
    Text(
      text = if (speedSteps[idx] == 0L) "Unlimited"
      else "${formatBytes(speedSteps[idx])}/s",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
