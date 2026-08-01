package com.mba.fc.ingressos.core.events.infra.db.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.events.domain.entities.SpotReservation;
import com.mba.fc.ingressos.core.events.infra.db.schemas.CustomerSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.SpotReservationSchema;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SpotReservationMapper")
class SpotReservationMapperTest {

  private static final String VALID_SPOT_ID = UUID.randomUUID().toString();
  private static final String VALID_CUSTOMER_ID = UUID.randomUUID().toString();
  private static final Instant VALID_RESERVED_AT = Instant.parse("2026-07-30T10:00:00Z");

  private final SpotReservationMapper mapper = new SpotReservationMapper();

  @Nested
  @DisplayName("toDomain(SpotReservationSchema)")
  class ToDomain {

    @Test
    @DisplayName("should wrap the spot_id into an EventSpotId used as the aggregate identity")
    void shouldWrapSpotIdIntoEventSpotId() {
      SpotReservationSchema schema =
          new SpotReservationSchema(
              VALID_SPOT_ID,
              VALID_RESERVED_AT,
              new CustomerSchema(VALID_CUSTOMER_ID, "52998224725", "John Doe"));

      SpotReservation reservation = mapper.toDomain(schema);

      assertInstanceOf(EventSpotId.class, reservation.getId());
      assertEquals(VALID_SPOT_ID, reservation.getId().getValue());
    }

    @Test
    @DisplayName("each schema field should survive the mapping independently")
    void shouldMapAllFields() {
      SpotReservationSchema schema =
          new SpotReservationSchema(
              VALID_SPOT_ID,
              VALID_RESERVED_AT,
              new CustomerSchema(VALID_CUSTOMER_ID, "52998224725", "John Doe"));

      SpotReservation reservation = mapper.toDomain(schema);

      assertEquals(VALID_CUSTOMER_ID, reservation.getCustomerId().getValue());
      assertEquals(VALID_RESERVED_AT, reservation.getReservedAt());
    }
  }

  @Nested
  @DisplayName("toSchema(SpotReservation)")
  class ToSchema {

    @Test
    @DisplayName("should use the reservation's EventSpotId as the schema's spot_id")
    void shouldMapSpotIdFromReservation() {
      SpotReservation reservation =
          new SpotReservation(
              new EventSpotId(VALID_SPOT_ID), new CustomerId(VALID_CUSTOMER_ID), VALID_RESERVED_AT);

      SpotReservationSchema schema = mapper.toSchema(reservation);

      assertEquals(VALID_SPOT_ID, schema.getSpotId());
    }

    @Test
    @DisplayName("each reservation field should survive the mapping independently")
    void shouldMapAllFields() {
      SpotReservation reservation =
          new SpotReservation(
              new EventSpotId(VALID_SPOT_ID), new CustomerId(VALID_CUSTOMER_ID), VALID_RESERVED_AT);

      SpotReservationSchema schema = mapper.toSchema(reservation);

      assertEquals(VALID_CUSTOMER_ID, schema.getCustomer().getId());
      assertEquals(VALID_RESERVED_AT, schema.getReservedAt());
    }
  }

  @Nested
  @DisplayName("Round-trip")
  class RoundTrip {

    @Test
    @DisplayName("toDomain(toSchema(reservation)) should preserve all fields")
    void domainToSchemaAndBackShouldPreserveFields() {
      SpotReservation original =
          new SpotReservation(
              new EventSpotId(VALID_SPOT_ID), new CustomerId(VALID_CUSTOMER_ID), VALID_RESERVED_AT);

      SpotReservation roundTripped = mapper.toDomain(mapper.toSchema(original));

      assertEquals(original.getId().getValue(), roundTripped.getId().getValue());
      assertEquals(original.getCustomerId().getValue(), roundTripped.getCustomerId().getValue());
      assertEquals(original.getReservedAt(), roundTripped.getReservedAt());
    }
  }
}
