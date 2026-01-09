# =========================================================
# Express Checkout SDK – Consumer Rules
# Applied in MERCHANT app build
# =========================================================

# --- Keep SDK public API ---
-keep class com.plural_pinelabs.expresscheckoutsdk.** { *; }

# --- Fragments (Navigation + FragmentContainerView) ---
-keep class com.plural_pinelabs.expresscheckoutsdk.presentation.** extends androidx.fragment.app.Fragment {
    public <init>();
}

# --- ViewModels ---
-keep class * extends androidx.lifecycle.ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# --- Lifecycle (used by Fragments / repeatOnLifecycle) ---
-keep class androidx.lifecycle.** { *; }

# --- Navigation Component ---
-keep class androidx.navigation.** { *; }

# --- Fragment restore / state ---
-keep class androidx.fragment.app.FragmentContainerView { *; }
-keep class androidx.savedstate.** { *; }

# --- Gson models (used at runtime by merchant) ---
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Material Components (BottomSheetBehavior reflection) ---
-keep class com.google.android.material.** { *; }


# =========================================================
# ViewModel Factory / Provider (CRITICAL)
# =========================================================

# Prevent R8 from breaking ViewModelProvider.Factory
-keep class * implements androidx.lifecycle.ViewModelProvider$Factory { *; }

# Keep default AndroidX factory
-keep class androidx.lifecycle.ViewModelProvider$NewInstanceFactory { *; }
-keep class androidx.lifecycle.ViewModelProvider$AndroidViewModelFactory { *; }

# Keep generic signatures used in ViewModel creation
-keepattributes Signature


-keep class com.plural_pinelabs.expresscheckoutsdk.**Factory { *; }

