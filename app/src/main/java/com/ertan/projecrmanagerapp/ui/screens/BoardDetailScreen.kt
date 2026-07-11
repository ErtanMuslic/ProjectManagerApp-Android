package com.ertan.projecrmanagerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.ertan.projecrmanagerapp.data.local.TokenManager
import com.ertan.projecrmanagerapp.data.model.CardDetail
import com.ertan.projecrmanagerapp.data.model.ColumnDetail
import com.ertan.projecrmanagerapp.ui.components.ErrorState
import com.ertan.projecrmanagerapp.ui.components.LoadingState
import com.ertan.projecrmanagerapp.ui.theme.PriorityHigh
import com.ertan.projecrmanagerapp.ui.theme.PriorityLow
import com.ertan.projecrmanagerapp.ui.theme.PriorityMedium
import com.ertan.projecrmanagerapp.viewmodel.BoardDetailState
import com.ertan.projecrmanagerapp.viewmodel.BoardDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetailScreen(
    boardId: Int,
    onBack: () -> Unit,
    onCardClick: (Int) -> Unit,
    viewModel: BoardDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val role by tokenManager.getRole().collectAsState(initial = null)
    val isAdmin = role == "Admin"
    val isGuest = role == null

    var showCreateCardDialog by remember { mutableStateOf<Int?>(null) }
    var cardToMove by remember { mutableStateOf<CardDetail?>(null) }
    var showCreateColumnDialog by remember { mutableStateOf(false) }
    var showCreateSubColumnDialog by remember { mutableStateOf<Int?>(null) } // holds parent column id

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
                is BoardDetailState.Error -> ErrorState(message = s.message, onRetry = {})
                is BoardDetailState.Success -> {
                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        items(s.board.columns) { column ->
                            ColumnView(
                                column = column,
                                isAdmin = isAdmin,
                                isGuest = isGuest,
                                onAddCard = { columnId -> if(!isGuest) showCreateCardDialog = columnId },
                                onCardClick = { card -> onCardClick(card.id) },
                                onAddSubColumn = { parentId -> showCreateSubColumnDialog = parentId }
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
                onCreate = { name ->
                    viewModel.createColumn(name, null) {
                        showCreateColumnDialog = false
                    }
                }
            )
        }

        showCreateSubColumnDialog?.let { parentId ->
            CreateColumnDialog(
                isSubColumn = true,
                onDismiss = { showCreateSubColumnDialog = null },
                onCreate = { name ->
                    viewModel.createColumn(name, parentId) {
                        showCreateSubColumnDialog = null
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
    onAddCard: (Int) -> Unit,
    onCardClick: (CardDetail) -> Unit,
    onAddSubColumn: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .padding(horizontal = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = column.name, style = MaterialTheme.typography.titleMedium)

            // Only offer "add sub-column" for admin, and only if this column has no direct cards yet
            // (avoids the edge case of orphaning existing cards when a leaf column becomes a parent)
            if (isAdmin && column.cards.isEmpty()) {
                IconButton(onClick = { onAddSubColumn(column.id) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add sub-column")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (column.subColumns.isNotEmpty()) {
            LazyRow {
                items(column.subColumns) { subColumn ->
                    Box(modifier = Modifier.width(260.dp).padding(horizontal = 4.dp)) {
                        SubColumnView(subColumn = subColumn, onAddCard = onAddCard, onCardClick = onCardClick, isGuest = isGuest)
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 600.dp)) {
                items(column.cards) { card ->
                    CardItem(card = card, onClick = { onCardClick(card) })
                }
                if(!isGuest) {
                    item {
                        TextButton(onClick = { onAddCard(column.id) }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add card")
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
    onAddCard: (Int) -> Unit,
    onCardClick: (CardDetail) -> Unit
) {
    Column {
        Text(text = subColumn.name, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
            items(subColumn.cards) { card ->
                CardItem(card = card, onClick = { onCardClick(card) })
            }
            if(!isGuest) {
                item {
                    TextButton(onClick = { onAddCard(subColumn.id) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add card")
                    }
                }
            }
        }
    }
}

@Composable
fun CardItem(card: CardDetail, onClick: () -> Unit) {
    val priorityColor = when (card.priority) {
        "High" -> PriorityHigh
        "Low" -> PriorityLow
        else -> PriorityMedium
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = card.title, style = MaterialTheme.typography.bodyLarge)

            if (card.description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = card.description, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(priorityColor, shape = androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = card.priority, style = MaterialTheme.typography.labelSmall)

                if (card.assignedUserName != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "@${card.assignedUserName}", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (card.commentCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${card.commentCount} comments", style = MaterialTheme.typography.labelSmall)
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
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isSubColumn) "Add sub-column" else "Add column") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Column name") }
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onCreate(name) }) {
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