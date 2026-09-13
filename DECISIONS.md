# DECISIONS.md

Why this app is the way it is.

This file exists because `agents/agent-documentation-convention.md` routes one of the four kinds of
prose here, and a routing rule with no destination sends everything back into KDoc. **It starts
nearly empty on purpose.** The entries below are the template's own decisions; a fork adds its
product's decisions underneath and deletes nothing.

## What belongs here

A measurement, a rejected alternative, or a trade-off that was weighed — *cards cost +32% vertical
space*, *this colour measures 1.9:1 against the surface*, *22sp looked like a caption*. Anything
answering **"why is the app like this?"** rather than "what does this do" or "why is this line
surprising".

It belongs here **even when the code it justifies is right there**. Especially then: it is the code
being right *for a reason* that makes the reason worth keeping, and the reason outlives the file.

## The shape of an entry

A `###` heading that reads as a claim, then the reasoning, then what was rejected and why. The
heading is an anchor — code points at it with `// DECISIONS.md § The heading`, so headings are
renamed with the same care as a public function.

---

### `design/` is a peer of `app/`, not a package inside it

The design system is the most depended-on code in an app like this: in the project this template was
forked into, 254 of the 259 imports of `app/` from below targeted `app/design/`. Meanwhile `app/` is
meant to be terminal — it imports everything and nothing imports it — which is the property that
makes "nothing may import `app/`" a rule a machine can check.

Those two facts cannot both be true of one package. `app/design/` made the dependency graph run
`app -> core -> app`, and no rule saw it, because every package rule keyed on `feature` and neither
end of the cycle was one.

**Rejected: keeping the design system in `app/` and exempting it.** An exemption for the most-imported
package in the codebase is not an exemption, it is the rule not existing.

**Rejected: a top-level `ui/` package** holding domain-aware shared rendering, so that `design/`
could stay closed. It failed on population: applying the contract test consistently — *does this
component own a rule about the subject, or only about appearance?* — moves almost everything into
`design/`, and what is left belongs beside the model it renders, in `core/<slice>/presentation/`.

### `core/`, not `common/`

`common` says *put it here if several things need it*, which is a rule about callers: it can become
true without the file changing, and it cannot be answered by reading the file. That is how a
`common/` package becomes a dump.

`core` says *put it here if it models the subject*. That question is answerable from the file alone,
which is the property a placement rule needs.

### A preview brings its own ground, and renders both themes from one body

Compose's preview pane paints its own white behind whatever it renders. A screen that does not fill
its background therefore looks correct in light mode and shows dark-theme text on a white sheet in
dark mode — legible enough in the pane to pass a glance, and nothing like what the device does. The
harness paints the background so the dark rendering is dark before a single component draws.

The theme comes from the system flag rather than a `darkTheme` parameter: the tooling sets the ui
mode, the theme reads `isSystemInDarkTheme()`, and the rendering is then the real dark theme rather
than a preview-only override.

**Rejected: one preview function per theme.** Two functions with identical bodies and one differing
argument drift apart, and a preview that has drifted from its own dark twin still looks fine in
review. A multipreview annotation renders both from one body, and a third rendering later — a large
font scale, a small screen — is a change to the annotation rather than to every preview in the app.

### A screen waits as its own silhouette

A centred spinner is the same picture on every screen in every app and says only that something is
happening. A skeleton says what is about to arrive, in the shape it will arrive in, so the real
content lands in a layout the eye has already settled on rather than replacing one.

The pulse and the block colour are design tokens; the geometry belongs to the screen, because the
geometry is the entire point. That is why `design/theme/Shimmer.kt` is a token and not a
`LoadingScreen` component — there is nothing general to draw.

**Rejected: a travelling gradient highlight.** It needs a brush animated per frame across every
placeholder, which is work spent on the one screen that is by definition waiting for something else.
One alpha, provided once, animates a single value however many blocks read it.

### The detail screen reads its record once, rather than observing it

`DetailStoreFactory` does a one-shot read instead of collecting a `Flow`. Clearing the history from
the main screen while the detail screen is open therefore leaves the record on screen stale rather
than making it disappear underneath the reader.

That is the accepted trade for the example feature, not an oversight. Observing would be the right
call for a screen whose subject genuinely changes while it is open; here the alternative is a screen
that empties itself in response to a gesture made somewhere else, which is worse for the reader and
more machinery to demonstrate a pattern with.

### The Konsist task always runs

Konsist builds its scopes from strings at runtime, so Gradle cannot see that `:shared`'s sources are
inputs to `:konsistTest:test`. It marked the task UP-TO-DATE and replayed the previous result, which
turns a violation into a green run.

**Rejected: declaring the real inputs.** `scopeFromProject()` means "every `.kt` file in the repo",
which is a `fileTree` over the root at configuration time — awkward under the configuration cache,
and silently wrong again the day a scope widens. The task takes seconds. Correctness is worth more.

---

## Tall Grass

Decisions belonging to the product rather than the template it was forked from.

### The user-owned database ships with no tables

`app.db` holds only what the user creates — favourites, saved teams, the remembered game, settings — and none of that exists yet. Deleting the example feature took the only `.sq` file with it, and SQLDelight generates no database class from an empty source set, so the driver, the Koin module and `verifyCommonMainAppDatabaseMigration` all stopped compiling at once.

A `.sq` file containing only a comment fixes it: SQLDelight emits a valid `AppDatabase` interface with no queries, and the migration snapshot regenerates as an empty schema.

**Rejected: inventing a first table.** A favourites table nobody reads is dead code that looks like a feature, and the schema would be guessed months before the screen that uses it.

**Rejected: deleting the database plumbing until something needs it.** It would tear out one of the four mandatory verification commands and the `expect`/`actual` driver pair, to rebuild both from memory one ticket later. The dataset work needs a database immediately.

### A feature's `NavKeyHandler` is bound in `app/`, not in the feature's own DI module

`agents/agent-architecture-convention.md` says each feature owns its `di/` module, and a Konsist rule says a DI module may only import from its own subtree. The binding needs `infra`'s `NavKeyHandler` interface on one side and the feature's handler on the other, so it is cross-subtree by construction and cannot live in the feature.

`app/navigation/AppNavigationModule.kt` is where it goes — `app/` is terminal and is the aggregator the rule deliberately exempts.

This was discovered by the rule failing, not by reading it. Worth knowing before a feature grows a DI module and someone moves the binding into it for symmetry.

### The navigation host is wrapped in `SharedTransitionLayout` before any screen needs it

The grid-to-detail transition is the signature interaction of the app, and shared elements need matched keys on both sides with source and target composables structured to share bounds. That is not a property that can be added to finished screens without reopening all of them.

So the wrapper is in `NavGraph.kt` from the first screen, with the scope published as `LocalSharedTransitionScope`. Everything else in the animation catalogue — staggers, stat bars, the blur-up crossfade, list entrances — genuinely is paint-on-top and is deferred.

**Rejected: passing the scope down as a parameter.** A shared element is declared deep inside a screen, on a card's image rather than on the screen composable, so a parameter would thread through every layer in between. The composition local is in the ktlint allowlist for that reason.

**The local fails loudly rather than defaulting to null.** Reading it outside the host is a wiring mistake; a null default would turn that into a transition that silently does not run, which is the hardest kind of animation bug to notice.
