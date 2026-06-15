package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;

import java.util.UUID;

public class Customer extends AggregateRoot {
    private final String id;
    private final String cpf;
    private final String name;

    public Customer(String id, String cpf, String name) {
        super();
        this.id = id;
        this.cpf = cpf;
        this.name = name;
    }

    public static Customer create(String cpf, String name) {
        return new Customer(
                UUID.randomUUID().toString(),
                cpf,
                name
        );
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

    @Override
    public String toString() {
        //CPF é PII (Personally Identifiable Information). Se aparecer em log, viola LGPD.
        return "Customer{id=" + id + ", name=" + name + "}";
    }
}
