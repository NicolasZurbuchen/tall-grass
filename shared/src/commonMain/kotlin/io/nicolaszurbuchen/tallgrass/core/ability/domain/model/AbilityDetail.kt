package io.nicolaszurbuchen.tallgrass.core.ability.domain.model

/**
 * One ability, in full.
 *
 * Both halves of upstream's `effect_entries` and never `flavor_text_entries`: the effect entries are
 * PokeAPI's own prose under BSD, and flavour text is verbatim copyrighted game text. See #11.
 *
 * [shortEffect] is the line a card carries; [effect] is the paragraph behind it. **They are the same
 * string for 46 of the 314**, where upstream had nothing further to say — which is a fact about the
 * data the screen has to handle rather than one it can assume away.
 */
data class AbilityDetail(
    val slug: String,
    val name: String,
    val generation: Int,
    val shortEffect: String,
    val effect: String,
)
