# Keep Kotlin metadata and Serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Gson/Moshi/Firebase/Supabase data models
-keep class com.example.mawa.data.** { *; }
-keep class com.example.mawa.util.** { *; }
-keep class com.example.SupabaseClientConfig { *; }

# Keep Gson TypeToken generic signatures
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep Room entities & DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Ignore benign warnings from optional transitive dependencies
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
