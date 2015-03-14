/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.androidwearcolorpicker.app;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.jraf.android.androidwearcolorpicker.R;
import org.jraf.android.androidwearcolorpicker.view.ColorPickListener;
import org.jraf.android.androidwearcolorpicker.view.ColorPickView;

public class ColorPickActivity extends Activity {
    public static final String EXTRA_RESULT = "EXTRA_RESULT";
    public static final String EXTRA_OLD_COLOR = "EXTRA_OLD_COLOR";

    private static final long CONFIRM_ANIM_DURATION = 1000;

    private TextView mTextView;
    private View mConfirmLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.awcp_color_pick);
        ColorPickView colorPickView = (ColorPickView) findViewById(R.id.awcp_colorPick);
        mConfirmLayer = findViewById(R.id.awcp_confirmLayer);
        if (getIntent().hasExtra(EXTRA_OLD_COLOR)) {
            colorPickView.setOldColor(getIntent().getIntExtra(EXTRA_OLD_COLOR, 0));
        }
        colorPickView.setListener(new ColorPickListener() {
            @Override
            public void onColorPicked(int pickedColor) {}

            @Override
            public void onOkPressed(int pickedColor) {
                startConfirmAnimation(pickedColor);
            }

            @Override
            public void onCancelPressed() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void startConfirmAnimation(final int pickedColor) {
        mConfirmLayer.setBackgroundColor(pickedColor);
        mConfirmLayer.setAlpha(0);
        mConfirmLayer.animate().alpha(1).setDuration(CONFIRM_ANIM_DURATION).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                Intent result = new Intent();
                result.putExtra(EXTRA_RESULT, pickedColor);
                setResult(RESULT_OK, result);
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    public static class IntentBuilder {
        private int mOldColor;

        /**
         * Sets the old color to show on the bottom "Cancel" half circle, and also the initial value for the picked color.
         * The default value is black.
         *
         * @param oldColor The old color to use as an ARGB int (the alpha component is ignored).
         * @return This builder.
         */
        public IntentBuilder oldColor(int oldColor) {
            mOldColor = oldColor;
            return this;
        }

        /**
         * Build the resulting Intent.
         *
         * @param context The context to use to build the Intent.
         * @return The build Intent.
         */
        public Intent build(Context context) {
            Intent res = new Intent(context, ColorPickActivity.class);
            res.putExtra(EXTRA_OLD_COLOR, mOldColor);
            return res;
        }
    }

    /**
     * Extracts the picked color from an onActivityResult data Intent.
     *
     * @param data The intent passed to onActivityResult.
     * @return The resulting picked color, or 0 if the result could not be found in the given Intent.
     */
    public static int getPickedColor(Intent data) {
        return data.getIntExtra(EXTRA_RESULT, 0);
    }
}
