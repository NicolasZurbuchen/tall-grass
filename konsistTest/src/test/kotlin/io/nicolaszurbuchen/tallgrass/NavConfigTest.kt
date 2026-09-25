package io.nicolaszurbuchen.tallgrass

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

/**
 * **A destination that is not registered in `navConfig` crashes when Android saves state.**
 *
 * `NavKey` is an interface rather than a sealed hierarchy, so kotlinx.serialization cannot find its
 * subclasses on its own and `navConfig` names each one by hand. Forget one and nothing complains:
 * the app compiles, the screen opens, the back stack works. Then the reader backgrounds the app on
 * that screen and `onSaveInstanceState` throws `Serializer for subclass '...' is not found in the
 * polymorphic scope of 'NavKey'`.
 *
 * That shipped for both Moves destinations, and was found on a device rather than by any of the five
 * commands CLAUDE.md lists. This is the rule that would have caught it.
 *
 * It reads `NavConfig.kt` as text rather than resolving the calls, which is the trade Konsist offers:
 * a destination renamed but still registered under its old name would pass here and fail to compile
 * one file over, so nothing is lost.
 */
class NavConfigTest {
    companion object {
        private val scope = Konsist.scopeFromProduction(moduleName = "shared")

        private val navConfigSource =
            scope.files
                .single { it.name == "NavConfig" }
                .text

        /**
         * Concrete destinations only, which `classesAndObjects` gives for free: the per-feature
         * groupings above them -- `MovesDestination` and its siblings -- are sealed *interfaces*,
         * so they are not in this list at all. Nothing is ever an instance of one, and registering
         * one would be registering a type nothing serializes.
         */
        private val destinations =
            scope.files
                .withPackage("..presentation.navigation..")
                .flatMap { file -> file.classes() + file.objects() }
                .filter { it.name.endsWith("Destination") }
    }

    @Test
    fun `every navigation destination is registered in navConfig`() {
        // Guards the guard: a filter that quietly stopped matching would make this rule pass forever.
        check(destinations.size >= EXPECTED_AT_LEAST) {
            "Found ${destinations.size} destinations, which is fewer than this project has. " +
                "The filter in NavConfigTest has stopped matching."
        }

        destinations.assertTrue { navConfigSource.contains("subclass(${it.name}::class)") }
    }
}

// Seven at the time of writing: home, the dex and its detail, the moves list and its detail, the
// abilities list and its detail. A floor rather than an equality, so adding a destination does not
// fail this line as well as the rule above -- which is the one that should speak.
private const val EXPECTED_AT_LEAST = 7
