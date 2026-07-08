plugins {
    `kotlin-dsl`
}

group = "eventsnap.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "eventsnap.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "eventsnap.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "eventsnap.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "eventsnap.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "eventsnap.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidFlavors") {
            id = "eventsnap.android.flavors"
            implementationClass = "AndroidFlavorsConventionPlugin"
        }
        register("androidLint") {
            id = "eventsnap.android.lint"
            implementationClass = "AndroidLintConventionPlugin"
        }
        register("androidRoom") {
            id = "eventsnap.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("jvmLibrary") {
            id = "eventsnap.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
