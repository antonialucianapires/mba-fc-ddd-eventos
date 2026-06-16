package com.mba.fc.ingressos.core.common.domain.valueobjects;

import com.mba.fc.ingressos.core.common.domain.ValueObject;

import java.util.UUID;

public class Uuid extends ValueObject<String> {
    public Uuid(String value) {
        super(value);
        this.validate();
    }

    public Uuid() {
        super(UUID.randomUUID().toString());
    }

    private void validate() {
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID: " + value);
        }
    }
}
