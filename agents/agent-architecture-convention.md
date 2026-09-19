# Agent Architecture Convention

This file exists so an agent can place a new file in the right package and shape a new screen or feature correctly, without re-deriving the architecture from scratch or asking a human. The worked examples throughout are drawn from the `pokemon-explorer` feature this template shipped with. **That feature has been deleted** — it was an example, not a dependency — so the file paths in the examples no longer resolve. The pattern is what is fixed, and it is unchanged; `feature/home/` is the only feature in the repo today and is deliberately minimal, so it demonstrates the stateless end of the shape and nothing more. For a worked example that still exists, read **Yadlo** — same author, same template, a shipped app roughly a hundred pull requests deep. See CLAUDE.md § Yadlo is the reference implementation for what in it transfers and what does not.

## Package placement — decision procedure

Walk this in order for any new file. **Every question is answerable by reading the file itself** — that is the property that makes the procedure work, and the reason it no longer asks how many features use something. "Is it cross-feature" is a fact about the rest of the system: it can become true without the file changing, and a criterion like that cannot be checked at the moment of placement.

1. Does it **compose the app** — wire the graph, own the navigation host, hold a screen that belongs to no feature? → `app/`. This package is defined by **terminality**: it imports everything and nothing imports it.
2. Does it know about **exactly one** feature? → `feature/<name>/`
3. Does it **model the subject** — a domain type, a repository over it, or the rendering of one? → `core/`
4. Is it **tokens, or a component whose contract is purely presentational**? → `design/`
5. Is it **pure technical plumbing with no domain vocabulary and no brand**? → `infra/`

The generator test for the bottom four: **could this be a separately versioned library?** Design system yes, plumbing yes, domain model yes, feature slice no, shell no by definition. Everything that could be a library sits below the features.

### Is it `design/` or is it `core/<slice>/presentation/`?

> Does the component own a rule about the subject, or only about appearance?

- `AppFilterChip(label, selected, onClick)` decides nothing about meaning. → `design/`
- A timeline bar that decides time lays out proportionally across a day, that past segments dim, that a running one fills from its start — those are rules about the subject. → `core/<slice>/presentation/`

The linguistic form of the same test is the `App` prefix, which parses as *our version of a generic thing*. `AppFilterChip` reads fine; `AppInvoiceTimeline` reads as nonsense, and that is the signal.

Neither test counts callers. A `core/` slice with a `presentation/` package is not a layer violation — the slice simply has both layers, exactly as a feature does, and `core/<slice>/domain/` still may not import `core/<slice>/presentation/`.

## Shape of `app/`, `core/`, `design/` and `infra/`

```
app/                             # composition root. terminal: imports everything, nothing imports it
├── App.kt                       # Root Composable: theme + image loader + NavGraph
├── di/
│   └── AppModule.kt             # Aggregates every feature/infra Koin module into one list — the only DI file allowed to know about more than one feature
└── navigation/
    ├── impl/                    # Concrete *NavigatorImpl classes — the only place allowed to know about more than one feature's destinations at once
    ├── NavConfig.kt             # The serializers module. It has to name every destination class, which is what puts it here
    └── AppNavigationModule.kt

core/                            # the domain, and the rendering of it
└── error/                       # AppError / AppException, single throw-catch mechanism, plus its UiMapper
                                 # a slice grows data/ · domain/ · presentation/ · di/ as it needs them

design/                          # tokens, and components with purely presentational contracts
├── component/                   # App-wide reusable composables (e.g. AppErrorBanner). A component shared only within one feature belongs in feature/<name>/presentation/component/, and one used by a single screen in that screen's own component/
├── preview/                     # TallGrassPreview — the preview harness. Not in component/: it is never drawn in a shipped screen
├── theme/                       # Design tokens, colour palette, spacing, typography, shimmer — see agent-design-system-convention.md
└── uimodel/                     # UiModels the design components take, when they need one

infra/                           # plumbing. no domain, no brand. imports nothing in this project
├── database/                    # SQLDelight driver setup (expect/actual)
├── mvi/                         # MVIKotlin base wiring (StoreFactory binding)
├── navigation/                  # AppNavigator, NavKeyHandler, NavGraph, SharedTransition — feature-agnostic
├── network/                     # Ktor client configuration (expect/actual engine)
├── platform/                    # expect/actual platform utilities (BackHandler, Platform)
├── preview/                     # PreviewThemes, PreviewUiMode — the multipreview annotation and its constants
└── text/                        # UiText — resource/raw/composite text abstraction
```

