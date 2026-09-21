package io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.DamageClass
import io.nicolaszurbuchen.tallgrass.core.move.presentation.uimodel.DamageClassUiModel

/** Exhaustive by construction: a fourth damage class breaks this at compile time. */
fun DamageClass.toUiModel(): DamageClassUiModel =
    when (this) {
        DamageClass.PHYSICAL -> DamageClassUiModel.PHYSICAL
        DamageClass.SPECIAL -> DamageClassUiModel.SPECIAL
        DamageClass.STATUS -> DamageClassUiModel.STATUS
    }
