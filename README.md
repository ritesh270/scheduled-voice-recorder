# Auto Voice Recorder - Android (sample project)

This is a sample Android Studio project that implements:
- A foreground recording service using MediaRecorder.
- AlarmManager-based daily scheduling to auto-start recording.
- Directory picker using Storage Access Framework (ACTION_OPEN_DOCUMENT_TREE).
- Daily file rotation (new file created at midnight).
- Local storage of recordings (user-chosen folder or app-specific storage).

IMPORTANT:
- You **must** request runtime permission for RECORD_AUDIO on devices Android 6.0+ before starting recording. The sample code does not show runtime permission dialogs; add them before using.
- Background audio recording and long-running foreground services may be restricted by some OEMs and Android versions. Guide users to whitelist the app in battery optimizations if necessary.
- **Legal/ethical notice:** Recording people without their consent may be illegal in your jurisdiction. Ensure you obtain explicit consent of recorded parties and follow local laws.

How to open:
1. Download the zip and open in Android Studio (recommended Arctic Fox or later).
2. Update Gradle plugin versions and Kotlin versions as needed.
3. Add runtime permission handling for RECORD_AUDIO and handle Android 10+ scoped storage nuances if you want full filesystem paths.

This project is provided as-is and is meant as a starting point. Customize it to handle edge cases, permission flows, and UI polish before distributing.
