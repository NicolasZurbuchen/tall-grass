# How do Pokédexes present per-game data alongside cross-game discovery?

Research note on game scope: a game-scoped view vs a national view, and what to show for
data that is missing from the selected game.

- **Date of investigation:** 2026-10-11
- **Method:** pages opened directly on each site (Serebii, PokémonDB, Bulbapedia's raw wikitext
  via `action=raw`, rendered pages where layout mattered); Smogon's embedded dex JSON; the
  source of Pokémon Showdown, pkmn.help and PokédexTracker on GitHub; Pokémon Support articles
  read through the Zendesk article API (the HTML pages return 403 to non-browsers); PokéAPI
  CSVs measured at the dataset's pinned SHA `4b82c204` (`tools/datagen/.../Upstream.kt`).
- **Status:** answered. No product decision is made here.

---

## Summary

- **Nobody runs a single model.** Every reference site mixes at least three patterns: a
  latest-by-default page, per-section game tabs for learnsets, footnotes for historical
  types, abilities and stats, and a per-game availability table. Only the tools built for one
  job (Smogon, pkmn.help) commit to a **global game mode**, and both default to the newest
  generation or to National.
- **Global mode hides what the game lacks; it does not annotate it.** In Smogon's RB dex, Onix
  has `"evos":[]`, so Steelix is simply not there. pkmn.help drops every species outside the
  selected game's dex and puts back the old types. Reference wikis go the other way. They never
  hide anything, and they mark absence in the availability table ("Unobtainable", "Trade",
  "#---").
- **Historical differences are footnotes on the latest values almost everywhere.** PokémonDB's
  "*X* changes" list, Bulbapedia's "Gen IV+" ability superscripts and "Special base stat in
  Generation I was 60". Only learnsets get real per-game tabs, because they differ too much to
  footnote.
- **Seven patterns cover the field:** global mode, per-section tabs, latest plus footnotes,
  availability matrix, game as a filter facet, context from what the user owns, and (on the
  data side) inherit-plus-delta. Each one breaks on a different one of the five test cases. See
  §2.
- **HOME's model is narrower than "per-game data".** HOME keeps a separate **profile** per set
  of Switch games (SwSh, BDSP, LA, SV, Z-A). Per the official FAQ, each profile holds the
  **moveset**. Per Bulbapedia, it also holds the **Ability** (since 3.0.0) and PP Ups. Ball,
  origin mark, Alpha mark and effort levels are only *displayed* differently: "HOME only
  changes the displayed information and does **not** overwrite Pokémon data". Tera Type and
  the Gigantamax Factor are stored once and sent only to games that use them.
- **HOME's own Pokédex is National.** Game-specific dexes (added in 2.0.0) are completion
  trackers that "can only register Pokémon caught in those specific games". They are not
  data views. Availability shows as **grayed-out game icons** under "Games it can travel to".
- **PokéAPI can back most of this, with granularity traps.** Per-game dexes:
  `pokedex_version_groups`. Learnsets: `pokemon_moves` by version group. Past types, abilities
  and stats: `pokemon_*_past`, but **per generation, not per game**, and keyed on the *last*
  generation the value held. **Legends: Z-A and Mega Dimension have zero learnset rows**, and
  Champions has only `train` rows. There is no availability table: `pokemon_game_indices`
  copies SV's 733 species onto Z-A and gives Let's Go 802.
- **A correction to the brief:** Red/Blue/Yellow and FireRed/LeafGreen share **one** Kanto dex
  (PokéAPI `pokedex` 2 serves version groups 1, 2, 7, 28 and 29). Pairs that really differ
  within a region are D/P vs Platinum (151 vs 210), G/S vs HG/SS (251 vs 256), B/W vs B2/W2
  (156 vs 301) and S/M vs US/UM (302 vs 403).

---

## 1. Prior art survey

### 1.1 Comparison table

