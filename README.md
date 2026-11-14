# Xasuus

A Somali-first Android notes app with Firebase sync, reminders, checklists, attachments, a home-screen widget, and PDF export.

## Download Xasuus

- **Latest APK (stable URL):** [Download Xasuus.apk](https://github.com/ApdowJr/Xasuus/releases/latest/download/Xasuus.apk)
- **All versions and release notes:** [GitHub Releases](https://github.com/ApdowJr/Xasuus/releases)

The latest-download URL stays the same as new versions are published. Each version also has its own permanent asset URL, for example `https://github.com/ApdowJr/Xasuus/releases/download/v1.2.3/Xasuus.apk`.

To install, download the APK on Android and approve installation from your browser or file manager if Android asks. Releases are signed with the project's persistent Android release key so future APKs can update an existing installation without uninstalling it.

## Publish a version

Releases are built and published automatically when a semantic-version tag is pushed:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Use a new, increasing version each time (`v1.0.1`, `v1.1.0`, `v2.0.0`, …). The release workflow sets Android `versionName` from the tag and assigns a monotonically increasing `versionCode`; GitHub generates release notes from the changes since the previous release and uploads `Xasuus.apk` plus its SHA-256 checksum. GitHub Releases and Actions are free for this public repository.

### One-time GitHub Actions configuration

Before the first tag, add these repository **Actions secrets** under **Settings → Secrets and variables → Actions**:

- `ANDROID_KEYSTORE_BASE64` — base64 encoding of the persistent release keystore.
- `ANDROID_STORE_PASSWORD` — that keystore's password.
- `ANDROID_KEY_PASSWORD` — password for the `upload` key.
The Firebase Android configuration is included at `app/google-services.json`; it is client configuration, not a service-account credential. Protect your Firebase data with the deployed Firestore and Storage rules and Firebase Console restrictions. Never commit a service-account key.

Create the release key once and keep its keystore and passwords backed up securely. Do not commit the keystore or passwords. The app Gradle configuration uses the alias `upload`:

```bash
keytool -genkeypair -v \
  -keystore xasuus-release.jks \
  -alias upload \
  -keyalg RSA -keysize 2048 -validity 10000

# Linux:
base64 -w 0 xasuus-release.jks
# macOS:
base64 < xasuus-release.jks | tr -d '\n'
```

If the signing key is lost or replaced, Android will reject in-place updates signed by the new key. The checked-in `.firebaserc` selects the project configured in `app/google-services.json`. In Firebase Console, enable Email/Password sign-in, create/verify the configured Firestore database and Storage bucket, then deploy the repository's security rules with Firebase CLI:

```bash
firebase deploy --only firestore:rules,storage
```

Firebase Console setup and rule deployment require access to the Firebase project; this repository configuration does not itself turn on billing, Authentication providers, Firestore, or Storage.

## Local build and tests

Install JDK 17 and the Android SDK (including the SDK required by `app/build.gradle.kts`). Set `ANDROID_HOME` or `sdk.dir` in `local.properties`. The repository contains the Gradle wrapper; its first run downloads Gradle 9.3.1. The debug signing configuration expects `debug.keystore` in the repository root. Create a local debug-only key if it does not exist:

```bash
keytool -genkeypair -keystore debug.keystore -storepass android -keypass android \
  -alias androiddebugkey -dname 'CN=Android Debug,O=Android,C=US' -keyalg RSA -validity 10000
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest --tests 'com.example.security.AppPinStoreTest' \
  --tests 'com.example.notifications.ReminderNotificationsTest' --tests 'com.example.ExampleRobolectricTest'
```

Never use the debug key for releases. The repository tests under `data/repository` also require running Firestore and Auth emulators; they are not included in the standalone test command above. To run the separate Firestore security-rule tests, install Node.js 20 and Java, then run `npm ci` and `npm run test:rules`. This uses the local emulators and the demo project, not production data. Android CI runs both the standalone Android tests and the Firestore rules tests; device/instrumentation testing and Storage rules tests are not yet automated.

## Backup and recovery

Android cloud backup and device transfer intentionally exclude local app data, including cached notes, authentication state, PIN hashes, and locally scheduled reminders. Sign in on the new device to retrieve notes that were successfully synced to Firebase; **unsynced changes are not recovered**. Set the app PIN again and verify or recreate reminders on the new device. This policy does not replace Firebase backups or protect against deletion in Firebase.

## Firebase deployment checklist

1. In Firebase Console, select the project in `.firebaserc`; enable Email/Password Authentication, and create or verify the named Firestore database in `firebase.json` and the configured Storage bucket.
2. From an authenticated Firebase CLI with permission to deploy to that project, review `firestore.rules` and `storage.rules`, then run `firebase deploy --only firestore:rules,storage`.
3. Test sign-up, sign-in, syncing, attachments, and access isolation with two test accounts. Do not use production data for emulator tests. Check the project's App Check settings before enforcing them.
4. Configure the three release signing secrets described above, then push a new version tag to publish. Never replace an existing production signing key; back it up securely.

Console configuration, rule deployment, and release secrets require project-owner access and are not performed automatically by checking in this repository.
