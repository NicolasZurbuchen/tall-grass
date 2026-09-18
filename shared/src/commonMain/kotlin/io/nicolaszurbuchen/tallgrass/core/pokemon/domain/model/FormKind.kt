package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

/**
 * What kind of difference a form is.
 *
 * Derived once at generation from upstream's open `form` identifier, which has 164 values and cannot
 * be filtered on — `alola` and `rock-star` sit at the same level there. See DECISIONS.md § A form's
 * kind is derived and stored, not computed at read time.
 *
 * [COSMETIC] is the member with teeth: it is a form that differs from its base in nothing this app
 * models, and Pikachu alone has fourteen of them.
 */
enum class FormKind {
    NONE,
    REGIONAL,
    MEGA,
    GIGANTAMAX,
    BATTLE_ONLY,
    GENDER,
    TOTEM,
    ALTERNATE,
    COSMETIC,
    ;

    companion object {
        private val byName = entries.associateBy { it.name }

        /**
         * Falls back to [ALTERNATE] rather than to null, which is the opposite of how the other
         * enums here read a bad value.
         *
         * The difference is what being wrong costs. An unknown type or growth rate makes a row
         * unreadable, so the row goes. An unknown kind only means this build cannot say *how* a form
         * differs — the form itself is real, and hiding it would lose a Pokemon from the switcher
         * over a label.
         */
        fun fromName(name: String): FormKind = byName[name] ?: ALTERNATE
    }
}
