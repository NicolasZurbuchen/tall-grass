package io.nicolaszurbuchen.tallgrass.feature.moves.di

import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.MoveDetailStoreFactory
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.MoveDetailViewModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.MovesStoreFactory
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.MovesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val movesModule =
    module {
        factoryOf(::MovesStoreFactory)
        viewModelOf(::MovesViewModel)

        factoryOf(::MoveDetailStoreFactory)

        // Parameterised rather than declared with viewModelOf, on the same grounds as the Pokemon
        // detail: which move the screen is about arrives from the NavKey, so it is passed at
        // resolution rather than resolved.
        viewModel { (slug: String) -> MoveDetailViewModel(get(), slug) }
    }
