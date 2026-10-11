# Which Pokémon each game can hold, and how

Research note for GitHub issue #88 (`NicolasZurbuchen/tall-grass`), part of the game-lens map (#87).

- **Date of investigation:** 2026-10-11
- **Method:** Bulbapedia's per-game availability matrix, read from the rendered page and
  aggregated in the browser (Bulbapedia sits behind a Cloudflare challenge that blocks plain
  HTTP fetches); Bulbapedia's dex, Mega Evolution, Primal Reversion and Gigantamax articles;
  Serebii's per-game dex and "transfer only" pages; Nintendo and press coverage of HOME 4.0.0;
  PokéAPI CSVs at pinned SHA `4b82c204ddd19ecb8eda2ea044ccb59e222b721c`, compared with
  PowerShell `Import-Csv`.
- **Scope:** the 22 main-series version groups from Red/Blue to Legends: Z-A, with DLC
  folded in (Isle of Armor and Crown Tundra into Sword/Shield, Teal Mask and Indigo Disk into
  Scarlet/Violet, Mega Dimension into Z-A). Colosseum, XD and Champions are left out.
- **Status:** answered. Counts are firm for every group. Per-entry obtain methods are firm at
  category level, but Bulbapedia's licence means they can be checked against, not imported.

---

## TL;DR

**Most of the roster can be derived. Almost none of the "how" can.**

