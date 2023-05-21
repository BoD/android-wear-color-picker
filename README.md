[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android%20Wear%20Color%20Picker-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1662)

# Wear OS Color Picker

A color picker activity optimized for Wear OS (formerly known as Android Wear).  Handy for watch face settings.

The UI presents a wheel of colors with different hues and lightness.

![Demo](https://github.com/BoD/android-wear-color-picker/raw/master/etc/demo_opt.gif "Demo")


## How to use

### Adding the library to your project

The artifact is available on Maven Central.

```kotlin
dependencies {
    implementation("org.jraf:android-wear-color-picker:3.0.0")
}
```

*Note: the artifact was hosted on JCenter in the past, but is now on Maven Central since v2.2.4*

### Use the library

The picker uses the [ActivityResultContract](https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContract) API to be launched and to return the picked color:

```kotlin
// 1. Setup the pick launcher
val colorPickLauncher = registerForActivityResult(ColorPickActivity.Contract()) { pickedColorResult ->
    if (pickedColorResult == null) {
        // The user closed the picker without picking anything
    } else {
        // Get the picked color. The result is an Int in the form 0xAARRGGBB.
        pickedColor = pickedColorResult.pickedColor
    }
}

// ...

// 2. Launch the picker. The picked color parameter is optional - if specified, the picker will start already positioned on that color.
colorPickLauncher.launch(ColorPickActivity.Contract.PickRequest(pickedColor))
```

You can also have a look at the [sample](sample/).

That's it!

## License

```
Copyright (C) 2015-present Benoit 'BoD' Lubek (BoD@JRAF.org)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
