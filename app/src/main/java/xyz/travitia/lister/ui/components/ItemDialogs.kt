package xyz.travitia.lister.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onCreate: (CreateItemRequest) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var showItemSuggestions by remember { mutableStateOf(false) }
    var showCategorySuggestions by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val filteredItemSuggestions = remember(itemName, suggestions, suggestionCount) {
        if (itemName.isNotBlank()) {
            val filtered = suggestions.filter { it.contains(itemName, ignoreCase = true) }
            if (suggestionCount == 0) filtered else filtered.take(suggestionCount)
        } else {
            emptyList()
        }
    }

    val filteredCategorySuggestions = remember(category, categorySuggestions, suggestionCount) {
        val filtered = if (category.isNotBlank()) {
            categorySuggestions.filter { it.contains(category, ignoreCase = true) }
        } else {
            categorySuggestions
        }
        if (suggestionCount == 0) filtered else filtered.take(suggestionCount)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
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

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                                amount = amount.toDoubleOrNull(),
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
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

@Composable
fun EditItemDialog(
    item: Item,
    categorySuggestions: List<String>,
    suggestionCount: Int = 3,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSave: (UpdateItemRequest) -> Unit
) {
    var itemName by remember {
        mutableStateOf(
            TextFieldValue(
                text = item.name,
                selection = TextRange(item.name.length)
            )
        )
    }
    var amount by remember {
        mutableStateOf(
            item.amount?.let { amt ->
                if (amt % 1.0 == 0.0) {
                    amt.toInt().toString()
                } else {
                    amt.toString()
                }
            } ?: ""
        )
    }
    var unit by remember { mutableStateOf(item.amountUnit ?: "") }
    var category by remember { mutableStateOf(item.category ?: "") }
    var showCategorySuggestions by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val filteredCategorySuggestions = remember(category, categorySuggestions, suggestionCount) {
        val filtered = if (category.isNotBlank()) {
            categorySuggestions.filter { it.contains(category, ignoreCase = true) }
        } else {
            categorySuggestions
        }
        if (suggestionCount == 0) filtered else filtered.take(suggestionCount)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
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

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (itemName.text.isNotBlank()) {
                        val parsedAmount = if (amount.isBlank()) null else amount.toDoubleOrNull()
                        onSave(
                            UpdateItemRequest(
                                name = itemName.text,
                                amount = parsedAmount,
                                amountUnit = if (parsedAmount == null) null else (if (unit.isBlank()) null else unit),
                                category = if (category.isBlank()) null else category
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
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}
