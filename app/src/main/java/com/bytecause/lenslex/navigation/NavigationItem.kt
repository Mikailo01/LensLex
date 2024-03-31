package com.bytecause.lenslex.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

enum class Screen {
    HOME,
    ADD,
    TEXT_RESULT,
    MODIFIED_IMAGE_PREVIEW,
    TEXT_PROCESS_GALLERY
}

sealed class NavigationItem(val route: String) {
    data object Home : NavigationItem(Screen.HOME.name)
    data object Add : NavigationItem(Screen.ADD.name)
    data object TextResult : NavigationItem(Screen.TEXT_RESULT.name)/* {
        const val processedTextTypeArg = "processedText"
        val routeWithArgs = "$route/{$processedTextTypeArg}"
        val arguments = listOf(
            navArgument(processedTextTypeArg) { type = NavType.StringType }
        )
    }*/

    data object ModifiedImagePreview : NavigationItem(Screen.MODIFIED_IMAGE_PREVIEW.name) {
        const val uriTypeArg = "modifiedImageUri"
        val routeWithArgs = "$route/{$uriTypeArg}"
        val arguments = listOf(
            navArgument(uriTypeArg) { type = NavType.StringType }
        )
    }

    // Graphs
    data object TextProcessMainGraph : NavigationItem(Screen.TEXT_PROCESS_GALLERY.name)
}