| | Scope selector | Default | Absent from selected game | Historical types/abilities/stats | Learnsets | Availability | Dex numbers |
|---|---|---|---|---|---|---|---|
| **Serebii** | One directory per generation (`/pokedex/`, `/pokedex-swsh/`, `/pokedex-sv/`); "Gen I Dex … Gen IX Dex" links | Gen IX dex | Page still exists; regional numbers read `#---`; no Locations block for that game | Each gen dex shows that gen's values (Gen I page: HP/Atk/Def/**Special**/Spe) | Per gen dex; Gen IX page has a **Z-A / Scarlet-Violet toggle** | Per-game "Locations" rows | National plus every regional dex in the gen, `#---` if absent |
| **Bulbapedia** | None global. Learnset has "Other generations: I – II – …" subpage links | Newest gen | Availability row "Unobtainable" or "Trade" | Infobox superscripts ("Gen IV+"); prose ("Prior to Generation VI, it was a Normal type"); "Special base stat in Generation I was 60"; type chart labelled "in Generation IX" | Main page shows newest gen, with "*X* is available in …" line; gen subpages split **per game in columns** (Pikachu Gen I: RGB vs Y, `N/A` where not learnt) | "Game locations" table grouped by gen, one row per game | Dex/Gen block per gen, regional numbers |
| **PokémonDB** | Moves: game tabs + "In other generations" 1–9 links; separate per-game list pages | Newest games | Where-to-find row "Trade/migrate from another game"; empty tab text ("does not learn any level up moves in Pokémon Legends: Z-A") | "*X* changes" bullet list ("In Generations 1-5, Clefairy is Normal type") | Gen pages `/pokedex/onix/moves/1` with per-game tabs (Red/Blue, Yellow) | "Where to find" per game; SV/Z-A "Location data not yet available" | "Local №" list, one line per game |
| **Smogon dex** | **Generation in the URL** (`/dex/rb/`, `/dex/sv/`) | SV | **Hidden**: RB Onix `evos: []`; in SV dex, past Pokémon tagged `isNonstandard: "NatDex"` | Whole page is that gen's values (RB Clefairy Normal, `spa = spd = 60`) | Per gen | n/a (competitive) | National only |
| **Showdown data** | `data/mods/genN/` inherit from latest | Latest gen | `isNonstandard: "Past"` (Onix, Mega Gengar) | Per-gen mods override only the deltas | Per gen | n/a | n/a |
| **pkmn.help** | **One global "Pokédex" setting** (version group) in Settings | "National Pokédex" | **Hidden**: list narrowed to that VG's dex species | Past types restored per VG | n/a | n/a | n/a |
| **Pokémon HOME** | Connected game (Switch) or most recent game (mobile) per Pokémon profile; Guidebook filter by Switch game | National Pokédex | "Games it can travel to": game icon **grayed out** | n/a (stores per-game moves/Ability, see §3) | Move/Ability list filter per Switch game (3.0.0) | Guidebook "Appears In"; Switch dex lists obtainable Gen VI–IX games | National; game dexes as trackers |
| **PokédexTracker** | Dex instance = game family × dex type × shiny flag | Chosen at creation | Not in that dex type | n/a | n/a | Per-game location notes | Regional / game national / full national |

### 1.2 Notes per source

