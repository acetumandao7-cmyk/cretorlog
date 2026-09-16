# CreatorLog

Native Android mobile application using Kotlin + Jetpack Compose.

## Initial setup
- Package: `com.example.creatorlog`
- Minimum SDK: 26
- Target SDK: 35
- UI: Jetpack Compose

## Current milestone
The project contains only the clean base Android app. Backend, MySQL, authentication, notifications, and feature modules will be added later.

## Physical phone testing
1. Enable Developer Options on the Android phone.
2. Enable USB debugging.
3. Connect the phone with a data-capable USB cable.
4. Verify with:
   `adb devices`
5. Build/install with:
   `gradlew.bat installDebug`
6. Launch the CreatorLog app on the phone.
