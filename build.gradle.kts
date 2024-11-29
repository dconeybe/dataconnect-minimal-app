plugins {
  // Use whichever versions of these plugins suit your application.
  // The versions shown here were taken from the Android Studio application
  // template as of the time of writing (November 29, 2024).
  // Note, however, that the version of kotlin("plugin.serialization") _must_,
  // in general, match the version of kotlin("android").
  id("com.android.application") version "8.7.2"
  val kotlinVersion = "1.9.24"
  kotlin("android") version kotlinVersion
  kotlin("plugin.serialization") version kotlinVersion
}

dependencies {
  // Use whichever versions of these dependencies suit your application.
  // The versions shown here were the latest versions as of the time of writing
  // (November 29, 2024).
  implementation("com.google.firebase:firebase-dataconnect:16.0.0-beta03")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.3")
}

android {
  namespace = "com.google.firebase.example.dataconnect"
  compileSdk = 35
  defaultConfig {
      minSdk = 21
      targetSdk = 35
      versionCode = 1
      versionName = "1.0"
  }
}
