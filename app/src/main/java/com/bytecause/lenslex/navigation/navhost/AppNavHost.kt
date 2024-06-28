package com.bytecause.lenslex.navigation.navhost

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.bytecause.lenslex.navigation.NavGraph
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.screens.AccountScreen
import com.bytecause.lenslex.ui.screens.AccountSettingsScreen
import com.bytecause.lenslex.ui.screens.AddScreen
import com.bytecause.lenslex.ui.screens.HomeScreen
import com.bytecause.lenslex.ui.screens.LoginScreen
import com.bytecause.lenslex.ui.screens.ModifiedImagePreviewScreen
import com.bytecause.lenslex.ui.screens.RecognizedTextResultScreen
import com.bytecause.lenslex.ui.screens.SendEmailResetScreen
import com.bytecause.lenslex.ui.screens.UpdatePasswordScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val currentUser = Firebase.auth.currentUser

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) NavGraph.TextProcessMainGraph
        else NavGraph.UserAuthGraph,
        modifier = modifier
    ) {

        // Nested NavGraph
        navigation<NavGraph.UserAuthGraph>(startDestination = Screen.Login) {
            composable<Screen.Login> {
                LoginScreen(
                    isExpandedScreen = isExpandedScreen,
                    onNavigate = { screen ->
                        navController.navigate(screen)
                    }
                ) {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Login) {
                            inclusive = true
                        }
                    }
                }
            }

            composable<Screen.SendEmailPasswordReset> {
                SendEmailResetScreen(isExpandedScreen = isExpandedScreen)
            }

            composable<Screen.ResetPassword>(
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern =
                            "https://lens-lex.firebaseapp.com/request/action?mode={action}&oobCode={oobCode}"
                        action = Intent.ACTION_VIEW
                    }
                )
            ) { backStackEntry ->

                val oobCode: String = backStackEntry.toRoute<Screen.ResetPassword>().oobCode

                UpdatePasswordScreen(
                    isExpandedScreen = isExpandedScreen,
                    oobCode = oobCode,
                    onPasswordChangedSuccess = {
                        // Navigate and forget previous destination
                        navController.navigate(Screen.Login) {
                            popUpTo(Screen.ResetPassword) {
                                inclusive = true
                            }
                        }
                    },
                    onGetNewResetCodeClick = {
                        navController.navigate(Screen.SendEmailPasswordReset)
                    }
                )
            }
        }

        // Nested NavGraph
        navigation<NavGraph.TextProcessMainGraph>(startDestination = Screen.Home) {
            composable<Screen.Home> {
                HomeScreen(
                    onClickNavigate = { screen ->
                        navController.navigate(screen)
                    },
                    onPhotoTaken = { originalUri, modifiedUri ->
                        navController.navigate(
                            Screen.ModifiedImagePreview(
                                originalUri = originalUri.toString(),
                                modifiedUri = modifiedUri.toString()
                            )
                        )
                    }
                )
            }

            composable<Screen.ModifiedImagePreview> { backStackEntry ->
                val originalUri: String =
                    backStackEntry.toRoute<Screen.ModifiedImagePreview>().originalUri
                val modifiedUri: String =
                    backStackEntry.toRoute<Screen.ModifiedImagePreview>().modifiedUri

                ModifiedImagePreviewScreen(
                    originalImageUri = Uri.parse(originalUri),
                    modifiedImageUri = Uri.parse(modifiedUri),
                    onNavigateBack = { navController.popBackStackOnce() },
                    onClickNavigate = { navController.navigate(it) }
                )
            }

            composable<Screen.TextResult> { backStackEntry ->
                val text = backStackEntry.toRoute<Screen.TextResult>().text

                RecognizedTextResultScreen(
                    text = text,
                    onBackButtonClick = { navController.popBackStackOnce() },
                    onDone = {
                        navController.navigate(Screen.Home) {
                            popUpTo<Screen.Home> {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        // Nested NavGraph
        navigation<NavGraph.SettingsGraph>(startDestination = Screen.Account) {
            composable<Screen.Account> {
                AccountScreen(
                    onNavigate = { navController.navigate(it) },
                    onBackButtonClick = { navController.popBackStackOnce() },
                    onUserLoggedOut = {
                        navController.navigate(Screen.Login) {
                            popUpTo(Screen.Home) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable<Screen.AccountSettings> {
                AccountSettingsScreen(
                    isExpandedScreen = isExpandedScreen,
                    onNavigateBack = { navController.popBackStackOnce() },
                    onUserLoggedOut = {
                        navController.navigate(Screen.Login) {
                            popUpTo(Screen.Home) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        composable<Screen.Add> {
            AddScreen(onNavigateBack = { navController.popBackStackOnce() })
        }
    }
}

// Helper function for creating instance of sharedViewModel with bounded lifecycle to NavGraph.
@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = this.destination.parent?.route ?: return koinViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(viewModelStoreOwner = parentEntry)
}

// This prevents issues when the user tap on back button multiple times in row
fun NavHostController.popBackStackOnce() = run {
    previousBackStackEntry?.destination?.route?.let { route ->
        popBackStack(route, false)
    }
}
