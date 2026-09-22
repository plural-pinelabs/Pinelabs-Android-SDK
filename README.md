# Pine Labs Express Checkout Android SDK

Android SDK for integrating Pine Labs Express Checkout.

## Release coordinates

- Repository: JitPack
- Group: `com.github.plural-pinelabs`
- Artifact: `Pinelabs-Android-SDK`
- Version: `1.5.3`
- Minimum Android SDK: 26

Add JitPack to the merchant application's repositories:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Add the SDK dependency:

```kotlin
dependencies {
    implementation("com.github.plural-pinelabs:Pinelabs-Android-SDK:1.5.3")
}
```

## Version 1.5.3

- Uses long-lived root SPKI pins for the UAT and production endpoints.
- Removes the Last9/OpenTelemetry dependency.
- Includes release/R8 consumer rules for Retrofit, Gson, and Kotlin coroutines.
- Improves UPI return handling through inquiry-driven terminal-state resolution.
- Returns categorized initialization errors for TLS, connectivity, timeout, and HTTP failures.
