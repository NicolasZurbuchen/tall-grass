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

### The dex grid is two cards across, loaded whole

Two columns is what the card needs once it carries the name, the form label and both type pills down
its left side while the artwork fills the corner. At three across there is no width for a pill and a
name on the same card, so the card was a picture with a caption — and of the two things a reader
actually scans a dex for, what it is called and what it is, one of them was missing.

**This reverses the earlier three-across decision**, which held that two columns "read as a list and
wasted the width". It does read as a list, and that turns out to be the point: the cards are now wide
enough to be read rather than only recognised, and a generation is still a few flicks apart because
each card is shorter than it was.

The screen reads all ~1,025 rows in one query and holds them. Paging buys nothing here: the dataset
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

**The total's lane is full at six times that**, which makes it the mean of the six above it. Any
other ceiling — the highest total in the dataset, say — would put the total on a scale of its own,
and the one thing a reader does with a column of lanes is compare them down it.

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

**The stagger counts from the top of the viewport and caps at eight.** The dex is 1,025 cards; at
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
| Dex cards, as measured | 1,082 | **132.6 MB** |
| Every variant, forms included | 1,385 | 166.2 MB |
| Average / largest single image | | 123 KB / 289 KB |

Those were taken when the dex held 1,082 cards. It holds 1,025 now — see *The dex lists one card per
species, forms behind it* — so the prefetched set is smaller than the first row says. The ceiling is
sized against the corpus rather than against the dex, so it does not move.

The disk cache ceiling is **192 MB**. It has to clear the prefetched set or the cache thrashes —
later images evict earlier ones and the run undoes itself — and the headroom above it covers the
forms, which are fetched lazily and would otherwise start evicting cards.

A byte cap rather than a percentage of free space, because this corpus has a knowable size: a
percentage hands a 512 GB phone a quota nothing will ever fill, and a nearly-full phone one too small
to be worth writing to.

**Coil ships no disk cache unless it is given one.** Until this was configured, every artwork in the
dex was re-fetched on each cold start — which, in an app whose only network use is images, was the
whole of its offline story.

### The dex is mapped once per list, not once per state

`DexState` carries the entries and the prefetch progress together, so every prefetch report was a
new state, and mapping it rebuilt all 1,082 cards — 11ms a pass, and around 27,000 throwaway
UiModels across a run, during exactly the window the reader is scrolling.

`DexViewModel` keeps the cards it already built and `DexState.toUiModel` takes them as a parameter.
The check is **identity**: the reducer copies the state and leaves the list alone, so the same
instance coming back is precisely the signal that nothing about the entries changed, and comparing
by equality would walk all 1,082 to learn it.

**The mapping also runs off the main thread.** `viewModelScope` is the main dispatcher, so without
the `flowOn` every state change built the whole dex on the thread drawing the frame — measured at
22ms cold against the real dataset, landing exactly when the grid first appears.

### The prefetch reports in slots, not per image

A run emits when its progress crosses one of twenty-five slots, not once per image.

Every emission is a new `DexState`, and mapping that state rebuilt all 1,082 dex cards — about a
millisecond each time. A warm cache walks the list as fast as the disk answers, so the emissions
arrive in one burst, and the burst lands on the frame where the shimmer gives way to the list:
**236ms measured across a full run**, which was a visible freeze.

Twenty-five is more resolution than a percentage on one line of text can express, which is all the
banner shows.

### The prefetch has no cursor, because the disk is the cursor

Resuming a half-finished run needs to know what was already fetched. The obvious answer is to record
progress — which means a table, a migration, and a number that can disagree with reality after the
system empties the cache directory, as it is entitled to do.

Instead each URL is asked of the cache before it is fetched. What is on disk *is* the progress, it
cannot be stale, and a run killed halfway resumes by finding its own earlier work. The cost is 1,025
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

### Only the tapped card is a shared element

Every visible dex card once carried `Modifier.sharedElement`. At most one of them can ever transition
— the one that was tapped — and the other seventeen cost the first layout of the grid dearly.

Measured on a Galaxy S25, debug build, prefetch disabled on both sides, five cold opens of the dex
from the home screen each, timed from the grid's first composition to its first draw:

| | every card | tapped card only |
|---|---|---|
| samples | 790 797 829 919 975 ms | 223 290 355 383 529 ms |
| median | 829 ms | 355 ms |

