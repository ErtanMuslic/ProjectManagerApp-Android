package com.ertan.projecrmanagerapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ertan.projecrmanagerapp.viewmodel.AccountActionResult
import com.ertan.projecrmanagerapp.viewmodel.AccountState
import com.ertan.projecrmanagerapp.viewmodel.AccountViewModel
import com.ertan.projecrmanagerapp.ui.components.ErrorState
import com.ertan.projecrmanagerapp.ui.components.LoadingState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: AccountViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val actionResult by viewModel.actionResult.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionResult) {
        when (val result = actionResult) {
            is AccountActionResult.Success -> {
                snackbarHostState.showSnackbar("Saved successfully.")
                viewModel.resetActionResult()
            }
            is AccountActionResult.Error -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.resetActionResult()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            if(showTopBar) {
                TopAppBar(
                    title = { Text("Account Settings") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val s = state) {
            is AccountState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            is AccountState.Error -> ErrorState(message = s.message, onRetry = { viewModel.loadAccount() }, modifier = Modifier.padding(padding))
            is AccountState.Success -> {
                var name by remember(s.account.id) { mutableStateOf(s.account.name) }
                var currentPassword by remember { mutableStateOf("") }
                var newPassword by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = s.account.email, style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Role: ${s.account.role}", style = MaterialTheme.typography.labelMedium)
                    if (s.account.seniority != null) {
                        Text(text = "Seniority: ${s.account.seniority}", style = MaterialTheme.typography.labelMedium)
                    }

                    HorizontalDivider()

                    Text("Profile", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = { viewModel.updateName(name) }) {
                        Text("Save name")
                    }

                    HorizontalDivider()

                    if (s.account.hasPassword) {
                        Text("Change Password", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Current password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(onClick = {
                            if (currentPassword.isNotBlank() && newPassword.isNotBlank()) {
                                viewModel.changePassword(currentPassword, newPassword)
                                currentPassword = ""
                                newPassword = ""
                            }
                        }) {
                            Text("Change password")
                        }
                    } else {
                        Text(
                            text = "This account uses Google Sign-In. Password changes are managed through Google.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    HorizontalDivider()

                    Text("Danger Zone", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete my account")
                    }
                }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("Delete account?") },
                        text = { Text("This will permanently delete your account and cannot be undone. Your assigned cards will become unassigned.") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteAccount { success ->
                                    showDeleteConfirm = false
                                    if (success) onAccountDeleted()
                                }
                            }) {
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
        }
    }
}