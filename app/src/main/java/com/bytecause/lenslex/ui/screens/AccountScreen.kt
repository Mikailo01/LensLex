package com.bytecause.lenslex.ui.screens

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bytecause.lenslex.R
import com.bytecause.lenslex.models.UserData
import com.bytecause.lenslex.ui.components.Divider
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.screens.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    authViewModel: AuthViewModel,
    onBackButtonClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val signedInUser: UserData? = remember {
        authViewModel.getSignedInUser
    }

    val context = LocalContext.current
    val packageManager: PackageManager = context.packageManager
    val intent: Intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
    val componentName: ComponentName = intent.component!!
    val restartIntent: Intent = Intent.makeRestartActivityTask(componentName)

    val gradientBackground = Brush.horizontalGradient(
        0.1f to Color.Magenta,
        0.3f to Color.Cyan,
        0.6f to Color.Magenta,
        0.7f to Color.Cyan,
        0.8f to Color.Magenta,
        0.9f to Color.Cyan,
        startX = 0f,
        endX = 1000f
    )

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.account,
                navigationIcon = Icons.Filled.ArrowBack,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                onBackButtonClick()
            }
        }
    ) { innerPaddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPaddingValues)) {

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(gradientBackground))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPaddingValues)
            ) {

                AsyncImage(
                    model = signedInUser?.profilePictureUrl.takeIf { it != "null" }
                        ?: R.drawable.default_account_image,
                    contentDescription = "avatar",
                    modifier = Modifier
                        .padding(top = 100.dp, bottom = 10.dp)
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                if (signedInUser?.userName != null) {
                    Text(
                        text = signedInUser.userName,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Divider(thickness = 2, color = MaterialTheme.colorScheme.primary)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_language_24),
                        contentDescription = "Language",
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    )
                    Text(text = "Language")
                }

                Divider(thickness = 1, color = Color.Gray)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 10.dp)
                        .clickable {
                            coroutineScope.launch {
                                if (authViewModel.signOut()) {
                                    context.startActivity(restartIntent)
                                    Runtime
                                        .getRuntime()
                                        .exit(0)
                                }
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_logout_24),
                        contentDescription = "Log out",
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)

                    )
                    Text(text = stringResource(id = R.string.sign_out))
                }
            }
        }
    }
}