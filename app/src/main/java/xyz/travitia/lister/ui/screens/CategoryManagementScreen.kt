package xyz.travitia.lister.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.travitia.lister.R
import xyz.travitia.lister.data.model.CategoryWithCount
import xyz.travitia.lister.ui.components.CategoryActionDialog
import xyz.travitia.lister.ui.components.DeleteCategoryDialog
import xyz.travitia.lister.ui.components.RenameCategoryDialog
import xyz.travitia.lister.ui.viewmodel.CategoryManagementViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryManagementScreen(
    viewModel: CategoryManagementViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showActionDialog by remember { mutableStateOf<CategoryWithCount?>(null) }
    var showRenameDialog by remember { mutableStateOf<CategoryWithCount?>(null) }
    var showDeleteDialog by remember { mutableStateOf<CategoryWithCount?>(null) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_management_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.categories.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null && uiState.categories.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = uiState.error ?: stringResource(R.string.error_unknown),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.loadCategories() }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.button_retry))
                        }
                    }
                }
                uiState.categories.isEmpty() -> {
                    Text(
                        stringResource(R.string.no_categories_message),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.categories) { category ->
                            CategoryItem(
                                category = category,
                                onLongClick = { showActionDialog = category }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    showActionDialog?.let { category ->
        CategoryActionDialog(
            category = category,
            onDismiss = { showActionDialog = null },
            onRename = {
                showActionDialog = null
                showRenameDialog = category
            },
            onDelete = {
                showActionDialog = null
                showDeleteDialog = category
            }
        )
    }

    showRenameDialog?.let { category ->
        RenameCategoryDialog(
            currentName = category.name,
            onDismiss = { showRenameDialog = null },
            onSave = { newName ->
                viewModel.renameCategory(category.id, newName) {
                    showRenameDialog = null
                }
            }
        )
    }

    showDeleteDialog?.let { category ->
        DeleteCategoryDialog(
            category = category,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteCategory(category.id) {
                    showDeleteDialog = null
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryItem(
    category: CategoryWithCount,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onLongClick
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge
        )

        Badge(
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = category.itemCount.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

