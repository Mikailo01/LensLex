package com.bytecause.lenslex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.screens.FabNavigation
import com.bytecause.lenslex.util.introShowcaseBackgroundAlpha
import com.bytecause.lenslex.util.then
import com.canopas.lib.showcase.IntroShowcaseScope
import com.canopas.lib.showcase.component.ShowcaseStyle
import kotlinx.coroutines.delay

@Composable
fun IntroShowcaseScope.CircularFloatingActionMenu(
    iconState: Boolean,
    fabColor: Color,
    fabContentColor: Color,
    expandedFabBackgroundColor: Color,
    onInnerContentClick: (FabNavigation) -> Unit,
    onIconStateChange: (Boolean) -> Unit
) {

    // Animation state
    val rotationAnimation = animateFloatAsState(
        targetValue = if (iconState) 180f else 0f,
        label = "Fab content rotation animation"
    )

    // State to track if animation is finished
    var animationFinished by remember { mutableStateOf(false) }

    // LaunchedEffect to detect when animation finishes
    LaunchedEffect(rotationAnimation.value) {
        if (rotationAnimation.value == 180f || rotationAnimation.value == 0f) {
            // Small delay to ensure all composables are recomposed after the animation ends
            delay(200)
            animationFinished = true
        } else {
            animationFinished = false
        }
    }

    Box(modifier = Modifier.wrapContentSize()) {
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

                        MiniFab(
                            image = ImageResource.Painter(painterResource(id = R.drawable.baseline_camera_alt_24)),
                            text = stringResource(id = R.string.camera),
                            contentColor = fabContentColor,
                            modifier = Modifier
                                .constrainAs(camera) {
                                    top.linkTo(parent.top)
                                    end.linkTo(parent.end)
                                }
                                .padding(top = 10.dp, end = 10.dp)
                                .introShowCaseTarget(
                                    index = 3,
                                    style = ShowcaseStyle.Default.copy(
                                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                        backgroundAlpha = introShowcaseBackgroundAlpha,
                                        targetCircleColor = Color.White
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.camera),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        IntroShowcaseText(
                                            text = stringResource(id = R.string.camera_mini_fab_showcase_message),
                                            modifier = Modifier.padding(top = 30.dp)
                                        )
                                    }
                                },
                            contentDescription = stringResource(id = R.string.launch_camera),
                            navigation = FabNavigation.CAMERA,
                            onClick = {
                                onIconStateChange(false)
                                onInnerContentClick(it)
                            }
                        )
                        MiniFab(
                            image = ImageResource.Painter(painterResource(id = R.drawable.baseline_image_24)),
                            text = stringResource(id = R.string.gallery),
                            contentColor = fabContentColor,
                            modifier = Modifier
                                .constrainAs(gallery) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                }
                                .padding(top = 50.dp, start = 35.dp)
                                .introShowCaseTarget(
                                    index = 4,
                                    style = ShowcaseStyle.Default.copy(
                                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                        backgroundAlpha = introShowcaseBackgroundAlpha,
                                        targetCircleColor = Color.White
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.gallery),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        IntroShowcaseText(
                                            text = stringResource(id = R.string.gallery_mini_fab_showcase_message),
                                            modifier = Modifier.padding(top = 30.dp)
                                        )
                                    }
                                },
                            contentDescription = stringResource(id = R.string.launch_gallery_image_picker),
                            navigation = FabNavigation.GALLERY,
                            onClick = {
                                onIconStateChange(false)
                                onInnerContentClick(it)
                            }
                        )
                        MiniFab(
                            image = ImageResource.ImageVector(Icons.Filled.AddCircle),
                            text = stringResource(id = R.string.add),
                            contentColor = fabContentColor,
                            modifier = Modifier
                                .constrainAs(add) {
                                    top.linkTo(gallery.bottom)
                                    start.linkTo(parent.start)
                                }
                                .padding(top = 10.dp, start = 5.dp)
                                .introShowCaseTarget(
                                    index = 5,
                                    style = ShowcaseStyle.Default.copy(
                                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                        backgroundAlpha = introShowcaseBackgroundAlpha,
                                        targetCircleColor = Color.White
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.add),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        IntroShowcaseText(
                                            text = stringResource(id = R.string.add_mini_fab_showcase_message),
                                            modifier = Modifier.padding(top = 30.dp)
                                        )
                                    }
                                },
                            contentDescription = stringResource(id = R.string.add_new_word_into_the_list),
                            navigation = FabNavigation.ADD,
                            onClick = {
                                onIconStateChange(false)
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
            FloatingActionButton(
                onClick = { onIconStateChange(!iconState) },
                containerColor = fabColor,
                modifier = Modifier.then(
                    true, onTrue = {
                        introShowCaseTarget(
                            index = 2,
                            style = ShowcaseStyle.Default.copy(
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                backgroundAlpha = introShowcaseBackgroundAlpha,
                                targetCircleColor = Color.White
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(15.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.expand),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(64.dp)
                                )
                                IntroShowcaseText(
                                    text = stringResource(id = R.string.tap_to_expand_action_menu_showcase_message)
                                )
                            }
                        }
                    }
                )
            ) {
                Icon(
                    imageVector =
                    if (!iconState) Icons.Default.Add else Icons.Default.Close,
                    contentDescription = "Floating action button with actions.",
                    modifier = Modifier.graphicsLayer(
                        rotationZ = rotationAnimation.value
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
    image: ImageResource,
    contentColor: Color,
    text: String,
    contentDescription: String,
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
            when (image) {
                is ImageResource.Painter -> {
                    Icon(
                        painter = image.painter,
                        contentDescription = contentDescription,
                        tint = contentColor,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                is ImageResource.ImageVector -> {
                    Icon(
                        imageVector = image.imageVector,
                        contentDescription = contentDescription,
                        tint = contentColor,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
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