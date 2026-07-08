package com.mba.fc.ingressos.core.common.application;

public interface IUnitOfWork {

  void commit();

  void rollback();
}
