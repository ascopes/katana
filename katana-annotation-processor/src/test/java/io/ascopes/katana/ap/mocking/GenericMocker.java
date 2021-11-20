package io.ascopes.katana.ap.mocking;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.mockito.Mockito;

/**
 * Provides a wrapper around a mockito class to generate a generic mock. This is a hack for the
 * compiler.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class GenericMocker {
  private GenericMocker() {
    throw new UnsupportedOperationException("static-only class");
  }

  public static abstract class Ref<T> {
    private final Class<T> type;

    @SuppressWarnings("unchecked")
    public Ref() {
      ParameterizedType thisType = (ParameterizedType) this.getClass().getGenericSuperclass();
      Type tType = thisType.getActualTypeArguments()[0];
      if (Class.class.isAssignableFrom(tType.getClass())) {
        throw new UnsupportedOperationException(
            "<T> is not a parameterized type. Use Mockito.mock(T.class) instead"
        );
      }

      ParameterizedType tParameterizedType = (ParameterizedType) tType;
      // Unsafe.
      this.type = (Class<T>) tParameterizedType.getRawType();
    }
  }

  public static <T> T mock(Ref<T> reference) {
    return Mockito.mock(reference.type);
  }
}
