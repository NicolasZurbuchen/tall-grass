package io.nicolaszurbuchen.tallgrass

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class DomainLayerTest {
    companion object {
        private val scope = Konsist.scopeFromProduction(moduleName = "shared")
    }

    // region file location implies name

    @Test
    fun `files in domain usecase package must be suffixed with UseCase`() {
        scope.files
            .withPackage("..domain.usecase")
            .assertTrue { it.name.endsWith("UseCase") }
    }

    @Test
    fun `files in domain repository package must be suffixed with Repository`() {
        scope.files
            .withPackage("..domain.repository")
            .assertTrue { it.name.endsWith("Repository") }
    }

    // endregion

    // region name implies location

    @Test
    fun `files suffixed with UseCase must reside in domain usecase package`() {
        scope.files
            .withNameEndingWith("UseCase")
            .assertTrue { it.hasPackage("..domain.usecase") }
    }

    @Test
    fun `files suffixed with Repository must reside in domain repository package`() {
        scope.files
            .withNameEndingWith("Repository")
            .assertTrue { it.hasPackage("..domain.repository") }
    }

    // endregion

    // region type enforcement

    private fun KoClassDeclaration.isPlainClass() =
        !hasDataModifier &&
            !hasSealedModifier &&
            !hasAbstractModifier &&
            !hasEnumModifier &&
            !hasValueModifier &&
            hasPublicOrDefaultModifier

    private fun KoInterfaceDeclaration.isPlainInterface() = !hasSealedModifier && !hasFunModifier && hasPublicOrDefaultModifier

    @Test
    fun `declarations suffixed with UseCase must not be interfaces`() {
        scope.interfaces()
            .withNameEndingWith("UseCase")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with UseCase must not be objects`() {
        scope.objects()
            .withNameEndingWith("UseCase")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with UseCase must be plain classes`() {
        scope.classes()
            .withNameEndingWith("UseCase")
            .assertTrue { it.isPlainClass() }
    }

    @Test
    fun `declarations suffixed with Repository must not be classes`() {
        scope.classes()
            .withNameEndingWith("Repository")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with Repository must not be objects`() {
        scope.objects()
            .withNameEndingWith("Repository")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with Repository must be plain interfaces`() {
        scope.interfaces()
            .withNameEndingWith("Repository")
            .assertTrue { it.isPlainInterface() }
    }

    @Test
    fun `declarations in domain model package must not be interfaces`() {
        scope.interfaces()
            .withPackage("..domain.model")
            .assertEmpty()
    }

    @Test
    fun `declarations in domain model package must be data, sealed, or enum classes`() {
        scope.classes(includeNested = false)
            .withPackage("..domain.model")
            .assertTrue { it.hasDataModifier || it.hasSealedModifier || it.hasEnumModifier }
    }

    // endregion

    // region top-level structure

    @Test
    fun `top-level declaration name must match file name`() {
        scope.files
            .withPackage("..domain..")
            .assertTrue { file ->
                (
                    file.classes(includeNested = false) +
                        file.interfaces(includeNested = false) +
                        file.objects(includeNested = false)
                )
                    .any { it.name == file.name }
            }
    }

    @Test
    fun `files in domain layer must contain exactly one top-level declaration`() {
        scope.files
            .withPackage("..domain..")
            .assertTrue { file ->
                val topLevelDeclarations =
                    file.classes(includeNested = false) +
                        file.interfaces(includeNested = false) +
                        file.objects(includeNested = false)
                topLevelDeclarations.size == 1
            }
    }

    // endregion

    // region usecase rules

    @Test
    fun `UseCase classes must declare exactly one public function named invoke`() {
        scope.classes()
            .withNameEndingWith("UseCase")
            .assertTrue { clazz ->
                val publicFunctions =
                    clazz.functions(includeNested = false)
                        .filter { it.hasPublicOrDefaultModifier }
                publicFunctions.size == 1 && publicFunctions.single().name == "invoke"
            }
    }

    @Test
    fun `UseCase classes must not inject other UseCases`() {
        scope.classes()
            .withNameEndingWith("UseCase")
            .assertTrue { clazz ->
                clazz.constructors.all { constructor ->
                    constructor.parameters.none { it.type.name.endsWith("UseCase") }
                }
            }
    }

    // endregion

    // region repository rules

    @Test
    fun `Repository interfaces must not have default function implementations`() {
        scope.interfaces()
            .withNameEndingWith("Repository")
            .assertTrue { iface ->
                iface.functions(includeNested = false)
                    .none { it.hasExpressionBody || it.hasBlockBody }
            }
    }

    // endregion

    // region dependency boundaries

    @Test
    fun `project types injected into domain layer classes must respect feature boundaries`() {
        val projectPackagePrefix = "io.nicolaszurbuchen.tallgrass"

        scope.classes()
            .withPackage("..domain..")
            .assertTrue { clazz ->
                val packageName = clazz.packagee?.name ?: ""
                val currentFeature =
                    if (packageName.contains(".feature.")) {
                        packageName.substringAfter(".feature.").substringBefore(".")
                    } else {
                        null
                    }

                clazz.constructors.all { constructor ->
                    constructor.parameters.all { param ->
                        val typeName = param.type.name
                        val matchingImport =
                            clazz.containingFile.imports.find { it.name.endsWith(".$typeName") }

                        if (matchingImport != null) {
                            val fqn = matchingImport.name
                            if (fqn.startsWith(projectPackagePrefix)) {
                                val isSameFeature =
                                    currentFeature != null && fqn.contains(".feature.$currentFeature.")
                                val isCore = fqn.contains(".core.")
                                val isInfra = fqn.contains(".infra.")

                                isSameFeature || isCore || isInfra
                            } else {
                                true
                            }
                        } else {
                            true
                        }
                    }
                }
            }
    }

    @Test
    fun `domain layer must not import from data layer`() {
        scope.files
            .withPackage("..domain..")
            .assertTrue { !it.hasImportWithName("..data..") }
    }

    @Test
    fun `domain layer must not import from presentation layer`() {
        scope.files
            .withPackage("..domain..")
            .assertTrue { !it.hasImportWithName("..presentation..") }
    }

    // endregion
}
