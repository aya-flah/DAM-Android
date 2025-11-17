# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# WebView JavaScript Interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep WebView related classes
-keepclassmembers class com.pianokids.game.utils.components.AvatarCreationDialog* {
    public *;
}

# Keep JavaScript interface methods
-keepclassmembers class com.pianokids.game.utils.components.AvatarCreationDialog$*$* {
    public *;
}

# WebView
-keep class android.webkit.** { *; }
-dontwarn android.webkit.**

# Keep source file and line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep annotation attributes
-keepattributes *Annotation*

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile