package com.bytecause.lenslex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.screens.FabNavigation

@Composable
fun CircularFloatingActionMenu(
    fabColor: Color,
    fabContentColor: Color,
    expandedFabBackgroundColor: Color,
    onInnerContentClick: (FabNavigation) -> Unit,
) {
    var iconState by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = iconState, modifier = Modifier
                .align(Alignment.BottomEnd)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(
                        RoundedCornerShape(
                            topStartPercent = 100,
                        )
                    )
                    .background(expandedFabBackgroundColor)
                    .width(190.dp)
                    .height(170.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 20.dp)
                    ) {
                        val (camera, gallery, add) = createRefs()

                        CameraMiniFab(
                            icon = painterResource(id = R.drawable.baseline_camera_alt_24),
                            text = stringResource(id = R.string.camera),
                            contentColor = fabContentColor,
                            modifier = Modifier
                                .constrainAs(camera) {
                                    top.linkTo(parent.top)
                                    end.linkTo(parent.end)
                                }
                                .padding(top = 10.dp, end = 10.dp),
                            navigation = FabNavigation.CAMERA,
                            onClick = {
                                iconState = false
                                onInnerContentClick(it)
                            }
                        )
                        MiniFab(
                            icon = painterResource(id = R.drawable.baseline_image_24),
                            text = stringResource(id = R.string.gallery),
                            contentColor = fabContentColor,
                            modifier = Modifier
                                .constrainAs(gallery) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                }
                                .padding(top = 50.dp, start = 35.dp),
                            navigation = FabNavigation.GALLERY,
                            onClick = {
                                iconState = false
                                onInnerContentClick(it)
                            }
                        )
                        MiniFab(
                            icon = Icons.Filled.AddCircle,
                            text = stringResource(id = R.string.add),
                            contentColor = fabContentColor,
                            modifier = Modifier
                                .constrainAs(add) {
                                    top.linkTo(gallery.bottom)
                                    start.linkTo(parent.start)
                                }
                                .padding(top = 10.dp, start = 5.dp),
                            navigation = FabNavigation.ADD,
                            onClick = {
                                iconState = false
                                onInnerContentClick(it)
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            FloatingActionButton(onClick = {
                iconState = !iconState
            }, containerColor = fabColor) {
                Icon(
                    imageVector =
                    if (!iconState) Icons.Default.Add else Icons.Default.Close,
                    contentDescription = "",
                    modifier = Modifier.graphicsLayer(
                        rotationZ = animateFloatAsState(
                            targetValue = if (iconState) 180f else 0f,
                            label = "Fab content rotation animation"
                        ).value
                    ),
                    tint = fabContentColor
                )
            }
        }
    }
}

@Composable
fun MiniFab(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentColor: Color,
    text: String,
    navigation: FabNavigation,
    onClick: (FabNavigation) -> Unit
) {
    Box(modifier = modifier.clip(CircleShape)) {
        Column(
            modifier = Modifier
                .clickable { onClick(navigation) }
                .padding(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "",
                tint = contentColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = text, style = TextStyle(
                    color = contentColor,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
fun MiniFab(
    modifier: Modifier = Modifier,
    icon: Painter,
    contentColor: Color,
    text: String,
    navigation: FabNavigation,
    onClick: (FabNavigation) -> Unit
) {
    Box(modifier = modifier.clip(CircleShape)) {
        Column(
            modifier = Modifier
                .clickable { onClick(navigation) }
                .padding(5.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = "",
                tint = contentColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = text, style = TextStyle(
                    color = contentColor,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
fun CameraMiniFab(
    modifier: Modifier = Modifier,
    icon: Painter,
    contentColor: Color,
    text: String,
    navigation: FabNavigation,
    onClick: (FabNavigation) -> Unit
) {

    Box(
        modifier = modifier.clip(CircleShape)
    ) {
        Column(
            modifier = Modifier
                .clickable {
                    onClick(navigation)
                }
                .padding(5.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = "",
                tint = contentColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = text, style = TextStyle(
                    color = contentColor,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
            )
        }
    }
}