`infra/` importing nothing of this app is the property the placement rule leans on, and it is enforced: a file goes there when it would be as much at home in another project, which is only checkable if nothing in it names a domain type, a token or a screen.

Files in `androidMain/` and `iosMain/` are named after their platform — `Platform.android.kt`, `Platform.ios.kt`. Three identically named files in one search result is the cost of not doing it, and the rule covers every file in the source set, not only `actual` declarations.

## Layer shape inside a feature

```
feature/<name>/
├── data/
│   ├── datasource/
│   │   ├── local/                    # *LocalDataSource(Impl); a mapper/ subfolder holds only top-level extension functions mapping the local storage type to a domain model
│   │   └── remote/                   # *RemoteDataSource(Impl); an api/ subfolder holds the Ktor *Api(Impl), a dto/ subfolder the wire-format *Dto classes (nested payload classes carry the suffix too), and a mapper/ subfolder the top-level extension functions mapping Dto -> domain model
│   ├── repository/                   # *RepositoryImpl only, implements the domain interface
│   └── di/
├── domain/
│   ├── model/                        # pure values — no Flow/StateFlow, no internal mutability
│   ├── repository/                   # interface only, no default implementations
│   └── usecase/                      # reserved for logic that touches a port (repository, clock) or coordinates more than one step; a UseCase never injects another UseCase
└── presentation/
    ├── navigation/                   # *Destination, *Navigator (interface), *NavKeyHandler — a feature only ever knows its own destinations
    ├── component/                    # composables reused across screens *within this feature only*; take a UiModel, never raw primitives. Cross-feature reuse goes in design/component/ instead
    └── screen/<screen>/
        ├── *Contract.kt              # exactly Intent/Label/Action/Message (sealed interfaces) + State (data class) — plus any *State-suffixed type the Store holds. Never a UiModel
        ├── *StoreFactory.kt          # Bootstrapper + Executor + a nested `internal object ReducerImpl` — never a standalone *Reducer.kt file, never `private` (internal is what makes it directly unit-testable from commonTest). No top-level functions: a converter that grows at the bottom of one belongs in mapper/
        ├── *UiMapper.kt              # a single top-level extension function, State -> UiModel — the only place that whole conversion happens
        ├── *UiModel.kt               # the Composable's actual input type — no domain types as field types
        ├── *ViewModel.kt             # wraps the StoreFactory; exposes `state: StateFlow<*UiModel>` and `labels: Flow<*Label>` — never State
        ├── *Route.kt / *Screen.kt    # the Screen's public function takes only Modifier, the matching *UiModel, or lambdas — never *State
        ├── *ScreenPreview.kt         # one provider class and one preview function — see below
        ├── component/                # composables reused only within this screen; take a UiModel, never raw primitives
        ├── uimodel/                  # the *pieces* of the screen's vocabulary. The screen's own <Screen>UiModel stays beside the Screen; everything else suffixed UiModel goes here
        └── mapper/                   # *UiMapper files converting one domain type to its presentation twin. The only package inside presentation/ allowed to import the domain
```

A `core/` slice is the same tree minus `presentation/screen/` and `presentation/navigation/` — it has no screen and owns no destination. Its `presentation/` holds `component/`, `uimodel/` and `mapper/`, and `konsistTest/CorePresentationTest.kt` states that shape rather than holding it to the feature one.

### Where the domain crossing happens

