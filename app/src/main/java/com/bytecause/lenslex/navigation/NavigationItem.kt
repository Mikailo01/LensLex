package com.bytecause.lenslex.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

enum class Screens {
    LOGIN,
    ACCOUNT,
    ACCOUNT_SETTINGS,
    HOME,
    ADD,
    TEXT_RESULT,
    MODIFIED_IMAGE_PREVIEW,
    TEXT_PROCESS_GALLERY_GRAPH,
    SETTINGS_GRAPH
}

sealed class NavigationItem(val route: String) {
    data object Login : NavigationItem(Screens.LOGIN.name)
    data object Account : NavigationItem(Screens.ACCOUNT.name)
    data object AccountSettings : NavigationItem(Screens.ACCOUNT_SETTINGS.name)
    data object Home : NavigationItem(Screens.HOME.name)
    data object Add : NavigationItem(Screens.ADD.name)
    data object TextResult : NavigationItem(Screens.TEXT_RESULT.name)
    data object ModifiedImagePreview : NavigationItem(Screens.MODIFIED_IMAGE_PREVIEW.name) {
        const val ORIGINAL_URI_TYPE_ARG = "originalImageUri"
        const val MODIFIED_URI_TYPE_ARG = "modifiedImageUri"
        val routeWithArgs = "$route/{$ORIGINAL_URI_TYPE_ARG}/{$MODIFIED_URI_TYPE_ARG}"
        val arguments = listOf(
            navArgument(ORIGINAL_URI_TYPE_ARG) { type = NavType.StringType },
            navArgument(MODIFIED_URI_TYPE_ARG) { type = NavType.StringType }
        )
    }

    // Graphs
    data object TextProcessMainGraph : NavigationItem(Screens.TEXT_PROCESS_GALLERY_GRAPH.name)
    data object SettingsGraph : NavigationItem(Screens.SETTINGS_GRAPH.name)
}