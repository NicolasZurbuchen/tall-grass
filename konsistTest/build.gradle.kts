plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()

    // Konsist builds its scopes from strings at runtime, so Gradle never sees `:shared`'s sources as
    // inputs here and marks this UP-TO-DATE — replaying a pass over code it has not read.
    // DECISIONS.md § The Konsist task always runs.
    outputs.upToDateWhen { false }
}
