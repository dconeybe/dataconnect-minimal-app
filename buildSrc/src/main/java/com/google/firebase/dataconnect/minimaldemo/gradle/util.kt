package com.google.firebase.dataconnect.minimaldemo.gradle

import java.io.File
import org.gradle.api.Task

internal fun Task.createDirectory(directory: File) {
  logger.info("Creating directory: {}", directory.absolutePath)
  if (!directory.mkdirs()) {
    throw CreateDirectoryFailed("unable to create directory: ${directory.absolutePath}")
  }
}

internal fun Task.deleteDirectory(directory: File) {
  logger.info("Deleting directory: {}", directory.absolutePath)
  if (!directory.deleteRecursively()) {
    throw DeleteDirectoryFailed("unable to delete directory: ${directory.absolutePath}")
  }
}

private class CreateDirectoryFailed(message: String) : Exception(message)

private class DeleteDirectoryFailed(message: String) : Exception(message)
