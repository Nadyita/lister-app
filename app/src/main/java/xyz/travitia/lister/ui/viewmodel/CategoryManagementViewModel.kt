package xyz.travitia.lister.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.travitia.lister.data.model.CategoryWithCount
import xyz.travitia.lister.data.repository.ListerRepository

data class CategoryManagementUiState(
    val categories: List<CategoryWithCount> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isMultiSelectMode: Boolean = false,
    val selectedCategories: Set<Int> = emptySet()
)

class CategoryManagementViewModel(private val repository: ListerRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val categoriesResult = repository.getCategories()

            if (categoriesResult.isSuccess) {
                val categories = categoriesResult.getOrNull() ?: emptyList()
                val categoriesWithCount = mutableListOf<CategoryWithCount>()

                // Load all lists to get all items
                val listsResult = repository.getLists()
                val allItems = mutableListOf<xyz.travitia.lister.data.model.Item>()

                if (listsResult.isSuccess) {
                    val lists = listsResult.getOrNull() ?: emptyList()
                    // Load items from all lists
                    for (list in lists) {
                        val itemsResult = repository.getItems(list.id)
                        if (itemsResult.isSuccess) {
                            allItems.addAll(itemsResult.getOrNull() ?: emptyList())
                        }
                    }
                }

                // Count items per category
                for (category in categories) {
                    val itemCount = allItems.count { it.category == category.name }
                    categoriesWithCount.add(
                        CategoryWithCount(
                            id = category.id,
                            name = category.name,
                            itemCount = itemCount
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(
                    categories = categoriesWithCount,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = categoriesResult.exceptionOrNull()?.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }

    fun renameCategory(categoryId: Int, newName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.updateCategory(categoryId, newName)

            if (result.isSuccess) {
                loadCategories()
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to rename category",
                    isLoading = false
                )
            }
        }
    }

    fun deleteCategory(categoryId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.deleteCategory(categoryId)

            if (result.isSuccess) {
                loadCategories()
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to delete category",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun toggleMultiSelectMode() {
        _uiState.value = _uiState.value.copy(
            isMultiSelectMode = !_uiState.value.isMultiSelectMode,
            selectedCategories = emptySet()
        )
    }

    fun toggleCategorySelection(categoryId: Int) {
        val currentSelection = _uiState.value.selectedCategories
        _uiState.value = _uiState.value.copy(
            selectedCategories = if (currentSelection.contains(categoryId)) {
                currentSelection - categoryId
            } else {
                currentSelection + categoryId
            }
        )
    }

    fun deleteSelectedCategories(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val selectedIds = _uiState.value.selectedCategories.toList()
            var hasError = false

            for (categoryId in selectedIds) {
                val result = repository.deleteCategory(categoryId)
                if (result.isFailure) {
                    hasError = true
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Failed to delete categories"
                    )
                    break
                }
            }

            if (!hasError) {
                _uiState.value = _uiState.value.copy(
                    isMultiSelectMode = false,
                    selectedCategories = emptySet()
                )
                loadCategories()
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}

