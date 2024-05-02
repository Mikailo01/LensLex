package com.bytecause.lenslex.navigation

import android.content.Intent
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

enum class Screens {
    LOGIN,
    SEND_EMAIL_PASSWORD_RESET,
    RESET_PASSWORD,
    ACCOUNT,
    ACCOUNT_SETTINGS,
    HOME,
    ADD,
    TEXT_RESULT,
    MODIFIED_IMAGE_PREVIEW,
    USER_AUTH_GRAPH,
    TEXT_PROCESS_GALLERY_GRAPH,
    SETTINGS_GRAPH
}

sealed class NavigationItem(val route: String) {
    data object Login : NavigationItem(Screens.LOGIN.name)
    data object EmailPasswordReset : NavigationItem(Screens.SEND_EMAIL_PASSWORD_RESET.name)
    data object ResetPassword : NavigationItem(Screens.RESET_PASSWORD.name) {
        const val ACTION_MODE = "action"
        const val OOB_CODE = "code"

        val deepLinks = listOf(
            navDeepLink {
                uriPattern =
                    "https://lens-lex.firebaseapp.com/request/action?mode={action}&oobCode={code}"
                action = Intent.ACTION_VIEW
            }
        )
        val arguments = listOf(
            navArgument(ACTION_MODE) { type = NavType.StringType },
            navArgument(OOB_CODE) { type = NavType.StringType }
        )
    }

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
    data object UserAuthGraph : NavigationItem(Screens.USER_AUTH_GRAPH.name)
    data object TextProcessMainGraph : NavigationItem(Screens.TEXT_PROCESS_GALLERY_GRAPH.name)
    data object SettingsGraph : NavigationItem(Screens.SETTINGS_GRAPH.name)
}