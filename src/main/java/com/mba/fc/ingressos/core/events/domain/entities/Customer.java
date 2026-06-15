package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import com.mba.fc.ingressos.core.common.domain.valueobjects.CPF;

import java.util.UUID;

public class Customer extends AggregateRoot {
    private final String id;
    private final CPF cpf;
    private final String name;

    public Customer(String id, CPF cpf, String name) {
        super();
        this.id = id;
        this.cpf = cpf;
        this.name = name;
    }

    public static Customer create(String cpf, String name) {
        return new Customer(
                UUID.randomUUID().toString(),
                new CPF(cpf),
                name
        );
    }

    public String getId() {
        return id;
    }

    public CPF getCpf() {
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
