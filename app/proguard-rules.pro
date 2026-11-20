# Proguard rules for ADR BOOK
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Google Play Services Ads
-keep class com.google.android.gms.ads.** { *; }
-keep public class com.google.android.gms.ads.** {
    public *;
}
