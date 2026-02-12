package com.linroid.kdown.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linroid.kdown.api.DownloadState
import com.linroid.kdown.app.backend.BackendManager
import com.linroid.kdown.app.state.AppState
import com.linroid.kdown.app.state.StatusFilter
import com.linroid.kdown.app.ui.dialog.AddDownloadDialog
import com.linroid.kdown.app.ui.dialog.AddRemoteServerDialog
import com.linroid.kdown.app.ui.dialog.BackendSelectorSheet
import com.linroid.kdown.app.ui.list.DownloadList
import com.linroid.kdown.app.ui.sidebar.SidebarNavigation
import com.linroid.kdown.app.ui.sidebar.SpeedStatusBar
import com.linroid.kdown.app.ui.toolbar.BatchActionBar
import com.linroid.kdown.app.ui.toolbar.countTasksByFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(backendManager: BackendManager) {
  val scope = rememberCoroutineScope()
  val appState = remember(backendManager) {
    AppState(backendManager, scope)
  }

  DisposableEffect(Unit) {
    onDispose { backendManager.close() }
  }

  val sortedTasks by appState.sortedTasks.collectAsState()
  val version by appState.version.collectAsState()
  val activeBackend by
    appState.activeBackend.collectAsState()
  val connectionState by
    appState.connectionState.collectAsState()
  val serverState by
    appState.serverState.collectAsState()

  // Collect all task states for filtering/counts
  val taskStates = remember {
    mutableMapOf<String, DownloadState>()
  }
  sortedTasks.forEach { task ->
    val state by task.state.collectAsState()
    taskStates[task.taskId] = state
  }

  val filteredTasks by remember(
    sortedTasks, appState.statusFilter
  ) {
    derivedStateOf {
      if (appState.statusFilter == StatusFilter.All) {
        sortedTasks
      } else {
        sortedTasks.filter { task ->
          val state = taskStates[task.taskId]
          state != null &&
            appState.statusFilter.matches(state)
        }
      }
    }
  }

  val taskCounts by remember(sortedTasks, taskStates) {
    derivedStateOf {
      StatusFilter.entries.associateWith { filter ->
        countTasksByFilter(
          filter, taskStates, sortedTasks.size
        )
      }
    }
  }

  val hasActive by remember(taskStates) {
    derivedStateOf {
      taskStates.values.any { it.isActive }
    }
  }
  val hasPaused by remember(taskStates) {
    derivedStateOf {
      taskStates.values.any {
        it is DownloadState.Paused
      }
    }
  }
  val hasCompleted by remember(taskStates) {
    derivedStateOf {
      taskStates.values.any {
        it is DownloadState.Completed
      }
    }
  }

  val activeDownloadCount by remember(taskStates) {
    derivedStateOf {
      taskStates.values.count { it.isActive }
    }
  }
  val totalSpeed by remember(taskStates) {
    derivedStateOf {
      taskStates.values.sumOf { state ->
        if (state is DownloadState.Downloading) {
          state.progress.bytesPerSecond
        } else {
          0L
        }
      }
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    // Main content: sidebar + list
    Row(modifier = Modifier.weight(1f)) {
      // Left sidebar
      SidebarNavigation(
        selectedFilter = appState.statusFilter,
        taskCounts = taskCounts,
        onFilterSelect = { appState.statusFilter = it },
        onAddClick = {
          appState.showAddDialog = true
        }
      )

      VerticalDivider(
        color = MaterialTheme.colorScheme.outlineVariant
      )

      // Right content area
      Column(modifier = Modifier.weight(1f)) {
        // Top bar with title and batch actions
        TopAppBar(
          title = {
            Text(
              text = appState.statusFilter.label,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
          },
          actions = {
            BatchActionBar(
              hasActiveDownloads = hasActive,
              hasPausedDownloads = hasPaused,
              hasCompletedDownloads = hasCompleted,
              onPauseAll = { appState.pauseAll() },
              onResumeAll = { appState.resumeAll() },
              onClearCompleted = {
                appState.clearCompleted()
              }
            )
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor =
              MaterialTheme.colorScheme.surface
          )
        )

        // Error banner
        if (appState.errorMessage != null) {
          Card(
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme
                .errorContainer
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment =
                Alignment.CenterVertically,
              horizontalArrangement =
                Arrangement.spacedBy(12.dp)
            ) {
              Text(
                text = appState.errorMessage ?: "",
                style =
                  MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme
                  .onErrorContainer,
                modifier = Modifier.weight(1f)
              )
              TextButton(
                onClick = { appState.dismissError() }
              ) {
                Text("Dismiss")
              }
            }
          }
        }

        // Download list
        DownloadList(
          tasks = filteredTasks,
          isEmpty = sortedTasks.isEmpty() &&
            appState.errorMessage == null,
          isFilterEmpty = filteredTasks.isEmpty() &&
            sortedTasks.isNotEmpty(),
          selectedFilter = appState.statusFilter,
          scope = scope,
          onAddClick = {
            appState.showAddDialog = true
          },
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Bottom speed status bar
    SpeedStatusBar(
      activeDownloads = activeDownloadCount,
      totalSpeed = totalSpeed,
      backendLabel = activeBackend?.label,
      connectionState = connectionState,
      onBackendClick = {
        appState.showBackendSelector = true
      }
    )
  }

  // Dialogs
  if (appState.showAddDialog) {
    AddDownloadDialog(
      onDismiss = { appState.showAddDialog = false },
      onDownload = { url, fileName, speedLimit, priority ->
        appState.showAddDialog = false
        appState.dismissError()
        appState.startDownload(
          url, fileName, speedLimit, priority
        )
      }
    )
  }

  if (appState.showBackendSelector) {
    BackendSelectorSheet(
      backendManager = backendManager,
      activeBackendId = activeBackend?.id,
      switchingBackendId = appState.switchingBackendId,
      serverState = serverState,
      onSelectBackend = { entry ->
        appState.switchBackend(entry.id)
      },
      onRemoveBackend = { entry ->
        appState.removeBackend(entry.id)
      },
      onAddRemoteServer = {
        appState.showAddRemoteDialog = true
      },
      onDismiss = {
        appState.showBackendSelector = false
      }
    )
  }

  if (appState.showAddRemoteDialog) {
    AddRemoteServerDialog(
      onDismiss = {
        appState.showAddRemoteDialog = false
      },
      onAdd = { host, port, token ->
        appState.showAddRemoteDialog = false
        appState.addRemoteServer(host, port, token)
      }
    )
  }
}
