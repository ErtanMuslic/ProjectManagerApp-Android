package com.ertan.projecrmanagerapp.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    object Boards : BottomTab("boards", "Boards", Icons.Default.Dashboard)
    object MyTasks : BottomTab("my_tasks", "My Tasks", Icons.Default.Assignment)
    object Team : BottomTab("team", "Team", Icons.Default.Groups)
    object Profile : BottomTab("profile", "Profile", Icons.Default.Person)
}