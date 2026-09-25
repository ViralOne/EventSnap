package com.eventsnap.android.core.data.groq

import com.eventsnap.android.core.data.settings.SettingsStore
import com.eventsnap.android.core.model.AiModel
import com.eventsnap.android.core.model.AiModelArm
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/** How long a fetched model list is trusted before it's re-fetched. */
private const val CACHE_TTL_MILLIS = 6L * 60 * 60 * 1000

class GroqModelRegistryImpl(
    private val groqApi: GroqApi,
    private val settingsStore: SettingsStore,
    private val now: () -> Long = System::currentTimeMillis,
) : GroqModelRegistry {
    private val mutex = Mutex()

    // markUnavailable() can be called off the coroutine that filled the cache, so both are shared
    // mutable state: keep the list volatile and the id set concurrent.
    @Volatile
    private var cached: List<AiModel> = emptyList()

    @Volatile
    private var cachedAtMillis: Long = 0L

    /** Ids Groq rejected during this session; skipped even if the live list still advertises them. */
    private val unavailable: MutableSet<String> = ConcurrentHashMap.newKeySet()

    override suspend fun availableModels(forceRefresh: Boolean): List<AiModel> =
        mutex.withLock {
            val fresh = cached.isNotEmpty() && now() - cachedAtMillis < CACHE_TTL_MILLIS
            if (fresh && !forceRefresh) return@withLock cached

            val apiKey = settingsStore.groqApiKey.first()
            if (apiKey.isNullOrBlank()) return@withLock cached

            runCatching { groqApi.models(authorization = "Bearer $apiKey") }
                .map { response ->
                    response.data
                        .filter { it.active }
                        .map(::toAiModel)
                        .sortedWith(DISPLAY_ORDER)
                }.onSuccess { models ->
                    if (models.isNotEmpty()) {
                        cached = models
                        cachedAtMillis = now()
                    }
                }.onFailure { error ->
                    // A failed list must never block a capture — resolve() falls back to preferences.
                    Timber.w(error, "Could not fetch Groq model list")
                }
            cached
        }

    override suspend fun resolve(arm: AiModelArm): String {
        val live = availableModels()
        val liveIds = live.map { it.id }
        val preferences = if (arm == AiModelArm.VISION) GroqModelCatalog.VISION_PREFERENCES else GroqModelCatalog.TEXT_PREFERENCES
        val fallback = if (arm == AiModelArm.VISION) GroqModelCatalog.VISION_MODEL else GroqModelCatalog.TEXT_MODEL

        // In order: the user's pinned model, then the preference order, then any live model that
        // fits the arm, then the built-in default. A pinned or preferred id is only accepted when
        // Groq still lists it — an empty live list means "couldn't check", not "nothing available",
        // so it must not veto anything.
        val pinned = pinnedModel(arm)?.takeIf { liveIds.isEmpty() || it in liveIds }
        val preferred = preferences.firstOrNull { it in liveIds }
        val anyLiveForArm = live.firstOrNull { arm != AiModelArm.VISION || it.supportsVision }?.id

        return listOfNotNull(pinned, preferred, anyLiveForArm)
            .firstOrNull { it !in unavailable }
            ?: preferences.firstOrNull { it !in unavailable }
            ?: fallback
    }

    override fun markUnavailable(modelId: String) {
        unavailable += modelId
        // Drop it from the cache too, so the Settings list stops offering a model Groq just rejected.
        cached = cached.filterNot { it.id == modelId }
    }

    private suspend fun pinnedModel(arm: AiModelArm): String? =
        when (arm) {
            AiModelArm.TEXT -> settingsStore.textModel.first()
            AiModelArm.VISION -> settingsStore.visionModel.first()
        }?.takeIf { it.isNotBlank() }

    private fun toAiModel(dto: GroqModelDto): AiModel =
        AiModel(
            id = dto.id,
            ownedBy = dto.owned_by,
            contextWindow = dto.context_window,
            supportsVision = GroqModelCatalog.looksVisionCapable(dto.id),
        )

    private companion object {
        /** Vision-capable first (the scarce capability), then alphabetical for a stable picker. */
        val DISPLAY_ORDER: Comparator<AiModel> =
            compareByDescending<AiModel> { it.supportsVision }.thenBy { it.id }
    }
}