Instrumenting the phases of one such open attributes it:

| | every card | tapped card only |
|---|---|---|
| approach-pass card measure | 111 ms | 14 ms |
| measure to first draw | 337 ms | 32 ms |

`SharedTransitionLayout` puts everything under it in a `LookaheadScope`, so the grid is measured
twice, and every shared element pays for both passes plus a layer of its own. Eighteen of them land on
the one frame where the shimmer gives way to the list. Read the release numbers at the end of this
entry before concluding that this was the reported freeze: it was not.

`DexCard` therefore takes a nullable key. Null draws the same picture and registers nothing; the
screen hands the real key to the card whose slug matches `heroSlug`, set in the click handler before
the navigation label makes its way back. The registration lands two frames ahead of the transition
starting, which was confirmed by logging `SharedContentState.isMatchFound` on both legs of the trip.

`heroSlug` is `rememberSaveable` and not `remember`, for the reason the entrance clock's flag is: the
host disposes this composition while the detail is open, and the way back needs the sending half of
the transition to still be here to match against.

**This is not the case rejected under "A shared-element key names its source".** What was rejected
there is dropping the modifier as the *form switcher* is tapped, on a screen where the key itself
changes and the modifier would come and go repeatedly. Here the key is fixed per card and the
condition flips at most once, on the tap that ends the screen.

**Every number above is a debug build, and that turned out to be most of the story.** The same five
cold opens against a release build:

| | every card | tapped card only |
|---|---|---|
| median compose to first draw | 25 ms | 20 ms |
| frames dropped | 0 | 0 |

So the freeze does not exist in a release build, and this change is worth 5 ms there rather than
474 ms. It is kept because it is less work by construction -- seventeen registrations that cannot
pay off -- and not because it rescues the screen.

**The lesson is the one about where it was measured.** `debuggable` costs roughly 18x on composition
and layout here: ART holds back its optimisations and the Compose compiler keeps source information
and trace calls in every composable. A screen that janks in debug and not in release is the normal
case, and nothing in this repo said so before.

### A screen reached from an element does not also arrive from the side

The detail used to slide in from the right like every other destination, on top of a shared element
that was simultaneously saying it came from a card in the middle of the grid. Two answers to the same
question, and the reader gets both at once.

Such a destination cross-dissolves instead, and the matched element carries the movement by itself.
Everything else still slides, because a screen with nothing shared has nothing else to say about
where it came from.

The host is told by entry metadata — `SharedElementEntry`, passed at `entry<DetailDestination>(...)`
— rather than by a marker interface on the key. It is a fact about how the host draws the
destination, not about the destination, and navigation3 already hands metadata to the display for
this. Both directions are checked, because a transition is shared-element on the way back for the
same reason it was on the way in.


### Rejected: the card's colour travelling into the detail

Only the artwork is shared between a dex card and the detail. The card's colour stays on the card.

**This was built, it worked, and it was removed.** A dex card is a rectangle of the type's colour and
the detail's header is a band of the same colour, so the band can be the card's, grown — a second
shared element keyed `dex-tint/<slug>` travelling beside `dex/<slug>`. Frame-by-frame on a device at
10x animator scale it does exactly that: the colour lifts off the tapped card and expands into the
header with the Pokemon riding above it.

Two defects made it read as broken rather than as motion, and neither is cheap:

**Everything drawn on the colour appears all at once when the animation ends.** The header's name,
number, type pills and genus are not part of the shared element, so they are subject to the screen's
cross-dissolve while an opaque band sits over them in the shared-element overlay. They become visible
only when the overlay lets go, which is a hard pop at the exact moment the motion finishes — the
frame that should be the calmest.

**The card's rounded corners turn square the instant it starts moving.** `sharedBounds` interpolates
bounds, not shape. The radius comes from a `clip` on the card's side and the destination has none, so
there is nothing to interpolate and the corner is gone on the first frame. Animating it means
animating a shape through the transition's own fraction, which is a custom modifier rather than a
parameter.

Both are solvable — the first by making the header content part of the shared content, the second by
a shape that reads the transition — and together they are more machinery than a colour is worth
today. Recorded here because the approach is sound and the next person to have this idea should start
from the two problems rather than rediscover them.

The sizing lesson is worth keeping even so. The travelling layer has to be **exactly** the coloured
region, and three of the four ways to size it fail:

| the shared layer | what happens |
|---|---|
| Full screen, in the overlay | Covers the header, the sheet and every word for the whole flight |
| Full screen, drawn in place | Z-order is right, but it grows from the top-left corner of the screen rather than from the card |
| The header band only | Lands correctly, but the full-screen tint behind it is still fading up, so an opaque rectangle sits on a paler one and the seam shows |
| The band down to the sheet's top edge | The only one where the travelling layer and the coloured region are the same rectangle |

That last row is what the implementation reached, and it is where a second attempt should start.
### The database opens on the first query, not on the first injection

The data sources take `Lazy<Queries>` and the Koin modules bind them with `lazy { … }` rather than
`get<PokedexDatabase>().variantQueries`.

Koin resolves a constructor's dependencies on whichever thread first asks the graph for the object,
and here that is `koinViewModel()` — during composition, on the main thread. Opening the SQLite
database and, on a first launch, copying the 1.2 MB bundled dataset out of the APK are both real work
and neither belongs there. Deferred to the first `.value`, both happen inside the first query, which
already runs on `Dispatchers.Default`.

**The wrapper is the whole mechanism, so it has to survive a refactor.** A data source that took
`VariantQueries` directly would compile, pass every test, and quietly move the open back onto the
main thread — nothing would go red. The `Lazy` in the constructor signature is what makes that
regression visible at the call site.

Measured on a Galaxy S25: the first read of the dex is 37ms on a background thread. What that buys is
not the 37ms — it is that they are not spent while the navigation transition is drawing.

### The whole card travels, not just the artwork

Only the artwork was a shared element at first, and the name and the types cross-faded under it. At
two columns the card carries all three at a size the eye tracks as readily as the picture, and a name
that dissolves while the artwork flies reads as two unrelated things happening at once.

Making them travel forced the name and both type slugs onto `HeroHandoff`, beside the artwork URL
that was already there. A shared element matches only if the receiving half is composed when the
transition begins, so anything the detail could not draw until the database answered would have
cross-faded instead — which is what it was doing.

**Rejected: one shared element for the pill row.** The pills are stacked on the card and in a row in
the header, so a single container would have had to morph one layout into the other mid-flight. One
key per pill lets each fly its own path and asks nothing to reflow.

### A type pill on the type's own colour is a scrim, not a colour

Every pill the app draws sits on a ground that is already the primary type's colour: the dex card
takes its tint from it and so does the detail hero. Filled with `TypeUiModel.color`, the primary
pill is therefore exactly the value behind it — Ivysaur's Grass pill is `0xFF7AC74C` on a
`0xFF7AC74C` card, and only its white label shows. Only the *secondary* type was ever legible.

White at 25% makes both pills the same shape and both readable, and it keeps them stable in flight:
the pills travel from the card into the hero as shared elements, and two pills that change fill on
the way read as two objects rather than one.

Nothing about the type is lost. Its colour is still on screen — carried by the largest surface there
instead of by a 60dp chip.

**Rejected: keeping a coloured variant behind a parameter.** A branch with no caller is not an
option, it is dead code that reads as coverage. When a screen draws a pill on a neutral ground — the
type chart is the obvious one — the variant comes back then, with something to check it against.

### A screen title is a heading, not a toolbar label

Material's `TopAppBar` sets the title beside the navigation icon at body size, which is right for a
screen you are several levels inside: the title is chrome, confirming where you are while you read
something else. The dex is one tap from home and its title is the first thing on it, so it takes the
line under the back arrow at headline size and the content starts below.

This also keeps the two halves of the screen honest about their padding: the header indents to the
same gutter as the grid's content padding, so the title sits on the same vertical line as the first
card rather than on Material's own inset.

### The entrance belongs to the arrival, not to the content

The entrance clock was keyed on the active form, and every use of the switcher replayed the whole
screen: the tab row rose, the tab's content rose behind it, the matchup chips popped in one at a
time. Keying it on the content arriving fixed that and broke the same thing again the moment the
carousel landed, because a swipe empties the sheet and refills it — which is a content change by any
definition, and not an arrival by any.

So the key is latched: false until the first read lands, true from then on, never false again. The
entrance runs when the screen fills and not when anything refills it.

