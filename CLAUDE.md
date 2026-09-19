# CLAUDE.md

Guidance for Claude Code (or any agent) working in this repository.

## What this repository is

**Tall Grass** — a Pokédex for Android, built with Kotlin Multiplatform and Compose Multiplatform. It is a fork of a KMP/CMP application template; the fork is complete, the template's example feature is gone, and nothing here is a placeholder any more.

The app exists to practise Compose animation, and that shapes the code more than it sounds like it should. See **Animation is a constraint, not a finish** below.

[README.md](README.md) explains the project to a *human*. It is not the technical source of truth for an agent. That lives in **`agents/`**, read in full before doing any non-trivial work here:

- [`agents/agent-architecture-convention.md`](agents/agent-architecture-convention.md) — package placement decision procedure, layer shape, MVI vocabulary, previews, loading states, DI, error handling, testing conventions.
- [`agents/agent-commit-convention.md`](agents/agent-commit-convention.md) — deterministic `type`/`scope` selection for commit messages.
- [`agents/agent-design-system-convention.md`](agents/agent-design-system-convention.md) — the color/typography/spacing/shimmer layer model under `design/theme/`.
- [`agents/agent-documentation-convention.md`](agents/agent-documentation-convention.md) — **where prose goes.** Which declarations get KDoc, when a `//` comment earns its place, and what belongs in `DECISIONS.md` instead of in a file.

[`DECISIONS.md`](DECISIONS.md) is that fourth document's destination: why the app is the way it is, including the alternatives that were rejected.

> **`agents/` and `docs/agents/` are different things.** `agents/` is the four convention documents above — how to write code here. `docs/agents/` is skill configuration — which issue tracker to use and which labels mean what. Neither is a subset of the other.

---

## Agent skills

### Issue tracker

GitHub Issues on `NicolasZurbuchen/tall-grass`, via the `gh` CLI. See [`docs/agents/issue-tracker.md`](docs/agents/issue-tracker.md).

### Triage labels

The five canonical roles, label strings unchanged: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See [`docs/agents/triage-labels.md`](docs/agents/triage-labels.md).

### Domain docs

Single-context — `CONTEXT.md` and `docs/adr/` at the repo root, created lazily rather than scaffolded. See [`docs/agents/domain.md`](docs/agents/domain.md).

---

## The planning already happened — read it before deciding anything

Twenty-six design questions were resolved as GitHub issues and closed before any code existed. **A choice that looks arbitrary in the source usually has an issue explaining what it was weighed against**, and reopening one without reading it tends to re-derive a worse version of the same answer.

The ones that constrain implementation most:

- **#5** — Species and Variant are separate entities with different cardinality. Variants are keyed by **slug**, never by Dex number, and never by PokéAPI's 10000+ internal ids. Only regional forms get a grid card; Megas and alternate forms exist as full variants reached through the detail form switcher; cosmetic forms are not variants at all.
- **#10** — Two databases that never mix, a pinned upstream SHA, committed JSON intermediates, and a generated database written through the app's own schema.
- **#11** — The screen inventory, the route table, and the rule deciding which transitions are shared-element.
- **#14** — How the five packages map onto this project, and why `core/` gets anything that models Pokémon even when one feature reads it.
- **#22** — CMP 1.11.1 and Navigation 3, with the reasoning for *not* upgrading.
- **#12** — The animation catalogue: motion tokens, the twelve keyframes, and the three that need a cheaper mechanism than `InfiniteTransition`.

`docs/design/prototypes/tall-grass-app.html` is a navigable prototype of the whole app. **It is an approximation, not a pixel-exact spec** — a mismatch with it is not a defect.

---

## Yadlo is the reference implementation

`C:\Users\stari\Projects\Yadlo` — a shipped KMP/CMP app by the same author, forked from the same template, ~100 merged pull requests deep. **When the question is "how is this done here", read Yadlo before inventing an answer.**

This matters more than it normally would: the template's `pokemon-explorer` example feature was deleted during the fork (#29), so this repository has no worked example of a feature with a data layer, a store, or a test suite. `feature/home/` is stateless and demonstrates the shape's shallow end only. Yadlo fills that gap and is a better reference than the deleted example was, because everything in it survived contact with a real product.

