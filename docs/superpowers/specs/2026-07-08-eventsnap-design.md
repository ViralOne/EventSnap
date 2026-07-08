# EventSnap — Design Spec

**Date:** 2026-07-08
**Status:** Approved for scaffolding

## Problem

The Android app "Photo2Calendar" turns a photo, screenshot, audio, or typed text
into calendar events using AI, but is limited by ads and in-app-purchase paywalls.
EventSnap reproduces the core loop — **describe or photograph an event → AI extracts
structured event(s) → review & edit → confirm → added to calendar** — as a free,
personal, native Android app with no paywall.

## Why native Android (not a web app)

The hardest part of the web-app approach was getting confirmed events into the
user's calendar: OAuth + Google Cloud setup, or a slow-refresh subscribed `.ics`
feed. A native app writes events **directly into the on-device Google Calendar via
Android's Calendar Provider** (`WRITE_CALENDAR`). Events appear instantly, are
editable, and sync to the cloud automatically — no OAuth, no `.ics`, no server.

## Identity

| Field | Value |
|---|---|
| Project name | `eventsnap` |
| Application ID / root package | `com.eventsnap.android` |
| Class prefix | `Eventsnap` |
| Platforms | Mobile only |
| minSdk | 29 (Android 10) |
| compileSdk / targetSdk | 36 |
| JVM target | 21 |

## Stack

Kotlin + Jetpack Compose + MVI + Koin. Retrofit + Moshi (Groq API client),
Room + DataStore (event history + settings), Coil 3 (image thumbnails),
Timber (logging). Material 3 + dynamic color, follow-system dark mode,
edge-to-edge. No Firebase, no analytics, no auth, no CI (personal repo).

## AI provider

**Groq** (free tier). Text descriptions → fast text model; images → Groq
vision-capable model (Llama 4 / 3.2 Vision). One Retrofit client pointed at
`https://api.groq.com/openai/v1/`. Prompt instructs the model to return
structured JSON (one or more events). API key entered in-app (Settings),
stored in encrypted DataStore.

**Caveat:** free vision OCR is good but not perfect. The review screen is the
safety net — the user always edits before anything hits the calendar.

## Features

### capture
- Text field for a natural-language event description.
- Snap a photo (camera) or pick an existing image (photo picker — no storage
  permission needed on API 29+).
- Handles incoming **share-sheet** intents (`ACTION_SEND` text/image) so the
  user can share from Chrome, WhatsApp, Gmail, etc.
- Sends input to `GroqRepository`, receives structured event(s), navigates to
  review.

### review
- Renders each extracted event as an **editable card**: title, start, end,
  all-day toggle, location, notes, reminder, target calendar.
- One input may yield **multiple** events (e.g. a photographed schedule).
- Confirm → `CalendarRepository` inserts each event (+ reminder) via the
  Calendar Provider. Confirmed events saved to Room history.
- Per-event calendar override.

### settings
- Groq API key field (encrypted DataStore).
- Default target calendar — auto-selects the primary Google calendar on first
  run; changeable here.
- Default reminder offset.
- View event history (from Room).

## Data layer (feature-internal repositories + core)

- `core/model` — `CalendarEvent` domain model (pure Kotlin).
- `GroqRepository` — builds prompt, calls Groq, maps JSON → `List<CalendarEvent>`.
- `CalendarRepository` — reads available calendars, inserts events + reminders
  through `CalendarContract`.
- Room `EventHistory` — persists created events for re-open / re-edit / re-add.

## Permissions

`INTERNET`, `WRITE_CALENDAR`, `READ_CALENDAR`, `CAMERA`. Photo picker requires
no storage permission on API 29+.

## Architecture conventions

Follows `android-project-starter:conventions` exactly: build-logic convention
plugins, `feature/<x>/{data,ui-mobile}` layout, MVI base types in `core/common`,
Screen/ScreenContent split, Koin module aggregation, Material 3 theming tokens,
qa/prod flavors + dev-tools dialog.

## Build & verification

The scaffolding wizard resolves latest stable versions, generates all modules,
then runs the required build-success gate (`./gradlew help`, qaDebug + prodDebug
compile, test-compile smoke, `spotlessCheck detekt lint test`) and a warning
sweep before the initial `setup architecture` commit. Feature business logic
(Groq calls, Calendar Provider writes) is implemented after scaffolding via the
generated `eventsnap-android-planner` / `eventsnap-android-implementer` skills.

## Out of scope (v1)

- Audio input (Photo2Calendar has it; deferred — text + image cover the need).
- Recurring-event rules beyond what the AI extracts into a single RRULE.
- Multi-user / accounts / cloud backend.
