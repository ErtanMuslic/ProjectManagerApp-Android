package com.ertan.projecrmanagerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.ertan.projecrmanagerapp.data.local.TokenManager
import com.ertan.projecrmanagerapp.ui.screens.*
import com.ertan.projecrmanagerapp.ui.theme.ProjectManagerAppTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectManagerAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

// Routes that show the bottom navigation bar (top-level screens only)
private val bottomBarRoutes = setOf(
    BottomTab.Boards.route,
    BottomTab.MyTasks.route,
    BottomTab.Team.route,
    BottomTab.Profile.route
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val token by tokenManager.getToken().collectAsState(initial = null)
    val isGuest = token == null

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    val tabs = if (isGuest) {
        listOf(BottomTab.Boards, BottomTab.Team)
    } else {
        listOf(BottomTab.Boards, BottomTab.MyTasks, BottomTab.Team, BottomTab.Profile)
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(BottomTab.Boards.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(if (showBottomBar) padding else PaddingValues(0.dp))
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(BottomTab.Boards.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    onGuestContinue = {
                        navController.navigate(BottomTab.Boards.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(BottomTab.Boards.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(BottomTab.Boards.route) {
                BoardListScreen(
                    onBoardClick = { boardId -> navController.navigate("board/$boardId") },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(BottomTab.MyTasks.route) {
                MyTasksScreen(
                    onBack = { navController.popBackStack() },
                    onTaskClick = { boardId, cardId ->
                        navController.navigate("board/$boardId/card/$cardId")
                    },
                    showTopBar = false
                )
            }
            composable(BottomTab.Team.route) {
                TeamScreen()
            }
            composable(BottomTab.Profile.route) {
                AccountSettingsScreen(
                    onBack = { navController.popBackStack() },
                    onAccountDeleted = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    showTopBar = false
                )
            }
            composable("board/{boardId}") { backStackEntry ->
                val boardId = backStackEntry.arguments?.getString("boardId")?.toIntOrNull() ?: 0
                BoardDetailScreen(
                    boardId = boardId,
                    onBack = { navController.popBackStack() },
                    onCardClick = { cardId ->
                        navController.navigate("board/$boardId/card/$cardId")
                    },
                    onMyTasks = {
                        navController.navigate(BottomTab.MyTasks.route)
                    }
                )
            }
            composable("board/{boardId}/card/{cardId}") { backStackEntry ->
                val boardId = backStackEntry.arguments?.getString("boardId")?.toIntOrNull() ?: 0
                val cardId = backStackEntry.arguments?.getString("cardId")?.toIntOrNull() ?: 0
                CardDetailScreen(
                    boardId = boardId,
                    cardId = cardId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}