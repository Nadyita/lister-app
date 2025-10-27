package xyz.travitia.lister.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {

    companion object {
        private val BASE_URL_KEY = stringPreferencesKey("base_url")
        private val BEARER_TOKEN_KEY = stringPreferencesKey("bearer_token")
        private val SUGGESTION_COUNT_KEY = intPreferencesKey("suggestion_count")
        const val DEFAULT_BASE_URL = "http://192.168.42.12/api"
        const val DEFAULT_SUGGESTION_COUNT = 3
    }

    val baseUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BASE_URL_KEY] ?: DEFAULT_BASE_URL
    }

    val bearerToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[BEARER_TOKEN_KEY]
    }

    val suggestionCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SUGGESTION_COUNT_KEY] ?: DEFAULT_SUGGESTION_COUNT
    }

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[BASE_URL_KEY] = url
        }
    }

    suspend fun setBearerToken(token: String?) {
        context.dataStore.edit { preferences ->
            if (token.isNullOrBlank()) {
                preferences.remove(BEARER_TOKEN_KEY)
            } else {
                preferences[BEARER_TOKEN_KEY] = token
            }
        }
    }

    suspend fun setSuggestionCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[SUGGESTION_COUNT_KEY] = count.coerceIn(0, 100)
        }
    }
}

