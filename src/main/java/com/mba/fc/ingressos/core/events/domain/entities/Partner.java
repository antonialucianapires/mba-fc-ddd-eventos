package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;

public class Partner extends AggregateRoot<PartnerId> {

    private final PartnerId id;
    private final String name;

    public Partner(String name) {
        super(new PartnerId());
        this.id = new PartnerId();
        this.name = name;
    }

    public Partner(String id, String name) {
        super(new PartnerId(id));
        this.id = new PartnerId(id);
        this.name = name;
    }

    public Partner(PartnerId id, String name) {
        super(id);
        this.id = id;
        this.name = name;
    }

    public static Partner create(String name) {
        return new Partner(
                new PartnerId(),
                name
        );
    }

    public PartnerId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Partner{id=" + id.getValue() + ", name=" + name + "}";
    }
}
