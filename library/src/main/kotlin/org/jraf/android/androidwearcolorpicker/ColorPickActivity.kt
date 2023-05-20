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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import kotlinx.parcelize.Parcelize
import org.jraf.android.androidwearcolorpicker.ColorPickActivity.Contract.Companion.EXTRA_REQUEST
import org.jraf.android.androidwearcolorpicker.ColorPickActivity.Contract.Companion.EXTRA_RESULT

class ColorPickActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ColorPicker(
                pickedColor = @Suppress("DEPRECATION") intent.getParcelableExtra<Contract.PickRequest>(EXTRA_REQUEST)?.pickedColor,
                onColorPicked = { pickedColor ->
                    setResult(
                        RESULT_OK,
                        Intent().putExtra(
                            EXTRA_RESULT,
                            Contract.PickResult(pickedColor = pickedColor)
                        )
                    )
                    finish()
                }
            )
        }
    }

    class Contract : ActivityResultContract<Contract.PickRequest, Contract.PickResult?>() {
        companion object {
            internal const val EXTRA_REQUEST = "EXTRA_REQUEST"
            internal const val EXTRA_RESULT = "EXTRA_RESULT"
        }

        @Parcelize
        data class PickRequest(val pickedColor: Int? = null) : Parcelable

        @Parcelize
        data class PickResult(val pickedColor: Int) : Parcelable

        override fun createIntent(context: Context, input: PickRequest) = Intent(context, ColorPickActivity::class.java)
            .putExtra(EXTRA_REQUEST, input)

        override fun parseResult(resultCode: Int, intent: Intent?): PickResult? {
            @Suppress("DEPRECATION")
            return intent?.getParcelableExtra(EXTRA_RESULT)
        }
    }
}
