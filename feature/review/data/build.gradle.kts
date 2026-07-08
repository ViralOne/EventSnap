plugins {
    alias(libs.plugins.eventsnap.android.library)
    alias(libs.plugins.eventsnap.android.lint)
}

android {
    namespace = "com.eventsnap.android.feature.review.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))

    implementation(libs.koin.core)
    implementation(libs.timber)
}
