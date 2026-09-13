# Agent Documentation Conventions

Where prose goes, and how much of it. This file is about *routing* — writing is rarely bad in a
codebase like this, it is filed in the wrong place, and a sentence in the wrong file is a second
source of truth waiting to diverge from the first.

## The four questions, and the one home each has

Every piece of prose answers exactly one of these. Write it in that home and nowhere else.

| The reader is asking | It goes in | The test |
|---|---|---|
| *What does this do, how do I call it?* | **KDoc** — the contract | Would a caller need it to call this correctly? |
| *Why is this line surprising?* | **`//` at the line**, one or two lines | Is the surprise in the **code**? |
| *Why is the app like this?* | **`DECISIONS.md`** | Is the surprise in the **design**? |
| *How does this feature behave?* | **the tests** | — |

The third row is the one that gets violated. A measurement, a rejected alternative, a trade-off that
was weighed — *cards cost +32% vertical space*, *this gold measures 1.9:1 against the surface*, *22sp
looked like a caption* — is a decision record. It belongs in `DECISIONS.md` even when the code it
justifies is right here. Especially then: it is the code being right *for a reason* that makes the
reason worth keeping, and the reason outlives the file.

## KDoc — where the line is

**KDoc goes on the narrowest declaration a caller binds to without reading the body.**

| Declaration | KDoc? | Why |
|---|---|---|
| `interface` — repository, data source, port | **Always** | This *is* the contract: what it promises, which `AppError` it throws, what it guarantees about ordering and nullability. Callers bind here and never open the impl. |
| `sealed interface` / `sealed class` — domain model, `AppError`, Intent, Label | **On the type, once** | The *set* needs explaining: why these members and not others. Not on each member. |
| `...Impl` class | **Only for a non-obvious strategy** | The interface holds the contract. A scheduler that rebuilds everything on every resume earns it; a repository that delegates does not. |
| public `fun`, use case | **When the contract is not in the signature** | Preconditions, error behaviour, ordering. Never a restatement of the name. |
| `data class` — UiModel, domain model | **Only for a cross-field invariant** | *"Exactly one of `sections` and `catalogue` is ever populated"* is real and unrecoverable from the type. *"Holds the main screen state"* is the name again. |
| property, constructor parameter | **Almost never** | See below. |
| `private fun` | **No** | Nothing outside the file can call it. If the name does not say what it does, rename it — that is the fix, not a paragraph. |
| `override` | **No** | The supertype has it. |
| `@Preview` function, `object`, `const`, enum entry | **No** | |

Go gates doc comments on exportedness and Rust's `missing_docs` fires only on public items. Both
communities landed on the same line: **visibility decides.**

### Properties

A property earns documentation only when it carries an **invariant its type does not express** — and
then it goes in the *class* KDoc as a `[property]` reference, one line, not its own block.

- a nullable field whose nullability means something specific (*non-null only when exactly one day is
  in scope*) — **one line.**
- `isLoading: Boolean`, `id: String`, `label: UiText` — **nothing.**

When four properties each need an invariant explained, the type is too wide. Fix the type.

### KDoc is a contract, not a history

No *"this used to be written twice"*, no *"replaces the old shape"*, no migration notes. Git holds
that. A KDoc that narrates how the code got here is describing a file that no longer exists.

Likewise, do not restate a rule that already lives in `CLAUDE.md` or
`agents/agent-architecture-convention.md`. *"It is `infra/` because it does not know this app
exists"* is the package placement rule, and it is written down once already.

## Inline comments — the surprise test

A `//` comment earns its place when a careful reader would be **surprised by the code**, and it is
one or two lines.

Earns it — the surprise is mechanical, and getting it wrong is a crash or a silent bug:

- `// Weights must be positive, so an item flush against the previous one contributes no spacer.`
- `// matchParentSize rather than fillMaxSize, so nothing here is measured against unbounded height.`
- `// A Route may only take lambdas, a Modifier or a ViewModel.`

