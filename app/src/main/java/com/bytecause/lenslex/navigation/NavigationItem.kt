package com.bytecause.lenslex.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

enum class Screens {
    LOGIN,
    HOME,
    ADD,
    TEXT_RESULT,
    MODIFIED_IMAGE_PREVIEW,
    TEXT_PROCESS_GALLERY
}

sealed class NavigationItem(val route: String) {
    data object Login : NavigationItem(Screens.LOGIN.name)
    data object Home : NavigationItem(Screens.HOME.name)
    data object Add : NavigationItem(Screens.ADD.name)
    data object TextResult : NavigationItem(Screens.TEXT_RESULT.name)
    data object ModifiedImagePreview : NavigationItem(Screens.MODIFIED_IMAGE_PREVIEW.name) {
        const val uriTypeArg = "modifiedImageUri"
        val routeWithArgs = "$route/{$uriTypeArg}"
        val arguments = listOf(
            navArgument(uriTypeArg) { type = NavType.StringType }
        )
    }

    // Graphs
    data object TextProcessMainGraph : NavigationItem(Screens.TEXT_PROCESS_GALLERY.name)
}