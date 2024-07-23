package com.bytecause.lenslex.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bytecause.lenslex.R
import com.bytecause.lenslex.util.shimmerEffect
import com.bytecause.lenslex.util.then

@Composable
fun ProfilePicture(
    profilePicture: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    @DrawableRes cornerIcon: Int = R.drawable.baseline_camera_alt_24,
    cornerIconSize: Dp = 28.dp,
    onCornerIconClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = modifier
    ) {
        AsyncImage(
            model = profilePicture.takeIf { it != "null" }
                ?: R.drawable.default_account_image,
            contentDescription = "avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .then(isLoading, onTrue = { shimmerEffect() })
        )

        Box(
            modifier = Modifier
                .size(cornerIconSize + 8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {

            IconButton(
                onClick = { onCornerIconClick() },
                modifier = Modifier.matchParentSize()
            ) {
                Icon(
                    painter = painterResource(id = cornerIcon),
                    contentDescription = stringResource(id = R.string.edit_profile_picture),
                    modifier = Modifier.size((cornerIconSize + 8.dp).times(0.6f)),
                    tint = MaterialTheme.colorScheme.inversePrimary
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ProfilePicturePreview() {
    ProfilePicture(profilePicture = "") { }
}