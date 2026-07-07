package com.mba.fc.ingressos.core.events.infra.db.schemas;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class CustomerSchema {

  @Id
  @Column(name = "id", nullable = false, unique = true)
  private String id;

  @Column(name = "cpf", nullable = false, unique = true)
  private String cpf;

  @Column(name = "name", nullable = false)
  private String name;

  public CustomerSchema() {}

  public CustomerSchema(String id, String cpf, String name) {
    this.id = id;
    this.cpf = cpf;
    this.name = name;
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
