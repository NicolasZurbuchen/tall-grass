package io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * How a Pokemon turns up, as a row or a tab says it.
 *
 * **The actual method, never one of the four coarse groups.** #9's correction: each method is its own
 * 100% table, so merging Old Rod into a "Fishing" tab gives Magikarp 100% under one denominator and
 * 55% under another and a tab claiming 255%. The groups survive only as sort order.
 *
 * [label] is curated for the 44 methods where title-casing the upstream slug reads wrong -- `walk` is
 * "Tall grass", `sos` is "SOS call", `npc-trade` is "In-game trade" -- and falls back to the slug with
 * its hyphens opened out for the other twenty. A fallback is not a failure here: an unrecognised
 * method is one upstream has added since, and "Sky Battle" spelled out of its slug is still readable.
 */
@Immutable
data class EncounterMethodUiModel(
    val slug: String,
    val label: UiText,
)
