package com.ertan.projecrmanagerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ertan.projecrmanagerapp.data.model.MyTaskResponse
import com.ertan.projecrmanagerapp.ui.components.EmptyState
import com.ertan.projecrmanagerapp.ui.components.ErrorState
import com.ertan.projecrmanagerapp.ui.components.LoadingState
import com.ertan.projecrmanagerapp.ui.theme.PriorityHigh
import com.ertan.projecrmanagerapp.ui.theme.PriorityLow
import com.ertan.projecrmanagerapp.ui.theme.PriorityMedium
import com.ertan.projecrmanagerapp.viewmodel.MyTasksState
import com.ertan.projecrmanagerapp.viewmodel.MyTasksViewModel
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTasksScreen(
    onBack: () -> Unit,
    onTaskClick: (boardId: Int, cardId: Int) -> Unit,
    viewModel: MyTasksViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is MyTasksState.Loading -> LoadingState()
                is MyTasksState.Error -> ErrorState(message = s.message, onRetry = { viewModel.loadTasks() })
                is MyTasksState.Success -> {
                    if (s.tasks.isEmpty()) {
                        EmptyState(message = "You have no assigned tasks.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(s.tasks) { task ->
                                MyTaskCard(
                                    task = task,
                                    onClick = { onTaskClick(task.boardId, task.cardId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyTaskCard(task: MyTaskResponse, onClick: () -> Unit) {
    val priorityColor = when (task.priority) {
        "High" -> PriorityHigh
        "Low" -> PriorityLow
        else -> PriorityMedium
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)

            if (task.description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = task.description, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${task.boardName} — ${task.columnName}",
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(priorityColor, shape = androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = task.priority, style = MaterialTheme.typography.labelSmall)

                if (task.dueDate != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Due: ${formatDueDate(task.dueDate)}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}