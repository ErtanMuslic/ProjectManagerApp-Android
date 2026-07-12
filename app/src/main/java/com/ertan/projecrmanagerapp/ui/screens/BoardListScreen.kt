package com.ertan.projecrmanagerapp.ui.screens

import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ertan.projecrmanagerapp.data.local.TokenManager
import com.ertan.projecrmanagerapp.ui.components.EmptyState
import com.ertan.projecrmanagerapp.ui.components.ErrorState
import com.ertan.projecrmanagerapp.ui.components.LoadingState
import com.ertan.projecrmanagerapp.viewmodel.BoardListState
import com.ertan.projecrmanagerapp.viewmodel.BoardViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.MoreVert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardListScreen(
    onBoardClick: (Int) -> Unit,
    onLogout: () -> Unit,
    onManageUsers: () -> Unit,
    onMyTasks: () -> Unit,
    viewModel: BoardViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()
    val token by tokenManager.getToken().collectAsState(initial = null)

    val role by tokenManager.getRole().collectAsState(initial = null)
    val boardListState by viewModel.boardListState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(

                    title = { Text("My Boards") },
                    actions = {
                        if(token !=null){
                            IconButton(onClick = onMyTasks) {
                                Icon(Icons.Default.Assignment, contentDescription = "My tasks")
                            }
                        }

                        if (role == "Admin") {
                            IconButton(onClick = onManageUsers) {
                                Icon(Icons.Default.Person, contentDescription = "Manage users")
                            }
                        }
                        if(token != null) {
                        IconButton(onClick = {
                            scope.launch {
                                tokenManager.clear()
                                onLogout()
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                        }
                    } else {
                        TextButton(onClick = onLogout) {
                            Text("Login")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (role == "Admin") {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create board")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = boardListState) {
                is BoardListState.Loading -> LoadingState()
                is BoardListState.Error -> ErrorState(message = state.message, onRetry = {viewModel.loadBoards()})
                is BoardListState.Success -> {
                    if (state.boards.isEmpty()) {
                        EmptyState(message = "No boards yet")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.boards) { board ->
                                var showMenu by remember { mutableStateOf(false) }
                                var showEditDialog by remember { mutableStateOf(false) }
                                var showDeleteConfirm by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    onClick = { onBoardClick(board.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = board.name, style = MaterialTheme.typography.titleMedium)

                                        if (role == "Admin") {
                                            Box {
                                                IconButton(onClick = { showMenu = true }) {
                                                    Icon(Icons.Default.MoreVert, contentDescription = "Board options")
                                                }
                                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                                    DropdownMenuItem(
                                                        text = { Text("Edit") },
                                                        onClick = {
                                                            showMenu = false
                                                            showEditDialog = true
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Delete") },
                                                        onClick = {
                                                            showMenu = false
                                                            showDeleteConfirm = true
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (showEditDialog) {
                                    EditBoardDialog(
                                        currentName = board.name,
                                        onDismiss = { showEditDialog = false },
                                        onConfirm = { newName ->
                                            viewModel.updateBoard(board.id, newName) { showEditDialog = false }
                                        }
                                    )
                                }

                                if (showDeleteConfirm) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteConfirm = false },
                                        title = { Text("Delete board?") },
                                        text = { Text("This will delete \"${board.name}\" and all of its columns and cards. This action cannot be undone.") },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                viewModel.deleteBoard(board.id) { showDeleteConfirm = false }
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
            }
        }


        if (showCreateDialog) {
            CreateBoardDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name ->
                    viewModel.createBoard(name) { success ->
                        showCreateDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun CreateBoardDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create new board") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Board name") }
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onCreate(name) }) {
                Text("Create")
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
fun EditBoardDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit board name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Board name") }
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

