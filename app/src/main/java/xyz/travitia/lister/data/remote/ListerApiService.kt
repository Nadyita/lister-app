package xyz.travitia.lister.data.remote

import retrofit2.Response
import retrofit2.http.*
import xyz.travitia.lister.data.model.*

interface ListerApiService {

    @GET("lists")
    suspend fun getLists(): Response<List<ShoppingListWithCount>>

    @POST("lists")
    suspend fun createList(@Body request: CreateListRequest): Response<ShoppingList>

    @GET("lists/{id}")
    suspend fun getList(@Path("id") id: Int): Response<ShoppingList>

    @PUT("lists/{id}")
    suspend fun updateList(@Path("id") id: Int, @Body request: UpdateListRequest): Response<ShoppingList>

    @DELETE("lists/{id}")
    suspend fun deleteList(@Path("id") id: Int): Response<Unit>

    @GET("lists/{list_id}/items")
    suspend fun getItems(@Path("list_id") listId: Int): Response<List<Item>>

    @POST("lists/{list_id}/items")
    suspend fun createItem(@Path("list_id") listId: Int, @Body request: CreateItemRequest): Response<Item>

    @GET("items/{id}")
    suspend fun getItem(@Path("id") id: Int): Response<Item>

    @PUT("items/{id}")
    suspend fun updateItem(@Path("id") id: Int, @Body request: UpdateItemRequest): Response<Item>

    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: Int): Response<Unit>

    @PATCH("items/{id}/toggle")
    suspend fun toggleItemCart(@Path("id") id: Int): Response<Item>

    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<Category>

    @GET("categories/{id}")
    suspend fun getCategory(@Path("id") id: Int): Response<Category>

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body request: UpdateCategoryRequest): Response<Category>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<Unit>

    @GET("search")
    suspend fun searchItems(): Response<List<String>>

    @GET("search/category-mappings")
    suspend fun getCategoryMappings(): Response<Map<String, String?>>
}