The latch rather than keying on the screen opening, because a clock started while the sheet is still
a skeleton can be finished before there is anything to enter, and a slow read would then deliver its
content already settled.

What still moves on a switch or a swipe is what actually changed: the tint, the artwork, the name,
the number, the types. Those are the same screen becoming something else, which is the thing worth
animating.

### Rejected: counting the stat figures to their new values

The bars grow to their new lengths when the form changes, so the figures beside them were made to
count to theirs on the same curve, duration and delay. It was asked for, built, and watched on a
device: Attack reading 86 on its way from 84 to 130 while its bar filled underneath.

It reads badly. A bar growing is a quantity changing; a number spinning is a slot machine, and the
eye goes to it instead of to the six bars that carry the comparison. Worse, the figure is the precise
value — the one thing on the row that is supposed to be readable at a glance — and for the length of
the animation it is a number the Pokemon does not have.

So the figures cut and the bars move. `StatBarUiModel.valueText` and `StatsUiModel.totalText` are
formatted strings again, like every other display value on this screen.

### The stat bars answer a form switch together

The six bars were staggered by 55ms each, which is the app's entrance stagger applied to a movement
that is not an entrance. On a form switch the effect is that the row you are looking at waits up to
275ms before it starts, and the switch reads as the screen being slow to respond rather than as six
numbers changing at once.

The stagger is right when items are *arriving* — the eye needs somewhere to start. Here nothing
arrives: six bars that are already on screen change length. They start together.

(The bars never staggered on first view of the tab anyway. `animateFloatAsState` initialises at its
target, so the first composition has nothing to travel from, and the stagger only ever applied to a
switch — the one case where it was wrong.)

### A matchup chip is sized by its name, not by the grid

The chips were three across in a hand-chunked grid, each stretched to a third of the width, so
"Electric ½" and "Bug ½" occupied the same space and the last row was padded with blanks to keep the
columns. It reads as a table of a fixed shape rather than as a list of the types that happen to
matter, which for a single-typed Pokemon can be as few as five.

They wrap instead, each sized to its own text. What is lost is the row index the stagger used, since
a flow does not report where it broke; the stagger is per chip now, and `AppStagger`'s cap holds
eighteen of them under four hundred milliseconds, which is what the row grouping was there to avoid.

**The label is the type's own colour, shifted.** A chip filled with `TypeUiModel.color` at full
strength is the dex card's problem again — see *A type pill on the type's own colour is a scrim* — so
the ground is an 18% wash of it and the label is the same hue moved 45% toward black on a light
sheet, or toward white on a dark one. Pure Grass measures 1.9:1 against white; the shift is what
makes it a colour rather than a suggestion, and it has to know the theme because "darker" is only
legible in one of them.

### Text that trails a heading enters from the side

The dex number and the genus sit at the trailing edge of their rows, beside the name rather than
under it. Entering them downwards with `heroUp`, as the header did, made four things drop in
formation and read as one block arriving — which is not what the layout says they are.

They slide in from the trailing edge instead, 48dp against the vertical entrances' 16 and 24: a
sideways movement has the whole width to read against, so the same distance registers as less.

`slideInFromEnd` takes the layout direction rather than defaulting it, because `graphicsLayer` is
handed a density and nothing else. A hard-coded rightward slide is correct until the first
right-to-left locale, at which point it is silently entering from the wrong side.

### A back arrow takes the navigation inset, not the content gutter

An `IconButton` centres a 24dp glyph in a 48dp touch target, so its drawing always sits 12dp inside
its own box. Padded to the screen's content gutter, the arrow therefore lands 12dp further in than
everything it sits above: at a 24dp gutter that is an arrow at 36dp over a title at 24dp, which reads
as a mistake because it is one.

Both headers give the button Material's own navigation-icon padding of 4dp instead, which puts the
glyph 16dp from the edge whatever the gutter under it is doing. The gutter is a rule about where text
starts; the inset is a rule about where a touch target starts, and they were never the same number.

**Rejected: shrinking the touch target so the box could take the gutter.** A 24dp button lines the
arrow up arithmetically and is below every guideline's minimum for something you tap with a thumb.

### The stat table is a grid, so its columns measure themselves

The rows carried two magic widths — 72dp for the name, 32dp for the figure — chosen to fit "Sp. Def"
and three digits. Both were guesses in the direction nobody checks: a name column sized for the
longest label in English, and a figure column that a four-digit total would have clipped.

