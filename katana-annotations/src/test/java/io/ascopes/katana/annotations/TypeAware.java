package io.ascopes.katana.annotations;

import java.lang.reflect.ParameterizedType;

abstract class TypeAware<T> {

  @SuppressWarnings("unchecked")
  protected Class<T> getGenericType() {
    ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
    return (Class<T>) type.getActualTypeArguments()[0];
  }
}
