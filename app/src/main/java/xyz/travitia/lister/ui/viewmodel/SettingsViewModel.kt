package xyz.travitia.lister.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.travitia.lister.data.preferences.SettingsPreferences

data class SettingsUiState(
    val baseUrl: String = SettingsPreferences.DEFAULT_BASE_URL,
    val bearerToken: String = "",
    val suggestionCount: Int = SettingsPreferences.DEFAULT_SUGGESTION_COUNT,
    val isSaving: Boolean = false
)

class SettingsViewModel(private val settingsPreferences: SettingsPreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsPreferences.baseUrl.collect { url ->
                _uiState.value = _uiState.value.copy(baseUrl = url)
            }
        }
        viewModelScope.launch {
            settingsPreferences.bearerToken.collect { token ->
                _uiState.value = _uiState.value.copy(bearerToken = token ?: "")
            }
        }
        viewModelScope.launch {
            settingsPreferences.suggestionCount.collect { count ->
                _uiState.value = _uiState.value.copy(suggestionCount = count)
            }
        }
    }

    fun saveSettings(url: String, token: String, suggestionCount: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            settingsPreferences.setBaseUrl(url)
            settingsPreferences.setBearerToken(token.ifBlank { null })
            settingsPreferences.setSuggestionCount(suggestionCount)
            _uiState.value = _uiState.value.copy(isSaving = false)
            onSuccess()
        }
    }
}

