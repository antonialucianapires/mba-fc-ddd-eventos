package com.mba.fc.ingressos.events.customers;

import com.mba.fc.ingressos.core.events.domain.entities.Customer;

public class CustomerResponse {

  private final String id;
  private final String cpf;
  private final String name;

  public CustomerResponse(Customer customer) {
    this.id = customer.getId().getValue();
    this.cpf = customer.getCpf().getValue();
    this.name = customer.getName();
  }

  public String getId() {
    return id;
  }

  public String getCpf() {
    return cpf;
  }

  public String getName() {
    return name;
  }
}
