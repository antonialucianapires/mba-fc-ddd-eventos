package com.mba.fc.ingressos.core.events.infra.db.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.commands.AddSectionCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Event;
import com.mba.fc.ingressos.core.events.domain.entities.EventSection;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSectionSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.PartnerSchema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EventMapper")
class EventMapperTest {

  private static final String VALID_ID = UUID.randomUUID().toString();
  private static final String VALID_PARTNER_ID = UUID.randomUUID().toString();
  private static final String VALID_NAME = "Rock in Rio";
  private static final String VALID_DESCRIPTION = "Festival de musica";
  private static final LocalDate VALID_DATE = LocalDate.of(2026, 9, 12);

  private final EventMapper mapper = new EventMapper();

  @Nested
  @DisplayName("toDomain(EventSchema)")
  class ToDomain {

    @Test
    @DisplayName("should wrap the schema ID into an EventId value object")
    void shouldWrapIdIntoEventId() {
      EventSchema schema =
          new EventSchema(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              0,
              0,
              new PartnerSchema(VALID_PARTNER_ID, "Partner"),
              new LinkedHashSet<>());

      Event event = mapper.toDomain(schema);

      assertInstanceOf(EventId.class, event.getId());
      assertEquals(VALID_ID, event.getId().getValue());
    }

    @Test
    @DisplayName("should wrap the schema partner ID into a PartnerId value object")
    void shouldWrapPartnerIdIntoPartnerId() {
      EventSchema schema =
          new EventSchema(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              0,
              0,
              new PartnerSchema(VALID_PARTNER_ID, "Partner"),
              new LinkedHashSet<>());

      Event event = mapper.toDomain(schema);

      assertInstanceOf(PartnerId.class, event.getPartnerId());
      assertEquals(VALID_PARTNER_ID, event.getPartnerId().getValue());
    }

    @Test
    @DisplayName("should map every section from the schema")
    void shouldMapAllSections() {
      EventSchema eventSchema =
          new EventSchema(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              10,
              0,
              new PartnerSchema(VALID_PARTNER_ID, "Partner"),
              new LinkedHashSet<>());
      EventSectionSchema section1 =
          new EventSectionSchema(
              UUID.randomUUID().toString(),
              "Pista",
              "desc",
              false,
              5,
              0,
              100.0,
              eventSchema,
              new LinkedHashSet<>());
      EventSectionSchema section2 =
          new EventSectionSchema(
              UUID.randomUUID().toString(),
              "Camarote",
              "desc",
              false,
              5,
              0,
              200.0,
              eventSchema,
              new LinkedHashSet<>());
      eventSchema =
          new EventSchema(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              10,
              0,
              new PartnerSchema(VALID_PARTNER_ID, "Partner"),
              new LinkedHashSet<>(Set.of(section1, section2)));

      Event event = mapper.toDomain(eventSchema);

      assertEquals(2, event.getSections().size());
    }

    @Test
    @DisplayName("each schema field should survive the mapping independently")
    void shouldMapAllFields() {
      EventSchema schema =
          new EventSchema(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              true,
              10,
              4,
              new PartnerSchema(VALID_PARTNER_ID, "Partner"),
              new LinkedHashSet<>());

      Event event = mapper.toDomain(schema);

      assertEquals(VALID_ID, event.getId().getValue());
      assertEquals(VALID_NAME, event.getName());
      assertEquals(VALID_DESCRIPTION, event.getDescription());
      assertEquals(VALID_DATE, event.getDate());
      assertTrue(event.isPublished());
      assertEquals(10, event.getTotalSpots());
      assertEquals(4, event.getTotalSpotsReserved());
    }
  }

  @Nested
  @DisplayName("toSchema(Event)")
  class ToSchema {

    @Test
    @DisplayName("should return an EventSchema with the same ID as the event")
    void shouldMapIdFromEvent() {
      Event event =
          new Event(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              0,
              0,
              new PartnerId(VALID_PARTNER_ID),
              new LinkedHashSet<>());

      EventSchema schema = mapper.toSchema(event);

      assertEquals(VALID_ID, schema.getId());
    }

    @Test
    @DisplayName("should store the partner ID as a plain String on the partner schema")
    void shouldMapPartnerId() {
      Event event =
          new Event(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              0,
              0,
              new PartnerId(VALID_PARTNER_ID),
              new LinkedHashSet<>());

      EventSchema schema = mapper.toSchema(event);

      assertEquals(VALID_PARTNER_ID, schema.getPartner().getId());
    }

    @Test
    @DisplayName("should map every section from the event, associated with the new schema")
    void shouldMapAllSections() {
      EventSection section =
          EventSection.create(new AddSectionCommand("Pista", "desc", 5, BigDecimal.TEN));
      Event event =
          new Event(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              5,
              0,
              new PartnerId(VALID_PARTNER_ID),
              new LinkedHashSet<>(Set.of(section)));

      EventSchema schema = mapper.toSchema(event);

      assertEquals(1, schema.getSections().size());
      for (EventSectionSchema sectionSchema : schema.getSections()) {
        assertSame(schema, sectionSchema.getEvent());
      }
    }
  }

  @Nested
  @DisplayName("Round-trip")
  class RoundTrip {

    @Test
    @DisplayName("toDomain(toSchema(event)) should preserve the original fields")
    void domainToSchemaAndBackShouldPreserveFields() {
      EventSection section =
          EventSection.create(new AddSectionCommand("Pista", "desc", 5, BigDecimal.TEN));
      Event original =
          new Event(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              true,
              5,
              0,
              new PartnerId(VALID_PARTNER_ID),
              new LinkedHashSet<>(Set.of(section)));

      Event roundTripped = mapper.toDomain(mapper.toSchema(original));

      assertEquals(original.getId().getValue(), roundTripped.getId().getValue());
      assertEquals(original.getName(), roundTripped.getName());
      assertEquals(original.getDescription(), roundTripped.getDescription());
      assertEquals(original.getDate(), roundTripped.getDate());
      assertEquals(original.isPublished(), roundTripped.isPublished());
      assertEquals(original.getPartnerId().getValue(), roundTripped.getPartnerId().getValue());
      assertEquals(original.getSections().size(), roundTripped.getSections().size());
    }
  }
}
