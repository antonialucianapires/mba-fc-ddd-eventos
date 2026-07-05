package com.mba.fc.ingressos.core.events.infra.db.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.events.domain.entities.EventSpot;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSectionSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSpotSchema;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EventSpotMapper")
class EventSpotMapperTest {

  private static final String VALID_ID = UUID.randomUUID().toString();
  private static final String VALID_LOCATION = "Spot 1";

  private final EventSpotMapper mapper = new EventSpotMapper();

  @Nested
  @DisplayName("toDomain(EventSpotSchema)")
  class ToDomain {

    @Test
    @DisplayName("should wrap the schema ID into an EventSpotId value object")
    void shouldWrapIdIntoEventSpotId() {
      EventSpotSchema schema = new EventSpotSchema(VALID_ID, VALID_LOCATION, false, false, null);

      EventSpot spot = mapper.toDomain(schema);

      assertInstanceOf(EventSpotId.class, spot.getId());
      assertEquals(VALID_ID, spot.getId().getValue());
    }

    @Test
    @DisplayName("each schema field should survive the mapping independently")
    void shouldMapAllFields() {
      EventSpotSchema schema = new EventSpotSchema(VALID_ID, VALID_LOCATION, true, true, null);

      EventSpot spot = mapper.toDomain(schema);

      assertEquals(VALID_ID, spot.getId().getValue());
      assertEquals(VALID_LOCATION, spot.getLocation());
      assertTrue(spot.isReserved());
      assertTrue(spot.isPublished());
    }
  }

  @Nested
  @DisplayName("toSchema(EventSpot, EventSectionSchema)")
  class ToSchema {

    @Test
    @DisplayName("should return an EventSpotSchema with the same ID as the spot")
    void shouldMapIdFromSpot() {
      EventSpot spot = new EventSpot(VALID_ID, VALID_LOCATION, false, false);

      EventSpotSchema schema = mapper.toSchema(spot, null);

      assertEquals(VALID_ID, schema.getId());
    }

    @Test
    @DisplayName("should store the spot ID as a plain String, not a UUID object")
    void shouldStoreIdAsString() {
      EventSpot spot = new EventSpot(VALID_ID, VALID_LOCATION, false, false);

      EventSpotSchema schema = mapper.toSchema(spot, null);

      assertInstanceOf(String.class, schema.getId());
    }

    @Test
    @DisplayName("each spot field should survive the mapping independently")
    void shouldMapAllFields() {
      EventSpot spot = new EventSpot(VALID_ID, VALID_LOCATION, true, true);

      EventSpotSchema schema = mapper.toSchema(spot, null);

      assertEquals(VALID_ID, schema.getId());
      assertEquals(VALID_LOCATION, schema.getLocation());
      assertTrue(schema.isReserved());
      assertTrue(schema.isPublished());
    }

    @Test
    @DisplayName("should associate the schema with the given section")
    void shouldAssociateSection() {
      EventSpot spot = new EventSpot(VALID_ID, VALID_LOCATION, false, false);
      EventSectionSchema section = new EventSectionSchema();

      EventSpotSchema schema = mapper.toSchema(spot, section);

      assertSame(section, schema.getEventSection());
    }
  }

  @Nested
  @DisplayName("Round-trip")
  class RoundTrip {

    @Test
    @DisplayName("toDomain(toSchema(spot)) should preserve all fields")
    void domainToSchemaAndBackShouldPreserveFields() {
      EventSpot original = new EventSpot(VALID_ID, VALID_LOCATION, true, true);

      EventSpot roundTripped = mapper.toDomain(mapper.toSchema(original, null));

      assertEquals(original.getId().getValue(), roundTripped.getId().getValue());
      assertEquals(original.getLocation(), roundTripped.getLocation());
      assertEquals(original.isReserved(), roundTripped.isReserved());
      assertEquals(original.isPublished(), roundTripped.isPublished());
    }
  }
}
