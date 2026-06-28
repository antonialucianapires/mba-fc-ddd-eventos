package com.mba.fc.ingressos.core.common.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EventSpotId")
class EventSpotIdTest {

  @Nested
  @DisplayName("Constructor")
  class Construction {

    @Test
    @DisplayName("should generate a valid UUID when no value is provided")
    void shouldGenerateValidUuid() {
      EventSpotId id = new EventSpotId();

      assertNotNull(id.getValue());
      assertDoesNotThrow(() -> UUID.fromString(id.getValue()));
    }

    @Test
    @DisplayName("should accept a valid UUID string")
    void shouldAcceptValidUuidString() {
      String raw = UUID.randomUUID().toString();
      EventSpotId id = new EventSpotId(raw);

      assertEquals(raw, id.getValue());
    }

    @Test
    @DisplayName("should throw when UUID string is invalid")
    void shouldThrowForInvalidUuid() {
      assertThrows(IllegalArgumentException.class, () -> new EventSpotId("invalid-uuid"));
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal when both have the same UUID value")
    void shouldBeEqualWithSameValue() {
      String raw = UUID.randomUUID().toString();
      EventSpotId a = new EventSpotId(raw);
      EventSpotId b = new EventSpotId(raw);

      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("should not be equal when values differ")
    void shouldNotBeEqualWithDifferentValues() {
      EventSpotId a = new EventSpotId();
      EventSpotId b = new EventSpotId();

      assertNotEquals(a, b);
    }
  }
}
