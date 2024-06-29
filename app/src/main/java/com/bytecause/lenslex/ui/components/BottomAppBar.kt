package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.bytecause.lenslex.R

enum class BottomAppBarItems {
    SELECT_ALL,
    UNSELECT_ALL,
    COPY
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
                        .clickable { onItemClick(BottomAppBarItems.SELECT_ALL) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(id = R.string.select_all),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = stringResource(id = R.string.select_all),
                        textAlign = TextAlign.Center
                    )
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
                        .clickable { onItemClick(BottomAppBarItems.UNSELECT_ALL) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(id = R.string.unselect_all),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = stringResource(id = R.string.unselect_all),
                        textAlign = TextAlign.Center
                    )
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
                        .clickable { onItemClick(BottomAppBarItems.COPY) }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_content_copy_24),
                        contentDescription = stringResource(id = R.string.copy_content),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = stringResource(id = R.string.copy),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Composable
@Preview
fun BottomAppBarPreview() {
    BottomAppBar { }
}