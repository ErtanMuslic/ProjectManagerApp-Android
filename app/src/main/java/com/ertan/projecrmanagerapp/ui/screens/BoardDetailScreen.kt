package com.ertan.projecrmanagerapp.ui.screens


import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.ertan.projecrmanagerapp.data.local.TokenManager
import com.ertan.projecrmanagerapp.data.model.CardDetail
import com.ertan.projecrmanagerapp.data.model.ColumnDetail
import com.ertan.projecrmanagerapp.ui.components.ErrorState
import com.ertan.projecrmanagerapp.ui.components.LoadingState
import com.ertan.projecrmanagerapp.viewmodel.BoardDetailState
import com.ertan.projecrmanagerapp.viewmodel.BoardDetailViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ChatBubbleOutline
import com.ertan.projecrmanagerapp.ui.components.Avatar
import com.ertan.projecrmanagerapp.ui.components.PriorityBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetailScreen(
    boardId: Int,
    onBack: () -> Unit,
    onCardClick: (Int) -> Unit,
    onMyTasks: () -> Unit,
    viewModel: BoardDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val role by tokenManager.getRole().collectAsState(initial = null)
    val isAdmin = role == "Admin"
    val isGuest = role == null

    var showCreateCardDialog by remember { mutableStateOf<Int?>(null) }
    var showCreateColumnDialog by remember { mutableStateOf(false) }
    var showCreateSubColumnDialog by remember { mutableStateOf<Int?>(null) }
    var columnToEditLimit by remember { mutableStateOf<ColumnDetail?>(null) }

    LaunchedEffect(boardId) {
        viewModel.loadBoard(boardId)
    }

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (val s = state) {
                            is BoardDetailState.Success -> s.board.name
                            else -> "Board"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isGuest) {
                        IconButton(onClick = onMyTasks) {
                            Icon(Icons.Default.Assignment, contentDescription = "My tasks")
                        }
                    }
                    if (isAdmin) {
                        IconButton(onClick = { showCreateColumnDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add column")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is BoardDetailState.Loading -> LoadingState()
                is BoardDetailState.Error -> ErrorState(message = s.message, onRetry = { viewModel.loadBoard(boardId) })
                is BoardDetailState.Success -> {
                    val sortedColumns = s.board.columns.sortedBy { it.order }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sortedColumns) { column ->
                            ColumnView(
                                column = column,
                                isAdmin = isAdmin,
                                isGuest = isGuest,
                                isFirst = column.id == sortedColumns.first().id,
                                isLast = column.id == sortedColumns.last().id,
                                onAddCard = { columnId -> showCreateCardDialog = columnId },
                                onCardClick = { card -> onCardClick(card.id) },
                                onEditLimit = { col -> columnToEditLimit = col },
                                onAddSubColumn = { parentId -> showCreateSubColumnDialog = parentId },
                                onMoveLeft = { columnId -> viewModel.moveColumnLeft(columnId) },
                                onMoveRight = { columnId -> viewModel.moveColumnRight(columnId) }
                            )
                        }
                    }
                }
            }
        }

        showCreateCardDialog?.let { columnId ->
            CreateCardDialog(
                onDismiss = { showCreateCardDialog = null },
                onCreate = { title ->
                    viewModel.createCard(columnId, title) {
                        showCreateCardDialog = null
                    }
                }
            )
        }

        if (showCreateColumnDialog) {
            CreateColumnDialog(
                isSubColumn = false,
                onDismiss = { showCreateColumnDialog = false },
                onCreate = { name, cardLimit ->
                    viewModel.createColumn(name, null, cardLimit) {
                        showCreateColumnDialog = false
                    }
                }
            )
        }

        showCreateSubColumnDialog?.let { parentId ->
            CreateColumnDialog(
                isSubColumn = true,
                onDismiss = { showCreateSubColumnDialog = null },
                onCreate = { name, cardLimit ->
                    viewModel.createColumn(name, parentId, cardLimit) {
                        showCreateSubColumnDialog = null
                    }
                }
            )
        }

        columnToEditLimit?.let { column ->
            EditColumnDialog(
                column = column,
                onDismiss = { columnToEditLimit = null },
                onSave = { name, limit ->
                    viewModel.updateColumn(column.id, name, limit) {
                        columnToEditLimit = null
                    }
                },
                onDelete = {
                    viewModel.deleteColumn(column.id) {
                        columnToEditLimit = null
                    }
                }
            )
        }
    }
}

