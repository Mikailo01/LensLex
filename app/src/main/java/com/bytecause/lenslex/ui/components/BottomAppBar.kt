package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R

enum class BottomAppBarItems {
    COPY,
    SHARE
}

@Composable
fun BottomAppBar(
    modifier: Modifier = Modifier,
    onItemClick: (BottomAppBarItems) -> Unit
) {
    androidx.compose.material3.BottomAppBar(
        actions = {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .padding(5.dp)
                        .clickable { onItemClick(BottomAppBarItems.SHARE) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share result",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(text = "Share")
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .wrapContentSize()
                        .padding(5.dp)
                        .clickable { onItemClick(BottomAppBarItems.COPY) }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_content_copy_24),
                        contentDescription = "Copy content",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(text = "Copy")
                }
            }
        },
        modifier = modifier
    )
}