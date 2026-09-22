plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("maven-publish")
}

android {
    namespace = "com.plural_pinelabs.expresscheckoutsdk"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "SDK_VERSION", "\"1.5.3\"")
        buildConfigField(
            "String",
            "SHA256_UAT_AMAZON_ROOT_CA_1",
            // Current pluraluat.v2.pinepg.in trust root. Pin the root SPKI so
            // ACM leaf/intermediate rotations do not break released clients.
            "\"++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI=\""
        )
        buildConfigField(
            "String",
            "SHA256_UAT_DIGICERT_GLOBAL_ROOT_G2",
            // Rotation pin supplied by the cloud team. This is the SPKI of
            // DigiCert Global Root G2 (the key is identical in its self-signed
            // and cross-signed certificate forms).
            "\"i7WTqTvh0OioIruIfFR4kMPnBqrS2rdiVPl/s2uC/CY=\""
        )
        buildConfigField(
            "String",
            "SHA256_UAT_DIGICERT_HIGH_ASSURANCE_ROOT",
            // Compatibility trust anchor for the supplied cross-signed
            // DigiCert Global Root G2 chain.
            "\"WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=\""
        )
        buildConfigField(
            "String",
            "SHA256_QA",
            "\"c2hhMjU2LzVwNjZBekxRU0kzdjdUd2RBeGVuQUswY0dU\""
        )
        buildConfigField(
            "String",
            "SHA256_PROD_AMAZON_ROOT_CA_1",
            // Long-lived production trust root. This survives leaf renewals
            // and switches between Amazon RSA intermediates such as M01/M04.
            "\"++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI=\""
        )
        buildConfigField(
            "String",
            "SHA256_PROD_STARFIELD_ROOT_CA_G2",
            // Compatibility root for Amazon Root CA 1's supplied cross-signed
            // trust path. This is a root SPKI, not an end-entity certificate.
            "\"KwccWaCgrnaw6tsrrSO61FgLacNgG2MMLq8GE6+oP5I=\""
        )
        buildConfigField(
            "boolean",
            "ENABLE_UPI_ICB_SAVINGS_UI",
            "false"
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


    // Keep Fragment and Navigation on mutually compatible AndroidX versions.
    implementation(libs.fragment.ktx)

    // -------------------------------------------------
    // Lifecycle (KTX REQUIRED for your code)
    // -------------------------------------------------
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // -------------------------------------------------
    // Navigation
    // -------------------------------------------------
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

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
            artifactId = "Pinelabs-Android-SDK"
            version = "1.5.3"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
