# Infoza Hub – Android App Development Internship (InfozaTech)

One Android app that completes **all 5 tasks** of the InfozaTech Android App Development task list
(only 3 were required): **Calculator, To-Do List, Alarm Clock, Weather and Notes.**

Built with **Kotlin + Jetpack Compose**, **Room** (local database), **Retrofit** (networking) and
**AlarmManager** (alarms). Material 3 design with light and dark mode.

Built by **Junaideen Risny Suha** · [GitHub](https://github.com/DevRisny) · [LinkedIn](https://linkedin.com/in/risny-suha-8a6b66423)

---

## Task checklist

### Task 1 – Calculator ✅
| Requirement | Where |
|---|---|
| Basic calculator in Kotlin | `calculator/CalculatorEngine.kt`, `calculator/CalculatorLogic.kt` |
| Number pad and operator buttons in a grid layout | `ui/calculator/CalculatorScreen.kt` (`Keypad`) |
| Display area for current input and result | `ui/calculator/CalculatorScreen.kt` (`Display`) |
| Correct calculations, including decimals | `CalculatorEngine` (parser with operator precedence) |
| Clear button and error handling (divide by zero) | `C` key, `CalcError.DIVIDE_BY_ZERO`, "Cannot divide by zero" |

**Extras:** brackets, `%`, `√`, powers (`^`), `±`, backspace, live answer preview while typing,
history list (tap an old answer to reuse it), no floating-point noise (`0.1 + 0.2 = 0.3`).

### Task 2 – To-Do List ✅
| Requirement | Where |
|---|---|
| Add, edit, mark complete, delete tasks | `ui/todo/TodoScreen.kt` |
| `LazyColumn` list | `TodoScreen` |
| Swipe-to-delete **and** a delete button | `SwipeToDismissBox` + delete icon |
| Saved after closing the app | Room database (`data/`) |

**Extras:** undo after delete, priority (low / medium / high), due dates with a date picker,
overdue highlight, filters (All / Active / Done), progress bar, "clear completed".

### Task 3 – Alarm Clock ✅
| Requirement | Where |
|---|---|
| Set one or more alarms with a time picker | `ui/alarm/AlarmScreen.kt` (Material 3 `TimePicker`) |
| List of alarms with an on/off switch | `AlarmCard` |
| `AlarmManager` triggers a notification and ringtone | `alarm/AlarmScheduler.kt`, `AlarmReceiver.kt`, `AlarmNotifications.kt` |
| Edit and delete alarms | tap a card to edit, delete icon (with undo) |

**Extras:** repeat on chosen weekdays, labels, "Rings in 7h 20m" countdown, Snooze (10 min) and Dismiss
buttons on the notification, alarms are restored after the phone restarts (`BootReceiver`),
notification permission request for Android 13+.

### Task 4 – Weather ✅
| Requirement | Where |
|---|---|
| Live weather for a city from a public API | Open-Meteo API (free, no key) – `weather/WeatherApi.kt` |
| Search bar by city name | `ui/weather/WeatherScreen.kt` |
| Temperature, condition, humidity, wind speed | main card + stat tiles |
| Retrofit, with loading and error states | `WeatherService`, `WeatherUiState` (Loading / Success / Error + retry) |

**Extras:** 5-day forecast, "feels like", pressure, °C/km/h and °F/mph switch, recent searches,
remembers the last city, background colour changes with the weather and time of day.

### Task 5 – Notes with Room ✅
| Requirement | Where |
|---|---|
| Create, view, edit, delete notes | `ui/notes/NotesScreen.kt` |
| List with title and content preview | `NoteCard` in a staggered grid |
| Room database | `data/Entities.kt`, `Daos.kt`, `AppDatabase.kt` |
| Bonus: search by title | search bar (`NotesViewModel.visibleNotes`) |

**Extras:** note colours, pin notes to the top, auto-save when you go back, long-press to delete.

---

## Tech stack
- Kotlin 2.0, Jetpack Compose, Material 3
- MVVM: `ViewModel` + `StateFlow`
- Room 2.6 (KSP), Retrofit 2.11 + Gson, OkHttp, Kotlin Coroutines
- `AlarmManager.setAlarmClock`, `BroadcastReceiver`, notification channels
- Min SDK 26 (Android 8.0), target SDK 34

## Project structure
```
app/src/main/java/com/infozatech/allinone/
├── MainActivity.kt          bottom navigation with 5 tabs
├── calculator/              expression parser + key-handling logic (no Android code, fully unit tested)
├── data/                    Room entities, DAOs, database
├── alarm/                   AlarmTime, AlarmScheduler, AlarmReceiver, BootReceiver, notifications
├── weather/                 Retrofit API, JSON models, weather code helpers
├── viewmodel/               one ViewModel per feature
└── ui/                      theme, shared components and the 5 screens
app/src/test/                unit tests (calculator + alarm time)
```

## How to run
1. Install **Android Studio** (Koala or newer) with **JDK 17**.
2. **File ▸ Open** and choose this folder. Wait for the Gradle sync to finish.
3. Pick an emulator or a phone (Android 8.0+) and press **Run ▶**.
4. The Weather tab needs internet. On Android 13+ allow notifications so alarms can ring.

Run the unit tests: `./gradlew test` (or right-click `app/src/test` ▸ *Run Tests*).

## Testing
54 unit tests cover the calculator engine (precedence, decimals, brackets, percent, powers, square root,
divide by zero, bad input), the key-press logic (live preview, history, continuing after `=`, negate,
implicit multiplication) and the alarm time maths (next ring time, repeat days, countdown text).

## Notes
- Weather data: [Open-Meteo](https://open-meteo.com/) (free for non-commercial use, attribution required).
- If alarms do not ring on some phones, turn off battery optimisation for the app
  (some brands stop background apps aggressively).
- The code in this repository was written for this internship project and is not copied from other repositories.
