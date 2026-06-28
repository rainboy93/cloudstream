# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Suppress R8 warnings for optional classes absent on Android (jsoup re2j, rhino
# java.beans / javax.script). They are referenced reflectively but unused at runtime.
-dontwarn com.google.re2j.Matcher
-dontwarn com.google.re2j.Pattern
-dontwarn java.beans.BeanDescriptor
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor
-dontwarn javax.script.ScriptEngineFactory

# --- Built-in plugins / providers ---
# Providers are loaded reflectively and their nested data classes are mapped by
# Jackson via Kotlin metadata. R8 must not rename/strip them or JSON parsing
# (tryParseJson) returns null at runtime (e.g. empty home pages).
-keep class com.lagradost.cloudstream3.plugins.** { *; }
-keep class * extends com.lagradost.cloudstream3.MainAPI { *; }
-keep class * extends com.lagradost.cloudstream3.utils.ExtractorApi { *; }

# --- Jackson + Kotlin reflection (JSON de/serialization) ---
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
# Keep Kotlin metadata so Jackson's KotlinModule can read constructor params.
-keep class kotlin.Metadata { *; }
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.* <fields>;
    @com.fasterxml.jackson.annotation.* <methods>;
}
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**
