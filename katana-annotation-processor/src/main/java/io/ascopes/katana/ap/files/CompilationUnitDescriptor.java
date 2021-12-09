package io.ascopes.katana.ap.files;

import com.squareup.javapoet.JavaFile;
import io.ascopes.katana.ap.utils.StringUtils;
import java.util.Objects;

/**
 * Descriptor for a compilation unit.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class CompilationUnitDescriptor {

  private final String fileName;
  private final JavaFile javaFile;

  /**
   * Initialize this compilation unit descriptor.
   *
   * @param fileName the descriptive file name of the compilation unit.
   * @param javaFile the compilation unit source.
   */
  public CompilationUnitDescriptor(String fileName, JavaFile javaFile) {
    this.fileName = Objects.requireNonNull(fileName);
    this.javaFile = Objects.requireNonNull(javaFile);
  }

  /**
   * Get the descriptive file name of the compilation unit.
   *
   * @return the file name.
   */
  public String getFileName() {
    return this.fileName;
  }

  /**
   * Get the compilation unit source code.
   *
   * @return the Java file source code.
   */
  public JavaFile getJavaFile() {
    return this.javaFile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "CompilationUnitDescriptor{"
        + "fileName=" + StringUtils.quoted(this.fileName)
        + '}';
  }
}