**Serebii.** The pages are scoped by generation, not by game. The Gen IX Onix page exists even
though Onix is not in Scarlet/Violet. It shows `Paldea #---`, `Kitakami #---`, `Blueberry #---`
and `Lumiose #197`, and its only Locations are Legends: Z-A and Mega Dimension
([serebii.net/pokedex-sv/onix](https://www.serebii.net/pokedex-sv/onix/)). Nothing says
"not in SV": absence is implied by `#---` and a missing block. The Gen I page shows the
**Special** stat, Red/Green/Blue/Yellow locations, and an evolution chain with **only Onix**.
Steelix is left out because it does not exist in that generation
([serebii.net/pokedex/095.shtml](https://www.serebii.net/pokedex/095.shtml)). The SwSh page
for Sentret, which is unobtainable in SwSh, shows Galar/IoA/CT `#---` and BDSP locations only
([serebii.net/pokedex-swsh/sentret](https://www.serebii.net/pokedex-swsh/sentret/)). The Gen IX
Gengar page shows Mega Gengar with Z-A art and a Z-A / SV learnset toggle. No sentence says
the Mega is Z-A-only
([serebii.net/pokedex-sv/gengar](https://www.serebii.net/pokedex-sv/gengar/)).

**Bulbapedia** (wikitext read via `action=raw`, layout checked on the rendered page,
[Clefairy](https://bulbapedia.bulbagarden.net/wiki/Clefairy_(Pok%C3%A9mon))):
- Infobox: current type only. The lede says "Prior to Generation VI, it was a Normal type".
  The ability carries `Gen IV+` (Magic Guard).
- Base stats: current values plus "This Pokémon's Special base stat in Generation I was 60."
- Type effectiveness: "Under normal battle conditions in Generation IX", with a pre-Fairy
  footnote.
- Game locations: one block per generation, one row per game. Absence is written
  `Availability/Entry2/None … area=Unobtainable`. Onix in SV reads "Unobtainable", Clefairy in
  SV reads "Trade, Version 2.0.1+"
  ([Onix](https://bulbapedia.bulbagarden.net/wiki/Onix_(Pok%C3%A9mon))).
- Learnset: the main page shows the newest generation and opens with "Clefairy is available
  in Scarlet and Violet Version 2.0.1+ and Legends: Z-A". For a Pokémon absent from every
  Gen IX game it **falls back to the newest generation that has it**: "Caterpie is available in
  Sword and Shield and Brilliant Diamond and Shining Pearl", with a Gen VIII table
  ([Caterpie](https://bulbapedia.bulbagarden.net/wiki/Caterpie_(Pok%C3%A9mon))). Older
  generations are subpages, and within a generation **each game gets its own column**. In
  Pikachu's Gen I table, Thunderbolt is `N/A` in R/G/B and level 26 in Yellow
  ([Pikachu/Generation I learnset](https://bulbapedia.bulbagarden.net/wiki/Pikachu_(Pok%C3%A9mon)/Generation_I_learnset)).

**PokémonDB.**
- Species page: latest values, plus a "Clefairy changes" list. Examples: "In Generations 1-5,
  Clefairy is Normal type", "In Generation 3, Clefairy lacks Magic Guard", "In Generation 1,
  Clefairy has a base Special stat of 60"
  ([pokemondb.net/pokedex/clefairy](https://pokemondb.net/pokedex/clefairy)). Gengar: "In
  Generations 3-6, Gengar has the Levitate ability"
  ([gengar](https://pokemondb.net/pokedex/gengar)).
- "Local №": one line per game, 16 for Clefairy.
- Moves: tabs for the newest games, plus "In other generations" links to
  `/pokedex/<name>/moves/<gen>`. Inside a generation each game is a tab. Onix Gen 1 has "Red/Blue"
  and "Yellow" tabs, and the page notes power and accuracy "as it was in the respective games"
  ([onix/moves/1](https://pokemondb.net/pokedex/onix/moves/1)).
- A failure worth noting: Caterpie, which is not in Z-A, has a **Legends: Z-A tab that says
  "Caterpie does not learn any level up moves in Pokémon Legends: Z-A"**. That reads as
  "learns nothing", not as "not in this game"
  ([caterpie](https://pokemondb.net/pokedex/caterpie)).
- Per-game list pages (`/pokedex/game/red-blue-yellow`) show the 151 with **Gen 1 types**
  (Clefairy Normal, Magnemite pure Electric, no Steel, Dark or Fairy) and RB sprites. Every
  entry still links to the single national species page
  ([game/red-blue-yellow](https://pokemondb.net/pokedex/game/red-blue-yellow)).

**Smogon Strategy Dex.** The generation is the first path segment and the page embeds that
generation's whole species table (`dump-basics` RPC in `dexSettings`). Read from the
embedded JSON of [smogon.com/dex/rb/pokemon/clefairy](https://www.smogon.com/dex/rb/pokemon/clefairy/):
RB Clefairy is `"types":["Normal"]` with `spa:60, spd:60`, so Gen 1 Special is stored as two
equal stats. RB Onix has `"evos":[]`. In [the SV dex](https://www.smogon.com/dex/sv/pokemon/onix/),
Onix has `"evos":["Steelix"]`, `"formats":["National Dex"]` and `"isNonstandard":"NatDex"`, so
a Pokémon absent from the game is kept and tagged.

**Pokémon Showdown** (data, not UI). The latest data plus per-generation mods that set
`inherit: true` and override only the deltas. `data/mods/gen1/pokedex.ts` overrides only
`baseStats` with `spa == spd`. `data/mods/gen5/pokedex.ts` gives Clefairy
`types: ["Normal"]`. `data/formats-data.ts` marks Onix and Mega Gengar
`isNonstandard: "Past"`
([formats-data.ts](https://github.com/smogon/pokemon-showdown/blob/master/data/formats-data.ts),
[gen1](https://github.com/smogon/pokemon-showdown/blob/master/data/mods/gen1/pokedex.ts),
[gen5](https://github.com/smogon/pokemon-showdown/blob/master/data/mods/gen5/pokedex.ts)).

**pkmn.help.** A single version-group setting labelled "Pokédex", with the help text "Controls
available Pokémon and types". `""` means "National Pokédex" and is the default, persisted in
`localStorage`. When a group is set, `App.tsx` narrows `allPokemon` to that group's species and
runs `restorePastTypesByVersionGroup`. The species list is built from PokéAPI's
`version_group.pokedexes`, with DLC groups folded into the base game and Colosseum/XD skipped
because "Colosseum and XD have no Pokédex data in PokeAPI at all"
([useVersionGroup.ts](https://github.com/wavebeem/pkmn.help/blob/main/src/hooks/useVersionGroup.ts),
[App.tsx](https://github.com/wavebeem/pkmn.help/blob/main/src/components/App.tsx),
[en.json](https://github.com/wavebeem/pkmn.help/blob/main/public/locales/en.json),
[scrapeVersions.ts](https://github.com/wavebeem/pkmn.help/blob/main/bin/lib/scrapeVersions.ts)).

**Pokémon HOME.** The official features page says HOME has a National Pokédex. Pokémon are
registered by depositing them, and Megas and Gigantamax forms are registered along with them.
The mobile app lists each Pokémon's Abilities "as well as the moves it can learn". Since 2.0.0
there are game-specific Pokédexes, and "Each game-specific Pokédex, such as the Sinnoh Pokédex of
Pokémon Brilliant Diamond and Pokémon Shining Pearl … can only register Pokémon caught in those
specific games". The Guidebook says "You can also discover which games a Pokémon can be
encountered in"
([home.pokemon.com/en-us/features](https://home.pokemon.com/en-us/features/)).
Per Pokémon, "Scroll down to the Games it can travel to section … If a game image is grayed
out, that Pokémon cannot be used in that game"
([Support: which games can I transfer to](https://support.pokemon.com/hc/en-us/articles/360039592832-How-can-I-tell-which-games-I-can-transfer-my-Pok%C3%A9mon-to-in-Pok%C3%A9mon-HOME)).
Secondary: Bulbapedia reports a v3.0.0 per-game filter on the move/Ability list, which drops
entries absent from that game and swaps in that game's descriptions. It also reports the
Guidebook's "Appears In" and its filter by obtainable Switch game
([Pokédex (HOME)](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9dex_(HOME)),
[Pokémon HOME](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_HOME)).

**Living-dex trackers: PokédexTracker.** A dex is a row with `game`, `dex_type`, `regional` and
`shiny` ([dexes/models.go](https://github.com/pokedextracker/api.pokedextracker.com/blob/master/pkg/dexes/models.go)).
The live `/api/dex-types` list has these per game family:
- "Regional" and "Full National".
- BDSP: "Game National", meaning the game's own capped national dex, as opposed to the full one.
- SV: "Full Paldea", described as "Includes Paldea base dex AND new Kitakami and Blueberry
  dexes", plus Kitakami-only and Blueberry-only.
- HOME: "Full National", plus a "Gigantamax Forms" customization layered on it via
  `base_dex_type_id`.

Shininess is a flag on the dex, not a dex type. The live list starts at X/Y
([pokedextracker.com/api/dex-types](https://pokedextracker.com/api/dex-types)). So a tracker's
"dex" is (game, which list, forms add-on, shiny?), and the user picks it once.

**Mobile apps.** Two apps could be checked only through their store listings, not their UI.
PikaDex ("Dex for every game!", "from Red/Blue/Yellow to the new Scarlet/Violet"; a national
dex arrived in 1.5.1) ([App Store](https://apps.apple.com/us/app/-/id1509561856)). ProKédex
("Manage your caught list for each game, including regional and national Dex")
([App Store](https://apps.apple.com/app/id6474760289)). Neither listing says how per-game values
are presented. The in-game SV Pokédex was not surveyed: it only lists its own game, so it says
nothing about discovery across games.

---

## 2. Taxonomy of patterns

The five test cases:
- **Onix/Steelix:** Onix seen in a Red/Blue context.
- **Gen 1 Special:** one stat where later games have two.
- **Old-only move:** a move a Pokémon learns only in older games.
- **Mega in SV:** a Mega viewed with Scarlet/Violet selected.
- **Absent from newest:** Caterpie or Onix, neither of which is in Scarlet/Violet.

| Pattern | Used by | Good at | Onix/Steelix | Gen 1 Special | Old-only move | Mega in SV | Absent from newest |
|---|---|---|---|---|---|---|---|
| **A. Global game mode** (one app-wide game/gen) | Smogon (URL), pkmn.help (setting), Serebii (directory) | Total consistency: every number on screen belongs to one game | Steelix **disappears** (Smogon RB `evos:[]`); clean, but gives no hint it exists | Natural: render the one stat | Natural: in that game's mode it is there; in the latest it is gone | Hidden or flagged (Showdown `Past`) | The page must still exist (Serebii) or the Pokémon vanishes from lists (pkmn.help) |
| **B. Per-section game tabs** | PokémonDB moves, Bulbapedia learnset, Serebii Z-A/SV toggle | Precision where data differs most (learnsets) without making the whole app modal | n/a: evolution is not tabbed | Not handled by tabs; stats stay latest | Found by visiting an older tab | n/a | **Empty-tab ambiguity**: PokémonDB's Caterpie Z-A tab says "does not learn any…" |
| **C. Latest + historical footnotes** | PokémonDB "changes", Bulbapedia superscripts and prose | Compact; latest stays the headline; history findable | Not expressible as a footnote | Works well ("Special was 60") | Too many to footnote | Silent on "not in SV" | Latest values shown with no sign the game lacks the Pokémon |
| **D. Availability matrix** (rows per game) | Bulbapedia Game locations, PokémonDB Where to find / Local №, Serebii `#---` | Discovery: one glance answers "where does this exist" | Shows Steelix's games, not Onix's evolution in RB | n/a | n/a | Can show Mega rows per game, if the data exists | Explicit "Unobtainable" / "Trade" / `#---` |
| **E. Game as filter facet** | pkmn.help list, PokémonDB per-game lists, HOME Guidebook/moves filter | Cross-game queries in one game ("Electric in SV") | Filters membership; evolution links still point outside | Only if the facet also swaps values (pkmn.help swaps types, PokémonDB lists show Gen 1 types) | Facet on the move list (HOME 3.0.0) | Megas filtered out | Excluded from the filtered list |
| **F. Context from what the user owns** | HOME profiles, PokédexTracker dex instances | Answers "in *my* game" with no global mode | Tracker: Steelix not in an RB-family dex | n/a | HOME: per-game moveset survives | HOME: Gmax Factor stored, shown where relevant | Grayed game icon |
| **G. Inherit + delta** (data, not UI) | Showdown `mods/genN` | A small way to get per-generation values for A, C or E | Needs a separate "exists in" flag | Delta row | n/a | `isNonstandard` flag | Flag |

Observations that cut across patterns:
- **A and E differ only in reach.** A applies the game to every screen. E applies it to one
  list. pkmn.help is E's mechanics wired up as A: the setting lives in Settings and narrows
  everything.
- **Discovery and precision fall to different patterns.** D and E carry discovery ("all
  Electric", "where does it live"). A, B and C carry precision. No source does both with one
  pattern.
- **Absence needs its own state, separate from empty.** Bulbapedia ("Unobtainable") and HOME
  (grayed icon) model it. PokémonDB's empty tab and Serebii's missing Locations block do not.

---

## 3. Pokémon HOME's data model, fact-checked

The claim was that HOME "stores each Pokémon's data per game, so moving between Scarlet, Let's
Go Pikachu, etc. keeps game-specific characteristics". **Partly true.** Per-game storage covers
moves and Ability, not "data" in general. Let's Go is not one of the profiles.

| What | Behaviour | Source |
|---|---|---|
| **Moveset** | Saved per linked game. Returning to a game "will restore the last move set it had prior to being transferred out of it". In a game that cannot use the moves, they are "replaced with moves that it is able to learn in the target software" | Official: [Why did my Pokémon's moves change?](https://support.pokemon.com/hc/en-us/articles/6372731346452-Why-did-my-Pok%C3%A9mon-s-moves-change) |
| **Let's Go** | Let's Go Pokémon moved to SwSh "will retain their original move set". Once moved to LA, BDSP or SwSh they "cannot be moved back" to Let's Go. **So Let's Go is a one-way source, not a profile** | Same article |
| **Ability** | A separate value per profile since v3.0.0 | Secondary: [Bulbapedia, Pokémon HOME § Game-specific data](https://bulbapedia.bulbagarden.net/wiki/Pok%C3%A9mon_HOME) |
| **PP Ups** | Stored per game | Secondary: same |
| **Profiles that exist** | SwSh, BDSP, LA, SV, Z-A. Bank, GO, Let's Go, Switch FRLG and most HOME gifts share the SwSh set | Secondary: same |
| **Poké Ball** | Shown as Poké Ball or Strange Ball where the original does not exist; the original returns on the next move to HOME | Official: [Poké Ball different](https://support.pokemon.com/hc/en-us/articles/6372740700692-Why-is-my-Pok%C3%A9mon-s-Pok%C3%A9-Ball-different-than-the-one-I-caught-it-in) |
| **Display-only differences** | Ball, moves, Alpha mark, "Conditions that are limited to a specific game (effort level, Super Contest Condition, etc.)": "HOME only changes the displayed information and does **not** overwrite Pokémon data" | Official: [information displays incorrectly](https://support.pokemon.com/hc/en-us/articles/7963042951444-Why-does-Pok%C3%A9mon-information-display-incorrectly-or-not-appear-in-Pok%C3%A9mon-HOME) |
| **Effort levels** | Visible only via LA | Official: [effort levels](https://support.pokemon.com/hc/en-us/articles/6372695365652-Why-can-I-no-longer-see-the-effort-levels-of-my-Pok%C3%A9mon) |
| **Gigantamax Factor** | "Yes, Pokémon HOME will recognize a Pokémon's Gigantamax Factor" | Official: [Gigantamax](https://support.pokemon.com/hc/en-us/articles/360039030792) |
| **Tera Type, met location** | Stored once, sent only to games that use them (Tera → SV only) | Secondary: Bulbapedia |
| **Which profile is shown** | Switch: the connected game's view. Pokémon not in that game show no status or moves. Unconnected, it shows "as if you were connected to Pokémon Sword or Pokémon Shield". Mobile: "will reflect the last game title they were in" | Official: [can't see moves](https://support.pokemon.com/hc/en-us/articles/6425814626580-Why-can-t-I-see-my-Pok%C3%A9mon-s-moves-in-Pok%C3%A9mon-HOME) |
| **Move Reminder (3.0.0)** | On withdrawal, choose "from among a list of moves it has learned up to that point", limited to moves legal in the target game | Official: [features](https://home.pokemon.com/en-us/features/); detail secondary (Bulbapedia) |
| **Z-A** | One-way: "If you transfer a Pokémon from a previous title to Pokémon Legends: Z-A, you will no longer be able to transfer it to previous video games" | Official: [legends.pokemon.com news](https://legends.pokemon.com/en-us/news/pokemon-home) |

**What HOME's own Pokédex treats as canonical:**
- **The National Pokédex**, registered by depositing Pokémon (official features page).
- Game dexes are completion trackers filled only by Pokémon whose game of origin matches.
- Per-game *reference* data shows up in only three places: the move/Ability list filter, the
  Guidebook's "Appears In" (both secondary, Bulbapedia), and the official grayed-out "Games it
  can travel to".
- Not verified: which game's values HOME's dex uses for a species' base stats and learnable
  moves. Neither official page says.

For Tall Grass the transferable idea is the separation itself: **a canonical national entity,
with game context as a view over it**. HOME shows the game as a profile or a grayed icon, and
never forks the species.

---

## 4. PokéAPI support for each pattern

Measured at the pinned SHA; CSV paths are `data/v2/csv/` in
[PokeAPI/pokeapi](https://github.com/PokeAPI/pokeapi/tree/master/data/v2/csv). Field semantics
are from the [v2 docs](https://pokeapi.co/docs/v2).

| Need | Table / field | Coverage | Gap that makes a pattern misleading |
|---|---|---|---|
| Per-game dex (A, D, E) | `pokedexes` (35), `pokedex_version_groups` (45 rows), `pokemon_dex_numbers` (8,010) | Every main-series VG except Colosseum/XD. DLC dexes are attached to the base VG too (Isle of Armor → VG 20 and 21) | `national` has **no** version group. Game-capped national dexes (FRLG 386, BDSP 493) are not modelled. Kanto is one dex for RB, Y, FRLG and the JP carts |
| Per-VG learnsets (B; also an availability proxy) | `pokemon_moves` (638,321 rows; `version_group_id`, method, level) | RB/Y split, as on Bulbapedia. SV 54,658 rows / 867 Pokémon | **`legends-za` and `mega-dimension`: 0 rows.** DLC VGs 0 (folded into base). `champions`: 19,810 rows, all `train`. Megas carry their own rows (Mega Gengar: XY, ORAS, SM, USUM, Let's Go, Champions; not Z-A) |
| Past types (C, A) | `pokemon_types_past` / `past_types` | 29 Pokémon (Fairy retcon, Magnemite line, Rotom forms, …) | "**The last generation** in which the referenced pokémon had the listed types". Per **generation**, not VG |
| Past abilities | `pokemon_abilities_past` | 569 rows, but only **29** carry an ability (Gengar → Levitate to gen 6). 540 are `ability_id` null, meaning "slot didn't exist yet" | Gen 1–2 have no abilities at all and that is **not** a row: it has to be a client rule. The same goes for Let's Go, which has no abilities, inside Gen 7 |
| Past stats | `pokemon_stats_past` / `past_stats` | 151 Gen 1 `special` rows (stat id 9), plus 84 later buffs over 69 Pokémon | Gen 1 needs the client to hide Sp. Atk/Sp. Def and show `special` |
| Past type chart | `type_efficacy_past` | 6 rows (Gen 1 Ghost→Psychic 0×, Bug/Poison 2×, Ice→Fire; Gen 2–5 Ghost/Dark→Steel ½×) | Per generation |
| Evolution scope (Onix/Steelix) | `pokemon_evolution.version_group_id` | Steelix's row is VG 3 (Gold/Silver) | It is the VG the *method* was introduced in, not a list of games the evolution exists in. Comparing it with `version_groups.order` is a usable "not yet" test |
| Mega / Gmax existence | `pokemon_forms.introduced_in_version_group_id`, `is_mega`; `pokemon_form_generations` | 97 Mega forms: 30 XY, 18 ORAS, 49 Mega Dimension | `pokemon_form_generations` lists only gens 6/7 for 48 Megas, and nothing for Z-A or Gmax. Nothing says "Megas are absent from SV" |
| Per-version availability (D) | **None.** `pokemon_game_indices` is the nearest | 151 RB … 649 BW, then an internal index count | **Not availability.** Let's Go 802, Z-A 733 (= a copy of SV's), SV 733 vs 664 species in SV's three dexes |
| Locations (D) | `encounters` | Gen 1–8 | **Zero** for SV, LA, BDSP, Z-A (see [pokeapi-encounter-data-coverage.md](pokeapi-encounter-data-coverage.md)) |

What this means per pattern:
- **A / E (game as mode or facet):** buildable for Gen 1–SV from dex membership plus past
  types, stats and abilities. **Z-A mode would show an empty Moves tab** for every Pokémon,
  and Gen 9 modes would show an empty Locations tab. Both are the empty-versus-absent problem
  from §2.
- **B (tabs per game):** fully backed up to SV. A Z-A tab cannot be built.
- **C (footnotes):** fully backed, at generation granularity. That is exactly how PokémonDB
  phrases its footnotes ("In Generations 1-5").
- **D (matrix):** dex numbers per game are backed. **Presence per game is not**: it has to be
  derived (dex membership, learnset VGs). That derivation gets it right for Onix in SV (no SV
  rows) and wrong for Onix in Z-A (in the Lumiose dex, but no learnset rows).

---

## Implications for Tall Grass

Neutral observations; the decision stays with the author.

- **The app already runs pattern C plus a local B/D.** It is latest-by-default
  (`DECISIONS.md` § "The learnset is the newest game that teaches"), with a game picker only
  on locations. That is the same mix as PokémonDB and Bulbapedia, minus the footnotes and the
  per-game rows. Adding "changes" footnotes is the cheapest precision upgrade, and the data
  for it exists (`pokemon_*_past`).
- **Discovery ("all Electric") is a facet question (E), not a mode question (A).** The facet
  gives "Electric in SV" without forcing every other screen into SV. pkmn.help shows the cost:
  its facet must also swap types, or Clefairy is listed as Fairy in a Red/Blue list.
- **A global game mode on a phone puts state where the user can't see it.** Smogon keeps it in
  the URL. pkmn.help keeps it in Settings, away from the list it changes. `DECISIONS.md` already
  plans for `app.db` to hold "the remembered game", though nothing stores it yet, so
  persistence is planned either way. The open question is how visible the selection is on screen.
- **Whichever pattern is picked, "absent from this game" needs its own UI state.** The two
  sites that skip it (PokémonDB's empty Z-A tab, Serebii's silent `#---`) are the ones that
  mislead. PokéAPI's empty Gen 9 locations and empty Z-A learnsets would reproduce the same
  ambiguity unless "no data upstream" is kept apart from "not in game".
- **Onix/Steelix splits two ways.** Hiding is Smogon's choice, with no trace left. Showing it
  with a "from Gold/Silver" mark is possible from `pokemon_evolution.version_group_id`. No
  surveyed reference site does the second inside a game-scoped view.
- **Generation-level history is what the data supports cleanly.** Per-version-group precision
  exists only for dex membership and learnsets. A UI that promises "exactly as in Yellow" for
  types, stats or abilities would rest on generation-level data plus hand-written rules (no
  abilities before Gen 3 or in Let's Go, Special in Gen 1).
- **HOME's lesson is architectural, not visual.** It keeps one national species and treats game
  context as a view or annotation (profile, grayed icon), never as a fork. That fits #5's
  Species/Variant split. A game would be a lens over the same rows, not a second dataset.
