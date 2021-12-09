package io.ascopes.katana.ap.files;

import com.squareup.javapoet.JavaFile;
import io.ascopes.katana.ap.logging.Diagnostics;
import io.ascopes.katana.ap.logging.Logger;
import io.ascopes.katana.ap.logging.LoggerFactory;
import io.ascopes.katana.ap.utils.Result;
import io.ascopes.katana.ap.utils.TimerUtils;
import java.io.IOException;
import javax.annotation.processing.Filer;
import javax.tools.Diagnostic.Kind;

/**
 * Helper to write out generated Java sources and report any errors.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class CompilationUnitWriter {

  private final Logger logger;
  private final Filer filer;
  private final Diagnostics diagnostics;

  /**
   * Initialize the file writer.
   *
   * @param filer       the filer to use.
   * @param diagnostics the diagnostics to report errors with.
   */
  public CompilationUnitWriter(Filer filer, Diagnostics diagnostics) {
    this.logger = LoggerFactory.loggerFor(this.getClass());
    this.filer = filer;
    this.diagnostics = diagnostics;
  }

  /**
   * Write out the given compilation unit.
   *
   * @param compilationUnit the compilation unit.
   * @return an empty OK result, or a failed result if an error occurred.
   */
  public Result<Void> write(CompilationUnitDescriptor compilationUnit) {
    try {
      String fileName = compilationUnit.getFileName();
      JavaFile javaFile = compilationUnit.getJavaFile();

      this.logger.info("Saving generated source for {}", fileName);

      double duration = TimerUtils.timing(() -> javaFile.writeTo(this.filer));

      this.logger.debug(
          "Saved generated source for {} successfully in ~{}!",
          fileName,
          String.format("%.3sms", duration)
      );

      return Result.ok();
    } catch (IOException ex) {
      this.diagnostics
          .builder()
          .kind(Kind.ERROR)
          .template("ioException")
          .param("fileName", compilationUnit.getFileName())
          .param("stacktrace", ex)
          .log();

      return Result.fail("Failed to write out file");
    }
  }
}
