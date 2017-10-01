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
 * Copyright (C) 2012 Lars Werkman
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
package org.jraf.android.androidwearcolorpicker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.jraf.android.androidwearcolorpicker.R;

public class ColorPickView extends View {
    private static final int[] HUE_COLORS = new int[] {0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000};
    public static final float[] LIGHT_POSITIONS = new float[] {.5f, .75f, 1f};
    public static final float[] SATURATION_POSITIONS = new float[] {0f, .5f};

    // Color (hue) wheel
    private Paint mColorWheelPaint;
    private RectF mColorWheelRect = new RectF();
    private float mColorWheelRadius;
    private float mColorAngleRad;

    // Saturation arc
    private Paint mSaturationArcPaint;
    private int[] mSaturationColors = new int[2];
    private float mSaturationAngleRad;
    private RectF mSaturationLightRect = new RectF();
    private float mSaturationLightWheelRadius;

    // Light arc
    private Paint mLightArcPaint;
    private int[] mLightColors = new int[3];
    private float mLightAngleRad;

    // Confirm half circle
    private Paint mConfirmHalfCirclePaint;
    private RectF mConfirmCancelRectangle = new RectF();
    private float mConfirmCancelRadius;
    private Paint mConfirmCancelIconsPaint;
    private Bitmap mConfirmBitmap;

    // Cancel half circle
    private Bitmap mCancelBitmap;

    // Indicator / separator
    private Paint mIndicatorPaint;

    // Dimens
    private float mStrokePx;
    private int mSpacerPx;
    private int mIndicatorStrokePx;

    private ColorPickListener mListener;

    private int mTranslationOffset;
    private boolean mIsInColorWheel;
    private boolean mIsInSaturationArc;
    private boolean mIsInLightArc;
    private boolean mIsInCancel;
    private boolean mIsInOk;
    private float[] mHsl = new float[3];
    private float[] mHsv = new float[3];
    private int mPickedColor;
    private int mOldColor;

    public ColorPickView(Context context) {
        super(context);
        init();
    }

    public ColorPickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ColorPickView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mStrokePx = getResources().getDimensionPixelSize(R.dimen.awcp_stroke_width);
        mSpacerPx = getResources().getDimensionPixelSize(R.dimen.awcp_spacer);
        mIndicatorStrokePx = getResources().getDimensionPixelSize(R.dimen.awcp_indicator_stroke_width);

        // Color
        mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Shader shader = new SweepGradient(0, 0, HUE_COLORS, null);
        mColorWheelPaint.setShader(shader);
        mColorWheelPaint.setStyle(Paint.Style.STROKE);
        mColorWheelPaint.setStrokeWidth(mStrokePx);

        // Saturation
        mSaturationArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSaturationArcPaint.setStyle(Paint.Style.STROKE);
        mSaturationArcPaint.setStrokeWidth(mStrokePx);
        mSaturationColors[1] = 0xFF808080; // grey

        // Light
        mLightArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLightArcPaint.setStyle(Paint.Style.STROKE);
        mLightArcPaint.setStrokeWidth(mStrokePx);
        mLightColors[0] = 0xFF000000; // black
        mLightColors[2] = 0xFFFFFFFF; // white

