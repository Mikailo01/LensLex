package com.bytecause.lenslex.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bytecause.lenslex.navigation.NavigationItem
import com.bytecause.lenslex.mlkit.TextRecognizer
import com.bytecause.lenslex.ui.components.IndeterminateCircularIndicator
import com.bytecause.lenslex.ui.screens.viewmodel.TextRecognitionSharedViewModel

@Composable
fun ModifiedImagePreviewScreen(
    sharedViewModel: TextRecognitionSharedViewModel,
    imageUri: Uri,
    onClickNavigate: (NavigationItem) -> Unit
) {
    val context = LocalContext.current

    var isProgressBarShown by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim)
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Modified image preview",
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        )
        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            onClick = {
                isProgressBarShown = true
                TextRecognizer(context).runTextRecognition(listOf(imageUri)) {
                    isProgressBarShown = false
                    if (it.isEmpty()) {
                        Toast.makeText(
                            context,
                            "This image doesn't contain any text.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@runTextRecognition
                    }
                    sharedViewModel.updateProcessedTextState(it)
                    onClickNavigate(NavigationItem.TextResult)
                }
            }
        ) {
            Text(text = "Process")
        }
        IndeterminateCircularIndicator(
            modifier = Modifier.align(Alignment.Center),
            isShowed = isProgressBarShown
        )
    }
}