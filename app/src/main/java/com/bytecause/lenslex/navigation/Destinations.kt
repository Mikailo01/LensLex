package com.bytecause.lenslex.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Login : Screen

    @Serializable
    data object SendEmailPasswordReset : Screen

    @Serializable
    data object Account : Screen

    @Serializable
    data object AccountSettings : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Add : Screen

    @Serializable
    data class ResetPassword(val action: String, val oobCode: String) : Screen

    @Serializable
    data class TextResult(val text: List<String>) : Screen

    @Serializable
    data class ModifiedImagePreview(val originalUri: String, val modifiedUri: String) : Screen
}

sealed interface NavGraph {
    @Serializable
    data object UserAuthGraph : NavGraph

    @Serializable
    data object TextProcessMainGraph : NavGraph

    @Serializable
    data object SettingsGraph : NavGraph
}