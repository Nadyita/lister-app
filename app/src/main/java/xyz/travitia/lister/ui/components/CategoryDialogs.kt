package xyz.travitia.lister.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.travitia.lister.R
import xyz.travitia.lister.data.model.CategoryWithCount

@Composable
fun RenameCategoryDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_category_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.category_name_label))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onSave(categoryName)
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

@Composable
fun DeleteCategoryDialog(
    category: CategoryWithCount,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val canDelete = category.itemCount == 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_category_dialog_title)) },
        text = {
            Text(
                if (canDelete) {
                    stringResource(R.string.delete_category_dialog_message, category.name)
                } else {
                    stringResource(R.string.delete_category_error_has_items, category.itemCount)
                }
            )
        },
        confirmButton = {
            if (canDelete) {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.button_yes))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    if (canDelete) {
                        stringResource(R.string.button_cancel)
                    } else {
                        stringResource(R.string.button_close)
                    }
                )
            }
        }
    )
}

@Composable
fun CategoryActionDialog(
    category: CategoryWithCount,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val canDelete = category.itemCount == 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(category.name) },
        text = { Text(stringResource(R.string.category_action_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onRename) {
                Text(stringResource(R.string.button_rename))
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onDelete,
                    enabled = canDelete
                ) {
                    Text(stringResource(R.string.button_delete))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        }
    )
}

