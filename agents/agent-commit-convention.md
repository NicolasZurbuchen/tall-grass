# Agent Commit Convention

This file exists so an agent can pick `type` and `scope` deterministically, without asking a human. Follow the decision procedure below in order — first match wins. Do not weigh multiple candidates and pick a "best fit"; the first rule that matches your diff is the answer.

Enforced by commitlint (`commitlint.config.js`). A commit that violates the enum or the scope-required rule will be **rejected at commit time** — there is no soft-fail here.

## Format

```
type(scope): description
```

`scope` is **required** for `feat`, `fix`, `refactor`, `build`. Optional (but encouraged when useful) for everything else.

---

## Step 1 — Determine `type`

Walk this list top to bottom. Stop at the first rule that matches the **primary intent** of the diff — not every file touched.

| Order | Type | Matches when… |
|---|---|---|
| 1 | `revert` | The commit's sole purpose is undoing a previous commit (via `git revert` or manual undo of a prior change). Never combine a revert with new work in the same commit. |
| 2 | `test` | Only test source sets change (unit tests, Konsist tests, UI tests). No production code changes. |
| 3 | `docs` | Only documentation changes: `README`, `*.md` files, KDoc/comments-only edits. No executable code changes. |
| 4 | `style` | Formatting only — ktlint/spotless output, whitespace, import ordering, trailing commas. Zero logic change. If you're not 100% sure it's zero logic change, it isn't `style` — move to the next check. |
| 5 | `ci` | Only `.github/workflows/**` or other CI pipeline config changes. |
| 6 | `build` | Changes to the build system itself: Gradle version catalog bumps, plugin version bumps, `commitlint`/`husky` config, Gradle script structure changes that don't touch app architecture. Requires scope. |
| 7 | `perf` | Measurable performance improvement (reduced allocations, faster query, fewer recompositions) with **no new capability and no bug being fixed**. If a perf change also fixes a bug, use `fix` instead — perf is the narrower case. |
| 8 | `refactor` | Structural/architectural code change with **no observable behavior change** (moving files to match layer boundaries, extracting an interface, renaming for clarity, splitting a module). Requires scope. If behavior changes even slightly, this is not refactor — it's `fix` or `feat`. |
| 9 | `fix` | Resolves incorrect behavior — a bug, a crash, wrong output, a broken build that isn't a `build`-type issue. Requires scope. |
| 10 | `feat` | Adds new user-facing or API-facing capability that did not exist before. Requires scope. |
| 11 | `chore` | Everything else: housekeeping with no build/dependency angle — `.gitignore`, LICENSE, non-code repo hygiene. This is the fallback, not a first choice. |

### Explicit tie-breakers (the cases that actually cause disagreement)

- **New Compose screen that also required a small unrelated bug fix to compile** → split into two commits: `feat` for the screen, `fix` for the bug. Never merge them under one type.
- **Refactor that happens to unblock a future feature but ships no new capability today** → still `refactor`. A refactor doesn't become `feat` because it's a prerequisite for one.
- **Gradle dependency version bump with no build-script structure change** → `build`, scope `deps`.
- **Gradle version catalog / plugin structure change** → `build`, scope `gradle`.
- **Koin module wiring change with no new bindable capability** → `refactor`, scope `di`. If it wires up a *new* feature's DI graph for the first time → `feat`, scope `di` (or the feature scope if the DI is feature-owned — see Step 2).
- **Konsist rule addition or change** → `test`. Point a new rule at a real violation before committing it — see the last section of `agent-architecture-convention.md`.
- **Cannot confidently place a diff in a single type** → split the commit into smaller commits until each one has an unambiguous type. Do not commit a mixed diff under a guessed type.

---

## Step 2 — Determine `scope`

Cross-cutting technical scopes take priority over feature scopes. Check in this order:

1. **Does the change belong to one of the fixed cross-cutting scopes?** `network`, `database`, `di`, `navigation`, `theme`, `gradle`, `deps` — these describe a technical concern, not a feature, and apply regardless of which feature triggered the work. Use these whenever the change's primary intent is the concern itself (e.g. "add retry policy to Ktor client" → `network`, even if it was needed for one feature).
2. **Is the change confined to a single feature module, and not primarily about one of the concerns above?** Use that feature's scope (`feature-a`, `feature-b` — replace with this project's actual feature slugs, e.g. `dashboard`, `quiz`, `auth`). A change is feature-owned when the feature is where the change lives *and* the feature is the reason the change exists.
3. **Does the change span two or more features with no single owner, and isn't one of the fixed technical scopes?** Use `core`, which is also the package such code lives in.
4. **CI-only or doc-only changes** — scope is optional; omit it unless a specific scope adds clarity (e.g. `ci(gradle): cache konsist test results`).

### Tie-breaker
If a change could plausibly be tagged with a cross-cutting scope *or* a feature scope, ask: "is the primary intent the technical concern, or the feature behavior?" A new Ktor endpoint call added only to support one feature's data layer is still `network` if the interesting part of the diff is the client/transport code; it's the feature scope if the interesting part is how that feature uses the response.

---

## Known irreducible ambiguity (flagged, not solved)

These two cases genuinely depend on judgment this doc can't fully remove. When you hit one, pick a side, state your reasoning in the commit body in one line, and move on — don't stall on it:

- **fix vs. feat** for behavior that was "obviously intended eventually" but never implemented (missing validation, unhandled edge case). If it was ever *working as designed*, treat it as `fix`. If the design never covered this case at all, treat it as `feat`.
- **refactor vs. build** for build-script restructuring that also changes module boundaries (e.g. splitting a Gradle module). If the primary output is a build-graph change, `build`. If the primary output is a code-architecture change that happens to require touching Gradle files, `refactor`.

---

## Project scopes

The `scope-enum` in `commitlint.config.js`, the `Scopes:` bullet in `README.md`, and this section must never drift apart. A CI job lints every commit message on a pull request, so a scope that exists in one file and not another fails the build rather than passing quietly.

**Cross-cutting technical scopes** — check these first, per Step 2:

`network`, `database`, `di`, `navigation`, `theme`, `core`, `app`, `gradle`, `deps`

`app` covers the composition root itself — `App.kt` and the shell. Prefer `di` for Koin wiring and `navigation` for the route graph; reach for `app` only when the change is the composition and nothing narrower fits.

**Tooling:**

`datagen` — the dataset generator, which is a Gradle module that is never shipped in the app.

**Feature scopes**, one per feature package:

`home`, `pokedex`, `search`, `moves`, `abilities`, `items`, `locations`, `typechart`, `teambuilder`, `compare`

Note that `pokedex` is the species-and-variants browsing feature, not the app. The app is Tall Grass; a Pokédex is the thing it contains.
