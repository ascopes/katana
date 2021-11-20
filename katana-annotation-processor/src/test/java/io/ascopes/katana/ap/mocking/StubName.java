package io.ascopes.katana.ap.mocking;

import java.util.Objects;
import javax.lang.model.element.Name;


/**
 * Stub for {@link Name} to use in tests.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class StubName implements Name {
  private final String content;

  public StubName(String content) {
    this.content = Objects.requireNonNull(content);
  }

  @Override
  public boolean contentEquals(CharSequence cs) {
    return this.content.contentEquals(cs);
  }

  @Override
  public int length() {
    return this.content.length();
  }

  @Override
  public char charAt(int index) {
    return this.content.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return this.content.subSequence(start, end);
  }

  @Override
  public String toString() {
    return this.content.toString();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Name && ((Name) other).contentEquals(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content);
  }
}
