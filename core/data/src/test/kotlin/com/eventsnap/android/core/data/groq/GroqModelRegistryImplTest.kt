package com.eventsnap.android.core.data.groq

import com.eventsnap.android.core.data.settings.SettingsStore
import com.eventsnap.android.core.model.AiModelArm
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private const val GPT_OSS = "openai/gpt-oss-120b"
private const val QWEN = "qwen/qwen3.8-27b"
private const val RETIRED_SCOUT = "meta-llama/llama-4-scout-17b-16e-instruct"

class GroqModelRegistryImplTest {
    private fun settings(
        apiKey: String? = "gsk_test",
        pinnedText: String? = null,
        pinnedVision: String? = null,
    ) = mock<SettingsStore>().apply {
        whenever(groqApiKey).thenReturn(flowOf(apiKey))
        whenever(textModel).thenReturn(flowOf(pinnedText))
        whenever(visionModel).thenReturn(flowOf(pinnedVision))
    }

    private suspend fun api(vararg ids: String) =
        mock<GroqApi>().apply {
            whenever(models(any())).thenReturn(GroqModelsResponse(ids.map { GroqModelDto(id = it) }))
        }

    @Test
    fun `resolve skips a preferred model Groq no longer lists`() =
        runTest {
            // gpt-oss-120b is the first text preference but isn't served here.
            val registry = GroqModelRegistryImpl(api(QWEN), settings())

            assertThat(registry.resolve(AiModelArm.TEXT)).isEqualTo(QWEN)
        }

    @Test
    fun `resolve prefers a vision-capable model for the image arm`() =
        runTest {
            val registry = GroqModelRegistryImpl(api(GPT_OSS, QWEN), settings())

            assertThat(registry.resolve(AiModelArm.VISION)).isEqualTo(QWEN)
        }

    @Test
    fun `a pinned model wins when Groq still serves it`() =
        runTest {
            val registry = GroqModelRegistryImpl(api(GPT_OSS, QWEN), settings(pinnedText = QWEN))

            assertThat(registry.resolve(AiModelArm.TEXT)).isEqualTo(QWEN)
        }

    @Test
    fun `a pinned model that Groq retired is ignored`() =
        runTest {
            val registry = GroqModelRegistryImpl(api(GPT_OSS, QWEN), settings(pinnedVision = RETIRED_SCOUT))

            assertThat(registry.resolve(AiModelArm.VISION)).isEqualTo(QWEN)
        }

    @Test
    fun `a model marked unavailable is not resolved again`() =
        runTest {
            val registry = GroqModelRegistryImpl(api(GPT_OSS, QWEN), settings())
            assertThat(registry.resolve(AiModelArm.TEXT)).isEqualTo(GPT_OSS)

            registry.markUnavailable(GPT_OSS)

            assertThat(registry.resolve(AiModelArm.TEXT)).isEqualTo(QWEN)
            assertThat(registry.availableModels().map { it.id }).doesNotContain(GPT_OSS)
        }

    @Test
    fun `an unreachable model list falls back to the built-in default`() =
        runTest {
            val failing = mock<GroqApi>().apply { whenever(models(any())).thenThrow(RuntimeException("offline")) }

            val registry = GroqModelRegistryImpl(failing, settings())

            assertThat(registry.availableModels()).isEmpty()
            assertThat(registry.resolve(AiModelArm.VISION)).isEqualTo(GroqModelCatalog.VISION_MODEL)
            assertThat(registry.resolve(AiModelArm.TEXT)).isEqualTo(GroqModelCatalog.TEXT_MODEL)
        }

    @Test
    fun `no api key means no model list and the built-in default`() =
        runTest {
            val registry = GroqModelRegistryImpl(mock(), settings(apiKey = null))

            assertThat(registry.availableModels()).isEmpty()
            assertThat(registry.resolve(AiModelArm.TEXT)).isEqualTo(GroqModelCatalog.TEXT_MODEL)
        }

    @Test
    fun `inactive models are not offered`() =
        runTest {
            val api =
                mock<GroqApi>().apply {
                    whenever(models(any())).thenReturn(
                        GroqModelsResponse(
                            listOf(GroqModelDto(id = GPT_OSS, active = false), GroqModelDto(id = QWEN)),
                        ),
                    )
                }

            val registry = GroqModelRegistryImpl(api, settings())

            assertThat(registry.availableModels().map { it.id }).containsExactly(QWEN)
            assertThat(registry.resolve(AiModelArm.TEXT)).isEqualTo(QWEN)
        }

    @Test
    fun `the list is cached until the ttl expires and force refresh bypasses it`() =
        runTest {
            val api = api(GPT_OSS)
            val registry = GroqModelRegistryImpl(api, settings()) { 0L }

            registry.availableModels()
            registry.availableModels()
            registry.availableModels(forceRefresh = true)

            // Two fetches: the first, plus the forced one. The middle call came from the cache.
            verify(api, times(2)).models(any())
        }
}
