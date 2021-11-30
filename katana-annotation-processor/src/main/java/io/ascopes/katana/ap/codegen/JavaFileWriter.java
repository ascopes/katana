package io.ascopes.katana.ap.codegen;

import com.squareup.javapoet.JavaFile;
import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.utils.Result;
import java.io.IOException;
import javax.annotation.processing.Filer;
import javax.tools.Diagnostic.Kind;

/**
 * Helper to write out generated Java sources and report any errors.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class JavaFileWriter {

  private final Logger logger;
  private final Filer filer;
  private final Diagnostics diagnostics;

  /**
   * Initialize the file writer.
   *
   * @param filer       the filer to use.
   * @param diagnostics the diagnostics to report errors with.
   */
  public JavaFileWriter(Filer filer, Diagnostics diagnostics) {
    this.logger = LoggerFactory.loggerFor(this.getClass());
    this.filer = filer;
    this.diagnostics = diagnostics;
  }

  /**
   * Write out the given file with the given name.
   *
   * @param name     the name of the file, used for logging.
   * @param javaFile the actual file to write out.
   * @return an empty OK result, or a failed result if an error occurred.
   */
  public Result<Void> writeOutFile(String name, JavaFile javaFile) {
    try {
      this.logger.info("Writing out generated source for {}", name);
      javaFile.writeTo(this.filer);
      this.logger.debug("Written out generated source for {} successfully!", name);
      return Result.ok();

    } catch (IOException ex) {
      this.diagnostics
          .builder()
          .kind(Kind.ERROR)
          .template("ioException")
          .param("fileName", name)
          .param("stacktrace", ex)
          .log();

      return Result.fail("Failed to write out file");
    }
  }
}
