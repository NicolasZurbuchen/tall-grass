package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase.GetAbilityDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase.GetAbilityHoldersUseCase
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface AbilityDetailStore : Store<AbilityDetailIntent, AbilityDetailState, AbilityDetailLabel>

class AbilityDetailStoreFactory(
    private val storeFactory: StoreFactory,
    private val getAbilityDetail: GetAbilityDetailUseCase,
    private val getAbilityHolders: GetAbilityHoldersUseCase,
) {
    fun create(slug: String): AbilityDetailStore =
        object :
            AbilityDetailStore,
            Store<AbilityDetailIntent, AbilityDetailState, AbilityDetailLabel> by storeFactory.create(
                name = "AbilityDetailStore",
                initialState = AbilityDetailState(isLoading = true),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl(slug) },
                reducer = ReducerImpl,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<AbilityDetailAction>() {
        override fun invoke() {
            dispatch(AbilityDetailAction.LoadAbility)
        }
    }

    private inner class ExecutorImpl(
        private val slug: String,
    ) : CoroutineExecutor<
            AbilityDetailIntent,
            AbilityDetailAction,
            AbilityDetailState,
            AbilityDetailMessage,
            AbilityDetailLabel,
        >() {
        override fun executeAction(action: AbilityDetailAction) {
            when (action) {
                AbilityDetailAction.LoadAbility -> load()
            }
        }

        override fun executeIntent(intent: AbilityDetailIntent) {
            when (intent) {
                is AbilityDetailIntent.TabSelected -> dispatch(AbilityDetailMessage.TabChanged(intent.tab))
                is AbilityDetailIntent.HolderClicked -> navigateToPokemon(intent.variantSlug)
                AbilityDetailIntent.RetryClicked -> load()
            }
        }

        // The holder is read back out of state rather than carried on the Intent: what the Pokemon's
        // hero opens with is a fact about the row that was tapped, and the Store is where that is
        // known. The same shape the dex uses for its cards.
        private fun navigateToPokemon(variantSlug: String) {
            val holder = state().holders.firstOrNull { it.variantSlug == variantSlug } ?: return

            publish(
                AbilityDetailLabel.NavigateToPokemon(
                    cardSlug = holder.cardSlug,
                    formSlug = holder.variantSlug,
                    name = holder.name,
                    artworkUrl = holder.artworkUrl,
                    primaryTypeSlug = holder.primaryType.slug,
                    secondaryTypeSlug = holder.secondaryType?.slug,
                ),
            )
        }

        private fun load() {
            dispatch(AbilityDetailMessage.LoadStarted)
            scope.launch {
                try {
                    // A slug with no row is a disagreement between this build and the bundled
                    // dataset rather than a lookup that can legitimately miss, so it reads as the
                    // not-found error rather than as an empty screen. An empty holder list is not
                    // the same thing -- see the Known by tab, which says so out loud.
                    val ability = getAbilityDetail(slug)
                    if (ability == null) {
                        dispatch(AbilityDetailMessage.LoadFailed(AppError.Database.NotFound))
                    } else {
                        dispatch(AbilityDetailMessage.AbilityLoaded(ability, getAbilityHolders(slug)))
                    }
                } catch (e: AppException) {
                    dispatch(AbilityDetailMessage.LoadFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(AbilityDetailMessage.LoadFailed(AppError.Unexpected(e)))
                }
            }
        }
    }

    internal object ReducerImpl : Reducer<AbilityDetailState, AbilityDetailMessage> {
        override fun AbilityDetailState.reduce(msg: AbilityDetailMessage): AbilityDetailState =
            when (msg) {
                AbilityDetailMessage.LoadStarted -> {
                    copy(isLoading = true, error = null)
                }

                is AbilityDetailMessage.AbilityLoaded -> {
                    copy(isLoading = false, ability = msg.ability, holders = msg.holders, error = null)
                }

                is AbilityDetailMessage.LoadFailed -> {
                    copy(isLoading = false, error = msg.error)
                }

                // Deliberately not reset by a reload: a retry from the Known by tab should come back
                // to it rather than to the one the screen happened to open on.
                is AbilityDetailMessage.TabChanged -> {
                    copy(tab = msg.tab)
                }
            }
    }
}
