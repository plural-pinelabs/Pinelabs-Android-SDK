# =========================================================
# Express Checkout SDK – Library ProGuard
# Applied ONLY while building the AAR
# =========================================================

# --- Debugging support ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlin metadata ---
-keepattributes KotlinMetadata

# --- Retrofit reflection support ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Retrofit interfaces
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# Kotlin coroutines continuation
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Retrofit response
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# --- Gson internals ---
-dontwarn sun.misc.**

-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# --- SDK public entry points ---
-keep class com.plural_pinelabs.expresscheckoutsdk.ExpressSDKCallback {*;}
-keep class com.plural_pinelabs.expresscheckoutsdk.ExpressSDKInitializer {*;}
-keep class com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject {*;}

# --- SDK data models ---
-keep class com.plural_pinelabs.expresscheckoutsdk.data.** { *; }

# --- Enums ---
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Lifecycle observers ---
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}

# --- AppCompat ---
-keep class * extends androidx.appcompat.app.AppCompatActivity

# --- Remove logs ---
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
