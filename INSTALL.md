# Installationsanleitung – Google Account Login Monitor

## 1. Repository auf GitHub hochladen

```bash
git init
git add .
git commit -m "Initial commit: Google Account Login Monitor"
git branch -M main
git remote add origin https://github.com/DEIN_USERNAME/GoogleAccountLoginMonitor.git
git push -u origin main
```

Sobald der Code auf `main` (oder `master`) liegt, startet der GitHub-Actions-Workflow **automatisch**.

## 2. GitHub Actions Ergebnis

- Gehe zu deinem Repository → Tab **Actions**
- Der Workflow „Android CI – Build APK“ läuft
- Nach erfolgreichem Build findest du unter **Artifacts**:
  - `app-debug`
  - `app-release-unsigned`

## 3. Google OAuth Client-ID (Pflicht)

Die App braucht eine eigene OAuth-Client-ID:

1. https://console.cloud.google.com/ → Projekt anlegen
2. „APIs & Services“ → „Credentials“ → „Create Credentials“ → „OAuth client ID“
3. Application type: **Web application**
4. Die erzeugte Client-ID in `app/build.gradle.kts` eintragen:

```kotlin
buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"123456789-abcdef.apps.googleusercontent.com\"")
```

5. SHA-1 des Debug-Keystores hinzufügen (für lokale Tests):

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

## 4. Lokal bauen (optional)

```bash
./gradlew assembleDebug
```

APK liegt unter: `app/build/outputs/apk/debug/app-debug.apk`

## 5. Installation auf dem Gerät

- Debug-APK einfach per `adb install` oder Dateimanager installieren
- Für Release: APK signieren (Android Studio → Build → Generate Signed Bundle/APK)

## Wichtiger Hinweis zur Funktionalität

Die App kann **keinen** vollständigen Google-Login-Verlauf von Googles Servern abrufen, weil Google dafür keine öffentliche API anbietet.  
Sie protokolliert nur lokale Ereignisse und die Anmeldung in dieser App selbst und verlinkt auf die offizielle Sicherheitsseite.
