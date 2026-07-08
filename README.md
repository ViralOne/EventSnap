# EventSnap

Turn a typed description **or** a photo into calendar events with AI, review/edit them, and add
them straight to your device calendar. A free, personal, native Android take on "Photo2Calendar" —
no ads, no paywall.

## How it works

1. **Capture** — type an event description, pick/snap a photo, or share text/an image into the app.
2. **AI extraction** — the text/image is sent to [Groq](https://groq.com) (OpenAI-compatible API);
   a fast text model handles descriptions, a vision model reads photos. It returns structured events.
3. **Review** — every extracted event is an editable card (title, time, location, reminder, target
   calendar). One capture can yield several events.
4. **Confirm** — events are written directly into your Google/device calendar via Android's
   CalendarProvider (`WRITE_CALENDAR`) and saved to a local history.

You need a free Groq API key — paste it in **Settings** (stored in EncryptedSharedPreferences).

## Stack

Kotlin · Jetpack Compose · MVI · Koin · Retrofit/Moshi (Groq) · Room + DataStore ·
Coil 3 · Material 3 (dynamic color) · minSdk 29 / compileSdk 37.

## Module layout

```
app-mobile/                     launcher, Application (Koin), MainActivity, Nav3 host, bottom nav
core/
  common/                       MVI base types (BaseViewModel, ViewState/Action/SideEffect)
  model/                        CalendarEvent, TargetCalendar, CaptureInput (pure Kotlin)
  data/                         Groq client, CalendarProvider writer, encrypted settings,
                                Room history, env/dev-tools (qa)
  designsystem/                 EventsnapTheme, @EventsnapPreviews, tokens, shared components
  ui-mobile/                    HandleEffects + dev-tools UI
  testing/                      MainCoroutineRule + test helpers
feature/<capture|review|settings>/
  data/                         feature repository (interface public, impl internal) + Koin module
  ui-mobile/                    MVI screen: Route, Entry, ViewModel, Screen/ScreenContent, tests
```

Each feature's `data` module is internal to the feature; the app depends only on the `ui-mobile`
modules and reaches the data layer through each feature's `<feature>Modules` Koin aggregator.

## Build & run

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21          # JDK 21
./gradlew installQaDebug                                # build + install the default (qa) flavor
./gradlew test                                          # unit + Robolectric Compose tests
./gradlew spotlessApply detekt lint                     # format + static analysis + lint
```

Open in Android Studio and run `app-mobile` on a device/emulator with a Google account so events
sync to the cloud.

## Build variants

Two product flavors × two build types = four variants. `qa` is the default flavor, so
`./gradlew assembleDebug` resolves to `assembleQaDebug`.

| Variant         | Application ID            | Notes                                  |
| --------------- | ------------------------- | -------------------------------------- |
| `qaDebug`       | `com.eventsnap.android`         | Default. Dev dialog enabled.           |
| `qaRelease`     | `com.eventsnap.android`         | Signed qa for distribution.            |
| `prodDebug`     | `com.eventsnap.android.prod`    | Debuggable prod build for triage.      |
| `prodRelease`   | `com.eventsnap.android.prod`    | Shipping build. Dev dialog disabled.   |

Install commands:

    ./gradlew installQaDebug          # default — shake/broadcast dev dialog enabled
    ./gradlew installProdDebug        # prod-flavored debug build, dev dialog off
    ./gradlew assembleProdRelease     # signed prod release

## Development tools (qa builds only)

QA builds (`IS_QA = true`) include a runtime dialog for switching the API base URL. Prod builds
compile the dialog out — the `DevToolsHost` becomes a zero-cost passthrough.

**On a phone (qa build only):** shake the device.

**From CLI (qa build only):**

    adb shell am broadcast -a com.eventsnap.android.OPEN_DEV_TOOLS -p com.eventsnap.android

The `-p` scopes the broadcast to this app so it doesn't leak to other apps on the device.

## Planning & implementation skills

This project ships two Claude Code skills under `.claude/skills/`:

- **`eventsnap-android-planner`** — plans new features against this architecture.
- **`eventsnap-android-implementer`** — writes the code following the conventions.

## Notes

- Free Groq vision OCR is good but imperfect — the review screen is the safety net; you always
  edit before anything is written to your calendar.
- The Groq model ids live in `core/data/.../groq/GroqModelCatalog.kt`; update them if Groq renames
  its free models.
