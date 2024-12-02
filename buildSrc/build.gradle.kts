plugins {
  // See https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin
  `kotlin-dsl`
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

dependencies { compileOnly("com.android.tools.build:gradle-api:8.7.2") }
