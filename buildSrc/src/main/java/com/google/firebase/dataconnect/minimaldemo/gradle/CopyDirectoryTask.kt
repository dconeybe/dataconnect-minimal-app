package com.google.firebase.dataconnect.minimaldemo.gradle

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@Suppress("unused")
abstract class CopyDirectoryTask : DefaultTask() {

  @get:InputDirectory abstract val srcDirectory: DirectoryProperty

  @get:OutputDirectory abstract val destDirectory: DirectoryProperty

  @get:Inject protected abstract val fileSystemOperations: FileSystemOperations

  @TaskAction
  fun run() {
    val srcDirectory: File = srcDirectory.get().asFile
    val destDirectory: File = destDirectory.get().asFile

    logger.info("srcDirectory: {}", srcDirectory.absolutePath)
    logger.info("destDirectory: {}", destDirectory.absolutePath)

    deleteDirectory(destDirectory)
    createDirectory(destDirectory)

    fileSystemOperations.copy {
      from(srcDirectory)
      into(destDirectory)
    }
  }
}
