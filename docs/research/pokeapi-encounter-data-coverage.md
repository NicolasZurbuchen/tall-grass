# Does PokéAPI carry per-game encounter data at scale?

Research note for GitHub issue #2 (`NicolasZurbuchen/pokedex`).

- **Date of investigation:** 2026-09-12
- **Method:** live crawl of `pokeapi.co/api/v2` (all 1025 National Dex IDs), plus direct
  reads of PokéAPI's upstream source CSVs and issue tracker.
- **Status:** answered — with one blocking caveat for the default view.

---

## TL;DR

**Yes for Gen 1–8. Flatly no for Gen 9.**

PokéAPI's encounter data is richer than expected: methods, rarity, level ranges,
time-of-day, season, weather and story-progress conditions are all present, and
regional forms are cleanly separated. 846 of 1025 species (82.5%) return data.

But **Scarlet/Violet, Legends: Arceus, BDSP, Legends Z-A, Mega Dimension and
Champions have exactly zero encounter rows** — not thin, not partial, zero. This is
confirmed in the upstream source data, not just the API surface.

The design assumption that the Location screen defaults to "the most recent
generation" is therefore **unbuildable as specified**. The newest generation with
encounter data is **Gen 8 (Sword/Shield + both DLCs)**, and it happens to be the
single best-covered generation in the entire dataset.

Payload is a non-issue: 35 MiB raw, but **0.79 MiB gzipped**, or **0.40 MiB** after
trimming the redundant `url` fields.

---

## 1. What `GET /api/v2/pokemon/{id}/encounters` actually returns

