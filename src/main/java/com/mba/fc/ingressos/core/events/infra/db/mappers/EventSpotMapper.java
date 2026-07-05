package com.mba.fc.ingressos.core.events.infra.db.mappers;

import com.mba.fc.ingressos.core.events.domain.entities.EventSpot;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSectionSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSpotSchema;

public class EventSpotMapper {

  public EventSpot toDomain(EventSpotSchema schema) {
    return new EventSpot(
        schema.getId(), schema.getLocation(), schema.isReserved(), schema.isPublished());
  }

  public EventSpotSchema toSchema(EventSpot domain, EventSectionSchema section) {
    return new EventSpotSchema(
        domain.getId().getValue(),
        domain.getLocation(),
        domain.isReserved(),
        domain.isPublished(),
        section);
  }
}