Inside `presentation/`, exactly four kinds of file may import `domain/`: the StoreFactory (it wires use cases), the ViewModel, the Contract (it is written in domain terms), and anything in a `mapper/` package. The fourth is the only one that exists *to* cross the boundary — concentrating the crossing in files named for it is what stops it happening at the bottom of whichever file needed it first.

## MVI vocabulary

- `Intent` — a user-initiated event from the UI
- `Label` — a one-shot side effect (navigation, etc.) — a screen with genuinely nothing to signal still declares an empty sealed interface, it isn't omitted
- `Action` — a bootstrapper-initiated internal trigger
- `Message` — reducer input, produced by the executor
- `State` — an immutable, screen-logical snapshot the reducer reads and writes — **never leaves the Store/Executor/Reducer/UiMapper boundary**. If a Composable seems to need a State field, that means the UiMapper is missing a field, not a reason to pass State through. This is enforced by Konsist (`PresentationLayerTest.kt`), not just convention.

A Contract may declare a fifth kind of type, suffixed `State`: something the Store holds that is neither domain nor rendered — which of five filter chips is selected is a real thing to remember, and the domain has no opinion about it. That is not the same as a UiModel, which is what a Composable is handed and may not appear in a Contract at all.

## Previews

Every screen folder holds a `*ScreenPreview.kt`, and it has a fixed shape: **one provider class named `<Screen>StateProvider`, one private preview function, and nothing else at the top level.** The fixtures live inside the provider, where they are visibly in service of the sequence they feed; hoisted to the top of the file they read as something the screen depends on.

The function is annotated `@PreviewThemes` — the multipreview that renders light and dark from one body — and renders inside `TallGrassPreview`, which provides the theme and paints the background. Both matter: Compose's preview pane paints its own white whatever the theme says, so a screen that does not fill its background renders dark-theme text on a white sheet and looks fine at a glance.

Cover the states the screen can actually reach, in the order it reaches them. The ones that go wrong are never the state the device happens to open on — a failed refresh over content that was kept, an empty list, a record that resolved to nothing.

## Loading states

**A screen waits as its own silhouette, not behind a spinner.** `ShimmerPulse` and `Modifier.shimmerBlock` in `design/theme/Shimmer.kt` own the pulse and the block; the screen owns the geometry, because the geometry is the point. A `CircularProgressIndicator` imported into a `*Screen.kt` fails a Konsist rule.

A skeleton is a second rendering of the same layout, so it goes in a file of its own rather than as a private function at the bottom of the screen — the two drift apart the moment one is easier to reach than the other.

## UiModel stability

**Every `UiModel` class is annotated `@Immutable`.** Enums are exempt — Compose already treats them as stable — and a Konsist rule covers the rest.

Compose decides whether it may *skip* a Composable by looking at its parameter types. A `List` field makes the class holding it unstable, because `List` is an interface and the compiler cannot know the instance behind it is not an `ArrayList` somebody mutates. An unstable parameter means the Composable taking it recomposes every single time its caller does, and the leak spreads upward: one unstable field makes its holder unstable too. `UiText` is the cautionary example — it carries `List<Any>` and `List<UiText>`, so before it was annotated *every* Composable taking a `UiText` anywhere in the app was unskippable.

The annotation is a promise, not a check. It is true here because these types are built by a mapper and never touched again. It is required on every UiModel rather than only the ones that need it today so the rule stays mechanical: adding a list to a model that did not have one must not quietly cost a screen its skipping.

**A UiMapper that is expensive may take its expensive part as a parameter.** `DexUiMapper` maps 1,082 cards; the prefetch produces twenty-five states that change everything except them. So `DexState.toUiModel` takes the cards with a default that maps them, and `DexViewModel` passes the ones it already has. The per-item mapper moves to `mapper/` and gets its own test, which is where it belonged anyway.


## Dependency injection (Koin)

- `factoryOf` — UseCases, StoreFactories
- `viewModelOf` — ViewModels
- `singleOf` — Repositories, DataSources, **only when every constructor parameter should be resolved from the DI graph**

