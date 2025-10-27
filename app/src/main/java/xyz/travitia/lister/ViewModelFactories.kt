package xyz.travitia.lister

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xyz.travitia.lister.data.preferences.SettingsPreferences
import xyz.travitia.lister.data.repository.ListerRepository
import xyz.travitia.lister.ui.viewmodel.ListDetailViewModel
import xyz.travitia.lister.ui.viewmodel.ListOverviewViewModel
import xyz.travitia.lister.ui.viewmodel.SettingsViewModel

class ListOverviewViewModelFactory(
    private val repository: ListerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListOverviewViewModel::class.java)) {
            return ListOverviewViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ListDetailViewModelFactory(
    private val repository: ListerRepository,
    private val settingsPreferences: SettingsPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListDetailViewModel::class.java)) {
            return ListDetailViewModel(repository, settingsPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModelFactory(
    private val settingsPreferences: SettingsPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(settingsPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

