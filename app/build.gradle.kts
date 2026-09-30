plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.plural_pinelabs.native_express_sdk"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.plural_pinelabs.native_express_sdk"
        minSdk = 26
        targetSdk = 36
        versionCode = 21
        versionName = "1.5.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Exercise the same R8/resource shrinking path used by merchant
            // release builds. This sample is the release-consumer smoke test.
            // Debug signing is used only so this local release smoke test can
            // be installed directly on a connected device.
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(project(":expresscheckoutsdk"))
    //implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
   // implementation("com.google.firebase:firebase-analytics")
   // implementation("com.google.firebase:firebase-crashlytics")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")


}
