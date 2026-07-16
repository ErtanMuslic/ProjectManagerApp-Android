//package com.ertan.projecrmanagerapp.ui.screens
//
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Assignment
//import androidx.compose.material.icons.filled.Dashboard
//import androidx.compose.material.icons.filled.Groups
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.navigation.NavDestination.Companion.hierarchy
//import androidx.navigation.NavGraph.Companion.findStartDestination
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.rememberNavController
//import com.ertan.projecrmanagerapp.data.local.TokenManager
//
//sealed class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
//    object Boards : BottomTab("boards_tab", "Boards", Icons.Default.Dashboard)
//    object MyTasks : BottomTab("my_tasks_tab", "My Tasks", Icons.Default.Assignment)
//    object Team : BottomTab("team_tab", "Team", Icons.Default.Groups)
//    object Profile : BottomTab("profile_tab", "Profile", Icons.Default.Person)
//}
//
//@Composable
//fun MainScreen(
//    onLogout: () -> Unit,
//    onBoardClick: (Int) -> Unit,
//    onCardClick: (boardId: Int, cardId: Int) -> Unit
//) {
//    val innerNavController = rememberNavController()
//    val context = LocalContext.current
//    val tokenManager = remember { TokenManager(context) }
//    val token by tokenManager.getToken().collectAsState(initial = null)
//
//    // Guests (no token) only see Boards and Team (read-only); My Tasks and Profile require login
//    val isGuest = token == null
//
//    val tabs = if (isGuest) {
//        listOf(BottomTab.Boards, BottomTab.Team)
//    } else {
//        listOf(BottomTab.Boards, BottomTab.MyTasks, BottomTab.Team, BottomTab.Profile)
//    }
//
//    Scaffold(
//        bottomBar = {
//            NavigationBar {
//                val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
//                val currentDestination = navBackStackEntry?.destination
//
//                tabs.forEach { tab ->
//                    NavigationBarItem(
//                        icon = { Icon(tab.icon, contentDescription = tab.label) },
//                        label = { Text(tab.label) },
//                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
//                        onClick = {
//                            innerNavController.navigate(tab.route) {
//                                popUpTo(innerNavController.graph.findStartDestination().id) {
//                                    saveState = true
//                                }
//
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    ) { padding ->
//        NavHost(
//            navController = innerNavController,
//            startDestination = BottomTab.Boards.route,
//            modifier = Modifier.padding(padding),
//            route = "main_inner_graph"
//        ) {
//            composable(BottomTab.Boards.route) {
//                BoardListScreen(
//                    onBoardClick = onBoardClick,
//                    onLogout = onLogout
//                )
//            }
//            if (!isGuest) {
//                composable(BottomTab.MyTasks.route) {
//                    MyTasksScreen(
//                        onBack = { }, // no back button needed, this is a tab root
//                        onTaskClick = {boardId,cardId ->
//                            innerNavController.navigate("board/$boardId/card/$cardId")
//                        },//onCardClick, // simplified below
//                        showTopBar = false
//                    )
//                }
//            }
//            composable("board/{boardId}/card/{cardId}") { backStackEntry ->
//                CardDetailScreen(
//                    boardId = backStackEntry.arguments?.getString("boardId")?.toIntOrNull() ?: 0,
//                    cardId = backStackEntry.arguments?.getString("cardId")?.toIntOrNull() ?: 0,
//                    onBack = { innerNavController.popBackStack() }
//                )
//            }
//            composable(BottomTab.Team.route) {
//                TeamScreen()
//            }
//            if (!isGuest) {
//                composable(BottomTab.Profile.route) {
//                    AccountSettingsScreen(
//                        onBack = { },
//                        onAccountDeleted = onLogout,
//                        showTopBar = false
//                    )
//                }
//            }
//        }
//    }
//}