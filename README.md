# Xasuus

A Somali-first Android notes app with Firebase sync, reminders, checklists, attachments, a home-screen widget, and PDF export.

## Download Xasuus

- **Latest APK (stable URL):** [Download Xasuus.apk](https://github.com/ApdowJr/Xasuus/releases/latest/download/Xasuus.apk)
- **All versions and release notes:** [GitHub Releases](https://github.com/ApdowJr/Xasuus/releases)

The latest-download URL stays the same as new versions are published. Each version also has its own permanent asset URL, for example `https://github.com/ApdowJr/Xasuus/releases/download/v1.2.3/Xasuus.apk`.

> No release has been published yet, so the latest-download link currently returns 404. Follow **Publish a version** below once the signing secrets are configured.

To install, download the APK on Android and approve installation from your browser or file manager if Android asks. Releases are signed with the project's persistent Android release key so future APKs can update an existing installation without uninstalling it.

## Publish a version

Releases are built and published automatically when a semantic-version tag is pushed:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Use a new, increasing version each time (`v1.0.1`, `v1.1.0`, `v2.0.0`, …). The release workflow sets Android `versionName` from the tag and assigns a monotonically increasing `versionCode`; GitHub generates release notes from the changes since the previous release and uploads `Xasuus.apk` plus its SHA-256 checksum. GitHub Releases and Actions are free for this public repository.

### One-time GitHub Actions configuration

A persistent release key already exists and is registered in Firebase (see the checklist below), so the only remaining step is to upload it to GitHub. Add these repository **Actions secrets** under **Settings → Secrets and variables → Actions**:

- `ANDROID_KEYSTORE_BASE64` — base64 encoding of `xasuus-release.jks` (alias `upload`).
- `ANDROID_STORE_PASSWORD` — that keystore's password.
- `ANDROID_KEY_PASSWORD` — password for the `upload` key (the same value as the store password).

Generate the base64 value without line breaks:

```bash
# Linux:
base64 -w 0 xasuus-release.jks
# macOS:
base64 < xasuus-release.jks | tr -d '\n'
```

The Firebase Android configuration is included at `app/google-services.json`; it is client configuration, not a service-account credential. Protect your Firebase data with the deployed Firestore and Storage rules and Firebase Console restrictions. Never commit a service-account key.

If the keystore ever has to be recreated, keep the new keystore and passwords backed up securely and never commit them. The app Gradle configuration uses the alias `upload`, and the fingerprints registered in Firebase must be replaced together with the key. Changing the signing key rejects in-place updates from the old key, so users would have to uninstall first.

The checked-in `.firebaserc` selects the project configured in `app/google-services.json`. In Firebase Console, enable Email/Password sign-in, create/verify the configured Firestore database and Storage bucket, then deploy the repository's security rules with Firebase CLI:

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

Never use the debug key for releases. The repository tests under `data/repository` also require running Firestore and Auth emulators; they are not included in the standalone test command above. To run the separate Firestore security-rule tests, install Node.js 20 and Java, then run `npm ci` and `npm run test:rules`. This uses the local emulators and the demo project, not production data. Android CI runs the standalone Android tests, a signed release-variant build with a throwaway key, and the Firestore rules tests; device/instrumentation testing and Storage rules tests are not yet automated.

`app/google-services.json` is committed because it is client configuration, and the build now fails on purpose when it is missing: an APK without Firebase configuration installs but cannot sync, which is easy to miss until runtime.

## Backup and recovery

Android cloud backup and device transfer intentionally exclude local app data, including cached notes, authentication state, PIN hashes, and locally scheduled reminders. Sign in on the new device to retrieve notes that were successfully synced to Firebase; **unsynced changes are not recovered**. Set the app PIN again and verify or recreate reminders on the new device. This policy does not replace Firebase backups or protect against deletion in Firebase.

## Firebase deployment checklist

The checked-in configuration targets project `gen-lang-client-0072788294` with Android app `com.aistudio.mynotes.xasuus`.

1. **Firestore.** This app uses a named database, not `(default)`: `ai-studio-android-mynotes-4e5f18b1-7fbc-4699-9908-9e8ab69dbbe1`. `firebase.json` deploys `firestore.rules` to that database, and `app/src/main/res/values/firebase_applet_config.xml` tells the Android SDK to read it through `R.string.firestore_database_id`. Both files and the database must stay in sync; otherwise writes fail with `PERMISSION_DENIED` or reads fail with "database not found". Deploy with:

   ```bash
   firebase deploy --only firestore:rules
   ```

2. **Authentication.** Email/Password and Google sign-in are both enabled in Firebase Authentication (`defaultSupportedIdpConfigs/google.com` has `enabled: true`), so no console change is needed. Google Sign-In additionally needs the signing certificate registered (step 5).

3. **Storage.** Cloud Storage is **not initialized** in this project and the project has **no billing account**. Cloud Storage for Firebase now requires the Blaze (pay-as-you-go) plan even within its free usage tier, so bucket creation is rejected with "The billing account for the owning project is disabled in state absent". Until billing is enabled and **Build → Storage → Get Started** is completed in the Firebase Console, attachment uploads fail and the rest of the app keeps working. After the bucket exists, deploy the rules:

   ```bash
   firebase deploy --only storage
   ```

4. **App Check.** The app does not initialize App Check, so do not turn on enforcement for Firestore or Storage; the release APK would be blocked. Add a Play Integrity provider before enforcing.

5. **Release signing certificate.** The persistent release key (`xasuus-release.jks`, alias `upload`, RSA 4096) is registered in Firebase under **Project settings → Your apps → com.aistudio.mynotes.xasuus → Additional fingerprints**:
   - SHA-1 `B0:B5:EE:B0:2A:59:F1:A8:7B:2E:1D:49:54:91:8F:09:46:D9:B7:FC`
   - SHA-256 `BC:3F:D9:7A:35:77:E9:A0:D0:75:F9:A6:8C:AC:F0:54:5F:A7:98:E9:44:4C:53:DC:65:A3:CE:9E:0F:42:C6:4F`

   The debug SHA-1 (`417c25b6…`) is also still registered for local development. The release workflow prints the keystore's SHA-1/SHA-256 (`apksigner verify --print-certs`) and fails if the APK is debug-signed. Without a matching fingerprint, Google Sign-In fails in the release APK with `DEVELOPER_ERROR` code 10, while email/password sign-in keeps working. `app/google-services.json` lists no certificate hashes (`certificate_hash` is empty), so it does not need regenerating after adding a fingerprint; the SDK checks the installed APK at runtime.

6. **Test with two accounts.** Verify sign-up, sign-in, sync, attachments, trash/restore, reminders, and user isolation. Use emulators/demo data, never production notes.

7. **Publish.** Configure the three release signing secrets described above, make sure the Android checks workflow is green on `main`, then push a semantic-version tag. The release workflow runs the unit tests, builds the signed APK, verifies its signature and `versionName`, writes the SHA-256 file, and publishes the GitHub Release.

Console configuration, rule deployment, and release secrets require project-owner access and are not performed automatically by checking in this repository.
