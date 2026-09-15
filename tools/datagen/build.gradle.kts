plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(17)
}

// This module is a build-time tool and is never shipped. Nothing in `androidApp` or `shared`
// depends on it; the arrow points the other way, at the .sq files it borrows below.
sqldelight {
    databases {
        create("PokedexDatabase") {
            packageName.set("io.nicolaszurbuchen.tallgrass.pokedex")

            // The same schema files the app compiles against, read across the module boundary
            // rather than copied. This is the guarantee from DECISIONS.md § The generator writes
            // through the app's own schema: a column the app queries and the generator does not
            // write cannot exist, because there is only one declaration of it.
            srcDirs.setFrom(rootProject.file("shared/src/commonMain/sqldelightPokedex"))
            verifyMigrations.set(false)
        }
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.driver.jdbc.sqlite)

    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}

val datasetDir = rootProject.file("data")
val databaseFile = rootProject.file("shared/src/androidMain/assets/pokedex.db")

// Fetches the pinned upstream CSVs and rewrites `data/*.json`.
//
// Deliberately separate from buildPokedexDatabase and deliberately not wired into `build`: it
// reaches the network and rewrites files that are reviewed in a diff, so it runs when a human
// decides to bump the pinned SHA, never as a side effect of building the app.
tasks.register<JavaExec>("generateDataset") {
    group = "datagen"
    description = "Fetches the pinned PokeAPI CSVs and rewrites the committed JSON dataset."
    mainClass.set("io.nicolaszurbuchen.tallgrass.datagen.GenerateDatasetKt")
    classpath = sourceSets["main"].runtimeClasspath
    args(datasetDir.absolutePath)
}

// Builds `pokedex.db` from the committed JSON and puts it where the app bundles assets from.
//
// Declares the JSON as inputs and the database as output, so it is skipped when neither moved and
// re-runs the moment the dataset does. Runs offline -- everything it needs is in the repository.
tasks.register<JavaExec>("buildPokedexDatabase") {
    group = "datagen"
    description = "Builds pokedex.db from the committed JSON dataset."
    mainClass.set("io.nicolaszurbuchen.tallgrass.datagen.BuildDatabaseKt")
    classpath = sourceSets["main"].runtimeClasspath

    inputs.dir(datasetDir).withPropertyName("dataset")
    outputs.file(databaseFile).withPropertyName("database")

    args(datasetDir.absolutePath, databaseFile.absolutePath)
}
