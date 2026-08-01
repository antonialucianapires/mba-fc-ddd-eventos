package com.mba.fc.ingressos.core.common.application;

import java.util.function.Supplier;

public interface IUnitOfWork {

  void commit();

  void rollback();

  void beginTransaction();

  void completeTransaction();

  void rollbackTransaction();

  <T> T runTransaction(Supplier<T> action);
}
