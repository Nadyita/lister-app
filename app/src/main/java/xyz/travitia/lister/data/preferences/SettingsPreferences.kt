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
import xyz.travitia.lister.data.model.PrimaryColor

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {

    companion object {
        private val BASE_URL_KEY = stringPreferencesKey("base_url")
        private val BEARER_TOKEN_KEY = stringPreferencesKey("bearer_token")
        private val SUGGESTION_COUNT_KEY = intPreferencesKey("suggestion_count")
        private val PRIMARY_COLOR_KEY = stringPreferencesKey("primary_color")
        private val LIST_ORDER_KEY = stringPreferencesKey("list_order")
        const val DEFAULT_BASE_URL = ""
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

    val primaryColor: Flow<PrimaryColor> = context.dataStore.data.map { preferences ->
        PrimaryColor.fromName(preferences[PRIMARY_COLOR_KEY])
    }

    val listOrder: Flow<Map<Int, Int>> = context.dataStore.data.map { preferences ->
        val orderString = preferences[LIST_ORDER_KEY] ?: ""
        if (orderString.isBlank()) {
            emptyMap()
        } else {
            orderString.split(",")
                .mapNotNull { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 2) {
                        parts[0].toIntOrNull()?.let { id ->
                            parts[1].toIntOrNull()?.let { order ->
                                id to order
                            }
                        }
                    } else null
                }
                .toMap()
        }
    }

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            val normalizedUrl = if (url.isNotBlank() && !url.endsWith("/")) "$url/" else url
            preferences[BASE_URL_KEY] = normalizedUrl
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

    suspend fun setPrimaryColor(color: PrimaryColor) {
        context.dataStore.edit { preferences ->
            preferences[PRIMARY_COLOR_KEY] = color.name
        }
    }

    suspend fun setListOrder(order: Map<Int, Int>) {
        context.dataStore.edit { preferences ->
            val orderString = order.entries.joinToString(",") { "${it.key}:${it.value}" }
            preferences[LIST_ORDER_KEY] = orderString
        }
    }
}

