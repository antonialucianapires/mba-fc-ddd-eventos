package com.mba.fc.ingressos.core.common.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EventId")
class EventIdTest {

  @Nested
  @DisplayName("Constructor")
  class Construction {

    @Test
    @DisplayName("should generate a valid UUID when no value is provided")
    void shouldGenerateValidUuid() {
      EventId id = new EventId();

      assertNotNull(id.getValue());
      assertDoesNotThrow(() -> UUID.fromString(id.getValue()));
    }

    @Test
    @DisplayName("should accept a valid UUID string")
    void shouldAcceptValidUuidString() {
      String raw = UUID.randomUUID().toString();
      EventId id = new EventId(raw);

      assertEquals(raw, id.getValue());
    }

    @Test
    @DisplayName("should throw when UUID string is invalid")
    void shouldThrowForInvalidUuid() {
      assertThrows(IllegalArgumentException.class, () -> new EventId("invalid-uuid"));
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal when both have the same UUID value")
    void shouldBeEqualWithSameValue() {
      String raw = UUID.randomUUID().toString();
      EventId a = new EventId(raw);
      EventId b = new EventId(raw);

      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("should not be equal when values differ")
    void shouldNotBeEqualWithDifferentValues() {
      EventId a = new EventId();
      EventId b = new EventId();

      assertNotEquals(a, b);
    }
  }
}