`Grid` from `androidx.compose.foundation.layout` sizes the two text columns to their own widest
content and hands the lanes what is left as `1.fr`. The widths become facts about the text rather
than estimates of it, and the 24dp gap is declared once instead of being assembled from a row
arrangement plus a padding that had to sum to it.

Its `config` block is not composable and runs during the measure pass, so the gaps are read from the
theme before it rather than inside it.

**It is experimental, and that is the cost.** The opt-in is `@ExperimentalGridApi` and the shape of
the API can change under a Compose upgrade. The exposure is bounded: this project pins CMP 1.11.1 and
#22 records why it is not moving, and the fallback is the `Row` this replaced — about fifteen lines.

**Rejected: `LazyVerticalGrid`.** Seven rows, all on screen at once, inside a column that already
scrolls. Nesting a scroller of the same direction inside one is unmeasurable, which is why the
matchup chips were hand-chunked long before they became a flow.

### Rejected: explaining the matchup chart under its heading

"Damage taken from each attacking type. Neutral matchups are left out." sat under the Type Defenses
heading as a two-line hint. It is true, and it is the kind of sentence a screen accumulates until
nothing on it is read.

The chips say `×4`, `½`, `0` — the first two are legible to anyone who has played, and the third is
obvious. What the hint added was that the *absent* types are the neutral ones, which is a fact about
a list nobody is looking at. #42 is where that belongs if it turns out to be needed, alongside the
other numbers on this screen that are opaque without a legend.

### The tabs are a pager, so the sheet stops scrolling as one piece

The sheet was one `verticalScroll` holding the form switcher, the tab row and the tab's content, with
a `Crossfade` between tabs. A pager cannot live in that. `HorizontalPager` needs a bounded height and
inside a vertical scroll it has none — and given one, two tabs of different heights would resize the
scroll under the reader every time they swiped.

So the sheet is a fixed column. The switcher and the tab row are pinned, the pager takes what is
left, and each tab scrolls inside its own page. That is also the shape the expandable sheet needs, so
it lands here rather than being rebuilt there.

**The two halves are bound one direction each, with a guard.** Tapping a tab animates the pager, but
only when the pager is not already on that page. Swiping reports its page and writes it back as the
same intent a tap sends. Without the guard they fight: a drag past the halfway point changes the
page, which changes the Store, which animates the pager to the page the finger is already on.

Swiping reports `currentPage` rather than `settledPage`, which is what makes the underline cross with
the finger instead of snapping once the animation finishes.

**Rejected: giving the pager a fixed height so the sheet could keep scrolling as one piece.** The
About tab is roughly a third of the Base Stats tab, so any fixed height is wrong for one of them:
either Base Stats scrolls inside a box two hundred dp shorter than it needs, or About sits in a
mostly empty one.

### The dex lists one card per species, forms behind it

Regional forms used to have cards of their own — 57 of them, so the browse list was 1,082 entries for
1,025 species. #5 argued for it and the argument is a fair one: a regional form is a different
Pokemon in play, with a different type, different stats and different matchups.

It reads wrong all the same. Scrolling the dex is walking the National Dex, and a second Vulpix
appearing between #037 and #038 raises a question the list cannot answer — *why this form and not
Mega Charizard, not Gigantamax, not Zen Mode?* The rule underneath was "regional forms are different
enough", which is a judgement the reader has to already share for the list to look consistent rather
than arbitrary.

One card per species needs no such agreement. Every form of every kind is reached from the card of
the species it belongs to, through the same switcher, and a filter is where "show me the regional
forms" belongs when it arrives.

Nothing is lost from the app. `toFormPillsUiModel` already lists every non-cosmetic form, so Alolan
Vulpix is one tap from Vulpix and always was.

**Rejected: dropping them from the dataset.** They are real variants with their own types, stats and
artwork, and the detail screen renders them. Only `listedInDex` changed — the column that decides
grid representation and nothing else — which is why the diff is 57 booleans and no rows.

Entries above this one quote 1,082 where they record a measurement. Those stay as measured; the count
they were taken against is this one.

### The carousel traverses the query, not a copy of its results

Swiping sideways on the hero moves along the list the detail was opened from. When a filter arrives,
picking Fire and opening Charmander has to keep the swipe inside Fire — a carousel that silently
walked the whole dex would be a different list from the one the reader was just looking at.