**It is the source of truth for *how*, never for *what*.** Product decisions belong to this project's closed issues; where the two disagree about what Tall Grass should do, the issue wins. Copy the patterns, not the festival.

### What is directly worth reading

| Looking for | Read |
|---|---|
| A full feature, every layer | `feature/programme/`, `feature/search/` |
| A `core/` slice modelling the subject | `core/content/`, `core/plan/`, `core/time/` |
| Real `.sq` **and `.sqm`** migrations | `shared/src/commonMain/sqldelight/` |
| A third-party singleton wrapped as a port | `infra/image/` |
| Committed JSON content with a schema doc and a validator | `content/` |
| Glossary and decision-record layout | `CONTEXT.md`, `DECISIONS.md` |

**The migrations are the most valuable thing there right now.** This repository's `app.db` has no tables and no `.sqm` files yet, so the discipline CLAUDE.md warns about — a table added to a `.sq` with no matching `.sqm` compiles, runs and passes every other check — has never actually been exercised here. Yadlo has done it twice. Read those before writing the first migration, not after.

`content/` is the closest existing thing to what #10 asks for: JSON committed to the repo as the reviewable source of truth, with a schema document and a validator that fails the build when the two disagree.

### Two differences that will mislead if assumed away

- **Yadlo fetches its content at launch. Tall Grass bakes its dataset into the binary.** So Yadlo's `content/` is a model for *committed, validated, reviewable JSON* and not for the generation pipeline, the bundled database, or the first-run asset copy. None of those exist there.
- **Yadlo's `ImageCache` is not a prefetcher.** It reports a size and clears the cache. #32 needs prefetching, resumption and storage-exhaustion handling, and there is no prior art for it in either project — the port's *placement* is the lesson, not its contents.

> The path is absolute and local to one machine, so it may simply not be there. If it is missing, say so and work from the conventions in `agents/` rather than guessing at what it would have shown.

---

## Judgment calls not obvious from the code alone

Everything structural, deterministic, or repeatable already lives in `agents/agent-architecture-convention.md` and is enforced by Konsist — it isn't restated here. What follows is the handful of things that came up as real corrections and are genuinely easy to get wrong once.

