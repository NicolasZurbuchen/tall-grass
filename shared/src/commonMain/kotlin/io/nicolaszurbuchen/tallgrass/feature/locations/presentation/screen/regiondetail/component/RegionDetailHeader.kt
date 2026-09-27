package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.asString
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.header_back

/**
 * The region's name and the games it holds, and the toolbar it turns into.
 *
 * [collapseProgress] is how far the sheet below has been dragged up, and everything here goes with it
 * except the back arrow, which never moves. Same behaviour as a Pokemon's, a move's and an ability's
 * hero, and for the same reason -- see `DECISIONS.md § The sheet expands and the hero becomes a
 * toolbar`.
 *
 * **The name cross-fades rather than travelling**, unlike the ability header's single Text. A region's
 * hero is three lines -- the kana, the name and the games -- so there is no one element for the
 * toolbar title to be: two of the three have to go regardless, and a name that travels out of a stack
 * that is also collapsing reads as two animations fighting.
 */
@Composable
fun RegionDetailHeader(
    name: String,
    nativeName: String,
    subtitle: UiText?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapseProgress: Float = 0f,
) {
    // Front-loaded on purpose. The sheet is what the reader is moving, so what it is taking the place
    // of should be gone by the time they have decided to move it.
    val heroAlpha = 1f - (collapseProgress / HERO_FADE_BY).coerceIn(0f, 1f)
    val titleAlpha = ((collapseProgress - TITLE_FADE_FROM) / (1f - TITLE_FADE_FROM)).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(TOOLBAR_ROW_HEIGHT)) {
            // Outside the gutter: the arrow takes Material's navigation inset and the text does not.
            // DECISIONS.md § A back arrow takes the navigation inset, not the content gutter
            //
            // Deliberately outside the fade. It is the one control on this screen that has to work
            // whatever else is happening, and a target that is still moving can be missed.
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.header_back),
                    tint = Color.White,
                )
            }

            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = TITLE_GUTTER)
                        .alpha(titleAlpha),
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.lg)
                    .padding(bottom = MaterialTheme.spacing.lg)
                    .alpha(heroAlpha),
        ) {
            Text(
                text = nativeName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = NATIVE_ALPHA),
                maxLines = 1,
            )

            Text(
                text = name,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            subtitle?.let {
                Text(
                    text = it.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = SUBTITLE_ALPHA),
                    maxLines = 1,
                )
            }
        }
    }
}

// Material's toolbar height, which is what the back arrow's row has to be for the sheet's raised
// position to line up with it.
private val TOOLBAR_ROW_HEIGHT = 56.dp

// Enough room for the arrow on one side and its mirror on the other, so a centred title stays centred.
private val TITLE_GUTTER = 56.dp

// The hero is gone by the time the sheet is a third of the way up, and the title only starts arriving
// after that -- so the two never overlap and the row is never crowded.
private const val HERO_FADE_BY = 0.35f
private const val TITLE_FADE_FROM = 0.5f

private const val NATIVE_ALPHA = 0.7f
private const val SUBTITLE_ALPHA = 0.85f
