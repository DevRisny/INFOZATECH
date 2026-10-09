# Security

Infoza Hub is a learning project. These are the security choices made in the app.

## What the app does to stay safe
- **HTTPS only.** All network calls (Open-Meteo weather and geocoding) use HTTPS. Plain HTTP is blocked (`usesCleartextTraffic="false"`).
- **No secrets in the code.** There is no API key, password or token in the source. Open-Meteo needs no key.
- **Data stays on the phone.** Tasks, alarms and notes are stored locally in a Room database. Nothing is sent to a server.
- **No cloud backup of private data.** `allowBackup="false"` stops notes and tasks from being copied out through backups.
- **Small permission list.** Only Internet, Notifications, Alarms and Boot-completed are requested. No camera, contacts, location or storage access.
- **Protected components.** The alarm receiver is `exported="false"`, so other apps cannot trigger it.
- **No crash on refusal.** If the system refuses to schedule an alarm, the app logs it and keeps running.

## Ideas for the future
- Turn on R8 / code shrinking for release builds.
- Encrypt the notes database (for example with SQLCipher).
- Add a screen lock option for private notes.

## Reporting a problem
Please open an issue on this GitHub repository or contact the author: Junaideen Risny Suha.
