# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

# Keep data models used with Firestore reflection-based (de)serialization
-keep class co.edu.eafit.appeafit.domain.model.** { *; }
-keepclassmembers class co.edu.eafit.appeafit.domain.model.** {
    <init>();
}
