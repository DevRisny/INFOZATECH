# 2-Minute Demo Video Script

**Tip:** record the emulator (or your phone) with the screen recorder and talk while you tap.
Practise once first. Speak slowly and keep it under 2 minutes.

**Before you record:** run the app, allow notifications, add two or three sample tasks and notes,
and make sure the emulator has internet.

---

### 0:00 – 0:12 Intro
> "Hi, I'm Risny Suha. This is Infoza Hub, my Android app for the InfozaTech internship.
> It completes all five tasks in one app: Calculator, To-Do list, Alarm clock, Weather and Notes.
> It is built with Kotlin, Jetpack Compose, Room and Retrofit."

*Show: the bottom navigation bar with five tabs.*

### 0:12 – 0:32 Calculator
> "First, the calculator. Type `2 + 3 × 4` – you can see the live answer 14, because it follows operator
> precedence. It supports decimals, brackets, percent and square root. If I divide by zero, it shows a
> friendly error instead of crashing. The history sheet keeps my old answers."

*Do: type 2+3×4 → =, then 8 ÷ 0 → =, then open History.*

### 0:32 – 0:52 To-Do list
> "Next, tasks. I can add a task with a priority and a due date, tick it as done, edit it, and delete it
> by swiping. There is an Undo button. Everything is saved with Room, so the list is still here after I
> close the app."

*Do: add a task, tick it, swipe one away, tap Undo, switch the filter chips.*

### 0:52 – 1:12 Alarm clock
> "In Alarms, I pick a time with the time picker, choose repeat days and save. Each alarm has an on/off
> switch and shows how long until it rings. It uses AlarmManager, so it rings with a notification at the
> exact time, with Snooze and Dismiss buttons."

*Do: add an alarm for 1–2 minutes from now, toggle the switch, wait for the notification if time allows.*

### 1:12 – 1:32 Weather
> "The Weather tab uses Retrofit and the Open-Meteo API. I search for a city and get temperature,
> condition, humidity and wind speed, plus a five-day forecast. If there is no internet or the city
> is wrong, the app shows a clear error with a retry button."

*Do: search "Kandy", switch °C/°F, search a wrong name like "xyzabc" to show the error.*

### 1:32 – 1:50 Notes
> "Finally, Notes with a Room database. I can create, edit and delete notes, choose a colour, pin a note,
> and search by title."

*Do: create a note, change its colour, pin it, type in the search bar.*

### 1:50 – 2:00 Outro
> "I also wrote 54 unit tests for the calculator and alarm logic. The code is on GitHub in the INFOZATECH
> repository. Thank you, InfozaTech, for this internship!"

*Show: the GitHub repo page or the project in Android Studio with the tests passing.*

---

## If you are asked "how does it work?" – quick notes
- **Calculator:** `CalculatorEngine.kt` is a recursive-descent parser. Each precedence level
  (+ −, × ÷, power, percent, brackets) is its own small function.
- **Room:** `Entities.kt` (tables), `Daos.kt` (queries), `AppDatabase.kt` (database). The screens read
  data as a `Flow`, so the list updates by itself.
- **Alarms:** `AlarmScheduler` sets the alarm → `AlarmReceiver` shows the notification when it rings →
  `BootReceiver` sets the alarms again after a restart.
- **Weather:** `WeatherApi.kt` has the Retrofit interfaces; `WeatherViewModel` holds Loading / Success /
  Error states.
