# Keep kotlinx.serialization generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.qaspro.worldexplorer.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
