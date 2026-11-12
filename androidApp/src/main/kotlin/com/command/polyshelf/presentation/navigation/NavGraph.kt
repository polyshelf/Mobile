package com.command.polyshelf.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.command.polyshelf.presentation.screen.auth.login.LoginScreen
import com.command.polyshelf.presentation.screen.auth.registration.RegistrationScreen
import com.command.polyshelf.presentation.screen.changepassword.ChangePasswordScreen
import com.command.polyshelf.presentation.screen.main.MainScreenContainer
import com.command.polyshelf.presentation.screen.main.MainViewModel
import com.command.polyshelf.presentation.screen.splash.SplashScreen
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
    data object Main : Screen

    @Serializable
    data object ChangePassword : Screen
}

/**
 * popUpTo - удаляет пройденные экраны (например Splash или Login) из стека навигации,
 * чтобы при нажатии физической кнопки "Назад" пользователь не возвращался на экран входа/сплеша.
 */
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
                    navController.navigate(Screen.Main) {
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
                    navController.navigate(Screen.Main) {
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
                    navController.navigate(Screen.Registration) {
                        popUpTo<Screen.Registration> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Main> {
            val mainViewModel: MainViewModel = koinViewModel()
            MainScreenContainer(
                onChangePasswordClick = {
                    navController.navigate(Screen.ChangePassword)
                },
                onLogoutClick = {
                    mainViewModel.logout()
                    navController.navigate(Screen.Login) {
                        popUpTo<Screen.Main> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.ChangePassword> {
            ChangePasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
