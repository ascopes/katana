package io.ascopes.katana.compilertesting.java

import javax.lang.model.SourceVersion
import javax.lang.model.SourceVersion.RELEASE_11


/**
 * All releases that the compiler supports, above Java 11.
 */
fun javaReleases() = SourceVersion
    .values()
    .filter { it >= RELEASE_11 }
    .toList()
