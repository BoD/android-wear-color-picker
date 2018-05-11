/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015-2018 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.androidwearcolorpicker.app

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView

class ColorAdapter(
    context: Context,
    private val colorPickCallbacks: (Int, ImageView) -> Unit
) :
    RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
    companion object {
        const val HUE_COUNT = 12
        const val SATURATION_COUNT = 3
        const val VALUE_COUNT = 3

        const val MID_POSITION =
            Int.MAX_VALUE / 2 - ((Int.MAX_VALUE / 2) % (HUE_COUNT * SATURATION_COUNT + 1))
    }

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    class ViewHolder(val binding: AwcpColorPickItemBinding) : RecyclerView.ViewHolder(binding.root)

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
        val position = realPosition % (HUE_COUNT * SATURATION_COUNT + 1)
        val hue =
            if (position == 0) 0F else (((position - 1) / SATURATION_COUNT) / HUE_COUNT.toFloat()) * 360f
        val saturation =
            if (position == 0) 0F else (((position - 1) % SATURATION_COUNT) + 1) / (SATURATION_COUNT.toFloat())
        for (i in 0 until VALUE_COUNT) {
            var value =
                if (position == 0) i / (VALUE_COUNT - 1).toFloat() else (i + 1) / VALUE_COUNT.toFloat()
            // Make the first values darker
            value = Math.pow(value.toDouble(), 1.5).toFloat()

            val imgColor = holder.binding.ctnColors.getChildAt(i) as ImageView
            val color = Color.HSVToColor(floatArrayOf(hue, saturation, value))
            (imgColor.drawable as GradientDrawable).setColor(color)
            imgColor.setOnClickListener { colorPickCallbacks(color, imgColor) }
        }
    }

    override fun getItemCount() = Int.MAX_VALUE
}

