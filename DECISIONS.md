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

### The dex grid is three cards across, loaded whole

Three columns keeps the artwork large enough to recognise at a glance while putting a generation a
few flicks apart. Two reads as a list and wastes the width; four shrinks the artwork to the point
where the tint is doing most of the identifying.

The screen reads all ~1,080 rows in one query and holds them. Paging buys nothing here: the dataset
is on the device, the rows are small, and a Pokédex that cannot be scrolled to its end without a
round trip is worse than one costing a few hundred kilobytes of heap.

**Rejected: keying the grid by Dex number.** Vulpix and Alolan Vulpix are both #037, so the key would
collide and the list would lose its scroll position whenever a form was involved. The slug is unique
and is already the route key.

### A form's kind is derived and stored, not computed at read time

`form` is upstream's raw identifier and is an open set — 164 values, 134 of them used once, so
`alola` and `rock-star` sit at the same level and nothing can filter on it. `formKind` is a closed
taxonomy derived from it once, at generation, and committed.

Deriving it in the app instead would put the classification behind a release: a form filed wrongly
would be wrong on every device until the next build, and invisible until someone noticed a Mega in
the wrong list. Committed, it appears in a pull-request diff — the same mechanism the ability
categories use.

**The cosmetic test compares stats, types *and* abilities.** Stats and types alone file eight real
forms as costumes, because an ability is the only thing separating them from their base form.

### The database ships with its schema version stamped

`Schema.create` builds the tables and leaves SQLite's `user_version` at 0, and that pragma is the
only thing a driver reads to decide whether a database is empty. Shipped at 0, the app opened a
populated file, concluded it was blank, and tried to create the tables a second time.

It is unreachable from any host test: the generator writes the file, the tests build their own in
memory through the same `create` path, and none of them re-open a populated database the way first
run does. So the build stamps the version and reads it back, and the check was verified by shipping
a deliberate 0 and watching it fail.

### The destination carries what the tapped card was already drawing

A `DetailDestination` holds a `HeroHandoff`: the artwork URL, the primary type's slug and the
shared-element key. None of it is read from the database, and all of it is on screen before the tap.

Both halves earn their place by removing a different flicker. Artwork resolved on arrival renders
empty for a frame, which is the frame the shared element exists to hide. A tint learned from the
database changes colour under the reader a moment after the screen opens — less obvious in a
screenshot, more obvious in the hand.

**The type travels as a slug, not as a packed colour.** Which colour a type is drawn in is a
decision the design system makes; a destination that carried an ARGB value would be remembering the
answer to a question it is not allowed to ask.

**Rejected: loading the hero from the Store like everything else.** It would make the hero wait on
an answer it already has, and the loading state of a five-query local read is one frame — exactly
the frame that has to be right.

### A shared-element key names its source

`SharedElementKey(source, id)` rather than the id alone. The detail hero has four entry points under
#11's rule — dex, search, locations, and later the team builder — and two of them can be composed at
the same time, because the host cross-fades between screens rather than swapping them.

With the id alone, a dex card and a search result for the same Pokemon would match while that
cross-fade runs and animate a transition nobody asked for. The key is built by one function per
source, so the sending and receiving halves cannot drift: a literal on each side would compile,
run, and simply not animate.

**The hero's key follows the form on screen, not the form that was tapped.** Switching to Mega
Charizard X swaps the picture; a hero that kept `dex/charizard` would fly the Mega's artwork back
into Charizard's card. Keyed by the active form, the key matches no card after a switch and the two
screens cross-fade, which is the honest answer.

**Rejected: dropping the modifier when the form is not the tapped one.** `rememberSharedContentState`
is a `remember`, and moving it in and out of the composition on every tap of the switcher is a
composition bug waiting for a slot to shift under it.

### The About tab is a function of the species, with the form passed in

`AboutUiModel` is built by an extension on `PokemonSpecies` that takes the variant as a parameter,
and the variant is read for exactly two fields: height and weight. Everything else in the tab is
breeding and training, which are true of Vulpix whichever region it is from.

The rule is #5's, and the point of writing it into the mapper's signature is that it stops being a
rule anyone has to remember. Breeding data moving when the form changes is the specific bug the
Species/Variant split exists to prevent, and a mapper that cannot see the variant cannot cause it.

### Which tab is open lives in the Store, as a nested enum

