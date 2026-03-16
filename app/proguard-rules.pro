# ProGuard Rules for CodeDroid
# This file is used to optimize and obfuscate the code, making it harder for attackers
# to reverse engineer or modify the APK.

# Basic Android Rules
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Keep important Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Chaquopy (Python) Rules - VERY IMPORTANT
# Python integration relies on some reflection and specific class names
-keep class com.chaquo.python.** { *; }
-keep interface com.chaquo.python.** { *; }

# OkHttp/Retrofit/Gson Rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Apache Commons Net
-keep class org.apache.commons.net.** { *; }
-dontwarn org.apache.commons.net.**

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Project-specific Obfuscation
# DO NOT keep everything in com.example.codedroid.**
# We only keep what's absolutely necessary (like ViewModels or entry points if needed)
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Remove Log calls in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Security & Anti-Tamper Rules
# Obfuscate our security check logic heavily
-repackageclasses 'com.example.codedroid.hidden'
-allowaccessmodification
-keepattributes !*Annotation*,!Signature

# Do NOT keep the names of these classes or their members
-keepclassmembers class com.example.codedroid.util.SecurityCheck {
    <methods>;
    <fields>;
}
-keep class !com.example.codedroid.util.SecurityCheck { *; }

# Prevent reverse engineering of core logic
-optimizationpasses 10
-overloadaggressively
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify

