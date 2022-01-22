module katana.compilertesting.java {
  requires java.base;
  requires java.compiler;
  requires jimfs;                        // https://github.com/google/jimfs/pull/180
  requires kotlin.logging.jvm;           // https://github.com/MicroUtils/kotlin-logging/issues/223
  requires kotlin.reflect;               // Needed for testing to work properly.
  requires kotlin.stdlib;
  requires kotlin.stdlib.jdk7;
  requires kotlin.stdlib.jdk8;
  requires me.xdrop.fuzzywuzzy;
  requires org.opentest4j;
  requires org.slf4j;

  requires transitive katana.compilertesting.core;

  exports io.ascopes.katana.compilertesting.java;

  // Needed for mocking to work properly.
  opens io.ascopes.katana.compilertesting.java to kotlin.reflect;
}