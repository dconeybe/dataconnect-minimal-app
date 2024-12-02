package com.google.firebase.dataconnect.minimaldemo.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

@Suppress("unused")
abstract class DataConnectMinimalAppPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    val generateSourcesTask =
      project.tasks.register<DataConnectGenerateSourcesTask>("dataConnectGenerateSources") {
        group = TASK_GROUP
        description =
          "Run firebase dataconnect:sdk:generate to generate the Data Connect Kotlin SDK sources"

        inputDirectory = project.layout.projectDirectory.dir("firebase")
        outputDirectory = project.layout.projectDirectory.dir("dataConnectGeneratedSources")

        nodeExecutableDirectory =
          project.providers.gradleProperty("dataConnect.minimalApp.nodeExecutableDirectory").map {
            project.layout.projectDirectory.dir(it)
          }
        firebaseCommand = project.providers.gradleProperty("dataConnect.minimalApp.firebaseCommand")

        workDirectory = project.layout.buildDirectory.dir(name)
      }

    val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
    androidComponents.onVariants { variant ->
      val copyTaskName = "dataConnectCopy${variant.name.uppercaseFirstChar()}GenerateSources"
      val copyTask =
        project.tasks.register<CopyDirectoryTask>(copyTaskName) {
          group = TASK_GROUP
          description =
            "Copy the generated Data Connect Kotlin SDK sources into the " +
              "generated code directory for the \"${variant.name}\" variant."
          srcDirectory = generateSourcesTask.flatMap { it.outputDirectory }
        }

      variant.sources.java!!.addGeneratedSourceDirectory(copyTask, CopyDirectoryTask::destDirectory)
    }
  }

  companion object {
    private const val TASK_GROUP = "Firebase Data Connect Minimal App"
  }
}
