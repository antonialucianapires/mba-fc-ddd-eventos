package com.mba.fc.ingressos.core.events.infra.db.repositories;

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

  public CustomerH2Repository(EntityManager entityManager, CustomerMapper customerMapper) {
    this.entityManager = entityManager;
    this.customerMapper = customerMapper;
  }

  @Override
  public Customer add(Customer entity) {
    CustomerSchema schema = customerMapper.toSchema(entity);
    entityManager.persist(schema);
    return customerMapper.toDomain(schema);
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
