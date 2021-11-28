package io.ascopes.katana.annotations;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

abstract class TypeAware<T> {
  @SuppressWarnings("unchecked")
  protected Class<T> getGenericType() {
    ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
    return (Class<T>) type.getActualTypeArguments()[0];
  }
}
