package io.ascopes.katana.compilertesting.java.it

import org.junit.jupiter.params.provider.MethodSource
import java.lang.annotation.Inherited
import javax.lang.model.SourceVersion
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

object Each {
  @Inherited
  @MethodSource("io.ascopes.katana.compilertesting.java.it.Each#javaVersions")
  @MustBeDocumented
  @Retention(RUNTIME)
  @Target(ANNOTATION_CLASS, CLASS, FUNCTION)
  annotation class JavaVersion

  @JvmStatic
  fun javaVersions() = SourceVersion
      .values()
      .filter { it >= SourceVersion.RELEASE_11 }
      .toList()
}