package com.mba.fc.ingressos.core.common.infra;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import jakarta.persistence.EntityManager;
import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UnitOfWorkJpa implements IUnitOfWork {

  private final EntityManager entityManager;
  private final PlatformTransactionManager transactionManager;
  private TransactionStatus transactionStatus;

  public UnitOfWorkJpa(EntityManager entityManager, PlatformTransactionManager transactionManager) {
    this.entityManager = entityManager;
    this.transactionManager = transactionManager;
  }

  @Override
  public void commit() {
    entityManager.flush();
  }

  @Override
  public void rollback() {
    entityManager.clear();
  }

  @Override
  public void beginTransaction() {
    this.transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
  }

  @Override
  public void completeTransaction() {
    transactionManager.commit(transactionStatus);
  }

  @Override
  public void rollbackTransaction() {
    transactionManager.rollback(transactionStatus);
  }

  @Override
  public <T> T runTransaction(Supplier<T> action) {
    beginTransaction();
    try {
      T result = action.get();
      completeTransaction();
      return result;
    } catch (RuntimeException e) {
      rollbackTransaction();
      throw e;
    }
  }
}
