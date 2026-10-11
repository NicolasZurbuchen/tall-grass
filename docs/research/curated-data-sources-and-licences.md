# Where curated data can come from, and under what licence

Research note for GitHub issue #90 (`NicolasZurbuchen/tall-grass`), part of the game-lens map (#87).

- **Date of investigation:** 2026-10-11
- **Method:** primary files opened directly. PokéAPI and Pokémon Showdown data were downloaded
  from `master` (PokéAPI at `c8075719`, 2026-10-09) and parsed locally. PKHeX's binary SwSh
  personal table was decoded byte by byte. Bulbapedia, Serebii and PokémonDB were read in a real
  browser, because Bulbapedia returns 403 to curl and `WebFetch`. Statutes were read from Fedlex
  and legislation.gov.uk. Case law is cited from CJEU press material and commentary, not from the
  full judgments (see Known gaps).
- **Status:** answered for completeness. The licence part is answered as far as the law allows,
  with the uncertain points marked.

> **This is not legal advice.** It is a developer's reading of public licences, statutes and
> case summaries, written to decide what a hobby project can reasonably do. A Swiss or EU IP
> lawyer could disagree with any of it. Where the reading is uncertain, the note says so.

---

## TL;DR

1. **The Isle of Armor tutor gap in #87 is not a gap in PokéAPI's rows. It is a gap in their
   labelling.** All four sources checked (PokéAPI, Showdown, Bulbapedia, Serebii) agree with the
   game's own data (decoded from PKHeX) on the 62 species that learn **Grassy Glide** from the
   tutor. PokéAPI files those rows under `sword-shield`, though. Its `the-isle-of-armor` and
   `the-crown-tundra` version groups have **zero** learnset rows, so nothing says the move is
   DLC-only. Bulbapedia ("SwShIA") and Serebii ("By Tutor on the Isle of Armor") say it is.
2. **Only PokéAPI and Showdown carry a licence that lets the data ship.** PokéAPI is BSD-3 and
   Showdown is MIT. In both repos the licence is repo-wide and the data files sit inside it.
   Neither licence fixes provenance, though. PokéAPI's own history includes Legends: Arceus
   learnsets "scrapped from serebii.net", and several fixes sourced from Bulbapedia.
3. **Bulbapedia (CC BY-NC-SA 2.5)** can be *imported* only if the result is released under BY-NC-SA,
   with attribution and no commercial use. That would make the dataset non-free in the OSI sense
   and incompatible with a BSD/MIT/Apache dataset licence. **Serebii and PokémonDB** grant nothing,
   and PokémonDB says explicitly *"In general, no."*
4. **PKHeX (GPL-3.0)** is the most complete source for Gen 8–9 per-game data, Z-A included. Its
   tables are Nintendo's own game files, repackaged. The GPL does not cleanly cover them, and they
   are a poor thing to commit to a public repo.
5. **Facts do not carry the page's licence. Selection and arrangement can, and in the EU so can
   the investment behind them.** Re-entering a learnset fact by fact, as a check, is the safest
   position. Bulk-copying a whole table from a site is where the uncertainty lives. Switzerland has
   **no** database right, but its Unfair Competition Act (Art. 5 lit. c) covers "technical
   reproduction" of another's market-ready work. Shipping on Google Play brings the EU rule in.

---

## 1. Spot-check: Grassy Glide from the Isle of Armor tutor

Grassy Glide (move 803) was introduced in *The Isle of Armor* and is taught only by a tutor there.
It is TM194 in Scarlet/Violet. The same fact was read from every source:

