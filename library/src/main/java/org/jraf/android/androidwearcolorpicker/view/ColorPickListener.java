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
package org.jraf.android.androidwearcolorpicker.view;

public interface ColorPickListener {
    /**
     * Called every time the user picks a color, including while adjusting the different knobs.
     * @param pickedColor The picked color.
     */
    void onColorPicked(int pickedColor);

    /**
     * Called when the user presses the "OK" top half circle, indicating s/he has chosen a color.
     * @param pickedColor The chosen color.
     */
    void onOkPressed(int pickedColor);

    /**
     * Called when the user presses the "Cancel" bottom half circle, indicating s/he does not want to pick a color.
     */
    void onCancelPressed();
}
