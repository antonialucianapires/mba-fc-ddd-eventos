package com.mba.fc.ingressos.core.common.domain;

import com.mba.fc.ingressos.core.common.domain.valueobjects.Uuid;
import java.util.Set;

public interface Repository<T extends AggregateRoot<? extends Uuid>> {

  T add(T entity);

  T findById(Uuid id);

  Set<T> findAll();

  void delete(Uuid id);
}
