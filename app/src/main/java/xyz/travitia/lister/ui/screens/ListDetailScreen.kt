package xyz.travitia.lister.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import xyz.travitia.lister.R
import xyz.travitia.lister.data.model.Item
import xyz.travitia.lister.ui.components.CreateItemDialog
import xyz.travitia.lister.ui.components.EditItemDialog
import xyz.travitia.lister.ui.theme.ListerGray
import xyz.travitia.lister.ui.theme.ListerGreen
import xyz.travitia.lister.ui.viewmodel.ListDetailViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListDetailScreen(
    viewModel: ListDetailViewModel,
    listId: Int,
    listName: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Item?>(null) }
    var renamingCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(listId) {
        viewModel.initialize(listId, listName)
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    // Show error as Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    val itemsInCart = uiState.items.filter { it.inCart }
    val hasItemsInCart = itemsInCart.isNotEmpty()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(listName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (hasItemsInCart) {
                    FloatingActionButton(
                        onClick = { viewModel.deleteAllInCartItems() },
                        containerColor = ListerGreen
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.cd_delete_all_in_cart),
                            tint = Color.White
                        )
                    }
                }
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_new_item),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                viewModel.refresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.items.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        ItemsList(
                            items = uiState.items,
                            onItemClick = { item ->
                                viewModel.toggleItemCart(item.id)
                            },
                            onItemLongClick = { item ->
                                editingItem = item
                            },
                            onCategoryLongClick = { categoryName ->
                                renamingCategory = categoryName
                            },
                            onNoCategoryLongClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.cannot_rename_no_category)
                                    )
                                }
                            },
                            onInCartLongClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.cannot_rename_in_cart)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateItemDialog(
            suggestions = uiState.itemSuggestions,
            categorySuggestions = uiState.categorySuggestions,
            categoryMappings = uiState.categoryMappings,
            suggestionCount = uiState.suggestionCount,
            onDismiss = { showCreateDialog = false },
            onCreate = { request ->
                viewModel.createItem(request) {
                    showCreateDialog = false
                }
            }
        )
    }

    editingItem?.let { item ->
        EditItemDialog(
            item = item,
            categorySuggestions = uiState.categorySuggestions,
            suggestionCount = uiState.suggestionCount,
            onDismiss = { editingItem = null },
            onSave = { request ->
                viewModel.updateItem(item.id, request) {
                    editingItem = null
                }
            }
        )
    }

    renamingCategory?.let { categoryName ->
        RenameCategoryDialog(
            currentName = categoryName,
            onDismiss = { renamingCategory = null },
            onSave = { newName ->
                viewModel.renameCategory(categoryName, newName) {
                    renamingCategory = null
                }
            }
        )
    }
}

@Composable
fun ItemsList(
    items: List<Item>,
    onItemClick: (Item) -> Unit,
    onItemLongClick: (Item) -> Unit,
    onCategoryLongClick: (String) -> Unit,
    onNoCategoryLongClick: () -> Unit,
    onInCartLongClick: () -> Unit
) {
    val itemsNotInCart = items.filter { !it.inCart }
    val itemsInCart = items.filter { it.inCart }

    val noCategoryString = stringResource(R.string.category_no_category)
    val inCartString = stringResource(R.string.category_in_cart)

    val groupedNotInCart = itemsNotInCart.groupBy { it.category ?: noCategoryString }
    val groupedInCart = if (itemsInCart.isNotEmpty()) {
        mapOf(inCartString to itemsInCart)
    } else {
        emptyMap()
    }

    val allGroups = groupedNotInCart.entries.toList() + groupedInCart.entries.toList()
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }
    val primaryColor = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        allGroups.forEachIndexed { _, (category, categoryItems) ->
            val isInCart = category == inCartString
            val isNoCategory = category == noCategoryString
            val color = if (isInCart) ListerGray else primaryColor
            val isExpanded = expandedCategories[category] ?: true

            item(key = "header_$category") {
                CategoryHeader(
                    category = category,
                    color = color,
                    isExpanded = isExpanded,
                    onToggle = { expandedCategories[category] = !isExpanded },
                    onLongClick = when {
                        !isInCart && !isNoCategory -> {{ onCategoryLongClick(category) }}
                        isNoCategory -> onNoCategoryLongClick
                        isInCart -> onInCartLongClick
                        else -> null
                    }
                )
            }

            item(key = "content_$category") {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        categoryItems.forEach { item ->
                            ItemRow(
                                item = item,
                                onClick = { onItemClick(item) },
                                onLongClick = { onItemLongClick(item) }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryHeader(
    category: String,
    color: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val cdExpand = stringResource(R.string.cd_expand)
    val cdCollapse = stringResource(R.string.cd_collapse)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.3f))
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 20.sp,
                lineHeight = 24.sp
            ),
            fontWeight = FontWeight.Bold,
            color = color
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) cdCollapse else cdExpand,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemRow(
    item: Item,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 20.sp,
                lineHeight = 24.sp
            ),
            modifier = Modifier.weight(1f)
        )

        if (item.amount != null) {
            Badge(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = buildString {
                        // Format amount: remove ".0" for whole numbers
                        val formattedAmount = if (item.amount % 1.0 == 0.0) {
                            item.amount.toInt().toString()
                        } else {
                            item.amount.toString()
                        }
                        append(formattedAmount)
                        if (item.amountUnit != null) {
                            append(" ${item.amountUnit}")
                        }
                    },
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun RenameCategoryDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var categoryName by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentName,
                selection = TextRange(currentName.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_category_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.category_name_label))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (categoryName.text.isNotBlank() && categoryName.text != currentName) {
                        onSave(categoryName.text)
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

