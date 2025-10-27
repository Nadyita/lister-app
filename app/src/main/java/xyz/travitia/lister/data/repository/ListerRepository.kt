package xyz.travitia.lister.data.repository

import android.util.Log
import kotlinx.coroutines.flow.first
import retrofit2.Response
import xyz.travitia.lister.data.model.*
import xyz.travitia.lister.data.preferences.SettingsPreferences
import xyz.travitia.lister.data.remote.ApiClient

class ListerRepository(private val settingsPreferences: SettingsPreferences) {

    companion object {
        private const val TAG = "ListerRepository"
    }

    private suspend fun getApiService() = ApiClient.getApiService(
        settingsPreferences.baseUrl.first(),
        settingsPreferences.bearerToken.first()
    )

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            Log.d(TAG, "API Response: ${response.code()} - ${response.message()}")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d(TAG, "API Success: $body")
                    Result.success(body)
                } else if (response.code() == 204) {
                    // 204 No Content is a success without body
                    Log.d(TAG, "API Success: 204 No Content")
                    @Suppress("UNCHECKED_CAST")
                    Result.success(Unit as T)
                } else {
                    val error = "API Error: Response body is null for ${response.code()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } else {
                val error = "API Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "API Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getLists(): Result<List<ShoppingListWithCount>> {
        Log.d(TAG, "==> getLists()")
        return safeApiCall { getApiService().getLists() }
    }

    suspend fun createList(name: String): Result<ShoppingList> {
        Log.d(TAG, "==> createList(name=$name)")
        return safeApiCall { getApiService().createList(CreateListRequest(name)) }
    }

    suspend fun updateList(id: Int, name: String): Result<ShoppingList> {
        Log.d(TAG, "==> updateList(id=$id, name=$name)")
        return safeApiCall { getApiService().updateList(id, UpdateListRequest(name)) }
    }

    suspend fun deleteList(id: Int): Result<Unit> {
        Log.d(TAG, "==> deleteList(id=$id)")
        return safeApiCall { getApiService().deleteList(id) }
    }

    suspend fun getItems(listId: Int): Result<List<Item>> {
        Log.d(TAG, "==> getItems(listId=$listId)")
        return safeApiCall { getApiService().getItems(listId) }
    }

    suspend fun createItem(listId: Int, request: CreateItemRequest): Result<Item> {
        Log.d(TAG, "==> createItem(listId=$listId, request=$request)")
        return safeApiCall { getApiService().createItem(listId, request) }
    }

    suspend fun updateItem(id: Int, request: UpdateItemRequest): Result<Item> {
        Log.d(TAG, "==> updateItem(id=$id, request=$request)")
        return safeApiCall { getApiService().updateItem(id, request) }
    }

    suspend fun deleteItem(id: Int): Result<Unit> {
        Log.d(TAG, "==> deleteItem(id=$id)")
        return safeApiCall { getApiService().deleteItem(id) }
    }

    suspend fun toggleItemCart(id: Int): Result<Item> {
        Log.d(TAG, "==> toggleItemCart(id=$id) - PATCH /items/$id/toggle")
        return safeApiCall { getApiService().toggleItemCart(id) }
    }

    suspend fun getCategories(): Result<List<Category>> {
        Log.d(TAG, "==> getCategories()")
        return safeApiCall { getApiService().getCategories() }
    }

    suspend fun searchItems(): Result<List<String>> {
        Log.d(TAG, "==> searchItems()")
        return safeApiCall { getApiService().searchItems() }
    }

    suspend fun getCategoryMappings(): Result<Map<String, String?>> {
        Log.d(TAG, "==> getCategoryMappings()")
        return safeApiCall { getApiService().getCategoryMappings() }
    }
}

