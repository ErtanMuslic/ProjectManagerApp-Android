package com.ertan.projecrmanagerapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ertan.projecrmanagerapp.data.local.TokenManager
import com.ertan.projecrmanagerapp.data.model.CommentResponse
import com.ertan.projecrmanagerapp.data.model.UpdateCardRequest
import com.ertan.projecrmanagerapp.data.model.UserSummary
import com.ertan.projecrmanagerapp.ui.components.ErrorState
import com.ertan.projecrmanagerapp.ui.components.LoadingState
import com.ertan.projecrmanagerapp.viewmodel.CardDetailUiState
import com.ertan.projecrmanagerapp.viewmodel.CardDetailViewModel
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    boardId: Int,
    cardId: Int,
    onBack: () -> Unit,
    viewModel: CardDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val role by tokenManager.getRole().collectAsState(initial = null)
    val currentUserId by tokenManager.getUserId().collectAsState(initial = null)
    val isAdmin = role == "Admin"
    val isGuest = role == null
    val seniority by tokenManager.getSeniority().collectAsState(initial = null)
    val canEditCards = isAdmin || seniority == "Senior"

    var showMoveDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(cardId) {
        viewModel.load(boardId, cardId)
    }

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Card details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if(canEditCards){
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete card")
                    }
                        }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            is CardDetailUiState.Loading -> LoadingState()
            is CardDetailUiState.Error -> ErrorState(message = s.message, onRetry = {})
            is CardDetailUiState.Success -> {
                var title by remember(s.card.id) { mutableStateOf(s.card.title) }
                var description by remember(s.card.id) { mutableStateOf(s.card.description ?: "") }
                var priority by remember(s.card.id) { mutableStateOf(s.card.priority) }
                var assignedUserId by remember(s.card.id) { mutableStateOf(s.card.assignedUserId) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { if (canEditCards) title = it },
                            label = { Text("Title") },
                            readOnly = !canEditCards,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { if(canEditCards) description = it },
                            label = { Text("Description") },
                            readOnly = !canEditCards,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }

                    item {
                        Text("Priority", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        SingleChoiceSegmentedButtonRow {
                            listOf("Low", "Medium", "High").forEachIndexed { index, option ->
                                SegmentedButton(
                                    selected = priority == option,
                                    onClick = { priority = option },
                                    enabled = canEditCards,
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                                ) {
                                    Text(option)
                                }
                            }
                        }
                    }

                    item {
                        Text("Due date", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(onClick = { showDatePicker = true },
                            enabled = canEditCards) {
                            Text(formatDueDate(s.card.dueDate) ?: "Set due date")
                        }
                    }

                    item {
                        Text("Assignee", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        AssigneeDropdown(
                            users = s.users,
                            selectedUserId = assignedUserId,
                            enabled = canEditCards,
                            onSelect = { assignedUserId = it }
                        )
                    }

                    item {
                        if(canEditCards) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    viewModel.updateCard(
                                        UpdateCardRequest(
                                            title = title,
                                            description = description,
                                            assignedUserId = assignedUserId,
                                            priority = priority
                                        )
                                    ) {}
                                }) {
                                    Text("Save changes")
                                }
                                OutlinedButton(onClick = { showMoveDialog = true }) {
                                    Text("Move to...")
                                }
                            }
                        }
                    }
                    item {
                        val context = LocalContext.current
                        if (!isGuest) {
                            if (s.card.assignedUserId == currentUserId) {
                                OutlinedButton(onClick = {
                                    viewModel.unassignMe { success ->
                                        Toast.makeText(context, if (success) "Unassigned." else "Failed to unassign.", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Text("Unassign myself")
                                }
                            } else if (s.card.assignedUserId == null) {
                                Button(onClick = {
                                    viewModel.assignToMe { success, error ->
                                        Toast.makeText(context, if (success) "Assigned to you." else (error ?: "Failed."), Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Text("Assign to me")
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Comments (${s.comments.size})", style = MaterialTheme.typography.titleMedium)
                    }

                    items(s.comments) { comment ->
                        CommentItem(
                            comment = comment,
                            canDelete = isAdmin || comment.userId == currentUserId,
                            onDelete = { viewModel.deleteComment(comment.id) }
                        )
                    }

                    item {
                        if(!isGuest) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    label = { Text("Add a comment") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    if (commentText.isNotBlank()) {
                                        viewModel.addComment(commentText)
                                        commentText = ""
                                    }
                                }) {
                                    Icon(Icons.Default.Send, contentDescription = "Send comment")
                                }
                            }
                        }
                    }
                }

                if (showMoveDialog) {
                    val leafColumns = flattenLeafColumns(s.allColumns)
                    MoveCardDialog(
                        card = s.card,
                        leafColumns = leafColumns,
                        onDismiss = { showMoveDialog = false },
                        onMove = { targetColumnId ->
                            viewModel.moveCard(targetColumnId)
                            showMoveDialog = false
                        }
                    )
                }

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val millis = datePickerState.selectedDateMillis
                                if (millis != null) {
                                    val isoDate = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneOffset.UTC)
                                        .format(DateTimeFormatter.ISO_INSTANT)
                                    viewModel.updateCard(UpdateCardRequest(dueDate = isoDate)) {}
                                }
                                showDatePicker = false
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("Delete card?") },
                        text = { Text("This action cannot be undone.") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteCard { success ->
                                    showDeleteConfirm = false
                                    if (success) onBack()
                                }
                            }) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssigneeDropdown(
    users: List<UserSummary>,
    selectedUserId: Int?,
    enabled: Boolean,
    onSelect: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = users.find { it.id == selectedUserId }?.name ?: "Unassigned"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if(enabled) expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Unassigned") },
                onClick = { onSelect(null); expanded = false }
            )
            users.forEach { user ->
                DropdownMenuItem(
                    text = { Text(user.name) },
                    onClick = { onSelect(user.id); expanded = false }
                )
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommentResponse, canDelete: Boolean, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = comment.userName, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
            }
            if (canDelete) {
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete comment")
                }
            }
        }
    }
}

// Formats an ISO-8601 date string (e.g. from the backend) into a short readable date
fun formatDueDate(isoDate: String?): String? {
    if (isoDate == null) return null
    return try {
        val instant = Instant.parse(isoDate)
        DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneOffset.UTC).format(instant)
    } catch (e: Exception) {
        isoDate
    }
}