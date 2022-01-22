module katana.compilertesting.core {
  requires kotlin.logging.jvm;            // https://github.com/MicroUtils/kotlin-logging/issues/223
  requires kotlin.reflect;                // Needed for testing to work properly.
  requires kotlin.stdlib;
  requires kotlin.stdlib.jdk7;
  requires kotlin.stdlib.jdk8;
  requires org.opentest4j;
  requires org.slf4j;

  exports io.ascopes.katana.compilertesting.core;

  // Needed for mocking to work properly.
  opens io.ascopes.katana.compilertesting.core to kotlin.reflect;
}