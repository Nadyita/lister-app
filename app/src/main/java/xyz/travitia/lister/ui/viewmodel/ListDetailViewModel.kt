package xyz.travitia.lister.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.travitia.lister.data.model.Category
import xyz.travitia.lister.data.model.CreateItemRequest
import xyz.travitia.lister.data.model.Item
import xyz.travitia.lister.data.model.UpdateItemRequest
import xyz.travitia.lister.data.preferences.SettingsPreferences
import xyz.travitia.lister.data.repository.ListerRepository

data class ListDetailUiState(
    val listId: Int = -1,
    val listName: String = "",
    val items: List<Item> = emptyList(),
    val itemSuggestions: List<String> = emptyList(),
    val categories: List<Category> = emptyList(),
    val categorySuggestions: List<String> = emptyList(),
    val categoryMappings: Map<String, String?> = emptyMap(),
    val suggestionCount: Int = SettingsPreferences.DEFAULT_SUGGESTION_COUNT,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ListDetailViewModel(
    private val repository: ListerRepository,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    companion object {
        private const val TAG = "ListDetailViewModel"
    }

    private val _uiState = MutableStateFlow(ListDetailUiState())
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()

    fun initialize(listId: Int, listName: String) {
        Log.d(TAG, "initialize(listId=$listId, listName=$listName)")
        _uiState.value = _uiState.value.copy(listId = listId, listName = listName)
        loadItems()
        loadSuggestions()
        loadCategories()
        loadCategoryMappings()
        loadSuggestionCount()
    }

    fun refresh() {
        Log.d(TAG, "refresh() for list ${_uiState.value.listId}")
        loadItems()
        loadSuggestions()
        loadCategories()
        loadCategoryMappings()
    }

    private fun loadItems() {
        viewModelScope.launch {
            Log.d(TAG, "loadItems() for list ${_uiState.value.listId}")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getItems(_uiState.value.listId).fold(
                onSuccess = { items ->
                    Log.d(TAG, "loadItems() success: ${items.size} items")
                    _uiState.value = _uiState.value.copy(
                        items = items,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "loadItems() error: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            repository.searchItems().fold(
                onSuccess = { suggestions ->
                    Log.d(TAG, "loadSuggestions() success: ${suggestions.size} suggestions")
                    _uiState.value = _uiState.value.copy(itemSuggestions = suggestions)
                },
                onFailure = { error ->
                    Log.e(TAG, "loadSuggestions() error: ${error.message}")
                }
            )
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories().fold(
                onSuccess = { categories ->
                    Log.d(TAG, "loadCategories() success: ${categories.size} categories")
                    val categoryNames = categories.map { it.name }
                    val sortedCategories = sortCategoriesByFrequency(categoryNames)
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        categorySuggestions = sortedCategories
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "loadCategories() error: ${error.message}")
                }
            )
        }
    }

    private fun sortCategoriesByFrequency(categories: List<String>): List<String> {
        val currentItems = _uiState.value.items
        val categoryFrequency = currentItems
            .mapNotNull { it.category }
            .groupingBy { it }
            .eachCount()

        return categories.sortedByDescending { categoryFrequency[it] ?: 0 }
    }

    private fun loadCategoryMappings() {
        viewModelScope.launch {
            repository.getCategoryMappings().fold(
                onSuccess = { mappings ->
                    Log.d(TAG, "loadCategoryMappings() success: ${mappings.size} mappings")
                    _uiState.value = _uiState.value.copy(categoryMappings = mappings)
                },
                onFailure = { error ->
                    Log.e(TAG, "loadCategoryMappings() error: ${error.message}")
                }
            )
        }
    }

    private fun loadSuggestionCount() {
        viewModelScope.launch {
            settingsPreferences.suggestionCount.collect { count ->
                _uiState.value = _uiState.value.copy(suggestionCount = count)
            }
        }
    }

    fun createItem(request: CreateItemRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "createItem($request)")
            repository.createItem(_uiState.value.listId, request).fold(
                onSuccess = {
                    Log.d(TAG, "createItem() success")
                    loadItems()
                    loadSuggestions()
                    loadCategories()
                    loadCategoryMappings()
                    onSuccess()
                },
                onFailure = { error ->
                    Log.e(TAG, "createItem() error: ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun updateItem(id: Int, request: UpdateItemRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "updateItem(id=$id, $request)")
            repository.updateItem(id, request).fold(
                onSuccess = { updatedItem ->
                    Log.d(TAG, "updateItem() success")
                    // Update nur das geänderte Item im State
                    val updatedItems = _uiState.value.items.map { item ->
                        if (item.id == updatedItem.id) updatedItem else item
                    }
                    _uiState.value = _uiState.value.copy(items = updatedItems)
                    loadCategoryMappings()
                    onSuccess()
                },
                onFailure = { error ->
                    Log.e(TAG, "updateItem() error: ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            Log.d(TAG, "deleteItem(id=$id)")
            repository.deleteItem(id).fold(
                onSuccess = {
                    Log.d(TAG, "deleteItem() success")
                    // Entferne das Item direkt aus dem State
                    val updatedItems = _uiState.value.items.filter { it.id != id }
                    _uiState.value = _uiState.value.copy(items = updatedItems)
                },
                onFailure = { error ->
                    Log.e(TAG, "deleteItem() error: ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun toggleItemCart(id: Int) {
        Log.d(TAG, ">>> toggleItemCart(id=$id) called <<<")
        viewModelScope.launch {
            repository.toggleItemCart(id).fold(
                onSuccess = { updatedItem ->
                    Log.d(TAG, "toggleItemCart() success: inCart=${updatedItem.inCart}")
                    // Update nur das geänderte Item im State, statt alles neu zu laden
                    val updatedItems = _uiState.value.items.map { item ->
                        if (item.id == updatedItem.id) updatedItem else item
                    }
                    _uiState.value = _uiState.value.copy(items = updatedItems)
                },
                onFailure = { error ->
                    Log.e(TAG, "toggleItemCart() error: ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun deleteAllInCartItems() {
        viewModelScope.launch {
            val itemsToDelete = _uiState.value.items.filter { it.inCart }
            Log.d(TAG, "deleteAllInCartItems(): ${itemsToDelete.size} items")
            itemsToDelete.forEach { item ->
                repository.deleteItem(item.id)
            }
            loadItems()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun renameCategory(categoryName: String, newName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "renameCategory(categoryName=$categoryName, newName=$newName)")
            // Find category by name
            val category = _uiState.value.categories.find { it.name == categoryName }
            if (category == null) {
                Log.e(TAG, "Category '$categoryName' not found")
                _uiState.value = _uiState.value.copy(error = "Category not found")
                return@launch
            }

            repository.updateCategory(category.id, newName).fold(
                onSuccess = {
                    Log.d(TAG, "renameCategory() success")
                    loadItems()
                    loadCategories()
                    loadCategoryMappings()
                    onSuccess()
                },
                onFailure = { error ->
                    Log.e(TAG, "renameCategory() error: ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }
}

