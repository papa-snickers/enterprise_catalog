package com.example.enterprisecatalog.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.enterprisecatalog.data.api.ApiService
import com.example.enterprisecatalog.data.local.DataStoreManager
import com.example.enterprisecatalog.data.repository.AuthRepository
import com.example.enterprisecatalog.data.repository.EnterpriseRepository
import com.example.enterprisecatalog.ui.admin.AdminPanelScreen
import com.example.enterprisecatalog.ui.auth.LoginScreen
import com.example.enterprisecatalog.ui.auth.RegisterScreen
import com.example.enterprisecatalog.ui.catalog.CatalogScreen
import com.example.enterprisecatalog.ui.edit.EnterpriseEditScreen
import com.example.enterprisecatalog.ui.profile.ProfileScreen
import com.example.enterprisecatalog.ui.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    apiService: ApiService,
    dataStoreManager: DataStoreManager,
    onToggleTheme: () -> Unit
) {
    val authRepository = AuthRepository(apiService, dataStoreManager)
    val enterpriseRepository = EnterpriseRepository(apiService)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                dataStoreManager = dataStoreManager,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToCatalog = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authRepository = authRepository,
                onLoginSuccess = { role ->
                    val dest = if (role == "ADMIN") Screen.Admin.route else Screen.Catalog.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authRepository = authRepository,
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Catalog.route) {
            CatalogScreen(
                enterpriseRepository = enterpriseRepository,
                dataStoreManager = dataStoreManager,
                onToggleTheme = onToggleTheme,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Admin.route) {
            AdminPanelScreen(
                enterpriseRepository = enterpriseRepository,
                dataStoreManager = dataStoreManager,
                onToggleTheme = onToggleTheme,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.EnterpriseEdit.createRoute())
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EnterpriseEdit.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.EnterpriseEdit.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            EnterpriseEditScreen(
                enterpriseId = id,
                enterpriseRepository = enterpriseRepository,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                dataStoreManager = dataStoreManager,
                authRepository = authRepository,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
