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
import com.parentpressure.app.nutrition.NutritionScreen
import com.parentpressure.app.nutrition.NutritionViewModel
import com.parentpressure.app.subscription.SubscriptionScreen
import com.parentpressure.app.subscription.SubscriptionViewModel
import com.parentpressure.app.workouts.WorkoutsScreen
import com.parentpressure.app.workouts.WorkoutsViewModel

private object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val WORKOUTS = "workouts"
    const val NUTRITION = "nutrition"
    const val SUBSCRIPTION = "subscription"
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
                    onViewWorkouts = { navController.navigate(Routes.WORKOUTS) },
                    onViewNutrition = { navController.navigate(Routes.NUTRITION) },
                    onViewSubscription = { navController.navigate(Routes.SUBSCRIPTION) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                )
            }
        }

        composable(Routes.WORKOUTS) {
            val workoutsViewModel: WorkoutsViewModel = viewModel()
            WorkoutsScreen(viewModel = workoutsViewModel)
        }

        composable(Routes.NUTRITION) {
            val nutritionViewModel: NutritionViewModel = viewModel()
            NutritionScreen(viewModel = nutritionViewModel)
        }

        composable(Routes.SUBSCRIPTION) {
            val subscriptionViewModel: SubscriptionViewModel = viewModel()
            SubscriptionScreen(viewModel = subscriptionViewModel)
        }
    }
}
