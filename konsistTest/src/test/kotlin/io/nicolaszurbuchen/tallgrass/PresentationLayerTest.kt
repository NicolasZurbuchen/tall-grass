package io.nicolaszurbuchen.tallgrass

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class PresentationLayerTest {
    companion object {
        private val scope = Konsist.scopeFromProduction(moduleName = "shared")

        private val screenFileSuffixes =
            setOf(
                "Contract",
                "Preview",
                "Route",
                "Screen",
                "StoreFactory",
                "UiMapper",
                "UiModel",
                "ViewModel",
            )

        /**
         * Neither `app/` nor `design/` is a feature, and neither builds screens the way one does.
         *
         * `app/` is the shell: it owns the navigation host and any screen that sits outside the
         * feature stacks — a splash gate is a composable and its preview, with no Contract, Store or
         * UiModel to layer and no domain or data sibling to justify a `presentation/screen/` package
         * around it.
         *
         * `design/` is the design system, and the one file it has that trips these rules is the
         * preview *harness*, which is never drawn in a shipped screen. Where it must live is
         * asserted positively by `PreviewTest`, so excusing it here loses nothing.
         *
         * Only `Screen` and `Preview` are excused. The four MVI files are not, because each one
         * implies the whole apparatus: a shell screen that grows a Store needs the screen package
         * the same way a feature does.
         */
        private fun List<KoFileDeclaration>.outsideFeatureScreens(): List<KoFileDeclaration> =
            filterNot { it.hasPackage("..app..") || it.hasPackage("..design..") }

        /** A screen's own subfolders. A file in one of these is not a screen file. */
        private val screenSubpackages = listOf("component", "uimodel", "mapper")
    }

    // region Name implies location

    @Test
    fun `files suffixed with Contract must reside in screen package`() {
        scope.files
            .withNameEndingWith("Contract")
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    @Test
    fun `files suffixed with Route must reside in screen package`() {
        scope.files
            .withNameEndingWith("Route")
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    @Test
    fun `files suffixed with Screen must reside in screen package`() {
        scope.files
            .withNameEndingWith("Screen")
            .outsideFeatureScreens()
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    @Test
    fun `files suffixed with Preview must reside in screen package`() {
        scope.files
            .withNameEndingWith("Preview")
            .outsideFeatureScreens()
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    @Test
    fun `files suffixed with StoreFactory must reside in screen package`() {
        scope.files
            .withNameEndingWith("StoreFactory")
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    @Test
    fun `files suffixed with ViewModel must reside in screen package`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .assertTrue { it.hasPackage("..presentation.screen..") }
    }

    // endregion

    // region Location implies name

    @Test
    fun `files in screen packages must use an allowed suffix`() {
        scope.files
            .withPackage("..presentation.screen..")
            .filter { file -> screenSubpackages.none { sub -> file.hasPackage("..$sub") } }
            .assertTrue { file -> screenFileSuffixes.any { suffix -> file.name.endsWith(suffix) } }
    }

    // endregion

    // region Component package rules

    @Test
    fun `files in component packages must not use a screen file suffix`() {
        val screenSuffixes = listOf("Contract", "Preview", "Route", "Screen", "ViewModel")

        scope.files
            .withPackage("..component..")
            .filter { file ->
                screenSuffixes.any { suffix -> file.name.endsWith(suffix) }
            }
            .assertEmpty()
    }

    @Test
    fun `files in component packages must contain at least one Composable-annotated function`() {
        scope.files
            .withPackage("..component..")
            .assertTrue { file ->
                file.functions().any { it.hasAnnotationWithName("Composable") }
            }
    }

    @Test
    fun `files in component packages must declare only functions`() {
        scope.files
            .withPackage("..component..")
            .filter { file ->
                file.classes().isNotEmpty() || file.interfaces().isNotEmpty() || file.objects().isNotEmpty()
            }
            .assertEmpty()
    }

    @Test
    fun `files in component packages must expose exactly one public declaration`() {
        val composableScopeReceivers = setOf("LazyListScope", "ColumnScope", "RowScope", "BoxScope")

        scope.files
            .withPackage("..component..")
            .assertTrue { file ->
                val publicFunctions = file.functions().filter { it.hasPublicOrDefaultModifier }
                if (publicFunctions.size != 1) return@assertTrue false

                val fn = publicFunctions.first()
                fn.hasAnnotationWithName("Composable") ||
                    fn.receiverType?.name in composableScopeReceivers
            }
    }

    @Test
    fun `files in component packages must not import from domain or data layers`() {
        scope.files
            .withPackage("..component..")
            .filter { file ->
                file.imports.any { import ->
                    import.name.contains(".domain.") || import.name.contains(".data.")
                }
            }
            .assertEmpty()
    }

    // endregion

    // region UiModel placement

    /**
     * A screen's own `<Screen>UiModel` is what the Composable is handed, and it belongs beside the
     * Screen that takes it. Everything else suffixed `UiModel` is a *piece* of that vocabulary, and
     * those go in a `uimodel` package, the same way a reusable composable goes in `component`.
     *
     * Without the split, a screen package is a flat list where the type the screen actually renders
     * sits between two enums it merely mentions, and the reader has to open all three to find out
     * which is which.
     */
    @Test
    fun `only the screen's own UiModel may sit directly in a screen package`() {
        scope.files
            .withNameEndingWith("UiModel")
            .withPackage("..presentation.screen..")
            .filterNot { it.hasPackage("..uimodel") }
            .assertTrue { file ->
                val screenName =
                    scope.files
                        .filter { it.packagee?.name == file.packagee?.name }
                        .firstOrNull { it.name.endsWith("Screen") }
                        ?.name
                        ?.removeSuffix("Screen")

                screenName == null || file.name == "${screenName}UiModel"
            }
    }

    @Test
    fun `a screen's own UiModel must not be hidden in the uimodel package`() {
        // The other half of the rule above: moving it in there would leave the Screen taking a type
        // from a package of parts, which says it is one of them.
        scope.files
            .withNameEndingWith("UiModel")
            .withPackage("..presentation.screen..uimodel")
            .assertTrue { file ->
                val screenPackage = file.packagee?.name?.removeSuffix(".uimodel")
                val screenName =
                    scope.files
                        .filter { it.packagee?.name == screenPackage }
                        .firstOrNull { it.name.endsWith("Screen") }
                        ?.name
                        ?.removeSuffix("Screen")

                screenName == null || file.name != "${screenName}UiModel"
            }
    }

    /**
     * A `UiModel` file is the screen's vocabulary, and the suffix is what says "this is what the
     * screen is handed" rather than "this is a domain type that leaked". Without the rule the suffix
     * survives on the record classes — which the mapper returns and so cannot be misnamed — and
     * quietly stops applying to the enums beside them, which is exactly where the confusion with a
     * domain enum of the same name would start.
     *
     * Top-level only. Members of a sealed hierarchy read as `BlockUiModel.Countdown`, where the
     * parent already carries the suffix and repeating it on the child says nothing.
     */
    @Test
    fun `top-level types in UiModel files must be suffixed with UiModel`() {
        scope.files
            .withNameEndingWith("UiModel")
            .assertTrue { file ->
                (file.classes(includeNested = false) + file.interfaces(includeNested = false) + file.objects(includeNested = false))
                    .all { it.name.endsWith("UiModel") }
            }
    }

    // endregion

    // region StoreFactory purity

    /**
     * A StoreFactory file holds the Store interface and the factory that builds it. Nothing else.
     *
     * The case this exists for is the domain-to-UiModel converter that grows at the bottom of one:
     * real work, used by exactly one Executor, and putting it there makes a file about wiring also
     * the place a reader has to look to find out how a domain type becomes a rendering one. Those
     * belong in `mapper/`.
     */
    @Test
    fun `StoreFactory files must not declare top-level functions`() {
        scope.files
            .withNameEndingWith("StoreFactory")
            .withPackage("..presentation.screen..")
            .assertTrue { file -> file.functions(includeNested = false).isEmpty() }
    }

    @Test
    fun `StoreFactory files must declare only the Store interface and the factory`() {
        scope.files
            .withNameEndingWith("StoreFactory")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("StoreFactory")
                val allowed = setOf("${prefix}Store", "${prefix}StoreFactory")

                (
                    file.classes(includeNested = false) +
                        file.interfaces(includeNested = false) +
                        file.objects(includeNested = false)
                ).all { it.name in allowed }
            }
    }

    // endregion

    // region presentation mappers

    /**
     * The conversion from a domain type to its presentation twin is a mapper, and mappers live in a
     * `mapper` package — the same rule the data layer already follows for DTO-to-domain.
     *
     * This is the one place inside `presentation/` allowed to import the domain layer, and that is
     * the point of concentrating it: the boundary is crossed in files whose whole job is crossing it,
     * rather than at the bottom of whichever file happened to need it first.
     */
    @Test
    fun `files in a presentation mapper package must be suffixed with UiMapper`() {
        scope.files
            .withPackage("..presentation.screen..mapper")
            .assertTrue { it.name.endsWith("UiMapper") }
    }

    @Test
    fun `presentation mapper files must contain only top-level extension functions`() {
        scope.files
            .withPackage("..presentation.screen..mapper")
            .assertTrue { file ->
                file.classes(includeNested = true).isEmpty() &&
                    file.interfaces(includeNested = true).isEmpty() &&
                    file.objects(includeNested = true).isEmpty() &&
                    file.functions(includeNested = false).all { it.hasReceiverType() }
            }
    }

    /**
     * **Both directions, because both are the same crossing.**
     *
     * The common direction is domain in, UiModel out. The other one exists: a tap hands back the
     * UiModel it was drawn from, and the Intent it becomes should name the domain. Nothing else in
     * `presentation/` may write that conversion — this package is the exemption — so forbidding it
     * here would forbid it everywhere, and the way out would be to let the Contract name a UiModel
     * instead. That is the wrong trade: it moves a rendering type into the Store to avoid a function
     * in the one package built to hold it.
     *
     * A UiModel on either side is what makes it a mapper. A function with a UiModel on neither is a
     * helper that happens to live here.
     */
    @Test
    fun `presentation mapper functions must have a UiModel on one side`() {
        scope.files
            .withPackage("..presentation.screen..mapper")
            .assertTrue { file ->
                file.functions(includeNested = false).all { function ->
                    function.returnType?.name?.contains("UiModel") == true ||
                        function.receiverType?.name?.contains("UiModel") == true
                }
            }
    }

    // endregion

    // region Screen subfolder rules

    @Test
    fun `screen folders must contain Route and Screen`() {
        val screenPackages =
            scope.files
                .withPackage("..presentation.screen..")
                .filter { file -> screenSubpackages.none { sub -> file.hasPackage("..$sub") } }
                .groupBy { it.packagee?.name }

        screenPackages.forEach { (packageName, files) ->
            val names = files.map { it.name }.toSet()
            assert(names.any { it.endsWith("Route") }) { "Missing Route in $packageName" }
            assert(names.any { it.endsWith("Screen") }) { "Missing Screen in $packageName" }
        }
    }

    @Test
    fun `stateful screen files must appear as a complete set`() {
        val statefulFiles = setOf("Contract", "StoreFactory", "ViewModel")

        val screenPackages =
            scope.files
                .withPackage("..presentation.screen..")
                .filter { file -> statefulFiles.any { suffix -> file.name.endsWith(suffix) } }
                .groupBy { it.packagee?.name }

        screenPackages.forEach { (packageName, files) ->
            val presentSuffixes =
                files.map { file ->
                    statefulFiles.first { suffix -> file.name.endsWith(suffix) }
                }.toSet()

            assert(presentSuffixes == statefulFiles) {
                "Incomplete stateful set in $packageName: found $presentSuffixes, expected $statefulFiles"
            }
        }
    }

    // endregion

    // region Route file rules

    @Test
    fun `Route files must contain exactly one declaration and it must be a public function`() {
        scope.files
            .withNameEndingWith("Route")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val declarations = file.functions() + file.classes() + file.interfaces() + file.objects()
                declarations.size == 1 && file.functions().single().hasPublicOrDefaultModifier
            }
    }

    @Test
    fun `Route files must contain a function matching the file name`() {
        scope.files
            .withNameEndingWith("Route")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions().any { it.name == file.name }
            }
    }

    @Test
    fun `Route public function must be annotated with Composable`() {
        scope.files
            .withNameEndingWith("Route")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions().single { it.hasPublicOrDefaultModifier }.hasAnnotationWithName("Composable")
            }
    }

    @Test
    fun `Route function parameters must only be lambdas, Modifier, or ViewModel`() {
        scope.files
            .withNameEndingWith("Route")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions()
                    .filter { it.hasPublicOrDefaultModifier }
                    .all { function ->
                        function.parameters.all { param ->
                            param.type.name == "Modifier" ||
                                param.type.name.endsWith("ViewModel") ||
                                param.type.isFunctionType
                        }
                    }
            }
    }

    @Test
    fun `Route function ViewModel parameter must have a default value`() {
        scope.files
            .withNameEndingWith("Route")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions()
                    .filter { it.hasPublicOrDefaultModifier }
                    .all { function ->
                        val viewModelParam = function.parameters.firstOrNull { it.type.name.endsWith("ViewModel") }
                        viewModelParam?.hasDefaultValue() ?: true
                    }
            }
    }

    // endregion

    // region ViewModel file rules

    @Test
    fun `ViewModel files must contain exactly one declaration and it must be a public class`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val declarations = file.classes() + file.interfaces() + file.objects() + file.functions().filter { it.isTopLevel }
                declarations.size == 1 && file.classes().single().hasPublicOrDefaultModifier
            }
    }

    @Test
    fun `ViewModel classes must extend ViewModel`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.classes().single().hasParentWithName("ViewModel")
            }
    }

    @Test
    fun `ViewModel classes must have a StoreFactory constructor parameter`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.classes().single().primaryConstructor?.parameters
                    ?.any { it.type.name.endsWith("StoreFactory") } ?: false
            }
    }

    @Test
    fun `ViewModel state property must be a StateFlow of the matching UiModel type`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val className = file.classes().single().name
                val prefix = className.removeSuffix("ViewModel")
                val stateProperty =
                    file.classes().single()
                        .properties().firstOrNull { it.name == "state" }
                stateProperty == null || stateProperty.type?.name == "StateFlow<${prefix}UiModel>"
            }
    }

    @Test
    fun `ViewModel labels property must be a Flow of the matching Label type`() {
        scope.files
            .withNameEndingWith("ViewModel")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val className = file.classes().single().name
                val prefix = className.removeSuffix("ViewModel")
                val labelsProperty =
                    file.classes().single()
                        .properties().firstOrNull { it.name == "labels" }
                labelsProperty == null || labelsProperty.type?.name == "Flow<${prefix}Label>"
            }
    }

    // endregion

    // region Contract file rules

    @Test
    fun `Contract files must contain an Intent sealed interface`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.interfaces().any { it.name == "${prefix}Intent" && it.hasSealedModifier }
            }
    }

    @Test
    fun `Contract files must contain a Label sealed interface`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.interfaces().any { it.name == "${prefix}Label" && it.hasSealedModifier }
            }
    }

    @Test
    fun `Contract files must contain an Action sealed interface`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.interfaces().any { it.name == "${prefix}Action" && it.hasSealedModifier }
            }
    }

    @Test
    fun `Contract files must contain a Message sealed interface`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.interfaces().any { it.name == "${prefix}Message" && it.hasSealedModifier }
            }
    }

    @Test
    fun `Contract files must contain a State data class`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.classes().any { it.name == "${prefix}State" && it.hasDataModifier }
            }
    }

    @Test
    fun `top-level classes in Contract files must be the State data class`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                file.classes()
                    .filter { it.isTopLevel }
                    .all { cls -> cls.name == "${prefix}State" }
            }
    }

    @Test
    fun `top-level interfaces in Contract files must be the four MVI sealed interfaces or a State`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Contract")
                val allowedNames =
                    setOf(
                        "${prefix}Intent",
                        "${prefix}Label",
                        "${prefix}Action",
                        "${prefix}Message",
                    )
                file.interfaces()
                    .filter { it.isTopLevel }
                    .all { it.name in allowedNames || it.name.endsWith("State") }
            }
    }

    /**
     * **A UiModel is what a Composable is handed, and it exists nowhere else.**
     *
     * The Contract is the Store's own vocabulary — Intent, Label, Action, Message, State — and it is
     * written in domain terms. A `PhaseUiModel` on a Message looks harmless because the conversion
     * has to happen somewhere, but it drags the presentation type backwards through the Executor and
     * the Reducer, so the Store ends up holding a type whose whole purpose is to be rendered. The
     * conversion belongs on the way *out*, in the UiMapper.
     *
     * Imports cannot catch this: the offending types usually sit in the same package as the Contract
     * that uses them, so there is nothing to import. It is checked on declared types instead.
     *
     * **It forbids rendering models, not UI state, and the difference is who the type is for.** A
     * Store may hold state that has no domain behind it — which of five chips is selected is a real
     * thing to remember and the subject has no opinion about it. What it may not hold is the type a
     * Composable is handed. The `State` suffix allowed above is where the first kind goes.
     *
     * The check is the `UiModel` suffix, so it is a naming convention with a tripwire on it rather
     * than a proof — renaming a type past it is possible. That is only a dodge when the new name is a
     * lie.
     */
    @Test
    fun `Contract files must not use UiModel types`() {
        scope.files
            .withNameEndingWith("Contract")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val declaredTypes =
                    file.classes(includeNested = true).flatMap { clazz ->
                        clazz.primaryConstructor?.parameters.orEmpty().map { it.type.name } +
                            clazz.properties().mapNotNull { it.type?.name }
                    } +
                        file.properties(includeNested = true).mapNotNull { it.type?.name }

                declaredTypes.none { it.contains("UiModel") }
            }
    }

    // endregion

    // region Screen file rules

    @Test
    fun `Screen files must contain exactly one public function`() {
        scope.files
            .withNameEndingWith("Screen")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val topLevelDeclarations = file.classes() + file.interfaces() + file.objects()
                val publicFunctions =
                    file.functions()
                        .filter { it.isTopLevel && it.hasPublicOrDefaultModifier }
                topLevelDeclarations.isEmpty() && publicFunctions.size == 1
            }
    }

    @Test
    fun `Screen public function must match the file name`() {
        scope.files
            .withNameEndingWith("Screen")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions()
                    .single { it.isTopLevel && it.hasPublicOrDefaultModifier }
                    .name == file.name
            }
    }

    @Test
    fun `Screen public function must be annotated with Composable`() {
        scope.files
            .withNameEndingWith("Screen")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.functions()
                    .single { it.isTopLevel && it.hasPublicOrDefaultModifier }
                    .hasAnnotationWithName("Composable")
            }
    }

    @Test
    fun `Screen public function parameters must only be Modifier, matching UiModel, or lambdas`() {
        scope.files
            .withNameEndingWith("Screen")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Screen")
                file.functions()
                    .single { it.isTopLevel && it.hasPublicOrDefaultModifier }
                    .parameters.all { param ->
                        param.type.name == "Modifier" ||
                            param.type.name == "${prefix}UiModel" ||
                            param.type.isFunctionType
                    }
            }
    }

    @Test
    fun `Screen UiModel parameter must not have a default value`() {
        scope.files
            .withNameEndingWith("Screen")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("Screen")
                val uiModelParam =
                    file.functions()
                        .single { it.isTopLevel && it.hasPublicOrDefaultModifier }
                        .parameters.firstOrNull { it.type.name == "${prefix}UiModel" }
                uiModelParam == null || !uiModelParam.hasDefaultValue()
            }
    }

    // endregion

    // region StoreFactory file rules

    @Test
    fun `StoreFactory classes must contain a create function returning the matching Store type`() {
        scope.files
            .withNameEndingWith("StoreFactory")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("StoreFactory")
                file.classes().single { it.isTopLevel }
                    .functions(includeNested = false)
                    .any { it.name == "create" && it.returnType?.name == "${prefix}Store" }
            }
    }

    @Test
    fun `StoreFactory nested ReducerImpl must be internal, not private`() {
        scope.files
            .withNameEndingWith("StoreFactory")
            .withPackage("..presentation.screen..")
            .assertTrue { file ->
                file.classes().single { it.isTopLevel }
                    .objects(includeNested = true)
                    .filter { it.name == "ReducerImpl" }
                    .all { it.hasInternalModifier }
            }
    }

    // endregion

    // region UiMapper file rules

    @Test
    fun `UiMapper files must contain only top-level extension functions`() {
        scope.files
            .withNameEndingWith("UiMapper")
            .withPackage("..presentation.screen..")
            .filterNot { it.hasPackage("..mapper") }
            .assertTrue { file ->
                file.classes(includeNested = true).isEmpty() &&
                    file.interfaces(includeNested = true).isEmpty() &&
                    file.objects(includeNested = true).isEmpty() &&
                    file.functions(includeNested = false).all { it.hasReceiverType() }
            }
    }

    /**
     * The screen's own UiMapper, which is the one that turns the whole State into the whole UiModel.
     * The type mappers in `mapper/` are a different job with a rule of their own above — they convert
     * one domain type, and their receiver is that type rather than a State.
     */
    @Test
    fun `UiMapper functions must map from the matching State to the matching UiModel`() {
        scope.files
            .withNameEndingWith("UiMapper")
            .withPackage("..presentation.screen..")
            .filterNot { it.hasPackage("..mapper") }
            .assertTrue { file ->
                val prefix = file.name.removeSuffix("UiMapper")
                file.functions(includeNested = false).all { function ->
                    function.receiverType?.name == "${prefix}State" &&
                        function.returnType?.name == "${prefix}UiModel"
                }
            }
    }

    // endregion

    // region State/UiModel boundary

    @Test
    fun `files with a Composable function must not import any State type`() {
        scope.files
            .filter { file -> file.functions().any { it.hasAnnotationWithName("Composable") } }
            .assertTrue { file ->
                file.imports.none { import ->
                    import.name.contains(".presentation.") && import.name.substringAfterLast(".").endsWith("State")
                }
            }
    }

    // endregion

    // region Dependency boundaries

    /**
     * `mapper/` joins the exemption list, and it is the only one of the four that exists *to* cross
     * the boundary. The other three touch the domain incidentally — a Store wires use cases, a
     * Contract is written in domain terms — while a presentation mapper's entire body is a domain
     * type on the left and a UiModel on the right. Concentrating the crossing in files named for it
     * is what stops it happening at the bottom of a StoreFactory.
     *
     * `core/` is excluded because its `presentation/` has no screens and therefore no
     * `screen/<name>/mapper/`; `CorePresentationTest` states the same rule in the shape that package
     * actually takes.
     */
    @Test
    fun `presentation files must not import from domain except StoreFactory, ViewModel, Contract, and mappers`() {
        scope.files
            .withPackage("..presentation..")
            .filterNot { it.hasPackage("..core..") }
            .filter { file ->
                !file.name.endsWith("StoreFactory") &&
                    !file.name.endsWith("ViewModel") &&
                    !file.name.endsWith("Contract") &&
                    !file.hasPackage("..presentation.screen..mapper")
            }
            .filter { file ->
                file.imports.any { it.name.contains(".domain.") }
            }
            .assertEmpty()
    }

    /**
     * **A screen waits as its own silhouette, not as a spinner.**
     *
     * A centred `CircularProgressIndicator` is the same picture on every screen in every app, and it
     * says only "something is happening". A shimmer skeleton says what is about to arrive, in the
     * shape it will arrive in, so the real content lands in a layout the eye has already settled on.
     * `ShimmerPulse` and `Modifier.shimmerBlock` in `design/theme/` are the tools; the geometry
     * belongs to the screen, because the geometry is the point.
     *
     * Checked on the import rather than on the call, so a screen that pulls the indicator in and
     * hides it behind a branch is caught too.
     */
    @Test
    fun `Screen files must not draw a spinner while they wait`() {
        scope.files
            .withNameEndingWith("Screen")
            .withPackage("..presentation.screen..")
            .filter { file -> file.hasImport { it.name.endsWith(".CircularProgressIndicator") } }
            .assertEmpty()
    }

    @Test
    fun `presentation layer must not import from data layer`() {
        scope.files
            .withPackage("..presentation..")
            .assertTrue { !it.hasImportWithName("..data..") }
    }

    // endregion
}