@Composable
fun ColumnView(
    column: ColumnDetail,
    isAdmin: Boolean,
    isGuest: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onAddCard: (Int) -> Unit,
    onCardClick: (CardDetail) -> Unit,
    onAddSubColumn: (Int) -> Unit,
    onEditLimit: (ColumnDetail) -> Unit,
    onMoveLeft: (Int) -> Unit,
    onMoveRight: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = column.name, style = MaterialTheme.typography.titleMedium)
                    if (column.cardLimit != null && column.subColumns.isEmpty()) {
                        val isFull = column.cards.size >= column.cardLimit
                        Text(
                            text = "${column.cards.size}/${column.cardLimit} cards",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isAdmin) {
                    IconButton(
                        onClick = { onMoveLeft(column.id) },
                        enabled = !isFirst,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Move up")
                    }
                    IconButton(
                        onClick = { onMoveRight(column.id) },
                        enabled = !isLast,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Move down")
                    }
                    if (column.subColumns.isEmpty()) {
                        IconButton(onClick = { onEditLimit(column) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Settings, contentDescription = "Edit WIP limit")
                        }
                    }
                    if (column.cards.isEmpty()) {
                        IconButton(onClick = { onAddSubColumn(column.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add sub-column")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val isFull = column.cardLimit != null && column.cards.size >= column.cardLimit

            if (column.subColumns.isNotEmpty()) {
                val sortedSubColumns = column.subColumns.sortedBy { it.order }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sortedSubColumns.forEach { subColumn ->
                        SubColumnView(
                            subColumn = subColumn,
                            isGuest = isGuest,
                            isAdmin = isAdmin,
                            isFirst = subColumn.id == sortedSubColumns.first().id,
                            isLast = subColumn.id == sortedSubColumns.last().id,
                            onAddCard = onAddCard,
                            onCardClick = onCardClick,
                            onEditLimit = onEditLimit,
                            onMoveLeft = onMoveLeft,
                            onMoveRight = onMoveRight
                        )
                    }
                }
            } else {
                Column {
                    column.cards.forEach { card ->
                        CardItem(card = card, onClick = { onCardClick(card) })
                    }
                    if (!isGuest) {
                        TextButton(
                            onClick = { onAddCard(column.id) },
                            enabled = !isFull
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isFull) "Column full" else "Add card")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubColumnView(
    subColumn: ColumnDetail,
    isGuest: Boolean,
    isAdmin: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onAddCard: (Int) -> Unit,
    onCardClick: (CardDetail) -> Unit,
    onEditLimit: (ColumnDetail) -> Unit,
    onMoveLeft: (Int) -> Unit,
    onMoveRight: (Int) -> Unit
) {
    val isFull = subColumn.cardLimit != null && subColumn.cards.size >= subColumn.cardLimit

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp), // indent to visually show it's nested under the parent column
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = subColumn.name, style = MaterialTheme.typography.titleSmall)
                    if (subColumn.cardLimit != null) {
                        Text(
                            text = "${subColumn.cards.size}/${subColumn.cardLimit} cards",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isAdmin) {
                    IconButton(
                        onClick = { onMoveLeft(subColumn.id) },
                        enabled = !isFirst,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Move up", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onMoveRight(subColumn.id) },
                        enabled = !isLast,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Move down", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onEditLimit(subColumn) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Settings, contentDescription = "Edit WIP limit", modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                subColumn.cards.forEach { card ->
                    CardItem(card = card, onClick = { onCardClick(card) })
                }
                if (!isGuest) {
                    TextButton(
                        onClick = { onAddCard(subColumn.id) },
                        enabled = !isFull
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFull) "Column full" else "Add card")
                    }
                }
            }
        }
    }
}

@Composable
fun CardItem(card: CardDetail, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            PriorityBadge(priority = card.priority)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = card.title, style = MaterialTheme.typography.titleSmall)

            if (card.description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (card.dueDate != null) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDueDate(card.dueDate) ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (card.commentCount > 0) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${card.commentCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (card.assignedUserName != null && card.assignedUserId != null) {
                    Avatar(name = card.assignedUserName, userId = card.assignedUserId, size = 26.dp)
                }
            }
        }
    }
}

@Composable
fun CreateCardDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add card") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Card title") }
            )
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onCreate(title) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Flattens the column tree into a list of leaf columns only (columns that can hold cards directly)
fun flattenLeafColumns(columns: List<ColumnDetail>): List<ColumnDetail> {
    val result = mutableListOf<ColumnDetail>()
    for (column in columns) {
        if (column.subColumns.isEmpty()) {
            result.add(column)
        } else {
            result.addAll(flattenLeafColumns(column.subColumns))
        }
    }
    return result
}

@Composable
fun MoveCardDialog(
    card: CardDetail,
    leafColumns: List<ColumnDetail>,
    onDismiss: () -> Unit,
    onMove: (targetColumnId: Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move \"${card.title}\" to...") },
        text = {
            LazyColumn {
                items(leafColumns) { column ->
                    TextButton(
                        onClick = { onMove(column.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = column.name, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateColumnDialog(
    isSubColumn: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, cardLimit: Int?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isSubColumn) "Add sub-column" else "Add column") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Column name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { c -> c.isDigit() } },
                    label = { Text("WIP limit (optional)") },
                    supportingText = { Text("Leave empty for no limit") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onCreate(name, limitText.toIntOrNull())
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditColumnDialog(
    column: ColumnDetail,
    onDismiss: () -> Unit,
    onSave: (name: String, limit: Int?) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(column.name) }
    var limitText by remember { mutableStateOf(column.cardLimit?.toString() ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit column") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Column name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { c -> c.isDigit() } },
                    label = { Text("WIP limit (empty = no limit)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete this column")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name, limitText.toIntOrNull()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete \"${column.name}\"?") },
            text = { Text("This will also delete all cards (and sub-columns, if any) inside it. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
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