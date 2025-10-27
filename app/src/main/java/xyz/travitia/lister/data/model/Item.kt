package xyz.travitia.lister.data.model

data class Item(
    val id: Int,
    val name: String,
    val amount: Int?,
    val amountUnit: String?,
    val inCart: Boolean,
    val list: Int,
    val category: String?
)

data class CreateItemRequest(
    val name: String,
    val amount: Int? = null,
    val amountUnit: String? = null,
    val category: String? = null
)

data class UpdateItemRequest(
    val name: String? = null,
    val amount: Int? = null,
    val amountUnit: String? = null,
    val category: String? = null
)

