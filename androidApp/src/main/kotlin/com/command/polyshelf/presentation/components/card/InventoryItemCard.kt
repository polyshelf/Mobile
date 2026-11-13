package com.command.polyshelf.presentation.components.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.command.polyshelf.domain.product.model.FreshnessStatus
import com.command.polyshelf.domain.product.model.InventoryItem
import com.command.polyshelf.presentation.theme.FreshnessExpired
import com.command.polyshelf.presentation.theme.FreshnessExpiringSoon
import com.command.polyshelf.presentation.theme.FreshnessFresh
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onToggleConsumption: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDismissed by remember(item.id) { mutableStateOf(false) }
    var isTogglingConsumption by remember(item.id) { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                isDismissed = true
                true
            } else false
        }
    )

    LaunchedEffect(isDismissed) {
        if (isDismissed) {
            delay(280L.milliseconds)
            onDelete()
        }
    }

    LaunchedEffect(isTogglingConsumption) {
        if (isTogglingConsumption) {
            delay(220L.milliseconds)
            onToggleConsumption()
        }
    }

    AnimatedVisibility(
        visible = !isDismissed && !isTogglingConsumption,
        exit = shrinkVertically(animationSpec = tween(durationMillis = 280)) + fadeOut(
            animationSpec = tween(
                durationMillis = 180
            )
        ),
        enter = fadeIn(animationSpec = tween(durationMillis = 180))
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                val isSwipingLeft =
                    dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart ||
                            dismissState.targetValue == SwipeToDismissBoxValue.EndToStart

                val backgroundColor by animateColorAsState(
                    if (isSwipingLeft) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                    label = "DismissBackground"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (isSwipingLeft) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            },
            content = {
                InventoryItemContent(
                    item = item,
                    isToggling = isTogglingConsumption,
                    onToggleClick = { isTogglingConsumption = true },
                    onDeleteClick = { isDismissed = true },
                    modifier = modifier
                )
            }
        )
    }
}

@Composable
private fun InventoryItemContent(
    item: InventoryItem,
    isToggling: Boolean,
    onToggleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusText) = when (item.freshnessStatus) {
        FreshnessStatus.FRESH -> FreshnessFresh to "Свежий"
        FreshnessStatus.EXPIRING_SOON -> FreshnessExpiringSoon to "Истекает скоро"
        FreshnessStatus.EXPIRED -> FreshnessExpired to "Просрочен"
    }

    val isConsumedState = item.isConsumed || isToggling

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isConsumedState) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Употреблено",
                    tint = if (isConsumedState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isConsumedState) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Годен до: ${item.expirationDate} ($statusText)",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
