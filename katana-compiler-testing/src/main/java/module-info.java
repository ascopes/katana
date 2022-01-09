module katana.compilertesting {
  requires java.base;
  requires java.compiler;
  requires kotlin.stdlib.jdk7;
  requires kotlin.stdlib.jdk8;
  requires org.opentest4j;

  exports io.ascopes.katana.compilertesting.compilation;
  exports io.ascopes.katana.compilertesting.diagnostics;
  exports io.ascopes.katana.compilertesting.files;

  /////////////////////////////////////////////////////////////////////
  /// Modules that need updating when a module release is available ///
  /////////////////////////////////////////////////////////////////////

  // https://github.com/xdrop/fuzzywuzzy/pull/95
  requires fuzzywuzzy;

  // https://github.com/google/jimfs/pull/180
  requires jimfs;
}