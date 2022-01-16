package io.ascopes.katana.compilertesting.java

import io.ascopes.katana.compilertesting.CommonAssertions
import javax.tools.StandardLocation
import org.opentest4j.AssertionFailedError
import org.opentest4j.MultipleFailuresError

/**
 * Assertions to apply to a [JavaRamFileManager]. This primarily only considers in-memory resources,
 * and ignores the rest of the classpath that may have been included for compilation.
 *
 * @author Ashley Scopes
 * @since 0.1.0
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class JavaRamFileManagerAssertions
  : CommonAssertions<JavaRamFileManager, JavaRamFileManagerAssertions> {

  /**
   * @param target the target of the assertions to perform.
   */
  @Suppress("ConvertSecondaryConstructorToPrimary")
  internal constructor(target: JavaRamFileManager) : super(target)

  /**
   * Assert that a given source file was generated in the non-module output sources.
   *
   * @param fileName the path of the file, relative to the output directory.
   * @return this assertion object for further checks.
   */
  fun hasSourceOutput(fileName: String) = hasSourceOutputs(fileName)

  /**
   * Assert that one or more source files were generated in the non-module output sources.
   *
   * @param fileName the first file name to check for.
   * @param moreFileNames the additional file names to check for.
   * @return this assertion object for further checks.
   */
  fun hasSourceOutputs(fileName: String, vararg moreFileNames: String) = apply {
    expectAllFilesToExist(StandardLocation.SOURCE_OUTPUT, fileName, moreFileNames)
  }

  /**
   * Assert that a given source file was generated in the modular source outputs.
   *
   * @param moduleName the name of the module.
   * @param fileName the path of the file, relative to the module's base directory.
   * @return this assertion object for further checks.
   */
  fun hasMultiModuleSourceOutput(
      moduleName: String,
      fileName: String
  ) = hasSourceOutputs(moduleName, fileName)

  /**
   * Assert that one or source files were generated in the modular source output location.
   *
   * @param moduleName the name of the module.
   * @param fileName the first file name to check for.
   * @param moreFileNames the additional file names to check for.
   * @return this assertion object for further checks.
   */
  fun hasMultiModuleSourceOutputs(
      moduleName: String,
      fileName: String,
      vararg moreFileNames: String
  ) = apply {
    expectAllFilesToExist(StandardLocation.SOURCE_OUTPUT, moduleName, fileName, moreFileNames)
  }

  /**
   * Assert that a given file was generated in the non-module output class location.
   *
   * @param fileName the path of the file, relative to the output directory.
   * @return this assertion object for further checks.
   */
  fun hasClassOutput(fileName: String) = hasClassOutputs(fileName)

  /**
   * Assert that one or more files were generated in the non-module output class location.
   *
   * @param fileName the first file name to check for.
   * @param moreFileNames the additional file names to check for.
   * @return this assertion object for further checks.
   */
  fun hasClassOutputs(fileName: String, vararg moreFileNames: String) = apply {
    expectAllFilesToExist(StandardLocation.CLASS_OUTPUT, fileName, moreFileNames)
  }

  /**
   * Assert that a given class file was generated in the modular class output location.
   *
   * @param moduleName the name of the module.
   * @param fileName the path of the file, relative to the module's base directory.
   * @return this assertion object for further checks.
   */
  fun hasMultiModuleClassOutput(
      moduleName: String,
      fileName: String
  ) = hasMultiModuleClassOutputs(moduleName, fileName)

  /**
   * Assert that one or more class files were generated in the modular class output location.
   *
   * @param moduleName the name of the module.
   * @param fileName the first file name to check for.
   * @param moreFileNames the additional file names to check for.
   * @return this assertion object for further checks.
   */
  fun hasMultiModuleClassOutputs(
      moduleName: String,
      fileName: String,
      vararg moreFileNames: String
  ) = apply {
    expectAllFilesToExist(StandardLocation.CLASS_OUTPUT, moduleName, fileName, moreFileNames)
  }

  /**
   * Assert that a given C/C++ header file was generated in the non-module native header sources
   * location.
   *
   * @param fileName the path of the file, relative to the output directory.
   * @return this assertion object for further checks.
   */
  fun hasHeaderOutput(fileName: String) = hasHeaderOutputs(fileName)

  /**
   * Assert that one or more C/C++ header files were generated in the non-module native header
   * sources location.
   *
   * @param fileName the first file name to check for.
   * @param moreFileNames the additional file names to check for.
   * @return this assertion object for further checks.
   */
  fun hasHeaderOutputs(fileName: String, vararg moreFileNames: String) = apply {
    expectAllFilesToExist(StandardLocation.NATIVE_HEADER_OUTPUT, fileName, moreFileNames)
  }

  /**
   * Assert that a given C/C++ header file was generated in the modular native header sources.
   *
   * @param moduleName the name of the module.
   * @param fileName the path of the file, relative to the module's base directory.
   * @return this assertion object for further checks.
   */
  fun hasMultiModuleHeaderOutput(
      moduleName: String,
      fileName: String
  ) = hasMultiModuleHeaderOutputs(moduleName, fileName)

  /**
   * Assert that one or more C/C++ header files were generated in the modular native header
   * sources location.
   *
   * @param moduleName the name of the module.
   * @param fileName the first file name to check for.
   * @param moreFileNames the additional file names to check for.
   * @return this assertion object for further checks.
   */
  fun hasMultiModuleHeaderOutputs(
      moduleName: String,
      fileName: String,
      vararg moreFileNames: String
  ) = apply {
    expectAllFilesToExist(
        StandardLocation.NATIVE_HEADER_OUTPUT,
        moduleName,
        fileName,
        moreFileNames
    )
  }

  private fun expectAllFilesToExist(
      location: StandardLocation,
      fileName1: String,
      moreFileNames: Array<out String>
  ) = expectAllFilesToExistForAccessor(fileName1, moreFileNames) {
    getExpectedFile(location, it)
  }

  private fun expectAllFilesToExist(
      location: StandardLocation,
      moduleName: String,
      fileName1: String,
      moreFileNames: Array<out String>
  ) = expectAllFilesToExistForAccessor(fileName1, moreFileNames) {
    getExpectedFile(location, moduleName, it)
  }

  private fun expectAllFilesToExistForAccessor(
      fileName1: String,
      moreFileNames: Array<out String>,
      accessor: (String) -> JavaRamFileObject
  ) {
    val allFileNames = arrayOf(fileName1, *moreFileNames)

    val missingFileExceptions = mutableListOf<AssertionFailedError>()

    allFileNames.forEach {
      try {
        accessor(it)
      } catch (ex: AssertionFailedError) {
        missingFileExceptions += ex
      }
    }

    if (missingFileExceptions.isNotEmpty()) {
      if (missingFileExceptions.size == 1) {
        throw missingFileExceptions.first()
      } else {
        throw MultipleFailuresError("Multiple files were not found", missingFileExceptions)
      }
    }
  }

  private fun getExpectedFile(
      location: StandardLocation,
      fileName: String
  ): JavaRamFileObject {
    val mappedLocation = target.getInMemoryLocationFor(location)
    return target.getFile(mappedLocation, fileName)
        ?: throw AssertionFailedError(fileNotFoundMessage(mappedLocation, fileName))
  }

  private fun getExpectedFile(
      location: StandardLocation,
      moduleName: String,
      fileName: String
  ): JavaRamFileObject {
    val mappedLocation = target.getInMemoryLocationFor(location, moduleName)
    return target.getFile(mappedLocation, fileName)
        ?: throw AssertionFailedError(fileNotFoundMessage(mappedLocation, fileName))
  }

  private fun fileNotFoundMessage(location: JavaRamLocation, fileName: String): String {
    val message = StringBuilder("File $fileName not found in $location")

    val alternatives = target.findClosestFileNameMatchesFor(location, fileName)

    if (alternatives.isNotEmpty()) {
      message
          .appendLine()
          .appendLine()
          .appendLine("Files with similar names were found in the same location:")

      alternatives.forEach {
        message
            .append(" - ")
            .appendLine(it)
      }
    }

    return message.toString()
  }
}