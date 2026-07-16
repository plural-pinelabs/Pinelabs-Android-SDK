# Express Checkout SDK

JitPack publishes this repository from the `ExpressCheckoutSdk` Android library module.

## Release Coordinates

- Group: `com.github.plural-pinelabs`
- Artifact: `express-checkout-sdk`
- Version: `v1.2.0`

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
    implementation("com.github.plural-pinelabs:express-checkout-sdk:v1.2.0")
}
```

## Changelog Version -1.2.0
- Added Brand Wallet as payment mode
- Updated Compile SDK and Target SDK to API 36.
- Added Android 16 KB page size compatibility.
- Improved SDK stability and performance.