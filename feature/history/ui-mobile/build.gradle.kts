plugins {
    alias(libs.plugins.eventsnap.android.feature)
}

android {
    namespace = "com.eventsnap.android.feature.history.ui.mobile"
}

dependencies {
    implementation(project(":feature:history:data"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui-mobile"))

    implementation(libs.androidx.compose.material.icons.extended)
}
