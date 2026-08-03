package com.mba.fc.ingressos.core.common.infra;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UnitOfWorkJpa implements IUnitOfWork {

  private final EntityManager entityManager;
  private final PlatformTransactionManager transactionManager;
  private TransactionStatus transactionStatus;

  // Agregados manipulados na transação em andamento. São exatamente o "persistStack" e o
  // "removeStack" descritos na versão Node: listas simples, preenchidas pelos repositórios
  // (persistStack via trackPersisted) e esvaziadas por drainManipulatedAggregates().
  private final List<AggregateRoot<?>> persistStack = new ArrayList<>();
  private final List<AggregateRoot<?>> removeStack = new ArrayList<>();

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
    // Uma transação abortada não deve deixar agregados "pendentes de publicação" para a
    // próxima vez que esta instância for usada, então esvaziamos as pilhas aqui também.
    persistStack.clear();
    removeStack.clear();
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

  @Override
  public void trackPersisted(AggregateRoot<?> aggregateRoot) {
    persistStack.add(aggregateRoot);
  }

  @Override
  public void trackRemoved(AggregateRoot<?> aggregateRoot) {
    removeStack.add(aggregateRoot);
  }

  @Override
  public List<AggregateRoot<?>> drainManipulatedAggregates() {
    List<AggregateRoot<?>> manipulated = new ArrayList<>(persistStack.size() + removeStack.size());
    manipulated.addAll(persistStack);
    manipulated.addAll(removeStack);
    persistStack.clear();
    removeStack.clear();
    return manipulated;
  }
}
