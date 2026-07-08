# EventSnap — Project Architecture

## Identity
- Package root: `com.eventsnap.android` · class prefix `Eventsnap` · plugin prefix `eventsnap`
- minSdk 29 · compileSdk 37 · targetSdk 36 · JVM 21 · mobile only
- Flavors: `qa` (default) + `prod`

## Modules
- `app-mobile` — Application (Koin `startKoin`), MainActivity (share-intent + DevToolsHost),
  `navigation/EventsnapNavHost` (Nav3 `NavDisplay` + bottom nav), permissions request.
- `core/common` — MVI base (`BaseViewModel`, `ViewState`, `ViewAction`, `ViewSideEffect`),
  package `com.eventsnap.android.core`.
- `core/model` — pure Kotlin: `CalendarEvent`, `TargetCalendar`, `CaptureInput` (Text | Image).
- `core/data` — `GroqApi` + models + `EventPromptBuilder` + `GroqModelCatalog`;
  `CalendarWriter`(Impl) over CalendarProvider; `SettingsStore`(EncryptedSettingsStore);
  Room `EventSnapDatabase`/`EventHistoryDao`; `ExtractedEventsHolder` (capture→review handoff);
  env/dev-tools; Koin `coreDataModule` + `environmentModule`. Applies flavors + Room.
- `core/designsystem` — `EventsnapTheme`, `@EventsnapPreviews`, `EventsnapColors`, `Spacing`,
  `EventsnapTypography`, `EventsnapLoading`, `EventsnapErrorState`.
- `core/ui-mobile` — `HandleEffects`, dev-tools UI (`DevToolsHost`, `ShakeListener`,
  `EnvSelectorDialog`, `DevToolsBroadcastListener`).
- `core/testing` — `MainCoroutineRule` (UnconfinedTestDispatcher default).
- `feature/capture` — text/image input → Groq extraction → `ExtractedEventsHolder` → navigate review.
- `feature/review` — editable event cards + calendar picker → `CalendarWriter.insertEvent` + history.
- `feature/settings` — Groq key (encrypted), default calendar, default reminder.

## MVI contract
`BaseViewModel<State, Action, Effect>(initialState)`: hot `StateFlow` state; `Channel` effects
(`effects: Flow<Effect>`); buffered actions via non-suspending `setAction`. Override
`suspend fun onAction`. `setState { copy(...) }`, `suspend setEffect(...)`. The `init` action
collector requires `UnconfinedTestDispatcher` in tests (that's the `MainCoroutineRule` default).

## Navigation (Nav3)
Back stack = `mutableStateListOf<NavKey>()` in `EventsnapNavHost`. Routes are `@Serializable data object`
implementing `NavKey`. Each feature exposes `fun EntryProviderScope<NavKey>.<feature>Entry(...)`
using the member `entry<Route> { … }` (do NOT import `entry`). Cross-feature nav is via callbacks
passed into the entry, never a NavController. Bottom-nav tabs: Capture, Settings.

## Koin
`EventsnapApplication.startKoin { modules(listOf(environmentModule, coreDataModule) + captureModules
+ reviewModules + settingsModules) }`. Each ui module exposes `<feature>Modules =
listOf(<feature>Module, <feature>DataModule)`.

## Data flow
CaptureInput → `CaptureRepository.extractEvents` (Groq, DTO→`CalendarEvent`) →
`ExtractedEventsHolder.set` → navigate → `ReviewViewModel` reads `pendingEvents()` →
edit → `ReviewRepository.confirm(calendarId, events)` → `CalendarWriter` + Room history.

## Theming
Material 3 + dynamic color, follow-system dark. No hardcoded colors/dimens — use
`MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `Spacing.*`.

## Groq
OpenAI-compatible `chat/completions`. Key from `SettingsStore` as `Bearer`. Models in
`GroqModelCatalog` (TEXT_MODEL, VISION_MODEL). Prompt from `EventPromptBuilder` requests a strict
JSON `{ "events": [...] }` envelope; response parsed with Moshi into `GroqEventEnvelope`.
