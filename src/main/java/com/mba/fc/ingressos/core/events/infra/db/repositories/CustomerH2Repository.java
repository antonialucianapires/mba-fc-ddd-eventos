package com.mba.fc.ingressos.core.events.infra.db.repositories;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.valueobjects.Uuid;
import com.mba.fc.ingressos.core.events.domain.entities.Customer;
import com.mba.fc.ingressos.core.events.domain.repositories.ICustomerRepository;
import com.mba.fc.ingressos.core.events.infra.db.mappers.CustomerMapper;
import com.mba.fc.ingressos.core.events.infra.db.schemas.CustomerSchema;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomerH2Repository implements ICustomerRepository {

  private final EntityManager entityManager;
  private final CustomerMapper customerMapper;
  private final IUnitOfWork unitOfWork;

  public CustomerH2Repository(
      EntityManager entityManager, CustomerMapper customerMapper, IUnitOfWork unitOfWork) {
    this.entityManager = entityManager;
    this.customerMapper = customerMapper;
    this.unitOfWork = unitOfWork;
  }

  @Override
  public Customer add(Customer entity) {
    // Ver o comentário equivalente em PartnerH2Repository.add(): rastreamos o "entity"
    // recebido porque é ele quem carrega os eventos de domínio, não o objeto reconstruído
    // pelo mapper depois do merge.
    unitOfWork.trackPersisted(entity);
    CustomerSchema schema = customerMapper.toSchema(entity);
    CustomerSchema merged = entityManager.merge(schema);
    return customerMapper.toDomain(merged);
  }

  @Override
  public Customer findById(Uuid id) {
    CustomerSchema schema = entityManager.find(CustomerSchema.class, id.getValue());
    if (schema == null) {
      return null;
    }
    return customerMapper.toDomain(schema);
  }

  @Override
  public Set<Customer> findAll() {
    return entityManager
        .createQuery("SELECT c FROM CustomerSchema c", CustomerSchema.class)
        .getResultStream()
        .collect(Collectors.toSet())
        .stream()
        .map(customerMapper::toDomain)
        .collect(Collectors.toSet());
  }

  @Override
  public void delete(Uuid id) {
    entityManager
        .createQuery("DELETE FROM CustomerSchema c WHERE c.id = :id")
        .setParameter("id", id.getValue())
        .executeUpdate();
  }
}
