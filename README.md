# Location Based Memos – Solution

## What it does
- When creating a memo, the user picks its location on a map (required).
- Within **200 m** of that location a notification shows the title and the first 140 characters of the text. Tapping it opens the memo.
- Works with the app in the foreground, background or closed. Reminds on every visit until the memo is marked as done.

## How to test
1. Run the app, tap the banner and grant all permissions (location: **"Allow all the time"**).
2. Create a memo. Its coordinates are shown on the memo's detail screen.
3. Simulate arriving there:
   ```
   scripts/simulate-location.sh 52.52000 13.40500     # use the memo's coordinates
   scripts/simulate-location.sh --reset                # afterwards: remove the fake location
   ```
   The notification appears within a few seconds.

Unit tests: `./gradlew testDebugUnitTest`

Note: after **Force stop** Android blocks the app's reminders until it is opened again (system behaviour).

## Design decisions
- **Geofencing (Play Services)** – Android watches the locations itself: battery friendly and works while the app is not running. Geofences are registered again after reboot / app update.
- **osmdroid (OpenStreetMap)** – no API key needed, so the project runs without configuration.
- **Layers**: `domain` (models, use cases, interfaces – plain Kotlin), `data` (Room), `platform` (library implementations), `presentation` (Activities + ViewModels), `di`.
- **All libraries wrapped** behind own interfaces: `GeofenceRegistrar`, `LocationPickerMap`, `MemoNotifier`, `PermissionChecker`, `MemoRepository`. This also allows simple fakes in tests.
- **Koin** – lightweight and Kotlin Multiplatform ready; a unit test verifies the dependency graph.
- **Room migration v1 → v2** – coordinates were stored as `Long` (decimals lost), now nullable `Double`; existing memos are kept.
- **targetSdk 36** – was not set (defaulted to 27), which made it impossible to request the notification permission.

## Refactoring of the base project
Main-safe repository with `suspend`/`Flow` (screens update automatically), one `UiState` per screen exposed via `asStateFlow()`, lifecycle-aware collection, `SavedStateHandle` for process death, `ListAdapter` + `DiffUtil`, removal of global singletons. Each step is a separate commit.

---

# android-codelab
Android project that serves as a base for code challenges implemented by applicants.
The base is written in kotlin. 

(!)If you have the NDK plugin installed, please disable it for the project, as errors may occur.

# Android Coding Challenges
Coding challenges are useful when the applicant does not provide a github repository or any work samples. Even if a github repository has been provided it is generally a good idea to give the applicant a task to solve and have him present his solution in a separate session. 

## General Instructions
The following instructions/conditions are valid independently of the actual coding challenge

- The code base has been tested with Android Studio Narwhal Feature Drop which is the recommended version, however feel free to try a higher version and adjust the configuration as needed
- The task should be implemented in kotlin
- Approach this task as if it was a real-world implementation - i.e. exactly how you would approach the task if you were working for a company
- 3rd party libraries may be used
- 3rd party libraries must be wrapped: They should be abstracted out, so any other library could be plugged into the solution
- The base project for this task will be provided by us
- Once completed, please send us your solution and presents it to us, followed by a discussion about the implementation and design decisions made
- The solution can be sent as a zip file or as a publicly accessible github/gitlub etc project link
- The solution sent to us must be complete, i.e. can be opened directly via Android Studio without additional configuration

## Location Based Notifications
In this challenge the applicant has to implement location-based notifications/reminders, the following conditions are given:

- When creating a new memo, the user provides a location by selecting a point on a map (for instance: google maps or open street maps)
- The memo is then saved
- Once the user physically reaches that location, a notification should be displayed in the phone's status bar, that contains the title and the first 140 characters of the note text
- "Reaching the location" is defined as follows: The user is within 200 meters of the location he initially selected during the memo creation
- The notification should also contain an icon (the icon choice is up to you)
- Some form of location tracking will be required to achieve the desired result, i.e. to know when a user is close to the given location of a memo
- The feature must also work, when the app is running in the background (or possibly not running at all)
