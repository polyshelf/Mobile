package com.command.polyshelf.di

import com.command.polyshelf.presentation.screen.consumed.ConsumedViewModel
import com.command.polyshelf.presentation.screen.inventory.InventoryViewModel
import com.command.polyshelf.presentation.screen.scanner.ScannerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val productUiModule = module {
    viewModelOf(::ScannerViewModel)
    viewModelOf(::InventoryViewModel)
    viewModelOf(::ConsumedViewModel)
}