Does not — the surprise is in the design, and the design has a file:

- *why the scrim is dark at both ends*, *why the price sits on its own line* → `DECISIONS.md`.

The magic-number rule in `CLAUDE.md` is the one case that survives all of this: a bound still gets
its "why" at the constant, because a name alone only moves the question.

## Repeated prose is a detector, not a diagnosis

**The same explanation in two files always means something is wrong. It does not always mean the
same thing is wrong**, and the two cases are indistinguishable from a grep.

**Before deciding, read the code under the sentence.** That is the whole test: is the code duplicated
too, or only the prose?

**The abstraction is missing — extract it.** A ratio constant declared in three files under three
copies of one sentence about a slow connection. The constant, the placeholder and the same five
arguments were all copied along with the prose, and the comment is *why nobody noticed*: it made
copy-paste read as considered. One component is where all of it belongs, and the call sites then say
nothing, because nothing surprising is left at them.

**The abstraction is already there — point at it.** The same sentence in four files, where the type
that defines the concept already existed and every call site already carried it. There was nothing to
extract: the code was right, and the prose had been written past it. The reasoning stays once at the
type, and the use sites reference it.

Getting this backwards is expensive in both directions — extracting what is already extracted invents
a wrapper nobody needed, and pointing at an abstraction that does not exist leaves the duplication in
place with a citation on top of it.

Three ways to point, and all three keep the prose in one place:

1. **A KDoc `[Reference]`.** It resolves in the IDE and renders as a link in Dokka, so it is
   checkable in a way prose is not.
2. **A stable anchor into the record**: `// DECISIONS.md § Offline images`. The same move as Google's
   `// TODO(b/123456)` — point at the record, do not carry it.
3. **A test.** An invariant enforced in one place beats an invariant described in three.

Never copy a sentence between files.

## Behaviour is documented by the tests

Test names are `subject_condition_expectedBehaviour` and read as specification:

```
toUiModel_emptyHistory_producesNullHeroAndEmptyHistory
toUiModel_numberTextPadsSpeciesIdToThreeDigits
```

**Write the name so it survives being read on its own, out of the file, by someone who has not seen
the code.** That is the whole mechanism: the behaviour document is generated from these names, so it
cannot drift from what actually runs. A comment inside a test saying *why this case matters* is part
of the spec and is welcome; one restating the assertion is not.

## Exemptions

Three places where the rules above do not apply, and the reason is the same each time: the file's
whole job is explaining something.

- **`konsistTest/`.** A rule whose reasoning is not attached is a rule the next person deletes when it
  is inconvenient. Every rule carries why it exists and what it is protecting against; that is what
  makes "do not loosen a Konsist rule to go green" hold.
- **A platform how-to that cannot be recovered from the code.** Notes on exercising something by hand
  on a device — which clock to move, what to warm first — are not obvious, not testable, and belong
  where someone would look for them.
- **`DECISIONS.md`, `agents/`, and any product docs a fork adds.** Prose is the product.

## What this is not

This is not the position that comments are failures. Code does not document its own reasoning, and a
codebase with no prose is not the goal — an interface with no contract on it is a worse defect than a
paragraph in the wrong file. The target is *fewer, better placed*, not *fewer*.

As calibration: this template's production source is currently about 8% comment by line, and a
project that has been through real design iteration tends to drift towards a quarter before anyone
notices. Somewhere near a tenth is where this lands. Treat that as a smell threshold, not a number to
hit.

## Applying it

**New code follows this from the first commit. Existing code drains by contact** — when a file is
touched for another reason, its prose comes into line then. There is no migration pass: a rewrite
across every file, with no behaviour change, would bake in whatever is wrong with these rules before
they have been used on real work.

The exception is duplication. Three copies of a constant is a defect, not a style preference, and it
gets fixed when it is found.
