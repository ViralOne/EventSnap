plugins {
    alias(libs.plugins.eventsnap.android.feature)
}

android {
    namespace = "com.eventsnap.android.feature.capture.ui.mobile"
}

dependencies {
    implementation(project(":feature:capture:data"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui-mobile"))

    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
}
