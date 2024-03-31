package com.bytecause.lenslex.navigation.navhost

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.bytecause.lenslex.navigation.NavigationItem
import com.bytecause.lenslex.ui.screens.AddScreen
import com.bytecause.lenslex.ui.screens.MainScreen
import com.bytecause.lenslex.ui.screens.ModifiedImagePreviewScreen
import com.bytecause.lenslex.ui.screens.RecognizedTextResultScreen
import com.bytecause.lenslex.ui.screens.viewmodel.TextRecognitionSharedViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationItem.TextProcessMainGraph.route,
        modifier = modifier
    ) {

        // Nested NavGraph
        navigation(
            startDestination = NavigationItem.Home.route,
            route = NavigationItem.TextProcessMainGraph.route
        ) {

            composable(route = NavigationItem.Home.route) {
                val viewModel = it.sharedViewModel<TextRecognitionSharedViewModel>(navController)

                MainScreen(
                    sharedViewModel = viewModel,
                    onClickNavigate = { navigationItem ->
                        navController.navigate(navigationItem.route)
                    },
                    onPhotoTaken = {
                        navController.navigate(
                            "${NavigationItem.ModifiedImagePreview.route}/${
                                Uri.encode(
                                    it.toString()
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
                    imageUri = Uri.parse(it.arguments?.getString(NavigationItem.ModifiedImagePreview.uriTypeArg)),
                    onClickNavigate = { navController.navigate(NavigationItem.TextResult.route) }
                )
            }

            composable(route = NavigationItem.TextResult.route) {
                val viewModel = it.sharedViewModel<TextRecognitionSharedViewModel>(navController)

                RecognizedTextResultScreen(
                    sharedViewModel = viewModel,
                    onBackButtonClick = { navController.popBackStack() }
                )
            }
        }

        composable(route = NavigationItem.Add.route) {
            AddScreen()
        }
    }
}

// Helper function for creating instance of sharedViewModel with bounded lifecycle to NavGraph.
@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = this.destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}