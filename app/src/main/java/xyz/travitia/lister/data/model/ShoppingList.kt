package xyz.travitia.lister.data.model

data class ShoppingList(
    val id: Int,
    val name: String
)

data class ShoppingListWithCount(
    val id: Int,
    val name: String,
    val count: Int?
)

data class CreateListRequest(
    val name: String
)

data class UpdateListRequest(
    val name: String
)

