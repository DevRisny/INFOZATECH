# Project Report – Infoza Hub

**Internship:** Android App Development, InfozaTech (10 Sep – 10 Oct 2026)
**Intern:** Junaideen Risny Suha
**Project:** Infoza Hub – one app with five tools (Calculator, To-Do, Alarm Clock, Weather, Notes)

## 1. Introduction
The internship asks interns to finish at least 3 of 5 Android tasks. This project completes **all five**
inside one app, so the tasks share one design, one navigation bar and one code base.

## 2. Objectives
- Build every task with Kotlin and modern Android tools.
- Keep data after the app is closed (Room database).
- Use real Android system features: AlarmManager, notifications, networking.
- Handle mistakes politely (errors, empty lists, no internet).
- Test the important logic with unit tests.

## 3. Tools and technology
| Area | Choice | Why |
|---|---|---|
| Language | Kotlin | Official Android language |
| UI | Jetpack Compose + Material 3 | Less code, easy light/dark mode |
| Architecture | MVVM (ViewModel + StateFlow) | UI stays simple, state survives rotation |
| Database | Room | Tasks, alarms and notes are saved locally |
| Networking | Retrofit + Gson | Clean API calls and JSON parsing |
| Alarms | AlarmManager + BroadcastReceiver | Rings on time, even when the app is closed |
| Weather data | Open-Meteo API | Free and needs no API key |

## 4. What was built

### Task 1 – Calculator
The calculator uses its own expression parser (recursive descent), so `2 + 3 × 4` correctly gives `14`.
It supports decimals, brackets, percent, square root and powers. Dividing by zero shows
"Cannot divide by zero" instead of crashing. A live preview shows the answer while typing, and a history
sheet keeps past results. Numbers are rounded for display so `0.1 + 0.2` shows `0.3`.

### Task 2 – To-Do list
Tasks can be added, edited, completed and deleted. The list is a `LazyColumn`. A task can be deleted by
swiping or with the delete button, and a snackbar offers **Undo**. Tasks are saved in Room. Extra features:
priority, due date, filters and a progress bar.

### Task 3 – Alarm clock
A Material 3 time picker sets the alarm. Each alarm has an on/off switch, an optional label and repeat days.
`AlarmManager.setAlarmClock` schedules the alarm, and a `BroadcastReceiver` shows a notification with the
alarm sound, **Snooze** and **Dismiss**. A boot receiver schedules the alarms again after a restart.

### Task 4 – Weather
The user searches for a city. The app finds its position (geocoding API), then loads the forecast with
Retrofit. It shows temperature, condition, humidity and wind speed, plus feels-like, pressure and a 5-day
forecast. There are clear Loading and Error screens (no internet, city not found, server error) with a
retry button.

### Task 5 – Notes
Notes are stored with Room and shown as cards with a title and a preview of the content. Users can create,
edit and delete notes, search by title, choose a colour and pin important notes.

## 5. Design decisions
- **One `ViewModel` per feature** keeps the code easy to read and test.
- **Pure Kotlin logic** (`CalculatorEngine`, `CalculatorLogic`, `AlarmTime`) has no Android code, so it can
  be unit tested quickly.
- **`setAlarmClock`** was chosen because it is exact, works in Doze mode and needs no special permission.
- **Open-Meteo** was chosen because it needs no API key, so the project runs right after cloning.

## 6. Challenges and solutions
| Challenge | Solution |
|---|---|
| Floating-point errors (`0.1 + 0.2`) | Round to 12 significant digits and strip trailing zeros when displaying |
| Operator precedence | Recursive-descent parser with one function per precedence level |
| Alarm must survive a restart | `BootReceiver` re-schedules all enabled alarms |
| Notifications on Android 13+ | Runtime permission request with a helpful banner if denied |
| Undo after delete | Re-insert the same object (same id) into Room |

## 7. Testing
54 unit tests (JUnit) check the calculator engine, the calculator key logic and the alarm time maths.
They include edge cases such as `10 ÷ 0`, `0 ÷ 0`, `1 ÷ (2 − 2)`, `√(−4)`, huge results, repeated decimal
points and alarms set for a time that has already passed today. Manual testing steps for the screens are
listed in the video script.

## 8. What was learned
- Building a multi-screen app with Jetpack Compose and Material 3.
- Using Room, Retrofit, coroutines and Flow together.
- Scheduling alarms and showing notifications with the Android system services.
- Writing testable code by keeping logic separate from the UI.

## 9. Future improvements
- Home-screen widget for tasks and weather.
- Cloud backup of notes and tasks.
- Weather by GPS location.
- Custom alarm ringtone and gradual volume.

## 10. Conclusion
All five tasks of the InfozaTech Android track are complete in a single, tidy app with extra features,
local storage, networking, background alarms and unit tests.

*Source code: GitHub repository **INFOZATECH** (see the link in the submission form).*
