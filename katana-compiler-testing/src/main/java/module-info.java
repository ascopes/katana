module katana.compilertesting {
  requires java.base;
  requires java.compiler;
  requires jimfs;           // https://github.com/google/jimfs/pull/180
  requires kotlin.reflect;  // Needed for testing to work properly.
  requires kotlin.stdlib;
  requires kotlin.stdlib.jdk7;
  requires kotlin.stdlib.jdk8;
  requires me.xdrop.fuzzywuzzy;
  requires org.opentest4j;

  exports io.ascopes.katana.compilertesting;
  exports io.ascopes.katana.compilertesting.java;

  // Needed for mocking to work properly.
  opens io.ascopes.katana.compilertesting to kotlin.reflect;
  opens io.ascopes.katana.compilertesting.java to kotlin.reflect;
}