The endpoint returns a **bare JSON array** (not an object, not paginated) of
`LocationAreaEncounter` objects. Note that PokéAPI's own docs do not give this
endpoint a dedicated schema section — the shape has to be read off live responses
and the `VersionEncounterDetail` / `Encounter` types under the Locations group
([docs](https://pokeapi.co/docs/v2#pokemon-location-areas)).

Structure, three levels deep:

```
[ LocationAreaEncounter ]                  // one per location area
  ├── location_area: NamedAPIResource
  └── version_details: [ ]                 // one per game version
        ├── version: NamedAPIResource
        ├── max_chance: integer
        └── encounter_details: [ ]         // one per distinct encounter row
              ├── min_level / max_level: integer
              ├── chance: integer          // rarity, percent
              ├── method: NamedAPIResource
              ├── condition_values: [ NamedAPIResource ]
              └── pokemon_details: object | null
```

### Real example — Wooloo (#831), Max Raid den

`GET /api/v2/pokemon/831/encounters`, first version detail of the
`galar-wild-area-max-dens-area` entry, truncated to one encounter row:

```json
{
  "location_area": {
    "name": "galar-wild-area-max-dens-area",
    "url": "https://pokeapi.co/api/v2/location-area/1383/"
  },
  "version_details": [
    {
      "version": { "name": "sword", "url": "https://pokeapi.co/api/v2/version/33/" },
      "max_chance": 50,
      "encounter_details": [
        {
          "min_level": 30,
          "max_level": 30,
          "chance": 50,
          "method": {
            "name": "max-raid",
            "url": "https://pokeapi.co/api/v2/encounter-method/61/"
          },
          "condition_values": [
            { "name": "max-den-rarity-special", "url": ".../encounter-condition-value/343/" },
            { "name": "max-den-rating-2-star",  "url": ".../encounter-condition-value/339/" }
          ],
          "pokemon_details": null
        }
      ]
    }
  ]
}
```

### Real example — Caterpie (#10), time-of-day split

Same endpoint shape, showing that time conditions are genuine separate rows rather
than a flag on one row:

```json
{
  "location_area": { "name": "johto-route-30-area", "url": ".../location-area/187/" },
  "version_details": [{
    "version": { "name": "gold", "url": ".../version/4/" },
    "max_chance": 85,
    "encounter_details": [
      { "min_level": 3, "max_level": 3, "chance": 30, "method": { "name": "walk" },
        "condition_values": [{ "name": "time-day" }] },
      { "min_level": 3, "max_level": 3, "chance": 30, "method": { "name": "walk" },
        "condition_values": [{ "name": "time-morning" }] }
    ]
  }]
}
```

### `pokemon_details` is present but nearly always null

A newer field carrying `{ min_perfect_ivs, always_shiny, never_shiny, is_alpha }`.
Measured across the full crawl: **non-null on 121 of 114,780 encounter rows (0.11%)**.
Treat it as optional decoration, never as a required field. Example (Bulbasaur gift
at the Master Dojo, Isle of Armor):

```json
"pokemon_details": {
  "min_perfect_ivs": null, "always_shiny": false,
  "never_shiny": true, "is_alpha": false
}
```

Source: measured over 1025 locally cached responses; see §5 for crawl method.

---

## 2. Version coverage — which games are actually there

This is best answered from **PokéAPI's upstream source data**, not the API, because
the source makes the zeroes unambiguous.
File: [`data/v2/csv/encounters.csv`](https://github.com/PokeAPI/pokeapi/blob/master/data/v2/csv/encounters.csv)
(117,127 rows), joined against `data/v2/csv/versions.csv`.

Rows per version, all 53 versions:

| version_id | version | encounter rows |
|---|---|---|
| 1–3 | red / blue / yellow | 891 / 891 / 877 |
| 4–6 | gold / silver / crystal | 2820 / 2820 / 3183 |
| 7–9 | ruby / sapphire / emerald | 1530 / 1527 / 1647 |
| 10–11 | firered / leafgreen | 2117 / 2117 |
| 12–14 | diamond / pearl / platinum | 4392 / 4392 / 4231 |
| 15–16 | heartgold / soulsilver | 6215 / 6215 |
| 17–18 | black / white | 2691 / 2691 |
| 19–20 | colosseum / xd | 83 / 104 |
| 21–22 | black-2 / white-2 | 3850 / 3850 |
| 23–24 | x / y | 1470 / 1470 |
| 25–26 | omega-ruby / alpha-sapphire | 302 / 302 |
| 27–28 | sun / moon | 891 / 890 |
| 29–30 | ultra-sun / ultra-moon | 1216 / 1216 |
| 31–32 | lets-go-pikachu / lets-go-eevee | 693 / 693 |
| **33–34** | **sword / shield** | **10796 / 10797** |
| **35, 50** | **the-isle-of-armor-sword / -shield** | **7150 / 7148** |
| **36, 51** | **the-crown-tundra-sword / -shield** | **5618 / 5571** |
| **37–38** | **brilliant-diamond / shining-pearl** | **0** |
| **39** | **legends-arceus** | **0** |
| **40–41** | **scarlet / violet** | **0** |
| **42–43, 52–53** | **the-teal-mask-\* / the-indigo-disk-\*** | **0** |
| 44–46 | red-japan / green-japan / blue-japan | 882 / 882 / 6 |
| **47–49** | **legends-za / mega-dimension / champions** | **0** |

**Sword/Shield alone accounts for ~21.6k rows — nearly 2× the next-best pair
(HeartGold/SoulSilver at 12.4k) and roughly 18% of the entire dataset.** Gen 8 is
the most thoroughly documented generation in PokéAPI, not the least.

### Coverage by generation (live crawl, National Dex #1–1025)

| Gen | Dex range | n | with data | empty | coverage |
|---|---|---|---|---|---|
| 1 | 1–151 | 151 | 151 | 0 | **100.0%** |
| 2 | 152–251 | 100 | 95 | 5 | 95.0% |
| 3 | 252–386 | 135 | 135 | 0 | **100.0%** |
| 4 | 387–493 | 107 | 96 | 11 | 89.7% |
| 5 | 494–649 | 156 | 146 | 10 | 93.6% |
| 6 | 650–721 | 72 | 68 | 4 | 94.4% |
| 7 | 722–809 | 88 | 74 | 14 | 84.1% |
| 8 | 810–905 | 96 | 81 | 15 | 84.4% |
| 9 | 906–1025 | 120 | **0** | **120** | **0.0%** |
| **Total** | 1–1025 | 1025 | **846** | 179 | **82.5%** |

The 179 empty species are: every Gen 9 species (#906–1025, 120 of them), the Hisui
forms at #899–905, and ~52 scattered legendaries/mythicals/starters that are never
wild encounters in any game (#154 Meganium, #493 Arceus, #647–649, #719–721, etc.).
That scattered set is **correct behaviour, not a gap** — those Pokémon genuinely
have no wild encounter, only evolution/gift/event acquisition.

Empty ID ranges, verbatim from the crawl:

```
154, 157, 160, 181, 233, 389, 392, 395, 398, 409, 411, 413-414, 474, 476,
489, 496, 499, 502, 512, 514, 516, 567, 647-649, 671, 719-721, 723-724,
726-727, 729-730, 740, 773, 790, 802, 804, 807-809, 811-812, 814-815,
817-818, 892-893, 899-1025
```

### Locations exist for Gen 9, but they are hollow

Worth knowing, because a naive check would suggest Paldea is supported:

- `GET /api/v2/region/paldea` → **84 locations**, `version_groups: [scarlet-violet, the-teal-mask, the-indigo-disk]`
- `GET /api/v2/location/mesagoza` → **`areas: []`** (zero location areas)
- `GET /api/v2/location/los-platos` → **`areas: []`**

So Paldea has place names but no `location-area` records, and encounters hang off
location areas. Hisui is one step further along and still useless:

- `GET /api/v2/location/aipom-hill` → 1 area, `aipom-hill-area`
- `GET /api/v2/location-area/aipom-hill-area` → **`pokemon_encounters: []`**

Contrast Galar, which is fully populated:

- `GET /api/v2/location/galar-route-2` → 3 areas
- `GET /api/v2/location-area/galar-route-2-main` → **13 `pokemon_encounters`**, versions `[sword, shield]`

---

## 3. Method, rarity and conditions — all present, and detailed

### Methods: 66 defined

`GET /api/v2/encounter-method?limit=100` → `count: 66`.

Every category the issue asks about exists as a first-class method:

- **walk / surf / fish** — `walk`, `surf`, `old-rod`, `good-rod`, `super-rod`, `surf-spots`, `super-rod-spots`, `feebas-tile-fishing`
- **raid** — `max-raid`, `dynamax-adventure`
- **gift** — `gift`, `gift-egg`
- **trade** — `npc-trade`
- plus `static`, `horde`, `sos`, `headbutt`, `rock-smash`, `honey-tree`, `hidden-grotto`, `berry-trees`, `overworld`/`overworld-water`/`overworld-flying`, `roaming-*`, `wanderer`, `snag`, `pokespot`, and ambush methods (`trash-can-ambush`, `ceiling-ambush`, `sky-ambush`, …)

Usage frequency across the crawl (114,780 encounter rows), top 15:

| method | rows |
|---|---|
| walk | 40,180 |
| **max-raid** | **22,463** |
| overworld | 7,758 |
| super-rod | 5,834 |
| wanderer | 5,306 |
| surf | 4,645 |
| good-rod | 3,734 |
| old-rod | 2,948 |
| overworld-water | 2,856 |
| grass-spots | 2,016 |
| dark-grass | 1,644 |
| headbutt | 1,488 |
| wanderer-water | 1,340 |
| cave-spots | 1,176 |
| static | 877 |

`max-raid` being the #2 method overall is another sign of how heavily Gen 8 is
represented.

### Rarity: present on every row

Two fields carry it:

- `chance` — per-encounter percentage
- `max_chance` — highest rate for that version/area

Upstream, `data/v2/csv/encounter_slots.csv` has **3,809 rows with a `rarity` column
populated on all 3,809** — there are no null rarities to defend against.

### Conditions: 30 conditions, 361 values

`GET /api/v2/encounter-condition` → `count: 30`;
`GET /api/v2/encounter-condition-value` → `count: 361`.

Conditions: `swarm, time, radar, slot2, radio, season, starter, tv-option,
story-progress, other, item, weekday, first-party-pokemon, special-encounter,
trade, coins, friend-safari-slot, great-marsh-daily-slot, honey-tree-group,
headbutt-tree, backlot, bug-catching-contest, save-data, alolan-diglett-found,
weather, johto-safari-blocks, max-den-rarity, max-den-rating, berry-tree-type,
trash-can-type`.

Both conditions the issue names explicitly are real and well-populated:

- **Time of day** — `time-day` (2,954 rows), `time-night` (2,956), `time-morning` (2,954)
- **Season** — 1,520 rows across `season-spring/summer/autumn/winter`, e.g. Dodrio (#84) in `unova-route-20-area` on `black-2` with `season-summer`

Most-used condition values overall:

| condition value | rows |
|---|---|
| max-den-rarity-common | 11,437 |
| max-den-rarity-rare | 11,008 |
| story-progress-hall-of-fame | 9,401 |
| story-progress-before-hall-of-fame | 8,711 |
| max-den-rating-5-star | 5,448 |
| max-den-rating-4-star | 5,428 |
| max-den-rating-3-star | 4,779 |
| max-den-rating-2-star | 3,998 |
| time-night / time-day / time-morning | 2,956 / 2,954 / 2,954 |
| weather-overcast | 2,624 |
| weather-intense-sun | 2,622 |
| weather-fog | 2,616 |
| weather-thunderstorm | 2,576 |
| weather-normal | 2,558 |
| weather-raining | 2,472 |
| weather-sandstorm | 2,100 |
| weather-snowing / -snowstorm | 1,706 / 1,673 |

**Weather is a bonus the issue didn't ask about** — 8 weather values, ~19k rows,
almost all Gen 8 Wild Area. If the Location screen ships a Gen 8 default, weather
is a natural filter axis.

One wrinkle for UI: condition values are **not namespaced consistently**. Some
carry their condition as a prefix (`time-day`, `season-summer`, `weather-fog`,
`max-den-rarity-common`), others do not (`backlot-not-mentioned`, seen in the
Pikachu payload below). Group by resolving each value's parent `condition` rather
than by string-prefixing the name.

---

## 4. Regional forms — cleanly distinguishable

**Yes, unambiguously.** Regional forms are separate `pokemon` resources with their
own IDs in the 10000+ range, and `/encounters` works on those IDs directly. There
is no need to disambiguate by location or heuristics.

Measured live:

| variety | pokemon id | areas | versions |
|---|---|---|---|
| `vulpix` | 37 | 42 | diamond, pearl, platinum, silver, soulsilver, lets-go-eevee, blue, leafgreen, green-japan, ruby, sapphire, emerald, omega-ruby, alpha-sapphire, black, white, black-2, white-2, sword, shield, xd, yellow |
| `vulpix-alola` | **10103** | 6 | lets-go-eevee, the-isle-of-armor-sword, the-isle-of-armor-shield, sun, ultra-sun |
| `meowth` | 52 | 24 | (17 versions) |
| `meowth-alola` | **10107** | 7 | lets-go-eevee, the-isle-of-armor-\*, sun, moon, ultra-sun, ultra-moon |
| `meowth-galar` | **10161** | 12 | sword, shield, the-isle-of-armor-\*, the-crown-tundra-\* |
| `meowth-gmax` | 10200 | 0 | — |
| `zigzagoon` | 263 | 34 | … |
| `zigzagoon-galar` | **10174** | 14 | sword, shield |
| `darumaka-galar` | **10176** | 17 | sword, the-crown-tundra-sword |
| `growlithe-hisui` | **10229** | **0** | — (Hisui gap) |

The Alolan Vulpix example from the issue resolves exactly as hoped: `vulpix-alola`
returns 6 areas across Alola and Isle of Armor, entirely disjoint from base Vulpix's
Gen 2/3/4/8 spread.

**Ingest implication:** iterating `1..1025` is not enough. `encounters.csv` contains
**81 distinct form IDs above 10000** that carry encounter data:

```
10008-10012, 10016, 10024-10025, 10027-10032, 10091-10092, 10100-10103,
10105-10110, 10112, 10114-10115, 10119, 10123-10126, 10151-10152,
10161-10164, 10166-10171, 10173-10177, 10179-10181, 10184, 10186,
10195-10198, 10201-10204, 10206-10207, 10212-10228
```

Total distinct `pokemon_id` in `encounters.csv`: **927** (846 base + 81 forms).
Enumerate varieties via `/api/v2/pokemon-species/{name}` → `varieties[]` rather than
hardcoding the list. Mega and Gigantamax forms (e.g. `raichu-mega-x` 10304,
`meowth-gmax` 10200) correctly return `[]`.

---

## 5. Payload size

Crawl method: 1,025 sequential-ish requests (concurrency 6) to
`/api/v2/pokemon/{id}/encounters`, responses cached to disk verbatim. All 1,025
returned HTTP 200. No rate limiting encountered.

| measurement | size |
|---|---|
| Raw JSON, all 1025 responses concatenated | **35.06 MiB** |
| Same, gzip -9 | **0.79 MiB** |
| Slimmed (drop every `url`, short keys), raw | 8.65 MiB |
| Slimmed, gzip -9 | **0.40 MiB** |
| Median response | 18.3 KB |
| Largest responses | #129 Magikarp 935 KB, #531 Audino 493 KB, #19 Rattata 451 KB, #113 Chansey 421 KB, #118 Goldeen 408 KB |

Aggregate structure: **16,447 location-area references** and **114,780 encounter
rows** across 846 species. (The upstream CSV's 117,127 rows is higher because it
includes the 81 form IDs the 1–1025 crawl skipped.)

**Verdict: size is not a risk.** The ~28× gzip ratio comes from the enormous
redundancy of repeated `url` strings — every `NamedAPIResource` carries a full
absolute URL that a pre-baked offline dataset has no use for. Dropping URLs and
interning `location_area` / `version` / `method` / `condition_value` names into
lookup tables would land comfortably **under 400 KB compressed**, which is nothing
for an app that already ships sprites.

Magikarp at 935 KB raw is the practical ceiling for a single-Pokémon response, and
only because it appears in nearly every water area in every game. If the Location
screen ever fetches live rather than from the baked dataset, that one is the
worst case to design around.

---

## 6. Gen 8 / Gen 9 — the decisive finding

### Sword/Shield: excellent

- Grookey (#810): 1 area, versions `[sword, shield]`, method `gift`
- Wooloo (#831): **18 areas**, 30,211 bytes, versions `[sword, shield, the-crown-tundra-sword, the-crown-tundra-shield]`, methods `[overworld, walk, max-raid]`
- Eternatus (#890): 1 area, `[sword, shield]`, method `static`
- Calyrex (#898): 1 area, `[the-crown-tundra-sword, the-crown-tundra-shield]`, method `static`

Base game and **both DLCs** are covered, and as of PokéAPI PR
[#1582](https://github.com/PokeAPI/pokeapi/pull/1582) (merged 2026-07-05) the DLC
versions are split per-game — `the-isle-of-armor-sword` vs `the-isle-of-armor-shield`
— so version-exclusive DLC encounters are distinguishable. Any older integration
guide referring to a single `the-isle-of-armor` version is stale.

### Scarlet/Violet: nothing

Every Gen 9 species returns `[]`:

```
906 Sprigatito:  areas=0 bytes=2
915 Lechonk:     areas=0 bytes=2
925 Maushold:    areas=0 bytes=2
1000 Gholdengo:  areas=0 bytes=2
1008 Miraidon:   areas=0 bytes=2
899 Wyrdeer:     areas=0 bytes=2      (Hisui)
```

Confirmed at source: `scarlet`, `violet`, `the-teal-mask-scarlet`,
`the-indigo-disk-scarlet`, `the-teal-mask-violet`, `the-indigo-disk-violet` all have
**0 rows** in `encounters.csv`. So do `brilliant-diamond`, `shining-pearl`,
`legends-arceus`, `legends-za`, `mega-dimension` and `champions`.

This is a known, long-standing, **still-open** gap in PokéAPI's own tracker:

| issue | title | opened | state |
|---|---|---|---|
| [#1018](https://github.com/PokeAPI/pokeapi/issues/1018) | Gen 9 Base Game Encounter Data Missing | 2024-01-22 | **open** |
| [#1019](https://github.com/PokeAPI/pokeapi/issues/1019) | Gen 9 DLC Encounter Data Missing | 2024-01-22 | **open** |
| [#1017](https://github.com/PokeAPI/pokeapi/issues/1017) | Hisui Encounter Data | 2024-01-22 | **open** |

Open for roughly 20 months, filed by the same reporter on the same day, with no
resolution — while in the same period the project *did* land substantial Gen 1–8
encounter fixes (#1386 hordes, #1412 Colosseum/XD, #1420 Friend Safari/Mirage Spots,
#1453 headbutt trees, #1475 Gen IV fossils, #1496 Great Marsh, #1522 Johto Safari
blocks, all closed in 2026). The maintainers are actively improving encounter data;
Gen 9 simply is not being worked. **Do not plan around it arriving.**

Also still open and worth knowing as known-thin areas: #1451 (missing trade
encounters), #1454 (missing shaking/rustling encounters in XY/SM/USUM), #1456
(ORAS missing surf/underwater/fishing — consistent with ORAS's low 302 rows),
#1458 (HGSS PokéWalker), #1115 (Gen 3 NPC trades).

---

## 7. Alternative sources and licensing

| Source | Data quality | Access | Licence | Usable? |
|---|---|---|---|---|
| **PokéAPI live API** | Gen 1–8 excellent, Gen 9 absent | Free REST, **no rate limit** since Nov 2018 static hosting | see below | **Yes — primary** |
| **PokéAPI source CSVs** (`PokeAPI/pokeapi` `data/v2/csv/`) | Identical data, no HTTP | `git clone` / raw.githubusercontent | BSD-3-Clause-style | **Yes — better for build-time bake** |
| **Bulbapedia** | Complete incl. Gen 9 | HTML scrape; MediaWiki API exists | **CC BY-NC-SA 2.5** | **Problematic** |
| **Serebii** | Complete incl. Gen 9 | HTML scrape only, no API | **All rights reserved** | **No** |

### PokéAPI licence

[`LICENSE.md`](https://github.com/PokeAPI/pokeapi/blob/master/LICENSE.md) is a
**BSD-3-Clause-style licence**:

> Copyright (c) © 2013–2023 Paul Hallett and PokéAPI contributors. Pokémon and
> Pokémon character names are trademarks of Nintendo. All rights reserved.
> Redistribution and use in source and binary forms, with or without modification,
> are permitted provided that the following conditions are met: […]

Three conditions: retain copyright notice in source redistributions; reproduce the
notice in documentation for binary redistributions; no endorsement use of the
PokéAPI name. **An app shipping a pre-baked dataset is a binary redistribution** —
so the copyright notice and disclaimer must appear in the app's about/licences
screen. That is the whole obligation. No share-alike, no non-commercial clause.

Fair-use policy from [the docs](https://pokeapi.co/docs/v2):

> "Locally cache resources whenever you request them. Be nice and friendly to your
> fellow PokéAPI developers."
>
> "Since the move to static hosting in November 2018, rate limiting has been removed
> entirely, but we still encourage you to limit the frequency of requests to limit
> our hosting costs."

A build-time bake is exactly the usage pattern they ask for. Even better: read the
CSVs from a cloned repo and skip their CDN entirely.

### Bulbapedia

[Bulbapedia:Copyrights](https://bulbapedia.bulbagarden.net/wiki/Bulbapedia:Copyrights)
— content is **CC BY-NC-SA 2.5** (2.0 for revisions before 2007-04-15). Three
problems for this project:

1. **NonCommercial** — bars any paid/ad-supported release, and arguably taints even
   a free app if commercialisation is ever contemplated.
2. **ShareAlike** — derivative works must carry the same licence, which would
   propagate CC BY-NC-SA onto the baked dataset and conflict with the app's own
   licensing.
3. **Attribution** — per-page attribution for ~1000 scraped pages.

Viable only as a **manual, human-curated fallback for a small number of entries**,
with attribution — not as a bulk ingest.

### Serebii

Footer: *"All Content is © Copyright of Serebii.net 1999-2026."* No API, no bulk
download, no stated reuse permission. **All rights reserved by default — do not
scrape or redistribute.**

### If Gen 9 is genuinely required later

Options, least-bad first:

1. **Contribute upstream.** Issues #1018/#1019 are open and unclaimed; PokéAPI
   accepts CSV PRs. This makes the data available under a licence that already works.
2. **Hand-author a small Gen 9 supplement** for the highest-traffic species, sourced
   from in-game observation rather than scraping, stored as project-owned data.
   This is what the design prototype already did for 15 Pokémon — it just does not
   scale to 120.
3. **Scrape Bulbapedia under CC BY-NC-SA** and accept the licence consequences.
   Only if the app will never be commercial.

---

## 8. Recommendation

**Build the Location feature. The data supports it.** But change the default-view
assumption.

1. **Default the Location screen to Sword/Shield, not Scarlet/Violet.** Gen 8 is
   both the newest generation with data *and* the richest one in the dataset
   (~21.6k base rows + ~25.5k DLC rows). The screen will look its best there,
   not worst.
2. **Ingest at build time from the source CSVs**, not the live API — no network
   flake, no rate-limit etiquette, reproducible builds, and it is the same data.
   Pin the commit.
3. **Iterate species varieties, not `1..1025`** — 81 regional-form IDs above 10000
   carry their own encounters, including every Alolan and Galarian form.
4. **Intern names into lookup tables and drop all `url` fields.** Target under
   400 KB compressed for the full dataset.
5. **Design the empty state as a first-class screen, not an error.** 179 of 1025
   species (17.5%) legitimately have no encounters — all of Gen 9, plus legendaries
   and starters that are never wild in any game. Copy should distinguish
   "not obtainable in the wild" from "we don't have data for this generation yet",
   because both are real and they mean different things to the user.
6. **Group conditions by resolving each value's parent `condition` resource**, not
   by string-prefixing the value name — prefixes are inconsistent
   (`time-day` vs `backlot-not-mentioned`).
7. **Ship the PokéAPI BSD notice** in the app's licences screen.

### Residual risk

Low. The single real risk is a product decision, not a data one: if "newest
generation by default" is non-negotiable for the Location screen, then either Gen 9
data must be sourced elsewhere (§7) or that screen needs a per-generation selector
that simply omits Gen 9. Everything else checks out.

---

## Appendix: reproducing these numbers

```bash
# Full-dex crawl (1025 requests, ~6 concurrent, responses cached to ./enc/)
#   GET https://pokeapi.co/api/v2/pokemon/{1..1025}/encounters

# Upstream source data (authoritative on the zeroes)
curl -O https://raw.githubusercontent.com/PokeAPI/pokeapi/master/data/v2/csv/encounters.csv
curl -O https://raw.githubusercontent.com/PokeAPI/pokeapi/master/data/v2/csv/encounter_slots.csv
curl -O https://raw.githubusercontent.com/PokeAPI/pokeapi/master/data/v2/csv/versions.csv

# Reference lists
curl https://pokeapi.co/api/v2/encounter-method?limit=100          # count: 66
curl https://pokeapi.co/api/v2/encounter-condition?limit=100       # count: 30
curl https://pokeapi.co/api/v2/encounter-condition-value?limit=400 # count: 361
curl https://pokeapi.co/api/v2/version?limit=200                   # count: 53
curl https://pokeapi.co/api/v2/location-area?limit=1               # count: 1539
curl https://pokeapi.co/api/v2/location?limit=1                    # count: 1104

# Spot-checks quoted above
curl https://pokeapi.co/api/v2/pokemon/831/encounters      # Wooloo — rich Gen 8
curl https://pokeapi.co/api/v2/pokemon/906/encounters      # Sprigatito — []
curl https://pokeapi.co/api/v2/pokemon/10103/encounters    # Vulpix-Alola
curl https://pokeapi.co/api/v2/location/mesagoza           # areas: []
curl https://pokeapi.co/api/v2/location-area/aipom-hill-area  # pokemon_encounters: []
```

### Primary sources cited

- PokéAPI v2 docs — <https://pokeapi.co/docs/v2>
- PokéAPI live API — <https://pokeapi.co/api/v2/>
- `PokeAPI/pokeapi` source CSVs — <https://github.com/PokeAPI/pokeapi/tree/master/data/v2/csv>
- `PokeAPI/pokeapi` LICENSE.md — <https://github.com/PokeAPI/pokeapi/blob/master/LICENSE.md>
- PokéAPI issues #1017, #1018, #1019, #1451, #1454, #1456, #1458, PR #1582
- Bulbapedia:Copyrights — <https://bulbapedia.bulbagarden.net/wiki/Bulbapedia:Copyrights>
- Serebii.net footer copyright notice — <https://www.serebii.net/>