The destination carries the **query** rather than the list it returns. A `NavKey` holding a thousand
slugs is around twenty kilobytes written into saved state on every navigation and read back on
process death, and it freezes a result set the dataset can move under. The query is small, it *is*
the identity of the result set, and the detail re-runs it.

There is one query and one list today, so `DexQuery` has a single value. That is the point: the
filter becomes another value rather than another field on the destination, and the detail already
asks "which list" instead of assuming.

**Rejected: reading the whole dex in the detail and calling it the same list.** It is the same list,
right up until it is not, and nothing in the code would have been wrong at the moment it broke.

### The carousel is a pager over the browse list

A thousand pages, one per card, rather than a hand-rolled three-position track. The pager is already
the thing that handles a drag, a fling, a settle and the offset in between, and it composes three
pages at a time whatever the count.

**The neighbours are drawn twice.** Each page renders its artwork, and the same artwork flattened to
a single colour on top of it, with the flat copy's alpha set to the page's distance from the centre.
A card therefore arrives by resolving out of the ground colour and leaves by dissolving back into it.
Animating a `ColorFilter` instead would rebuild the filter every frame of the drag; two images and an
alpha is one composition and a redraw.

The silhouette is *darker* than the ground. Lighter was tried first, on the argument that a dark flat
shape on a saturated colour reads as a hole punched in it. On a device it read as washed out instead —
too close to the ground to be a second object at all. Darker gives the cards either side the shadow
of the one in front, which is what they are standing in.

**They are half size, and the size is half the movement.** A card grows into the centre and shrinks
out of it, so arriving is not only a sideways translation.

**A neighbour is also a control.** Tapping one brings it to the centre — the same movement the swipe
makes, and the only one available to a reader who cannot make the gesture. It carries the Pokemon's
name as its click label, which is the only thing that name is for.

**The pages are a fixed width**, centred by content padding computed from the measured width, because
what a neighbour shows has to be a slice of the artwork and not a slice of a page with the artwork
somewhere inside it. At the viewport's width the artwork would sit in the middle of its page and the
neighbours would show empty margin.

**The swipe reports at the halfway point, not on the settle.** The name, the number, the types and
the colour cross with the finger, and the read for the new card starts while it is still moving. The
executor holds the read's `Job` and cancels it, so swiping faster than the database answers leaves
the card you stopped on rather than the last one to finish.

**The sheet empties on the way.** Holding the previous Pokemon's forms, stats and matchups under the
new one's name is a wrong screen rather than a slow one, and the read is a frame or two.

**Rejected: keeping the pager at one page until the list lands, then swapping it in.** The swap would
remount the composable holding the shared element in the middle of the transition from the grid. It
scrolls into place instead, which costs at most one frame on a page nobody has touched yet.

### The artwork flies out of the grid and does not fly back

Tapping a card flies its artwork, its name and its type pills into the hero. Pressing back does not
fly them home: the two screens cross-fade.

Not an oversight, and not a limitation. Once the carousel existed, the return could only be
consistent by accident — swipe twice and the card you came from is three screens back in a grid that
is not showing it, so there is nothing to fly to. A transition that runs when you have not moved and
does not when you have is worse than one that never runs, because the reader has to learn which case
they are in.

The mechanism is that the grid's `heroSlug` is `remember` rather than `rememberSaveable`. The host
disposes the grid's composition while the detail is open, so the flag is gone by the time the reader
comes back and no card registers a key to match against. The forward transition is unaffected — the
grid is still composed while it is being animated out.

**Rejected: clearing the flag on the way back instead.** It needs the screen to know it is being
returned to, which Navigation 3 does not hand it, and the answer would have been a guess dressed as a
lifecycle.

### The carousel reads ahead, so a swipe lands on content

Emptying the sheet on a swipe was the wrong half of a real problem. Holding the previous Pokemon's
stats under the new one's name is a wrong screen; replacing them with a skeleton for the length of a
database read is a blink, and a blink reads as a fault. The read is one frame, which is exactly the
duration at which a change of state looks like a glitch rather than like loading.

So neither. The Store holds the card on screen and the two either side of it, and reads the
neighbours as soon as the middle one lands. A swipe onto a card that was read ahead shows it on the
same frame, with no skeleton in between and nothing stale in the meantime.

