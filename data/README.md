# The dataset

Everything the app knows about Pokémon, as JSON committed to the repository.

This directory exists so that a change to the data is **reviewable in a pull request** before it is
reviewable on a device. `pokedex.db` is built from these files at build time and is gitignored; a
pipeline that went straight from upstream's CSVs to SQLite would put every change in a binary nobody
can read. See #10.

## Every file here is generated

Editing any of them is pointless — the next run of `generateDataset` overwrites them whole.

| File | |
|---|---|
| `manifest.json` | the pinned upstream SHA, the schema version, and the row counts |
| `species.json` | 1,025 species — the National Dex |
| `variants.json` | 1,385 forms, of which 1,025 get a card |
| `types.json` | the 18 types and the effectiveness chart |
| `abilities.json` | 314 abilities |
| `moves.json` | 919 moves |
| `learnset.json` | 62,777 rows saying which Pokemon learn which move |

There was briefly a hand-authored `ability-tag-overrides.json` holding corrections to a generated
classification. Both are gone: see `DECISIONS.md § Rejected for now: a classification for abilities`
and #65. If a hand-authored file comes back, this section is where it gets called out, because
"which of these may I edit" is not answerable by looking at them.

## Regenerating

```bash
./gradlew :tools:datagen:generateDataset
```

Reaches the network, reads the CSVs pinned in `Upstream.kt`, and rewrites the seven generated files.
**Deliberately not wired into `build`** — it runs when a human decides to move the pin, never as a
side effect of compiling the app. Same pin in, byte-identical files out, which is what makes a
changed line mean a changed fact rather than churn.

```bash
./gradlew :tools:datagen:buildPokedexDatabase
```

Builds `pokedex.db` from these files, offline. It writes through the app's own `.sq` schema rather
than hand-written SQL, so a column the app queries and the generator forgets to fill cannot exist.
It also checks the row counts back against `manifest.json` before declaring success.

It writes a second file beside it, `pokedex.stamp`, holding the schema version and the pinned SHA.
That is what lets an installed app notice its dataset has moved on: the copy a device makes on first
run is replaced rather than migrated, and nothing in the database itself says which dataset it is —
SQLite's `user_version` tracks the schema, so a pin bump that rewrites every row leaves it unchanged.
Both files are gitignored. See `DECISIONS.md § The bundled dataset is replaced when it changes, not
only when it is missing`.
