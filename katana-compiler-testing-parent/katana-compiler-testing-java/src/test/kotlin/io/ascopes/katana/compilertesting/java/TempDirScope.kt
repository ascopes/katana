package io.ascopes.katana.compilertesting.java

import java.io.IOException
import java.net.URLClassLoader
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.UUID
import kotlin.io.path.deleteExisting
import kotlin.io.path.isRegularFile

/**
 * Scope to create a temporary directory in that will delete the directory once the
 * given logic completes.
 */
fun <T> tempDir(prefix: String = UUID.randomUUID().toString(), logic: (Path) -> T): T {
  val tempDir = Files.createTempDirectory(prefix)

  try {
    return logic(tempDir)
  } finally {
    Files.walkFileTree(tempDir, emptySet(), Int.MAX_VALUE, object : SimpleFileVisitor<Path>() {
      override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        if (file.isRegularFile()) {
          file.deleteExisting()
        }
        return FileVisitResult.CONTINUE
      }

      override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
        if (exc != null) {
          throw exc
        }
        dir.deleteExisting()

        return FileVisitResult.CONTINUE
      }
    })
  }
}

/**
 * Create a temporary directory and make it into a class path for the given
 * class loader.
 */
fun <T> tempClassPath(
    prefix: String = UUID.randomUUID().toString(),
    logic: (Path, URLClassLoader) -> T
) = tempDir(prefix) {
  val loader = URLClassLoader(arrayOf(it.toUri().toURL()), ClassLoader.getSystemClassLoader())
  return@tempDir logic(it, loader)
}