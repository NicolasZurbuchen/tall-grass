# What changes at each generation boundary?

Research note for GitHub issue #89 (`NicolasZurbuchen/tall-grass`), part of the
game-lens map (#87).

- **Date of investigation:** 2026-10-11
- **Method:** three independent references, diffed against each other by script:
  - **Bulbapedia** raw wikitext (`index.php?action=raw`, the MediaWiki API)
  - **Pokémon Showdown** data at commit
    [`942cda7`](https://github.com/smogon/pokemon-showdown/tree/942cda71d14bf0618bd57991708a2e2734b6daba)
    (2026-10-10). Its `data/mods/genN/` files hold only what *differs* from gen N+1, so each
    one is a ready-made boundary diff.
  - **Serebii**, for spot checks
  - **PokéAPI** CSVs at the pinned SHA
    [`4b82c20`](https://github.com/PokeAPI/pokeapi/tree/4b82c204ddd19ecb8eda2ea044ccb59e222b721c/data/v2/csv),
    plus two live API reads to confirm field semantics
- **Status:** answered. Every drifted mechanic is listed below with its boundary and size.
  PokéAPI's coverage was measured against the references: complete for types, the type
  chart and base stats, mostly complete for abilities and move values up to Gen 8, and
  absent for every side-mechanic game.

---

## TL;DR

1. **In the mainline games, the drift is small and PokéAPI holds most of it.** The
   mainline games have:
   - 3 retyping events (29 Pokémon/forms)
   - 2 type-chart revisions
   - 59 species whose base stats changed
   - about 150 changes to which abilities a species has
   - 168 moves with changed numbers in PokéAPI's changelog
   - PokéAPI agrees with Showdown on **every** base stat, type and type-chart value.
   - For move power, accuracy, PP and type it disagrees on only 76 cells across Gen 1–8:
     - 50 are Gen 9 changes PokéAPI never recorded, so it shows the new value in old games
     - 22 are representation noise
     - 4 are **two real PokéAPI errors** (§7.4)
2. **Generation-keyed data cannot tell a lens which *game* it is in.**
   - `past_stats`, `past_types`, `past_abilities` and `past_damage_relations` are keyed by
     generation. To them, Let's Go is Sun/Moon, Legends: Arceus is Sword/Shield, and
     Legends: Z-A and Champions are Scarlet/Violet.
   - `move.past_values` is keyed by **version group**, not generation, and its
     `version_group` means "the values below were replaced *in* this group". The docs say
     the opposite (§10.1).
3. **The side-mechanic games are where curation lives.** PokéAPI has no game-specific
   values for any of them.
   - **Legends: Arceus** changes 138 of its 177 moves and has no abilities.
   - **Legends: Z-A** has no PP, no accuracy and no abilities. It changes 33 of 340 move
     powers and raises 5 Pokémon's Attack.
   - **Champions** re-scales every move's PP to 8/12/16/20 and retunes about 30 moves.
   - **Let's Go** has no abilities.
4. **Gen 9 itself is the other gap.**
   - PokéAPI is missing the Gen 8→9 healing-move PP halving (7 moves).
   - It is also missing 4 of the 5 Gen 8→9 power changes to Sword/Shield-era moves.
   - It has no move-category history at all. The type-based rule before Gen 4 can be
     derived; the 4 per-move category changes after Gen 4 cannot.
5. **Several About-tab fields drift too**, and PokéAPI has no history for them: EV yield,
   base friendship, base experience, and English move names. Its *current* base
   friendship is stale for 667 species.

---

## 1. How to read this note

- **Boundary.** "Gen 5→6" means *the value changed when Gen 6 came out*. Under a Gen 1–5
  lens the old value applies; under Gen 6+ the new one does. A *within-generation*
  change is labelled with the two games it separates (e.g. "SwSh→LA").
- **Size.** The number of species, forms or moves affected. Counts come from Bulbapedia's
  change tables. Where Showdown or PokéAPI give a different number, both are shown.
- **The lens unit is a version group with its DLC folded in** (#87, settled point 3). Every
  version group in the pinned `version_groups.csv` maps to one mechanics regime:

| Regime | Version groups (PokéAPI identifiers) | Notes |
|---|---|---|
| Gen 1 | `red-green-japan`, `blue-japan`, `red-blue`, `yellow` | No abilities, five stats, Gen 1 chart |
| Gen 2 | `gold-silver`, `crystal` | No abilities, six stats, Gen 2–5 chart |
| Gen 3 | `ruby-sapphire`, `emerald`, `firered-leafgreen`, `colosseum`, `xd` | Abilities start; type-based category |
| Gen 4 | `diamond-pearl`, `platinum`, `heartgold-soulsilver` | Per-move category starts |
| Gen 5 | `black-white`, `black-2-white-2` | Hidden Abilities start |
| Gen 6 | `x-y`, `omega-ruby-alpha-sapphire` | Fairy; Gen 6+ chart |
| Gen 7 | `sun-moon`, `ultra-sun-ultra-moon` | |
| Gen 7 side | `lets-go-pikachu-lets-go-eevee` | **No abilities**, no held items; 4 move values differ |
| Gen 8 | `sword-shield` (+ `the-isle-of-armor`, `the-crown-tundra`), `brilliant-diamond-shining-pearl` | |
| Gen 8 side | `legends-arceus` | **No abilities**, no held items; own move table; own dual-type multipliers |
| Gen 9 | `scarlet-violet` (+ `the-teal-mask`, `the-indigo-disk`) | |
| Gen 9 side | `legends-za` (+ `mega-dimension`) | **No abilities**, no PP, no accuracy; own move powers |
| Gen 9 side | `champions` | Abilities **present**; own PP scale; ~30 retuned moves |

Sources: PokéAPI [`version_groups.csv`](https://raw.githubusercontent.com/PokeAPI/pokeapi/4b82c204ddd19ecb8eda2ea044ccb59e222b721c/data/v2/csv/version_groups.csv);
regime notes are sourced in §2–§8.

---

## 2. The boundaries at a glance

| Boundary | What changes | Size | PokéAPI | Curation |
|---|---|---|---|---|
| **Gen 1→2** | Special splits into Sp. Atk/Sp. Def | 151 species | `past_stats` ✓ | — |
| | Steel and Dark types introduced | 2 types | `types.generation_id` ✓ | — |
| | Retyping: Magnemite, Magneton gain Steel | 2 | `past_types` ✓ | — |
| | Type chart: 4 cells change | 4 | `past_damage_relations` ✓ | — |
| | Moves: power 5, accuracy 4, PP 1, type 4 | ~13 moves | `past_values` ✓ (except the Gen 1 Struggle PP) | trivial |
| | Held items, gender, breeding/egg groups introduced | game-wide | implicit | rule |
| **Gen 2→3** | **Abilities introduced** (absent in Gen 1–2) | game-wide | implicit (`abilities.generation_id`) | rule |
| | Natures, the modern EV system and per-species base friendship introduced | game-wide | — | rule |
| | Moves: power 1, accuracy 4 | 5 | ✓ (2 of the accuracy rows are status "—" noise) | — |
| **Gen 3→4** | **Physical/special split**: category goes per move | 54 moves flip | rule derivable (`types.damage_class_id`) | rule |
| | Species gain a second ability | 89 species | `past_abilities` ✓ (89) | — |
| | EV yield changes | 6 species (boundary per PokéAPI; Bulbapedia's category lists 17 without boundaries, §9) | `past_stats.effort` | see §9 |
| | Moves: power 10, accuracy 11, PP 6 | ~25 | ✓ | — |
| **Gen 4→5** | **Hidden Abilities introduced** | 420 species/forms get a hidden-ability slot (PokéAPI) | `past_abilities` ✓ | — |
| | Retyping: Rotom's 5 appliance forms lose Ghost | 5 forms | `past_types` ✓ | — |
| | Moves: power 20, accuracy 19, PP 8, type 1 (Curse ???→Ghost) | ~40 | ✓ | — |
| **Gen 5→6** | **Fairy type**; 22 Pokémon retyped | 22 species | `past_types` ✓ | — |
| | Type chart: Steel loses its Ghost and Dark resistances | 2 cells | ✓ | — |
| | Base stat buffs | 29 species | `past_stats` ✓ | — |
| | Abilities: 7 changed, 16 added | 23 | ✓ (24 rows, incl. 1 no-op) | — |
| | Moves: power 50, accuracy 11, PP 16, type 3 | ~70 | ✓ | — |
| **Gen 6→7** | Base stat buffs | 25 species + Mega Alakazam | ✓ | — |
| | Abilities: 4 changed, 11 added | 15 | ✓ (15) | — |
| | Moves: power 7, accuracy 4, PP 1; Water Shuriken becomes Special | ~12 | ✓ except category | 1 category |
| **SM/USUM→LGPE** | No abilities, no held items, no breeding; 4 moves buffed | game-wide | 4 moves ✓ (bracketed rows) | abilities flag |
| **Gen 7→8** | Aegislash's stats nerfed (2 formes) | 1 species | ✓ | — |
| | Moves: power 6, PP 2 (incl. reverting the LGPE buffs) | 8 | ✓ | — |
| **SwSh→LA** | No abilities or held items; agile/strong styles; dual-type 2.5×/0.4× | 177 moves, 138 differ | **none** | **full move table** |
| **Gen 8→9** | Stat nerfs: Cresselia, Zacian, Zamazenta | 3 species (5 forms) | ✓ | — |
| | Abilities: 16 changed, 6 added | 22 | 19 rows | reconcile |
| | **Healing moves' PP 10→5** | 7 moves | **missing** | **yes** |
| | Power: Wicked Blow, Glacial Lance, Grassy Glide, Luster Purge (Mist Ball is covered) | 4 | **missing** | **yes** |
| | LA-introduced moves re-tuned for SV | 12 (power), 8 (PP), 2 (accuracy) | n/a (holds SV values) | LA only |
| **SV→Z-A** | No abilities, no PP (cooldowns), no accuracy; 33 powers; Growth→Grass | 340 moves | **none** | **full move table** |
| | Attack +40% on 5 Pokémon/forms instead of Huge/Pure Power | 5 | **none** | **yes** |
| **SV→Champions** | PP re-scaled to 8/12/16/20; ~30 moves retuned; 3 abilities changed | every move | **none** | **yes** |

Every row is sourced in the sections that follow.

---

## 3. Stat layout and base stats

### 3.1 The five-stat block (Gen 1)

Gen 1 has one **Special** stat. At Gen 2 it split into Special Attack and Special Defense.
Bulbapedia's categories count the 151 Gen 1 species by what Special became:

| What Special became in Gen 2 | Species |
|---|---|
| their Special Attack | 68 |
| their Special Defense | 43 |
| both | 40 |

Sources: Bulbapedia categories
[Special became Sp. Atk](https://bulbapedia.bulbagarden.net/wiki/Category:Pok%C3%A9mon_whose_Special_stat_became_their_Special_Attack),
[… Sp. Def](https://bulbapedia.bulbagarden.net/wiki/Category:Pok%C3%A9mon_whose_Special_stat_became_their_Special_Defense),
[… both](https://bulbapedia.bulbagarden.net/wiki/Category:Pok%C3%A9mon_whose_Special_stat_became_both_Special_Attack_and_Special_Defense).
Showdown's [`gen1/pokedex.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen1/pokedex.ts)
overrides `baseStats` for all 151 (plus MissingNo.) with `spa == spd == Special`.

**Implication for the screen.** Under a Gen 1 lens the stat block has five rows, labelled
"Special", and the stat total is a different number. Showdown's display table even names
the slot "Special" for gen 1
([`sim/dex-data.ts` L382](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/sim/dex-data.ts#L382)).

**PokéAPI:** `pokemon_stats_past.csv` has 151 rows `(generation 1, stat 9 "special")`. The
other four stats did not change, so a Gen 1 lens is: current HP/Atk/Def/Spe plus
`special`, with no Sp. Atk/Sp. Def. Complete.

### 3.2 Base stat changes

Base stats changed at four boundaries only: Gen 6, Gen 7, Gen 8 and Gen 9. None changed
between Gen 2 and Gen 5.

| Boundary | Species | Bulbapedia | Showdown mod | PokéAPI rows (keyed to the *last old* generation) |
|---|---|---|---|---|
| Gen 5→6 (buffs) | Butterfree, Beedrill, Pidgeot, Pikachu, Raichu, Nidoqueen, Nidoking, Clefable, Wigglytuff, Vileplume, Poliwrath, Alakazam, Victreebel, Golem, Ampharos, Bellossom, Azumarill, Jumpluff, Beautifly, Exploud, Staraptor, Roserade, Stoutland, Unfezant, Gigalith, Seismitoad, Leavanny, Scolipede, Krookodile | 29 | 29 | 29 species (gen 5) |
| Gen 6→7 (buffs) | Arbok, Dugtrio, Farfetch'd, Dodrio, Electrode, Exeggutor, Noctowl, Ariados, Qwilfish, Magcargo, Corsola, Mantine, Swellow, Pelipper, Masquerain, Delcatty, Volbeat, Illumise, Lunatone, Solrock, Chimecho, Woobat, Crustle, Beartic, Cryogonal, **Mega Alakazam** | 26 | 26 (+2 CAP) | 25 species + `alakazam-mega` (gen 6) |
| Gen 7→8 (nerf) | Aegislash (Shield and Blade) | 1 | 2 formes | `aegislash-shield`, `aegislash-blade` (gen 7) |
| Gen 8→9 (nerfs) | Cresselia, Zacian (Hero, Crowned), Zamazenta (Hero, Crowned) | 3 | 5 forms | 5 forms (gen 8) |

Sources: Bulbapedia categories for
[Gen VI](https://bulbapedia.bulbagarden.net/wiki/Category:Pok%C3%A9mon_whose_base_stats_changed_in_Generation_VI),
[Gen VII](https://bulbapedia.bulbagarden.net/wiki/Category:Pok%C3%A9mon_whose_base_stats_changed_in_Generation_VII),
[Gen VIII](https://bulbapedia.bulbagarden.net/wiki/Category:Pok%C3%A9mon_whose_base_stats_changed_in_Generation_VIII) and
[Gen IX](https://bulbapedia.bulbagarden.net/wiki/Category:Pok%C3%A9mon_whose_base_stats_changed_in_Generation_IX)
(29/26/1/3 members). For Showdown, the `baseStats` overrides in
[`gen5`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen5/pokedex.ts),
`gen6`, `gen7` and `gen8` `pokedex.ts`. PokéAPI rows are from
[`pokemon_stats_past.csv`](https://raw.githubusercontent.com/PokeAPI/pokeapi/4b82c204ddd19ecb8eda2ea044ccb59e222b721c/data/v2/csv/pokemon_stats_past.csv).

The three references agree species-for-species. **No curation is needed for mainline
base stats.**

### 3.3 Base stats that differ inside a side game

| Game | What differs | Size | Source | PokéAPI |
|---|---|---|---|---|
| Legends: Z-A | Base Attack **+40%** on Mega Starmie, Mega Mawile, Meditite, Medicham and Mega Medicham, to stand in for the missing Huge Power/Pure Power | 5 | Bulbapedia [Ability § Generation IX](https://bulbapedia.bulbagarden.net/wiki/Ability) and [Pokémon battle (Legends: Z-A) § Abilities](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_battle_(Legends:_Z-A)) | **No.** Z-A is generation 9, the same key as SV. PokéAPI's Mega Starmie Attack is 100 |
| Legends: Arceus | Cherrim-Sunshine keeps Flower Gift's boost although abilities are absent | 1 form | Bulbapedia [Ability § Generation VIII](https://bulbapedia.bulbagarden.net/wiki/Ability). Showdown's [`gen8legends/pokedex.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen8legends/pokedex.ts) bakes it in as Atk 90 / Sp. Def 117 | No |
| Let's Go | Partner Pikachu and Partner Eevee have higher base stats | 2 forms | Bulbapedia [LGPE page](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon:_Let%27s_Go,_Pikachu!_and_Let%27s_Go,_Eevee!) | Yes, as separate forms `pikachu-starter`, `eevee-starter` |

**Showdown is incomplete here.** Its
[`gen9legends/pokedex.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen9legends/pokedex.ts)
raises only the three Megas. These are 140/147/140 Attack, i.e. ×1.4 of 100/105/100. It
does not raise non-Mega Meditite and Medicham, which Bulbapedia lists. **Unverified:**
whether Meditite and Medicham are really boosted in-game. Bulbapedia says so, Showdown
does not model it, and Serebii was not checked.

---

## 4. Types and the type chart

### 4.1 Types that exist

Steel and Dark arrive in Gen 2 and Fairy in Gen 6. Stellar (Gen 9) is Tera-only. PokéAPI
models this with `types.generation_id`
([`types.csv`](https://raw.githubusercontent.com/PokeAPI/pokeapi/4b82c204ddd19ecb8eda2ea044ccb59e222b721c/data/v2/csv/types.csv)).
The pseudo-type `unknown` (`???`, id 10001) is the type of Curse in Gen 2–4.

### 4.2 Pokémon retypings

| Boundary | Pokémon | Old → new | Size |
|---|---|---|---|
| Gen 1→2 | Magnemite, Magneton | Electric → Electric/Steel | 2 |
| Gen 4→5 | Rotom Heat/Wash/Frost/Fan/Mow | Electric/Ghost → Electric/(appliance type) | 5 forms |
| Gen 5→6 | Clefairy, Clefable, Cleffa, Togepi, Snubbull, Granbull (Normal → Fairy); Jigglypuff, Wigglytuff, Igglybuff (Normal → Normal/Fairy); Togetic, Togekiss (Normal/Flying → Fairy/Flying); Mr. Mime, Mime Jr., Ralts, Kirlia, Gardevoir (Psychic → Psychic/Fairy); Marill, Azumarill, Azurill (Water / Normal → Water/Fairy, Normal/Fairy); Mawile (Steel → Steel/Fairy); Cottonee, Whimsicott (Grass → Grass/Fairy) | 22 |

Sources:
- Bulbapedia [Fairy (type) § Changed types](https://bulbapedia.bulbagarden.net/wiki/Fairy_(type)):
  "22 Pokémon and three moves were changed to have the Fairy type".
- Bulbapedia [Steel (type) § Changed types](https://bulbapedia.bulbagarden.net/wiki/Steel_(type)) for Magnemite/Magneton.
- Bulbapedia [Rotom](https://bulbapedia.bulbagarden.net/wiki/Rotom_(Pok%C3%A9mon)): "In Generation IV,
  all forms of Rotom are Electric/Ghost-type".
- Showdown `types` overrides in
  [`gen1`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen1/pokedex.ts) (2),
  [`gen4`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen4/pokedex.ts) (5) and
  [`gen5`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen5/pokedex.ts) (22).

**PokéAPI `pokemon_types_past.csv` has exactly these 29 Pokémon/forms**: 2 in gen 1, 5 in
gen 4 and 22 in gen 5. **Complete.** No side game retypes a species. (The Mega forms that
debut in Z-A are new forms, not retypings.)

### 4.3 Type chart per generation

| Chart | Applies to | Differences from the next chart |
|---|---|---|
| Gen 1 | RG/B/Y | No Steel or Dark. **Bug→Poison 2×** (later ½×). **Poison→Bug 2×** (later 1×). **Ghost→Psychic 0×** (later 2×; the famous bug). **Ice→Fire 1×** (later ½×). |
| Gen 2–5 | GS through B2W2 | **Ghost→Steel ½×, Dark→Steel ½×** (later 1×). No Fairy. |
| Gen 6+ | XY onward, including LGPE, LA, Z-A and Champions | current chart |

Sources: Bulbapedia [Type/Type chart § Changes](https://bulbapedia.bulbagarden.net/wiki/Type/Type_chart).
Showdown's [`gen1/typechart.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen1/typechart.ts)
(its header comment lists the same four differences),
[`gen2/typechart.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen2/typechart.ts)
and [`gen5/typechart.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen5/typechart.ts).
Showdown's gen 5/6 charts also carry non-chart immunities (powder, paralysis, trapping,
Prankster), which are battle rules and not matchup cells.

**PokéAPI `type_efficacy_past.csv` has exactly 6 rows**: the 4 Gen 1 cells and the 2 Gen 5
cells. Together with `types.generation_id`, **that is the whole history. Complete.**

**One side game changes the arithmetic, not the chart.** In Legends: Arceus a doubly
super-effective hit is **2.5×** (not 4×) and a doubly resisted hit is **0.4×** (not ¼×).
Source: Bulbapedia [Type § Type effectiveness](https://bulbapedia.bulbagarden.net/wiki/Type).
A matchup screen that shows "4×" under an LA lens is wrong. PokéAPI has no field for
this, so it is one rule per version group. (Champions relabels 4× and ¼× as "extremely
effective" and "mostly ineffective". That is wording only:
[Pokémon Champions](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Champions).)

---

## 5. Abilities

### 5.1 When there are no abilities at all

| Games | Abilities? | Source |
|---|---|---|
| Gen 1–2 (RG/B/Y, GS/C) | **No.** Abilities were introduced in Gen 3 | Bulbapedia [Ability](https://bulbapedia.bulbagarden.net/wiki/Ability): "a game mechanic introduced in Generation III" |
| Let's Go | **No.** They exist in the game's code, unused | [Ability](https://bulbapedia.bulbagarden.net/wiki/Ability): "Abilities are functionally absent in LGPE, Legends: Arceus, and Legends: Z-A"; [LGPE page](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon:_Let%27s_Go,_Pikachu!_and_Let%27s_Go,_Eevee!) |
| Legends: Arceus | **No.** Abilities, held items, breeding and Eggs were all removed | [Pokémon Legends: Arceus](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Legends:_Arceus) |
| Legends: Z-A | **No.** But a few Pokémon get hard-coded passives modelled on abilities (Stance Change, Disguise, Hunger Switch, Power Construct, Commander, Justified via Sacred Sword, …) | [Pokémon Legends: Z-A](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Legends:_Z-A) ("Abilities, breeding, and Eggs are not featured"); [Pokémon battle (Legends: Z-A) § Abilities](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_battle_(Legends:_Z-A)) |
| Champions | **Yes.** 7 new abilities for the Z-A Megas; 3 existing abilities changed (Healer 30%→50%, Unseen Fist, Run Away) | [Pokémon Champions](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Champions); Showdown [`champions/abilities.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/champions/abilities.ts) |

For Z-A's passives the honest screen is probably "no abilities section". The passives are
per-species battle quirks, not abilities. That is a product call for the decision ticket.

### 5.2 Hidden Abilities and changes to which abilities a species has

| Boundary | Change | Bulbapedia | PokéAPI `pokemon_abilities_past` |
|---|---|---|---|
| Gen 3→4 | 89 species gain a second ability | 89 | 89 rows `(gen 3, slot 2, ability null)` ✓ |
| Gen 4→5 | Hidden Ability slot introduced | (game-wide) | 420 rows `(gen 4, slot 3 hidden, null)` ✓ |
| within Gen 5 (BW→B2W2) | Blue-Striped Basculin: Reckless → Rock Head | 1 | 1 row, but **a no-op**: its old value (Rock Head) equals the current value, and generation keys cannot split BW from B2W2 |
| Gen 5→6 | 7 changed (mostly Hidden Abilities), 16 added | 23 | 24 rows (gen 5): these 23 plus the Basculin no-op ✓ |
| Gen 6→7 | 4 changed, 11 added | 15 | 15 rows (gen 6) ✓ |
| Gen 7→8 | 2 added | 2 | 2 rows (gen 7) ✓ |
| Gen 8→9 | 16 changed, 6 added | 22 | 19 rows (gen 8) ~ |

Sources: Bulbapedia [List of modified Ability access](https://bulbapedia.bulbagarden.net/wiki/List_of_modified_Ability_access)
(rows counted per section) and
[`pokemon_abilities_past.csv`](https://raw.githubusercontent.com/PokeAPI/pokeapi/4b82c204ddd19ecb8eda2ea044ccb59e222b721c/data/v2/csv/pokemon_abilities_past.csv)
(569 rows). Per the PokéAPI docs, a null ability means "the slot was previously empty".

**Two caveats for Gen 8→9:**
- **Most of PokéAPI's Gen 8 rows describe no real game.** 14 of the 19 are Hisuian forms,
  Kleavor, Basculegion, Sneasler and Enamorus. Their "Gen 8" abilities come from Legends: Arceus,
  which has no abilities, and none of them exist in SwSh or BDSP. Under the lens model
  those rows can never render. They are harmless, but nothing should treat them as
  meaningful.
- **Some changes fell inside Gen 9.** Shiftry's Wind Rider and the Piplup line's
  Competitive arrived with the SV DLC era. Showdown's
  [`gen9predlc/pokedex.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen9predlc/pokedex.ts)
  keeps the pre-DLC abilities. Because the lens folds DLC into SV, showing the post-DLC
  value is correct for an SV lens. The difference only matters for a pre-DLC SV view,
  which the lens model does not have.

I did not reconcile Bulbapedia's 22 Gen 8→9 entries against PokéAPI's 19 rows one by one.
The visible gap is edge cases: Kubfu's never-distributed No Guard, and Greninja, Zygarde
and Rockruff's special-ability forms. **Treat Gen 8→9 ability access as "verify on
build"**.

### 5.3 Ability effects

PokéAPI's
[`ability_changelog.csv`](https://raw.githubusercontent.com/PokeAPI/pokeapi/4b82c204ddd19ecb8eda2ea044ccb59e222b721c/data/v2/csv/ability_changelog.csv)
holds 52 prose entries keyed by version group, all at or before Black/White (version
group ids 6, 8, 9, 11). There is nothing for Gen 6+, and nothing for the 3 Champions
changes. Ability flavour text per version group exists separately and covers the
description line. An ability *detail* screen showing mechanics prose under an old lens
would need curation past Gen 5. That is low priority for a reference app.

---

## 6. The physical/special split

**Before Gen 4, a damaging move's category was decided by its type:**
- **Special:** Fire, Water, Grass, Ice, Electric, Psychic, Dragon, Dark
- **Physical:** everything else

**From Gen 4, each move has its own category.**

Sources:
- Bulbapedia [Type § Type-affected game mechanics](https://bulbapedia.bulbagarden.net/wiki/Type):
  "all Normal damaging moves are physical moves and all Water damaging moves are special
  moves".
- Showdown's [`gen3/scripts.ts` L5–12](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen3/scripts.ts#L5-L12)
  rewrites every non-status move's category from that same 8-type list at load.

**Size.** Bulbapedia's [List of modified moves § Move category changes](https://bulbapedia.bulbagarden.net/wiki/List_of_modified_moves)
lists **54 moves** whose category flipped at Gen 3→4. Recomputing from Showdown (moves #1–354
with the rule applied to their Gen 3 type) gives 62. That is the same 54 once Showdown's
9 per-type Hidden Power entries are collapsed into 1. Examples: Shadow Ball
physical→special, Crunch special→physical, all elemental punches special→physical, Hyper
Beam physical→special. Weather Ball and Hidden Power show "varies" before Gen 4.

**Category changes after Gen 4 are rare but real:**

| Boundary | Move | Change |
|---|---|---|
| Gen 1→2 | Bite | Physical → Special (a side effect of becoming Dark) |
| Gen 6→7 | Water Shuriken | Physical → Special |
| BDSP→LA, then reverted at Gen 9 | Double Hit, Spikes, Stealth Rock | In LA, Spikes and Stealth Rock deal damage (40 power); Double Hit has no power |

Sources: same Bulbapedia section. Spikes and Stealth Rock at 40 power are confirmed by
Serebii's [Legends: Arceus attack list](https://www.serebii.net/legendsarceus/attacks.shtml).

**PokéAPI.**
- `types.csv` carries a `damage_class_id` per type: 2 physical, 3 special, empty for types
  newer than Gen 3. **The pre-Gen 4 rule is derivable.** Combine it with the move's type
  under the lens (from `past_values`) and Bite comes out right in every generation.
- There is **no per-move category history**. `move_changelog.csv` has no damage-class
  column, and the API's `PastMoveStatValues` has no `damage_class`
  ([docs](https://pokeapi.co/docs/v2#pastmovestatvalues)). So Water Shuriken is Special in
  every generation, and LA's three oddities are absent. That is 1 mainline curation row,
  plus LA's table (§8.2).

---

## 7. Move values: power, accuracy, PP, type

### 7.1 Size per boundary (Bulbapedia)

Counts of moves per change table on Bulbapedia's
[List of modified moves](https://bulbapedia.bulbagarden.net/wiki/List_of_modified_moves)
(raw wikitext, HTML comments stripped, rows counted per section). The page carries an
`{{incomplete}}` banner for "missing Generation VIII changes".

| Boundary | Power | Accuracy | PP | Type | Priority |
|---|---|---|---|---|---|
| Gen 1→2 | 5 | 4 | 1 | 4 | 2 |
| Gen 2→3 | 1 | 4 | 0 | 0 | 5 |
| Gen 3→4 | 10 | 11 | 6 | 0 | 1 |
| Gen 4→5 | 20 | 19 | 8 | 1 | 5 |
| Gen 5→6 | 50 | 11 | 16 | 3 | 4 |
| Gen 6→7 | 7 | 4 | 1 | 0 | 1 |
| Gen 7→8 | 6 | 0 | 2 | 0 | 0 |
| Gen 8→9 | 17 | 2 | 15 | 1 | 0 |
| within Gen 3 (Colosseum→XD) | 1 | — | — | — | — |
| within Gen 4 (DP→Pt) | — | 1 | — | — | — |
| within Gen 7 (USUM→LGPE) | 4 | — | 2 | — | 1 |
| within Gen 9 (SV patch / Z-A / Champions) | 1 / 4 / 12 | — / — / 2 | — | — / 1 (Growth, Z-A) / — | — |

Notable members:
- **Gen 1→2.** Bite, Gust, Karate Chop and Sand Attack become Dark, Flying, Fighting and
  Ground (all Normal in Gen 1). Dig 100→60 and Double-Edge 100→120. Explosion 170→250 and
  Self-Destruct 130→200. Wing Attack 35→60. Blizzard's accuracy drops 90→70%. Struggle's
  PP 10→1.
- **Gen 4→5.** Curse ??? → Ghost.
- **Gen 5→6.** Charm, Moonlight and Sweet Kiss become Fairy. The big ~95→90 / 120→110 nerf
  pass hits Flamethrower, Surf, Ice Beam, Thunderbolt, Fire Blast, Hydro Pump, Blizzard,
  Thunder and Hurricane. Knock Off goes 20→65.
- **Gen 6→7.** Tackle 50→40, Leech Life 20→80, Sucker Punch 80→70.
- **USUM→LGPE.** Absorb 20→40, Mega Drain 40→75, Sky Attack 140→200, Solar Beam 120→200.
  All four revert at Gen 8.
- **Gen 8→9.** Recover, Soft-Boiled, Milk Drink, Slack Off, Roost, Shore Up and Rest go
  10→5 PP. Wicked Blow 80→75, Glacial Lance 130→120, Grassy Glide 70→(60)→55, Luster Purge
  and Mist Ball 70→95. Twelve LA-introduced moves change between their LA and SV values
  (§8.2).

### 7.2 PokéAPI's coverage, measured

`move_changelog.csv` has 198 rows over 168 moves. Grouped by the version group in which
the change took effect:

| Changed in | Rows | power / acc / PP / type / priority |
|---|---|---|
| gold-silver | 20 | 5 / 3 / 0 / 4 / 0 (+ 5 effect, 9 effect-chance) |
| ruby-sapphire | 2 | 1 / 2 / 0 / 0 / 0 |
| diamond-pearl | 19 | 10 / 3 / 6 / 0 / 0 |
| platinum | 1 | 0 / 1 / 0 / 0 / 0 |
| black-white | 40 | 22 / 19 / 9 / 1 / 0 |
| x-y | 83 | 50 / 11 / 16 / 3 / 4 |
| sun-moon | 11 | 7 / 3 / 1 / 0 / 0 |
| lets-go-pikachu-lets-go-eevee | 5 | 4 / 0 / 2 / 0 / 1 |
| sword-shield | 16 | 15 / 5 / 9 / 0 / 0 |
| scarlet-violet | 1 | 1 (Mist Ball) / 0 / 0 / 0 / 0 |

Source: [`move_changelog.csv`](https://raw.githubusercontent.com/PokeAPI/pokeapi/4b82c204ddd19ecb8eda2ea044ccb59e222b721c/data/v2/csv/move_changelog.csv).

**Cross-check.** I reconstructed each move's power, accuracy, PP and type at the last
version group of each generation from 1 to 8 (Yellow, Crystal, FRLG, HGSS, B2W2, ORAS,
USUM, Crown Tundra), once from PokéAPI's changelog and once from Showdown's layered mods.
Over every move both know, they **disagree on 76 cells**:

- **Representation, mostly not data (22 cells).**
  - Showdown writes 100% where PokéAPI writes "—" (never misses) on status moves such as
    Mimic, Foresight, Lock-On and Pain Split.
  - Bide is `???` in Gen 1, Punishment's power is variable, and Nature Power's Gen 3
    accuracy is 95.
  - One cell is real: Struggle's Gen 1 PP. Bulbapedia and Showdown agree it changed 10→1
    at Gen 2, and PokéAPI says 1 throughout.
- **Gen 9 changes PokéAPI lacks (50 cells).** PokéAPI shows the Gen 9 value for every
  earlier generation:
  - **healing PP 10→5:** Recover, Soft-Boiled, Rest, Milk Drink, Slack Off, Roost and
    Shore Up, each over every generation it existed in (41 cells)
  - **power:** Luster Purge 70→95, Grassy Glide 70→55, Wicked Blow 80→75, Glacial Lance
    130→120 (9 cells)
- **PokéAPI errors (4 cells, §7.4):** Mind Reader in Gen 2–4 and Take Heart.

So the cross-check finds **no other** discrepancy in power, accuracy, PP or type across
Gen 1–8. Re-run `movecmp.ps1` (appendix) against a new SHA before trusting it again.

### 7.3 What `past_values` leaves out

| Not modelled | Size | Why it matters on a move screen |
|---|---|---|
| Category history | 54 at Gen 3→4 (derivable from the type rule), 2 later (Bite is derivable; Water Shuriken is not) | Category chip |
| Priority | The CSV has a `priority` column (5 rows); the **API's `PastMoveStatValues` does not expose it**. Bulbapedia counts 19 priority changes | Priority row, if the move screen shows one |
| Gen 8→9 numbers | 7 PP + 4 power (above) | Under a SwSh/BDSP lens these moves show SV values |
| Every side game | LA 138 moves, Z-A all 340, Champions all | See §8 |
| Within-SV patch | Grassy Glide 60→55 at SV 2.0.1 | Irrelevant under "DLC folded in": SV shows 55 |
| Name history | 21 English renames at Gen 5→6 (Hi Jump Kick→High Jump Kick, AncientPower→Ancient Power, SolarBeam→Solar Beam, …), 1 at Gen 7→8 (Vice Grip→Vise Grip) | `move_names.csv` has one current name per language. A Red/Blue lens would print "High Jump Kick" |

Sources: Bulbapedia [List of modified moves § Priority changes and § Name changes](https://bulbapedia.bulbagarden.net/wiki/List_of_modified_moves);
PokéAPI docs [`PastMoveStatValues`](https://pokeapi.co/docs/v2#pastmovestatvalues) (fields:
accuracy, effect_chance, power, pp, effect_entries, type, version_group);
[`move_names.csv`](https://raw.githubusercontent.com/PokeAPI/pokeapi/4b82c204ddd19ecb8eda2ea044ccb59e222b721c/data/v2/csv/move_names.csv).

### 7.4 Two PokéAPI move errors

| Move | PokéAPI | Bulbapedia | Showdown | Verdict |
|---|---|---|---|---|
| Mind Reader | 40 PP before Black/White, then 5 | base PP 5, no PP change listed | 5 in Gen 2–4 | **PokéAPI wrong**; the Gen 2–4 PP is 5 |
| Take Heart | 10 PP | base PP 15, "10 in P:LA" | 15 | **PokéAPI holds the LA value as the current one**; SV is 15 |

Sources: Bulbapedia [Mind Reader (move)](https://bulbapedia.bulbagarden.net/wiki/Mind_Reader_(move))
and [Take Heart (move)](https://bulbapedia.bulbagarden.net/wiki/Take_Heart_(move)) infobox
`basepp`; Showdown [`gen2/moves.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen2/moves.ts)
and [`data/moves.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/moves.ts).
Comparing Showdown's current data with PokéAPI's current `moves.csv` turns up only Take
Heart, plus three null-versus-0 accuracy encodings. So **PokéAPI's current move values
are otherwise SV-accurate**.

---

## 8. Side-mechanic games

### 8.1 Let's Go, Pikachu!/Eevee! (`lets-go-pikachu-lets-go-eevee`, Gen 7)

- **No abilities, held items, breeding or Eggs**; no weather, no Z-Moves. EVs are replaced
  by Awakening Values. Mega Evolutions of Gen 1 Pokémon return.
  Source: [LGPE page](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon:_Let%27s_Go,_Pikachu!_and_Let%27s_Go,_Eevee!).
- **Types and chart:** Gen 6+ (Clefairy is Fairy here).
- **Moves:** 4 buffed (Absorb 40/15 PP, Mega Drain 75/10 PP, Solar Beam 200, Sky Attack
  200). Teleport's effect and priority change. The partner-exclusive moves (Zippy Zap,
  Sparkly Swirl, …) have LGPE values that SwSh later changed.
  - PokéAPI encodes all of this as a pair of rows: values "before LGPE" plus values
    "before SwSh". Resolving by version-group order gives the right LGPE answer.
  - Showdown's [`gen7letsgo/moves.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen7letsgo/moves.ts)
    agrees.
- **Curation:** a per-version-group "no abilities" flag. The rest is covered.

### 8.2 Legends: Arceus (`legends-arceus`, Gen 8)

- **No abilities, held items, breeding, Eggs or Pokérus.** EVs and IVs are replaced (for
  display) by effort levels. Source: [Pokémon Legends: Arceus](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Legends:_Arceus);
  [Effort values](https://bulbapedia.bulbagarden.net/wiki/Effort_values) ("EVs … cannot be
  viewed within Legends: Arceus, and have no effect").
- **Dual-type multipliers are 2.5×/0.4×** (§4.3).
- **Moves: an entirely separate table.** Every masterable move has a standard power, an
  agile power and a strong power, plus standard and strong accuracy. Source:
  [Move mastery](https://bulbapedia.bulbagarden.net/wiki/Move_mastery);
  [List of moves in Pokémon Legends: Arceus](https://bulbapedia.bulbagarden.net/wiki/List_of_moves_in_Pok%C3%A9mon_Legends:_Arceus).
  The list has **177 moves**, and Serebii's
  [attack list](https://www.serebii.net/legendsarceus/attacks.shtml) also shows 177.
  - **Diffed by script against Showdown's SwSh values** (and against SV for the 24 moves
    LA introduced), **138 of 177 moves differ** in at least one standard-style value:
    - **PP: 84 moves.** Thunderbolt 15→10, Double-Edge 15→5, Ancient Power 5→15, …
    - **Power: 73 moves.** Flamethrower 90→80, Hyper Beam 150→120, Outrage 120→90,
      Triple Arrows (SV 90) 50, …
    - **Accuracy: 21 moves.** Hypnosis 60→70, Dark Void 50→90, Blizzard 70→75, …
    - **Category: 3** (§6). **Type: 0.**
  - Spot-checked against Serebii: Flamethrower 10 PP / 80 / 100%, Double-Edge 5/100,
    Hyper Beam 5/120/90%, Triple Arrows 15/50, Take Heart 10 PP.
- **Base stats:** no changes found other than Cherrim-Sunshine (§3.3). **Unverified:** I
  did not diff LA's full species table, and Bulbapedia has no LA base-stat change category.
- **PokéAPI:** nothing game-specific. LA is "generation 8", so stats, types and abilities
  resolve to SwSh values. There are no `legends-arceus` rows in `move_changelog`, and the
  24 LA-introduced moves carry their SV values (or, for Take Heart, LA's PP).
- **Curation:** the 177-move table (PP; power ×3; accuracy ×2; category), the
  no-abilities flag and the 2.5×/0.4× rule.

### 8.3 Brilliant Diamond/Shining Pearl (`brilliant-diamond-shining-pearl`, Gen 8)

Gen 8 mechanics throughout: Fairy exists, Gen 8 stats, abilities present. Showdown's
[`gen8bdsp`](https://github.com/smogon/pokemon-showdown/tree/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/gen8bdsp)
mod contains only availability flags (`isNonstandard`) and evolution-method tweaks. It
has no value changes. **Generation-keyed data is correct here.**

### 8.4 Legends: Z-A (`legends-za` + `mega-dimension`, Gen 9)

- **No abilities, breeding or Eggs. Held items do exist.** Source:
  [Pokémon Legends: Z-A](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Legends:_Z-A).
- **No PP.** Each move has a **cooldown time** instead. Source:
  [Cooldown](https://bulbapedia.bulbagarden.net/wiki/Cooldown).
- **No accuracy and no evasion.** Source:
  [Pokémon battle (Legends: Z-A) § Stat modifiers](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_battle_(Legends:_Z-A)).
  A Z-A move screen that shows a PP or accuracy row is wrong for **every move**.
- **Move values:** [List of moves in Pokémon Legends: Z-A](https://bulbapedia.bulbagarden.net/wiki/List_of_moves_in_Pok%C3%A9mon_Legends:_Z-A)
  lists **340 base moves**, plus "Plus" and Rogue-boss variants (446 variant rows, which I
  excluded). The page is marked incomplete for version 2.0.0 availability.
  - Diffed against SV (Showdown current data):
    - **33 power differences.** The four +1-priority moves drop 40→30 (Quick Attack, Mach
      Punch, Bullet Punch, Aqua Jet). Fissure and Sheer Cold become 250/150-power hits.
      Seed Bomb becomes 35×4. Nihil Light goes 100→200. Several multi-hit moves are
      rewritten as fixed ×N.
    - **1 type change:** Growth, Normal → Grass.
    - **1 category change:** Dark Void, Status → Special.
  - New fields Z-A shows instead: cooldown, duration, range.
- **Base stats:** +40% Attack on 5 Pokémon/forms (§3.3). Z-A adds 26 new Mega Evolutions,
  and Mega Dimension adds more; these are new forms, i.e. identity data. EVs exist
  ([Effort values](https://bulbapedia.bulbagarden.net/wiki/Effort_values)).
- **PokéAPI:** nothing game-specific. Z-A resolves to SV values everywhere.
- **Curation:** a cooldown column replacing PP, accuracy suppressed, 33 power overrides,
  Growth's type, Dark Void's category, 5 Attack overrides, and the no-abilities flag.

### 8.5 Pokémon Champions (`champions`, Gen 9)

Source for everything below unless noted:
[Pokémon Champions § Changes from Scarlet and Violet](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Champions);
cross-checked against Showdown's
[`champions/moves.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/champions/moves.ts)
and [`champions/scripts.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods/champions/scripts.ts).

- **Abilities present.** The 7 new abilities (Dragonize, Mega Sol, Piercing Drill, Spicy
  Spray, Eelevate, Fire Mane, Aura Guard) are already in PokéAPI's `abilities.csv`
  (ids 308–314, generation 9). Healer, Unseen Fist and Run Away were modified.
- **PP is re-scaled for every move.** A base PP of 5, 10, 15 or ≥20 is shown as 8, 12, 16
  or 20. There are 10 further exceptions (Protect, Spiky Shield, King's Shield and Baneful
  Bunker at 8; Wish, Strength Sap, Sandstorm and Snowscape at 8; Beak Blast 8; Night Slash
  20). Showdown models the cap as "PP > 20 → 20" (34 moves) plus 15 explicit overrides.
- **Power: 15 moves in the prose of Bulbapedia's Champions page** (Slash, Trop Kick,
  Psyshield Bash, Apple Acid, Fire Lash, Grav Apple, Spirit Shackle, First Impression,
  Beak Blast, Mountain Gale, Meteor Assault, Bone Rush, Infernal Parade, Snipe Shot,
  Night Daze). Its modified-moves table lists 12 of them, and hidden comments add more
  for moves "not in the game yet". Showdown has **25** power overrides.
- **Accuracy:** Crabhammer 95%, Syrup Bomb 90%, Make It Rain 95%.
- **Type:** Snap Trap, Grass → Steel. Showdown also makes Growth Grass, matching Z-A;
  Bulbapedia's Champions section does not mention Growth. **Unverified** for Champions.
- **Effect chances:** Iron Head 20%, Moonblast 10%, Dire Claw 30%, Salt Cure halved, Toxic
  Thread −2 Speed.
- **Stats:** level is hidden and stats are computed at level 50 with IVs fixed at 31. EVs
  are replaced by stat points. **Base stats are unchanged.** Showdown has no
  `champions/pokedex.ts`.
- **PokéAPI:** the version group exists (id 32), but nothing in any changelog is keyed to
  it.
- **Curation:** a PP mapping rule plus ~10 exceptions, ~15–25 power, 3 accuracy, 1–2 type
  and 5 effect-chance overrides, and 3 ability-prose changes.

### 8.6 Colosseum/XD and the Japanese Gen 1 releases

These are small and can be skipped for v1:
- Shadow Rush is 90 power in Colosseum and 55 in XD.
- Blizzard's freeze chance is 30% in RG/Blue (Japan) and 10% from Yellow on. That is the
  "within Gen I" row of Bulbapedia's additional-effect table.

PokéAPI has neither.

---

## 9. Other things a game-faithful screen gets wrong

These are fields a Pokémon or move screen shows that are absent or drifted in older
games. "Rule" means it can be decided per version group or generation without per-row data.

| Field | Drift | Source | PokéAPI |
|---|---|---|---|
| **EV yield** | Not a field in Gen 1–2: stat experience gained = the defeated Pokémon's base stats. Replaced by AVs (LGPE), effort levels (LA) and stat points (Champions) | [Effort values](https://bulbapedia.bulbagarden.net/wiki/Effort_values) | rule |
| EV yield values | **17 species** changed yield (Yanma, Misdreavus, Blissey, Roselia, Duskull, Dusclops, Slowking, Watchog, Basculin, Shaymin, Aegislash, Kleavor, Wyrdeer, Basculegion, Sneasler, Overqwil, Enamorus) | Category [EV yield changed](https://bulbapedia.bulbagarden.net/wiki/Category:Pok%C3%A9mon_whose_EV_yield_changed) | **7 covered** (`pokemon_stats_past.effort`, 6 at Gen 3→4 plus Slowking). Also beware: 7 base-stat rows carry a placeholder `effort` that is not a real past yield (e.g. Raichu 0 vs 3) |
| **Abilities section** | none in Gen 1–2, LGPE, LA, Z-A | §5.1 | rule |
| **Held items** (wild held items row) | none in Gen 1, LGPE, LA | [Held item](https://bulbapedia.bulbagarden.net/wiki/Held_item) ("first introduced in Generation II"), LGPE/LA pages | per-version data in `pokemon_items` |
| **Gender ratio** | Gen 1 has no gender (only the two Nidoran species) | [Gender](https://bulbapedia.bulbagarden.net/wiki/Gender) | rule |
| **Egg groups / hatch steps** | No breeding in Gen 1, LGPE, LA or Z-A | [Egg Group](https://bulbapedia.bulbagarden.net/wiki/Egg_Group) ("introduced in Generation II"); LGPE, LA and Z-A pages | rule |
| **Natures** (if ever shown) | Gen 3+ | [Nature](https://bulbapedia.bulbagarden.net/wiki/Nature) | rule |
| **Base friendship** | All 70 in Gen 2; per-species from Gen 3. **Gen 8 lowered every 70 to 50** (except non-Galarian Stunfisk and Timburr) | [List of Pokémon by base friendship](https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_base_friendship) | **One value, and stale.** `pokemon_species.base_happiness` still holds **70 for 667 species**: the Gen 3–7 value, not the latest |
| **Base experience yield** | Re-based at Gen 5 (it was a byte, capped at 255; Magikarp 20) | [Experience](https://bulbapedia.bulbagarden.net/wiki/Experience) | One value. Bulbapedia gives Blissey 635 "as of Generation IX"; PokéAPI has 608. **Unverified which is right**; no history either way |
| **Stat total** | Gen 1 sums five stats (Special counted once) | follows §3.1 | derived |
| **Move names** | 22 English renames | §7.3 | none |
| **Move/ability prose** | Effect text changed with mechanics | — | `move_effect_changelog` (28) and `ability_changelog` (52) prose up to Gen 5, plus per-version-group flavour text |
| **Pokédex text, sprites** | per game | — | per-version flavour text exists (per-game data, out of scope here) |

---

## 10. PokéAPI `past_*` coverage summary

### 10.1 Keying and semantics (read before building)

| API field | CSV | Keyed by | Meaning of the key |
|---|---|---|---|
| `pokemon.past_stats` | `pokemon_stats_past` (235 rows) | generation | "The last generation in which the referenced pokémon had the listed stats" |
| `pokemon.past_types` | `pokemon_types_past` (36 rows) | generation | last generation with those types |
| `pokemon.past_abilities` | `pokemon_abilities_past` (569 rows) | generation | last generation; a null ability means the slot was empty |
| `type.past_damage_relations` | `type_efficacy_past` (6 rows) | generation | last generation with those relations |
| `move.past_values` | `move_changelog` (198 rows) | **version group** | **the version group in which the values below were replaced** |

Source: [PokéAPI docs](https://pokeapi.co/docs/v2) (`PokemonStatPast`, `PokemonTypePast`,
`PokemonAbilityPast`, `TypeRelationsPast`, `PastMoveStatValues`).

**The `past_values` docs are misleading.** They describe `version_group` as "the version
group in which these move stat values were in effect". The CSV column is
`changed_in_version_group_id`, and the live API serves that column under the name
`version_group`:
- `GET /api/v2/move/bite` returns `{"type": "normal", "version_group": "gold-silver"}`.
  Bite was Normal *before* Gold/Silver, not in it.
- `GET /api/v2/move/absorb` returns three entries (`diamond-pearl`: PP 20;
  `lets-go-pikachu-lets-go-eevee`: power 20, PP 25; `sword-shield`: power 40, PP 15).
  They only make sense as "values that held *until* this group". That is how they encode
  LGPE's 40/15 Absorb.

**Resolution rule.** For a lens version group L and a field f, take the first changelog row
for that move, ordered by `version_groups.order`, whose group's order is greater than L's
and whose f is non-null. If there is none, use the current value. My cross-check (§7.2)
used exactly this rule. The version-group order must be the CSV's `order` column, not the
id: `red-green-japan` is id 28 but order 1.

**Generation keying flattens side games.** Every `past_stats`, `past_types` and
`past_abilities` lookup for LGPE gives SM/USUM's answer, and for LA it gives SwSh's. For
Z-A and Champions it gives SV's. That is right for types and the chart. It is right for
stats except Z-A's 5 overrides. It is wrong for abilities in LGPE, LA and Z-A, where they
should be absent.

### 10.2 Covered vs curated

| Mechanic | PokéAPI coverage (Gen 1–9 mainline) | Needs curation |
|---|---|---|
| Stat layout (Special) | ✓ complete | — |
| Base stats | ✓ complete, matches Bulbapedia and Showdown species-for-species | Z-A: 5 Attack overrides |
| Types / retypings | ✓ complete (29 Pokémon/forms) | — |
| Type chart | ✓ complete (6 cells + type introduction gens) | LA dual-type 2.5×/0.4× (rule) |
| Abilities absent | ✗ (implicit for Gen 1–2 via `abilities.generation_id` ≥ 3) | Flag for Gen 1–2, LGPE, LA, Z-A |
| Ability access | ✓ through Gen 7; ~ Gen 8→9 (19 rows vs 22 entries) | Verify Gen 8→9 edge cases |
| Ability effects prose | ✓ through Gen 5 only | Gen 6+ and Champions (low priority) |
| Physical/special | ✓ derivable for Gen 1–3 (`types.damage_class_id`) | Water Shuriken (Gen 6); LA's 3 |
| Move power/acc/PP/type | ✓ Gen 1–8 except the 2 errors | Gen 9: 7 PP + 4 power; Mind Reader; Take Heart |
| Move priority | CSV partial (5 of 19), **not exposed by the API** | ~14 rows if a priority row is shown |
| LGPE moves | ✓ (bracketed rows) | — |
| LA moves | ✗ | **177-move table** (138 differ, ×3 power styles) |
| Z-A moves | ✗ | **340-move table**: cooldown instead of PP, no accuracy, 33 power, 1 type, 1 category |
| Champions moves | ✗ | PP mapping rule + ~10 exceptions, ~15–25 power, 3 accuracy, 1–2 type |
| EV yield | 7 of 17 | 10 rows + per-game "no EV yield" rule |
| Base friendship, base exp | no history; friendship stale | rule (70/50) + 667-species fix if shown |
| Move names | no history | 22 renames |

---

## 11. Recommendation

1. **Import PokéAPI's `past_*` tables for Gen 1–9 mainline as they are.** Types, the
   chart and stats need no curation. Moves need a 13-row patch: 7 healing PP rows, 4
   power rows, Mind Reader and Take Heart.
2. **Model the lens's mechanics regime as data on the version group, not on the
   generation.** At minimum:
   - `hasAbilities`: false for Gen 1–2, LGPE, LA and Z-A
   - `statLayout`: 5 for Gen 1
   - `categoryRule`: type-based before Gen 4
   - `dualTypeMultiplier`: 2.5/0.4 for LA
   - `moveCost`: PP, or cooldown for Z-A
   - `hasAccuracy`: false for Z-A
   - `ppScale`: Champions

   These are a dozen booleans and enums across 32 version groups. They replace every
   "is this game X" branch in UI code.
3. **Curate the three side-game move tables as committed JSON** (the #10 intermediate),
   keyed by version group and move slug: LA (177), Z-A (340) and Champions (overrides
   only). Seed LA and Z-A from Bulbapedia's per-game move lists for verification, and
   author the data rather than importing it. Bulbapedia is CC BY-NC-SA (see the
   encounter-coverage note §7). Showdown's data is MIT
   ([LICENSE](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/LICENSE)),
   which makes it the licence-friendly cross-check for everything it models. It does not
   model LA or Z-A moves.
4. **Decide two product questions before the screens.** These are decision-ticket
   material, not research:
   - what an LA move row shows for its agile and strong values
   - whether Z-A's ability-like passives get an abilities section at all
5. **Fix or drop base friendship**: PokéAPI's value is the Gen 3–7 one, not the latest.

### Residual uncertainty

- Z-A's Meditite and Medicham Attack boost: Bulbapedia only.
- Growth's type in Champions: Showdown only.
- Champions' remaining power changes: Bulbapedia 12 vs Showdown 25, partly unreleased
  moves.
- Gen 8→9 ability access: 19 vs 22.
- Blissey's base experience: 608 vs 635.
- LA base stats beyond Cherrim: not diffed.

Each is small, and each is named where it occurs above.

---

## Appendix: reproducing these numbers

Python is not installed on the investigating machine. Everything ran in PowerShell 5.1
(`Import-Csv`, regex over the Showdown `.ts` files) and Git Bash (`curl`, `awk`).
Bulbapedia's HTML returns 403 to automated fetchers; its raw wikitext does not:

```bash
# Bulbapedia raw wikitext (any page)
curl -A "Mozilla/5.0 (research)" \
  "https://bulbapedia.bulbagarden.net/w/index.php?title=List_of_modified_moves&action=raw"
# Category sizes
curl "https://bulbapedia.bulbagarden.net/w/api.php?action=query&list=allcategories&acprefix=Pok%C3%A9mon%20whose&acprop=size&format=json"

# PokéAPI CSVs at the pinned SHA
S=4b82c204ddd19ecb8eda2ea044ccb59e222b721c
for n in pokemon_stats_past pokemon_types_past pokemon_abilities_past type_efficacy_past \
         move_changelog ability_changelog move_effect_changelog version_groups types \
         moves pokemon_stats pokemon_species abilities move_names; do
  curl -sO https://raw.githubusercontent.com/PokeAPI/pokeapi/$S/data/v2/csv/$n.csv
done

# Showdown at the cited commit: data/{moves,pokedex,typechart}.ts and
# data/mods/{gen1..gen8,gen7letsgo,gen8bdsp,gen8legends,gen9legends,gen9predlc,gen9dlc1,champions}/*.ts
SD=942cda71d14bf0618bd57991708a2e2734b6daba
curl -sO https://raw.githubusercontent.com/smogon/pokemon-showdown/$SD/data/mods/gen1/pokedex.ts
```

Scripts used (kept in the session scratchpad, not committed):
- `movecmp.ps1` resolves each move's value per generation, both from PokéAPI's changelog
  (with the rule in §10.1) and from Showdown's layered mods, then diffs them. It produced
  the 76 disagreements in §7.2.
- `lacmp.ps1` parses Bulbapedia's LA move list and diffs it against Showdown's gen 8
  values. It produced the 138/177 count. An equivalent inline pass over the Z-A list
  produced 33/340.
- The Gen 3→4 category count applies Showdown's own `specialTypes` list to moves #1–354.

### Primary sources cited

- Bulbapedia (CC BY-NC-SA, used for verification only):
  - Mechanics pages:
    [List of modified moves](https://bulbapedia.bulbagarden.net/wiki/List_of_modified_moves),
    [List of modified Ability access](https://bulbapedia.bulbagarden.net/wiki/List_of_modified_Ability_access),
    [Type](https://bulbapedia.bulbagarden.net/wiki/Type),
    [Type/Type chart](https://bulbapedia.bulbagarden.net/wiki/Type/Type_chart),
    [Fairy (type)](https://bulbapedia.bulbagarden.net/wiki/Fairy_(type)),
    [Steel (type)](https://bulbapedia.bulbagarden.net/wiki/Steel_(type)),
    [Rotom](https://bulbapedia.bulbagarden.net/wiki/Rotom_(Pok%C3%A9mon)),
    [Ability](https://bulbapedia.bulbagarden.net/wiki/Ability)
  - Stat pages:
    [Effort values](https://bulbapedia.bulbagarden.net/wiki/Effort_values),
    [List of Pokémon by base friendship](https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_base_friendship),
    [Experience](https://bulbapedia.bulbagarden.net/wiki/Experience),
    plus the base-stat, Special-split and EV-yield categories linked in §3 and §9
  - Introduction of other mechanics:
    [Held item](https://bulbapedia.bulbagarden.net/wiki/Held_item),
    [Gender](https://bulbapedia.bulbagarden.net/wiki/Gender),
    [Egg Group](https://bulbapedia.bulbagarden.net/wiki/Egg_Group),
    [Nature](https://bulbapedia.bulbagarden.net/wiki/Nature)
  - Single moves:
    [Mind Reader](https://bulbapedia.bulbagarden.net/wiki/Mind_Reader_(move)),
    [Take Heart](https://bulbapedia.bulbagarden.net/wiki/Take_Heart_(move))
  - Side games:
    [LGPE](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon:_Let%27s_Go,_Pikachu!_and_Let%27s_Go,_Eevee!),
    [Pokémon Legends: Arceus](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Legends:_Arceus),
    [List of moves in Pokémon Legends: Arceus](https://bulbapedia.bulbagarden.net/wiki/List_of_moves_in_Pok%C3%A9mon_Legends:_Arceus),
    [Move mastery](https://bulbapedia.bulbagarden.net/wiki/Move_mastery),
    [Pokémon Legends: Z-A](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Legends:_Z-A),
    [Pokémon battle (Legends: Z-A)](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_battle_(Legends:_Z-A)),
    [List of moves in Pokémon Legends: Z-A](https://bulbapedia.bulbagarden.net/wiki/List_of_moves_in_Pok%C3%A9mon_Legends:_Z-A),
    [Cooldown](https://bulbapedia.bulbagarden.net/wiki/Cooldown),
    [Pokémon Champions](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_Champions)
- Serebii: [Pokémon Legends: Arceus – Attacks](https://www.serebii.net/legendsarceus/attacks.shtml)
- Pokémon Showdown (MIT), commit `942cda71d14bf0618bd57991708a2e2734b6daba`:
  - [`data/mods/`](https://github.com/smogon/pokemon-showdown/tree/942cda71d14bf0618bd57991708a2e2734b6daba/data/mods):
    gen1–gen8, gen7letsgo, gen8bdsp, gen8legends, gen9legends, gen9predlc, gen9dlc1, champions
  - [`data/moves.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/data/moves.ts),
    [`sim/dex-data.ts`](https://github.com/smogon/pokemon-showdown/blob/942cda71d14bf0618bd57991708a2e2734b6daba/sim/dex-data.ts)
- PokéAPI:
  - CSVs at [`4b82c204`](https://github.com/PokeAPI/pokeapi/tree/4b82c204ddd19ecb8eda2ea044ccb59e222b721c/data/v2/csv)
  - [docs](https://pokeapi.co/docs/v2)
  - live `GET /api/v2/move/bite` and `/move/absorb`, read 2026-10-11
