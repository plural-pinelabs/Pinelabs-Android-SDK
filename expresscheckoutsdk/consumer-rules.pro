# =========================================================
# Express Checkout SDK – Consumer ProGuard Rules
# Applied in MERCHANT app build
# =========================================================


# ---------------------------------------------------------
# 1. SDK Public API (ENTRY POINTS)
# ---------------------------------------------------------
# Prevent obfuscation / removal of SDK-facing classes
-keep class com.plural_pinelabs.expresscheckoutsdk.** { *; }


# ---------------------------------------------------------
# 2. Fragments (Navigation + reflection instantiation)
# ---------------------------------------------------------
# Fragments are instantiated from Navigation XML via reflection
-keep class com.plural_pinelabs.expresscheckoutsdk.presentation.**
    extends androidx.fragment.app.Fragment {
    public <init>();
}


# ---------------------------------------------------------
# 3


-keep class com.plural_pinelabs.expresscheckoutsdk.**ViewModelFactory { *; }

