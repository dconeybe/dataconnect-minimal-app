import com.google.firebase.dataconnect.minimaldemo.gradle.CopyDirectoryTask
import com.google.firebase.dataconnect.minimaldemo.gradle.DataConnectGenerateSourcesTask
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

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
  kotlinOptions.jvmTarget = "11"
}

spotless {
  val ktfmtVersion = "0.53"
  kotlin {
    target("**/*.kt")
    targetExclude("build/")
    ktfmt(ktfmtVersion).googleStyle()
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    targetExclude("build/")
    ktfmt(ktfmtVersion).googleStyle()
  }
  json {
    target("**/*.json")
    targetExclude("build/")
    simple().indentWithSpaces(2)
  }
  yaml {
    target("**/*.yaml")
    targetExclude("build/")
    jackson()
      .yamlFeature("INDENT_ARRAYS", true)
      .yamlFeature("MINIMIZE_QUOTES", true)
      .yamlFeature("WRITE_DOC_START_MARKER", false)
  }
}

dependencies {
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test:runner:1.6.2")
}

val dataConnectTaskGroupName = "Firebase Data Connect"

val dataConnectGenerateSourcesTask =
  tasks.register<DataConnectGenerateSourcesTask>("dataConnectGenerateSources") {
    group = dataConnectTaskGroupName
    inputDirectory = file("firebase")
    outputDirectory = file("dataConnectGeneratedSources")
    nodeExecutable = providers.gradleProperty("dataConnect.nodeExecutable").map { file(it) }
    workDirectory = layout.buildDirectory.dir(name)
    firebaseCommand = providers.gradleProperty("dataConnect.firebaseExecutable").orElse("firebase")
  }

androidComponents.onVariants { variant ->
  val copyTaskName = "dataConnectCopy${variant.name.uppercaseFirstChar()}GenerateSources"
  val copyTask =
    tasks.register<CopyDirectoryTask>(copyTaskName) {
      group = dataConnectTaskGroupName
      description =
        "Copy the generated Data Connect Kotlin SDK sources into the " +
          "generated code directory for the \"${variant.name}\" variant."
      srcDirectory = dataConnectGenerateSourcesTask.flatMap { it.outputDirectory }
    }

  variant.sources.java!!.addGeneratedSourceDirectory(copyTask, CopyDirectoryTask::destDirectory)
}
