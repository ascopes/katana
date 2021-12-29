module katana.compilertesting {
  requires java.base;
  requires java.compiler;
  requires jimfs;
  requires kotlin.stdlib.jdk7;
  requires kotlin.stdlib.jdk8;

  exports io.ascopes.katana.compilertesting.java;
}