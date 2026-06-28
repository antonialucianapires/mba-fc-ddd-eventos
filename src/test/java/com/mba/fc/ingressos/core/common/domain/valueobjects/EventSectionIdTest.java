package com.mba.fc.ingressos.core.common.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EventSectionId")
class EventSectionIdTest {

  @Nested
  @DisplayName("Constructor")
  class Construction {

    @Test
    @DisplayName("should generate a valid UUID when no value is provided")
    void shouldGenerateValidUuid() {
      EventSectionId id = new EventSectionId();

      assertNotNull(id.getValue());
      assertDoesNotThrow(() -> UUID.fromString(id.getValue()));
    }

    @Test
    @DisplayName("should accept a valid UUID string")
    void shouldAcceptValidUuidString() {
      String raw = UUID.randomUUID().toString();
      EventSectionId id = new EventSectionId(raw);

      assertEquals(raw, id.getValue());
    }

    @Test
    @DisplayName("should throw when UUID string is invalid")
    void shouldThrowForInvalidUuid() {
      assertThrows(IllegalArgumentException.class, () -> new EventSectionId("invalid-uuid"));
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal when both have the same UUID value")
    void shouldBeEqualWithSameValue() {
      String raw = UUID.randomUUID().toString();
      EventSectionId a = new EventSectionId(raw);
      EventSectionId b = new EventSectionId(raw);

      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("should not be equal when values differ")
    void shouldNotBeEqualWithDifferentValues() {
      EventSectionId a = new EventSectionId();
      EventSectionId b = new EventSectionId();

      assertNotEquals(a, b);
    }
  }
}
