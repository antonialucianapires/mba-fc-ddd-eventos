package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import com.mba.fc.ingressos.core.common.domain.valueobjects.Cpf;
import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import java.util.UUID;

public class Customer extends AggregateRoot<CustomerId> {

  private final Cpf cpf;
  private final String name;

  public Customer(Cpf cpf, String name) {
    super(new CustomerId());
    this.cpf = cpf;
    this.name = name;
  }

  public Customer(String id, Cpf cpf, String name) {
    super(new CustomerId(id));
    this.cpf = cpf;
    this.name = name;
  }

  public Customer(CustomerId id, Cpf cpf, String name) {
    super(id);
    this.cpf = cpf;
    this.name = name;
  }

  public static Customer create(String cpf, String name) {
    return new Customer(UUID.randomUUID().toString(), new Cpf(cpf), name);
  }

  public Customer changeName(String name) {
    return new Customer(this.id, this.cpf, name);
  }

  public CustomerId getId() {
    return id;
  }

  public Cpf getCpf() {
    return cpf;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Customer{id=" + id.getValue() + ", name=" + name + "}";
  }
}
