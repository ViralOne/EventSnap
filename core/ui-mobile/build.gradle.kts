plugins {
    alias(libs.plugins.eventsnap.android.library)
    alias(libs.plugins.eventsnap.android.library.compose)
    alias(libs.plugins.eventsnap.android.lint)
}

android {
    namespace = "com.eventsnap.android.core.ui.mobile"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.core.ktx)
}
