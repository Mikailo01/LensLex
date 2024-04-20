package com.bytecause.lenslex.navigation.navhost

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
import androidx.navigation.navigation
import com.bytecause.lenslex.navigation.NavigationItem
import com.bytecause.lenslex.ui.screens.AccountScreen
import com.bytecause.lenslex.ui.screens.AccountSettingsScreen
import com.bytecause.lenslex.ui.screens.AddScreen
import com.bytecause.lenslex.ui.screens.HomeScreen
import com.bytecause.lenslex.ui.screens.LoginScreen
import com.bytecause.lenslex.ui.screens.ModifiedImagePreviewScreen
import com.bytecause.lenslex.ui.screens.RecognizedTextResultScreen
import com.bytecause.lenslex.ui.screens.viewmodel.TextRecognitionSharedViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val currentUser = Firebase.auth.currentUser

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) NavigationItem.TextProcessMainGraph.route
        else NavigationItem.Login.route,
        modifier = modifier
    ) {

        composable(route = NavigationItem.Login.route) {
            LoginScreen {
                navController.navigate(NavigationItem.Home.route) {
                    popUpTo(NavigationItem.Login.route) {
                        inclusive = true
                    }
                }
            }
        }

        // Nested NavGraph
        navigation(
            startDestination = NavigationItem.Home.route,
            route = NavigationItem.TextProcessMainGraph.route
        ) {

            composable(route = NavigationItem.Home.route) {
                val viewModel = it.sharedViewModel<TextRecognitionSharedViewModel>(navController)

                HomeScreen(
                    sharedViewModel = viewModel,
                    onClickNavigate = { navigationItem ->
                        navController.navigate(navigationItem.route)
                    },
                    onPhotoTaken = { originalUri, modifiedUri ->
                        navController.navigate(
                            "${NavigationItem.ModifiedImagePreview.route}/${
                                Uri.encode(
                                    originalUri.toString()
                                )
                            }/${
                                Uri.encode(
                                    modifiedUri.toString()
                                )
                            }"
                        )
                    }
                )
            }

            composable(
                route = NavigationItem.ModifiedImagePreview.routeWithArgs,
                arguments = NavigationItem.ModifiedImagePreview.arguments
            ) {
                val viewModel = it.sharedViewModel<TextRecognitionSharedViewModel>(navController)

                ModifiedImagePreviewScreen(
                    sharedViewModel = viewModel,
                    originalImageUri = Uri.parse(it.arguments?.getString(NavigationItem.ModifiedImagePreview.ORIGINAL_URI_TYPE_ARG)),
                    modifiedImageUri = Uri.parse(it.arguments?.getString(NavigationItem.ModifiedImagePreview.MODIFIED_URI_TYPE_ARG)),
                    onClickNavigate = { navController.navigate(NavigationItem.TextResult.route) }
                )
            }

            composable(route = NavigationItem.TextResult.route) {
                val viewModel = it.sharedViewModel<TextRecognitionSharedViewModel>(navController)

                RecognizedTextResultScreen(
                    sharedViewModel = viewModel,
                    onBackButtonClick = { navController.popBackStackOnce() }
                )
            }
        }

        navigation(
            startDestination = NavigationItem.Account.route,
            route = NavigationItem.SettingsGraph.route
        ) {

            composable(
                route = NavigationItem.Account.route
            ) {
                AccountScreen(
                    onNavigate = { navController.navigate(it.route) },
                    onBackButtonClick = { navController.popBackStackOnce() },
                    onUserLoggedOut = {
                        navController.navigate(NavigationItem.Login.route) {
                            popUpTo(NavigationItem.Home.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(
                route = NavigationItem.AccountSettings.route
            ) {
                AccountSettingsScreen(
                    onNavigateBack = { navController.popBackStackOnce() },
                    onUserLoggedOut = {
                        navController.navigate(NavigationItem.Login.route) {
                            popUpTo(NavigationItem.Home.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        composable(route = NavigationItem.Add.route) {
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
