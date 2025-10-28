package xyz.travitia.lister.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
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

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    // Show error as Snackbar only if lists are not empty
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
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
                    }
                }
            )
        },
        floatingActionButton = {
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
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                viewModel.loadLists()
            },
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.lists) { list ->
                                ListItem(
                                    list = list,
                                    onClick = { onNavigateToList(list.id, list.name) },
                                    onLongClick = { showActionDialog = list }
                                )
                                HorizontalDivider()
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
            text = list.name,
            style = MaterialTheme.typography.bodyLarge
        )

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

