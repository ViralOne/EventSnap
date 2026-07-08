plugins {
    alias(libs.plugins.eventsnap.android.application)
    alias(libs.plugins.eventsnap.android.application.compose)
    alias(libs.plugins.eventsnap.android.lint)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.eventsnap.android.flavors) // last — reads the extension configured above
}

android {
    namespace = "com.eventsnap.android"

    defaultConfig {
        applicationId = "com.eventsnap.android"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui-mobile"))

    implementation(project(":feature:capture:ui-mobile"))
    implementation(project(":feature:review:ui-mobile"))
    implementation(project(":feature:settings:ui-mobile"))
    implementation(project(":feature:history:ui-mobile"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.timber)
}
