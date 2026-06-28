package com.mba.fc.ingressos.core.common.domain;

import java.util.Objects;

public abstract class Entity<ID> {

  protected final ID id;

  protected Entity(ID id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Entity<?> other = (Entity<?>) obj;
    return Objects.equals(this.id, other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public abstract String toString();
}
