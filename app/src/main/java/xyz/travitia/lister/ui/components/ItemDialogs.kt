package xyz.travitia.lister.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.travitia.lister.R
import xyz.travitia.lister.data.model.CreateItemRequest
import xyz.travitia.lister.data.model.Item
import xyz.travitia.lister.data.model.UpdateItemRequest

@Composable
fun CreateItemDialog(
    suggestions: List<String>,
    categorySuggestions: List<String>,
    categoryMappings: Map<String, String?>,
    suggestionCount: Int = 3,
    onDismiss: () -> Unit,
    onCreate: (CreateItemRequest) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var showItemSuggestions by remember { mutableStateOf(false) }
    var showCategorySuggestions by remember { mutableStateOf(false) }

    val filteredItemSuggestions = remember(itemName, suggestions, suggestionCount) {
        if (itemName.isNotBlank()) {
            suggestions.filter { it.contains(itemName, ignoreCase = true) }.take(suggestionCount)
        } else {
            emptyList()
        }
    }

    val filteredCategorySuggestions = remember(category, categorySuggestions, suggestionCount) {
        if (category.isNotBlank()) {
            categorySuggestions.filter { it.contains(category, ignoreCase = true) }.take(suggestionCount)
        } else {
            categorySuggestions.take(suggestionCount)
        }
    }

    LaunchedEffect(itemName) {
        categoryMappings[itemName]?.let { suggestedCategory ->
            category = suggestedCategory
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_item_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.item_name_label))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = itemName,
                    onValueChange = {
                        itemName = it
                        showItemSuggestions = it.isNotBlank()
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (showItemSuggestions && filteredItemSuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        LazyColumn {
                            items(filteredItemSuggestions) { suggestion ->
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            itemName = suggestion
                                            showItemSuggestions = false
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.item_amount_label))
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        placeholder = { Text(stringResource(R.string.item_unit_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.item_category_label))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                        showCategorySuggestions = true
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            showCategorySuggestions = focusState.isFocused
                        }
                )

                if (showCategorySuggestions && filteredCategorySuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        LazyColumn {
                            items(filteredCategorySuggestions) { suggestion ->
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            category = suggestion
                                            showCategorySuggestions = false
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (itemName.isNotBlank()) {
                        onCreate(
                            CreateItemRequest(
                                name = itemName,
                                amount = amount.toIntOrNull(),
                                amountUnit = unit.ifBlank { null },
                                category = category.ifBlank { null }
                            )
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.button_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_close))
            }
        }
    )
}

@Composable
fun EditItemDialog(
    item: Item,
    categorySuggestions: List<String>,
    suggestionCount: Int = 3,
    onDismiss: () -> Unit,
    onSave: (UpdateItemRequest) -> Unit
) {
    var itemName by remember { mutableStateOf(item.name) }
    var amount by remember { mutableStateOf(item.amount?.toString() ?: "") }
    var unit by remember { mutableStateOf(item.amountUnit ?: "") }
    var category by remember { mutableStateOf(item.category ?: "") }
    var showCategorySuggestions by remember { mutableStateOf(false) }

    val filteredCategorySuggestions = remember(category, categorySuggestions, suggestionCount) {
        if (category.isNotBlank()) {
            categorySuggestions.filter { it.contains(category, ignoreCase = true) }.take(suggestionCount)
        } else {
            categorySuggestions.take(suggestionCount)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_item_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.item_name_label))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.item_amount_label))
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        placeholder = { Text(stringResource(R.string.item_unit_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.item_category_label))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                        showCategorySuggestions = true
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            showCategorySuggestions = focusState.isFocused
                        }
                )

                if (showCategorySuggestions && filteredCategorySuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        LazyColumn {
                            items(filteredCategorySuggestions) { suggestion ->
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            category = suggestion
                                            showCategorySuggestions = false
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (itemName.isNotBlank()) {
                        onSave(
                            UpdateItemRequest(
                                name = itemName,
                                amount = amount.toIntOrNull(),
                                amountUnit = unit.ifBlank { null },
                                category = category.ifBlank { null }
                            )
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.button_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_close))
            }
        }
    )
}
