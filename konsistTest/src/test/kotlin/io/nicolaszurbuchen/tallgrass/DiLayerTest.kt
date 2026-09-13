package io.nicolaszurbuchen.tallgrass

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class DiLayerTest {
    companion object {
        private val scope = Konsist.scopeFromProduction(moduleName = "shared")

        private val projectPackagePrefix =
            scope.packages
                .map { it.name }
                .reduce { acc, name ->
                    acc.commonPrefixWith(name).trimEnd('.')
                }
    }

    // region Package conventions

    @Test
    fun `files in di package must be suffixed with Module`() {
        scope.files
            .withPackage("..di..")
            .filter { it.hasPackage("..feature..di..") || it.hasPackage("..core..di..") }
            .assertTrue { it.name.endsWith("Module") }
    }

    /**
     * **This could not fail as first written.** The filter selected only files already in a `di`
     * package and then asserted they were in a `di` package — moving a Module out of its slice
     * removed it from the filter rather than failing the rule.
     *
     * It now selects every `*Module.kt` under a `feature/` or `core/` slice, wherever it sits,
     * and requires a `di` package. `app/` and `infra/` are out of scope on purpose: both keep
     * their modules beside the code they wire, which is right for packages that have no slices to
     * group by — `infra/network/NetworkModule.kt` beside `HttpClientFactory` says more than an
     * `infra/di/` holding every module would.
     */
    @Test
    fun `files suffixed with Module must reside in di package`() {
        scope.files
            .withNameEndingWith("Module")
            .filter { it.hasPackage("..feature..") || it.hasPackage("..core..") }
            .assertTrue { it.hasPackage("..di..") }
    }

    // endregion

    // region Dependency boundaries

    @Test
    fun `di modules must only import from their own subtree`() {
        scope.files
            .withPackage("..di..")
            .filter { it.hasPackage("..feature..di..") || it.hasPackage("..core..di..") }
            .assertTrue { file ->
                // The aggregator allowed to cross subtrees is `app/di/AppModule.kt`, which this
                // filter never selected. The exclusion that used to sit here named `infra.di.app`,
                // a package this template has never had.
                val ownSubtree =
                    projectSubtree(file.packagee?.name)
                        ?: return@assertTrue true

                file.imports.all { import ->
                    val importSubtree = projectSubtree(import.name)
                    importSubtree == null || importSubtree == ownSubtree
                }
            }
    }

    // endregion

    /**
     * The owning subtree of a project package:
     *   <prefix>.feature.home.di          -> "feature.home"
     *   <prefix>.core.error               -> "core.error"
     *   <prefix>.infra.text                 -> "infra"
     * Returns null for external (non-project) packages.
     */
    private fun projectSubtree(qualifiedName: String?): String? {
        if (qualifiedName == null || !qualifiedName.startsWith(projectPackagePrefix)) return null
        val segments =
            qualifiedName
                .removePrefix(projectPackagePrefix)
                .trimStart('.')
                .split('.')
        return when (segments.firstOrNull()) {
            "feature", "core" -> segments.take(2).joinToString(".")
            "infra" -> "infra"
            else -> null
        }
    }
}
