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
import com.ertan.projecrmanagerapp.data.model.CardDetail
import com.ertan.projecrmanagerapp.data.model.ColumnDetail
import com.ertan.projecrmanagerapp.viewmodel.BoardDetailState
import com.ertan.projecrmanagerapp.viewmodel.BoardDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetailScreen(
    boardId: Int,
    onBack: () -> Unit,
    viewModel: BoardDetailViewModel = viewModel()
) {
    var showCreateCardDialog by remember { mutableStateOf<Int?>(null) } // holds target column id

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
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is BoardDetailState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is BoardDetailState.Error -> {
                    Text(text = s.message, modifier = Modifier.align(Alignment.Center))
                }
                is BoardDetailState.Success -> {
                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        items(s.board.columns) { column ->
                            ColumnView(
                                column = column,
                                onAddCard = { columnId -> showCreateCardDialog = columnId }
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
    }
}

@Composable
fun ColumnView(column: ColumnDetail, onAddCard: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .padding(horizontal = 6.dp)
    ) {
        Text(text = column.name, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (column.subColumns.isNotEmpty()) {
            // Parent column: show sub-columns side by side, no direct cards here
            LazyRow {
                items(column.subColumns) { subColumn ->
                    Box(modifier = Modifier.width(260.dp).padding(horizontal = 4.dp)) {
                        SubColumnView(subColumn = subColumn, onAddCard = onAddCard)
                    }
                }
            }
        } else {
            // Leaf column: show cards directly
            LazyColumn(modifier = Modifier.heightIn(max = 600.dp)) {
                items(column.cards) { card ->
                    CardItem(card = card)
                }
                item {
                    TextButton(onClick = { onAddCard(column.id) }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add card")
                    }
                }
            }
        }
    }
}

@Composable
fun SubColumnView(subColumn: ColumnDetail, onAddCard: (Int) -> Unit) {
    Column {
        Text(text = subColumn.name, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
            items(subColumn.cards) { card ->
                CardItem(card = card)
            }
            item {
                TextButton(onClick = { onAddCard(subColumn.id) }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add card")
                }
            }
        }
    }
}

@Composable
fun CardItem(card: CardDetail) {
    val priorityColor = when (card.priority) {
        "High" -> Color(0xFFE57373)
        "Low" -> Color(0xFF81C784)
        else -> Color(0xFFFFB74D)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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