`singleOf(::Impl)` resolves every constructor parameter via reflection, including ones with Kotlin default values — it does not skip them. A class with a defaulted parameter that must *not* come from the DI graph (e.g. an injected clock lambda kept as a default for deterministic testing) needs an explicit binding instead: `single<Interface> { Impl(get(), get()) }`. Omitting the argument lets Kotlin's own default apply, since Koin never touches a parameter it wasn't asked to resolve.

**Group a feature's module by screen, not by declaration type.** Each screen's StoreFactory sits next to its ViewModel under a comment naming the screen. Listing every `factoryOf` together and then every `viewModelOf` together means adding a screen edits two places and reading one scans two lists. Not enforceable by Konsist — it is the order of DSL calls inside a lambda, which the API does not expose — so it is convention, and this is where it is written down.

## Error handling

- `AppError` (a single sealed interface) plus `AppException` is the only throw/catch mechanism in the app — no ad-hoc exception types.
- Display resolution happens once, at a shared `AppError -> AppErrorUiModel` mapping (`core/error/AppErrorUiMapper.kt`), using `UiText` (`infra/text/UiText.kt`) for anything that needs runtime data rather than only a static resource. The banner that draws it lives in `design/component/` and takes a `UiText` — the mapper names the domain, the component does not.
- Every user-facing string is a Compose Multiplatform resource (`shared/src/commonMain/composeResources/values/strings.xml`) — never a hardcoded literal in a Composable. Naming: `feature_screen_role`, or `common_role` for a cross-feature string. Add an `element` segment only when `role` alone would be ambiguous within that screen. Group entries by feature/concern with a blank line between groups, not alphabetically.

## Testing

- No mocking library. A seam is either a hand-written fake — file-local `private class` for a one-off data-source/API test, or a single shared `Fake<Feature>Repository` in `domain/fake/` when it's reused across several test files for the same feature — or an injected lambda (the clock pattern above).
- `runTest` wraps every suspend-based test, unconditionally, even ones with no real async work.
- Turbine is for `Label` flows only (one-shot events). `StateFlow` state is read synchronously after `testDispatcher.scheduler.runCurrent()` / `advanceTimeBy(...)`.
- Every `Mapper`, `RepositoryImpl`, `DataSourceImpl`, `UseCase`, `UiMapper`, and `StoreFactory` (as a matching `ReducerTest` + `ExecutorTest` pair) needs a corresponding test file — enforced by `konsistTest/TestingTest.kt`, not just this document. So does any file in a `uimodel/` package that declares a top-level function: that is where rules about the subject quietly end up, and a suffix-driven coverage list is exactly what misses them. Add to that file's coverage list when a new category of production file gets established.
- Test names are `subject_condition_expectedBehaviour` and are written to survive being read on their own, out of the file — see `agent-documentation-convention.md`.

## Where prose goes

`agents/agent-documentation-convention.md` routes it: KDoc is a contract, a `//` explains a surprise in the *code*, and why the app is like this goes in `DECISIONS.md`. The same explanation appearing in two files always means something is wrong, and that document says how to tell which.

## Konsist rules — do not modify without asking

`konsistTest/` is the enforcement mechanism for every rule above. Do not loosen, delete, or work around a Konsist rule to make a change compile or a build go green. If a rule is genuinely too strict for something you're legitimately trying to do, stop and tell the developer why, and let them decide whether the rule should change — don't decide that yourself. Silently editing the rule to fit the code defeats the entire point of having it.

Two corollaries learned the hard way:

**A new rule is not trusted until it has been seen to fail.** Point it at a real violation, watch it go red, then revert. A rule that cannot fail is worse than no rule, because it reads as coverage — the suite here has shipped a tautology, two exclusions naming packages that never existed, and four allow-list entries for packages nobody had written.

**An exclusion that matches nothing is an invitation.** Delete it rather than leaving it as a documented exception to a rule it never applied to.
