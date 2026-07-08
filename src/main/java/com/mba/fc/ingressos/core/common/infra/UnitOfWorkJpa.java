package com.mba.fc.ingressos.core.common.infra;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import jakarta.persistence.EntityManager;

public class UnitOfWorkJpa implements IUnitOfWork {

  private final EntityManager entityManager;

  public UnitOfWorkJpa(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public void commit() {
    entityManager.flush();
  }

  @Override
  public void rollback() {
    entityManager.clear();
  }
}
