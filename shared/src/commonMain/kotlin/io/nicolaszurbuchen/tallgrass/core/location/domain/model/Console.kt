package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * The machine a game was played on, which is how the availability grid groups its cells.
 *
 * Not the generation, on the author's reasoning in #9: if I play Diamond I may not know it is the
 * fourth generation, but I know I am holding a DS. Generation needed eight rows and scattered the
 * Switch games across three of them.
 *
 * Five rows, which is the five #9 named. There is no GameCube row: its only games are Colosseum and
 * XD, and this app covers the main series, so neither they nor the region that exists for them are in
 * the dataset.
 *
 * The declaration order is the order the rows are drawn, newest machine first.
 */
enum class Console {
    SWITCH,
    THREE_DS,
    DS,
    GBA,
    GB_GBC,
    ;

    companion object {
        private val byName = entries.associateBy { it.name }

        fun fromName(name: String): Console? = byName[name]
    }
}
