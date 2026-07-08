plugins {
    alias(libs.plugins.eventsnap.android.library)
    alias(libs.plugins.eventsnap.android.lint)
    alias(libs.plugins.eventsnap.android.room)
    alias(libs.plugins.eventsnap.android.flavors) // last — reads the extension configured above
}

android {
    namespace = "com.eventsnap.android.core.data"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    implementation(libs.koin.android)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    implementation(libs.timber)
}
