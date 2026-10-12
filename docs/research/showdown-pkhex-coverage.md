# How much of the curation gap Showdown and PKHeX can fill

Research note for GitHub issue #98 (`NicolasZurbuchen/tall-grass`). It builds on
[`per-game-rosters.md`](per-game-rosters.md) (#88),
[`generation-mechanic-changes.md`](generation-mechanic-changes.md) (#89) and
[`curated-data-sources-and-licences.md`](curated-data-sources-and-licences.md) (#90), under the
permissive-dataset shape chosen in #91.

- **Date of investigation:** 2026-10-12
- **Method:** primary files downloaded raw from GitHub at pinned commits and parsed locally in
  PowerShell 5.1 and Git Bash (`curl`, `awk`, `xxd`). Python is not installed.
  - **Pokémon Showdown** (MIT) at
    [`942cda7`](https://github.com/smogon/pokemon-showdown/tree/942cda71d14bf0618bd57991708a2e2734b6daba)
    (2026-10-10), the same commit as #89.
  - **PKHeX** (GPL-3.0) at
    [`66ef598`](https://github.com/kwsch/PKHeX/tree/66ef5983a5d349efa2dd615f2926a1b1ee97306e)
    (2026-10-08). Binary tables were decoded byte by byte, using the field offsets declared in
    PKHeX's own C# readers. **Nothing from PKHeX was copied into the repo.** The decoders are
    scratchpad scripts that print facts.
  - **PokéAPI** CSVs at the pinned SHA
    [`4b82c20`](https://github.com/PokeAPI/pokeapi/tree/4b82c204ddd19ecb8eda2ea044ccb59e222b721c/data/v2/csv).
  - **Bulbapedia and Serebii** were not re-read. Every count compared against them is a figure
    from the three earlier notes.
- **Status:** answered for all five questions. The open points are listed in §7.

---

## TL;DR

1. **Rosters: fully derivable, from both sources, and the two agree.** Showdown's
   `formats-data.ts` mods and PKHeX's personal-table presence flags give exactly the earlier
   notes' rosters for every checked game: SwSh 664, SV 733, BDSP 493, LA 242, Z-A 364,
   LGPE 153, USUM 807, SM 802. Both give the **80** SwSh and **69** SV out-of-dex species,
   species for species. PKHeX does it without a second source, because its table also carries the
   dex indexes. **Nothing is left to curate.**
2. **Regional forms per game: fully derivable.** Both sources give all **162** form-in-game pairs
   (SM 18, USUM 18, LGPE 18, SwSh 30, LA 18, SV 44, Z-A 16), including which 7 Alolan forms SwSh
   lacks and which 3 SV lacks. The open question from #88 is settled: **Hisuian Samurott is not in
   Z-A**, according to both.
3. **Obtain method: derivable at category level for the four games PokéAPI leaves empty.**
   Decoding PKHeX's encounter, gift, trade and wondercard tables for BDSP, LA, SV and Z-A puts
   every one of the 1,832 entries #88 counted as underivable into a category. The catch, gift
   and evolve counts land within a few species of Bulbapedia's (§3.2). What is left to curate:
   - the "conditional" class (trade evolution, another game's save, an event item), about 35
     entries;
   - a residual that means "transfer or not modelled" (BDSP 3, SV 30);
   - the gift/catch boundary, which PKHeX does not label.

   **Showdown's `S` sources are not an obtain-method signal.** They mix real distributions with
   in-game gifts and statics that have fixed moves.
4. **Gen 8–9 encounters: PKHeX has location, species, form, level range and game-specific
   conditions. It has no encounter rates**, except a weight on SV Tera raids. Time and weather
   exist only for SV. Alpha status exists for LA and Z-A. Location ids need a separate string table.
5. **Side-game move values: Showdown has none.** `gen8legends` and `gen9legends` contain
   learnsets, availability and a few stat overrides, and **no `moves.ts`**. PKHeX has LA's PP (it
   matches Bulbapedia on all 177 moves), Z-A's type table (Growth = Grass) and both games'
   usable-move lists. **Power, accuracy, agile/strong styles and cooldowns are in neither.** The
   #89 curation load stands, minus LA's PP column.
6. **Learnsets: Showdown is finer than PokéAPI exactly where PokéAPI is thinnest.** At the pinned
   SHA, PokéAPI has **0** learnset rows for Z-A and Mega Dimension, and 329 tutor rows for LA.
   Showdown has 17,180 Z-A entries and 3,232 LA tutor entries. For Gen 3–7, Showdown is
   coarser: one code per generation, and the per-game differences survive only in partial
   snapshot mods.

---

## 1. Rosters

### 1.1 Where the facts are

| Source | File | Field | Decode effort |
|---|---|---|---|
| Showdown | `data/formats-data.ts` (SV) and `data/mods/<mod>/formats-data.ts` | `isNonstandard` per species/forme id. A species is in the game when the field resolves to null. `"Past"` = not in this game, `"Future"` = later generation, `"Unobtainable"`, `"LGPE"`, `"CAP"` | **Low, with three traps.** (a) The value has to be resolved through the mod chain in `sim/dex.ts`: an entry without `inherit: true` replaces the parent entry, and a missing entry inherits it. (b) Two rules live in **code**, not data, in `sim/dex-species.ts` L563–583: everything above the mod's generation is `Future`, and `gen7letsgo` and `gen8bdsp` impose hard-coded rosters (Kanto + Meltan line + Alolan forms + Megas; ≤ Gen 4). (c) One-line `foo: {},` entries break a naive multi-line regex. Trap (c) cost this note a wrong USUM count (805) before it was fixed. |
| PKHeX | `PKHeX.Core/Resources/byte/personal/personal_{swsh,bdsp,la,sv,za}` | `IsPresentInGame`: bit 6 of byte `0x21` (SwSh, BDSP, LA; record size `0xB0`/`0x44`/`0xB0`), or byte `0x1C` ≠ 0 (SV, Z-A; record `0x50`). Dex indexes: SwSh `0x5C`/`0xAC`/`0xAE` (Galar/Armor/Crown), SV `0x1E` (u16 Paldea) and `0x4A`/`0x4B` (Kitakami/Blueberry), LA `0x60` (Hisui) | **Low.** Fixed-size records in national order. Entry *n* is species *n*. Alternate forms are appended at the end and reached through `FormStatsIndex` (`0x1E` or `0x18`) plus `FormCount` (`0x20` or `0x1A`). Offsets come from `PKHeX.Core/PersonalInfo/Info/PersonalInfo{8SWSH,8BDSP,8LA,9SV,9ZA}.cs`. |

Showdown mods used, by inheritance (from each mod's `scripts.ts`): `gen8` → SwSh, `gen7` → USUM,
`gen7sm` → SM, `gen7letsgo` → LGPE, `gen8bdsp` → BDSP, `gen8legends` → LA, `gen9legends` → Z-A,
base `data/` → SV.

### 1.2 Measured against the #88 figures

| Group | #88 roster | Showdown | PKHeX present | Set-identical? |
|---|---|---|---|---|
| Sun/Moon | 802 | 802 | — (no Gen 7 table checked) | count only |
| Ultra Sun/Ultra Moon | 807 | 807 | — | count only |
| Let's Go | 153 | 153 (by the code rule) | — | count only |
| Sword/Shield | 664 | 664 | 664 | ✅ SD = PKHeX |
| BDSP | 493 | 493 | 493 | ✅ |
| Legends: Arceus | 242 | 242 | 242 (counting a species present if any form is) | ✅ |
| Scarlet/Violet | 733 | 733 | 733 | ✅ |
| Legends: Z-A (+MD) | 364 | 364 | 364 | ✅ |

**Out-of-dex species:**

| Group | #88 (curate) | PKHeX: present ∧ no dex index | Showdown roster − PokéAPI dex union | Match with the #88 number list |
|---|---|---|---|---|
| Sword/Shield | 80 | **80** | **80** (union 584) | exact: `150-151, 243-245, 249-260, 380-385, 480-488, 494, 641-647, 649, 716-719, 721-730, 785-809` |
| Scarlet/Violet | 69 | **69** | **69** (union 664) | exact: `144-146, 150-151, 243-245, 249-250, 377-386, 480-493, 638-648, 719-721, 789-792, 800-801, 863, 888-899, 903, 905` |

Two cautions:

- **LA presence is per form.** Counting only base-form entries gives 226, because 16 Hisui
  dex slots are filled by a Hisuian form whose base form is absent (Growlithe and others).
  Count a species when any of its forms is present.
- **PKHeX's dex-index fields do not all mean "in a regional dex".**
  - For SwSh and SV they reproduce PokéAPI's dex unions.
  - BDSP has 150 species with a non-zero `PokeDexIndex`, not 151. Probably one entry sits at
    index 0. Not reconciled.
  - For Z-A, `DexIndex` is non-zero on 603 species, which is more than the 364 present. Its
    meaning outside the game's own species was not established.

  Roster derivation should use the presence flag, not the index.

> **Already true of PokéAPI, and worth knowing.** #88 §6 found `pokemon_game_indices.csv`
> set-identical for SwSh (664) and SV (733). Roster minus dex union is therefore derivable from
> PokéAPI alone for those two groups as well. #88 §7 still lists the 80 and 69 as curated,
> presumably because the same table is wrong for five other groups. Showdown and PKHeX now give
> two independent confirmations.

### 1.3 Regional forms per game (the 162 pairs)

| Group | #88 | Showdown (non-battle-only `Alola`/`Galar`/`Hisui`/`Paldea*`/`Bloodmoon` formes, standard in the mod; cap Pikachu excluded) | PKHeX (form entries present) |
|---|---|---|---|
| Sun/Moon | 18 | 18 | — |
| Ultra Sun/Ultra Moon | 18 | 18 | — |
| Let's Go | 18 | 18 (code rule) | — |
| Sword/Shield | 30 (11 A + 19 G) | 30 (11 + 19) | 30 (11 + 19) |
| Legends: Arceus | 18 (16 H + 2 A) | 18 | 18 |
| Scarlet/Violet | 44 (15 A, 8 G, 16 H, 4 P, Bloodmoon) | 44, same split | 44, same split |
| Legends: Z-A | 16 (4 A, 8 G, 4 H) | 16 | 16 |
| **Total** | **162** | **162** | 146 checked (the Gen 7 tables were not decoded) |

The absent forms match #88 one for one:

- **SwSh lacks** Alolan Rattata, Raticate, Geodude, Graveler, Golem, Grimer and Muk.
- **SV lacks** Alolan Rattata, Raticate and Marowak.
- **Z-A's four Hisuian forms** are Qwilfish, Sliggoo, Goodra and Avalugg. **Hisuian Samurott
  (503 form 1) is not present** in `personal_za`, and it is not standard in `gen9legends`. The
  empty Bulbapedia cell flagged in #88 §4 note ³ resolves to "absent".

The PKHeX form numbers are game-internal: Slowbro's Galarian form is form 2, because form 1 is
the Mega. Mapping them to Tall Grass variants needs a hand-made species:form → slug table of 58
rows. That table is new curation, but it does not grow per game.

**Gen 7 caveat.** For SM, USUM and LGPE, the Showdown counts come from mod data plus the
hard-coded LGPE rule. They were not cross-checked against a game table.

---

## 2. Gen 8–9 encounters: what PKHeX's tables hold

All wild tables are "BinLinker" containers:

- a 2-byte ASCII tag (`bs`, `la`, `sv`, `za`);
- a u16 count;
- an offset table, u32 pairs, or u16 pairs for Z-A (`BinLinkerAccessor16.cs`);
- area records.

Record layouts are taken from the `ReadSlot` methods in
`PKHeX.Core/Legality/Encounters/Templates/Gen8b|Gen8a|Gen9|Gen9a/EncounterArea*.cs`.

| Game | Wild file(s) | Per area | Per slot | Slots / location ids / species | Rates? |
|---|---|---|---|---|---|
| **BDSP** | `legality/wild/Gen8/encounter_{bd,sp,bd_underground,sp_underground}.pkl` | u16 location, u8 slot type | 4 bytes: species (10 bits) + form (top 5 bits), level min, level max | 57,922 / 235 / 283 | **No** |
| **Legends: Arceus** | `encounter_la.pkl` | a *list* of u8 locations, `SlotType8a` (Standard, Distortion, Landmark, Mass Outbreak, Massive Mass Outbreak) | 8 bytes: species, form, **alpha**, level min/max, gender, **flawless-IV count** | 7,132 / — / 224 | **No** |
| **Scarlet/Violet** | `wild/Gen9/encounter_wild_paldea.pkl` | u8 location, u8 **cross-from** location (spill-over spawns) | 8 bytes: species, form, gender, level min/max, **time-of-day bitmask**, **weather bitmask** (`AreaWeather9`: Normal, Overcast, Raining, Thunderstorm, Mist, Snowing, Snowstorm, Sandstorm) | 26,861 / 79 / 519 | **No** |
| SV, other | `encounter_gem_{paldea,kitakami,blueberry}.pkl` (Tera raids, 24-byte records), `encounter_fixed_paldea.pkl` (fixed symbols, 20), `encounter_outbreak_paldea.pkl` (outbreaks, 28), `encounter_dist_paldea.pkl` and `encounter_might_paldea.pkl` (distribution and 7★ raids, 62) | — | raids: species, form, level, **stars**, Tera type, moves, **`RandRate`** plus per-version min weights; outbreaks: level range, weather, scale range | — | **Raid weights only** |
| **Legends: Z-A** | `encounter_za.pkl`, `encounter_hyperspace_za.pkl` | u16 location | 8 bytes: species, form, gender, level min/max, **alpha**, shiny lock | 2,369 / 100 / 332 | **No** |

Static, gift and trade encounters are not in pickles. They are C# array literals, one per line,
with a trailing `// Name` comment:

- `Legality/Encounters/Data/Gen8/Encounters8b.cs`: 48 statics, 4 trades
- `Encounters8a.cs`: 81 statics
- `Gen9/Encounters9.cs`: 84 statics, 33 trades
- `Gen9a/Encounters9a.cs`: 44 statics, 26 gifts, 5 trades

They carry species, form, level, location, fixed ball, flawless IVs, shiny lock, nature, moves
and flags such as `IsRoaming`, `IsTitan`, `IsAlpha` and `FatefulEncounter`. **Decode effort:
moderate.** Each file has its own constructor syntax (`Species = N`, `new(N,form,lvl,…)`,
`new(TradeNames, i, SV, N, lvl)`), so a reader has to be written per file and kept up to date as
the file changes.

**What is *not* there, for all four games:**

- **No encounter rates** for wild slots. These are legality tables: they say *can*, not *how
  often*.
- **No location names.** Locations are numeric ids. Names come from per-language string tables
  (also game text) or have to be curated.
- **No time or weather for BDSP, LA or Z-A.** BDSP's swarms, Poké Radar and Great Marsh
  rotations are not visible as conditions in the slot format. Whether `SlotType8b` encodes any of
  them was not checked.
- **No version split in LA or Z-A** (single-version games). BDSP and SV split by file or flag.

Sword/Shield, for completeness, has `encounter_sw|sh_{symbol,hidden,nest,dist}.pkl`, plus
`encounter_swsh_underground.pkl` (Dynamax Adventures) and the 100 KB `Encounters8.cs`. These were
listed, not decoded: #88 found PokéAPI's SwSh encounters usable.

---

## 3. Obtain method

### 3.1 What each source can say

| Category | PKHeX | Showdown | Evolution data |
|---|---|---|---|
| **catch** | wild slots, Tera raids, fixed symbols, outbreaks, non-gift statics (§2) | no | — |
| **gift** | statics with a fixed Poké Ball (BDSP, SV), `Gifts` (Z-A), in-game `Trade*` arrays | partly: `eventData` entries with `pokeball: "pokeball"` and `source: "gen8bdsp"/"gen8legends"/"gen9legends"` are in-game gifts and statics with fixed moves | — |
| **evolve** | no, but `Resources/byte/evolve/evos_{la,sv,za}.pkl` hold per-game evolution methods (not decoded) | `prevo`/`evoType` in `pokedex.ts`, with per-mod overrides (`gen8legends/pokedex.ts` re-points 10 evolutions to Hisuian forms) | PokéAPI `pokemon_evolution.csv`; trigger `trade` gives trade evolutions |
| **trade (paired version)** | per-version files and flags (BD/SP, SL/VL arrays) | no | — |
| **transfer** | only as a residual: present, but no encounter of any kind | no | — |
| **event** | `Resources/legality/mgdb/{wc8,wb8,wa8,wc9,wa9}.pkl` | `eventData` with `pokeball: "cherishball"` | — |

**Showdown's `S` sources do not separate events from in-game encounters.** At `942cda7`,
`learnsets.ts` has gen 8 `eventData` on 127 species (186 entries) and gen 9 on 127 (152). Split
by tag:

| Game (by `source`, else generation) | Cherish Ball (real distributions) | Other ball (in-game gifts/statics) |
|---|---|---|
| SwSh (gen 8, untagged) | 36 species | 96 |
| BDSP (`gen8bdsp`) | — | 32 |
| LA (`gen8legends`) | — | 17 |
| SV (gen 9, untagged) | 45 | 88 |
| Z-A (`gen9legends`) | — | 6 |

The table records events whose moves or IVs matter for battle legality. That is not the same as
"this species is event-only", which Showdown flags (`eventOnly: true`, 203 species) without
saying which game. **Usable as a cross-check for gifts and events. Not usable to classify a
roster.**

### 3.2 Derived from PKHeX, against #88 §2

The PKHeX tables were decoded for BDSP, LA, SV and Z-A. Each roster species got the best
category it has: catch > gift > evolve (a pre-evolution is catch or gift, following Showdown
`prevo` chains) > breed (BDSP and SV only; an evolution is obtainable) > event (a wondercard,
plus SV distribution and 7★ raids) > residual.

| Group | Source | catch | gift | evolve | breed | conditional | event | transfer / residual | roster |
|---|---|---|---|---|---|---|---|---|---|
| BDSP | Bulbapedia (#88) | 309 | 8 | 143 | 8 | 21 | 2 | 2 | 493 |
| | **PKHeX** | 315 | 10 | 156 | 8 | — | 1 | **3** (Celebi, Deoxys, Phione) | 493 |
| Legends: Arceus | Bulbapedia | 240 | — | — | — | 2 | — | — | 242 |
| | **PKHeX** | 242 | 0 | 0 | — | — | 0 | 0 | 242 |
| Scarlet/Violet | Bulbapedia | 624 | 3 | 59 | — | 8 | 4 | 35 | 733 |
| | **PKHeX** | 627 | 3 | 62 | 1 | — | 10 | **30** | 733 |
| Legends: Z-A | Bulbapedia | 319 | 6 | 35 | — | 4 | — | — | 364 |
| | **PKHeX** | 354 | 3 | 7 | — | — | 0 | 0 | 364 |

The differences are about where a species is filed, not whether data is missing:

- **"Conditional" has no counterpart in PKHeX.** Trade evolutions, Darkrai and Shaymin's
  save-data condition in LA, and event-item encounters all appear in PKHeX as ordinary
  statics or evolutions. Trade evolution can be recovered from PokéAPI's evolution trigger. The
  save-data and item conditions cannot be recovered from any source checked.
- **Z-A catch 354 versus 319, and evolve 7 versus 35.** In PKHeX, evolved species that appear
  wild are "catch". Bulbapedia codes some of them as evolve. This is a coding convention, and it
  matters only for an "evolve" label.
- **SV's residual of 30 against Bulbapedia's 35 transfer + 4 event.** The residual is
  `377-379, 385, 480-482, 485-490, 492-493, 641-642, 645, 719-721, 789, 801, 888-890, 894-895, 898, 905`.
  The labels differ in the way #88 §5 row 2 already found between Serebii and Bulbapedia.
- **BDSP's Phione lands in the residual** because it hatches from Manaphy but is not Manaphy's
  pre-evolution. Breeding exceptions need a rule (Phione, incense babies).
- **The gift/catch boundary is a heuristic.** "Fixed Poké Ball" marks BDSP and SV gifts, but SV
  also uses it for the scripted Yungoos capture. LA marks every static with a fixed ball, so LA
  gifts cannot be told apart from catches this way.

Wondercard DBs, as decoded (record sizes `0x2D0`/`0x2DC`/`0x2C8`, card type at `0x11` or
`0x0F`, species at `0x240`/`0x288`/`0x238`/`0x270`; SV and Z-A species go through
`SpeciesConverter.Table9InternalToNational` above #917):

| Game | Records | Pokémon cards | Distinct species |
|---|---|---|---|
| SwSh `wc8.pkl` | 949 | 113 | 57 |
| BDSP `wb8.pkl` | 18 | 10 | 7 |
| LA `wa8.pkl` | 19 | 11 | 9 |
| SV `wc9.pkl` | 228 | 113 | 66 |
| Z-A `wa9.pkl` | 17 | 9 | 9 |

"Pokémon card" assumes `GiftType` value 1 = Pokémon (from each class's `IsEntity` getter). The
enum file itself was not read.

### 3.3 What is left to curate

Of the **1,832** BDSP, LA, SV and Z-A roster entries that #88 counted as having no derivable
obtain method:

- **derivable at category level:** about 1,797, so everything except the next two items.
- **curate:**
  - the conditional class, **about 35** entries (21 + 2 + 8 + 4 per Bulbapedia);
  - the residual, **33** (BDSP 3, SV 30), each to confirm as transfer-only or as a gap in the
    model;
  - a gift-versus-catch review of the roughly 260 statics and gifts (§2).
- **paired-version trades** (#88 §3, 146 upper bound for BDSP and SV) are derivable from PKHeX's
  per-version files and flags. Not computed here.

For Gen 1–8, #88's "about 4,200 non-catch rows" is not reduced by this note. PKHeX has tables
for those games too, but they were not decoded.

---

## 4. Side-game move tables (Legends: Arceus, Z-A)

| Value | Showdown `gen8legends` / `gen9legends` | PKHeX | #89 curation |
|---|---|---|---|
| Files present | `formats-data.ts`, `learnsets.ts`, `pokedex.ts`, `scripts.ts`. **No `moves.ts`** in either (GitHub contents API at `942cda7`) | `PKHeX.Core/Moves/MoveInfo8a.cs`, `MoveInfo9a.cs` | — |
| LA PP | none | `MoveInfo8a.PP`, a byte per move id, as a C# array literal. **177 of 177 match Bulbapedia's LA list** (Thunderbolt 10, Double-Edge 5, Ancient Power 15) | 84 PP differences: **derivable** |
| LA power (standard/agile/strong), accuracy, category | none | none | 73 power + 21 accuracy + 3 category: **still curate** |
| LA move availability | implied by learnsets | `MoveInfo8a.DummiedMoves` bitfield: **177 usable** | matches 177 |
| Z-A type | none | `MoveInfo9a.Type`: Growth (74) = 11 = **Grass** | 1 type change: derivable |
| Z-A PP | — | `MoveInfo9a.PP` holds **mainline values** (Thunderbolt 15). The data field exists, but Z-A shows cooldowns instead | not usable |
| Z-A power, cooldown, duration, range, category | none | none | 33 power, 1 category, cooldown column: **still curate** |
| Z-A availability | implied by learnsets | `MoveInfo9a.DummiedMoves`: **339 usable** | Bulbapedia 340: **1 unexplained** |
| Stat overrides | `gen9legends/pokedex.ts`: Mega Starmie, Mawile, Medicham Atk ×1.4; `gen8legends/pokedex.ts`: Cherrim-Sunshine | personal tables carry base stats (not diffed here) | as #89 §3.3 |

`legality/misc/mastery_la.pkl` and `plus_za.pkl` are the same size as `lvlmove_la.pkl` and
`lvlmove_za.pkl`. They are per-species learnset annotations (mastery levels and Plus-move
eligibility), not move values. **Decode effort for `MoveInfo*`: trivial.** They are integer
literals in C#, indexed by move id. This is the single cheapest PKHeX fact read in this note.

**Net change to #89's LA/Z-A load:**

- **LA:** the PP column (84 differences) and the move list come free. Power ×3, accuracy ×2 and
  category stay hand-curated.
- **Z-A:** the move list (339) and Growth's type come free. Everything else stays.

---

## 5. Per-game learnsets: Showdown against PokéAPI

Pokémon × move × method rows in PokéAPI `pokemon_moves.csv`, against Showdown entries by source
letter (`L` level-up, `M` TM/TR, `T` tutor, `E` egg, `S` event, `R` restricted, `V` VC or LGPE
transfer):

| Game | PokéAPI version group (rows by method) | Showdown file (entries by letter) | Which is finer |
|---|---|---|---|
| Legends: Arceus | `legends-arceus`: 2,230 = L 1,901 + tutor **329** | `mods/gen8legends/learnsets.ts`: 253 species, L 1,934 + T **3,232** + S 68 | **Showdown.** PokéAPI's LA tutor gap (#1316) is about 2,900 rows; Showdown has them |
| Legends: Z-A + MD | `legends-za`, `mega-dimension`: **0 rows** | `mods/gen9legends/learnsets.ts`: 396 species, L 5,257 + M 11,923 + S 24 | **Showdown, the only source** besides PKHeX's `lvlmove_za.pkl` |
| BDSP | `brilliant-diamond-shining-pearl`: 24,797 = L 7,851 + M 14,945 + E 1,969 + T 32 | `mods/gen8bdsp/learnsets.ts`: L 7,981 + M 15,519 + E 2,032 + T 33 | equivalent. Differences are forms, CAP and egg-move inheritance (#90 §1) |
| Sword/Shield (+DLC) | `sword-shield`: 44,204 (DLC groups empty) | main `learnsets.ts` `8x`: L 12,098 + M 31,576 + T 1,260 + E 1,645. DLC split only in snapshot mods `gen8dlc1` | equivalent; neither labels DLC rows (#90 §1) |
| Scarlet/Violet (+DLC) | `scarlet-violet`: 54,658 | main `9x`: L 13,732 + M 39,998 + E 1,829. Snapshots `gen9predlc`, `gen9dlc1` | equivalent. Showdown's snapshots can date a DLC move, PokéAPI cannot |
| Gen 3–7 pairs (RS/E/FRLG, DP/Pt/HGSS, BW/B2W2, XY/ORAS, SM/USUM) | one version group each | **one code per generation** in the main file; per-game state only in snapshot mods `gen3rs`, `gen3frlg`, `gen4pt`, `gen5bw1`, `gen6xy`, `gen7sm` | **PokéAPI.** Recovering Emerald, HGSS or B2W2 from Showdown would mean diffing snapshots, and not every game has one |
| Let's Go | `lets-go-…`: 5,776 | `mods/gen7letsgo/learnsets.ts` (113 KB) | not compared row by row |
| Champions | `champions`: 19,810 | `mods/champions/learnsets.ts` (348 KB) | not compared |

Showdown also has two kinds of fact PokéAPI lacks: event-only moves (`S`, with the event's level
and ball) and transfer-only moves (`V`). Both are tied to a generation, not to a game.

**Correction to #90 §2.1.** That note listed `legends-za` and `mega-dimension` among PokéAPI's
learnset version groups. It was read at PokéAPI `c8075719`. **At the pinned SHA `4b82c20` both
groups have zero rows.** Either they were added after the pin, or #90 listed groups rather than
rows. Not resolved here.

---

## 6. Summary by item

"Derivable" means a fact can be imported (Showdown) or read out by a tool (PKHeX) without hand
entry. Effort: **low** = fixed-offset bytes or a flat TypeScript table; **moderate** = a reader
per file or code-encoded rules; **high** = not available.

| Item | #88/#89 figure to curate | Derivable | Left to curate | Source file and field | Decode effort |
|---|---|---|---|---|---|
| SwSh out-of-dex species | 80 | **80** | 0 | PKHeX `personal_swsh` `IsPresentInGame` (0x21 bit 6) − dex indexes 0x5C/0xAC/0xAE; Showdown `mods/gen8/formats-data.ts` `isNonstandard` − PokéAPI dex union | low; moderate for Showdown (inheritance + code rules) |
| SV out-of-dex species | 69 | **69** | 0 | PKHeX `personal_sv` 0x1C − 0x1E/0x4A/0x4B; Showdown `data/formats-data.ts` | low |
| Rosters, other Gen 7–9 groups | (rule-based in #88) | all 8 checked match | 0 | as above, plus mods `gen7`, `gen7sm`, `gen7letsgo`, `gen8bdsp`, `gen8legends`, `gen9legends` | low to moderate |
| Regional form × game | 162 | **162** (Showdown); 146 cross-checked in PKHeX | 0 facts; one 58-row form-number → slug map | Showdown per-forme `isNonstandard`; PKHeX form entries via `FormStatsIndex` | low |
| Obtain method, BDSP/LA/SV/Z-A | 1,832 | ~1,797 at category level | ~35 conditional + 33 residual + gift/catch review of ~260 statics | PKHeX `legality/wild/Gen8|Gen9/*.pkl`, `Encounters8b/8a/9/9a.cs`, `mgdb/w*.pkl`; evolution from Showdown `prevo` or PokéAPI | moderate (one reader per file; C# literals) |
| Obtain method, Gen 1–8 non-catch | ~4,200 | not measured | unchanged | PKHeX has tables; not decoded | — |
| Events | (part of the above) | wondercard species per game | label choices (event versus transfer) | PKHeX `mgdb/*.pkl`; Showdown `eventData` (Cherish Ball), not game-tagged for SwSh/SV | low |
| Encounter locations and levels | 0 PokéAPI rows for 4 groups | location id, species, form, level range for all 4 | location names; DLC/area grouping | PKHeX wild pickles (§2) | moderate |
| Encounter rates | — | **none** (SV raid weights only) | all, if shown | — | high |
| Encounter conditions | — | SV time + weather; LA/Z-A alpha; LA outbreak/distortion type | BDSP swarm/radar/marsh; Z-A time/weather | PKHeX slot bytes | low |
| LA move table | 177 moves (138 differ) | PP (84 differences), move list | power ×3, accuracy ×2, category (3) | PKHeX `Moves/MoveInfo8a.cs` `PP`, `DummiedMoves` | trivial |
| Z-A move table | 340 moves | move list (339), Growth's type | cooldown, 33 power, Dark Void category, 1 move-count gap | PKHeX `Moves/MoveInfo9a.cs` `Type`, `DummiedMoves` | trivial |
| Showdown LA/Z-A move values | — | **none** (no `moves.ts`) | — | — | — |
| Learnsets, LA | PokéAPI tutors thin | ~2,900 extra tutor entries | none found | Showdown `mods/gen8legends/learnsets.ts` | low (+ CAP filter) |
| Learnsets, Z-A | PokéAPI 0 rows | 17,180 entries | none found | Showdown `mods/gen9legends/learnsets.ts` | low |
| Learnsets, Gen 3–7 per game | PokéAPI per version group | Showdown coarser | — | — | — |

---

## 7. Not verified

- **Gen 7 rosters and forms** (SM, USUM, LGPE) come from Showdown only. No PKHeX `personal_sm`,
  `personal_uu` or `personal_gg` cross-check was run.
- **BDSP and Z-A dex-index semantics** (§1.2): 150 versus 151, and 603 non-zero entries.
- **Z-A usable moves**: PKHeX 339 against Bulbapedia 340.
- **`GiftType` = 1** read as "Pokémon card". Inferred from the getters, not from the enum.
- **Gift/catch split**: a heuristic on fixed Poké Ball (§3.2). LA cannot be split this way.
- **`SlotType8b`** values (does BDSP encode swarm, radar or marsh as a type?) were not read.
- **SwSh and Gen 1–7 PKHeX encounter tables** were listed, not decoded.
- **PokéAPI Z-A learnsets**: is the 0 at `4b82c20` a pin artefact (§5)?
- **Showdown snapshot mods** (`gen3rs`, `gen4pt`, …): exactly which game state each one freezes
  was not checked file by file.
- **Licence:** reading facts out of GPL'd files over Nintendo data follows #90 §3–4. This note
  adds no legal analysis. Every PKHeX figure above was printed by a scratch script, and no PKHeX
  file is meant to sit in the repo.

---

## Appendix: reproducing these numbers

```bash
SD=942cda71d14bf0618bd57991708a2e2734b6daba
PK=66ef5983a5d349efa2dd615f2926a1b1ee97306e
S=4b82c204ddd19ecb8eda2ea044ccb59e222b721c
# Showdown: data/{pokedex,formats-data,learnsets,moves}.ts, sim/{dex,dex-species}.ts,
#   data/mods/{gen7,gen7sm,gen7letsgo,gen8,gen8bdsp,gen8legends,gen8dlc1,gen9legends,gen9dlc1,gen9predlc}/*
curl -sO https://raw.githubusercontent.com/smogon/pokemon-showdown/$SD/data/mods/gen9legends/formats-data.ts
# PKHeX: Resources/byte/personal/personal_*, Resources/legality/wild/Gen8|Gen9/encounter_*.pkl,
#   Resources/legality/mgdb/w{c8,b8,a8,c9,a9}.pkl, Legality/Encounters/Data/Gen8|Gen9/Encounters*.cs,
#   PersonalInfo/Info/PersonalInfo*.cs, Moves/MoveInfo8a.cs, MoveInfo9a.cs, PKM/Util/Conversion/SpeciesConverter.cs
curl -sO https://raw.githubusercontent.com/kwsch/PKHeX/$PK/PKHeX.Core/Resources/byte/personal/personal_sv
# PokéAPI: pokemon_dex_numbers, pokedex_version_groups, pokemon_moves, version_groups
curl -sO https://raw.githubusercontent.com/PokeAPI/pokeapi/$S/data/v2/csv/pokemon_moves.csv
```

Scratch scripts (session scratchpad `research/scripts98/`, not committed):

- `personal.ps1`: decodes the five personal tables (presence, dex indexes, forms).
- `sdroster.ps1`: resolves Showdown `isNonstandard` through mod inheritance and the code
  rules.
- `compare.ps1`: diffs the two rosters and counts regional forms.
- `regional.ps1`: counts PKHeX regional forms.
- `encounters.ps1`: decodes the wild pickles and wondercards.
- `obtain.ps1`: assigns obtain categories.

The LA PP check reuses the Bulbapedia wikitext parse from #89's `lacmp.ps1`.
