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
        versionCode = 9
        versionName = "0.5.3"
    }

    // A stable release keystore, provided via env vars in CI (from encrypted GitHub Secrets — the
    // keystore file is NEVER committed to this public repo). When the env vars are absent (local
    // builds, contributors, PRs), release falls back to the debug key so everything still builds.
    val releaseStorePath: String? = System.getenv("SIGNING_KEYSTORE_PATH")
    val hasReleaseSigning = !releaseStorePath.isNullOrBlank() && file(releaseStorePath).exists()
    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStorePath)
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // R8: shrink + obfuscate + strip unused resources for a much smaller APK.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Use the stable release key when available (CI), else the debug key so local builds
            // still produce an installable, sideloadable APK.
            signingConfig =
                if (hasReleaseSigning) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
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
