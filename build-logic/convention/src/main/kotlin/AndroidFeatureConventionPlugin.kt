import eventsnap.android.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("eventsnap.android.library")
            pluginManager.apply("eventsnap.android.library.compose")
            pluginManager.apply("eventsnap.android.lint")

            dependencies {
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("androidx-navigation3-runtime").get())
                add("implementation", libs.findLibrary("kotlinx-collections-immutable").get())
                add("implementation", libs.findLibrary("koin-androidx-compose").get())
            }
        }
    }
}
