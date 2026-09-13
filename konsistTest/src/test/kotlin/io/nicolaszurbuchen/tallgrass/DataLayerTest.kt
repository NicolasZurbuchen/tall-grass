package io.nicolaszurbuchen.tallgrass

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.declaration.type.KoTypeDeclaration
import com.lemonappdev.konsist.api.ext.list.declaration.flatten
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withoutNameEndingWith
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class DataLayerTest {
    companion object {
        private val scope = Konsist.scopeFromProduction(moduleName = "shared")

        private val projectPackagePrefix =
            scope.packages
                .map { it.name }
                .reduce { acc, name ->
                    acc.commonPrefixWith(name).trimEnd('.')
                }
    }

    // region file location implies name

    @Test
    fun `files in data repository package must be suffixed with RepositoryImpl`() {
        scope.files
            .withPackage("..data.repository")
            .assertTrue { it.name.endsWith("RepositoryImpl") }
    }

    @Test
    fun `files in remote datasource package must be suffixed with RemoteDataSource or RemoteDataSourceImpl`() {
        scope.files
            .withPackage("..data.datasource.remote")
            .assertTrue { it.name.endsWith("RemoteDataSource") || it.name.endsWith("RemoteDataSourceImpl") }
    }

    @Test
    fun `files in local datasource package must be suffixed with LocalDataSource or LocalDataSourceImpl`() {
        scope.files
            .withPackage("..data.datasource.local")
            .assertTrue { it.name.endsWith("LocalDataSource") || it.name.endsWith("LocalDataSourceImpl") }
    }

    @Test
    fun `files in remote dto package must be suffixed with Dto`() {
        scope.files
            .withPackage("..data.datasource.remote.dto")
            .assertTrue { it.name.endsWith("Dto") }
    }

    @Test
    fun `classes in the remote dto package must be suffixed with Dto, nested ones included`() {
        // A nested payload is as much a wire type as the envelope around it, and the suffix is what
        // says so at the call site: `OrderDto.LineDto` cannot be mistaken for the domain's
        // `Order.Line`, which is the pair most likely to be confused while mapping between the two.
        // The file-level rule above only ever saw the top-level declaration.
        scope.classes(includeNested = true)
            .withPackage("..data.datasource.remote.dto")
            .assertTrue { it.name.endsWith("Dto") }
    }

    @Test
    fun `files in remote api package must be suffixed with Api or ApiImpl`() {
        scope.files
            .withPackage("..data.datasource.remote.api")
            .assertTrue { it.name.endsWith("Api") || it.name.endsWith("ApiImpl") }
    }

    @Test
    fun `files in remote mapper package must be suffixed with Mapper`() {
        scope.files
            .withPackage("..data.datasource.remote.mapper")
            .assertTrue { it.name.endsWith("Mapper") }
    }

    // endregion

    // region name implies location

    @Test
    fun `files suffixed with RepositoryImpl must reside in repository package`() {
        scope.files
            .withNameEndingWith("RepositoryImpl")
            .assertTrue { it.hasPackage("..data.repository") }
    }

    @Test
    fun `files suffixed with RemoteDataSource must reside in remote datasource package`() {
        scope.files
            .withNameEndingWith("RemoteDataSource")
            .assertTrue { it.hasPackage("..data.datasource.remote") }
    }

    @Test
    fun `files suffixed with RemoteDataSourceImpl must reside in remote datasource package`() {
        scope.files
            .withNameEndingWith("RemoteDataSourceImpl")
            .assertTrue { it.hasPackage("..data.datasource.remote") }
    }

    @Test
    fun `files suffixed with LocalDataSource must reside in local datasource package`() {
        scope.files
            .withNameEndingWith("LocalDataSource")
            .assertTrue { it.hasPackage("..data.datasource.local") }
    }

    @Test
    fun `files suffixed with LocalDataSourceImpl must reside in local datasource package`() {
        scope.files
            .withNameEndingWith("LocalDataSourceImpl")
            .assertTrue { it.hasPackage("..data.datasource.local") }
    }

    @Test
    fun `files suffixed with Dto must reside in remote dto package`() {
        scope.files
            .withNameEndingWith("Dto")
            .assertTrue { it.hasPackage("..data.datasource.remote.dto") }
    }

    @Test
    fun `files suffixed with Api must reside in remote api package`() {
        scope.files
            .withNameEndingWith("Api")
            .assertTrue { it.hasPackage("..data.datasource.remote.api") }
    }

    @Test
    fun `files suffixed with Mapper must reside in remote mapper package`() {
        scope.files
            .withPackage("..data..")
            .withNameEndingWith("Mapper")
            .assertTrue { it.hasPackage("..data.datasource.remote.mapper") || it.hasPackage("..data.datasource.local.mapper") }
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
    fun `declarations suffixed with RepositoryImpl must not be interfaces`() {
        scope.interfaces()
            .withNameEndingWith("RepositoryImpl")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with RepositoryImpl must not be objects`() {
        scope.objects()
            .withNameEndingWith("RepositoryImpl")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with RepositoryImpl must be plain classes`() {
        scope.classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue { it.isPlainClass() }
    }

    @Test
    fun `declarations suffixed with RemoteDataSource must not be classes`() {
        scope.classes()
            .withNameEndingWith("RemoteDataSource")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with RemoteDataSource must not be objects`() {
        scope.objects()
            .withNameEndingWith("RemoteDataSource")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with RemoteDataSource must be interfaces`() {
        scope.interfaces()
            .withNameEndingWith("RemoteDataSource")
            .assertTrue { it.isPlainInterface() }
    }

    @Test
    fun `declarations suffixed with RemoteDataSourceImpl must not be interfaces`() {
        scope.interfaces()
            .withNameEndingWith("RemoteDataSourceImpl")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with RemoteDataSourceImpl must not be objects`() {
        scope.objects()
            .withNameEndingWith("RemoteDataSourceImpl")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with RemoteDataSourceImpl must be plain classes`() {
        scope.classes()
            .withNameEndingWith("RemoteDataSourceImpl")
            .assertTrue { it.isPlainClass() }
    }

    @Test
    fun `declarations suffixed with LocalDataSource must not be classes`() {
        scope.classes()
            .withNameEndingWith("LocalDataSource")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with LocalDataSource must not be objects`() {
        scope.objects()
            .withNameEndingWith("LocalDataSource")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with LocalDataSource must be plain interfaces`() {
        scope.interfaces()
            .withNameEndingWith("LocalDataSource")
            .assertTrue { it.isPlainInterface() }
    }

    @Test
    fun `declarations suffixed with LocalDataSourceImpl must not be interfaces`() {
        scope.interfaces()
            .withNameEndingWith("LocalDataSourceImpl")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with LocalDataSourceImpl must not be objects`() {
        scope.objects()
            .withNameEndingWith("LocalDataSourceImpl")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with LocalDataSourceImpl must be plain classes`() {
        scope.classes()
            .withNameEndingWith("LocalDataSourceImpl")
            .assertTrue { it.isPlainClass() }
    }

    @Test
    fun `declarations suffixed with Dto must not be interfaces`() {
        scope.interfaces()
            .withNameEndingWith("Dto")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with Dto must not be objects`() {
        scope.objects()
            .withNameEndingWith("Dto")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with Dto must be data classes`() {
        scope.classes()
            .withNameEndingWith("Dto")
            .assertTrue { it.hasDataModifier }
    }

    @Test
    fun `declarations suffixed with Api must not be classes`() {
        scope.classes()
            .withNameEndingWith("Api")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with Api must not be objects`() {
        scope.objects()
            .withNameEndingWith("Api")
            .assertEmpty()
    }

    @Test
    fun `declarations suffixed with Api must be plain interfaces`() {
        scope.interfaces()
            .withNameEndingWith("Api")
            .assertTrue { it.isPlainInterface() }
    }

    // endregion

    // region top-level structure

    @Test
    fun `top-level declaration name must match file name`() {
        scope.files
            .withPackage("..data..")
            .withoutNameEndingWith("Mapper")
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
    fun `files in data layer must contain exactly one top-level declaration`() {
        scope.files
            .withPackage("..data..")
            .withoutNameEndingWith("Mapper")
            .assertTrue { file ->
                val topLevelDeclarations =
                    file.classes(includeNested = false) +
                        file.interfaces(includeNested = false) +
                        file.objects(includeNested = false)
                topLevelDeclarations.size == 1
            }
    }

    // endregion

    // region repository rules

    @Test
    fun `DataSource interfaces must not have default function implementations`() {
        scope.interfaces()
            .withNameEndingWith("DataSource")
            .assertTrue { iface ->
                iface.functions(includeNested = false)
                    .none { it.hasExpressionBody || it.hasBlockBody }
            }
    }

    // endregion

    // region dto rules

    @Test
    fun `Dto classes must not have any function`() {
        scope.classes()
            .withNameEndingWith("Dto")
            .assertTrue { it.numFunctions(includeNested = false) == 0 }
    }

    @Test
    fun `Dto classes must not implement any interface`() {
        scope.classes()
            .withNameEndingWith("Dto")
            .assertTrue { it.parents().isEmpty() }
    }

    // endregion

    // region mapper rules

    @Test
    fun `Mapper files must not declare classes, interfaces, or objects`() {
        scope.files
            .withPackage("..data..")
            .withNameEndingWith("Mapper")
            .assertTrue { file ->
                file.classes(includeNested = true).isEmpty() &&
                    file.interfaces(includeNested = true).isEmpty() &&
                    file.objects(includeNested = true).isEmpty()
            }
    }

    @Test
    fun `Mapper files must contain only top-level extension functions`() {
        scope.files
            .withPackage("..data..")
            .withNameEndingWith("Mapper")
            .flatMap { it.functions(includeNested = false) }
            .assertTrue { it.hasReceiverType() }
    }

    @Test
    fun `Mapper must have at least one public function`() {
        scope.files
            .withPackage("..data..")
            .withNameEndingWith("Mapper")
            .assertTrue { mapper ->
                mapper.functions(includeNested = false)
                    .any { it.hasPublicOrDefaultModifier }
            }
    }

    @Test
    fun `Mapper functions must follow toX naming convention`() {
        scope.files
            .withPackage("..data..")
            .withNameEndingWith("Mapper")
            .flatMap { it.functions(includeNested = false) }
            .assertTrue { it.name.matches(Regex("to.*(Domain|Entity|Dto|Value|Enum)$")) }
    }

    @Test
    fun `Mapper functions must not map Dto to Dto`() {
        scope.files
            .withPackage("..data..")
            .withNameEndingWith("Mapper")
            .flatMap { it.functions(includeNested = false) }
            .filter { function ->
                function.parameters.any { it.type.name.endsWith("Dto") }
            }
            .assertTrue { function ->
                function.returnType?.name?.endsWith("Dto") != true
            }
    }

    @Test
    fun `Mapper functions must not map Entity to Entity`() {
        scope.files
            .withPackage("..data..")
            .withNameEndingWith("Mapper")
            .flatMap { it.functions(includeNested = false) }
            .filter { function ->
                function.parameters.any { it.type.name.endsWith("Entity") }
            }
            .assertTrue { function ->
                function.returnType?.name?.endsWith("Entity") != true
            }
    }

    // endregion

    // region implementation contracts

    @Test
    fun `classes suffixed with RepositoryImpl must implement interface from same feature domain repository`() {
        scope.classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue { impl ->
                val expectedInterfaceName = impl.name.removeSuffix("Impl")
                val expectedPackage =
                    impl.packagee?.name
                        ?.replace(".data.repository", ".domain.repository")

                impl.containingFile.hasImportWithName("$expectedPackage.$expectedInterfaceName")
            }
    }

    @Test
    fun `classes suffixed with RemoteDataSourceImpl must implement their RemoteDataSource interface`() {
        scope.classes()
            .withNameEndingWith("RemoteDataSourceImpl")
            .assertTrue { it.hasParentWithName(it.name.removeSuffix("Impl")) }
    }

    @Test
    fun `classes suffixed with LocalDataSourceImpl must implement their LocalDataSource interface`() {
        scope.classes()
            .withNameEndingWith("LocalDataSourceImpl")
            .assertTrue { it.hasParentWithName(it.name.removeSuffix("Impl")) }
    }

    // endregion

    // region dependency boundaries

    @Test
    fun `project types injected into data layer classes must respect feature boundaries`() {
        val projectPackagePrefix = "io.nicolaszurbuchen.tallgrass"
        val classesToCheck =
            scope.classes()
                .filter { it.name.endsWith("RepositoryImpl") || it.name.endsWith("DataSourceImpl") }

        classesToCheck.assertTrue { clazz ->
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
                            // It's a project type
                            val isSameFeature =
                                currentFeature != null && fqn.contains(".feature.$currentFeature.")
                            val isCore = fqn.contains(".core.")
                            val isInfra = fqn.contains(".infra.")

                            isSameFeature || isCore || isInfra
                        } else {
                            // External type (e.g., Firebase, Kotlin, Java) - Allowed
                            true
                        }
                    } else {
                        // Not imported: same package or standard type - Allowed
                        true
                    }
                }
            }
        }
    }

    private fun extractLeafTypeNames(returnType: KoTypeDeclaration?): List<String> {
        val containerTypes = setOf("Flow", "List", "Set")

        if (returnType == null) return emptyList()

        return (
            returnType.typeArguments
                ?.flatten()
                ?.map { it.name }
                ?: listOf(returnType.name)
        )
            .filterNot { it in containerTypes }
    }

    private fun isAllowedReturnType(
        function: KoFunctionDeclaration,
        allowedProjectSuffix: String,
        allowUnresolvedTypes: Boolean = false,
    ): Boolean {
        val returnType = function.returnType ?: return true // Unit/void - allowed

        if (returnType.name == "Unit") return true

        return extractLeafTypeNames(returnType).all { typeName ->
            val matchingImport =
                function.containingFile.imports
                    .find { it.name.endsWith(".$typeName") }

            when {
                matchingImport == null -> allowUnresolvedTypes // primitive or stdlib
                matchingImport.name.startsWith(projectPackagePrefix) ->
                    matchingImport.name.contains(".$allowedProjectSuffix.")
                else -> false // external SDK type - not allowed
            }
        }
    }

    @Test
    fun `RemoteDataSource functions must only return Dto or Unit`() {
        scope.interfaces()
            .withNameEndingWith("RemoteDataSource")
            .flatMap { it.functions(includeNested = false) }
            .assertTrue { isAllowedReturnType(it, allowedProjectSuffix = "dto", allowUnresolvedTypes = true) }
    }

    @Test
    fun `data layer must not import from presentation layer`() {
        scope.files
            .withPackage("..data..")
            .assertTrue { !it.hasImportWithName("..presentation..") }
    }

    // endregion

    // region public surface

    @Test
    fun `public functions in data layer implementations must be interface overrides`() {
        scope.classes()
            .filter { it.name.endsWith("RepositoryImpl") || it.name.endsWith("DataSourceImpl") }
            .assertTrue { clazz ->
                clazz.functions(includeNested = false)
                    .filter { it.hasPublicOrDefaultModifier }
                    .all { it.hasOverrideModifier }
            }
    }

    // endregion
}
