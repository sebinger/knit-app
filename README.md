# StitchTrack

Hello teacher!

This is an Android app for tracking knitting projects: progress, counters, working time, notes, and PDF patterns.

The debug APK is built in GitHub Actions. Android Studio is not required to obtain the APK.

## How to Get the APK

1. Open the repository on GitHub.
2. Go to the **Actions** tab.
3. Select the **Android Debug APK** workflow.
4. Click **Run workflow** and confirm the run on the `main` branch.
5. Wait for the build to finish. A green checkmark means that `assembleDebug` completed successfully.
6. In the completed workflow run, open **Artifacts**.
7. Download `stitchtrack-debug-apk`.
8. Extract the archive and install `app-debug.apk` on an Android device running API 26 or later.

Direct link to workflow runs:

```text
[https://github.com/sebinger/knit-app/actions/workflows/android-debug.yml](https://github.com/sebinger/knit-app/actions/workflows/android-debug.yml)
```

The APK appears in the workflow artifacts after a successful run. GitHub stores artifacts from this build for a limited time, so run the workflow again if necessary.

## How to Open the Project

The project can be opened in Android Studio, but Android Studio is not required to build the APK: the build runs automatically in GitHub Actions.

1. Clone the repository:

```bash
git clone [https://github.com/sebinger/knit-app.git](https://github.com/sebinger/knit-app.git)
cd knit-app
```

2. Open the folder in Android Studio.
3. Wait for Gradle Sync to complete.
4. Select an emulator or a connected Android device.
5. Click **Run**.

## Local Build

If JDK 17 and the Android SDK are installed, `local.properties` is not needed in the repository: the SDK path is provided through the `ANDROID_HOME` environment variable.

```bash
./gradlew assembleDebug
```

On Windows:

```bat
gradlew.bat assembleDebug
```

APK location:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Tests

```bash
./gradlew testDebugUnitTest
```

## Features

- Create and edit projects
- Colored status tags: Not Started, In Progress, Completed, Paused
- Counters for rows, stitches, and repeats
- Main counter and progress calculation
- Undo the last counter change
- Targets and progress toward a target
- Knitting timer with pause and total time
- Notes
- Select and view a PDF pattern
- Fully offline operation

## Technologies

- Kotlin
- Jetpack Compose
- Material 3
- Room
- Hilt
- Navigation Compose
- Coroutines and Flow
- JUnit

## Requirements

| Tool | Version |
|---|---|
| Android Studio | Latest stable version (for opening the project; not required to obtain the APK) |
| JDK | 17. This version is used by GitHub Actions. JDK 17 or newer works locally |
| Android SDK | Installed automatically on CI. Locally, install it through Android Studio. `compileSdk` 37 |
| Device | Emulator or Android device with API 26+ |

SDK versions are defined in `gradle/libs.versions.toml`: `minSdk` 26 and `compileSdk` 37. Android Gradle Plugin 9.4.1 and Gradle Wrapper 9.6.0 are used.

## Project Structure

```text
app/src/main/java/com/stitchtrack/app/
├─ core/
├─ data/
├─ domain/
└─ feature/
```

## Known Limitations

- The app works offline.
- There are no user accounts or synchronization.
- There is no dark theme.
- One PDF pattern is supported per project.
- Counters are not grouped into sections.
