# APK Signing Configuration

This guide explains how to set up APK signing for local builds and GitHub Actions releases.

## Overview

The app is configured to automatically sign release APKs when the required signing credentials are provided via environment variables.

## Local Development

### 1. Generate a Keystore

If you don't have a keystore yet, create one:

```bash
keytool -genkey -v -keystore app/release.keystore -alias lister -keyalg RSA -keysize 2048 -validity 10000
```

You will be prompted to enter:
- **Keystore password**: Choose a secure password
- **Key password**: Choose a secure password (can be the same as keystore password)
- **Your name, organization, etc.**: Fill in your details

**Important**: Keep your keystore file and passwords safe! Store them securely. If you lose them, you won't be able to update your app.

### 2. Set Environment Variables

Before building a release APK locally, set these environment variables:

```bash
export KEYSTORE_FILE="app/release.keystore"
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="lister"
export KEY_PASSWORD="your_key_password"
```

Or create a shell script (e.g., `signing.sh`) that you can source:

```bash
#!/bin/bash
export KEYSTORE_FILE="app/release.keystore"
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="lister"
export KEY_PASSWORD="your_key_password"
```

Then use it:
```bash
source signing.sh
./gradlew assembleRelease
```

**Note**: Never commit `signing.sh` to git! Add it to `.gitignore` if you create it.

### 3. Build Signed APK

```bash
./gradlew assembleRelease
```

The signed APK will be at: `app/build/outputs/apk/release/app-release.apk`

## GitHub Actions Setup

To enable signed releases via GitHub Actions, you need to configure GitHub Secrets.

### 1. Encode Your Keystore to Base64

```bash
base64 app/release.keystore | tr -d '\n' > keystore.base64
```

Copy the contents of `keystore.base64`.

### 2. Add GitHub Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions → New repository secret

Add these four secrets:

1. **`KEYSTORE_BASE64`**
   - Value: The base64-encoded keystore (from `keystore.base64`)

2. **`KEYSTORE_PASSWORD`**
   - Value: Your keystore password

3. **`KEY_ALIAS`**
   - Value: Your key alias (e.g., `lister`)

4. **`KEY_PASSWORD`**
   - Value: Your key password

### 3. Create a Release

Once secrets are configured, create a new release:

```bash
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions will automatically:
- Build a signed release APK
- Build a debug APK
- Attach both APKs to the GitHub Release

## Verification

To verify that an APK is signed:

```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

You should see: **"jar verified."**

To see the signing certificate details:

```bash
keytool -printcert -jarfile app/build/outputs/apk/release/app-release.apk
```

## Security Best Practices

1. **Never commit your keystore or passwords to git**
2. **Keep backups of your keystore in a secure location** (password manager, encrypted storage)
3. **Use different passwords for keystore and key** (optional but recommended)
4. **Rotate GitHub secrets if they are ever compromised**
5. **Limit access to repository settings and secrets**

## Troubleshooting

### Build fails with "keystore not found"

Make sure the `KEYSTORE_FILE` environment variable points to the correct location.

### Build fails with "incorrect password"

Double-check your `KEYSTORE_PASSWORD` and `KEY_PASSWORD` environment variables.

### GitHub Actions fails to build

Check that all four secrets are correctly set in GitHub:
- KEYSTORE_BASE64
- KEYSTORE_PASSWORD
- KEY_ALIAS
- KEY_PASSWORD

View the workflow logs for detailed error messages.

## Without Signing

If you build without signing credentials, the build will succeed but produce an unsigned APK at:
`app/build/outputs/apk/release/app-release-unsigned.apk`

Unsigned APKs cannot be installed on most devices for security reasons.

