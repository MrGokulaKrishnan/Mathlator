package com.mathlator.app.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathlator.app.data.db.HistoryDao
import com.mathlator.app.data.db.HistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HistoryGroup(
    val title: String,
    val items: List<HistoryEntity>,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyDao: HistoryDao,
) : ViewModel() {

    val groupedHistory: StateFlow<List<HistoryGroup>> = historyDao.observeAll()
        .map { list -> groupHistoryEntities(list) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private val _loadCalculationEvent = MutableSharedFlow<String>()
    val loadCalculationEvent = _loadCalculationEvent.asSharedFlow()

    private var lastDeletedItem: HistoryEntity? = null

    fun onItemClick(entity: HistoryEntity) {
        viewModelScope.launch {
            _loadCalculationEvent.emit(entity.expression)
        }
    }

    fun deleteItem(entity: HistoryEntity) {
        lastDeletedItem = entity
        viewModelScope.launch {
            historyDao.delete(entity)
        }
    }

    fun undoDelete() {
        val item = lastDeletedItem ?: return
        viewModelScope.launch {
            historyDao.insert(item)
            lastDeletedItem = null
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            historyDao.deleteAll()
        }
    }

    private fun groupHistoryEntities(entities: List<HistoryEntity>): List<HistoryGroup> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

        return entities.groupBy { entity ->
            val date = Instant.ofEpochMilli(entity.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            when (date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> date.format(dateFormatter)
            }
        }.map { (title, items) ->
            HistoryGroup(title = title, items = items)
        }
    }
}
