package com.eventsnap.android.feature.history.data

import com.eventsnap.android.core.data.history.EventHistoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class HistoryRepositoryImpl(
    private val dao: EventHistoryDao,
) : HistoryRepository {
    override fun observeHistory(): Flow<List<HistoryItem>> =
        dao.observeAll().map { entities ->
            entities.map { entity ->
                HistoryItem(
                    id = entity.id,
                    title = entity.title,
                    startEpochMillis = entity.startEpochMillis,
                    allDay = entity.allDay,
                    location = entity.location,
                    createdAtEpochMillis = entity.createdAtEpochMillis,
                )
            }
        }
}
