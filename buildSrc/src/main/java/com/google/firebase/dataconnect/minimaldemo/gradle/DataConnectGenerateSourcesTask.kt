package com.google.firebase.dataconnect.minimaldemo.gradle

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

@Suppress("unused")
abstract class DataConnectGenerateSourcesTask : DefaultTask() {

  @get:InputDirectory abstract val inputDirectory: DirectoryProperty

  @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

  @get:Internal abstract val nodeExecutableDirectory: DirectoryProperty

  @get:Internal abstract val firebaseCommand: Property<String>

  @get:Internal abstract val workDirectory: DirectoryProperty

  @get:Inject protected abstract val execOperations: ExecOperations

  @get:Inject protected abstract val providerFactory: ProviderFactory

  @get:Inject protected abstract val fileSystemOperations: FileSystemOperations

  @TaskAction
  fun run() {
    val inputDirectory: File = inputDirectory.get().asFile
    val outputDirectory: File = outputDirectory.get().asFile
    val nodeExecutableDirectory: File? = nodeExecutableDirectory.orNull?.asFile
    val firebaseCommand: String? = firebaseCommand.orNull
    val workDirectory: File = workDirectory.get().asFile

    logger.info("inputDirectory: {}", inputDirectory.absolutePath)
    logger.info("outputDirectory: {}", outputDirectory.absolutePath)
    logger.info("nodeExecutableDirectory: {}", nodeExecutableDirectory)
    logger.info("firebaseCommand: {}", firebaseCommand)
    logger.info("workDirectory: {}", workDirectory.absolutePath)

    deleteDirectory(outputDirectory)
    createDirectory(outputDirectory)
    deleteDirectory(workDirectory)
    createDirectory(workDirectory)

    fileSystemOperations.copy {
      from(inputDirectory)
      into(workDirectory)
    }

    val newPath: String? =
      if (nodeExecutableDirectory === null) {
        null
      } else {
        val nodeExecutableDirectoryAbsolutePath = nodeExecutableDirectory.absolutePath
        val oldPath = providerFactory.environmentVariable("PATH").orNull
        if (oldPath === null) {
          nodeExecutableDirectoryAbsolutePath
        } else {
          nodeExecutableDirectoryAbsolutePath + File.pathSeparator + oldPath
        }
      }

    execOperations.exec {
      commandLine(firebaseCommand ?: "firebase", "--debug", "dataconnect:sdk:generate")
      workingDir(workDirectory)
      isIgnoreExitValue = false
      if (newPath !== null) {
        environment("PATH", newPath)
      }
    }
  }
}
