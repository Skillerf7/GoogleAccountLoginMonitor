# Google Account Login Monitor

Autorisierte Android-App zur lokalen Protokollierung von Sicherheitsereignissen eines **eigenen** Google-Kontos.

## Wichtige Einschränkung (bitte lesen)

**Google stellt keine öffentliche API bereit**, die Drittanbieter-Apps einen vollständigen automatisierten Login-Verlauf (mit IP-Adresse, Geräteliste, Standort pro Anmeldung) liefert.

Diese App:
- Authentifiziert sich über das **offizielle Google Sign-In** (nur Basis-Scopes: E-Mail + Profil)
- Speichert lokal, wann der Benutzer sich in **dieser App** angemeldet hat
- Ermöglicht manuelles Hinzufügen von Ereignissen
- Verlinkt direkt auf die offizielle Google-Kontosicherheitsseite
- Erfindet **keine** Login-Daten

Den echten offiziellen Verlauf finden Sie immer unter:  
https://myaccount.google.com/security

## Funktionen

- Google Sign-In (offiziell)
- Chronologische Ereignisliste (lokal)
- Manuelle Ereignisse hinzufügen (Gerät, IP, Standort, „neues Gerät“)
- Suche / Filter
- Benachrichtigungen bei neuen lokalen Ereignissen
- Direkter Link zur Google-Kontosicherheit
- Klare Kennzeichnung nicht verfügbarer Informationen

## Voraussetzungen zum Bauen

- Android Studio Hedgehog (2023.1.1) oder neuer
- JDK 17
- Ein Google Cloud Projekt mit OAuth 2.0 Client-ID (Web-Client-ID)

### Google OAuth Client-ID einrichten

1. Gehen Sie zu [Google Cloud Console](https://console.cloud.google.com/)
2. Erstellen Sie ein Projekt
3. Aktivieren Sie die „Google Sign-In“ / Identity API
4. Erstellen Sie eine OAuth-Client-ID vom Typ **Web application**
5. Tragen Sie die Client-ID in `app/build.gradle.kts` ein:

```kotlin
buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"IHRE_CLIENT_ID.apps.googleusercontent.com\"")
```

6. Fügen Sie das SHA-1-Zertifikat Ihres Debug-/Release-Keystores hinzu.

## Bauen der App

```bash
./gradlew assembleDebug
# oder
./gradlew assembleRelease
```

Die APK liegt danach unter:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release-unsigned.apk`

## GitHub Actions

Beim Hochladen des Repositories auf GitHub startet der Workflow `.github/workflows/android-build.yml` **automatisch** bei jedem Push auf `main`/`master` sowie bei Pull Requests.

Der Workflow:
- Baut Debug- und Release-APK
- Führt Unit-Tests aus
- Lädt die APKs als Artifacts hoch (14 Tage verfügbar)

## Sicherheit

- Keine Passwörter im Code oder in der Datenbank
- Nur minimale OAuth-Scopes
- Lokale Room-Datenbank (nicht in Cloud-Backup enthalten)
- Nur für das eigene Google-Konto gedacht

## Lizenz

Dieses Projekt ist als Beispiel / Vorlage gedacht. Nutzung auf eigene Verantwortung.
