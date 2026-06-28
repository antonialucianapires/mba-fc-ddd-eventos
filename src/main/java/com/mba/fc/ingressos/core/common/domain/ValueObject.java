package com.mba.fc.ingressos.core.common.domain;

import java.util.Objects;

public abstract class ValueObject<T> {
  protected final T value;

  public ValueObject(T value) {
    if (value == null) throw new IllegalArgumentException("Value cannot be null");
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ValueObject<?> other = (ValueObject<?>) obj;
    return Objects.equals(this.value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), value);
  }
}
