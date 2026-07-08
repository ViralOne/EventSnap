import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

// No `import eventsnap.android.buildlogic.libs` here — this plugin reads URL strings from
// `gradle.properties` via `findProperty(...)`, not from the version catalog.

class AndroidFlavorsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val qaUrl = stringProperty("eventsnap.qaApiBaseUrl", "https://api.groq.com/openai/v1/")
            val prodUrl = stringProperty("eventsnap.prodApiBaseUrl", "https://api.groq.com/openai/v1/")

            when {
                pluginManager.hasPlugin("com.android.application") ->
                    extensions.configure<ApplicationExtension> {
                        buildFeatures { buildConfig = true }
                        flavorDimensions += "env"
                        productFlavors {
                            create("qa") {
                                dimension = "env"
                                isDefault = true
                                buildConfigField("boolean", "IS_QA", "true")
                                buildConfigField("String", "API_BASE_URL", "\"$qaUrl\"")
                            }
                            create("prod") {
                                dimension = "env"
                                applicationIdSuffix = ".prod"
                                versionNameSuffix = "-prod"
                                buildConfigField("boolean", "IS_QA", "false")
                                buildConfigField("String", "API_BASE_URL", "\"$prodUrl\"")
                            }
                        }
                    }
                pluginManager.hasPlugin("com.android.library") ->
                    extensions.configure<LibraryExtension> {
                        buildFeatures { buildConfig = true }
                        flavorDimensions += "env"
                        productFlavors {
                            create("qa") {
                                dimension = "env"
                                isDefault = true
                                buildConfigField("boolean", "IS_QA", "true")
                                buildConfigField("String", "API_BASE_URL", "\"$qaUrl\"")
                            }
                            create("prod") {
                                dimension = "env"
                                buildConfigField("boolean", "IS_QA", "false")
                                buildConfigField("String", "API_BASE_URL", "\"$prodUrl\"")
                            }
                        }
                    }
                else ->
                    error(
                        "eventsnap.android.flavors must be applied AFTER eventsnap.android.application " +
                            "or eventsnap.android.library — apply it last in the plugins { } block of " +
                            "app-mobile or core/data only.",
                    )
            }
        }
    }

    private fun Project.stringProperty(
        key: String,
        default: String,
    ): String = (findProperty(key) as? String)?.takeIf { it.isNotBlank() } ?: default
}
