// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.4" apply false
   // `maven-publish`
    //id("maven-publish") apply false   // good to declare it here too


}


tasks.register("publishToMavenLocal") {
    dependsOn(":ExpressCheckoutSdk:publishMavenPublicationToMavenLocal")
}