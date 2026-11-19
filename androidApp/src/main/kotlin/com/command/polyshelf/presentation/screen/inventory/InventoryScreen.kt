package com.command.polyshelf.presentation.screen.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.command.polyshelf.domain.product.model.FreshnessStatus
import com.command.polyshelf.domain.product.model.InventoryItem
import com.command.polyshelf.presentation.components.banner.WarningBanner
import com.command.polyshelf.presentation.components.card.InventoryItemCard
import com.command.polyshelf.presentation.theme.PolyShelfTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private fun String.truncateName(maxLength: Int = 18): String {
    return if (length > maxLength) substring(0, maxLength) + "…" else this
}

@Composable
fun InventoryScreen(
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    InventoryContent(
        state = state,
        onIntent = viewModel::onIntent,
        onChangePasswordClick = onChangePasswordClick,
        onLogoutClick = onLogoutClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryContent(
    state: InventoryState,
    onIntent: (InventoryIntent) -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Мои продукты") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Профиль"
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Сменить пароль") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onChangePasswordClick()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Выйти") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    showLogoutDialog = true
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading && state.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "У вас пока нет продуктов в инвентаре.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Отсканируйте штрихкод или DataMatrix, чтобы добавить первый продукт!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        Box(modifier = Modifier.animateItem()) {
                            InventoryItemCard(
                                item = item,
                                onToggleConsumption = {
                                    onIntent(InventoryIntent.ToggleConsumption(item))
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "«${item.name.truncateName()}» ➔ Съедено",
                                            actionLabel = "Отмена",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            onIntent(
                                                InventoryIntent.ToggleConsumption(
                                                    item.copy(
                                                        isConsumed = true
                                                    )
                                                )
                                            )
                                        }
                                    }
                                },
                                onDelete = {
                                    onIntent(InventoryIntent.DeleteProduct(item.id))
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "«${item.name.truncateName()}» удалён",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (state.networkErrorMessage != null) {
                WarningBanner(
                    message = state.networkErrorMessage,
                    onDismiss = { onIntent(InventoryIntent.ClearError) },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выход из аккаунта") },
            text = { Text("Вы действительно хотите выйти из аккаунта?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text("Выйти", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryContentPreview() {
    PolyShelfTheme {
        InventoryContent(
            state = InventoryState(
                items = listOf(
                    InventoryItem(
                        id = "1",
                        name = "Молоко 3.2%",
                        expirationDate = "2026-10-20",
                        isConsumed = false,
                        freshnessStatus = FreshnessStatus.FRESH
                    ),
                    InventoryItem(
                        id = "2",
                        name = "Творог 9%",
                        expirationDate = "2026-04-01",
                        isConsumed = false,
                        freshnessStatus = FreshnessStatus.EXPIRING_SOON
                    )
                )
            ),
            onIntent = {},
            onChangePasswordClick = {},
            onLogoutClick = {}
        )
    }
}
