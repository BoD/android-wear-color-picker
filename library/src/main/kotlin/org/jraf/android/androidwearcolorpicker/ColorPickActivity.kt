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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.ColorInt
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.support.wear.widget.CurvingLayoutCallback
import android.support.wear.widget.WearableLinearLayoutManager
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewTreeObserver
import org.jraf.android.androidwearcolorpicker.databinding.AwcpColorPickBinding

class ColorPickActivity : Activity() {
    companion object {
        const val EXTRA_RESULT = "EXTRA_RESULT"
        const val EXTRA_OLD_COLOR = "EXTRA_OLD_COLOR"
        const val EXTRA_COLORS = "EXTRA_COLORS"

        /**
         * Extracts the picked color from an onActivityResult data Intent.
         *
         * @param data The intent passed to onActivityResult.
         * @return The resulting picked color, or 0 if the result could not be found in the given Intent.
         */
        @Suppress("unused")
        fun getPickedColor(data: Intent) = data.getIntExtra(EXTRA_RESULT, 0)
    }

    private lateinit var binding: AwcpColorPickBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.awcp_color_pick)!!
        binding.rclList.setHasFixedSize(true)
        binding.rclList.isEdgeItemsCenteringEnabled = true

        // Apply an offset + scale on the items depending on their distance from the center (only for Round screens)
        if (resources.configuration.isScreenRound) {
            binding.rclList.layoutManager =
                WearableLinearLayoutManager(this, object : CurvingLayoutCallback(this) {
                    override fun onLayoutFinished(child: View, parent: RecyclerView) {
                        super.onLayoutFinished(child, parent)

                        val childTop = child.y + child.height / 2f
                        val childOffsetFromCenter = childTop - parent.height / 2f

                        child.pivotX = 1f
                        child.rotation = -15f * (childOffsetFromCenter / parent.height)
                    }
                })

            // Also snap
            LinearSnapHelper().attachToRecyclerView(binding.rclList)
        } else {
            // Square screen: no scale effect and no snapping
            binding.rclList.layoutManager = WearableLinearLayoutManager(this)
        }

        val adapter = ColorAdapter(this, intent.getIntArrayExtra(EXTRA_COLORS)) { colorArgb, clickedView ->
            setResult(RESULT_OK, Intent().putExtra(EXTRA_RESULT, colorArgb))

            binding.vieRevealedColor.setBackgroundColor(colorArgb)

            val clickedViewRect = Rect()
            clickedView.getGlobalVisibleRect(clickedViewRect)

            val finalRadius = Math.hypot(
                binding.vieRevealedColor.width.toDouble(),
                binding.vieRevealedColor.height.toDouble()
            ).toFloat()

            val anim = ViewAnimationUtils.createCircularReveal(
                binding.vieRevealedColor,
                clickedViewRect.centerX(),
                clickedViewRect.centerY(),
                clickedViewRect.width() / 2F - 4,
                finalRadius
            )

            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    finish()
                }
            })

            binding.vieRevealedColor.visibility = View.VISIBLE
            anim.start()
        }
        binding.rclList.adapter = adapter

        val initialPosition = ColorAdapter.MID_POSITION +
                if (intent?.hasExtra(EXTRA_OLD_COLOR) != true) 0
                else adapter.colorToPositions(
                    intent!!.getIntExtra(
                        EXTRA_OLD_COLOR,
                        Color.WHITE
                    )
                ).first

        binding.rclList.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val oldColor = intent!!.getIntExtra(EXTRA_OLD_COLOR, Color.WHITE)
                binding.vieRevealedColor.setBackgroundColor(oldColor)
                binding.vieRevealedColor.visibility = View.VISIBLE

                val selectedViewIdx = adapter.colorToPositions(oldColor).second
                val selectedView = (binding.rclList.layoutManager!!.findViewByPosition(0) as ViewGroup).getChildAt(selectedViewIdx)
                val selectedViewRect = Rect()
                selectedView.getGlobalVisibleRect(selectedViewRect)

                ViewAnimationUtils.createCircularReveal(
                    binding.vieRevealedColor,
                    selectedViewRect.centerX(),
                    selectedViewRect.centerY(),
                    Math.hypot(
                        binding.vieRevealedColor.width.toDouble(),
                        binding.vieRevealedColor.height.toDouble()
                    ).toFloat(),
                    selectedViewRect.width() / 2F - 4
                ).apply {
                    startDelay = 300
                    duration = 500
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            binding.vieRevealedColor.visibility = View.INVISIBLE
                        }
                    })
                }
                    .start()

                // For some unknown reason, this must be posted - if done right away, it doesn't work
                Handler(Looper.getMainLooper()).post {
                    binding.rclList.scrollToPosition(initialPosition)
                    binding.rclList.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    @Suppress("unused")
    class IntentBuilder {
        private var oldColor: Int = Color.WHITE
        private var colors: List<Int>? = null

        /**
         * Sets the initial value for the picked color.
         * The default value is white.
         *
         * @param oldColor The old color to use as an ARGB int (the alpha component is ignored).
         * @return This builder.
         */
        fun oldColor(@ColorInt oldColor: Int): IntentBuilder {
            this.oldColor = oldColor
            return this
        }

        /**
         * Sets the colors to display in the picker.
         * This is optional - if not specified, all the colors of the rainbow will be used.
         *
         * @param colors The colors to display as ARGB ints (the alpha component is ignored).
         * @return This builder.
         */
        fun colors(colors: List<Int>): IntentBuilder {
            this.colors = colors
            return this
        }

        /**
         * Build the resulting Intent.
         *
         * @param context The context to use to build the Intent.
         * @return The build Intent.
         */
        fun build(context: Context): Intent = Intent(context, ColorPickActivity::class.java)
            .putExtra(EXTRA_OLD_COLOR, oldColor)
            .putExtra(EXTRA_COLORS, colors?.toIntArray())
    }
}