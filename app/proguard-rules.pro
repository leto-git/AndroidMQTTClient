# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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

# Rules for R8 to not alter external libraries
# Do not alter Eclipse Paho MQTT Client
-keep class org.eclipse.paho.client.mqttv3.** { *; }
-keep class org.eclipse.paho.mqttv5.** { *; }
-keep interface org.eclipse.paho.client.mqttv3.** { *; }
-keep interface org.eclipse.paho.mqttv5.** { *; }

# Dont warn about missing optional dependencies form paho
-dontwarn org.eclipse.paho.client.mqttv3.**
-dontwarn org.eclipse.paho.mqttv5.**

# Do not alter SQLCipher
-keep class net.zetetic.database.sqlcipher.** { *; }
-keep class net.zetetic.database.** { *; }