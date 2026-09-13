package io.nicolaszurbuchen.tallgrass

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import kotlin.test.Test

/**
 * **The five directions the dependency graph is allowed to run.** Together they are acyclic and
 * complete: `infra/` imports nothing here, `design/` imports only `infra/`, `core/` may not look up,
 * a feature may not look sideways, and nothing may look at `app/` at all.
 *
 * Only the feature rule existed before the layout changed. The rest were unstateable — `design/`
 * lived inside `app/`, so "nothing imports `app/`" was false by construction and "`design/` does not
 * know the domain" had no package to name.
 */
class LayerBoundariesTest {
    companion object {
        /**
         * Every check below is written against this rather than against a bare `.core.` or `.app.`
         * substring, because those match libraries too — `.core.` alone flags every
         * `androidx.compose.animation.core` import in the theme.
         */
        private val projectPrefix =
            Konsist
                .scopeFromProduction(moduleName = "shared")
                .packages
                .map { it.name }
                .reduce { acc, name -> acc.commonPrefixWith(name).trimEnd('.') }

        /** True when [importName] is a project import from the top-level package [topLevel]. */
        private fun isProjectImportFrom(
            importName: String,
            topLevel: String,
        ): Boolean = importName.startsWith("$projectPrefix.$topLevel.")
    }

    /**
     * **A feature may not reach into another feature at all**, not merely into its internals.
     *
     * This used to exempt `domain`, flagging only imports containing `.presentation` or `.data`, so
     * one feature could take a model straight off another. A domain type two features share is
     * `core/` material by the placement rule, and borrowing one across the boundary is how a slice
     * quietly stops being one.
     */
    @Test
    fun `feature layers should not depend on other features`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.hasPackage("..feature..") }
            .assertFalse { file ->
                val currentFeature = (file.packagee?.name ?: "").substringAfter("feature.").substringBefore(".")

                file.imports.any { import ->
                    isProjectImportFrom(import.name, "feature") &&
                        import.name.substringAfter("feature.").substringBefore(".") != currentFeature
                }
            }
    }

    /**
     * **`infra/` imports nothing of this app**, which is the property the placement rule leans on: a
     * file goes here when it would be as much at home in another project, and that is only checkable
     * if nothing here names a domain type, a token or a screen.
     *
     * Widened from *may not import features*, which left `infra -> core` and `infra -> design` open
     * — `infra/` was the one layer whose stated invariant nothing enforced.
     *
     * The generated packages are not exceptions to it. `cache` is SQLDelight's output and the
     * `*.shared.generated.resources` package is Compose's resource accessor: both are written by the
     * build from files `infra/` already owns, and neither is a layer.
     */
    @Test
    fun `infra should not depend on anything else in the project`() {
        val layers = listOf("app", "core", "design", "feature")

        Konsist.scopeFromProject()
            .files
            .filter { it.hasPackage("..infra..") }
            .assertFalse { file ->
                file.imports.any { import -> layers.any { isProjectImportFrom(import.name, it) } }
            }
    }

    /**
     * **Nothing inside `shared` may import `app/`.** That is what makes it the composition root
     * rather than a package that happens to hold `App.kt`: it imports everything, and an importer
     * would close a cycle with whatever it imports.
     *
     * Unstateable while the design system lived in `app/design/`, which every screen reads.
     *
     * The platform binaries are the deliberate exception and are outside this scope: `MainActivity`
     * and the `Application` subclass in `androidApp/` reach for `App` and `initKoin`, which is a
     * binary consuming its own root rather than a layer violation.
     */
    @Test
    fun `nothing in shared outside the app shell may import it`() {
        val belowTheRoot = listOf("core", "design", "feature", "infra")

        Konsist
            .scopeFromProduction(moduleName = "shared")
            .files
            .filter { file -> belowTheRoot.any { file.hasPackage("..$it..") } }
            .assertFalse {
                it.imports.any { import -> isProjectImportFrom(import.name, "app") }
            }
    }

    /**
     * **`core/` is below the features, and `app/` is above them.** An import in either direction from
     * here is the dependency graph folding back on itself: `core/` would be reaching up into
     * something that composes it, and every screen that reads `core/` would inherit the reach.
     */
    @Test
    fun `core should not depend on features or the app shell`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.hasPackage("..core..") }
            .assertFalse {
                it.imports.any { import ->
                    isProjectImportFrom(import.name, "feature") || isProjectImportFrom(import.name, "app")
                }
            }
    }

    /**
     * **The design system is the base of the graph, so nothing it imports may sit above it.**
     *
     * This is what makes `design/` a system rather than the folder shared UI ends up in: the whole of
     * it can be read without meeting a domain type. A component that needs one owns a rule about the
     * subject and belongs beside that subject, in `core/<slice>/presentation/`.
     *
     * The linguistic form of the same test is the `App` prefix: it parses as *our version of a
     * generic thing*. `AppFilterChip` reads fine; a name like `AppInvoiceTimeline` reads as nonsense,
     * and that is the signal it is not a design system component.
     */
    @Test
    fun `design should not depend on the domain, the features or the app shell`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.hasPackage("..design..") }
            .assertFalse {
                it.imports.any { import ->
                    isProjectImportFrom(import.name, "core") ||
                        isProjectImportFrom(import.name, "feature") ||
                        isProjectImportFrom(import.name, "app")
                }
            }
    }
}
