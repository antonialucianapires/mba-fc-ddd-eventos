package com.mba.fc.ingressos.core.events.infra.db.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.events.domain.entities.EventSection;
import com.mba.fc.ingressos.core.events.domain.entities.EventSpot;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSectionSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSpotSchema;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EventSectionMapper")
class EventSectionMapperTest {

  private static final String VALID_ID = UUID.randomUUID().toString();
  private static final String VALID_NAME = "Pista";
  private static final String VALID_DESCRIPTION = "Setor de pista";
  private static final double VALID_PRICE = 150.0;

  private final EventSectionMapper mapper = new EventSectionMapper();

  @Nested
  @DisplayName("toDomain(EventSectionSchema)")
  class ToDomain {

    @Test
    @DisplayName("should wrap the schema ID into an EventSectionId value object")
    void shouldWrapIdIntoEventSectionId() {
      EventSectionSchema schema =
          new EventSectionSchema(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              false,
              10,
              0,
              VALID_PRICE,
              null,
              new LinkedHashSet<>());

      EventSection section = mapper.toDomain(schema);

      assertInstanceOf(EventSectionId.class, section.getId());
      assertEquals(VALID_ID, section.getId().getValue());
    }

    @Test
    @DisplayName("should convert the schema price to BigDecimal")
    void shouldMapPriceAsBigDecimal() {
      EventSectionSchema schema =
          new EventSectionSchema(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              false,
              10,
              0,
              VALID_PRICE,
              null,
              new LinkedHashSet<>());

      EventSection section = mapper.toDomain(schema);

      assertEquals(0, BigDecimal.valueOf(VALID_PRICE).compareTo(section.getPrice()));
    }

    @Test
    @DisplayName("should map every spot from the schema")
    void shouldMapAllSpots() {
      EventSectionSchema schema =
          new EventSectionSchema(
              VALID_ID, VALID_NAME, VALID_DESCRIPTION, false, 2, 0, VALID_PRICE, null, null);
      EventSpotSchema spot1 =
          new EventSpotSchema(UUID.randomUUID().toString(), "Spot 1", false, false, schema);
      EventSpotSchema spot2 =
          new EventSpotSchema(UUID.randomUUID().toString(), "Spot 2", false, false, schema);
      Set<EventSpotSchema> spots = new LinkedHashSet<>(Set.of(spot1, spot2));
      schema =
          new EventSectionSchema(
              VALID_ID, VALID_NAME, VALID_DESCRIPTION, false, 2, 0, VALID_PRICE, null, spots);

      EventSection section = mapper.toDomain(schema);

      assertEquals(2, section.getSpots().size());
    }

    @Test
    @DisplayName("each schema field should survive the mapping independently")
    void shouldMapAllFields() {
      EventSectionSchema schema =
          new EventSectionSchema(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              true,
              10,
              3,
              VALID_PRICE,
              null,
              new LinkedHashSet<>());

      EventSection section = mapper.toDomain(schema);

      assertEquals(VALID_ID, section.getId().getValue());
      assertEquals(VALID_NAME, section.getName());
      assertEquals(VALID_DESCRIPTION, section.getDescription());
      assertTrue(section.isPublished());
      assertEquals(10, section.getTotalSpots());
      assertEquals(3, section.getTotalSpotsReserved());
    }
  }

  @Nested
  @DisplayName("toSchema(EventSection, EventSchema)")
  class ToSchema {

    @Test
    @DisplayName("should return an EventSectionSchema with the same ID as the section")
    void shouldMapIdFromSection() {
      EventSection section =
          new EventSection(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              false,
              0,
              0,
              BigDecimal.valueOf(VALID_PRICE),
              new LinkedHashSet<>());

      EventSectionSchema schema = mapper.toSchema(section, null);

      assertEquals(VALID_ID, schema.getId());
    }

    @Test
    @DisplayName("should convert the section price to a primitive double")
    void shouldMapPriceAsDouble() {
      EventSection section =
          new EventSection(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              false,
              0,
              0,
              BigDecimal.valueOf(VALID_PRICE),
              new LinkedHashSet<>());

      EventSectionSchema schema = mapper.toSchema(section, null);

      assertEquals(VALID_PRICE, schema.getPrice());
    }

    @Test
    @DisplayName("should associate the schema with the given event")
    void shouldAssociateEvent() {
      EventSection section =
          new EventSection(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              false,
              0,
              0,
              BigDecimal.valueOf(VALID_PRICE),
              new LinkedHashSet<>());
      EventSchema event = new EventSchema();

      EventSectionSchema schema = mapper.toSchema(section, event);

      assertSame(event, schema.getEvent());
    }

    @Test
    @DisplayName("should map every spot from the section, associated with the new schema")
    void shouldMapAllSpots() {
      EventSpot spot1 = EventSpot.create("Spot 1");
      EventSpot spot2 = EventSpot.create("Spot 2");
      EventSection section =
          new EventSection(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              false,
              2,
              0,
              BigDecimal.valueOf(VALID_PRICE),
              new LinkedHashSet<>(Set.of(spot1, spot2)));

      EventSectionSchema schema = mapper.toSchema(section, null);

      assertEquals(2, schema.getSpots().size());
      for (EventSpotSchema spotSchema : schema.getSpots()) {
        assertSame(schema, spotSchema.getEventSection());
      }
    }
  }

  @Nested
  @DisplayName("Round-trip")
  class RoundTrip {

    @Test
    @DisplayName("toDomain(toSchema(section)) should preserve the original fields")
    void domainToSchemaAndBackShouldPreserveFields() {
      EventSpot spot = EventSpot.create("Spot 1");
      EventSection original =
          new EventSection(
              VALID_ID,
              VALID_NAME,
              VALID_DESCRIPTION,
              true,
              1,
              0,
              BigDecimal.valueOf(VALID_PRICE),
              new LinkedHashSet<>(Set.of(spot)));

      EventSection roundTripped = mapper.toDomain(mapper.toSchema(original, null));

      assertEquals(original.getId().getValue(), roundTripped.getId().getValue());
      assertEquals(original.getName(), roundTripped.getName());
      assertEquals(original.getDescription(), roundTripped.getDescription());
      assertEquals(original.isPublished(), roundTripped.isPublished());
      assertEquals(0, original.getPrice().compareTo(roundTripped.getPrice()));
      assertEquals(original.getSpots().size(), roundTripped.getSpots().size());
    }
  }
}
