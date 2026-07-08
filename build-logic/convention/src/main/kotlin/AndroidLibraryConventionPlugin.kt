import com.android.build.api.dsl.LibraryExtension
import eventsnap.android.buildlogic.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // AGP 9.2+: do NOT apply org.jetbrains.kotlin.android manually (see Application plugin).
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
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
                    // Non-flavored library modules consume the flavored :core:data. When built in
                    // isolation (feature unit tests, `test`), no app root propagates the `env`
                    // flavor, so default to `qa` to resolve the otherwise-ambiguous variant.
                    // Modules that declare their own `env` dimension (core/data) ignore this.
                    missingDimensionStrategy("env", "qa")
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }
            }
            dependencies {
                add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
                add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
                add("implementation", libs.findLibrary("koin-core").get())
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("mockito-kotlin").get())
                add("testImplementation", libs.findLibrary("truth").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
                add("testImplementation", libs.findLibrary("turbine").get())
                if (path != ":core:testing") {
                    add("testImplementation", project(":core:testing"))
                }
            }
        }
    }
}
