package com.bytecause.lenslex.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

enum class Screens {
    LOGIN,
    ACCOUNT,
    HOME,
    ADD,
    TEXT_RESULT,
    MODIFIED_IMAGE_PREVIEW,
    TEXT_PROCESS_GALLERY
}

sealed class NavigationItem(val route: String) {
    data object Login : NavigationItem(Screens.LOGIN.name)
    data object Account : NavigationItem(Screens.ACCOUNT.name)
    data object Home : NavigationItem(Screens.HOME.name)
    data object Add : NavigationItem(Screens.ADD.name)
    data object TextResult : NavigationItem(Screens.TEXT_RESULT.name)
    data object ModifiedImagePreview : NavigationItem(Screens.MODIFIED_IMAGE_PREVIEW.name) {
        const val originalUriTypeArg = "originalImageUri"
        const val modifiedUriTypeArg = "modifiedImageUri"
        val routeWithArgs = "$route/{$originalUriTypeArg}/{$modifiedUriTypeArg}"
        val arguments = listOf(
            navArgument(originalUriTypeArg) { type = NavType.StringType },
            navArgument(modifiedUriTypeArg) { type = NavType.StringType }
        )
    }

    // Graphs
    data object TextProcessMainGraph : NavigationItem(Screens.TEXT_PROCESS_GALLERY.name)
}