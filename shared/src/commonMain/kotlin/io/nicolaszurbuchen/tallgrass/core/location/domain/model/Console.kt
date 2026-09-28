package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * The machine a game was played on, which is how the availability grid groups its cells.
 *
 * Not the generation, on the author's reasoning in #9: if I play Diamond I may not know it is the
 * fourth generation, but I know I am holding a DS. Generation needed eight rows and scattered the
 * Switch games across three of them.
 *
 * [GAME_CUBE] is not one of the five rows #9 named and exists because Orre does: Colosseum and XD are
 * that region's only games. A row is drawn only where a region has games in it, so it appears on Orre
 * and nowhere else -- the same rule that gives Kanto no 3DS row.
 *
 * The declaration order is the order the rows are drawn, newest machine first.
 */
enum class Console {
    SWITCH,
    THREE_DS,
    DS,
    GAME_CUBE,
    GBA,
    GB_GBC,
    ;

    companion object {
        private val byName = entries.associateBy { it.name }

        fun fromName(name: String): Console? = byName[name]
    }
}
