package com.google.firebase.dataconnect.minimaldemo.gradle

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

@Suppress("unused")
abstract class DataConnectGenerateSourcesTask : DefaultTask() {

  @get:InputDirectory abstract val inputDirectory: DirectoryProperty

  @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

  @get:Input @get:Optional abstract val nodeExecutable: Property<File>

  @get:Internal abstract val workDirectory: DirectoryProperty

  @get:Input abstract val firebaseCommand: Property<String>

  @get:Inject protected abstract val execOperations: ExecOperations

  @get:Inject protected abstract val providerFactory: ProviderFactory

  @get:Inject protected abstract val fileSystemOperations: FileSystemOperations

  init {
    description =
      "Run firebase dataconnect:sdk:generate to generate the Data Connect Kotlin SDK sources"
  }

  @TaskAction
  fun run() {
    val inputDirectory: File = inputDirectory.get().asFile
    val nodeExecutable: File? = nodeExecutable.orNull
    val outputDirectory: File = outputDirectory.get().asFile
    val workDirectory: File = workDirectory.get().asFile
    val firebaseCommand: String = firebaseCommand.get()

    logger.info("inputDirectory: {}", inputDirectory.absolutePath)
    logger.info("nodeExecutable: {}", nodeExecutable?.absolutePath)
    logger.info("outputDirectory: {}", outputDirectory.absolutePath)
    logger.info("workDirectory: {}", workDirectory.absolutePath)
    logger.info("firebaseCommand: {}", firebaseCommand)

    deleteDirectory(outputDirectory)
    createDirectory(outputDirectory)
    deleteDirectory(workDirectory)
    createDirectory(workDirectory)

    fileSystemOperations.copy {
      from(inputDirectory)
      into(workDirectory)
    }

    execOperations.exec {
      if (nodeExecutable !== null) {
        val nodeExecutableDir = nodeExecutable.absoluteFile.parentFile.absolutePath
        val oldPath = providerFactory.environmentVariable("PATH").orNull
        environment(
          "PATH",
          if (oldPath === null) {
            nodeExecutableDir
          } else {
            nodeExecutableDir + File.pathSeparator + oldPath
          },
        )
      }

      commandLine(firebaseCommand, "--debug", "dataconnect:sdk:generate")
      workingDir(workDirectory)
      setIgnoreExitValue(false)
    }
  }
}
