package xyz.travitia.lister.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.travitia.lister.R
import xyz.travitia.lister.data.model.CategoryWithCount
import xyz.travitia.lister.ui.components.CategoryActionDialog
import xyz.travitia.lister.ui.components.DeleteCategoryDialog
import xyz.travitia.lister.ui.components.RenameCategoryDialog
import xyz.travitia.lister.ui.theme.rememberBadgeFontSize
import xyz.travitia.lister.ui.theme.rememberBadgePadding
import xyz.travitia.lister.ui.theme.rememberBodyFontSize
import xyz.travitia.lister.ui.theme.rememberItemVerticalPadding
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
                    if (!uiState.isMultiSelectMode) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.isMultiSelectMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { viewModel.toggleMultiSelectMode() },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.button_cancel),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val hasSelection = uiState.selectedCategories.isNotEmpty()
                    FloatingActionButton(
                        onClick = {
                            if (hasSelection) {
                                viewModel.deleteSelectedCategories {
                                    // Success callback
                                }
                            }
                        },
                        containerColor = if (hasSelection) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.button_delete),
                            tint = if (hasSelection) {
                                MaterialTheme.colorScheme.onError
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            }
                        )
                    }
                }
            }
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
                                onClick = {
                                    if (uiState.isMultiSelectMode) {
                                        if (category.itemCount == 0) {
                                            viewModel.toggleCategorySelection(category.id)
                                        }
                                    } else {
                                        showActionDialog = category
                                    }
                                },
                                onLongClick = {
                                    if (!uiState.isMultiSelectMode) {
                                        viewModel.toggleMultiSelectMode()
                                        if (category.itemCount == 0) {
                                            viewModel.toggleCategorySelection(category.id)
                                        }
                                    }
                                },
                                isMultiSelectMode = uiState.isMultiSelectMode,
                                isSelected = uiState.selectedCategories.contains(category.id),
                                isSelectable = category.itemCount == 0
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
            errorMessage = uiState.error,
            onDismiss = {
                showRenameDialog = null
                viewModel.clearError()
            },
            onSave = { newName ->
                viewModel.renameCategory(category.id, newName) {
                    showRenameDialog = null
                    viewModel.clearError()
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    isSelectable: Boolean
) {
    val bodyFontSize = rememberBodyFontSize()
    val badgeFontSize = rememberBadgeFontSize()
    val badgePadding = rememberBadgePadding()
    val verticalPadding = rememberItemVerticalPadding()
    
    // Reduce horizontal padding for smaller font sizes
    val horizontalPadding = when {
        bodyFontSize.value <= 14f -> 8.dp   // SMALL
        bodyFontSize.value <= 16f -> 10.dp  // MEDIUM
        else -> 16.dp  // LARGE
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Always reserve space for checkbox to prevent layout shift
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isMultiSelectMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        enabled = isSelectable
                    )
                }
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = bodyFontSize,
                    lineHeight = bodyFontSize * 1.2f
                ),
                color = if (isMultiSelectMode && !isSelectable) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1
            )
        }

        Badge(
            containerColor = if (category.itemCount == 0) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            }
        ) {
            Text(
                text = category.itemCount.toString(),
                fontSize = badgeFontSize,
                color = if (category.itemCount == 0) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                modifier = Modifier.padding(horizontal = badgePadding)
            )
        }
    }
}

