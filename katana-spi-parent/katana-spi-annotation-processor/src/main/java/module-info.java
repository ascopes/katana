import io.ascopes.katana.spi.annotationprocessor.KatanaSpiAnnotationProcessor;
import javax.annotation.processing.Processor;

module katana.spi.annotationprocessor {
  requires java.base;
  requires java.compiler;
  requires kotlin.stdlib;
  requires kotlin.stdlib.jdk7;
  requires kotlin.stdlib.jdk8;
  requires katana.spi.annotations;

  exports io.ascopes.katana.spi.annotationprocessor;

  provides Processor with KatanaSpiAnnotationProcessor;
}
