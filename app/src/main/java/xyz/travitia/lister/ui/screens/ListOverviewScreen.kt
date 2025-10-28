package xyz.travitia.lister.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import xyz.travitia.lister.R
import xyz.travitia.lister.data.model.ShoppingListWithCount
import xyz.travitia.lister.ui.components.CreateListDialog
import xyz.travitia.lister.ui.components.DeleteListDialog
import xyz.travitia.lister.ui.components.EditListDialog
import xyz.travitia.lister.ui.components.ListActionDialog
import xyz.travitia.lister.ui.viewmodel.ListOverviewViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListOverviewScreen(
    viewModel: ListOverviewViewModel,
    onNavigateToList: (Int, String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isRefreshing by remember { mutableStateOf(false) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showActionDialog by remember { mutableStateOf<ShoppingListWithCount?>(null) }
    var showEditDialog by remember { mutableStateOf<ShoppingListWithCount?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ShoppingListWithCount?>(null) }

    var localLists by remember(uiState.lists) { mutableStateOf(uiState.lists) }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            localLists = localLists.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    LaunchedEffect(uiState.isReorderMode) {
        if (!uiState.isReorderMode && localLists != uiState.lists) {
            // Save when exiting reorder mode
            viewModel.reorderLists(localLists)
        }
        if (uiState.isReorderMode) {
            // Reset local list when entering reorder mode
            localLists = uiState.lists
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            if (uiState.lists.isNotEmpty()) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.list_overview_title)) },
                actions = {
                    if (!uiState.isReorderMode) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.isReorderMode) {
                FloatingActionButton(
                    onClick = { viewModel.toggleReorderMode() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close reorder mode",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = { viewModel.toggleReorderMode() },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = "Reorder lists",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_new_list),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                viewModel.loadLists()
            },
            swipeEnabled = !uiState.isReorderMode,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.lists.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.error != null && uiState.lists.isEmpty() -> {
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
                                onClick = { viewModel.loadLists() }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.button_retry))
                            }
                        }
                    }
                    uiState.lists.isEmpty() -> {
                        Text(
                            stringResource(R.string.no_lists_message),
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    else -> {
                        val allLists = if (uiState.isReorderMode) localLists else uiState.lists
                        val displayLists = if (uiState.isReorderMode) {
                            allLists
                        } else {
                            allLists.filter { !uiState.hiddenLists.contains(it.id) }
                        }
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = lazyListState
                        ) {
                            items(
                                items = displayLists,
                                key = { it.id }
                            ) { list ->
                                ReorderableItem(reorderableLazyListState, key = list.id) { isDragging ->
                                    ListItem(
                                        list = list,
                                        onClick = {
                                            if (!uiState.isReorderMode) {
                                                onNavigateToList(list.id, list.name)
                                            }
                                        },
                                        onLongClick = {
                                            if (!uiState.isReorderMode) {
                                                showActionDialog = list
                                            }
                                        },
                                        isReorderMode = uiState.isReorderMode,
                                        isDragging = isDragging,
                                        dragModifier = Modifier.draggableHandle(),
                                        isVisible = !uiState.hiddenLists.contains(list.id),
                                        onVisibilityToggle = { viewModel.toggleListVisibility(list.id) }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateListDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createList(name) {
                    showCreateDialog = false
                }
            }
        )
    }

    showActionDialog?.let { list ->
        ListActionDialog(
            listName = list.name,
            onDismiss = { showActionDialog = null },
            onEdit = {
                showActionDialog = null
                showEditDialog = list
            },
            onDelete = {
                showActionDialog = null
                showDeleteDialog = list
            }
        )
    }

    showEditDialog?.let { list ->
        EditListDialog(
            currentName = list.name,
            onDismiss = { showEditDialog = null },
            onSave = { newName ->
                viewModel.updateList(list.id, newName) {
                    showEditDialog = null
                }
            }
        )
    }

    showDeleteDialog?.let { list ->
        DeleteListDialog(
            listName = list.name,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteList(list.id) {
                    showDeleteDialog = null
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItem(
    list: ShoppingListWithCount,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isReorderMode: Boolean,
    isDragging: Boolean,
    dragModifier: Modifier,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit
) {
    val elevation = if (isDragging) 8.dp else 0.dp
    
    Surface(
        shadowElevation = elevation,
        tonalElevation = if (isDragging) 4.dp else 0.dp
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (isReorderMode) {
                    Icon(
                        Icons.Default.DragHandle,
                        contentDescription = "Drag handle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = dragModifier
                    )
                }
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (isReorderMode) {
                Switch(
                    checked = isVisible,
                    onCheckedChange = { onVisibilityToggle() }
                )
            } else {
                list.count?.let { count ->
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = count.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
