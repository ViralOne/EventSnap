---
name: eventsnap-android-implementer
description: Implements features for the EventSnap Android app following its architecture and conventions. Use when writing code for a planned EventSnap feature.
---

# EventSnap — Android Implementer

Write code for EventSnap following the established patterns. Read
`references/project-architecture.md` first (module inventory, MVI contract, nav, Koin, data flow).

## Workflow

1. TDD for logic (ViewModels, repositories, mappers): write the test first.
   Test-after for Compose (need testTags + text to assert).
2. Match the existing feature shape exactly — copy `feature/capture` or `feature/review` as the
   template. Every ui module has: `Route`, `Entry`, `mvi/<Feature>Contract`, `<Feature>ViewModel`,
   `components/<Feature>Screen` + `<Feature>ScreenContent`, `Module`, `<Feature>Modules`, tests.
3. Build after each module: `./gradlew :feature:<x>:ui-mobile:compileDebugKotlin
   :feature:<x>:ui-mobile:compileDebugUnitTestKotlin`.
4. Before done: `./gradlew spotlessApply detekt lint test` and compile both flavors.

## Non-obvious rules learned building this project

- **compileSdk is 37** (latest AndroidX core/lifecycle require it). Keep it there.
- **Koin repository binding**: use explicit `single<Interface> { Impl(get(), …) }` — the
  `singleOf(::Impl){ bind<>() }` form failed to resolve against Koin 4.1 here.
- **MVI base package is `com.eventsnap.android.core`** (module `:core:common`) — import
  `ViewState`/`ViewAction`/`ViewSideEffect` from there, NOT `…core.common`.
- **A feature `data` module that uses Moshi/Retrofit directly must declare that dep itself** —
  `core/data` exposes them as `implementation`, so they're not transitive.
- **Library modules get `missingDimensionStrategy("env", "qa")`** (in the library convention
  plugin) so they resolve against the flavored `:core:data` when built in isolation. Don't remove it.
- **`entry<Route>`** is a member of `EntryProviderScope` — never `import …runtime.entry`.
- **ScreenContent** stays `(state, onAction, modifier)` pure UI. A system launcher (image picker)
  is passed as a separate callback from Screen (see `CaptureScreen`), the one allowed exception.

## Compose authoring rules

Follow the Android conventions Compose rules: state ownership (VM `StateFlow` vs `remember`),
stability (`@Immutable`, `ImmutableList`), modifier order = visual order, correct side-effect API
(`LaunchedEffect`/`DisposableEffect`/`rememberUpdatedState`), lazy-list `key`/`contentType`,
M3 motion tokens, semantics for accessibility. Theme only via `MaterialTheme.*` and `Spacing.*`.
Use `@EventsnapPreviews` alone; add a preview per meaningful state (content/loading/error).

## Tests

- ViewModel: `MainCoroutineRule` + Turbine (`vm.effects.test { … }`) + Truth. Mock repos with
  mockito-kotlin.
- ScreenContent: Robolectric `@RunWith(AndroidJUnit4::class)` + `createComposeRule()`, target
  ScreenContent (never Screen — it needs Koin). `src/test/resources/robolectric.properties` = `sdk=34`.
- Assert action dispatch from clicks and state-driven rendering; don't test static text.
