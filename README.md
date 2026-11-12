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
- `GOOGLE_SERVICES_JSON` — the Firebase Android `google-services.json` file contents for this app.

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

If the signing key is lost or replaced, Android will reject in-place updates signed by the new key. Configure and enable the Firebase project and Storage before publishing, and deploy the repository's `firestore.rules` and `storage.rules` as appropriate.
