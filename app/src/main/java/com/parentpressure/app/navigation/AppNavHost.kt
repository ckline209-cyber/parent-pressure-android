package com.parentpressure.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.parentpressure.app.auth.AuthViewModel
import com.parentpressure.app.auth.LoginScreen
import com.parentpressure.app.auth.RegisterScreen
import com.parentpressure.app.home.HomeScreen

private object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

@Composable
fun AppNavHost() {
    val navController: NavHostController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }

        composable(Routes.HOME) {
            val uiState by authViewModel.uiState.collectAsState()
            val user = uiState.user
            if (user != null) {
                HomeScreen(
                    user = user,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
