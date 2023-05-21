# Changelog

## v3.0.0 (2023-05-21)
- The library has been rewritten using Compose
- **BREAKING** The minSdk is now 25
- Updated dependencies to latest versions
- **BREAKING** The ability to specify the colors to display has been removed. I am assuming this was not a very useful feature, and it was making the code more complex. If you need this feature, please open an issue and we can discuss it.
- Use of the ActivityResultContract API to start the activity and get the result

## v2.2.4 (2021-09-27)
- Updated dependencies to latest versions
- Artifacts are now on Maven Central, instead of JCenter

## v2.2.3 (2021-01-02)
- Support for rotary input
- Updated dependencies to latest versions and use gradle.kts

## v2.2.2 (2019-09-07)
- Migration to AndroidX

## v2.2.1 (2019-02-23)
- Fixed the landing animation in specific color mode

## v2.2.0 (2019-02-16)
- **BREAKING** Moved the package from `org.jraf.android.androidwearcolorpicker.app` to `org.jraf.android.androidwearcolorpicker`.
The old package name was a mistake, sorry about it. 
- New API to specify the colors to display instead of automatically compute them
- Improved enter/exit animations
- Added a sample app

## v2.1.0 (2019-02-03)
- Use the old color param to scroll to it (thanks to @TpmKranz)
