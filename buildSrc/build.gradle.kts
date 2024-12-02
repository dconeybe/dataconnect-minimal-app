plugins {
  // See https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin
  `kotlin-dsl`
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

dependencies { implementation("com.android.tools.build:gradle-api:8.7.3") }

gradlePlugin {
  plugins {
    register("dataConnectPlugin") {
      id = "DataConnectMinimalAppPlugin"
      implementationClass =
        "com.google.firebase.dataconnect.minimaldemo.gradle.DataConnectMinimalAppPlugin"
    }
  }
}
