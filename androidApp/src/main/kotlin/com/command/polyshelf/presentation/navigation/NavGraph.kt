package com.command.polyshelf.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.command.polyshelf.presentation.screen.auth.login.LoginScreen
import com.command.polyshelf.presentation.screen.auth.registration.RegistrationScreen
import com.command.polyshelf.presentation.screen.home.HomeScreen
import com.command.polyshelf.presentation.screen.splash.SplashScreen
import com.command.polyshelf.presentation.screen.splash.SplashViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Login : Screen

    @Serializable
    data object Registration : Screen

    @Serializable
    data object Home : Screen
}

@Composable
fun PolyShelfNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = modifier
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onAuthenticated = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Splash> { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    navController.navigate(Screen.Login) {
                        popUpTo<Screen.Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Login> {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate(Screen.Registration)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Login> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Registration> {
            RegistrationScreen(
                onLoginClick = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Registration> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Home> {
            val splashViewModel: SplashViewModel = koinViewModel()
            HomeScreen(
                onLogoutClick = {
                    splashViewModel.logout()
                    navController.navigate(Screen.Login) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                }
            )
        }
    }
}
