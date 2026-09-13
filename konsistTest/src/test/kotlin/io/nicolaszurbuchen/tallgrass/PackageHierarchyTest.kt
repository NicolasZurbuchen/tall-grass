package io.nicolaszurbuchen.tallgrass

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class PackageHierarchyTest {
    companion object {
        private val scope = Konsist.scopeFromProduction(moduleName = "shared")
    }

    @Test
    fun `Direct children of feature or core must be in allowed list`() {
        val allowed = listOf("presentation", "domain", "data", "di")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of presentation must be in allowed list`() {
        val allowed = listOf("screen", "component", "navigation", "uimodel")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.feature\\.[^.]+\\.presentation\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(".feature.").last().split(".")
                allowed.contains(segments.last())
            }
    }

    /**
     * **A `core/` slice is not a feature, so its `presentation/` is not shaped like one.**
     *
     * It has no `screen/`, because it has no screen, and no `navigation/`, because it owns no
     * destination. What it has is rendering vocabulary several features draw — and a `mapper/`,
     * because the domain crossing has to happen somewhere and a slice with no screens has no
     * `screen/<name>/mapper/` to put it in.
     *
     * Splitting this from the feature rule rather than widening it is the point: most of what
     * `konsistTest/` asserts about `presentation/` describes a screen, and a package that has none
     * should be held to what it actually is instead of to the feature shape with holes in it.
     */
    @Test
    fun `Direct children of a core slice presentation must be in allowed list`() {
        val allowed = listOf("component", "uimodel", "mapper")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.core\\.[^.]+\\.presentation\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(".core.").last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of domain must be in allowed list`() {
        val allowed = listOf("model", "repository", "usecase")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.domain\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of data must be in allowed list`() {
        val allowed = listOf("repository", "datasource")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.data\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of data datasource must be in allowed list`() {
        val allowed = listOf("remote", "local")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.data\\.datasource\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of data datasource remote must be in allowed list`() {
        val allowed = listOf("api", "dto", "mapper")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.data\\.datasource\\.remote\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Direct children of data datasource local must be in allowed list`() {
        val allowed = listOf("mapper")

        scope.packages
            .filter { it.name.matches(Regex(".*\\.(feature|core)\\.[^.]+\\.data\\.datasource\\.local\\.[^.]+$")) }
            .assertTrue { pkg ->
                val segments = pkg.name.split(Regex("\\.(feature|core)\\.")).last().split(".")
                allowed.contains(segments.last())
            }
    }

    @Test
    fun `Top level packages must be in allowed list`() {
        val allowed = listOf("app", "core", "design", "feature", "infra")

        val allPackages = Konsist.scopeFromDirectory("shared/src/commonMain/kotlin").packages
        val allPackageNames = allPackages.map { it.name }

        if (allPackageNames.isEmpty()) return

        val rootPrefix =
            allPackageNames
                .reduce { acc, name -> acc.commonPrefixWith(name).trimEnd('.') }

        allPackages
            .assertTrue { pkg ->
                val relative = pkg.name.removePrefix("$rootPrefix.").trimStart('.')
                if (relative.isEmpty()) {
                    true
                } else {
                    relative.split(".").first() in allowed
                }
            }
    }

    @Test
    fun `Leaf packages must not have child packages`() {
        val leafPackageNames =
            setOf("api", "component", "di", "dto", "mapper", "model", "navigation", "repository", "usecase", "uimodel")

        val allPackages = Konsist.scopeFromProject().packages
        val allPackageNames = allPackages.map { it.name }.toSet()

        allPackages
            .filter { pkg ->
                pkg.name.contains(Regex("\\.(feature|core)\\.")) &&
                    pkg.name.split(".").last() in leafPackageNames
            }
            .assertTrue { pkg ->
                allPackageNames.none { it.startsWith("${pkg.name}.") }
            }
    }

    /**
     * **A file in a platform source set says so in its name.** `Platform.kt`, `Platform.android.kt`
     * and `Platform.ios.kt` are three files a search returns together, and only the suffix tells you
     * which one you are reading before you open it.
     *
     * It applies to every file in the source set, not only to `actual` declarations: a module with a
     * twin per platform that is not an expect/actual pair is the same problem for a reader.
     */
    @Test
    fun `platform source set files must name their platform`() {
        scope.files
            .filter { it.resideInSourceSet("androidMain") }
            .assertTrue { it.name.endsWith(".android") }

        scope.files
            .filter { it.resideInSourceSet("iosMain") }
            .assertTrue { it.name.endsWith(".ios") }
    }

    /**
     * Three, and each is a different kind of thing the screen owns: composables it reuses
     * (`component`), the pieces of its vocabulary that are not the model itself (`uimodel`), and the
     * domain-to-presentation converters (`mapper`). A screen package with no subfolders is still the
     * common case — these appear when there is more than one of something.
     */
    @Test
    fun `Screen name packages must not have child packages other than component, uimodel or mapper`() {
        scope.files
            .filter { file ->
                file.packagee?.name?.contains(".presentation.screen.") == true
            }
            .assertTrue { file ->
                file.packagee?.name
                    ?.matches(
                        Regex(
                            ".*\\.(feature|core)\\.[^.]+\\.presentation\\.screen\\.[^.]+(\\.(component|uimodel|mapper))?$",
                        ),
                    ) == true
            }
    }
}
