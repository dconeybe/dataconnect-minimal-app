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

  // The following code in this "plugins" block can be omitted from customer
  // facing documentation.
  id("com.diffplug.spotless") version "7.0.0.BETA4"
}

dependencies {
  // Use whichever versions of these dependencies suit your application.
  // The versions shown here were the latest versions as of the time of writing
  // (November 29, 2024).
  implementation("com.google.firebase:firebase-dataconnect:16.0.0-beta03")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.3")
}

// The remaining code in this file can be omitted from customer facing
// documentation. It's here just to make things compile and/or configure
// optional components of the build (e.g. spotless code formatting).

android {
  namespace = "com.google.firebase.dataconnect.minimaldemo"
  compileSdk = 35
  defaultConfig {
    minSdk = 21
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions { jvmTarget = "11" }
}

spotless {
  val ktfmtVersion = "0.43"
  kotlin {
    target("src/**/*.kt")
    ktfmt(ktfmtVersion).googleStyle()
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    ktfmt(ktfmtVersion).googleStyle()
  }
}

dependencies {
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test:runner:1.6.2")
}
