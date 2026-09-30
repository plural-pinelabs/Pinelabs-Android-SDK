# Pine Labs Express Checkout Android SDK

Android SDK for integrating Pine Labs Express Checkout.

## Release coordinates

- Repository: JitPack
- Group: `com.github.plural-pinelabs`
- Artifact: `Pinelabs-Android-SDK`
- Version: `1.5.5`
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
    implementation("com.github.plural-pinelabs:Pinelabs-Android-SDK:1.5.5")
}
```

## Version 1.5.5

- Fixed production checkout initialization when the API returns a convenience-fee maximum amount larger than the 32-bit integer range.
- Preserved the full maximum-fee value when sending the process-payment request.
