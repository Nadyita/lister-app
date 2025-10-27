package xyz.travitia.lister.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.travitia.lister.data.model.ShoppingListWithCount
import xyz.travitia.lister.data.repository.ListerRepository

data class ListOverviewUiState(
    val lists: List<ShoppingListWithCount> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ListOverviewViewModel(private val repository: ListerRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ListOverviewUiState())
    val uiState: StateFlow<ListOverviewUiState> = _uiState.asStateFlow()

    init {
        loadLists()
    }

    fun loadLists() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getLists().fold(
                onSuccess = { lists ->
                    _uiState.value = _uiState.value.copy(
                        lists = lists,
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
}

