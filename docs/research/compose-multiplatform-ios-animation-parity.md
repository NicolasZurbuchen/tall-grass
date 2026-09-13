# Compose Multiplatform animation parity on iOS

Research for GitHub issue [#3](https://github.com/NicolasZurbuchen/pokedex/issues/3) — "Compose Multiplatform animation parity on iOS".

**Researched:** 2026-09-12
**Applies to:** Compose Multiplatform **1.12.0** (current stable, released 25 Aug 2026) and **1.13.0-alpha01** (current pre-release, 10 Sep 2026). Previous stable line: 1.11.1 (2 Jun 2026).
**Sources:** JetBrains CMP changelog + GitHub releases, JetBrains YouTrack (`CMP` project — the real CMP issue tracker; the GitHub issue tracker is largely archived), compose-multiplatform-core PR diffs, Kotlin Multiplatform docs on kotlinlang.org, androidx release notes, Maven Central.

---

## TL;DR for the animation catalogue

**Nothing in the core Compose animation toolbox is Android-only.** `androidx.compose.animation` ships as a real Kotlin/Native iOS artifact
(`org.jetbrains.compose.animation:animation-iosarm64:1.12.0` resolves on Maven Central — verified 2026-09-12), and CMP 1.12.0 is built on
Jetpack Compose Runtime/UI/Foundation/Material **1.12.0** ([CHANGELOG, 1.12.0 components table](https://github.com/JetBrains/compose-multiplatform/blob/master/CHANGELOG.md)).
So the API surface is identical in `commonMain`.

The risk is **not** "does it compile for iOS" — it's **"does it look right on iOS"**. The real constraints are:

| Constraint | Severity for this project |
|---|---|
| Reduce Motion on iOS **kills** animations dead (scale 0) rather than cross-fading them | High — affects every animation you build |
| Shared-element + Navigation 3 has an **open crash regression in 1.12.0** on iOS | High if you build shared-element hero transitions |
| Custom `NavHost` enter/exit transitions have a **pop-animation bug on iOS** fixed only in 1.13.0-alpha01 | Medium |
| `Modifier.animateItem()` fights the iOS Cupertino overscroll effect (open) | Medium |
| Gesture-driven sheets: moving-node drags can produce zero/reversed fling velocity on iOS (open) | Medium |
| UIKit interop views lag frames behind Compose during animation on real devices | Low unless you embed native views |

**Recommendation:** design the catalogue freely, but (a) pin to a version deliberately, (b) treat Reduce Motion as a first-class design case, and (c) prototype the shared-element hero transition on a real iOS device early, since that is the single technique with an open Major-priority iOS crash.

---

## 1. Shared-element / shared-bounds transitions (`SharedTransitionLayout`)

### Supported on iOS: yes. Stable: yes (since CMP 1.10.0).

- Shared Element Transitions were added to CMP in **1.7.0 (October 2024)**, listed under Highlights → Navigation ([CHANGELOG § 1.7.0](https://github.com/JetBrains/compose-multiplatform/blob/master/CHANGELOG.md)). They arrived via the upstream Compose merge, in `commonMain`, so iOS got them at the same time as Android.
- They were **experimental** (`@ExperimentalSharedTransitionApi`) at that point.
- **The shared transition APIs became stable in androidx `compose-animation` 1.10.0-alpha05 (8 Oct 2025)**: "Shared transition APIs are now stable." ([compose-animation release notes](https://developer.android.com/jetpack/androidx/releases/compose-animation)). They shipped in stable Compose Animation 1.10.0 (3 Dec 2025).
- CMP 1.10.0 (January 2026) is built on Compose 1.10.0 (verified in the CMP CHANGELOG 1.10.0 components table), so **from CMP 1.10.0 onward `SharedTransitionLayout` / `sharedElement` / `sharedBounds` are stable, non-opt-in APIs on iOS too.**
- Useful stable-era additions in Compose Animation 1.10.0: `Modifier.skipToLookaheadPosition`, dynamic enable/disable of shared elements, an API for **initial velocity** so a shared-element transition can be started from a fling, and enter/exit transitions for veil layers. 1.11.0 (22 Apr 2026) added `LookaheadAnimationVisualDebugging` for debugging animated bounds. Source: androidx compose-animation release notes.

### Known bugs — one is serious and current

| Issue | State | What |
|---|---|---|
| [CMP-10722](https://youtrack.jetbrains.com/issue/CMP-10722) | **OPEN**, Major, affects **1.12.0**, updated 2026-09-10 | `[iOS] NullPointerException in LookaheadPassDelegate.remeasure during NavDisplay transition inside SharedTransitionLayout`. Reporter states it is a **regression in CMP 1.12.0**: the same setup ran in production on 1.10.x–1.11.1 for ~3 months with zero crashes of this signature; it appeared within a week of the 1.12.0 upgrade. Production-only, ~3% crash rate, seen on iPads, not locally reproducible. |
| [CMP-8843](https://youtrack.jetbrains.com/issue/CMP-8843) | OPEN, affects 1.8.2 & 1.10.0 | `LookaheadScope` + `Modifier.animateBounds` behaves wrongly during iOS overscroll in a `LazyVerticalGrid`; Android is fine. |
| [CMP-7096](https://youtrack.jetbrains.com/issue/CMP-7096) | Resolved 2025-03-20 | "Shadow behind image when return back by transition in iOS" — historical, fixed. |
| [CMP-5979](https://youtrack.jetbrains.com/issue/CMP-5979) | Resolved 2024-08-26 | Nested navigation + `SharedTransitionLayout` back animation not functional — historical, fixed. |

**Practical read:** shared-bounds Pokédex-card→detail hero transitions are a legitimate cross-platform technique and are *API*-stable. But if you use them **inside a Navigation 3 `NavDisplay`**, CMP-10722 is an unresolved crash on 1.12.0 with no fix version assigned. Two mitigations: use Navigation 2 (`NavHost`) as the host for the shared transition, or stay on 1.11.1 until CMP-10722 is triaged.

---

## 2. Predictive back — Android availability and the iOS story

### Android

- Predictive back is a platform feature from Android 13 (developer option), with system animations appearing automatically from **Android 15+** for opted-in apps. Opt-out is `android:enableOnBackInvokedCallback="false"` at application or activity level. Source: [Add support for the predictive back gesture](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture).
- Compose exposes `PredictiveBackHandler(enabled) { progress: Flow<BackEventCompat> -> ... }` for progress-driven custom transitions, and `BackHandler` for plain interception.

### iOS — there is a real equivalent, and it is wired into Compose navigation

- **CMP 1.8.0 (May 2025) implemented multiplatform `BackHandler` *and* `PredictiveBackHandler`** and used them in Material 3 widgets and the androidx-navigation library ([compose-multiplatform-core PR #1771](https://github.com/JetBrains/compose-multiplatform-core/pull/1771), CHANGELOG § 1.8.0). This is the key fact: **`PredictiveBackHandler` is not Android-only.** On iOS the progress flow is driven by the UIKit edge-swipe gesture (`UIKitBackGestureDispatcher` in `compose/ui/ui/src/uikitMain/.../scene/`).
- CMP 1.8.0 also made the default `androidx.navigation` transition animation on iOS "as close as possible to the iOS back gesture" ([PR #1861](https://github.com/JetBrains/compose-multiplatform-core/pull/1861)).
- The docs state it plainly: *"By default, on iOS the back gesture triggers native-like animation of the swipe transition to another screen."* and *"The multiplatform Navigation library translates back gestures on each platform into navigating to the previous screen (for example, on iOS this is a simple back swipe, and on desktop, the Esc key)."* ([Navigation and routing](https://kotlinlang.org/docs/multiplatform/compose-navigation.html)).
- It can be turned off per-view-controller with `enableBackGesture = false` in the `ComposeUIViewController` configuration (same doc; the flag landed via [PR #1951](https://github.com/JetBrains/compose-multiplatform-core/pull/1951) in 1.8.0-beta02).
- [CMP-9920](https://youtrack.jetbrains.com/issue/CMP-9920) "Correctly implement `DefaultNavTransitions` predictive pop transitions for iOS" was **fixed 2026-06-15** — before that iOS delegated to the generic `StandardDefaultNavTransitions`. So iOS-correct predictive *pop* transitions are a recent (≤ CMP 1.12.0) improvement.

### Swipe-back has had a long tail of gesture-conflict bugs — most now fixed

CMP 1.12.0 alone fixed four:
- content jump at the start of a swipe-back ([#3101](https://github.com/JetBrains/compose-multiplatform-core/pull/3101))
- **conflict with horizontally scrollable components like `HorizontalPager`** ([#3116](https://github.com/JetBrains/compose-multiplatform-core/pull/3116))
- back gesture briefly dispatching drag input into Compose content, causing navigation drawers to pop open during back navigation ([#3192](https://github.com/JetBrains/compose-multiplatform-core/pull/3192))
- RTL swipe-back behaviour ([#3196](https://github.com/JetBrains/compose-multiplatform-core/pull/3196))

1.13.0-alpha01 adds: back-swipe progress could exceed its maximum when the gesture left the window bounds ([#3337](https://github.com/JetBrains/compose-multiplatform-core/pull/3337)).

**Practical read:** a horizontally-paging Pokémon detail screen (swipe between Pokémon) next to the iOS edge-swipe-back is exactly the conflict that was only fixed in **1.12.0**. Do not build that on ≤1.11.1.

---

## 3. `AnimatedContent`, `AnimatedVisibility`, `Modifier.animateItem` — parity status

All three live in `commonMain` (`androidx.compose.animation` / `androidx.compose.foundation.lazy`) and ship for iOS. `Modifier.animateItem()` on iOS was broken once ([CMP-6558](https://youtrack.jetbrains.com/issue/CMP-6558)) and fixed back in 2024. **API parity: complete. Behavioural parity: good, with specific documented divergences.**

Open divergences worth knowing before designing:

| Issue | State | Divergence |
|---|---|---|
| [CMP-10595](https://youtrack.jetbrains.com/issue/CMP-10595) | **OPEN**, Major, affects 1.12.0 | `Modifier.animateItem()` interferes with overscroll: at the list boundary the item placement animation keeps moving content instead of handing over to the overscroll effect; only then does the stretch start. Reporter marks it as affecting **both Android and iOS**. Without `animateItem()` the behaviour is correct. |
| [CMP-7666](https://youtrack.jetbrains.com/issue/CMP-7666) | OPEN since 2025-02, still updated 2026-08-27 | `AnimatedVisibility` does not behave the same on iOS as on Android/JVM — specifically, content inside a `Dialog` appears instantly with no animation on iOS. |
| [CMP-10240](https://youtrack.jetbrains.com/issue/CMP-10240) | OPEN | `[iOS] AnimatedVisibility` enter transition is instant for content inside a `Popup`. |
| [CMP-10437](https://youtrack.jetbrains.com/issue/CMP-10437) | OPEN, affects 1.11.1 & 1.12.0 | On **physical iOS devices only** (simulator unaffected), UIKit interop views wrapped in `AnimatedVisibility` inside lazy-list items lag many frames behind the Compose layout — inserts/removals/frame updates are queued into `UIKitInteropMutableTransaction` and only flushed when the next frame is presented, so a native control can be drawn outside the bounds of the row that already collapsed. |
| [CMP-9311](https://youtrack.jetbrains.com/issue/CMP-9311) | OPEN | `AnimatedVisibility` for a FAB using size-animations misbehaves. |

**Pattern:** the iOS divergences cluster around **`AnimatedVisibility` inside a separate scene layer** (Dialog / Popup) and around **UIKit interop**. Plain `AnimatedVisibility` / `AnimatedContent` in the main scene is fine. CMP 1.11.0 separately added enter/exit animation for `Dialog` itself, controllable via `DialogProperties.animateTransition` or `ComposeUiFlags.isDialogAnimationEnabled` ([PR #2596](https://github.com/JetBrains/compose-multiplatform-core/pull/2596)) — use that rather than hand-rolling `AnimatedVisibility` inside a dialog.

---

## 4. Gesture-driven, interruptible animation vs. native iOS scroll physics

### The good news: iOS gets real Cupertino physics, by default, in common code

- CMP **1.8.0** enabled **Cupertino overscroll by default** for scrollable components ([PR #1753](https://github.com/JetBrains/compose-multiplatform-core/pull/1753)) and removed the `optOutOfCupertinoOverscroll()` escape hatch. The experimental `CupertinoScrollDecayAnimationSpec` / `CupertinoOverscrollEffect` classes were removed from the public API ([PR #1806](https://github.com/JetBrains/compose-multiplatform-core/pull/1806)); overscroll effects are instead configured from `commonMain` ([CMP-3691](https://youtrack.jetbrains.com/issue/CMP-3691), resolved).
- So `rememberModalBottomSheetState` / `AnchoredDraggable` / `Modifier.draggable` + `Animatable` all run against iOS-flavoured decay and overscroll without platform-specific code.

### Where it still bites

| Issue | State | What |
|---|---|---|
| [CMP-10758](https://youtrack.jetbrains.com/issue/CMP-10758) | **OPEN**, affects 1.12.0, created 2026-09-05 | **iOS: translating a scrollable gesture node can produce zero or reversed fling velocity.** When a vertical `Modifier.scrollable` node *itself moves* during a drag, an upward swipe can yield zero or a *downward* fling velocity. Keeping the node stationary and moving only its content gives the expected fling. This is precisely the drag-to-expand-sheet shape (the draggable container moves with the finger). |
| [CMP-9100](https://youtrack.jetbrains.com/issue/CMP-9100) | OPEN, affects 1.9.x | Cupertino overscroll effect **consumes nested-scroll offset** on iOS when the first item is at the top, so a `NestedScrollConnection` can't react — e.g. a collapsing header driven by nested scroll stops responding. Works correctly on Android. Slow scrolling behaves correctly, fast does not. |
| [CMP-10595](https://youtrack.jetbrains.com/issue/CMP-10595) | OPEN | `animateItem()` delays hand-over to overscroll (see §3). |
| [CMP-8843](https://youtrack.jetbrains.com/issue/CMP-8843) | OPEN | `animateBounds` misbehaves during iOS overscroll. |

Recently fixed and worth having: scrolling inertia on short scroll gestures ([#2851](https://github.com/JetBrains/compose-multiplatform-core/pull/2851), 1.11.0), scrolling inside a modal view controller ([#2883](https://github.com/JetBrains/compose-multiplatform-core/pull/2883), 1.11.0), trackpad overscroll/fling ([CMP-7720](https://youtrack.jetbrains.com/issue/CMP-7720), resolved 2026-09-11).

### Against *native* iOS scroll views (interop)

If a Compose scrollable sits next to or inside a native `UIScrollView`, CMP arbitrates with a `UIScrollView`-inspired **150 ms delay**: if Compose consumes events within the delay the native view never sees the touch; otherwise native takes over for the rest of the sequence. From CMP **1.12.0** this is configurable via `UIKitInteropProperties.interactionMode` — `Cooperative` (default, with configurable delay), `NonCooperative`, or `null` to disable interaction entirely. Source: [Touch handling on iOS](https://kotlinlang.org/docs/multiplatform/compose-ios-touch.html).

**Practical read:** a drag-to-expand bottom sheet is very buildable, and gesture interruption works (`Animatable.animateTo` + `stop()`, `AnchoredDraggable`). But **verify fling direction on a real device** because of CMP-10758, and prefer moving *content* rather than the scrollable node itself where the layout allows it.

---

## 5. Known iOS performance traps for animation-heavy screens

**Rendering baseline is good now.** Concurrent/parallel rendering (offloading render command encoding to a dedicated thread) was introduced opt-in in CMP 1.8.0 and is **enabled by default since CMP 1.11.0** ([PR #2732](https://github.com/JetBrains/compose-multiplatform-core/pull/2732); [What's new in CMP 1.11.1](https://kotlinlang.org/docs/multiplatform/whats-new-compose-111.html)). The render queue runs at highest priority ([PR #2623](https://github.com/JetBrains/compose-multiplatform-core/pull/2623)).

**Trap 1 — interop cancels the parallel-rendering win.** The 1.8.0 docs state the feature "may improve performance in scenarios **without UIKit interop**" ([What's new in CMP 1.8.2](https://kotlinlang.org/docs/multiplatform/whats-new-compose-180.html)). Combined with CMP-10437 (interop views lagging frames behind animated Compose layout on device), **native views inside animating lists are the single worst pattern.** For a Pokédex: don't embed native map/video/webviews inside animating cards.

**Trap 2 — `InfiniteTransition` burns CPU on iOS.** [CMP-8146](https://youtrack.jetbrains.com/issue/CMP-8146) (OPEN, affects 1.7.3/1.8.0, still updated 2026-08-28): high CPU usage with `InfiniteTransition`; reporter measured ~40% CPU with a minimal reproducer. Relevant to always-on idle animations (shimmer placeholders, pulsing type badges, breathing Pokéball loaders).

**Trap 3 — `LazyColumn` with heterogeneous item types.** [CMP-7841](https://youtrack.jetbrains.com/issue/CMP-7841) (OPEN, Major, updated 2026-09-08): significant frame drops on iOS with `LazyColumn` when **different view types** are present (especially horizontal lists nested in a vertical list), mostly in the first few scrolls, then it smooths out. Not reproduced with a homogeneous list. Directly relevant to a Pokédex home screen mixing a horizontal "featured" rail into a vertical grid.

**Trap 4 — 120 Hz displays.** [CMP-9465](https://youtrack.jetbrains.com/issue/CMP-9465) reported `LazyColumn` stutter specifically at 120 Hz on an iPhone 14 Pro with complex items (closed as Incomplete 2026-04-28, but a recurring theme — see also the historical [CMP-4594](https://youtrack.jetbrains.com/issue/CMP-4594)). **Always profile on a ProMotion device**, not the simulator.

**Trap 5 — simulator lies.** CMP-10437 is explicitly "simulator unaffected". CMP-8152 reports an FPS drop dependent on the home-indicator bar and background contrast that vanishes while screen-recording. Device testing is non-optional for this project's goal.

**Trap 6 — transition end-of-animation glitches, current.** [CMP-10750](https://youtrack.jetbrains.com/issue/CMP-10750) (OPEN, Major, created 2026-09-03): since **1.12.0-alpha01**, iOS shows a visible **one-frame flicker at the exact moment a `NavHost` pop transition finishes** — a brief dim/blank frame across the whole Compose surface, on every pop. Bisected to a `SeekableTransitionState` end-of-animation reorder. Android with the same Compose version is unaffected. 1.11.1 does not show it.

Also fixed-in-1.12.0 and worth noting as evidence of the class of problem: "Fix incorrect frames order during high load rendering" ([PR #3122](https://github.com/JetBrains/compose-multiplatform-core/pull/3122)).

---

## 6. Compose Navigation in multiplatform, and custom enter/exit transitions

### Two supported stacks, both multiplatform, both still pre-stable

CMP 1.12.0 ships:

| Library | CMP coordinate (1.12.0) | Based on Jetpack |
|---|---|---|
| Navigation 2 | `org.jetbrains.androidx.navigation:navigation-*:2.10.0-alpha02` | Navigation 2.10.0-alpha05 |
| Navigation 3 | `org.jetbrains.androidx.navigation3:navigation3-*:1.2.0-alpha02` | Navigation3 1.2.0-alpha04 |
| Navigation Event | `org.jetbrains.androidx.navigationevent:navigationevent-compose:1.1.0` | Navigation Event 1.1.1 |

In 1.13.0-alpha01 both move to **beta**: `navigation-*:2.10.0-beta01` and `navigation3-*:1.2.0-beta01`.
(Source: CHANGELOG components tables for 1.12.0 and 1.13.0-alpha01.)

- **Navigation 3 works on all CMP targets — Android, iOS, desktop, web — starting with CMP 1.10** ([Navigation 3](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html), and the [CMP 1.10.0 announcement](https://blog.jetbrains.com/kotlin/2026/01/compose-multiplatform-1-10-0/)).
- iOS caveat for Nav3: Android's reflection-based serialization for `NavKey` isn't available on non-JVM targets, so you must supply **polymorphic serialization for destination keys** via `SavedStateConfiguration` + a `SerializersModule` (same doc; cf. [CMP-9077](https://youtrack.jetbrains.com/issue/CMP-9077)).

### Custom enter/exit transitions: supported, with an iOS caveat

- Fully supported on `NavHost` via `enterTransition` / `exitTransition`. The docs are explicit that **customizing disables the platform default**: *"If you customize the `NavHost` animation with `enterTransition` or `exitTransition` arguments, the default animation is not going to trigger"* ([Navigation and routing](https://kotlinlang.org/docs/multiplatform/compose-navigation.html)). So on iOS, opting into custom transitions means opting *out* of the native-like swipe-back animation unless you rebuild it.
- `DefaultNavTransitions` was made **public** so you can fall back to the platform defaults conditionally instead of duplicating the whole `NavHost` call — [CMP-8063](https://youtrack.jetbrains.com/issue/CMP-8063), fixed 2026-03-23 (so available from CMP 1.11.0 onward). This is the right tool for "custom transition on some routes, native default on others".
- CMP 1.12.0 "Improved iOS specific default navigation transactions in Nav2" and set Web/Desktop defaults to `None` ([PR #3023](https://github.com/JetBrains/compose-multiplatform-core/pull/3023)).
- Nav3 got `unveilIn` / `veilOut` animations in the default iOS transition specs ([PR #2655](https://github.com/JetBrains/compose-multiplatform-core/pull/2655), CMP 1.11.0).
- **Known bug, fixed only in 1.13.0-alpha01:** *"Fix pop animations on iOS when custom enter/exit animations are set"* ([PR #3292](https://github.com/JetBrains/compose-multiplatform-core/pull/3292)). i.e. on **1.12.0 stable, custom nav transitions have broken pop animations on iOS.** This matters a lot for a project whose goal is custom navigation animation.
- 1.13.0-alpha01 also makes default Nav3 push/pop transitions follow layout direction, matching native `UINavigationController` in RTL ([PR #3346](https://github.com/JetBrains/compose-multiplatform-core/pull/3346)).
- Historical, fixed: back gestures are ignored while a dialog is open on non-Android targets ([PR #2439](https://github.com/JetBrains/compose-multiplatform-core/pull/2439), CMP 1.10.0); swipe-back bypassing a blocked `onBack` ([CMP-9338](https://youtrack.jetbrains.com/issue/CMP-9338), fixed 2026-01-05).

---

## 7. Reduced-motion / accessibility motion settings — readable cross-platform?

**Yes — and it is the same common API on both platforms. But the current iOS behaviour is arguably wrong, and there is an open bug about it.**

### The mechanism (verified against source, not docs)

The seam is `androidx.compose.ui.MotionDurationScale`, which lives in **`commonMain`** of `compose-ui` and is a public `@Stable` interface implementing `CoroutineContext.Element`
([MotionDurationScale.kt](https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/MotionDurationScale.kt)):

> "Provides a duration scale for motion such as animations. When the duration `scaleFactor` is 0, the motion will end in the next frame callback. Otherwise, the duration `scaleFactor` will be used as a multiplier to scale the duration of the motion."

- **Android:** `WindowRecomposer.android.kt` installs a `MotionDurationScaleImpl` into the recomposer's coroutine context, which observes the **system animation duration scale** flow (`Settings.Global` animator duration scale) and recomposes on change. Android's accessibility "Remove animations" toggle sets that scale to 0.
- **iOS:** CMP 1.8.0 added the equivalent — [compose-multiplatform-core PR #1847](https://github.com/JetBrains/compose-multiplatform-core/pull/1847), "[A11y] Support Reduce Motion" ([CMP-7614](https://youtrack.jetbrains.com/issue/CMP-7614), fixed 2025-02-20). The diff in `ComposeHostingViewController.uikit.kt` is unambiguous:

  ```kotlin
  private fun updateMotionSpeed() {
      motionDurationScale.scaleFactor = if (UIAccessibilityIsReduceMotionEnabled()) {
          // 0f would cause motion to finish in the next frame callback.
          0f
      } else {
          1f / (view.window?.layer?.speed?.takeIf { it > 0 } ?: 1f)
      }
  }
  ```

  It reads `UIAccessibilityIsReduceMotionEnabled()` and folds a `MotionDurationScale` into the Compose coroutine context (`composeCoroutineContext`), updating on `viewDidLoad` and whenever the app becomes active again (`ApplicationActiveStateListener`). Note the CMP-7614 note: *"Application restart is required to see the effect"* — in practice, app re-activation.

### Consequences for your catalogue

1. **You get it for free, in common code**, with no `expect`/`actual`: every duration-based Compose animation is scaled by the platform's motion preference automatically.
2. **You can read it yourself in common code** — `currentCoroutineContext()[MotionDurationScale]?.scaleFactor` — to branch design decisions (e.g. swap a slide for a cross-fade) rather than just having durations collapse.
3. **It can be overridden in tests** via the `effectContext` parameter of `runComposeUiTest` / the test rule (stated in the KDoc above). CMP 1.11.0 added `effectContext` support for the v2 `ComposeUiTest` APIs on non-Android targets.
4. **The iOS behaviour is currently a hard cut, and that's a known complaint.** [CMP-10284](https://youtrack.jetbrains.com/issue/CMP-10284) (OPEN, created 2026-06-05, affects 1.11.1): *"[accessibility] iOS: Reduce Motion hard-cuts animations (NavDisplay transitions, ModalBottomSheet) instead of reducing them."* The report notes that native iOS does **not** suppress animation under Reduce Motion — UIKit/SwiftUI substitute a cross-dissolve for slide/zoom transitions. CMP sets scale 0, so `NavDisplay` transitions and `ModalBottomSheet` show/hide snap instantly.

**Recommendation:** since the primary goal is practising animation, build a small `rememberReducedMotion()` helper over `MotionDurationScale` and design a deliberate cross-fade fallback per animation in the catalogue. That turns an accessibility gap into a design exercise, and it is portable.

Other accessibility motion-adjacent facts: CMP 1.13.0-alpha01 adds **iOS Dynamic Type font scaling** based on accessibility settings ([PR #3306](https://github.com/JetBrains/compose-multiplatform-core/pull/3306)); Increase Contrast is readable via `UIAccessibilityDarkerSystemColorsEnabled` ([Support for iOS accessibility features](https://kotlinlang.org/docs/multiplatform/compose-ios-accessibility.html)). Reduce Motion is **not** documented on that accessibility page — the only primary evidence is the changelog entry and the PR diff above.

---

## Version guidance for this project

| If you want… | Pin |
|---|---|
| Fewest current iOS animation bugs overall | **1.11.1** (no NavHost pop flicker, no shared-element/NavDisplay crash) — but you lose the swipe-back vs `HorizontalPager` fix and public `DefaultNavTransitions` is 1.11.0+ so that's fine |
| Swipe-back that coexists with a `HorizontalPager` | **1.12.0** minimum ([#3116](https://github.com/JetBrains/compose-multiplatform-core/pull/3116)) |
| Custom `NavHost` enter/exit transitions with correct pop animations on iOS | **1.13.0-alpha01** ([#3292](https://github.com/JetBrains/compose-multiplatform-core/pull/3292)) — alpha, so accept the risk |
| Shared elements inside Navigation 3 `NavDisplay` | Avoid 1.12.0 until [CMP-10722](https://youtrack.jetbrains.com/issue/CMP-10722) resolves; or host them under Navigation 2 |

Given the project goal, **1.12.0 with Navigation 2 (`NavHost`) is the pragmatic default**, with the caveat that custom pop transitions on iOS need visual verification until 1.13.0 stabilises. Re-check CMP-10722, CMP-10750, CMP-10758 and CMP-10284 before locking the catalogue — all four were updated within the last two weeks of this research.

---

## Open issues to watch

| Issue | Topic |
|---|---|
| [CMP-10722](https://youtrack.jetbrains.com/issue/CMP-10722) | Shared element + NavDisplay crash on iOS, 1.12.0 regression |
| [CMP-10750](https://youtrack.jetbrains.com/issue/CMP-10750) | One-frame flicker at end of NavHost pop on iOS, 1.12.0-alpha01 regression |
| [CMP-10758](https://youtrack.jetbrains.com/issue/CMP-10758) | Zero/reversed fling velocity for moving scrollable nodes on iOS |
| [CMP-10595](https://youtrack.jetbrains.com/issue/CMP-10595) | `animateItem()` vs overscroll hand-over |
| [CMP-10284](https://youtrack.jetbrains.com/issue/CMP-10284) | Reduce Motion hard-cuts instead of cross-dissolving on iOS |
| [CMP-10437](https://youtrack.jetbrains.com/issue/CMP-10437) | UIKit interop views lag frames behind animated Compose layout on device |
| [CMP-8146](https://youtrack.jetbrains.com/issue/CMP-8146) | `InfiniteTransition` high CPU on iOS |
| [CMP-7841](https://youtrack.jetbrains.com/issue/CMP-7841) / [CMP-8152](https://youtrack.jetbrains.com/issue/CMP-8152) | iOS `LazyColumn` frame drops (mixed item types; contrast/home-indicator) |
| [CMP-7666](https://youtrack.jetbrains.com/issue/CMP-7666) / [CMP-10240](https://youtrack.jetbrains.com/issue/CMP-10240) | `AnimatedVisibility` instant inside Dialog / Popup on iOS |
| [CMP-9100](https://youtrack.jetbrains.com/issue/CMP-9100) | Cupertino overscroll consumes nested-scroll offset |
| [CMP-8843](https://youtrack.jetbrains.com/issue/CMP-8843) | `animateBounds` + overscroll on iOS |

---

## Primary sources

- [JetBrains/compose-multiplatform CHANGELOG.md](https://github.com/JetBrains/compose-multiplatform/blob/master/CHANGELOG.md) — per-version iOS/Navigation sections and component version tables
- [JetBrains/compose-multiplatform releases](https://github.com/JetBrains/compose-multiplatform/releases) — 1.12.0 (25 Aug 2026), 1.13.0-alpha01 (10 Sep 2026), 1.11.1 (2 Jun 2026)
- [JetBrains YouTrack, CMP project](https://youtrack.jetbrains.com/issues/CMP) — queried via the public REST API on 2026-09-12
- [compose-multiplatform-core PR #1847](https://github.com/JetBrains/compose-multiplatform-core/pull/1847) — Reduce Motion implementation diff
- [androidx MotionDurationScale.kt](https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/MotionDurationScale.kt) and [WindowRecomposer.android.kt](https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/platform/WindowRecomposer.android.kt)
- [androidx compose-animation release notes](https://developer.android.com/jetpack/androidx/releases/compose-animation) — shared transition stability timeline; latest 1.12.1 (9 Sep 2026)
- [Predictive back gesture (Android)](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture)
- Kotlin Multiplatform docs: [Compatibility and versions](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html), [Navigation and routing](https://kotlinlang.org/docs/multiplatform/compose-navigation.html), [Navigation 3](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html), [Touch handling on iOS](https://kotlinlang.org/docs/multiplatform/compose-ios-touch.html), [iOS accessibility](https://kotlinlang.org/docs/multiplatform/compose-ios-accessibility.html), [What's new 1.8.2](https://kotlinlang.org/docs/multiplatform/whats-new-compose-180.html), [What's new 1.11.1](https://kotlinlang.org/docs/multiplatform/whats-new-compose-111.html)
- JetBrains blog: [CMP 1.10.0](https://blog.jetbrains.com/kotlin/2026/01/compose-multiplatform-1-10-0/), [CMP 1.11.0](https://blog.jetbrains.com/kotlin/2026/05/compose-multiplatform-1-11-0/), [CMP 1.12.0](https://blog.jetbrains.com/kotlin/2026/08/compose-multiplatform-1-12-0/), [CMP 1.8.0 / iOS stable](https://blog.jetbrains.com/kotlin/2025/05/compose-multiplatform-1-8-0-released-compose-multiplatform-for-ios-is-stable-and-production-ready/)
- Maven Central — existence and target verification for `org.jetbrains.compose.animation:animation-iosarm64:1.12.0`, `org.jetbrains.compose.animation:animation:1.13.0-alpha01`, `org.jetbrains.androidx.navigation:navigation-compose:2.10.0-alpha02`
