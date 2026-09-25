package io.nicolaszurbuchen.tallgrass.feature.abilities.di

import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.AbilitiesStoreFactory
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.AbilitiesViewModel
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.AbilityDetailStoreFactory
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail.AbilityDetailViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val abilitiesModule =
    module {
        factoryOf(::AbilitiesStoreFactory)
        viewModelOf(::AbilitiesViewModel)

        factoryOf(::AbilityDetailStoreFactory)

        // Parameterised rather than declared with viewModelOf, on the same grounds as the move
        // detail: which ability the screen is about arrives from the NavKey, so it is passed at
        // resolution rather than resolved.
        viewModel { (slug: String) -> AbilityDetailViewModel(get(), slug) }
    }
