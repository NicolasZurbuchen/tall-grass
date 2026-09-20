package io.nicolaszurbuchen.tallgrass.design.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import io.nicolaszurbuchen.tallgrass.design.theme.appColors
import io.nicolaszurbuchen.tallgrass.design.theme.spacing
import org.jetbrains.compose.resources.stringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.header_back

/**
 * The top of a screen reached from somewhere else: a row holding the way back, and the title on its
 * own line underneath.
 *
 * Not Material's `TopAppBar`, which sets the title beside the navigation icon at body size. The
 * title here is the largest thing on the screen until the content starts, which is what tells the
 * reader where they landed. See `DECISIONS.md § A screen title is a heading, not a toolbar label`.
 */
@Composable
fun AppScreenHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.sm)) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.header_back),
                tint = MaterialTheme.appColors.textPrimary,
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.appColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier.padding(
                    start = MaterialTheme.spacing.sm,
                    end = MaterialTheme.spacing.sm,
                    top = MaterialTheme.spacing.sm,
                ),
        )
    }
}
