package com.mba.fc.ingressos.core.events.infra.db.mappers;

import com.mba.fc.ingressos.core.events.domain.entities.EventSection;
import com.mba.fc.ingressos.core.events.domain.entities.EventSpot;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSectionSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSpotSchema;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

public class EventSectionMapper {

  private final EventSpotMapper spotMapper = new EventSpotMapper();

  public EventSection toDomain(EventSectionSchema schema) {
    Set<EventSpot> spots = new LinkedHashSet<>();
    for (EventSpotSchema spotSchema : schema.getSpots()) {
      spots.add(spotMapper.toDomain(spotSchema));
    }
    return new EventSection(
        schema.getId(),
        schema.getName(),
        schema.getDescription(),
        schema.isPublished(),
        schema.getTotalSpots(),
        schema.getTotalSpotsReserved(),
        BigDecimal.valueOf(schema.getPrice()),
        spots);
  }

  public EventSectionSchema toSchema(EventSection domain, EventSchema event) {
    Set<EventSpotSchema> spotSchemas = new LinkedHashSet<>();
    EventSectionSchema sectionSchema =
        new EventSectionSchema(
            domain.getId().getValue(),
            domain.getName(),
            domain.getDescription(),
            domain.isPublished(),
            domain.getTotalSpots(),
            domain.getTotalSpotsReserved(),
            domain.getPrice().doubleValue(),
            event,
            spotSchemas);
    for (EventSpot spot : domain.getSpots()) {
      spotSchemas.add(spotMapper.toSchema(spot, sectionSchema));
    }
    return sectionSchema;
  }
}
