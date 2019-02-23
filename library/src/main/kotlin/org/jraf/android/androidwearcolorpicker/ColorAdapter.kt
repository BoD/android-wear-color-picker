/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015-2019 Benoit 'BoD' Lubek (BoD@JRAF.org)
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

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import org.jraf.android.androidwearcolorpicker.ColorAdapter.Companion.HUE_COUNT
import org.jraf.android.androidwearcolorpicker.ColorAdapter.Companion.SATURATION_COUNT
import org.jraf.android.androidwearcolorpicker.databinding.AwcpColorPickItemBinding
import kotlin.math.ceil
import kotlin.math.cos

class ColorAdapter(
    context: Context,
    private val colors: IntArray?,
    private val colorPickCallbacks: (Int, ImageView) -> Unit
) :
    RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
    companion object {
        const val HUE_COUNT = 36
        const val SATURATION_COUNT = 3
        const val VALUE_COUNT = 4
        private const val VALUE_MIN = .15F

        /**
         * Turns a wrapped adapter position into a hue-saturation pair.
         *
         * If you want to use this as a full HSV triple, use [floatArrayOf] and the spread operator.
         *
         * @param position adapter position of the color row, mod ([HUE_COUNT] * [SATURATION_COUNT] + 1)
         * @return a [FloatArray] with two elements: the hue and saturation values
         */
        private fun positionToHS(position: Int) = if (position == 0) floatArrayOf(0f, 0f) else floatArrayOf(
            (((position - 1) / SATURATION_COUNT) / HUE_COUNT.toFloat()) * 360f,
            (((position - 1) % SATURATION_COUNT) + 1) / (SATURATION_COUNT.toFloat())
        )

        /**
         * Turns a wrapped adapter position and a row index into a [Color].
         *
         * @param position adapter position of the color row, mod ([HUE_COUNT] * [SATURATION_COUNT] + 1)
         * @return the corresponding [Int] color
         */
        fun positionAndSubPositionToColor(position: Int, subPosition: Int) = Color.HSVToColor(
            floatArrayOf(
                *positionToHS(position),
                // Special case for white: no minimum
                if (position == 0) subPosition / (VALUE_COUNT - 1).toFloat()
                // Other colors
                else VALUE_MIN + (subPosition.toFloat() / (VALUE_COUNT - 1)) * (1F - VALUE_MIN)
            )
        )

        /**
         * Approximates a hue-saturation pair's adapter position.
         *
         * Due to numerical errors, [positionToHS] is not trivially reversible; this function should
         * therefore not be used as is in an attempt to revert the conversion.
         *
         * @param hs the [Color.colorToHSV] components of a color, although only hue and saturation are needed
         * @return a positive integer such that, when [positionToHS] is applied to it, the result is
         *         close to the input values
         */
        private fun hsToPosition(hs: FloatArray) =
            if (ceil(hs[1] * SATURATION_COUNT).toInt() == 0) 0
            else ceil(hs[0] / 360f * HUE_COUNT).toInt() * SATURATION_COUNT +
                    ceil(hs[1] * SATURATION_COUNT).toInt() // - 1 + 1

        /**
         * Computes a hue-saturation pair's most fitting adapter position.
         *
         * This actually compares positions surrounding the output of [hsToPosition] regarding their fitness to
         * represent the hue-saturation pair.
         *
         * @param hs the [Color.colorToHSV] components of a color, although only hue and saturation are needed
         * @return a positive integer such that, when [positionToHS] is applied to it, the result is the closest
         * possible to the input values
         */
        private fun hsToNearestPosition(hs: FloatArray): Int {
            val position = hsToPosition(hs)
            if (position == 0) {
                return position
            }
            var nearest = position
            var minDistanceSquared: Double = Double.POSITIVE_INFINITY
            for (i in -2 * SATURATION_COUNT..2 * SATURATION_COUNT) {
                val correctedPosition = when {
                    position + i <= 0 -> position + HUE_COUNT * SATURATION_COUNT + i
                    position + i > HUE_COUNT * SATURATION_COUNT -> position + i - HUE_COUNT * SATURATION_COUNT
                    else -> position + i
                } // in case the saturation==0 line is crossed
                val trying = positionToHS(correctedPosition)
                val a2 = hs[1].toDouble() * hs[1].toDouble()
                val b2 = trying[1].toDouble() * trying[1].toDouble()
                val ab = hs[1].toDouble() * trying[1].toDouble()
                val gamma = 2.0 * Math.PI * (hs[0].toDouble() - trying[0].toDouble()) / 360.0
                val c2 = a2 + b2 - 2.0 * ab * cos(gamma) // Law of cosines
                if (c2 < minDistanceSquared) {
                    nearest = correctedPosition
                    minDistanceSquared = c2
                }
            }
            return nearest
        }
    }

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    class ViewHolder(val binding: AwcpColorPickItemBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        if (colors != null && (colors.size < VALUE_COUNT || colors.size % VALUE_COUNT != 0)) {
            throw IllegalArgumentException("colors.size must be at least, and a multiple of $VALUE_COUNT")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<AwcpColorPickItemBinding>(
            layoutInflater,
            R.layout.awcp_color_pick_item,
            parent,
            false
        )!!
        for (i in 0 until VALUE_COUNT) {
            binding.ctnColors.addView(
                layoutInflater.inflate(R.layout.awcp_color_pick_item_color, binding.ctnColors, false)
            )
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, realPosition: Int) {
        if (colors != null) {
            // Specific colors mode
            val position = (realPosition % (colors.size / VALUE_COUNT)) * VALUE_COUNT
            for (i in 0 until VALUE_COUNT) {
                val imgColor = holder.binding.ctnColors.getChildAt(i) as ImageView
                val color = colors[position + i]
                (imgColor.drawable as GradientDrawable).setColor(color)
                imgColor.setOnClickListener { colorPickCallbacks(color, imgColor) }
            }
        } else {
            // "Rainbow" mode
            val position = realPosition % (HUE_COUNT * SATURATION_COUNT + 1)
            for (i in 0 until VALUE_COUNT) {
                val imgColor = holder.binding.ctnColors.getChildAt(i) as ImageView
                val color = positionAndSubPositionToColor(position, i)
                (imgColor.drawable as GradientDrawable).setColor(color)
                imgColor.setOnClickListener { colorPickCallbacks(color, imgColor) }
            }
        }
    }

    override fun getItemCount() = Int.MAX_VALUE

    fun getMiddlePosition(): Int {
        val actualNumberOfLines = if (colors != null) {
            colors.size / VALUE_COUNT
        } else {
            // Add one for the "shades of grey" line
            HUE_COUNT * SATURATION_COUNT + 1
        }
        return Int.MAX_VALUE / 2 - ((Int.MAX_VALUE / 2) % actualNumberOfLines)
    }

    /**
     * Computes a [Color]'s closest neighbour within the color picker.
     *
     * @param color the color to search a neighbour for
     * @return a pair consisting of an adapter position and an index within the value row
     */
    fun colorToPositions(color: Int): Pair<Int, Int> {
        if (colors != null) {
            // Specific colors mode
            val idx = colors.indexOf(color)
            if (idx == -1) return 0 to 0
            return idx / VALUE_COUNT to idx % VALUE_COUNT
        } else {
            // "Rainbow" mode
            val hsv = floatArrayOf(0f, 0f, 0f)
            Color.colorToHSV(color, hsv)
            val position = hsToNearestPosition(hsv)
            val value = hsv[2]
            return Pair(
                position,
                if (position == 0) ceil(value * (VALUE_COUNT - 1).toFloat()).toInt()
                else ((value - VALUE_MIN) / (1F - VALUE_MIN) * (VALUE_COUNT - 1)).toInt()
            )
        }
    }
}