| Source | Where | Present? | Marked as DLC? | Learners (Gen 8 tutor) |
|---|---|---|---|---|
| **Game data** (via PKHeX) | `personal_swsh`, `TutorArmor` bitfield at `0xA8`; move 803 is index 4 of `SpecialTutorMoves` | yes | yes, an 18-move "Armor" tutor field that is separate from the base-game type tutors | 62 species + 9 form entries |
| **PokéAPI** | `pokemon_moves.csv`, `version_group_id=20` (`sword-shield`), method 3 (`tutor`) | yes | **no**: filed under the base game; `the-isle-of-armor` (vg 21) has 0 learnset rows | 62 species + 8 forms = 70 rows |
| **Showdown** | `data/learnsets.ts`, `grassyglide: ["8T"]` / `["9M", "8T"]` | yes | **no** in the main file. The `data/mods/gen8dlc1` snapshot exists, but it is a format snapshot, not a DLC label | 78 entries = 62 species + 2 Pumpkaboo/Gourgeist + **14 CAP fakemon** |
| **Bulbapedia** | [Grassy Glide (move)](https://bulbapedia.bulbagarden.net/wiki/Grassy_Glide_(move)), Learnset, column "SwShIA Tutor" | yes | **yes**: "Move Tutor in The Isle of Armor" | 63 rows (62 species + Alolan Exeggutor; forms collapsed to "All forms") |
| **Serebii** | [attackdex-swsh/grassyglide](https://www.serebii.net/attackdex-swsh/grassyglide.shtml), "Pokémon That Learn Grassy Glide By Tutor on the Isle of Armor" | yes | **yes** | 62 species (+ Alolan Exeggutor row) |
| **PokémonDB** | [wooloo/moves/8](https://pokemondb.net/pokedex/wooloo/moves/8), "Move Tutor moves… in Pokémon Sword & Shield" | yes | **no** | not counted. The move page lists only current-game (SV) TM learners |

The species sets are **identical** across all six. The remaining differences are about forms:

- PokéAPI and the game list Pumpkaboo/Gourgeist sizes as separate entries. Bulbapedia and Serebii
  collapse them. Showdown keeps one entry per species and inherits forms.
- The game's ninth form entry that PokéAPI lacks is internal species 421, Cherrim, in a form slot.
  That is the battle-only Sunshine form, which PokéAPI gives no moves. It is correct to drop.
- **Showdown's learnsets include CAP (Create-A-Pokémon) species**: Brattler, Caribolt, Embirch,
  Necturna and others. An importer must filter on `isNonstandard: "CAP"` in `pokedex.ts`, or
  invented Pokémon will appear in the dex.

Across all Gen 8 tutor moves, PokéAPI holds 1,171 rows and Showdown 1,260. Every move with a
difference above 3 is one of the 18 Armor tutor moves, and the differences match the CAP and
form-naming pattern above. **No source has more real Isle of Armor tutor learners than PokéAPI.**

**What #87 probably saw:** the rows are there, but nothing in PokéAPI marks them as Isle of Armor
rows. That matters for the lens, which folds the DLC in. For a "this was added by the DLC" note,
PokéAPI can't answer. For "can Wooloo learn it in Sword/Shield", it answers correctly.

> Comparing raw totals between PokéAPI and Showdown is not meaningful for other methods. Showdown
> stores egg moves only on the lowest evolution stage and lets forms inherit, so its 9E total
> (1,829) sits well below PokéAPI's SV egg rows (2,659) by construction, not because of missing
> data.

---

## 2. Completeness by source

### 2.1 PokéAPI (`PokeAPI/pokeapi`, `data/v2/csv/`)

| Area | Coverage |
|---|---|
| Per-game learnsets | Per **version group**: Gen 1–9 plus `legends-arceus`, `brilliant-diamond-shining-pearl`, `legends-za`, `mega-dimension`, `champions`. DLC groups (`the-isle-of-armor`, `the-crown-tundra`, `the-teal-mask`, `the-indigo-disk`) exist, but SwSh and SV **file DLC moves under the base group**. Known open gaps: [#603](https://github.com/PokeAPI/pokeapi/issues/603) (Gen 7 tutor moves) and [#1316](https://github.com/PokeAPI/pokeapi/issues/1316) (Hisui tutor moves) |
| Rosters | `pokedexes.csv`, `pokemon_dex_numbers.csv`, `pokedex_version_groups.csv`: regional dexes per version group. Nothing models "Also in this game" (what a game can hold outside its dexes) |
| Encounters | Gen 1–8 excellent. **0 rows** for BDSP, Legends: Arceus, SV + DLCs, Z-A, Mega Dimension and Champions, re-measured on 2026-10-11 (see `pokeapi-encounter-data-coverage.md`; issues #1017–#1019 are still open) |
| Forms | `pokemon_forms.csv` and related tables; forms as separate `pokemon` ids (10000+) |
| Past values | `pokemon_types_past.csv`, `pokemon_stats_past.csv`, `pokemon_abilities_past.csv`, `move_changelog.csv`, `type_efficacy_past.csv`, `ability_changelog.csv` |

### 2.2 Pokémon Showdown (`smogon/pokemon-showdown`, `data/`)

| Area | Coverage |
|---|---|
| Per-game learnsets | Main `learnsets.ts` (3.7 MB) is **per generation**, not per game: `8T` = "tutor in Gen 8", with no split between SwSh and its DLC. In Gen 7 it does not split SM from USUM. Per-game snapshots exist as mods: `gen8bdsp`, `gen8legends` (LA), `gen9legends` (Z-A), `gen8dlc1`, `gen9predlc`, `gen9dlc1`, `gen7letsgo`, `gen3frlg`, `gen3rs`, `gen4pt`, `gen5bw1`, `gen6xy`, `gen7sm`, `champions`. Event distributions are encoded as `S` sources |
| Rosters | None as dexes. Mod `formats-data.ts` marks what is unavailable (`isNonstandard: "Past"`), which approximates "can this game hold it". No regional dex numbers |
| Encounters | **None.** Showdown is a battle simulator |
| Forms | Rich: `formes`, `cosmeticFormes`, `battleOnly`, `changesFrom` in `pokedex.ts`, plus `data/FORMES.md` |
| Past values | Yes: each `data/mods/genN/` overrides stats, types, moves and the type chart for that generation |

### 2.3 Bulbapedia

Per-game learnsets with DLC marked (the "SwShIA" column above), per-game rosters (for example
"List of Pokémon by Paldea Pokédex number", with sections for compatible Pokémon outside any SV
dex), encounters for every game including Gen 9 (per the earlier encounter note; not re-checked
here), forms, and past values in prose ("Grassy Glide's base power was lowered to 60, …
then to 55"). It is the most complete human-readable source. It is HTML or wikitext, not a
dataset.

### 2.4 Serebii

Per-game learnsets with DLC marked, per-game dexes and location pages, Gen 9 included. HTML only,
with no API (see `pokemon-news-api.md` §2.3).

### 2.5 PokémonDB

Per-generation learnset pages split by game ("Sword & Shield", BDSP and LA on one Gen 8 page),
without DLC marking. Its [about page](https://pokemondb.net/about) credits its data to Project
Pokémon, the SV datamines by slp32, and Veekun. It is a *downstream* source, not an independent
one. The FAQ answers "Do you have a downloadable database or API?" with *"currently we do not offer
any database or API"* and points to Veekun and PokéAPI.

### 2.6 Community datamines

- **PKHeX** (`kwsch/PKHeX`, by Kaphotics): `PKHeX.Core/Resources/` carries per-game binary tables.
  These are `lvlmove_*.pkl` and `eggmove_*.pkl` for every game through `za`, plus `personal_*` for
  SwSh, BDSP, LA, SV and Z-A. The personal tables include presence-in-game flags and Galar, Armor
  and Crown dex indexes. `legality/wild/Gen8/` and `Gen9/` hold encounter tables for BD/SP
  (including the Underground), LA, Paldea, Kitakami, Blueberry and Z-A (`encounter_za.pkl`,
  `encounter_hyperspace_za.pkl`). **This is the only source checked here with Gen 9, BDSP, LA and
  Z-A encounter data in machine-readable form.** It is built for legality checking. It is not
  documented as a dataset, and its fields (whether it carries rates and conditions) were not
  decoded here.
- **pkNX** (`kwsch/pkNX`): a ROM editor that produces the dumps. GPL-3.0.
- **Project Pokémon**: the upstream credited by PokémonDB. **Its site returned a Cloudflare 502
  and its terms could not be read** (see Known gaps).
- **pret disassemblies** (`pret/pokered`, `pret/pokeemerald`) hold Gen 1–3 game data with **no
  licence file** at all.

---

## 3. Licences, source by source

### PokéAPI: BSD-3, repo-wide, with mixed provenance

[`LICENSE.md`](https://github.com/PokeAPI/pokeapi/blob/master/LICENSE.md) (GitHub detects it as
`BSD-3-Clause`) speaks of "source and binary forms". There is no separate data licence, and
`data/v2/` has no LICENSE file of its own, so the CSVs are covered by the repo licence. The
original CSVs came from Veekun, whose [`LICENSE`](https://github.com/veekun/pokedex/blob/master/LICENSE)
is MIT. The obligation when shipping: reproduce the notice in the app's licences screen.

**The licence does not settle provenance.** PokéAPI's history shows rows taken from the sites this
note treats as off-limits:

- [`8453c311`](https://github.com/PokeAPI/pokeapi/commit/8453c31155772e7b79ba22a4e272eac8eb41f434)
  (2024-10-11): *"add legends pokemon arceus pokemon moves scrapped from serebii.net"*. It touches
  `pokemon_moves.csv`.
- [`e091bbc2`](https://github.com/PokeAPI/pokeapi/commit/e091bbc2) and
  [`a0a7f38c`](https://github.com/PokeAPI/pokeapi/commit/a0a7f38c): base experience, happiness and
  hatch counters corrected "based on Bulbapedia data".
- [PR #1600](https://github.com/PokeAPI/pokeapi/pull/1600) (Isle of Armor encounters): the
  contributor checked trades "on Bulbapedia".

PokéAPI cannot grant rights it did not hold. Whether that matters depends on §4: if the copied
material is facts, nothing was owed in the first place.

### Pokémon Showdown: MIT, repo-wide

[`LICENSE`](https://github.com/smogon/pokemon-showdown/blob/master/LICENSE) is MIT, "Copyright (c)
2011-2026 Guangcong Luo and other contributors" (GitHub: `MIT`). The README says *"Pokémon
Showdown's server is distributed under the terms of the MIT License"*. The licence covers "this
software and associated documentation files". `data/` has no carve-out and no separate licence, so
the reading that MIT covers `data/learnsets.ts` is the natural one. The repackaged
[`pkmn/ps`](https://github.com/pkmn/ps) (`@pkmn/data`) is MIT too. The obligation is to reproduce
the notice. Provenance is not documented per file. Showdown's data is community research and
datamining, so the same caveat as PokéAPI applies.

### Bulbapedia: CC BY-NC-SA 2.5 (2.0 for older revisions)

From [Bulbapedia:Copyrights](https://bulbapedia.bulbagarden.net/wiki/Bulbapedia:Copyrights),
read 2026-10-11: original content is licensed BY-NC-SA. Revisions after 2007-04-15 are 2.5,
earlier ones are 2.0, and articles taken from Wikipedia are GFDL. The page's own summary is three
conditions: attribute, not for commercial purposes, and *"the resulting work is also licensed under
the same copyright"*.

What that would require **if** the dataset were a licensed adaptation:

| Term | What it would mean here |
|---|---|
| **BY** | Credit Bulbapedia, and in principle each page used, in the app and in the repo |
| **NC** | No commercial use, by the author *or anyone downstream*. The app being free and ad-free today is fine. But a fork that adds ads, or a later paid version, would breach it. And NC data cannot be relicensed as BSD/MIT |
| **SA** | The adapted dataset, the committed JSON, must itself be BY-NC-SA 2.5 (or a compatible later version). The app's *code* need not be. But the dataset could no longer be offered under the same licence as the rest of the repo, and anyone mixing it with PokéAPI or Showdown data would produce a BY-NC-SA whole |

**The licence only bites where copyright (or a database right) does.** CC's own FAQ says
*"CC licenses do not contractually impose restrictions on uses of a work where there is no
underlying copyright"* ([CC FAQ](https://creativecommons.org/faq/)). The
[CC Data page](https://wiki.creativecommons.org/wiki/Data) says *"Facts are not subject to
copyright"*, and that using facts from a licensed database "will not require attribution" unless
the use touches copyright in the database's structure. CC 2.5 also does not license sui generis
database rights: the unported 3.0 licences "do not expressly license sui generis database rights",
and only 4.0 does. So a 2.5 licence on Bulbapedia's text says nothing either way about any EU
database right in its tables. See §4 for why that is mostly moot.

### Serebii: all rights reserved

The footer reads *"All Content is © Copyright of Serebii.net 1999-2026"*. There is no terms page,
licence or permissions page, as before (`pokemon-news-api.md` §2.3). The operator is UK-based, so
a UK database right is possible (§4.3).

### PokémonDB: no, explicitly

From the [about page FAQ](https://pokemondb.net/about), under "Can I use some content from your
site?": *"In general, no."* The FAQ says the written content, design and "data display/layouts"
took a great deal of research, and asks people not to steal it. In the same FAQ, PokémonDB says it
is *"unable to make a Pokémon app as Nintendo/TPCi gets them taken down from all app stores"*. That
is a separate risk from data licensing, but a real one for Tall Grass (see also the brand-usage
point in `pokemon-news-api.md` §2.2).

### PKHeX and pkNX: GPL-3.0, over data that is not theirs

`kwsch/PKHeX` ships a GPL-3.0 `LICENSE` (the README badge says GPLv3; GitHub reports
`NOASSERTION` because the file is formatted unusually). `kwsch/pkNX` is `GPL-3.0`. Two problems
for a dataset:

1. **The tables are Nintendo's own data structures**, carried byte for byte (the `personal_swsh`
   layout above *is* the game's personal table). The GPL can license only what Kaphotics wrote.
   It cannot license Game Freak's files. Committing those binaries to a public repo means
   redistributing extracted game files, whatever one concludes about the facts inside them.
2. If the files were treated as GPL'd, shipping them inside the app would raise the question of
   whether the app is a GPL "covered work". That is unresolved, and not worth inheriting.

Reading the facts out of them (*"Wooloo can learn move 803 from the Armor tutor"*) and writing
them into the project's own format is a different act. It is the same as reading them from a
page, and §4 applies.

---

## 4. Do copied facts carry the page's licence?

### 4.1 The general rule: facts no, expression and selection maybe

- **United States:** *Feist Publications v. Rural Telephone Service*, 499 U.S. 340 (1991). Facts
  are not copyrightable. A compilation is protected only in its original selection or arrangement,
  and "sweat of the brow" earns nothing. Bulbagarden is generally understood to be US-run (not
  verified here), so this is likely the rule for Bulbapedia's own claim.
- **Switzerland:** the Copyright Act protects "literary and artistic intellectual creations with
  individual character" (Art. 2(1) CopA, SR 231.1). Collections are protected *"insofar as they are
  intellectual creations with individual character with regard to their selection and
  arrangement"* (Art. 4(1)). The Act contains no database right. A full-text search of the Fedlex
  English text for "database" finds nothing.
- **EU, copyright:** Database Directive 96/9/EC Art. 3(1): databases are protected by copyright
  "by reason of the selection or arrangement of their contents". Art. 3(2): that protection
  *"shall not extend to their contents"*. *Football Dataco v Yahoo* (C-604/10, 2012) holds that
  skill and labour do not count; only original selection or arrangement does.

**Applied here.** "Wooloo learns Grassy Glide by tutor in Sword/Shield" is a fact about a game.
Copying it carries no copyright licence. Which Pokémon learn a move is decided by the game, not
selected by the wiki, so the selection is not the wiki's. The arrangement (the table layout, the
column grouping, the prose) can be protected, and a scrape that reproduces that arrangement or
the prose is where copyright, and so CC's NC/SA, would attach. **A dataset written in the
project's own schema, holding facts only, takes no copyright from any page it was checked
against.** That is the well-supported part.

### 4.2 The EU sui generis database right: the uncertain part

Directive 96/9/EC Art. 7(1) gives the maker of a database a right *"to prevent extraction and/or
re-utilization of the whole or of a substantial part"* of its contents, where there was *"a
substantial investment in either the obtaining, verification or presentation of the contents"*.
Under Art. 7(4) the right applies *"irrespective of eligibility of the contents … for protection
by copyright"*: facts included. Art. 7(5) also bars *"repeated and systematic extraction … of
insubstantial parts"*, so scraping one page at a time does not avoid it. Term: 15 years, renewed by
any substantial new investment (Art. 10(1), 10(3)). A wiki that is constantly updated plausibly
never expires.

Three points cut it down:

1. **Who gets it.** Art. 11(1): only makers who are *"nationals of a Member State or who have their
   habitual residence in the territory of the Community"*, or EU companies (Art. 11(2)). A US-run
   Bulbapedia does not qualify. A UK-run Serebii lost EU eligibility for databases made after
   1 January 2021, though pre-2021 rights survive under the Withdrawal Agreement. The UK has its
   own mirrored right ([gov.uk guidance](https://www.gov.uk/guidance/sui-generis-database-rights)).
2. **Which investment counts.** *British Horseracing Board v William Hill* (C-203/02, 2004): only
   resources used to *seek out and collect existing* materials count, not resources used to
   *create* them ([CJEU press release](https://curia.europa.eu/en/actu/communiques/cp04/aff/cp040089en.pdf)).
   Learnsets exist in the game before the wiki records them, so a wiki's effort is plausibly
   "obtaining and verification". This point probably *favours* the wiki.
3. **Where it bites.** *Football Dataco v Sportradar* (C-173/11, 2012): re-utilisation happens
   where the act *targets* the public, not where the server sits. An app on Google Play available
   in the EU, in EU languages, plausibly targets EU users. So a Swiss developer is not outside
   the EU right simply by living in Switzerland, as long as the maker qualifies.

**Contract fills the gap where no right exists.** *Ryanair v PR Aviation* (C-30/14, 2015): for a
database with neither copyright nor sui generis protection, the Directive does not stop the owner
from imposing contractual limits, subject to national law. That matters only if there is a
contract. Bulbapedia's licence is a licence, not a click-through. Serebii has no terms.
PokémonDB's FAQ is a request on a page nobody agrees to, and is unlikely to be a binding contract.

### 4.3 Switzerland: no database right, but unfair competition

Switzerland never adopted a sui generis right. Its functional substitute is the Unfair Competition
Act (SR 241). Art. 5 lit. c makes it unfair to *"take over and exploit another person's work
product that is ready for the market by means of technical reproduction processes without any
reasonable effort of their own"*.

- **"Technical reproduction … without reasonable effort of their own"** describes a scraper
  exactly. It describes a developer re-keying facts, checking them against the game, and merging
  several sources much less well.
- The Act's general clause (Art. 2) frames unfairness around *"the relationship between competitors
  or between suppliers and customers"*. **Whether a non-commercial hobby app competes with
  Bulbapedia or Serebii in the Act's sense is genuinely uncertain.** An ad-free app is still a
  market substitute for an ad-funded site's traffic, which is one argument that it does. The note
  takes no view on how a Swiss court would decide.

### 4.4 What this means in practice

| Act | Copyright (CH, US, EU) | EU/UK database right | Swiss UWG 5(c) | Verdict |
|---|---|---|---|---|
| Read a fact on Bulbapedia or Serebii and check one's own row against it | none | none: verification is not extraction | none | **Safe** |
| Re-key a handful of facts from a page into the project's schema | none: facts | an insubstantial part; risky only if "repeated and systematic" | no technical reproduction | **Low risk** |
| Scrape every learnset table from a wiki into JSON | arrangement possibly reproduced | **substantial part**, if the maker qualifies | **squarely "technical reproduction"** | **Real risk**, plus NC/SA if copyright attaches |
| Import PokéAPI or Showdown files | licensed (BSD/MIT) | licensed | licensed | **Safe**, with the notice; provenance caveat |
| Commit PKHeX's `.pkl` or game binaries | Nintendo's files | — | — | **Avoid**; read facts out instead |

**Uncertain, stated plainly:**

- Whether a site's *arrangement* survives when facts are re-keyed into a different schema. It
  probably does not, but there is no case on wiki learnset tables.
- Whether Bulbapedia's maker qualifies for any EU right (US-run, not verified), and whether a UK
  right in Serebii's pre-2021 tables is enforceable against an app distributed from Switzerland.
- Whether UWG Art. 5(c) reaches a non-commercial app.
- Whether Nintendo would treat a learnset table as protectable expression of the game. That has
  not been tested, and Nintendo's track record with fan *apps* is takedowns on trademark and
  artwork grounds, not data (PokémonDB's FAQ).

---

## 5. Comparison

Recommendation-neutral. "Ship" means the data can be imported into a dataset committed to a public
repo and bundled in a free app. "Verify" means read and checked against only.

| | PokéAPI | Showdown `data/` | Bulbapedia | Serebii | PokémonDB | PKHeX / pkNX |
|---|---|---|---|---|---|---|
| **Per-game learnsets** | per version group; DLC folded into base | per **generation**; per-game only via mods | per game, DLC marked | per game, DLC marked | per game, DLC unmarked | per game (game files) |
| **IoA tutor (Grassy Glide)** | ✔ 62 sp., not DLC-marked | ✔ 62 sp. + 14 CAP | ✔ "SwShIA" | ✔ "Isle of Armor" | ✔ unmarked | ✔ `TutorArmor` bitfield |
| **Per-game rosters** | regional dexes | availability flags only | full, incl. "outside any dex" | full | per game | presence flags + dex indexes |
| **Encounters, Gen 9 / BDSP / LA / Z-A** | **none** | **none** | yes | yes | partial (not checked) | yes (binary, undocumented) |
| **Forms** | yes (separate ids) | rich (battle-only, cosmetic) | yes | yes | yes | yes (internal ids) |
| **Past values** | `*_past.csv`, changelogs | per-gen mods | prose | per-gen pages | per-gen pages | per-game tables |
| **Format** | CSV | TypeScript objects | wikitext/HTML | HTML | HTML | binary `.pkl` |
| **Licence** | BSD-3 (repo) | MIT (repo) | CC BY-NC-SA 2.5 | © all rights reserved | © "In general, no" | GPL-3.0 over Nintendo data |
| **Ship or verify** | **ship** | **ship** | **verify**; ship only as BY-NC-SA | **verify** | **verify** | **verify**: read facts, don't commit files |
| **Provenance caveat** | partly from Serebii/Bulbapedia | undocumented | — | — | from Project Pokémon / slp32 / Veekun | extracted game files |

---

## 6. Sources

Primary, read 2026-10-11:

- `PokeAPI/pokeapi`: [LICENSE.md](https://github.com/PokeAPI/pokeapi/blob/master/LICENSE.md);
  `data/v2/csv/{pokemon_moves,moves,pokemon,version_groups,pokemon_move_methods,encounters}.csv`
  at `c8075719`; commits `8453c311`, `e091bbc2`, `a0a7f38c`; PR #1600; issues #603, #1316,
  #1017–#1019
- `veekun/pokedex` [LICENSE](https://github.com/veekun/pokedex/blob/master/LICENSE) (MIT)
- `smogon/pokemon-showdown`: [LICENSE](https://github.com/smogon/pokemon-showdown/blob/master/LICENSE),
  README "License"; `data/learnsets.ts`; `data/mods/{gen8dlc1,gen8bdsp,gen8legends,gen9legends}/learnsets.ts`
- `pkmn/ps` repo licence (MIT)
- `kwsch/PKHeX`: LICENSE (GPL-3.0); `PKHeX.Core/PersonalInfo/Info/PersonalInfo8SWSH.cs`
  (`TutorArmor = 0xA8`, `SpecialTutorMoves`); `PKHeX.Core/Resources/byte/personal/personal_swsh`
  (decoded); resource tree listing. `kwsch/pkNX` licence (GPL-3.0)
- [Bulbapedia:Copyrights](https://bulbapedia.bulbagarden.net/wiki/Bulbapedia:Copyrights);
  [Grassy Glide (move)](https://bulbapedia.bulbagarden.net/wiki/Grassy_Glide_(move))
- [Serebii AttackDex SwSh: Grassy Glide](https://www.serebii.net/attackdex-swsh/grassyglide.shtml) and footer
- [PokémonDB about/FAQ](https://pokemondb.net/about);
  [Wooloo Gen 8 moves](https://pokemondb.net/pokedex/wooloo/moves/8);
  [Grassy Glide](https://pokemondb.net/move/grassy-glide)
- Swiss Copyright Act, [SR 231.1](https://www.fedlex.admin.ch/eli/cc/1993/1798_1798_1798/en), Art. 2, 4
- Swiss Unfair Competition Act, [SR 241](https://www.fedlex.admin.ch/eli/cc/1988/223_223_223/en), Art. 2, 5
- Directive 96/9/EC, Art. 3, 7, 10, 11, [as adopted](https://www.legislation.gov.uk/eudr/1996/9/contents)
  (legislation.gov.uk copy; EUR-Lex served a JS shell)
- [gov.uk: Sui generis database rights](https://www.gov.uk/guidance/sui-generis-database-rights)
- [Creative Commons FAQ](https://creativecommons.org/faq/); [CC wiki: Data](https://wiki.creativecommons.org/wiki/Data)

Secondary (case law, read from press releases and commentary):

- *Feist v. Rural*, 499 U.S. 340 (1991)
- C-203/02 *BHB v William Hill*: [CJEU press release 89/04](https://curia.europa.eu/en/actu/communiques/cp04/aff/cp040089en.pdf)
- C-604/10 *Football Dataco v Yahoo*
- C-173/11 *Football Dataco v Sportradar*: [Kluwer Copyright Blog](https://legalblogs.wolterskluwer.com/copyright-blog/football-dataco-ii-re-utilisation-must-be-interpreted-broadly/),
  [Fieldfisher](https://fieldfisher.com/en/insights/court-of-justice-confirms-that-you-cant-avoid-sui-generis-database-rights-by-locating-a-server-outside-of-the-jurisdict)
- C-30/14 *Ryanair v PR Aviation*: [Kluwer Copyright Blog](https://legalblogs.wolterskluwer.com/copyright-blog/ryanair-ltd-v-pr-aviation-bv-contracts-rights-and-users-in-a-low-cost-database-law/),
  [IPPT judgment PDF](https://www.ippt.eu/sites/ippt/files/2015/IPPT20150115_ECJ_Ryanair_v_PR_Aviation.pdf)

### Known gaps

- **Project Pokémon's terms could not be read**: Cloudflare 502, and a later navigation was refused.
  Its role here rests on PokémonDB's credit line.
- CJEU judgments were not read in full. The holdings above match across the press release and
  several commentaries, but are not quoted from the operative text. *Football Dataco v Yahoo* is
  cited from memory of the holding and was not re-fetched.
- Bulbagarden's legal seat, and so whether it qualifies for any EU database right, was not
  verified.
- Gen 9 / Legends encounter coverage on Bulbapedia, Serebii and PokémonDB was not re-checked page
  by page. It carries over from `pokeapi-encounter-data-coverage.md` §7. PKHeX's encounter `.pkl`
  files were listed but not decoded, so whether they carry rates and conditions is unknown.
- PKHeX's ninth Grassy Glide form entry was identified as internal species 421 (Cherrim) from the
  `Species` field. That this is the Sunshine form is inferred, not confirmed.
- No Swiss case law on UWG Art. 5(c) and scraped databases was checked.
