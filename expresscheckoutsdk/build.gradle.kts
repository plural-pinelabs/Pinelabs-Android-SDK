
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
   id("maven-publish")
}

android {
    namespace = "com.plural_pinelabs.expresscheckoutsdk"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField(
            "String",
            "SHA256_UAT",
            "\"c2hhMjU2LzRnZU5TQkpuem9BYVc2K3puR2x3YmhYZWdSS1Q0c0s2bEdUZ0w2YmVZQmM9\""
        )
        buildConfigField(
            "String",
            "SHA256_QA",
            "\"c2hhMjU2LzVwNjZBekxRU0kzdjdUd2RBeGVuQUswY0dU\""
        )
        buildConfigField(
            "String",
            "SHA256_PROD",
            "\"D3pseS7ojH9IDxqT4rUEuAt5/IykaPmeaiNHhJabd3c=\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.auth.api.phone)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.lottie)
    implementation(libs.shimmer)

    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.extension)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.retrofit.logging.interceptor)

    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.clevertap)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.coil)
    implementation(libs.coil.svg)
}

// ---- Publishing ----
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = (project.findProperty("group") ?: "com.github.plural-pinelabs").toString()
                artifactId = "plural-sdk"
                version = (project.findProperty("version") ?: "10").toString()
            }
        }
    }
}
