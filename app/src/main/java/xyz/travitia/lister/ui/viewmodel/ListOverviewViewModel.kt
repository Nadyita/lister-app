package xyz.travitia.lister.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.travitia.lister.data.model.ShoppingListWithCount
import xyz.travitia.lister.data.preferences.SettingsPreferences
import xyz.travitia.lister.data.repository.ListerRepository

data class ListOverviewUiState(
    val lists: List<ShoppingListWithCount> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isReorderMode: Boolean = false
)

class ListOverviewViewModel(
    private val repository: ListerRepository,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListOverviewUiState())
    val uiState: StateFlow<ListOverviewUiState> = _uiState.asStateFlow()

    private var listOrder: Map<Int, Int> = emptyMap()

    init {
        loadOrder()
        loadLists()
    }

    private fun loadOrder() {
        viewModelScope.launch {
            settingsPreferences.listOrder.collect { order ->
                listOrder = order
                // Re-sort lists when order changes
                val currentLists = _uiState.value.lists
                if (currentLists.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        lists = sortListsByOrder(currentLists)
                    )
                }
            }
        }
    }

    private fun sortListsByOrder(lists: List<ShoppingListWithCount>): List<ShoppingListWithCount> {
        return lists.sortedWith(compareBy(
            { listOrder[it.id] ?: Int.MAX_VALUE },
            { it.id }
        ))
    }

    fun loadLists() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getLists().fold(
                onSuccess = { lists ->
                    _uiState.value = _uiState.value.copy(
                        lists = sortListsByOrder(lists),
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun createList(name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.createList(name).fold(
                onSuccess = {
                    loadLists()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun updateList(id: Int, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateList(id, name).fold(
                onSuccess = {
                    loadLists()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun deleteList(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteList(id).fold(
                onSuccess = {
                    loadLists()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun toggleReorderMode() {
        _uiState.value = _uiState.value.copy(
            isReorderMode = !_uiState.value.isReorderMode
        )
    }

    fun reorderLists(newOrder: List<ShoppingListWithCount>) {
        viewModelScope.launch {
            val orderMap = newOrder.mapIndexed { index, list ->
                list.id to index
            }.toMap()
            settingsPreferences.setListOrder(orderMap)
            _uiState.value = _uiState.value.copy(lists = newOrder)
        }
    }
}

