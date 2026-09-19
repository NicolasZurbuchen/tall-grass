package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.ENTRANCE_DONE
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.entranceFraction
import io.nicolaszurbuchen.tallgrass.design.theme.pop
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.FormPillUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.asString

/**
 * The form switcher.
 *
 * A row that scrolls, not a wrapping one: Arceus and Silvally have eighteen forms each, and a wrap
 * would push the tabs and everything under them off the bottom of the screen for those two.
 */
@Composable
fun FormPillRow(
    forms: List<FormPillUiModel>,
    activeSlug: String,
    onFormClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    elapsedMillis: Int = ENTRANCE_DONE,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        modifier = modifier.fillMaxWidth(),
    ) {
        itemsIndexed(items = forms, key = { _, form -> form.slug }) { index, form ->
            val isActive = form.slug == activeSlug

            Surface(
                onClick = { onFormClick(form.slug) },
                shape = MaterialTheme.shapes.extraLarge,
                color = if (isActive) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.surface,
                contentColor = if (isActive) MaterialTheme.appColors.textInverse else MaterialTheme.appColors.textSecondary,
                border = BorderStroke(PILL_BORDER, MaterialTheme.appColors.borderSubtle),
                modifier = Modifier.pop(entranceFraction(index, elapsedMillis)),
            ) {
                Text(
                    text = form.label.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier =
                        Modifier.padding(
                            horizontal = MaterialTheme.spacing.md,
                            vertical = MaterialTheme.spacing.sm,
                        ),
                )
            }
        }
    }
}

private val PILL_BORDER = 1.dp