The Contract may not name a `UiModel` and a Screen may not name a `State` — two Konsist rules that
meet head-on over a tab, which is neither a domain type nor a rendering one. `DetailState.Tab` is
nested inside the State rather than declared beside it, which is what keeps the Contract to the
Store's own vocabulary.

The crossing happens in the Route, which shares a package with the Contract and so needs no import
to see both sides. It is the only place in the screen that sees both, and it is already the file
that turns callbacks into Intents.

**Rejected: keeping the tab in the Screen as `rememberSaveable` state.** It would work, and it would
leave the form switcher — the same kind of choice — in the Store and the tab outside it, for no
reason a reader could recover.

### A stat bar is full at 160, not at 255

Blissey's 255 HP is the real maximum and almost nothing else comes near it. Scaling every bar to it
squashes the ordinary range into the left third, where the differences the tab exists to show stop
being visible. The bars are drawn against 160 and clamped, so the handful above it read as full.

This is a rendering choice and the number is in the mapper, not the domain: the stat is 255 whatever
the bar does with it.

### The preview harness opens the navigation host's scopes

`TallGrassPreview` wraps its content in a `SharedTransitionLayout` and an `AnimatedContent` that
never changes state, so `LocalSharedTransitionScope` and `LocalNavAnimatedContentScope` both resolve.

Both composition locals fail rather than defaulting, which is deliberate — a null default turns a
wiring mistake into a transition that silently does not run. The cost is that any screen carrying a
shared element is unpreviewable without a host, and the transition is the point of several of them.
The harness already stands in for the shell's theme and background; these are two more things the
shell provides.

### The form switcher lists forms that change something

Cosmetic forms are in the dataset and out of the switcher.

Pikachu is the case that forces it: seventeen variants, fourteen of them costumes — Rock Star, Pop
Star, Ph.D., Libre, Cosplay and eight regional hats — all carrying Pikachu's types, stats and
abilities exactly. A row of seventeen pills whose numbers never change is a worse screen than no row,
and it buries Partner Pikachu and Gigantamax, which do change something.

They stay in `PokemonDetail.variants` and are dropped at the presentation edge. Whether a costume is
worth showing is a question about a screen rather than about the Pokemon, and a count of them — the
dashed `+N cosmetic` pill in the prototype — needs them present to be counted.

**An unknown `formKind` reads as `ALTERNATE`, not as null.** Every other enum read from the dataset
here drops its row when it cannot parse the value, because an unreadable type or growth rate makes
the row meaningless. A kind is different: it says how a form differs, not what it is. Failing to
recognise one should cost a label, not a Pokemon.

### Two art sources, because thirty-four forms have no official artwork

Almost every variant points at PokeAPI's `official-artwork`, which is what the dex grid is designed
around. The Arceus Plates and Silvally Memories point at the `home` renders instead.

They have to. Those thirty-four forms have no `pokemon` row — they exist only in `pokemon_forms`,
which is the whole reason they had to be promoted into variants in the first place — and the artwork
set is keyed by `pokemon` id. `official-artwork/493-fighting.png` is a 404. The HOME set is keyed by
form and has all thirty-four.

**The alternative was showing the base form's picture eighteen times.** On a screen whose purpose is
to show what changes between forms, that is a worse answer than a change of art style, and the style
only ever appears in the detail hero: none of these forms earns a card in the grid.

**Two id spaces, both in the ten-thousands.** `pokemon` and `pokemon_forms` are numbered separately,
so an id from the wrong one produces a URL that resolves to another Pokemon rather than a 404 — Mega
Mewtwo X for Dragon Arceus. `DatasetTest.noTwoVariants_shareOnePicture` is the tripwire, because
nothing downstream can tell a right picture from a wrong one.

### Three curves and five durations, measured rather than chosen

M3's springs are the default vocabulary and most of the app should keep using them. `AppEasing`
carries three exceptions because matching the Flutter reference is an explicit goal of this project
and a spring cannot reproduce a 600ms `easeOutQuint` settle. See #12.

Four of the five durations in `AppDuration` are read off that reference rather than picked;
`MEDIUM` is the one derived value, sitting where a step between `SHORT` and `LONG` was needed.

**The stagger counts from the top of the viewport and caps at eight.** The dex is 1,082 cards; at
55ms of absolute index, card 500 would enter twenty-seven seconds in, which is not a stagger but a
bug that looks like a hang. Capped, the ninth visible item and everything after it start together at
385ms, and a full entrance takes the same time whether the viewport holds nine cards or ninety.
### One entrance clock per screen, not one animation per item

