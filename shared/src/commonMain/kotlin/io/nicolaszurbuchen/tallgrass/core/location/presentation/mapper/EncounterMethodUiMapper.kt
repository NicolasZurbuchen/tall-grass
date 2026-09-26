package io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.EncounterMethodUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.spellOutSlug
import org.jetbrains.compose.resources.StringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.encounter_method_berry_trees
import tallgrass.shared.generated.resources.encounter_method_bubbling_spots
import tallgrass.shared.generated.resources.encounter_method_cave_spots
import tallgrass.shared.generated.resources.encounter_method_ceiling_ambush
import tallgrass.shared.generated.resources.encounter_method_dark_grass
import tallgrass.shared.generated.resources.encounter_method_dynamax_adventure
import tallgrass.shared.generated.resources.encounter_method_gift
import tallgrass.shared.generated.resources.encounter_method_gift_egg
import tallgrass.shared.generated.resources.encounter_method_good_rod
import tallgrass.shared.generated.resources.encounter_method_grass_spots
import tallgrass.shared.generated.resources.encounter_method_headbutt
import tallgrass.shared.generated.resources.encounter_method_headbutt_high
import tallgrass.shared.generated.resources.encounter_method_headbutt_low
import tallgrass.shared.generated.resources.encounter_method_headbutt_normal
import tallgrass.shared.generated.resources.encounter_method_hidden_grotto
import tallgrass.shared.generated.resources.encounter_method_honey_tree
import tallgrass.shared.generated.resources.encounter_method_horde
import tallgrass.shared.generated.resources.encounter_method_island_scan
import tallgrass.shared.generated.resources.encounter_method_max_raid
import tallgrass.shared.generated.resources.encounter_method_npc_trade
import tallgrass.shared.generated.resources.encounter_method_old_rod
import tallgrass.shared.generated.resources.encounter_method_overworld
import tallgrass.shared.generated.resources.encounter_method_overworld_dirt
import tallgrass.shared.generated.resources.encounter_method_overworld_flying
import tallgrass.shared.generated.resources.encounter_method_overworld_flying_special
import tallgrass.shared.generated.resources.encounter_method_overworld_special
import tallgrass.shared.generated.resources.encounter_method_overworld_water
import tallgrass.shared.generated.resources.encounter_method_purple_flowers
import tallgrass.shared.generated.resources.encounter_method_red_flowers
import tallgrass.shared.generated.resources.encounter_method_roaming_grass
import tallgrass.shared.generated.resources.encounter_method_rock_smash
import tallgrass.shared.generated.resources.encounter_method_rough_terrain
import tallgrass.shared.generated.resources.encounter_method_snag
import tallgrass.shared.generated.resources.encounter_method_sos
import tallgrass.shared.generated.resources.encounter_method_sos_from_bubbling_spot
import tallgrass.shared.generated.resources.encounter_method_static
import tallgrass.shared.generated.resources.encounter_method_super_rod
import tallgrass.shared.generated.resources.encounter_method_super_rod_spots
import tallgrass.shared.generated.resources.encounter_method_surf
import tallgrass.shared.generated.resources.encounter_method_surf_spots
import tallgrass.shared.generated.resources.encounter_method_walk
import tallgrass.shared.generated.resources.encounter_method_wanderer
import tallgrass.shared.generated.resources.encounter_method_wanderer_water
import tallgrass.shared.generated.resources.encounter_method_yellow_flowers

/**
 * The label a method row or tab carries.
 *
 * Curated for the 44 where title-casing the slug reads wrong, and opened out of the slug for the
 * other twenty of the 64 in this dataset. "Tall grass" rather than "Walk" is the clearest of the
 * curated ones and #9 called it out: `walk` was wrong twice over, at the wrong granularity and less
 * evocative than what the games call the place it happens in.
 *
 * The fallback is not a failure state. An unrecognised method is one upstream has added since this
 * table was written, and a slug opened out still reads: `sky-battle` becomes "Sky battle".
 */
fun String.toEncounterMethodUiModel(): EncounterMethodUiModel =
    EncounterMethodUiModel(
        slug = this,
        label = CURATED[this]?.let { UiText.Resource(it) } ?: UiText.Raw(spellOutSlug()),
    )

private val CURATED: Map<String, StringResource> =
    mapOf(
        "walk" to Res.string.encounter_method_walk,
        "dark-grass" to Res.string.encounter_method_dark_grass,
        "grass-spots" to Res.string.encounter_method_grass_spots,
        "overworld" to Res.string.encounter_method_overworld,
        "overworld-water" to Res.string.encounter_method_overworld_water,
        "overworld-flying" to Res.string.encounter_method_overworld_flying,
        "overworld-dirt" to Res.string.encounter_method_overworld_dirt,
        "overworld-special" to Res.string.encounter_method_overworld_special,
        "overworld-flying-special" to Res.string.encounter_method_overworld_flying_special,
        "wanderer" to Res.string.encounter_method_wanderer,
        "wanderer-water" to Res.string.encounter_method_wanderer_water,
        "roaming-grass" to Res.string.encounter_method_roaming_grass,
        "surf" to Res.string.encounter_method_surf,
        "surf-spots" to Res.string.encounter_method_surf_spots,
        "old-rod" to Res.string.encounter_method_old_rod,
        "good-rod" to Res.string.encounter_method_good_rod,
        "super-rod" to Res.string.encounter_method_super_rod,
        "super-rod-spots" to Res.string.encounter_method_super_rod_spots,
        "bubbling-spots" to Res.string.encounter_method_bubbling_spots,
        "cave-spots" to Res.string.encounter_method_cave_spots,
        "headbutt" to Res.string.encounter_method_headbutt,
        "headbutt-low" to Res.string.encounter_method_headbutt_low,
        "headbutt-normal" to Res.string.encounter_method_headbutt_normal,
        "headbutt-high" to Res.string.encounter_method_headbutt_high,
        "rock-smash" to Res.string.encounter_method_rock_smash,
        "honey-tree" to Res.string.encounter_method_honey_tree,
        "berry-trees" to Res.string.encounter_method_berry_trees,
        "max-raid" to Res.string.encounter_method_max_raid,
        "dynamax-adventure" to Res.string.encounter_method_dynamax_adventure,
        "horde" to Res.string.encounter_method_horde,
        "sos" to Res.string.encounter_method_sos,
        "sos-from-bubbling-spot" to Res.string.encounter_method_sos_from_bubbling_spot,
        "static" to Res.string.encounter_method_static,
        "gift" to Res.string.encounter_method_gift,
        "gift-egg" to Res.string.encounter_method_gift_egg,
        "npc-trade" to Res.string.encounter_method_npc_trade,
        "island-scan" to Res.string.encounter_method_island_scan,
        "hidden-grotto" to Res.string.encounter_method_hidden_grotto,
        "ceiling-ambush" to Res.string.encounter_method_ceiling_ambush,
        "rough-terrain" to Res.string.encounter_method_rough_terrain,
        "snag" to Res.string.encounter_method_snag,
        "red-flowers" to Res.string.encounter_method_red_flowers,
        "yellow-flowers" to Res.string.encounter_method_yellow_flowers,
        "purple-flowers" to Res.string.encounter_method_purple_flowers,
    )
