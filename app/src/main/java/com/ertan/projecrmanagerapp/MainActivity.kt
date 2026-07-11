package com.ertan.projecrmanagerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ertan.projecrmanagerapp.ui.screens.BoardDetailScreen
import com.ertan.projecrmanagerapp.ui.screens.BoardListScreen
import com.ertan.projecrmanagerapp.ui.screens.CardDetailScreen
import com.ertan.projecrmanagerapp.ui.screens.LoginScreen
import com.ertan.projecrmanagerapp.ui.screens.RegisterScreen
import com.ertan.projecrmanagerapp.ui.screens.UserManagementScreen
import com.ertan.projecrmanagerapp.ui.theme.ProjectManagerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectManagerAppTheme() {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onGuestContinue = {
                    navController.navigate("home") {
                        popUpTo("login") {inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable("home") {
            BoardListScreen(
                onBoardClick = { boardId ->
                    navController.navigate("board/$boardId")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onManageUsers = {
                    navController.navigate("manage_users")
                }
            )
        }
        composable("manage_users"){
            UserManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("board/{boardId}") { backStackEntry ->
            val boardId = backStackEntry.arguments?.getString("boardId")?.toIntOrNull() ?: 0
            BoardDetailScreen(
                boardId = boardId,
                onBack = { navController.popBackStack() },
                onCardClick = { cardId ->
                    navController.navigate("board/$boardId/card/$cardId")
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