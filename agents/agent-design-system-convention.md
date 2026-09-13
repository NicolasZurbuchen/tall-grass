# Agent Design System Conventions

This file teaches syntax and wiring, not correct quantities — how many palettes, accents, font families, or domain layers a project has is decided by the real design you hand the agent, not by anything fixed here. The concrete pattern for each piece lives in the actual file under `design/theme/`; this doc explains why it's shaped that way.

`design/` is a top-level package, a peer of `app/` rather than a folder inside it. It is the most depended-on code in the project and `app/` is meant to be terminal, so the two cannot share a name without making the dependency graph circular — see `DECISIONS.md` § *`design/` is a peer of `app/`*. The corollary that matters when adding a component: **nothing in `design/` may import the domain.** A component that needs a domain type owns a rule about the subject and belongs beside that subject, in `core/<slice>/presentation/`.

## Layer model

```
Material ColorScheme  →  AppColors  →  <Domain>Colors (zero, one, or more)
```

**Your own composables always read `AppColors` (or a domain layer), never `colorScheme` directly.** Material's own vocabulary is too limited and too cryptic for anything beyond its native components — there's no clean "border" or "separator" concept in Material3, so using it directly means misusing `surfaceVariant` or an invented `backgroundHigh` for things Material never modeled. `colorScheme` exists purely because stock Material components (`Button`, `TextField`, `Card`) read it internally — it's the default that keeps those components looking right, not a source your own code should touch. Map `AppColors` values onto `colorScheme` slots in `Theme.kt`; from there, forget `colorScheme` exists in the rest of the codebase.

## Which layer does a color go in?

- **Only** `Theme.kt`'s `colorScheme` setup touches Material's slots directly — because stock components need it.
- A bespoke composable with the app's own naming (`surfaceRaised`, `textTertiary`, `accent`) goes in `AppColors`.
- A color tied to domain-specific state a generic app screen would never need (correct/wrong, difficulty tiers, a status pipeline) goes in a `<Domain>Colors` layer, same shape as `AppColors`, named for the domain (`GameColors`, not `<Project>GameColors`).
- If a domain layer already owns a color matching a Material status slot's purpose (`colorScheme.error` and a domain "wrong" color, say), wire `colorScheme.error` from the domain layer's value rather than inventing an independent one — same "colorScheme is a mapped default" logic above.

## Palettes (`theme/Palette.kt`)

A palette name always describes the color itself, never the role it plays. `CrimsonPalette`, `SkyBluePalette` — correct. `AccentPalette`, `HardPalette` — wrong; that bakes an app concept into a color definition. Role assignment happens one layer up, in `AppColors` or a domain layer, where a field like `accent` or `difficultyHard` picks which palette fills that role for a given theme — which is also what makes a theme-swap possible (`AppColors.accent` pointing at `CrimsonPalette` in dark mode, `SkyBluePalette` in light mode).

11-step ramp, `50` through `950`, lightest to darkest, one object per named color family in the source design — five palettes in, five objects out, no default count.

The objects currently in `Palette.kt` are placeholder examples: delete, rename, or replace them entirely to match the real design. Only the ramp shape and the color-only naming rule above are fixed — not which hues exist or how many.

## `AppColors` (`theme/AppColor.kt`)

Every accent-shaped role is a four-field quad (`accent`/`onAccent`/`accentSubtle`/`onAccentSubtle`); add another quad under a new name (`accentSecondary`, whatever the design calls it) for each additional accent-shaped role — no cap at one. `Dark*`/`Light*` are two full instances of the data class; each field pulls from whichever palette supplies that color in that theme.

Fields can be added, renamed, or dropped to match the project's real semantic needs — the fixed part is the layer placement rule above and the accent-quad shape, not the specific field list currently in the file.

## Typography (`theme/Type.kt`)

One `FontFamily` val per distinct typeface the design uses. A `FontFamily` usually bundles several `Font(resource, weight)` entries, one per weight the typeface ships — see the commented example in the file. Every slot inside `Typography(...)` carries an inline comment naming its concrete UI purpose in *this* project ("large score display," not "big number").

**This makes the Typography a role mapping, not Material's scale, and that is the single easiest thing in this document to get wrong.** `titleLarge` here is whatever the comment beside it says it is — quite possibly a 14sp button label — and picking a slot by what Material calls it rather than by what this project uses it for is how a screen ends up with its headline rendered at caption size. It has happened at a scale of a dozen slots in one screen, silently, because every one of them compiled.

So: when extending, **read the comments and find the closest existing slot by purpose** before adding a new mapping. When a stock Material component is involved, say what you want explicitly — `TopAppBar` defaults its title slot to `titleLarge`, which in this theme may be nothing like a title.

## Shapes / Spacing (`theme/Shape.kt`, `theme/Spacing.kt`)

A flat `data class` of named tokens with defaults, exposed via a `MaterialTheme` extension property. Token names and scale are a starting point — resize or rename to match the project's actual system.

## Shimmer (`theme/Shimmer.kt`)

The loading placeholder is a token, not a component. `ShimmerPulse` provides one animated alpha and `Modifier.shimmerBlock` paints a block that reads it, so every placeholder on a screen breathes together; the *geometry* of a skeleton belongs to the screen that draws it, because matching the real layout is the entire point.

`LocalShimmerAlpha` has a static default, so a preview or a screenshot test with no `ShimmerPulse` above it still draws the frame — it simply does not animate. The alpha bounds and the pulse duration carry their reasoning at the constants; adjust them for a real design rather than copying numbers into a screen.
