package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.mapper

import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.uimodel.PrefetchUiModel
import io.nicolaszurbuchen.tallgrass.infra.image.ImagePrefetchProgress
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_dex_prefetch_no_space
import tallgrass.shared.generated.resources.pokedex_dex_prefetch_progress

/**
 * Null when there is nothing worth saying, which is most of the time: a finished run is the expected
 * outcome and does not need announcing, and neither does a run that found everything already on
 * disk. Only a run in flight or one that gave up has a line.
 */
fun ImagePrefetchProgress.toUiModel(): PrefetchUiModel? =
    when {
        stoppedForSpace -> {
            PrefetchUiModel(
                message = UiText.Resource(Res.string.pokedex_dex_prefetch_no_space),
                fraction = null,
            )
        }

        isComplete -> {
            null
        }

        else -> {
            PrefetchUiModel(
                message =
                    UiText.Resource(
                        Res.string.pokedex_dex_prefetch_progress,
                        listOf((fraction * PERCENT).toInt().toString()),
                    ),
                fraction = fraction,
            )
        }
    }

private const val PERCENT = 100
