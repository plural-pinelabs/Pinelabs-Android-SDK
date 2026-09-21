# Express Checkout SDK

JitPack publishes this repository from the `expresscheckoutsdk` Android library module.

## Release Coordinates

- Group: `com.github.plural-pinelabs`
- Artifact: `Pinelabs-Android-SDK`
- Version: `1.5.2`

## Gradle

Add JitPack to your repositories:

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

Add the dependency:

```kotlin
dependencies {
    implementation("com.github.plural-pinelabs:Pinelabs-Android-SDK:1.5.2")
}
```

## Changelog Version -1.5.2
- Removed the Last9 runtime dependency so the published SDK is self-contained.
- Made UPI completion inquiry-driven: external app result codes no longer cancel polling, and terminal navigation is handled only once.
- Updated production certificate pinning for the M04/M01 intermediate certificates and the supplied next certificate backup.
- Updated Fragment and Navigation dependencies to versions compatible with current R8 release builds.
- Hardened the AAR consumer rules for Retrofit/Gson metadata, navigation fragments, and JavaScript bridges.
- Enabled R8 and resource shrinking in the sample release build as a regression check.
- Preserved initialization failure categories (offline, timeout, DNS, TLS, HTTP, and unexpected errors) instead of collapsing every pre-checkout failure into `1000`.
- Resolved UAT/production endpoints per SDK session and combined all certificate pins into one OkHttp pinner.
- Guarded terminal callbacks so a checkout session delivers at most one success, failure, or cancellation callback.
- Added Brand Wallet as payment mode
- Updated Compile SDK and Target SDK to API 36.
- Added Android 16 KB page size compatibility.
- Improved SDK stability and performance.
