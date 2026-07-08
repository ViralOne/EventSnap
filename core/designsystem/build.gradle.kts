plugins {
    alias(libs.plugins.eventsnap.android.library)
    alias(libs.plugins.eventsnap.android.library.compose)
    alias(libs.plugins.eventsnap.android.lint)
}

android {
    namespace = "com.eventsnap.android.core.designsystem"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.extended)
}