- **Roster membership is derivable for 20 of 22 groups.** Gen 1–7 (except Let's Go) and BDSP
  hold *every species up to a per-game cap*. Let's Go, Legends: Arceus and Legends: Z-A hold
  *exactly the union of their dexes*. That leaves **149 species** to curate by hand: **80 for
  Sword/Shield** and **69 for Scarlet/Violet**. Evolution does not fill the gap. Following
  evolution lines from the dexes finds 0 of the 80 and 3 of the 69, and it wrongly adds
  species elsewhere (37 in Let's Go).
- **How each entry is obtained must be curated.** The only machine-readable signal is
  PokéAPI's encounter table. It is good for "catch here" in Gen 1–8, and Sword/Shield agrees
  with Bulbapedia to within 3 species (629 vs 626). It has **nothing** for BDSP, Legends:
  Arceus, Scarlet/Violet or Z-A: **1,832 roster entries** with no derivable obtain method.
  Transfer-only, event-only, trade-evolution and "trade from the paired version" have no
  PokéAPI source in any generation.
- **Forms by game must be curated.** PokéAPI has no per-game form availability. Megas follow a
  rule (every Mega whose species is in the roster, in games that have the mechanic). Regional
  forms do not: **162 form-in-game rows** have to be hand-listed.
- **PokéAPI's closest roster table (`pokemon_game_indices`) is right for 17 of 22 groups and
  wrong for 5.** It copies Sword/Shield's 664 onto BDSP and Legends: Arceus, copies
  Scarlet/Violet's 733 onto Z-A (and onto Mega Dimension), gives Let's Go 802 instead of 153,
  and gives Ultra Sun/Ultra Moon 802 instead of 807.
- **Serebii and Bulbapedia agree on every count.** They disagree on *categorisation* (what
  counts as transfer-only versus event-only). Bulbapedia also disagrees with itself once: its
  Paldea dex page omits 29 out-of-dex species that its own availability matrix lists, 13 of
  which Serebii also lists.

---

## 1. Dexes and roster, per version group

"Roster" means every species the game can hold by any route (catch, evolve, breed, gift,
trade, transfer, event). "Out of dex" means roster species in none of the game's regional
dexes, which is the "Also in this game" chip (settled point 8 on #87).

| Version group | Regional dexes (entries) | National dex in game? | Roster (species) | Out of dex | Roster rule |
|---|---|---|---|---|---|
| Red/Blue | Kanto 151 | no (Kanto = all 151) | 151 | 0 | all ≤ #151 |
| Yellow | Kanto 151 | no | 151 | 0 | all ≤ #151 |
| Gold/Silver | Johto ("New") 251 | yes ("Old" mode) | 251 | 0 | all ≤ #251 |
| Crystal | Johto 251 | yes | 251 | 0 | all ≤ #251 |
| Ruby/Sapphire | Hoenn 202 | yes (unlocked by trading) | 386 | 184 | all ≤ #386 |
| Emerald | Hoenn 202 | yes | 386 | 184 | all ≤ #386 |
| FireRed/LeafGreen | Kanto 151 | yes | 386 | 235 | all ≤ #386 |
| Diamond/Pearl | Sinnoh 151 | yes | 493 | 342 | all ≤ #493 |
| Platinum | Sinnoh (extended) 210 | yes | 493 | 283 | all ≤ #493 |
| HeartGold/SoulSilver | Johto (extended) 256 | yes | 493 | 237 | all ≤ #493 |
| Black/White | Unova 156 (#000–#155) | yes | 649 | 493 | all ≤ #649 |
| Black 2/White 2 | Unova (extended) 301 (#000–#300) | yes | 649 | 348 | all ≤ #649 |
| X/Y | Central 153 · Coastal 153 · Mountain 151 (457, no overlap) | yes | 721 | 264 | all ≤ #721 |
| Omega Ruby/Alpha Sapphire | Hoenn (extended) 211 | yes | 721 | 510 | all ≤ #721 |
| Sun/Moon | Alola 302 + 4 island sub-dexes (120/130/130/100) | **no** | 802 | 500 | all ≤ #802 |
| Ultra Sun/Ultra Moon | Alola 403 + 4 island sub-dexes (150/160/160/130) | **no** | 807 | 404 | all ≤ #807 |
| Let's Go Pikachu/Eevee | Kanto 153 (#152–153 = Meltan, Melmetal) | no | 153 | 0 | = dex |
| Sword/Shield (+IoA, CT) | Galar 400 · Isle of Armor 211 · Crown Tundra 210 (584 unique) | no | 664 | **80** | dex + **curated** |
| Brilliant Diamond/Shining Pearl | Sinnoh 151 | yes | 493 | 342 | all ≤ #493 |
| Legends: Arceus | Hisui 242 | no | 242 | 0 | = dex |
| Scarlet/Violet (+TM, ID) | Paldea 400 · Kitakami 200 · Blueberry 243 (664 unique) | no | 733 | **69** | dex + **curated** |
| Legends: Z-A (+MD) | Lumiose 232 · Hyperspace 132 (364, no overlap) | no | 364 | 0 | = dex |

Sources:

- **Dex entry counts:** Bulbapedia's
  [Regional Pokédex](https://bulbapedia.bulbagarden.net/wiki/Regional_Pok%C3%A9dex) list (for
  example "Isle of Armor Pokédex (IA): #001 Slowpoke – #211 Zarude", "Hyperspace Pokédex (MD):
  #001 Mankey – #132 Zeraora"). Serebii agrees where checked:
  [Isle of Armor dex](https://www.serebii.net/swordshield/isleofarmordex.shtml) #001 Slowpoke
  to #211 Zarude, and
  [Hyperspace dex](https://www.serebii.net/legendsz-a/hyperspacepokedex.shtml) #001 Mankey to
  #132 Zeraora. **PokéAPI's `pokemon_dex_numbers.csv` matches every count.** The unique-species
  unions are computed from that file.
- **Which games have a National dex:** Bulbapedia's
  [National Pokédex](https://bulbapedia.bulbagarden.net/wiki/National_Pok%C3%A9dex) article.
  It has been "available as a Pokédex mode in all core series games from Generation I to VI,
  and in Pokémon Brilliant Diamond and Shining Pearl". Gen 7 onward does not have one. In
  Gen 1, Kanto *is* the full list. The same article confirms the closed rosters: Legends:
  Arceus allows "only Pokémon in the Hisui Pokédex (excluding some regional forms)", and Z-A
  allows "only Pokémon in the Lumiose Pokédex or the Hyperspace Pokédex". Sun/Moon cannot use
  the five Ultra Sun/Ultra Moon debuts.
- **Roster counts:** Bulbapedia's
  [List of Pokémon by availability](https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_availability),
  counting a species when any of its rows (including regional-form rows) is anything other
  than "—" in either version of the group. The out-of-dex figure for Sword/Shield is
  corroborated verbatim on Bulbapedia's
  [Galar dex page](https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_Galar_Pok%C3%A9dex_number):
  "The following 80 Pokémon are not in the Galar Pokédex, Isle of Armor Pokédex, or Crown
  Tundra Pokédex, but can still be obtained".

**The Gen 2–7 rule needs a per-game cap, not a per-generation one.** Sun/Moon stops at #802.
Ultra Sun/Ultra Moon stops at #807. Neither holds Meltan or Melmetal (#808–809), which debuted
alongside Let's Go and only reach Gen 8+. Every Gen 2–6 game, and BDSP, holds every species up
to its own generation's last number.

**Z-A's closed roster now covers HOME too.** HOME 4.0.0 added Z-A connectivity on
2026-04-02. Transfers are "limited to Pokémon that can appear in each game", and they are
one-way: Pokémon "cannot be transferred from Pokémon Legends: Z-A to previous titles"
([Nintendo Life](https://www.nintendolife.com/news/2026/04/pokemon-legends-z-a-is-getting-pokemon-home-connectivity-this-week);
[Nintendo support](https://en-americas-support.nintendo.com/app/answers/detail/a_id/68628)
gives no species list). Serebii's
[Z-A transfer-only page](https://www.serebii.net/legendsz-a/transferonly.shtml) says "As of
the Mega Dimension DLC, only certain forms are transfer only. All Pokémon can be found
natively in the game." Bulbapedia's matrix agrees: no species is coded transfer-only for Z-A.

---

## 2. How each roster entry is obtained, by game

Bulbapedia's matrix gives every species a per-version code. A solid cell means normal in-game,
a striped cell means it needs communication or an event item, and a white cell means not
obtainable in this game. The codes were collapsed to the issue's categories. Each species
takes the best code across its rows and across both versions of the group:

| Category | Bulbapedia codes folded in |
|---|---|
| **catch** | `C`, `S` (swarm / Island Scan / rotating), `D`, `DA` (Max Raid / Dynamax Adventure), `CD` (paid DLC), solid `EV` |
| **gift** | `R`, `RD` (starter, fossil, in-game trade, gift) |
| **evolve** | `E`, `ED` |
| **breed** | `B`, `BD` |
| **conditional** | any striped cell: `ET` (trade evolution), `TE` (evolve in another game), `CC…` (needs another game's save or a link), `FS` (Friend Safari), `DS` (dual-slot), event-item `EV` |
| **event-only** | white `Ev`, `EvB`, `EvE`, `DW`, `DR`, `PW` (real-world distributions, Dream World, Dream Radar, Pokéwalker) |
| **transfer** | white `T` (trade from another game, Bank or HOME) |

Species counts per category (whole roster):

| Group | catch | gift | evolve | breed | conditional | event-only | transfer | roster |
|---|---|---|---|---|---|---|---|---|
| Red/Blue | 99 | 15 | 32 | — | 4 | 1 | — | 151 |
| Yellow | 91 | 16 | 27 | — | 3 | 2 | 12 | 151 |
| Gold/Silver | 157 | 9 | 50 | 6 | 10 | 19 | — | 251 |
| Crystal | 156 | 16 | 41 | — | 10 | 20 | 8 | 251 |
| Ruby/Sapphire | 127 | 7 | 57 | 3 | 6 | 88 | 98 | 386 |
| Emerald | 141 | 8 | 68 | 3 | 21 | 58 | 87 | 386 |
| FireRed/LeafGreen | 133 | 15 | 48 | 9 | 12 | 89 | 80 | 386 |
| Diamond/Pearl | 247 | 11 | 115 | 9 | 55 | 19 | 37 | 493 |
| Platinum | 247 | 12 | 113 | 10 | 52 | 13 | 46 | 493 |
| HeartGold/SoulSilver | 270 | 19 | 109 | 12 | 24 | 33 | 26 | 493 |
| Black/White | 290 | 16 | 115 | 29 | 18 | 149 | 32 | 649 |
| Black 2/White 2 | 308 | 34 | 82 | 43 | 15 | 134 | 33 | 649 |
| X/Y | 259 | 20 | 148 | 28 | 138 | 45 | 83 | 721 |
| Omega Ruby/Alpha Sapphire | 241 | 28 | 166 | 22 | 20 | 69 | 175 | 721 |
| Sun/Moon | 239 | 16 | 93 | 24 | 13 | 38 | 379 | 802 |
| Ultra Sun/Ultra Moon | 365 | 22 | 109 | 46 | 19 | 21 | 225 | 807 |
| Let's Go | 125 | 6 | 15 | — | 4 | 3 | — | 153 |
| Sword/Shield | 608 | 18 | 18 | 3 | 4 | 9 | 4 | 664 |
| BDSP | 309 | 8 | 143 | 8 | 21 | 2 | 2 | 493 |
| Legends: Arceus | 240 | — | — | — | 2 | — | — | 242 |
| Scarlet/Violet | 624 | 3 | 59 | — | 8 | 4 | 35 | 733 |
| Legends: Z-A | 319 | 6 | 35 | — | 4 | — | — | 364 |

Source: Bulbapedia,
[List of Pokémon by availability](https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_availability),
aggregated 2026-10-11. The "best code wins" collapse is this note's, not Bulbapedia's. A
species catchable in one version and transfer-only in the other counts as *catch*. Section 3
counts those cases separately.

Notes for reading the table:

- **"Transfer" in Gen 1–7 is not the same thing as in Gen 8+.** Before HOME it means trade from
  any compatible game, including the same generation's other titles. Yellow's 12 transfers
  are Red/Blue species Yellow cannot catch. Sun/Moon's 379 are mostly Bank arrivals. From
  Sword/Shield on it means HOME.
- **Legends: Arceus has no evolve rows** because Bulbapedia codes nearly everything there as
  `C` (almost every evolved form is also wild). Its two conditional entries are Darkrai and
  Shaymin (`CC`, which need BDSP or Sword/Shield save data).
- **Gen 8+ out-of-dex sets, by category:**
  - **Sword/Shield's 80:** 51 catch (mostly Crown Tundra Dynamax Adventures and the legendary
    quests), 8 evolve, 5 gift, 3 breed, 1 conditional, 8 event-only, 4 transfer.
  - **Scarlet/Violet's 69:** 23 catch (mostly Indigo Disk Snacksworth legendaries), 2 evolve,
    5 conditional, 4 event-only, 35 transfer.

Out-of-dex species, by National number (Bulbapedia roster minus PokéAPI dex union):

```
Sword/Shield (80):   150-151, 243-245, 249-260, 380-385, 480-488, 494, 641-647, 649,
                     716-719, 721-730, 785-809
Scarlet/Violet (69): 144-146, 150-151, 243-245, 249-250, 377-386, 480-493, 638-648,
                     719-721, 789-792, 800-801, 863, 888-899, 903, 905
```

### Cross-check: what PokéAPI's encounters can supply

Species with at least one row in PokéAPI's `encounters.csv` (any method, gift included),
compared with Bulbapedia's catch + gift:

| Group | PokéAPI species with encounters | Bulbapedia catch + gift |
|---|---|---|
| Red/Blue | 114 | 114 |
| Yellow | 106 | 107 |
| Gold/Silver | 164 | 166 |
| Crystal | 170 | 172 |
| Sun/Moon | 256 | 255 |
| Ultra Sun/Ultra Moon | 394 | 387 |
| Let's Go | 130 | 131 |
| Sword/Shield + both DLCs | 629 | 626 |
| Omega Ruby/Alpha Sapphire | **140** | 269 |
| BDSP · Legends: Arceus · Scarlet/Violet · Z-A | **0** | 317 · 240 · 627 · 325 |

This agrees with the encounter-coverage note
([`pokeapi-encounter-data-coverage.md`](pokeapi-encounter-data-coverage.md), §2 and §6): Gen
1–8 is usable, ORAS is thin (PokéAPI issue #1456), and the four newest groups are empty.

---

## 3. Trade from the paired version

Species obtainable in-game in **exactly one** version of a two-version group, so the other
version needs a trade. Derived from the same matrix (in-game in one version, transfer, event or
absent in the other):

| Group | RB | GS | RS | FRLG | DP | HGSS | BW | B2W2 | XY | ORAS | SM | USUM | LGPE | SwSh | BDSP | SV |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Version-only species | 22 | 20 | 16 | 43 | 38 | 40 | 128 | 53 | 40 | 44 | 30 | 55 | 22 | 89 | 70 | 76 |

Source: Bulbapedia availability matrix. **Treat these as upper bounds, not official
version-exclusive counts.** They include cases where one version can catch and the other can
only evolve or breed, plus DLC-era and Grand Underground differences. Black/White's 128 is
inflated by White Forest and Black City, and by event cells. This note did not reconcile them
against Bulbapedia's or Serebii's version-exclusive lists. For Gen 1–8 the exact answer is
derivable from PokéAPI's per-version encounter rows. For BDSP and Scarlet/Violet it has to be
curated.

---

## 4. Forms by game

| Group | Megas | Primals | Gigantamax | Regional forms the game can hold |
|---|---|---|---|---|
| Gen 1–5, BDSP, Legends: Arceus, Scarlet/Violet | — | — | — | see the last column |
| X/Y | 30 (28 species)¹ | — | — | none |
| Omega Ruby/Alpha Sapphire | 48 | Kyogre, Groudon | — | none |
| Sun/Moon · Ultra Sun/Ultra Moon | 48 | Kyogre, Groudon | — | 18 Alolan |
| Let's Go | 15 (Kanto species only) | — | — | 18 Alolan |
| Sword/Shield | — | — | 32 species / 32 forms² | 11 Alolan + 19 Galarian = 30 |
| Legends: Arceus | — | — | — | 16 Hisuian + Alolan Vulpix and Ninetales = 18 |
| Scarlet/Violet | — | — | — | 15 Alolan + 8 Galarian + 16 Hisuian + 4 Paldean + Bloodmoon Ursaluna = 44 |
| Legends: Z-A (+MD) | **93** (96 counting variants) | Kyogre, Groudon (MD) | — | 4 Alolan + 8 Galarian + 4 Hisuian = 16³ |

¹ Mega Latias and Mega Latios "were officially made available in Pokémon Omega Ruby and Alpha
Sapphire, but they were already programmed into Pokémon X and Y", so their stones can be traded
in ([Bulbapedia, Mega Evolution](https://bulbapedia.bulbagarden.net/wiki/Mega_Evolution), note 2).

² Bulbapedia counts 32 Gigantamax forms. PokéAPI has 34 `-gmax` form rows because it splits
Toxtricity's two forms and Urshifu's two styles.

³ Bulbapedia's matrix has an empty Z-A cell for Hisuian Samurott, neither "—" nor a code. It is
excluded here, but **unverified**.

Sources:

- **Megas.**
  [Bulbapedia, Mega Evolution](https://bulbapedia.bulbagarden.net/wiki/Mega_Evolution): "30
  Mega Evolutions for 28 Pokémon were introduced with Pokémon X and Y", ORAS adds 18, "26 new
  Mega Evolutions were introduced with the base game of Pokémon Legends: Z-A", and "19 new
  Mega Evolutions (22, if form variations are counted) for 18 Pokémon" came with Mega
  Dimension. That is "93 (or 96, counting specific variations)" in total. Every Mega row's
  availability cell includes Z-A or Mega Dimension, so Z-A with its DLC holds all of them.
  Let's Go's 15 are the rows marked "PE": Venusaur, Charizard X/Y, Blastoise, Beedrill,
  Pidgeot, Alakazam, Slowbro, Gengar, Kangaskhan, Pinsir, Gyarados, Aerodactyl, Mewtwo X/Y.
- **Primals.**
  [Bulbapedia, Primal Reversion](https://bulbapedia.bulbagarden.net/wiki/Primal_Reversion):
  present in ORAS and SM/USUM, "removed from Pokémon: Let's Go… onwards, reappearing in the
  Mega Dimension DLC of Pokémon Legends: Z-A".
- **Gigantamax.**
  [Bulbapedia, Gigantamax](https://bulbapedia.bulbagarden.net/wiki/Gigantamax): "32 species…
  32 different known Gigantamax forms"; it "has only made one core series appearance".
- **Regional forms.** The form rows in Bulbapedia's availability matrix (58 rows, every
  Alolan, Galarian, Hisuian and Paldean form plus Bloodmoon Ursaluna). Sword/Shield lacks 7
  Alolan forms even though their base species is in its roster (Rattata, Raticate, Geodude,
  Graveler, Golem, Grimer, Muk). Scarlet/Violet lacks Alolan Rattata, Raticate and Marowak.
  **No "every regional form of a roster species" rule holds.**
- **Transfer-only cosmetic forms**, outside this table. Serebii lists them for
  [Sword/Shield](https://serebii.net/swordshield/transferonly.shtml) (cap Pikachu, East/West
  Shellos and Gastrodon, Original Color Magearna) and
  [Z-A](https://www.serebii.net/legendsz-a/transferonly.shtml) (17 Vivillon patterns,
  Original Color Magearna, Roaming Gimmighoul). Bulbapedia's matrix has rows for Magearna and
  Gimmighoul only. Under #5 cosmetic forms are not variants, but they matter for a future
  tracker.

---

## 5. Where the references disagree

| # | Topic | Serebii | Bulbapedia | Reading |
|---|---|---|---|---|
| 1 | Sword/Shield transfer-only | 12 species: Mew, Celebi, Jirachi, Victini, Genesect, Diancie, Volcanion, Magearna, Marshadow, Zeraora, Meltan, Melmetal ([page](https://serebii.net/swordshield/transferonly.shtml)) | 13: the same 12 **plus Zarude**. Only Jirachi, Diancie, Magearna and Meltan are `T`; the rest are `Ev` (event-only) | Same species apart from Zarude. Serebii merges transfer and event; Bulbapedia splits them. |
| 2 | Scarlet/Violet transfer-only | 41 species ([page](https://www.serebii.net/scarletviolet/transferonly.shtml)) | 35 `T` + 4 `Ev` (Mew, Dialga, Palkia, Darkrai) | Serebii also counts Mewtwo (Bulbapedia: event raid, `EV`) and Wyrdeer (Bulbapedia: `TE`, evolve in Legends: Arceus). It omits Rayquaza, Kyurem and Necrozma (Bulbapedia: `CCD`, Indigo Disk with a condition), Ursaluna (`TE`), and Walking Wake, Iron Leaves and Pecharunt (event/DLC). **Same roster, different labels.** |
| 3 | Scarlet/Violet out-of-dex list | — | Matrix: 69 species. [Paldea dex page](https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_Paldea_Pok%C3%A9dex_number) table: 41 species plus regional forms | The Paldea page omits 29 species that the matrix places in SV. 13 of them are also on Serebii's SV transfer-only list (the Regis, Jirachi, Deoxys, Regigigas, Phione, Manaphy, Darkrai, Shaymin, Keldeo, Cosmog, Cosmoem). The rest are Indigo Disk catches per the matrix. The full 29 (Raikou, Entei, Suicune, Lugia, Ho-Oh, the three Regis, Latias, Latios, Jirachi, Deoxys, Regigigas, Phione, Manaphy, Darkrai, Shaymin, Cobalion, Terrakion, Virizion, Reshiram, Zekrom, Kyurem, Keldeo, Cosmog, Cosmoem, Solgaleo, Lunala, Necrozma). **The Paldea page is stale. Do not use it.** |
| 4 | Z-A size | [Unobtainable page](https://www.serebii.net/legendsz-a/unobtainable.shtml): "791 Pokémon are, at time of writing, unavailable" (implies 234) | 364 | Serebii's page appears to predate Mega Dimension (232 Lumiose + 132 Hyperspace = 364). **Not confirmed**: the page does not say when it was written. |
| 5 | Z-A Hisuian Samurott | not listed | empty matrix cell | Unresolved. |
| 6 | Mega counts | — | 93 Megas (96 with variants); Mega Dimension adds "22, if form variations are counted" | PokéAPI has 97 `-mega` form rows and 23 Mega Dimension variants (it splits male/female Meowstic and Original Color Magearna). Which variants count is a counting choice, not a factual disagreement. |

Everywhere else the two agree: every dex count checked, Z-A's "all species native", and
Sword/Shield's "any original or Alolan form of a Pokémon in the game is transferable".

---

## 6. Where PokéAPI agrees, and where it falls short

**Dexes: correct.** `pokedexes.csv`, `pokedex_version_groups.csv` and `pokemon_dex_numbers.csv`
match Bulbapedia's entry counts for every dex in §1, Isle of Armor's 211 and Hyperspace's 132
included. Two gaps:

- **The National dex is linked to no version group.** It has to be added by rule to Gen 2–6
  and BDSP.
- **Mega Dimension is its own version group** (31, with Hyperspace also linked to Z-A's 30),
  and so are the four Gen 8–9 DLCs. Folding is the app's job, as #87 point 10 already notes
  for encounters.

**Roster: `pokemon_game_indices.csv` is the closest table, and it is unreliable after Gen 7.**
Distinct species per version, compared with the Bulbapedia roster:

| Group | PokéAPI | True | Extra in PokéAPI | Missing from PokéAPI | Verdict |
|---|---|---|---|---|---|
| Gen 1–6, Sun/Moon | 151 … 802 | same | 0 | 0 | ✅ |
| Ultra Sun/Ultra Moon | 802 | 807 | 0 | 5 (#803–807) | ❌ copied from Sun/Moon |
| Let's Go | 802 | 153 | 651 | 2 (Meltan, Melmetal) | ❌ copied from USUM |
| Sword/Shield | 664 | 664 | 0 | 0 | ✅ set-identical |
| BDSP | 664 | 493 | 342 | 171 | ❌ copied from Sword/Shield |
| Legends: Arceus | 664 | 242 | 502 | 80 | ❌ copied from Sword/Shield |
| Scarlet/Violet | 733 | 733 | 0 | 0 | ✅ set-identical |
| Legends: Z-A (and `mega-dimension`) | 733 | 364 | 478 | 109 | ❌ copied from Scarlet/Violet |

The table also has **no rows above id 10000**, so it says nothing about which forms a game holds.

**Forms: no per-game availability anywhere.** The two candidate tables do not cover it:

- `pokemon_form_generations.csv` is per *generation* and stops at Gen 8.
- `pokemon_forms.introduced_in_version_group_id` records only debuts, and it files all 26
  base-game Z-A Megas under `mega-dimension` (31) instead of `legends-za` (30).

**Obtain method: encounters only.** See §2. There is no PokéAPI notion of transfer-only,
event-only or HOME compatibility.

---

## 7. Derived versus curated

| Fact | Derivable from | Must be curated | Count to curate |
|---|---|---|---|
| Dex list and numbering | PokéAPI dex tables (verified) | National-dex flag for Gen 2–6 + BDSP; DLC folding | 13 flags |
| Roster, Gen 1–7 (not Let's Go) + BDSP | rule: all species ≤ cap | the cap per group | 17 numbers |
| Roster, Let's Go · Legends: Arceus · Z-A | rule: = union of the game's dexes | nothing | 0 |
| Roster, Sword/Shield | dex union (584) | out-of-dex species | **80** |
| Roster, Scarlet/Violet | dex union (664) | out-of-dex species | **69** |
| Obtain method, Gen 1–8 | PokéAPI encounters, for "catch here" and gifts only | evolve / breed / trade-evolution / transfer / event; ORAS gaps | every non-catch, non-gift entry: **about 4,200** rows over the 18 groups (roster minus catch + gift in §2), plus about 130 ORAS catches PokéAPI lacks |
| Obtain method, BDSP · Legends: Arceus · SV · Z-A | nothing | all of it | **1,832** (493 + 242 + 733 + 364) |
| Trade from paired version | PokéAPI per-version encounters (Gen 1–8) | BDSP, SV | 146 upper bound (70 + 76) |
| Megas per game | rule: Megas of roster species, in games with the mechanic, introduced by then | the mechanic flag | 6 flags |
| Primals, Gigantamax | rule: those species, in games with the mechanic | the flag | 5 flags |
| Regional forms per game | nothing | every form-in-game pair | **162** |

**Evolution is not a usable derivation rule.** Following evolution lines from each game's dex
union (using `pokemon_species.evolution_chain_id`):

| Group | Out-of-dex species found | Species wrongly added |
|---|---|---|
| Sword/Shield | 0 of 80 | 10 later-generation evolutions (Kleavor, Overqwil, Kingambit…) |
| Scarlet/Violet | 3 of 69 (Perrserker, Wyrdeer, Sneasler) | 0 |
| Let's Go | — | 37 (Pichu, Crobat, Steelix…) |
| Legends: Arceus | — | 1 (Mr. Rime) |

Evolution helps classify *how* an entry is obtained (evolve versus catch), not *whether* it is
there.

**Licensing.** It constrains the curation, as #87 notes. Bulbapedia is CC BY-NC-SA and
Serebii is all rights reserved. Both were used here to verify, not to import. The curated
dataset has to be written by the author, with these pages as the check.

---

## Appendix: reproducing these numbers

```powershell
# PokéAPI at the pinned SHA
$sha = '4b82c204ddd19ecb8eda2ea044ccb59e222b721c'
foreach ($n in 'pokedexes','pokedex_version_groups','pokemon_dex_numbers','pokemon_game_indices',
               'pokemon','pokemon_species','pokemon_forms','pokemon_form_generations',
               'encounters','versions','version_groups') {
  Invoke-WebRequest "https://raw.githubusercontent.com/PokeAPI/pokeapi/$sha/data/v2/csv/$n.csv" -OutFile "$n.csv"
}
# Roster per version: distinct species_id over pokemon_game_indices joined to pokemon.csv
# Dex union per group: pokedex_version_groups -> pokemon_dex_numbers (excluding pokedex 1)
```

The Bulbapedia matrix was read from the rendered
[availability page](https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_availability).
It is 11 tables, one per generation, with 1,101 rows (1,025 species, 58 regional/variant form
rows and 18 other-form rows). Each table drops the columns of games that predate its
generation. Each cell was classed by its style: `background:` solid, `repeating-linear-gradient`
striped, `color:` white. `colspan` was expanded.

### Primary sources cited

- Bulbapedia: [List of Pokémon by availability](https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_availability) ·
  [Regional Pokédex](https://bulbapedia.bulbagarden.net/wiki/Regional_Pok%C3%A9dex) ·
  [National Pokédex](https://bulbapedia.bulbagarden.net/wiki/National_Pok%C3%A9dex) ·
  [Galar dex list](https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_Galar_Pok%C3%A9dex_number) ·
  [Paldea dex list](https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_Paldea_Pok%C3%A9dex_number) ·
  [Mega Evolution](https://bulbapedia.bulbagarden.net/wiki/Mega_Evolution) ·
  [Primal Reversion](https://bulbapedia.bulbagarden.net/wiki/Primal_Reversion) ·
  [Gigantamax](https://bulbapedia.bulbagarden.net/wiki/Gigantamax)
- Serebii: [SwSh transfer only](https://serebii.net/swordshield/transferonly.shtml) ·
  [SV transfer only](https://www.serebii.net/scarletviolet/transferonly.shtml) ·
  [Z-A transfer only](https://www.serebii.net/legendsz-a/transferonly.shtml) ·
  [Z-A unobtainable](https://www.serebii.net/legendsz-a/unobtainable.shtml) ·
  [Isle of Armor dex](https://www.serebii.net/swordshield/isleofarmordex.shtml) ·
  [Hyperspace dex](https://www.serebii.net/legendsz-a/hyperspacepokedex.shtml)
- HOME and Z-A: [Nintendo Life, 2026-04](https://www.nintendolife.com/news/2026/04/pokemon-legends-z-a-is-getting-pokemon-home-connectivity-this-week) ·
  [Nintendo support 68628](https://en-americas-support.nintendo.com/app/answers/detail/a_id/68628).
  No official per-game HOME species list was found. The official statement is the rule
  "limited to Pokémon that can appear in each game".
- PokéAPI CSVs at `4b82c204ddd19ecb8eda2ea044ccb59e222b721c`: `pokedexes`,
  `pokedex_version_groups`, `pokemon_dex_numbers`, `pokemon_game_indices`, `pokemon`,
  `pokemon_species`, `pokemon_forms`, `pokemon_form_generations`, `encounters`, `versions`,
  `version_groups`.
