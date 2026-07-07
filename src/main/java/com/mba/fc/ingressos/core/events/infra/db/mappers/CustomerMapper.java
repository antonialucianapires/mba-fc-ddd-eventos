package com.mba.fc.ingressos.core.events.infra.db.mappers;

import com.mba.fc.ingressos.core.common.domain.valueobjects.Cpf;
import com.mba.fc.ingressos.core.events.domain.entities.Customer;
import com.mba.fc.ingressos.core.events.infra.db.schemas.CustomerSchema;

public class CustomerMapper {

  public Customer toDomain(CustomerSchema schema) {
    return new Customer(schema.getId(), new Cpf(schema.getCpf()), schema.getName());
  }

  public CustomerSchema toSchema(Customer domain) {
    return new CustomerSchema(
        domain.getId().getValue(), domain.getCpf().getValue(), domain.getName());
  }
}
