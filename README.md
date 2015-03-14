Android Wear Color Picker
===

A color picker activity optimized for Android Wear.  Handy for watch face settings.

The UI presents a wheel to select the base color (hue), and two half wheels for the saturation and light.

The chosen color is shown on the center, with a check mark icon that the user can tap to confirm.

The previous color (or black) is shown below the chosen color, with a cross icon to cancel.


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
    compile 'org.jraf:android-wear-color-picker:1.0.0'
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


Credits
---

The code is **strongly** based on the [HoloColorPicker](https://github.com/LarsWerkman/HoloColorPicker) library by Lars Werkman.


License
---

```
Copyright (C) 2015 Benoit 'BoD' Lubek (BoD@JRAF.org)

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