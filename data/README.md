# The dataset

Everything the app knows about Pokémon, as JSON committed to the repository.

This directory exists so that a change to the data is **reviewable in a pull request** before it is
reviewable on a device. `pokedex.db` is built from these files at build time and is gitignored; a
pipeline that went straight from upstream's CSVs to SQLite would put every change in a binary nobody
can read. See #10.

## What is generated and what is not

**Five of these files are output. Editing them is pointless** — the next run of `generateDataset`
overwrites them whole.

| File | | |
|---|---|---|
| `manifest.json` | generated | the pinned upstream SHA, the schema version, and the row counts |
| `species.json` | generated | 1,025 species — the National Dex |
| `variants.json` | generated | 1,385 forms, of which 1,025 get a card |
| `types.json` | generated | the 18 types and the effectiveness chart |
| `abilities.json` | generated | 314 abilities, each with its tags and its trigger |
| `moves.json` | generated | 919 moves |
| **`ability-tag-overrides.json`** | **hand-authored** | **corrections to the tags. The generator reads it and never writes it.** |

## `ability-tag-overrides.json`

**It is not the list of tags.** That list is the `AbilityTag` enum in
`tools/datagen/.../AbilityTag.kt`, which is compiled against and so cannot drift from the code.

This file is the short list of abilities where the classifier got it **wrong**, and what the answer
should be instead. One entry per correction, keyed by ability slug, and the value **replaces** that
ability's tags outright rather than adding to them:

```json
{
  "truant": ["PRIORITY"]
}
```

Truant is "Skips every second turn", which no keyword rule reads as turn order, so it is corrected
here. The other 309 abilities are not in this file at all — they are whatever the classifier said.

**Why it is a separate file rather than an edit to `abilities.json`.** #27 requires that a hand-fixed
answer stay fixed, and `abilities.json` is regenerated whole on every run: a correction made there
would survive exactly until the next time somebody bumped the pinned SHA. Keeping the human's input
in its own file means the classifier's output stays pure — a rerun with no upstream change produces
an empty diff — and a correction reads as its own line instead of a hunk buried in generated output.

Two tests in `DatasetTest` guard it from both sides: one fails if an entry names an ability that does
not exist (a typo here is otherwise silent, since the classifier's answer simply stands), and one
fails if an entry did not take (which would mean the generator never read the file).

## Regenerating

```bash
./gradlew :tools:datagen:generateDataset
```

Reaches the network, reads the CSVs pinned in `Upstream.kt`, and rewrites the five generated files.
**Deliberately not wired into `build`** — it runs when a human decides to move the pin, never as a
side effect of compiling the app. Same pin in, byte-identical files out, which is what makes a
changed line mean a changed fact rather than churn.

```bash
./gradlew :tools:datagen:buildPokedexDatabase
```

Builds `pokedex.db` from these files, offline. It writes through the app's own `.sq` schema rather
than hand-written SQL, so a column the app queries and the generator forgets to fill cannot exist.
It also checks the row counts back against `manifest.json` before declaring success.
