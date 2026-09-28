package io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel

import androidx.compose.ui.graphics.Color

/**
 * A region's colour, which is the ground its card is drawn on and the tint its detail carries.
 *
 * **Upstream has no colour for a region**, the way it has none for a type either -- the eighteen type
 * colours are this app's too. So the eleven live here, beside the model rather than in `design/`, on
 * the same grounds as `TypeUiModel`: a region's colour is a fact about the region and not about
 * appearance in general, and `design/` may not depend on the domain.
 *
 * The values are the prototype's, which picked them to say something: Kanto takes Red's red, Johto
 * Gold's gold, Sinnoh the blue of Diamond's box. They are chosen to be told apart down a scrolling
 * list of eleven, so they are not derived from the box art's types -- doing that collides three
 * times over, giving Kanto and Johto the same orange and Kalos and Galar the same pink.
 */
enum class RegionThemeUiModel(
    val slug: String,
    val color: Color,
) {
    KANTO("kanto", Color(0xFFE8685D)),
    JOHTO("johto", Color(0xFFF0C33C)),
    HOENN("hoenn", Color(0xFF5BC98F)),
    SINNOH("sinnoh", Color(0xFF5BA7F7)),
    UNOVA("unova", Color(0xFF6E6059)),
    KALOS("kalos", Color(0xFFB368C9)),
    ALOLA("alola", Color(0xFFFF9D5C)),
    GALAR("galar", Color(0xFF7A5BC9)),
    HISUI("hisui", Color(0xFF9DB945)),
    PALDEA("paldea", Color(0xFFEE97CE)),
    ORRE("orre", Color(0xFF8FA0B5)),
    ;

    companion object {
        private val bySlug = entries.associateBy { it.slug }

        /**
         * Null when the dataset names a twelfth region this build has no colour for, which is the
         * day Game Freak ships one. A card with no colour is better than eleven cards and a
         * stranger, and the call site draws it in the neutral surface instead.
         */
        fun fromSlug(slug: String): RegionThemeUiModel? = bySlug[slug]
    }
}