- **Compose call sites**: `Modifier` is always the **last** argument at the call site, not just last in the function signature (trailing lambdas aside). Nothing currently enforces this mechanically — it's a review-time check.
- **Don't add defensive nullability**: don't make a field or parameter nullable "just in case" if the call site is only ever reachable with a valid value given how the layer above maps things. Nullability should describe a real, reachable state, not hedge against a scenario the codebase already prevents.
- **Magic numbers get a comment, not just a name**: a named constant with no explanation just moves the "why this bound?" question one file over instead of answering it.
- **Prose is routed, not sprinkled**: a KDoc says what a caller needs to call it correctly, a `//` says why the *code* is surprising, and why the *app* is like this goes in `DECISIONS.md` — even when the code it justifies is right there. The same explanation appearing twice always means something is wrong, but read the code under it before deciding what: either the abstraction is missing and wants extracting, or it already exists and the prose was written past it. See `agents/agent-documentation-convention.md`.
- **Gradle version catalog bundles**: if three or more libraries are always added together (see `ktor-common`, `compose-common`, `mvikotlin-common` in `[bundles]`), define a bundle and consume it via `libs.bundles.x` instead of listing each one at every call site.
- **Typography slots are roles, not Material's scale.** Every slot in `design/theme/Type.kt` carries a comment naming what it's for *here*; `titleLarge` may well be a 14sp button label. Picking by Material's semantics instead of by the comment is how a screen ends up rendering its headline at caption size — and it compiles, so nothing catches it.
- **A new Konsist rule is not trusted until it has been seen to fail.** Point it at a real violation, watch it go red, revert. A rule that cannot fail is worse than no rule because it reads as coverage: this repo has shipped a tautological rule, two exclusions naming packages that never existed, and four allow-list entries for packages nobody had written.
- **Package placement decision criteria** (also in `agents/agent-architecture-convention.md`, repeated because it's the single most load-bearing judgment call here). Five top-level packages, and **every question is answerable by reading the file itself** — that's the property that makes the procedure work, and the reason it doesn't ask how many features use something: "is it cross-feature" is a fact about the rest of the system, and it can become true without the file changing.
  1. Does it **compose the app** — wire the graph, own the navigation host? → `app/`, which is terminal: it imports everything and nothing imports it.
  2. Exactly **one** feature? → `feature/<name>/`
  3. Does it **model the subject** — a domain type, or the rendering of one? → `core/`
  4. **Tokens, or a component whose contract is purely presentational?** → `design/`
  5. **Pure plumbing, no domain vocabulary, no brand?** → `infra/`
- **`design/` versus `core/<slice>/presentation/`** is the one call that list doesn't settle: does the component own a rule about *the subject*, or only about *appearance*? A filter chip decides nothing about meaning and is `design/`; a timeline that decides how a day lays out owns a rule about the subject and belongs beside the model. The linguistic form of the same test is whether the `App` prefix reads sensibly — `AppFilterChip` does, `AppInvoiceTimeline` doesn't.

---


### Animation is a constraint, not a finish

The author's stated approach is to build the app so the domain and UI work, *then* add transitions. That holds for almost everything — staggers, stat bars, crossfades, list entrances are all paint-on-top and should be deferred.

**Two things are the exception, and both are already wired:**

1. **The nav host is wrapped in `SharedTransitionLayout`** (`infra/navigation/NavGraph.kt`), with the scope exposed as `LocalSharedTransitionScope`. A shared element needs matched keys on both sides and composables structured to share bounds; adding the wrapper later means reopening every screen written without it.
2. **Sprite URLs travel as navigation arguments.** A transition into a screen that *then* resolves its image will flicker. The target has to render artwork synchronously.

If either looks like unused ceremony while reading the code, it is not — it is load-bearing for a screen that does not exist yet. See #11.

## Verification before calling anything done

```bash
./gradlew :konsistTest:test
```

```bash
./gradlew :shared:testAndroidHostTest
```

```bash
./gradlew ktlintCheck
```

```bash
./gradlew :shared:verifyCommonMainAppDatabaseMigration
```

```bash
./gradlew :shared:compileCommonMainKotlinMetadata
```

Run all five, not just the one you think is relevant — `ktlintCheck` in particular has a history in this repo of catching violations across files a narrower, filtered test run never touches.

**The metadata compile is the one that stands in for iOS.** The Kotlin/Native targets do not build on a Windows machine, so everything above can pass with `commonMain` broken for iOS — `Dispatchers.IO`, for one, resolves happily against the Android compilation and does not exist in the common source set. Twice now that has been found by a twelve-minute CI job instead of a fifteen-second local one. It does not cover `iosMain` itself, which only CI can compile; it does cover every mistake of the form *"this API is not actually in commonMain"*.

The migration check is the odd one out and the reason it's on this list: **a table added to a `.sq` file without a matching `.sqm` compiles, runs, and passes every other command here.** It only breaks on a device that already had the database, because SQLDelight takes the schema version from the migration files rather than the schema files. Regenerate the snapshot with `:shared:generateCommonMainAppDatabaseSchema` whenever a `.sq` file changes, and commit the `.db` it writes.

`:konsistTest:test` does not need `--rerun-tasks`. It used to: Konsist builds its scopes from strings at runtime, so Gradle could not see that `:shared`'s sources are this task's real inputs and marked it UP-TO-DATE, replaying the last result. A violation added to `:shared` left the command green. The task is now pinned to always run — see the comment in `konsistTest/build.gradle.kts`. If you find `--rerun-tasks` in an old note or habit, it's stale rather than wrong.

All four run in CI on every push and pull request, alongside an iOS job that compiles the framework and runs `commonTest` on Native, and an Android job that assembles and lints the app. Running them locally first is still faster than waiting for a red build.

If a Konsist rule fails and you're tempted to change the rule to make it pass: don't. See `agents/agent-architecture-convention.md`'s last section.
