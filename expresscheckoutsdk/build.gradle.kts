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
        buildConfigField("String", "SHA256_QA", "\"c2hhMjU2LzVwNjZBekxRU0kzdjdUd2RBeGVuQUswY0dU\"")
        buildConfigField(
            "String",
            "SHA256_PROD",
            //"\"c2hhMjU2L2dmVUJRQzB1WWNmQ2k3d21CdWllcnZjNWlNWGZnSXE3U2JQcVNyeU1LZDA9\""
            "\"D3pseS7ojH9IDxqT4rUEuAt5/IykaPmeaiNHhJabd3c=\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
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
    //coroutines
    implementation(libs.kotlinx.coroutines.core) // Replace with the latest version
    implementation(libs.kotlinx.coroutines.android) // Replace with the latest version
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.coil)
    implementation(libs.coil.svg)
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.plural-pinelabs"
            artifactId = "express-checkout-sdk"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
