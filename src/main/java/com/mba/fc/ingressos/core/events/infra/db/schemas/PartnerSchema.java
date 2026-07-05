package com.mba.fc.ingressos.core.events.infra.db.schemas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "partners")
public class PartnerSchema {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    public PartnerSchema() {
    }

    public PartnerSchema(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public PartnerSchema(UUID id, String name) {
        this.id = id.toString();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
