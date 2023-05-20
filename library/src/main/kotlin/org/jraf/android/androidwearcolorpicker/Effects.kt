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

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import kotlin.math.cos
import kotlin.math.hypot

/**
 * Unfortunately it doesn't seem possible to know the duration of a spring animation, so this is an approximation.
 */
internal const val REVEAL_ANIMATION_DURATION = 600L

/**
 * Implements a reveal effect where a circle is drawn on top of the content, centered on a given color item.
 */
internal fun Modifier.reveal(
    containerSize: IntSize,
    revealParams: RevealParams,
    radiusFactor: Float,
) = drawWithCache {
    onDrawWithContent {
        drawContent()
        val radius = maxOf(
            hypot(revealParams.center.x, revealParams.center.y),
            hypot(revealParams.center.x, containerSize.height - revealParams.center.y),
            hypot(containerSize.width - revealParams.center.x, revealParams.center.y),
            hypot(containerSize.width - revealParams.center.x, containerSize.height - revealParams.center.y),
        )
        drawCircle(
            color = revealParams.color,
            radius = radius * radiusFactor,
            center = revealParams.center,
        )
    }
}

internal data class RevealParams(
    val center: Offset,
    val color: Color,
)

internal val listItemEffectTransformOrigin = TransformOrigin(1.0f, .5f)

/**
 * Implements a translation and rotation on the list items, giving the impression of them being on a wheel.
 */
internal fun Modifier.listItemEffect(
    containerSize: IntSize,
    listState: LazyListState,
    listItemIndex: Int,
    listItemHeight: Dp,
    rotationFactor: Float,
) = graphicsLayer {
    val itemOffsetPx =
        (listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == listItemIndex }?.offset ?: 0) -
                containerSize.height / 2 +
                listItemHeight.toPx() / 2
    transformOrigin = listItemEffectTransformOrigin
    rotationZ = -rotationFactor * (itemOffsetPx / containerSize.height)
    translationX = (cos(3 * (itemOffsetPx / containerSize.height)) - 1) * -rotationFactor * 10
}
