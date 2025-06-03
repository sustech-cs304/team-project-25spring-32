
# Smart Album - Developer Guide

## Project Overview

Smart Album is an Android application for intelligent photo management. It supports smart album creation, advanced search, photo editing, memory video generation, and social sharing. The project is modular and easy to extend or maintain.

## Main Features

- **Photo Management**: Automatically or manually create albums by time, location, or tags. Move or copy photos between albums.
- **Smart Search**: Search photos by tags, date, location, or image content. Includes keyword suggestion and auto-completion.
- **Photo Editing**: Basic editing (rotate, crop, mirror), AI-powered enhancements (background removal, HD restoration, etc.).
- **Memory Videos**: Automatically generate themed videos with custom music and transitions.
- **Social Sharing**: Share albums/photos privately, publicly, or collaboratively. Built-in social features.
- **User Authentication**: Registration, login, and session management.

## Environment Requirements

- **OS**: Android 8.0+ (API 26+)
- **IDE**: Android Studio (recommended)
- **JDK**: 11 or higher

## Dependencies & Build

### Main Dependencies

- AndroidX, Material, Room, Glide, Retrofit, OkHttp, RxJava, FFmpeg-kit, TensorFlow Lite, etc.
- See `app/build.gradle.kts` for full details.

### Build Steps

1. Open the project root in Android Studio.
2. Sync Gradle dependencies.
3. Connect a device or start an emulator, then click "Run" to build and install.
4. APK output: `app/build/outputs/apk/debug/`

## Project Structure

```
app/
  ├── src/
  │   └── main/
  │       ├── java/com/example/pa/
  │       │   ├── auth/      // Authentication
  │       │   ├── data/      // Data layer (Repository, Model, Network, DB)
  │       │   ├── ui/        // UI layer (photo, album, search, memory, social, etc.)
  │       │   ├── util/      // Utilities
  │       │   └── MyApplication.java // App entry
  │       ├── res/           // Resources
  │       └── AndroidManifest.xml
  └── build.gradle.kts       // Build script
```

## Core Classes & Modules

- `MainActivity.java`: Main UI and navigation
- `auth/`: `LoginActivity`, `RegisterActivity` for user authentication
- `data/`: `FileRepository`, `MainRepository`, `DatabaseHelper` for data management
- `ui/photo/`: Photo browsing, editing, details
- `ui/album/`: Album management and display
- `ui/search/`: Smart search
- `ui/memory/`: Memory video generation and management
- `ui/social/`: Social and sharing features
- `util/`: General utilities (path conversion, password encryption, AI tools, etc.)

## Permissions

Ensure the following permissions are declared in `AndroidManifest.xml`:

- Storage read/write (for Android 13+, use READ_MEDIA_* permissions)
- Internet access
- See `app/src/main/AndroidManifest.xml` for details

## Testing & Code Quality

- Unit and UI tests: `src/test/java`, `src/androidTest/java`
- Integrated with Jacoco (code coverage) and SonarQube (code quality)
- Run tests via Android Studio or Gradle commands

## FAQ

- **Dependency Conflicts**: For Mockito or other conflicts, see comments in `build.gradle.kts`.
- **Permission Issues**: For Android 13+, request media permissions at runtime.
- **AI/Video Features**: Some features require device hardware support (TensorFlow Lite/FFmpeg).

## Contribution & Feedback

Feel free to submit issues or pull requests to help improve the project!

---

For more detailed guides, API documentation, or module extension instructions, see `design-32.md` or contact the project maintainers.


