package io.nicolaszurbuchen.tallgrass.core.location.domain.model

/**
 * What kind of place a location is, for the marker beside it in a region's list.
 *
 * **Upstream has no such field.** It is read out of the slug at generation time, which is the only
 * signal there is, and it is decoration rather than a fact: about one place in six lands on [OTHER],
 * and nothing depends on the answer being right.
 *
 * An unknown name falls to [OTHER] rather than to null, because there is no such thing here as a
 * place with no kind -- only one this has nothing to say about.
 */
enum class LocationCategory {
    ROUTE,
    TOWN,
    CAVE,
    FOREST,
    WATER,
    MOUNTAIN,
    BUILDING,
    PARK,
    OTHER,
    ;

    companion object {
        private val byName = entries.associateBy { it.name }

        fun fromName(name: String): LocationCategory = byName[name] ?: OTHER
    }
}
