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

package org.jraf.android.androidwearcolorpicker.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.view.View
import android.widget.Button
import org.jraf.android.androidwearcolorpicker.ColorPickActivity

class MainActivity : WearableActivity() {

    private var pickedColor = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Enables Always-on
        setAmbientEnabled()

        // "Rainbow" mode
        findViewById<Button>(R.id.btnPickColorRainbow).setOnClickListener {
            startActivityForResult(
                ColorPickActivity.IntentBuilder()
                    .oldColor(pickedColor)
                    .build(this),
                REQUEST_PICK_COLOR
            )
        }

        // Specific colors mode
        findViewById<Button>(R.id.btnPickColorSpecific).setOnClickListener {
            startActivityForResult(
                ColorPickActivity.IntentBuilder()
                    .oldColor(pickedColor)
                    .colors(
//                        createListOfColors()

                        listOf(
                            Color.BLACK,
                            Color.DKGRAY,
                            Color.GRAY,
                            Color.LTGRAY,
                            Color.WHITE,
                            Color.RED,
                            Color.GREEN,
                            Color.BLUE,
                            Color.YELLOW,
                            Color.CYAN,
                            Color.MAGENTA,
                            0xFFBB00DD.toInt()
                        )
                    )
                    .build(this),
                REQUEST_PICK_COLOR
            )
        }
    }

    private fun createListOfColors(): List<Int> {
        val colorCount = 4 * 12
        val res = mutableListOf<Int>()
        for (i in 0 until colorCount) {
            res += Color.HSVToColor(floatArrayOf((360F / colorCount) * i.toFloat(), 1F, 1F))
        }
        return res
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_COLOR && resultCode == Activity.RESULT_OK) {
            pickedColor = ColorPickActivity.getPickedColor(data!!)
            updatePickedColor()
        }
    }

    private fun updatePickedColor() {
        findViewById<View>(R.id.pickedColor).setBackgroundColor(pickedColor)
    }

    companion object {
        private const val REQUEST_PICK_COLOR = 1
    }
}
