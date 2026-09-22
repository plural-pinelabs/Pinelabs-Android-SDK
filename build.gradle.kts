
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
}

/**
 * Required for JitPack.
 * JitPack runs `./gradlew publishToMavenLocal` at the ROOT project level.
 * This forwards that task to the Android library module.
 */
tasks.register("publishToMavenLocal") {
    dependsOn(":expresscheckoutsdk:publishToMavenLocal")
}
