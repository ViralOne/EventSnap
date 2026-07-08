import com.android.build.api.dsl.ApplicationExtension
import eventsnap.android.buildlogic.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // AGP 9.2+: do NOT apply org.jetbrains.kotlin.android manually — AGP's new DSL
            // hard-errors on the combination. Kotlin is auto-applied by com.android.application
            // when android.builtInKotlin=true (the default).
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                compileSdk =
                    libs
                        .findVersion("compileSdk")
                        .get()
                        .toString()
                        .toInt()
                defaultConfig {
                    minSdk =
                        libs
                            .findVersion("minSdk")
                            .get()
                            .toString()
                            .toInt()
                    targetSdk =
                        libs
                            .findVersion("targetSdk")
                            .get()
                            .toString()
                            .toInt()
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }
                buildFeatures {
                    buildConfig = true // BuildConfig.DEBUG gates Timber init in EventsnapApplication
                }
            }
        }
    }
}