**Bounded by the window, not by a count.** The cache is filtered to the three slugs around the active
card every time it changes, so swiping the length of the dex holds three records whatever route it
took. An eviction policy would have needed a size, and a size would have been a guess.

**A read answers for a named card.** `DetailLoaded` carries the entry slug it was asked for, because
a neighbour's read can land while the reader has moved on, and a message that only carried a
`PokemonDetail` would overwrite what they are looking at with what they are not.

**Read-ahead failures are swallowed.** A card nobody has asked for cannot produce an error message,
and the read runs again if they swipe onto it.

**Retry goes past the cache.** It is only reachable from the error state, where nothing is cached, so
the flag changes nothing today — but a button that says "try again" and quietly does not is worse
than no button.

### The sheet expands and the hero becomes a toolbar

Dragging the sheet up is what makes a long tab readable on a phone. What it takes from the screen is
the hero, so the hero has to leave in a way that keeps the reader oriented: the artwork, the number,
the types and the genus fade out, and the name travels into the middle of the back arrow's row and
shrinks to a title with a chevron either side of it. What is left is a toolbar, which is what a
screen with no hero needs anyway.

**One number drives all of it.** How far the sheet has been dragged positions the sheet, fades the
hero, and moves the name. There is no second animation to keep in step, and a drag released halfway
leaves every part of the screen halfway.

**The hero and the sheet are siblings in a box, not a column.** The sheet slides up *over* the hero,
and a column cannot place a child above its predecessor.

**The hero measures itself.** Where the sheet rests is the bottom of the hero less the artwork's
overlap, and the hero is a status bar plus however tall a name, a row of pills, a genus and the
artwork turn out to be. Only the first of those is a number anyone could have written down. An
earlier version added the status bar to a height that already included it, and the sheet sat a status
bar too low — visible only as "that looks slightly wrong", which is the failure mode of a layout
assembled from constants.

**The whole sheet scrolls it open.** A handle alone is discoverable only by people looking for one.
A nested-scroll connection spends an upward drag on expanding until the sheet is up, and a downward
drag on collapsing — but only what the content underneath did not take, which is what keeps a
scrolled tab scrolling rather than pulling the sheet down with it. The handle stays as the affordance
that says the sheet moves at all, and as the one target that works when the content has nothing to
scroll.

This was first built as handle-only, on the argument that a horizontal pager full of vertical
scrollers gives a nested-scroll connection three gestures to arbitrate between. That is true and it
is not a reason: the arbitration is one `if` on the sign of the drag, and a gesture nobody discovers
is worse than one that occasionally guesses.

**The hero draws after the sheet**, so the Pokemon stands on it rather than behind it. In a `Box`,
draw order and hit-test order are the same order reversed — the last child is drawn on top *and*
asked about a pointer first — so putting the hero on top also put it in front of the sheet's
gestures.

**Which is why the faded hero is not merely faded.** An alpha of zero draws nothing and still answers
a pointer, and the carousel at zero lies exactly over the expanded sheet's tabs. A horizontal pager
between a finger and a horizontal pager is a gesture that arrives about one time in four, and a
vertical drag over it has to survive the pager deciding the drag is not its axis first. Both are
"nothing happened" to the reader.

It is measured but not placed, which is `View.INVISIBLE` and which Compose has no single modifier
for. Not placing it rather than not composing it: the hero's height is what decides where the sheet
rests, so a carousel that left the layout would take 200dp of that with it and the sheet would jump
on the way back down.

**Rejected: `userScrollEnabled = false` on the pager.** It stops the pager scrolling. It does not
stop it being the thing the pointer reaches, which was the actual problem.

**The hero goes in the first quarter of the drag.** The sheet is what the reader is moving, so
everything it is taking the place of should be gone by the time they have decided to move it — a
sheet sliding up behind something still solid reads as two things happening rather than one. The
chevrons arrive in the last two fifths, so nothing is half-faded on top of something else.

**The name lands in the middle of the toolbar, with a chevron either side.** One Text that moves
rather than two that cross-fade, which means its width has to be measured before its destination can
be computed: a title is centred against its own measurement. The chevrons flank a gap the size of the
scaled name, so it arrives between them rather than beside them, and they do what the carousel's
swipe does for a reader who would rather press a button.
