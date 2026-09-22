# Express Checkout SDK consumer rules.
#
# These rules are packaged in the AAR and are applied automatically by R8 in
# the merchant application. Keep this file self-contained: rules from
# proguard-rules.pro are only used while building the AAR and do not propagate
# to applications consuming it.

# Retrofit reads method annotations and generic signatures at runtime. Gson's
# TypeToken also needs generic signatures, while Kotlin anonymous/nested types
# need their enclosing-method metadata.
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# Retrofit service methods are invoked through dynamic proxies. R8 full mode
# must retain their annotations and the generic type carried by the final
# Continuation parameter of Kotlin suspend methods. Without the Continuation
# rule Retrofit sees a raw Class instead of ParameterizedType at runtime.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

-keep,allowoptimization,allowshrinking,allowobfuscation class kotlin.coroutines.Continuation
-keep,allowoptimization,allowshrinking,allowobfuscation class retrofit2.Response

# Gson discovers TypeToken's generic parameter from the anonymous subclass at
# runtime. This conditional rule is required for any generic TypeToken still
# used by SDK payment-mode deserializers in an R8 full-mode merchant build.
-if class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowoptimization class <1>

# The SDK currently uses reflection-backed navigation, Gson field binding,
# Parcelable models, and JavaScript bridges across its internal packages. Keep
# the complete SDK surface until each reflection boundary has a narrower rule.
# This is intentionally conservative for merchant release builds.
-keep class com.plural_pinelabs.expresscheckoutsdk.** { *; }

# Document the two most important reflection entry points explicitly. These
# are redundant with the conservative rule above, but prevent regressions when
# that rule is narrowed in the future.
-keep class com.plural_pinelabs.expresscheckoutsdk.presentation.** extends androidx.fragment.app.Fragment {
    public <init>();
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
