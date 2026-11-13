package com.command.polyshelf.presentation.screen.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.command.polyshelf.presentation.screen.consumed.ConsumedScreen
import com.command.polyshelf.presentation.screen.inventory.InventoryScreen
import com.command.polyshelf.presentation.screen.scanner.ScannerScreen
import com.command.polyshelf.presentation.screen.settings.SettingsScreen

enum class MainTab {
    INVENTORY,
    SCANNER,
    CONSUMED,
    SETTINGS
}

@Composable
fun MainScreenContainer(
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(MainTab.INVENTORY.ordinal) }
    var previousTab by remember { mutableIntStateOf(MainTab.INVENTORY.ordinal) }

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            item(
                selected = selectedTab == MainTab.INVENTORY.ordinal,
                onClick = {
                    previousTab = selectedTab
                    selectedTab = MainTab.INVENTORY.ordinal
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Kitchen,
                        contentDescription = "Продукты"
                    )
                },
                label = { Text("Продукты") }
            )

            // Center Scanner Button highlighted with a primary circle
            item(
                selected = selectedTab == MainTab.SCANNER.ordinal,
                onClick = {
                    previousTab = selectedTab
                    selectedTab = MainTab.SCANNER.ordinal
                },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (selectedTab == MainTab.SCANNER.ordinal) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Сканер",
                            tint = if (selectedTab == MainTab.SCANNER.ordinal) {
                                Color.White
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                },
                label = { Text("Сканер") }
            )

            item(
                selected = selectedTab == MainTab.CONSUMED.ordinal,
                onClick = {
                    previousTab = selectedTab
                    selectedTab = MainTab.CONSUMED.ordinal
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Съедено"
                    )
                },
                label = { Text("Съедено") }
            )

            item(
                selected = selectedTab == MainTab.SETTINGS.ordinal,
                onClick = {
                    previousTab = selectedTab
                    selectedTab = MainTab.SETTINGS.ordinal
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Настройки"
                    )
                },
                label = { Text("Настройки") }
            )
        }
    ) {
        val isMovingRight = selectedTab > previousTab

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (isMovingRight) {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut()
                    )
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> width } + fadeOut()
                    )
                }.using(
                    SizeTransform(clip = false)
                )
            },
            label = "TabTransition"
        ) { targetTab ->
            when (targetTab) {
                MainTab.INVENTORY.ordinal -> {
                    InventoryScreen(
                        onChangePasswordClick = onChangePasswordClick,
                        onLogoutClick = onLogoutClick
                    )
                }

                MainTab.SCANNER.ordinal -> {
                    ScannerScreen(
                        onNavigateBack = {
                            previousTab = selectedTab
                            selectedTab = MainTab.INVENTORY.ordinal
                        },
                        onProductAdded = {
                            previousTab = selectedTab
                            selectedTab = MainTab.INVENTORY.ordinal
                        }
                    )
                }

                MainTab.CONSUMED.ordinal -> {
                    ConsumedScreen()
                }

                MainTab.SETTINGS.ordinal -> {
                    SettingsScreen()
                }
            }
        }
    }
}
