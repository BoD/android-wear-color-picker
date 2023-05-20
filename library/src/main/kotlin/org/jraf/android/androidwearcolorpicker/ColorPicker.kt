/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jraf.android.androidwearcolorpicker

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ColorPicker(
    pickedColor: Int?,
    listItemHeight: Dp = 48.dp,
    rotationFactor: Float = 20f,
    onColorPicked: (Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val colorToPositions: Pair<Int, Int>?
        val initialFirstVisibleIndex: Int
        if (pickedColor == null) {
            colorToPositions = null
            initialFirstVisibleIndex = getMiddlePosition()
        } else {
            colorToPositions = colorToPositions(pickedColor)
            initialFirstVisibleIndex = getMiddlePosition() + colorToPositions.first

        }
        // We want the first visible item to be the middle one, so add half the number of items per screen
        val linesPerScreen = constraints.maxHeight / with(LocalDensity.current) { listItemHeight.toPx() }
        val listState = rememberLazyListState(initialFirstVisibleIndex - linesPerScreen.toInt() / 2)

        // True means reveal in, false means reveal out
        var revealIn: Boolean by remember { mutableStateOf(pickedColor == null) }
        val revealRadiusFactor by animateFloatAsState(
            targetValue = if (revealIn) 0f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
        )
        var revealParams by remember {
            mutableStateOf(
                colorToPositions?.let {
                    val (position, subPosition) = it
                    val x = subPosition * constraints.maxWidth / 4 + constraints.maxWidth / 8
                    RevealParams(
                        center = Offset(
                            x = x.toFloat(),
                            y = constraints.maxWidth / 2f
                        ),
                        color = Color(positionAndSubPositionToColor(position, subPosition))
                    )
                }
            )
        }

        @OptIn(ExperimentalFoundationApi::class)
        val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (revealParams == null) {
                        it
                    } else
                        it.reveal(
                            containerSize = constraints.toIntSize(),
                            revealParams = revealParams!!,
                            radiusFactor = revealRadiusFactor
                        )
                },
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = flingBehavior
        ) {
            items(Integer.MAX_VALUE) { listItemIndex ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(listItemHeight)
                        .listItemEffect(
                            containerSize = constraints.toIntSize(),
                            listState = listState,
                            listItemIndex = listItemIndex,
                            listItemHeight = listItemHeight,
                            rotationFactor = rotationFactor
                        )

                ) {
                    val coroutineScope = rememberCoroutineScope()
                    ColorPickerListItem(listItemIndex = listItemIndex, onColorPicked = { color, pickedColorCenterOffset ->
                        revealParams = RevealParams(
                            center = pickedColorCenterOffset,
                            color = color
                        )
                        revealIn = false
                        coroutineScope.launch {
                            // Wait for the animation to finish
                            delay(REVEAL_ANIMATION_DURATION)
                            onColorPicked(color.toArgb())
                        }
                    })
                }
            }
        }
        LaunchedEffect(Unit) {
            listState.scroll {
                // Snap to position - see https://stackoverflow.com/a/74880276
                with(flingBehavior) { performFling(-1F) }
            }
            if (pickedColor != null) revealIn = true
        }
    }
}

@Composable
private fun ColorPickerListItem(
    listItemIndex: Int,
    onColorPicked: (Color, Offset) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val position = listItemIndex % (HUE_COUNT * SATURATION_COUNT + 1)
        repeat(VALUE_COUNT) { subPosition ->
            val color = Color(positionAndSubPositionToColor(position, subPosition))
            var positionInRoot = Offset.Zero
            var size = IntSize.Zero
            Canvas(
                modifier = Modifier
                    .weight(1F)
                    .padding(horizontal = 1.dp)
                    .aspectRatio(1F)
                    .onGloballyPositioned {
                        positionInRoot = it.positionInRoot()
                        size = it.size
                    }
                    .clickable(
                        // No ripple
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onColorPicked(color, positionInRoot + Offset(size.width / 2F, size.height / 2F))
                    },
                onDraw = {
                    drawCircle(color = color)
                }
            )
        }
    }
}

private fun Constraints.toIntSize() = IntSize(width = maxWidth, height = maxHeight)
