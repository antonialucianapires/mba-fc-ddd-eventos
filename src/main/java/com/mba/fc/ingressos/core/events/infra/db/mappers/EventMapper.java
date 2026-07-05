package com.mba.fc.ingressos.core.events.infra.db.mappers;

import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.entities.Event;
import com.mba.fc.ingressos.core.events.domain.entities.EventSection;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSectionSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.PartnerSchema;
import java.util.LinkedHashSet;
import java.util.Set;

public class EventMapper {

  private final EventSectionMapper sectionMapper = new EventSectionMapper();

  public Event toDomain(EventSchema schema) {
    Set<EventSection> sections = new LinkedHashSet<>();
    for (EventSectionSchema sectionSchema : schema.getSections()) {
      sections.add(sectionMapper.toDomain(sectionSchema));
    }
    return new Event(
        schema.getId(),
        schema.getName(),
        schema.getDescription(),
        schema.getDate(),
        schema.isPublished(),
        schema.getTotalSpots(),
        schema.getTotalSpotsReserved(),
        new PartnerId(schema.getPartner().getId()),
        sections);
  }

  public EventSchema toSchema(Event domain) {
    PartnerSchema partnerSchema = new PartnerSchema(domain.getPartnerId().getValue(), null);
    Set<EventSectionSchema> sectionSchemas = new LinkedHashSet<>();
    EventSchema eventSchema =
        new EventSchema(
            domain.getId().getValue(),
            domain.getName(),
            domain.getDescription(),
            domain.getDate(),
            domain.isPublished(),
            domain.getTotalSpots(),
            domain.getTotalSpotsReserved(),
            partnerSchema,
            sectionSchemas);
    for (EventSection section : domain.getSections()) {
      sectionSchemas.add(sectionMapper.toSchema(section, eventSchema));
    }
    return eventSchema;
  }
}