        // Confirm
        mConfirmHalfCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mConfirmHalfCirclePaint.setStyle(Paint.Style.FILL);
        mConfirmCancelIconsPaint = new Paint();
        mConfirmBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.awcp_confirm);

        // Cancel
        mCancelBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.awcp_cancel);

        // Indicator / separator
        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setStyle(Paint.Style.STROKE);

        // Default old color
        setOldColor(0xFF000000); // black
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mTranslationOffset, mTranslationOffset);

        // Background
        canvas.drawColor(0xFF000000); // black

        // This is the "position" between 0 and 1 (in other words, a number of turns of the circle)
        float huePosition = (float) (mColorAngleRad / (2 * Math.PI));
        if (huePosition < 0) huePosition += 1;
        int hueColor = getColorOnGradient(huePosition, HUE_COLORS);

        // Color wheel
        canvas.drawOval(mColorWheelRect, mColorWheelPaint);

        // Saturation arc
        mSaturationColors[0] = hueColor;
        Shader shader = new SweepGradient(0, 0, mSaturationColors, SATURATION_POSITIONS);
        mSaturationArcPaint.setShader(shader);
        canvas.drawArc(mSaturationLightRect, 0, 180, false, mSaturationArcPaint);

        // Light arc
        mLightColors[1] = hueColor;
        shader = new SweepGradient(0, 0, mLightColors, LIGHT_POSITIONS);
        mLightArcPaint.setShader(shader);
        canvas.drawArc(mSaturationLightRect, 180, 180, false, mLightArcPaint);

        // Confirm half circle
        float hue = (float) Math.toDegrees(mColorAngleRad); // rad to deg
        if (hue < 0) hue = hue + 360;
        hue = 360 - hue; // invert
        mHsl[0] = hue;

        double saturationTurn = mSaturationAngleRad / (2 * Math.PI); // rad to turn
        saturationTurn = saturationTurn * 2; // circle to half circle
        saturationTurn = 1 - saturationTurn; // invert direction
        mHsl[1] = (float) saturationTurn;

        double lightTurn = mLightAngleRad / (2 * Math.PI); // rad to turn
        if (lightTurn < 0) lightTurn += 1;
        lightTurn = (lightTurn - .5) * 2; // circle to half circle
        mHsl[2] = (float) lightTurn;

        hslToHsv(mHsl, mHsv);
        mPickedColor = Color.HSVToColor(mHsv);
        // Inform listener
        if (mListener != null) mListener.onColorPicked(mPickedColor);

        mConfirmHalfCirclePaint.setColor(mPickedColor);
        canvas.drawArc(mConfirmCancelRectangle, 180, 180, false, mConfirmHalfCirclePaint);
        if (mIsInOk) {
            // Specific color for the pressed state (either darker or lighter, depending on current light)
            int pressedColor;
            if (mHsl[2] > .9f) {
                // Darker
                pressedColor = 0x22000000; // black
            } else {
                // Lighter
                pressedColor = 0x44FFFFFF; // white
            }
            mConfirmHalfCirclePaint.setColor(pressedColor);
            canvas.drawArc(mConfirmCancelRectangle, 180, 180, false, mConfirmHalfCirclePaint);
        }

        // Confirm icon
        int iconLeft = -mConfirmBitmap.getWidth() / 2;
        float iconTop = (-mTranslationOffset + mStrokePx * 2 + mSpacerPx) / 2 - mConfirmBitmap.getHeight() / 2;
        // Draw it with an inverted color so it is always visible
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(invertColor(mPickedColor), PorterDuff.Mode.SRC_ATOP);
        mConfirmCancelIconsPaint.setColorFilter(colorFilter);
        canvas.drawBitmap(mConfirmBitmap, iconLeft, iconTop, mConfirmCancelIconsPaint);

        // Cancel half circle
        mConfirmHalfCirclePaint.setColor(mOldColor);
        canvas.drawArc(mConfirmCancelRectangle, 0, 180, false, mConfirmHalfCirclePaint);
        if (mIsInCancel) {
            // Specific color for the pressed state (either darker or lighter, depending on current light)
            int pressedColor;
            float[] oldHsv = new float[3];
            Color.colorToHSV(mOldColor, oldHsv);
            float[] oldHsl = new float[3];
            if (oldHsl[2] > .9f) {
                // Darker
                pressedColor = 0x22000000; // black
            } else {
                // Lighter
                pressedColor = 0x44FFFFFF; // white
            }
            mConfirmHalfCirclePaint.setColor(pressedColor);
            canvas.drawArc(mConfirmCancelRectangle, 0, 180, false, mConfirmHalfCirclePaint);
        }

        // Cancel icon
        iconLeft = -mCancelBitmap.getWidth() / 2;
        iconTop = (mTranslationOffset - mStrokePx * 2 - mSpacerPx) / 2 - mCancelBitmap.getWidth() / 2;
        // Draw it with an inverted color so it is always visible
        colorFilter = new PorterDuffColorFilter(invertColor(mOldColor), PorterDuff.Mode.SRC_ATOP);
        mConfirmCancelIconsPaint.setColorFilter(colorFilter);
        canvas.drawBitmap(mCancelBitmap, iconLeft, iconTop, mConfirmCancelIconsPaint);

        // Separator
        mIndicatorPaint.setStrokeWidth(mSpacerPx);
        mIndicatorPaint.setColor(0xFF000000); // black
        canvas.drawLine(-mTranslationOffset + mStrokePx, 0, mTranslationOffset - mStrokePx, 0, mIndicatorPaint);

        // Color indicator
        mIndicatorPaint.setStrokeWidth(mIndicatorStrokePx);
        int invertColor = Color.rgb(255 - Color.red(hueColor), 255 - Color.green(hueColor), 255 - Color.blue(hueColor));
        mIndicatorPaint.setColor(invertColor);
        double cos = Math.cos(mColorAngleRad);
        double sin = Math.sin(mColorAngleRad);
        int startX = (int) ((mTranslationOffset - mStrokePx / 2) * cos);
        int startY = (int) ((mTranslationOffset - mStrokePx / 2) * sin);
        canvas.drawCircle(startX, startY, mStrokePx / 3, mIndicatorPaint);

        // Saturation indicator
        // Color
        int saturationColor = getColorOnGradient(1f - (float) saturationTurn, mSaturationColors);
        float[] invertHsv = new float[3];
        Color.colorToHSV(saturationColor, invertHsv);
        invertHsv[0] = (invertHsv[0] + 180) % 360; // Invert hue
        invertHsv[2] = 1f; // Max up value
        invertColor = Color.HSVToColor(invertHsv);
        mIndicatorPaint.setColor(invertColor);
        // Position
        cos = Math.cos(mSaturationAngleRad);
        sin = Math.sin(mSaturationAngleRad);
        startX = (int) ((mTranslationOffset - mStrokePx / 2 - mStrokePx - mSpacerPx) * cos);
        startY = (int) ((mTranslationOffset - mStrokePx / 2 - mStrokePx - mSpacerPx) * sin);
        canvas.drawCircle(startX, startY, mStrokePx / 3, mIndicatorPaint);

        // Light indicator
        // Color
        int lightColor = getColorOnGradient(1f - (float) lightTurn, mLightColors);
        Color.colorToHSV(lightColor, invertHsv);
        invertHsv[0] = (invertHsv[0] + 180) % 360; // Invert hue
        invertColor = Color.HSVToColor(invertHsv);
        mIndicatorPaint.setColor(invertColor);
        // Position
        cos = Math.cos(mLightAngleRad);
        sin = Math.sin(mLightAngleRad);
        startX = (int) ((mTranslationOffset - mStrokePx / 2 - mStrokePx - mSpacerPx) * cos);
        startY = (int) ((mTranslationOffset - mStrokePx / 2 - mStrokePx - mSpacerPx) * sin);
        canvas.drawCircle(startX, startY, mStrokePx / 3, mIndicatorPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        mTranslationOffset = min / 2;

        mColorWheelRadius = (min - mStrokePx) / 2;
        mColorWheelRect.set(-mColorWheelRadius, -mColorWheelRadius, mColorWheelRadius, mColorWheelRadius);

        mSaturationLightWheelRadius = mColorWheelRadius - mSpacerPx - mStrokePx;
        mSaturationLightRect.set(-mSaturationLightWheelRadius, -mSaturationLightWheelRadius, mSaturationLightWheelRadius, mSaturationLightWheelRadius);

        mConfirmCancelRadius = mSaturationLightWheelRadius - mSpacerPx - mStrokePx / 2;
        mConfirmCancelRectangle.set(-mConfirmCancelRadius, -mConfirmCancelRadius, mConfirmCancelRadius, mConfirmCancelRadius);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);

        // Convert coordinates to our internal coordinate system
        float x = event.getX() - mTranslationOffset;
        float y = event.getY() - mTranslationOffset;

        float angle = (float) Math.atan2(y, x);
        double distanceFromCenter = Math.sqrt(x * x + y * y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (distanceFromCenter >= mColorWheelRadius - mStrokePx / 2 && distanceFromCenter < mColorWheelRadius + mStrokePx / 2) {
                    // The point is in the color wheel
                    mIsInColorWheel = true;
                    mColorAngleRad = angle;
                    invalidate();
                } else if (distanceFromCenter >= mSaturationLightWheelRadius - mStrokePx / 2 &&
                        distanceFromCenter < mSaturationLightWheelRadius + mStrokePx / 2) {
                    // The point is in the saturation / light wheel
                    if (y >= 0) {
                        // The point is in the saturation arc
                        mIsInSaturationArc = true;
                        mSaturationAngleRad = angle;
                    } else {
                        // The point is in the light arc
                        mIsInLightArc = true;
                        mLightAngleRad = angle;
                    }
                    invalidate();
                } else if (distanceFromCenter < mSaturationLightWheelRadius - mStrokePx / 2) {
                    // The point is in the OK / Cancel center circle
                    if (y >= 0) {
                        // The point is in the Cancel half circle
                        mIsInCancel = true;
                    } else {
                        // The point is in the OK half circle
                        mIsInOk = true;
                    }
                    invalidate();
                } else {
                    // If user did not press pointer or center, report event not handled
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsInColorWheel) {
                    mIsInCancel = false;
                    mIsInOk = false;
                    mColorAngleRad = angle;
                    invalidate();
                } else if (mIsInSaturationArc) {
                    mIsInCancel = false;
                    mIsInOk = false;
                    if (y < 0) {
                        // Jump outside the saturation arc
                        return false;
                    }
                    mSaturationAngleRad = angle;
                    invalidate();
                } else if (mIsInLightArc) {
                    mIsInCancel = false;
                    mIsInOk = false;
                    if (y >= 0) {
                        // Jump outside the light arc
                        return false;
                    }
                    mLightAngleRad = angle;
                    invalidate();
                } else if (mIsInOk) {
                    if (distanceFromCenter >= mSaturationLightWheelRadius - mStrokePx / 2 || y >= 0) {
                        // Jump outside the half circle
                        mIsInOk = false;
                        invalidate();
                    }
                } else if (mIsInCancel) {
                    if (distanceFromCenter >= mSaturationLightWheelRadius - mStrokePx / 2 || y < 0) {
                        // Jump outside the half circle
                        mIsInCancel = false;
                        invalidate();
                    }
                } else {
                    // The point was nowhere interesting
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mIsInCancel) {
                    onCancelPressed();
                } else if (mIsInOk) {
                    onOkPressed();
                }
                mIsInCancel = false;
                mIsInOk = false;
                mIsInColorWheel = false;
                mIsInLightArc = false;
                mIsInSaturationArc = false;
                invalidate();
                break;
        }
        return true;
    }

    /**
     * Calculate the color using the supplied position on a gradient.
     *
     * @param position The selected color's position (from 0 to 1).
     * @param colors The colors of the gradient.
     * @return The ARGB value of the color on the gradient at the specified position.
     */
    private int getColorOnGradient(float position, int[] colors) {
        if (position <= 0) {
            return colors[0];
        }
        if (position >= 1) {
            return colors[colors.length - 1];
        }

        // Get the value of the position in relation to the colors
        float span = position * (colors.length - 1);
        // Index of the nearest color
        int colorIndex = (int) span;
        // Factor between 0 and 1 to calculate how close of colorA or colorB we are
        float factor = span - colorIndex;

        int colorA = colors[colorIndex];
        int colorB = colors[colorIndex + 1];
        int r = average(Color.red(colorA), Color.red(colorB), factor);
        int g = average(Color.green(colorA), Color.green(colorB), factor);
        int b = average(Color.blue(colorA), Color.blue(colorB), factor);

        return Color.rgb(r, g, b);
    }

    private int average(int a, int b, float factor) {
        return a + Math.round(factor * (b - a));
    }

    /**
     * Convert an HSL color to HSV.<br/>
     * Source:
     * - http://en.wikipedia.org/wiki/HSL_and_HSV and
     * - http://ariya.blogspot.com.ar/2008/07/converting-between-hsl-and-hsv.html
     *
     * @param inHsl the HSL color to convert.
     * @param outHsv the converted HSV color.
     */
    private static void hslToHsv(float[] inHsl, float[] outHsv) {
        outHsv[0] = inHsl[0];

        float s = inHsl[1];
        float l = inHsl[2] * 2;
        if (l <= 1) {
            s *= l;
        } else {
            s *= 2 - l;
        }

        outHsv[1] = (2 * s) / (l + s);
        outHsv[2] = (l + s) / 2;
    }

    /**
     * Convert an HSV color to HSL.<br/>
     * Source:
     * - http://en.wikipedia.org/wiki/HSL_and_HSV and
     * - http://ariya.blogspot.com.ar/2008/07/converting-between-hsl-and-hsv.html
     *
     * @param inHsv the HSV color to convert.
     * @param outHsl the converted HSL color.
     */
    private static void hsvToHsl(float[] inHsv, float[] outHsl) {
        outHsl[0] = inHsv[0];

        float l = (2 - inHsv[1]) * inHsv[2];
        float s = inHsv[1] * inHsv[2];
        if (l == 0 || l == 2) {
            s = 0;
        } else if (l <= 1) {
            s /= l;
        } else {
            s /= 2 - l;
        }

        outHsl[1] = s;
        outHsl[2] = l / 2;
    }

    private static int invertColor(int color) {
        return ((~color) | 0xFF000000) | (color & 0xFF000000);
    }

    /**
     * Sets the listener to use for callbacks.
     *
     * @param listener The listener to use for callbacks.
     */
    public void setListener(ColorPickListener listener) {
        mListener = listener;
    }

    /**
     * Sets the old color to show on the bottom "Cancel" half circle, and also the initial value for the picked color.
     * The default value is black.
     *
     * @param oldColor The old color to use as an ARGB int (the alpha component is ignored).
     */
    public void setOldColor(int oldColor) {
        mOldColor = oldColor;
        float[] hsv = new float[3];
        float[] hsl = new float[3];
        Color.colorToHSV(oldColor, hsv);
        hsvToHsl(hsv, hsl);
        mColorAngleRad = (float) Math.toRadians(-hsl[0]);
        mSaturationAngleRad = (float) (2 * Math.PI * (1 - hsl[1]) / 2);
        mLightAngleRad = (float) (2 * Math.PI * (.5 + hsl[2] / 2));
        invalidate();
    }

    /**
     * Returns the color picked by the user.
     *
     * @return The picked color as an ARGB int.
     */
    public int getPickedColor() {
        return mPickedColor;
    }

    private void onOkPressed() {
        if (mListener != null) mListener.onOkPressed(mPickedColor);
    }

    private void onCancelPressed() {
        if (mListener != null) mListener.onCancelPressed();
    }
}
