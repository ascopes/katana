module katana.compilertesting {
  requires fuzzywuzzy;
  requires java.base;
  requires java.compiler;
  requires jimfs;
  requires kotlin.stdlib.jdk7;
  requires kotlin.stdlib.jdk8;
  requires org.opentest4j;

  exports io.ascopes.katana.compilertesting.java;
}