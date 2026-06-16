package com.mba.fc.ingressos.core.common.domain;

public abstract class AggregateRoot<ID> extends Entity<ID> {
    protected AggregateRoot(ID id) {
        super(id);
    }
}
