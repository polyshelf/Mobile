package com.command.polyshelf.presentation.screen.consumed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
fun ConsumedScreen(
    modifier: Modifier = Modifier,
    viewModel: ConsumedViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ConsumedContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumedContent(
    state: ConsumedState,
    onIntent: (ConsumedIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Употреблённые продукты") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                            text = "У вас нет съеденных продуктов.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    onIntent(ConsumedIntent.UnmarkConsumed(item))
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "«${item.name.truncateName()}» ➔ Продукты",
                                            actionLabel = "Отмена",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            onIntent(
                                                ConsumedIntent.UnmarkConsumed(
                                                    item.copy(
                                                        isConsumed = false
                                                    )
                                                )
                                            )
                                        }
                                    }
                                },
                                onDelete = {
                                    onIntent(ConsumedIntent.DeleteProduct(item.id))
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
                    onDismiss = { onIntent(ConsumedIntent.ClearError) },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConsumedContentPreview() {
    PolyShelfTheme {
        ConsumedContent(
            state = ConsumedState(
                items = listOf(
                    InventoryItem(
                        id = "1",
                        name = "Яблоки",
                        expirationDate = "2026-03-20",
                        isConsumed = true,
                        freshnessStatus = FreshnessStatus.FRESH
                    ),
                    InventoryItem(
                        id = "2",
                        name = "Хлеб",
                        expirationDate = "2026-03-15",
                        isConsumed = true,
                        freshnessStatus = FreshnessStatus.EXPIRED
                    )
                )
            ),
            onIntent = {}
        )
    }
}
