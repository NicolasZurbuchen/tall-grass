package io.nicolaszurbuchen.tallgrass.infra.preview

object PreviewUiMode {
    // Android's Configuration.UI_MODE_NIGHT_NO and _NIGHT_YES, which commonMain cannot import: the
    // annotation is multiplatform and the constant it takes is not.
    const val NIGHT_NO = 0x10
    const val NIGHT_YES = 0x20
}
