[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android%20Wear%20Color%20Picker-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1662)

Android Wear Color Picker
===

A color picker activity optimized for Android Wear.  Handy for watch face settings.

The UI presents a wheel of colors with different hues and lightness.

![Demo](https://github.com/BoD/android-wear-color-picker/raw/master/etc/demo_opt.gif "Demo")


How to use
---

### Adding the library to your project

The aar artifact is available at the **jcenter** repository. Declare the repository and the
dependency in your `build.gradle` file:

```groovy
repositories {
    jcenter()
}

 (...)

dependencies {
    compile 'org.jraf:android-wear-color-picker:2.0.0'
}
```


### Use the library

Start the pick color activity:

```java
Intent intent = new ColorPickActivity.IntentBuilder().oldColor(oldColor).build(this);
startActivityForResult(intent, REQUEST_PICK_COLOR);
```

Get the picked color:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
        case REQUEST_PICK_COLOR:
            if (resultCode == RESULT_CANCELED) {
                // The user pressed 'Cancel'
                break;
            }

            int pickedColor = ColorPickActivity.getPickedColor(data);
            Log.d("pickedColor=" + Integer.toHexString(pickedColor));
            break;
    }
}
```

That's it!


License
---

```
Copyright (C) 2015-2018 Benoit 'BoD' Lubek (BoD@JRAF.org)

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
