package io.nicolaszurbuchen.tallgrass.core.error

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WifiOff
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.error_database_generic_subtitle
import tallgrass.shared.generated.resources.error_database_insert_failed_title
import tallgrass.shared.generated.resources.error_database_not_found_subtitle
import tallgrass.shared.generated.resources.error_database_not_found_title
import tallgrass.shared.generated.resources.error_database_query_failed_title
import tallgrass.shared.generated.resources.error_network_http_subtitle_default
import tallgrass.shared.generated.resources.error_network_http_title
import tallgrass.shared.generated.resources.error_network_timeout_subtitle
import tallgrass.shared.generated.resources.error_network_timeout_title
import tallgrass.shared.generated.resources.error_network_unavailable_subtitle
import tallgrass.shared.generated.resources.error_network_unavailable_title
import tallgrass.shared.generated.resources.error_unexpected_subtitle
import tallgrass.shared.generated.resources.error_unexpected_title

fun AppError.toUiModel(): AppErrorUiModel =
    when (this) {
        is AppError.Network.Unavailable -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_network_unavailable_title),
                subtitle = UiText.Resource(Res.string.error_network_unavailable_subtitle),
                icon = Icons.Outlined.WifiOff,
            )
        }

        is AppError.Network.Timeout -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_network_timeout_title),
                subtitle = UiText.Resource(Res.string.error_network_timeout_subtitle),
                icon = Icons.Outlined.WifiOff,
            )
        }

        is AppError.Network.Http -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_network_http_title),
                subtitle =
                    serverMessage?.let { UiText.Raw(it) }
                        ?: UiText.Resource(Res.string.error_network_http_subtitle_default),
                icon = Icons.Outlined.WifiOff,
            )
        }

        is AppError.Database.NotFound -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_database_not_found_title),
                subtitle = UiText.Resource(Res.string.error_database_not_found_subtitle),
                icon = Icons.Outlined.SearchOff,
            )
        }

        is AppError.Database.QueryFailed -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_database_query_failed_title),
                subtitle = UiText.Resource(Res.string.error_database_generic_subtitle),
                icon = Icons.Outlined.Storage,
            )
        }

        is AppError.Database.InsertFailed -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_database_insert_failed_title),
                subtitle = UiText.Resource(Res.string.error_database_generic_subtitle),
                icon = Icons.Outlined.Storage,
            )
        }

        is AppError.Unexpected -> {
            AppErrorUiModel(
                title = UiText.Resource(Res.string.error_unexpected_title),
                subtitle = UiText.Resource(Res.string.error_unexpected_subtitle),
                icon = Icons.Outlined.ErrorOutline,
            )
        }
    }
