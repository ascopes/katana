package io.ascopes.katana.ap.stubbing;

import java.util.Objects;
import javax.lang.model.element.Name;
import org.checkerframework.checker.nullness.qual.NonNull;


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
  @NonNull
  public CharSequence subSequence(int start, int end) {
    return this.content.subSequence(start, end);
  }

  @Override
  @NonNull
  public String toString() {
    return this.content;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Name && ((Name) other).contentEquals(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.content);
  }
}
