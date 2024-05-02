package com.bytecause.lenslex.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.bytecause.lenslex.navigation.navhost.AppNavHost
import com.bytecause.lenslex.ui.theme.AppTheme

@Composable
fun LensLexApp(
    widthSizeClass: WindowWidthSizeClass
) {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            val isExpandedScreen = widthSizeClass == WindowWidthSizeClass.Expanded

            AppNavHost(
                navController = navController,
                isExpandedScreen = isExpandedScreen,
                modifier = Modifier
            )
        }
    }
}