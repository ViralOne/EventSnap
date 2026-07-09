package com.eventsnap.android.core.model

/** How the app chooses light vs dark colors. Persisted in settings and applied at the app root. */
enum class ThemePreference {
    /** Follow the system light/dark setting. */
    SYSTEM,

    /** Always light. */
    LIGHT,

    /** Always dark. */
    DARK,
}
