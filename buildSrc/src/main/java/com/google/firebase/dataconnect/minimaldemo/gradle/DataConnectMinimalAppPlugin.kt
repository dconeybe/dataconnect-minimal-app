package com.google.firebase.dataconnect.minimaldemo.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.google.firebase.dataconnect.minimaldemo.gradle.DataConnectMinimalAppPlugin.Companion.TASK_GROUP
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

@Suppress("unused")
abstract class DataConnectMinimalAppPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    DataConnectMinimalAppPluginApplier(
        taskGroup = TASK_GROUP,
        tasks = project.tasks,
        androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>(),
        projectDirectory = project.layout.projectDirectory,
        providerFactory = project.providers,
        layout = project.layout,
      )
      .apply()
  }

  companion object {
    private const val TASK_GROUP = "Firebase Data Connect Minimal App"
  }
}

private class DataConnectMinimalAppPluginApplier(
  val taskGroup: String,
  val tasks: TaskContainer,
  val androidComponents: ApplicationAndroidComponentsExtension,
  val projectDirectory: Directory,
  val providerFactory: ProviderFactory,
  val layout: ProjectLayout,
)

private fun DataConnectMinimalAppPluginApplier.apply() {
  val generateSourcesTask =
    tasks.register<DataConnectGenerateSourcesTask>("dataConnectGenerateSources") {
      group = taskGroup
      description =
        "Run firebase dataconnect:sdk:generate to generate the Data Connect Kotlin SDK sources"

      inputDirectory = projectDirectory.dir("firebase")
      outputDirectory = projectDirectory.dir("dataConnectGeneratedSources")

      nodeExecutableDirectory =
        providerFactory.gradleProperty("dataConnect.minimalApp.nodeExecutableDirectory").map {
          projectDirectory.dir(it)
        }
      firebaseCommand = providerFactory.gradleProperty("dataConnect.minimalApp.firebaseCommand")

      workDirectory = layout.buildDirectory.dir(name)
    }

  androidComponents.onVariants { variant ->
    val copyTaskName = "dataConnectCopy${variant.name.uppercaseFirstChar()}GenerateSources"
    val copyTask =
      tasks.register<CopyDirectoryTask>(copyTaskName) {
        group = taskGroup
        description =
          "Copy the generated Data Connect Kotlin SDK sources into the " +
            "generated code directory for the \"${variant.name}\" variant."
        srcDirectory = generateSourcesTask.flatMap { it.outputDirectory }
      }

    variant.sources.java!!.addGeneratedSourceDirectory(copyTask, CopyDirectoryTask::destDirectory)
  }
}
