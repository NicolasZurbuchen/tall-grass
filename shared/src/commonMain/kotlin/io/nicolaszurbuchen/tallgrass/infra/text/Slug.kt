package io.nicolaszurbuchen.tallgrass.infra.text

/**
 * A dataset slug, read out loud.
 *
 * Hyphens out, first letter up, and **deliberately not title case**: title-casing turns
 * `hall-of-fame` into "Hall Of Fame", which reads as a proper noun upstream did not write. Sentence
 * case is the safer guess for a label nobody has looked at.
 *
 * This is the fallback for the places where a slug has no curated label -- twenty of the 64 encounter
 * methods, and every condition axis. Not a failure state: a slug upstream has added since a table was
 * written still reads, and `sky-battle` becoming "Sky battle" is a better answer than a blank.
 *
 * In `infra` because it knows nothing about Pokemon: it is string plumbing, and both `core` and a
 * feature use it.
 */
fun String.spellOutSlug(): String = replace('-', ' ').replaceFirstChar { it.uppercase() }
