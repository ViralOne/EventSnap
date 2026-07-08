import com.android.build.api.dsl.LibraryExtension
import eventsnap.android.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
                testOptions {
                    unitTests {
                        // Required for Robolectric Compose tests — without this, createComposeRule()
                        // can't find ComponentActivity in the merged manifest and tests crash with
                        // "Unable to resolve activity for Intent".
                        isIncludeAndroidResources = true
                    }
                }
            }

            dependencies {
                val bom = libs.findLibrary("androidx-compose-bom").get()
                add("implementation", platform(bom))
                add("implementation", libs.findLibrary("androidx-compose-ui").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
                add("implementation", libs.findLibrary("androidx-compose-foundation").get())
                add("implementation", libs.findLibrary("androidx-compose-material3").get())
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
                add("testImplementation", platform(bom))
                add("testImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
                add("testImplementation", libs.findLibrary("robolectric").get())
                add("testImplementation", libs.findLibrary("androidx-test-ext-junit").get())
                add("androidTestImplementation", platform(bom))
                add("androidTestImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
            }
        }
    }
}
