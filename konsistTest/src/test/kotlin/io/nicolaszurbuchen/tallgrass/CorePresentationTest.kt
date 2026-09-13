package io.nicolaszurbuchen.tallgrass

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

/**
 * **The rules for a `presentation/` that has no screens.**
 *
 * `PresentationLayerTest` is written for `feature/`, and nearly every rule in it describes a screen
 * — a Contract, a Store, a Route, the `*UiMapper` that converts one State into one UiModel. A
 * `core/` slice has none of those. It renders vocabulary that several features draw, and that is all
 * it does.
 *
 * Holding it to the feature shape is what leaves a domain-to-UiModel converter with no legal home:
 * the one package allowed to import the domain from inside `presentation/` is `screen/<name>/mapper/`,
 * and a slice with no screens cannot have one. Widening the feature rule would make every feature's
 * exception list longer in order to describe something that is not a feature. This file describes
 * what a `core/` presentation actually is instead.
 *
 * The rules that are genuinely shared — every `component/` rule, the ban on importing `data/`, the
 * `UiModel` suffix — stay in `PresentationLayerTest` and still apply here. Only the screen-shaped
 * ones are replaced.
 *
 * **These rules have no subject until the first `core/` slice grows a `presentation/`.** That is the
 * template's job rather than a dead rule: the shape is stated before there is code to violate it, so
 * the first slice lands in the right shape instead of establishing a wrong one that later has to be
 * argued back.
 */
class CorePresentationTest {
    companion object {
        private val scope = Konsist.scopeFromProduction(moduleName = "shared")

        private const val CORE_PRESENTATION = "..core..presentation.."
    }

    /**
     * **`mapper/` is the only place a `core/` presentation may name a domain type.**
     *
     * The same rule the feature layer applies to `screen/<name>/mapper/`, for the same reason:
     * concentrating the crossing in files named for it is what stops it happening halfway down a
     * component. A component here takes a UiModel and draws it, exactly as one in a feature does.
     */
    @Test
    fun `core presentation may only import the domain from a mapper package`() {
        scope.files
            .withPackage(CORE_PRESENTATION)
            .filterNot { it.hasPackage("..presentation.mapper") }
            .filter { file ->
                file.imports.any { it.name.contains(".domain.") }
            }
            .assertEmpty()
    }

    /**
     * Named for what it does, like every other mapper in the app. A `core/` mapper converts on the
     * way out to a UiModel, so it is a `*UiMapper` rather than the `*Mapper` the data layer uses for
     * its Dto conversions.
     */
    @Test
    fun `files in a core presentation mapper package must be suffixed with UiMapper`() {
        scope.files
            .withPackage("..core..presentation.mapper")
            .assertTrue { it.name.endsWith("UiMapper") }
    }

    /**
     * A UiModel on one side is what makes a function a mapper rather than a helper that happens to
     * live here. The feature layer asserts the same thing about `screen/<name>/mapper/`.
     */
    @Test
    fun `core presentation mapper functions must have a UiModel on one side`() {
        scope.files
            .withPackage("..core..presentation.mapper")
            .assertTrue { file ->
                file.functions(includeNested = false).all { function ->
                    function.returnType?.name?.contains("UiModel") == true ||
                        function.receiverType?.name?.contains("UiModel") == true
                }
            }
    }

    /**
     * Top-level extension functions only — no class to hold state, no object to reach for. The
     * conversion is a function of its receiver and nothing else, and a mapper that needs a
     * constructor parameter is a UseCase wearing the wrong name.
     */
    @Test
    fun `core presentation mapper files must contain only top-level extension functions`() {
        scope.files
            .withPackage("..core..presentation.mapper")
            .assertTrue { file ->
                file.classes().isEmpty() &&
                    file.interfaces().isEmpty() &&
                    file.objects().isEmpty() &&
                    file.functions(includeNested = false).all { it.receiverType != null }
            }
    }

    /**
     * **A `core/` slice has no screens, so it may not carry the files that imply one.**
     *
     * The four MVI interfaces, a Store, a Route, a ViewModel — each one implies the whole apparatus,
     * and a slice that grows them has stopped being a slice and become a feature. That is a real move
     * to make one day; it is not one to make by accident, one file at a time.
     */
    @Test
    fun `core presentation must not contain screen files`() {
        val screenSuffixes = listOf("Contract", "Route", "Screen", "StoreFactory", "ViewModel", "Preview")

        scope.files
            .withPackage(CORE_PRESENTATION)
            .filter { file -> screenSuffixes.any { suffix -> file.name.endsWith(suffix) } }
            .assertEmpty()
    }
}
