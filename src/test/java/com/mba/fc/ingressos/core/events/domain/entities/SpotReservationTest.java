package com.mba.fc.ingressos.core.events.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SpotReservation")
class SpotReservationTest {

  @Nested
  @DisplayName("Constructor")
  class Construction {

    @Test
    @DisplayName("given an EventSpotId, should reuse the same instance as identity")
    void shouldReuseEventSpotIdAsIdentity() {
      EventSpotId spotId = new EventSpotId();
      SpotReservation reservation = new SpotReservation(spotId, new CustomerId(), Instant.now());

      assertSame(spotId, reservation.getId());
    }

    @Test
    @DisplayName("given a String spot ID, should wrap it into an EventSpotId value object")
    void shouldWrapStringIntoEventSpotId() {
      String rawId = UUID.randomUUID().toString();
      SpotReservation reservation = new SpotReservation(rawId, new CustomerId(), Instant.now());

      assertInstanceOf(EventSpotId.class, reservation.getId());
      assertEquals(rawId, reservation.getId().getValue());
    }

    @Test
    @DisplayName("should store all provided fields correctly")
    void shouldStoreAllFields() {
      CustomerId customerId = new CustomerId();
      Instant reservedAt = Instant.now();
      SpotReservation reservation = new SpotReservation(new EventSpotId(), customerId, reservedAt);

      assertEquals(customerId, reservation.getCustomerId());
      assertEquals(reservedAt, reservation.getReservedAt());
    }
  }

  @Nested
  @DisplayName("Factory method create()")
  class FactoryMethod {

    @Test
    @DisplayName("should use the given spot as its identity")
    void shouldUseSpotAsIdentity() {
      EventSpotId spotId = new EventSpotId();
      SpotReservation reservation = SpotReservation.create(spotId, new CustomerId());

      assertEquals(spotId, reservation.getId());
    }

    @Test
    @DisplayName("should store the given customer")
    void shouldStoreCustomer() {
      CustomerId customerId = new CustomerId();
      SpotReservation reservation = SpotReservation.create(new EventSpotId(), customerId);

      assertEquals(customerId, reservation.getCustomerId());
    }

    @Test
    @DisplayName("should fill reservedAt automatically")
    void shouldFillReservedAtAutomatically() {
      SpotReservation reservation = SpotReservation.create(new EventSpotId(), new CustomerId());

      assertNotNull(reservation.getReservedAt());
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal when both reservations share the same spot ID")
    void shouldBeEqualWithSameSpotId() {
      EventSpotId spotId = new EventSpotId();
      SpotReservation a = new SpotReservation(spotId, new CustomerId(), Instant.now());
      SpotReservation b = new SpotReservation(spotId, new CustomerId(), Instant.now());

      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("should not be equal when reservations have different spot IDs")
    void shouldNotBeEqualWithDifferentSpotIds() {
      SpotReservation a = SpotReservation.create(new EventSpotId(), new CustomerId());
      SpotReservation b = SpotReservation.create(new EventSpotId(), new CustomerId());

      assertNotEquals(a, b);
    }
  }

  @Nested
  @DisplayName("toString")
  class ToStringTest {

    @Test
    @DisplayName("should contain the spot ID and customer ID")
    void shouldContainRelevantFields() {
      SpotReservation reservation = SpotReservation.create(new EventSpotId(), new CustomerId());

      assertTrue(reservation.toString().contains(reservation.getId().getValue()));
      assertTrue(reservation.toString().contains(reservation.getCustomerId().getValue()));
    }
  }
}
