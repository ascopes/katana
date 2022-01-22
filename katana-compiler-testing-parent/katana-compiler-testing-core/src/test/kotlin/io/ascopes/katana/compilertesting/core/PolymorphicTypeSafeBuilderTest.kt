package io.ascopes.katana.compilertesting.core

import java.util.concurrent.atomic.AtomicBoolean
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PolymorphicTypeSafeBuilderTest {
  @Test
  fun `I can cast to the builder type`() {
    val dummy = Dummy()

    dummy
        .givenThatICallThis()
        .thenThisAlsoGetsCalled()

    assertTrue(dummy.givenIsCalled.get())
    assertTrue(dummy.thenIsCalled.get())
  }

  class Dummy : PolymorphicTypeSafeBuilder<Dummy>() {
    val givenIsCalled = AtomicBoolean(false)
    val thenIsCalled = AtomicBoolean(false)

    fun givenThatICallThis() = this.apply {
      givenIsCalled.set(true)
    }

    fun thenThisAlsoGetsCalled() {
      thenIsCalled.set(true)
    }
  }
}