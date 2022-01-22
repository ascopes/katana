package io.ascopes.katana.compilertesting.core

import java.nio.charset.StandardCharsets
import org.junit.jupiter.params.provider.MethodSource

/**
 * Various re-usable fixtures for parameterized tests.
 */
object Each {
  @MethodSource("io.ascopes.katana.compilertesting.core.Each#standardCharsets")
  annotation class StandardCharset

  @JvmStatic
  fun standardCharsets() = listOf(
      StandardCharsets.ISO_8859_1,
      StandardCharsets.US_ASCII,
      StandardCharsets.UTF_16BE,
      StandardCharsets.UTF_16BE,
      StandardCharsets.UTF_16,
      StandardCharsets.UTF_8,
  )
}