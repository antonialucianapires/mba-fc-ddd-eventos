package com.mba.fc.ingressos.core.events.infra.db.mappers;

import com.mba.fc.ingressos.core.events.domain.entities.Partner;
import com.mba.fc.ingressos.core.events.infra.db.schemas.PartnerSchema;

public class PartnerMapper {

    public Partner toDomain(PartnerSchema schema) {
        return new Partner(schema.getId(), schema.getName());
    }

    public PartnerSchema toSchema(Partner domain) {
        return new PartnerSchema(domain.getId().getValue(), domain.getName());
    }
}
