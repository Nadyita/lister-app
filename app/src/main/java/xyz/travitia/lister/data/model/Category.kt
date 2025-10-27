package xyz.travitia.lister.data.model

data class Category(
    val id: Int,
    val name: String
)

data class CreateCategoryRequest(
    val name: String
)

data class UpdateCategoryRequest(
    val name: String
)

