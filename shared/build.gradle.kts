import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.sqldelight)
}

ktlint {
    version.set(libs.versions.ktlint.engine.get())

    android.set(false)
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    ignoreFailures.set(false)

    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.JSON)
    }

    dependencies {
        add("ktlintRuleset", libs.ktlint.compose.rules)
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    android {
       namespace = "io.nicolaszurbuchen.tallgrass.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_17
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.android.driver)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.okhttp)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.sqldelight.driver.jdbc.sqlite)
        }
        commonMain.dependencies {
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.bundles.ktor.common)
            implementation(libs.bundles.compose.common)
            implementation(libs.bundles.mvikotlin.common)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.navigation3.ui)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.turbine)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.native.driver)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

sqldelight {
    databases {
        // The user-owned database. Migrated, never regenerated.
        create("AppDatabase") {
            packageName.set("io.nicolaszurbuchen.tallgrass.cache")
            srcDirs.setFrom("src/commonMain/sqldelight")

            // Replays the migrations against the committed snapshot, which is the only thing that
            // catches a table added with no .sqm. Regenerate it with
            // :shared:generateCommonMainAppDatabaseSchema whenever a .sq file changes.
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
        }

        // The generated dataset. Read-only to the app and replaced whole-file, so it has no
        // migrations and wants none: SQLDelight takes the schema version from migration files, and
        // there is no older copy of this database to upgrade -- a new build ships a new file.
        //
        // `tools/datagen` points its own SQLDelight at these same .sq files, which is what makes the
        // shipped database unable to drift from the queries that read it.
        create("PokedexDatabase") {
            packageName.set("io.nicolaszurbuchen.tallgrass.pokedex")
            srcDirs.setFrom("src/commonMain/sqldelightPokedex")
            verifyMigrations.set(false)
        }
    }
}

// The generated dataset is built, never committed, so it has to exist before Android packages its
// assets -- otherwise a clean checkout assembles an app with no Pokedex in it.
//
// Matched by name rather than by type: the asset tasks are created by the Android plugin during its
// own configuration, so there is no typed handle to name here, and `main` is not the only variant
// that needs the file -- the host tests read it too.
tasks.matching { it.name.endsWith("Assets") }.configureEach {
    dependsOn(":tools:datagen:buildPokedexDatabase")
}
