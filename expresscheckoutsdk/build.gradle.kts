plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("maven-publish")
}

android {
    namespace = "com.plural_pinelabs.expresscheckoutsdk"
    compileSdk = 34

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xstring-concat=inline"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {

    // -------------------------------------------------
    // Core AndroidX
    // -------------------------------------------------
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.11.0")


    // Fragment (BASE runtime — CRITICAL)
    implementation("androidx.fragment:fragment:1.3.6")

    // -------------------------------------------------
    // Lifecycle (KTX REQUIRED for your code)
    // -------------------------------------------------
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // -------------------------------------------------
    // Navigation (SAFE versions)
    // -------------------------------------------------
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    // -------------------------------------------------
    // Networking / UI
    // -------------------------------------------------
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("io.coil-kt:coil:2.4.0")
    implementation("io.coil-kt:coil-svg:2.4.0")

    // -------------------------------------------------
    // Coroutines
    // -------------------------------------------------
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // -------------------------------------------------
    // Other
    // -------------------------------------------------
    implementation(libs.clevertap)
    implementation(libs.play.services.auth.api.phone)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // -------------------------------------------------
    // Testing
    // -------------------------------------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.github.plural-pinelabs"
            artifactId = "express-checkout-sdk"
            version = "1.0.8"   // ⬅️ MUST bump version

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