A lazy list makes the obvious approach wrong. An entrance owned by the item re-runs every time that
item scrolls back into composition, so the dex re-animates cards the reader has already seen — and
remembering a flag per item does not help, because the composition is recycled along with everything
in it.

`rememberEntranceClock` runs one animation for the whole screen, in milliseconds since the content
landed. Each item reads its own slice of that clock through `entranceFraction`, offset by its
position in the viewport. An item composed after the clock has stopped reads 1 and draws with no
animation state of its own, which is the property that makes scrolling free.

The stagger each item reads is capped, so a deep viewport does not enter more slowly than a shallow
one. See § Three curves and five durations, measured rather than chosen.

**An entrance happens once per screen, not once per visit.** Opening a detail throws away the dex's
composition — the host keeps the back stack, not the layout — so a plain `remember` is gone by the
time the reader comes back, and the whole grid would cascade in again for a list that never went
anywhere. The fact that it has already run lives in `rememberSaveable`, which the host's state holder
restores along with the scroll position.

### Reduced motion is answered per category, not left to the duration scale

Compose already scales every animation's duration by the system factor, so doing nothing would be
*something*: animations would run in a single frame. That is the right answer for a stat bar, whose
length carries the number, and the wrong one for everything else.

- **Entrances and staggers** are switched off at the source — the clock snaps to finished, items
  appear together, and no animation is started to be scaled down.
- **Navigation** becomes a cross-dissolve chosen explicitly, because a slide at zero duration is a
  hard cut, and on iOS that is the documented behaviour rather than the cross-dissolve UIKit does.
- **The shimmer stops.** An infinite repeat at zero duration flickers between its two alphas as fast
  as the display allows, which is the worst possible response to a request for less movement.
- **Stat bars keep animating.** The movement is the information.

### The navigation host takes its motion as a parameter

`infra/` may not import `design/`, and the curves and durations are design tokens. So `NavGraph`
takes a `NavTransitions` and the app composes it from the token layer, with a plain cross-dissolve as
the default for a host that has nothing better to say.

This is the same shape as the insets decision above: the host owns the mechanism and the screen — or
here, the composition root — owns the answer.

### The artwork corpus is 133 MB, and the disk cache is sized against it

Measured, not estimated: every artwork URL in the committed dataset was asked for its length.

| | Files | Bytes |
|---|---|---|
| Dex cards (`listedInDex`) | 1,082 | **132.6 MB** |
| Every variant, forms included | 1,385 | 166.2 MB |
| Average / largest single image | | 123 KB / 289 KB |

The disk cache ceiling is **192 MB**. It has to clear the prefetched set or the cache thrashes —
later images evict earlier ones and the run undoes itself — and the headroom above it covers the
forms, which are fetched lazily and would otherwise start evicting cards.

A byte cap rather than a percentage of free space, because this corpus has a knowable size: a
percentage hands a 512 GB phone a quota nothing will ever fill, and a nearly-full phone one too small
to be worth writing to.

**Coil ships no disk cache unless it is given one.** Until this was configured, every artwork in the
dex was re-fetched on each cold start — which, in an app whose only network use is images, was the
whole of its offline story.

### The prefetch has no cursor, because the disk is the cursor

Resuming a half-finished run needs to know what was already fetched. The obvious answer is to record
progress — which means a table, a migration, and a number that can disagree with reality after the
system empties the cache directory, as it is entitled to do.

Instead each URL is asked of the cache before it is fetched. What is on disk *is* the progress, it
cannot be stale, and a run killed halfway resumes by finding its own earlier work. The cost is 1,082
cache lookups on a second visit, which is a few hundred milliseconds on a background dispatcher.

**Storage exhaustion is a check, not a caught exception.** A write that runs out of room surfaces as
an `IOException` whose message differs by platform and filesystem, and matching on that string is a
guess. The run asks the platform how much room is left, every fiftieth image, and stops at a 64 MB
floor — early, because the device does not belong to it.

### The prefetch starts with the dex, not with the app

#32 says "on first run". It begins when the Pokedex is first opened instead.

The purpose is that browsing the dex works offline, and that is still what happens: open it once with
a signal and it is filled. What changes is that somebody who opens Tall Grass, looks at the home
screen and leaves does not pay 133 MB for a screen they never reached.

**This is a deviation and should be read as one.** The literal reading is defensible too — artwork
ready before the user asks for it — and reversing it means moving the call, not rewriting anything.
