---
name: eventsnap-android-planner
description: Plans new features for the EventSnap Android app against its established architecture. Use when adding or changing a feature before writing code.
---

# EventSnap — Android Planner

Plan features for EventSnap (AI text/photo → calendar events). Read
`references/project-architecture.md` at the start of every run — it is the module inventory,
MVI contract, and navigation map.

## Workflow

1. Restate the feature in one paragraph: user story + which modules it touches.
2. Decide the module surface:
   - New screen → new `feature/<name>/{data,ui-mobile}` following the existing three.
   - Extending capture/review/settings → change that feature's MVI trio + repository only.
   - Shared infra (calendar, Groq, storage) → change `core/data`, never a feature.
3. Define the MVI contract first: `State` (immutable data class), `Action` (sealed), `Effect`
   (sealed) — see any existing `mvi/<Feature>Contract.kt`.
4. Define the data boundary: a public `<Feature>Repository` interface + `internal` impl +
   `<feature>DataModule` Koin module. Nothing else public.
5. List files to add/change with one line each. Note tests: a ViewModel test (Turbine on effects,
   Truth on state) and a ScreenContent Robolectric test per screen.
6. Call out permissions (calendar/camera), Groq model choice (text vs vision), and any new
   `core/model` types.

## Hard rules (enforced by the build)

- App module depends only on `feature/<x>:ui-mobile`, never `:data`.
- Repository impls are `internal`; only the interface + Koin module are public.
- MVI base types live in package `com.eventsnap.android.core` (module `:core:common`).
- ScreenContent is pure UI `(state, onAction, modifier)` — no Koin/Context. Screen does the wiring.
- Use `@EventsnapPreviews` alone (never stack `@Preview`). Theme via `MaterialTheme.*` + `Spacing`.
- Koin: register repositories with explicit `single<Interface> { Impl(get(), …) }`; ViewModels
  with `viewModelOf(::Vm)`.

## Quality gates before "done"

```
./gradlew spotlessApply detekt lint test
./gradlew :app-mobile:compileQaDebugKotlin :app-mobile:compileProdDebugKotlin
```

Hand the plan to `eventsnap-android-implementer`.
