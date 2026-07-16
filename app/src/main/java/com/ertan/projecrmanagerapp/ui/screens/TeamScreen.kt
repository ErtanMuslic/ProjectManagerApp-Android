package com.ertan.projecrmanagerapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ertan.projecrmanagerapp.data.local.TokenManager
import com.ertan.projecrmanagerapp.data.model.UserSummary
import com.ertan.projecrmanagerapp.ui.components.Avatar
import com.ertan.projecrmanagerapp.ui.components.EmptyState
import com.ertan.projecrmanagerapp.ui.components.ErrorState
import com.ertan.projecrmanagerapp.ui.components.LoadingState
import com.ertan.projecrmanagerapp.viewmodel.UserManagementState
import com.ertan.projecrmanagerapp.viewmodel.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(viewModel: UserManagementViewModel = viewModel()) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val role by tokenManager.getRole().collectAsState(initial = null)
    val isAdmin = role == "Admin"

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Team") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UserManagementState.Loading -> LoadingState()
                is UserManagementState.Error -> ErrorState(message = s.message, onRetry = { viewModel.loadUsers() })
                is UserManagementState.Success -> {
                    if (s.users.isEmpty()) {
                        EmptyState(message = "No team members yet.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(s.users) { user ->
                                TeamMemberCard(
                                    user = user,
                                    isAdmin = isAdmin,
                                    onSeniorityChange = { newSeniority ->
                                        viewModel.updateSeniority(user.id, newSeniority)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMemberCard(user: UserSummary, isAdmin: Boolean, onSeniorityChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val seniorityOptions = listOf("Junior", "Mid", "Senior")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(name = user.name, userId = user.id, size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = user.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isAdmin) {
                    // Read-only badge for non-admin viewers
                    Text(
                        text = user.seniority ?: "Not set",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(10.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = user.seniority ?: "Not set",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Seniority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        seniorityOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onSeniorityChange(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}