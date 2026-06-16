package com.mba.fc.ingressos.core.common.domain.valueobjects;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Uuid")
class UuidTest {

    @Nested
    @DisplayName("Constructor")
    class Construction {

        @Test
        @DisplayName("should generate a valid UUID when no value is provided")
        void shouldGenerateValidUuid() {
            Uuid uuid = new Uuid() {};

            assertNotNull(uuid.getValue());
            assertDoesNotThrow(() -> UUID.fromString(uuid.getValue()));
        }

        @Test
        @DisplayName("should accept a valid UUID string")
        void shouldAcceptValidUuidString() {
            String raw = UUID.randomUUID().toString();
            Uuid uuid = new Uuid(raw) {};

            assertEquals(raw, uuid.getValue());
        }

        @Test
        @DisplayName("should throw when UUID string is invalid")
        void shouldThrowForInvalidUuid() {
            assertThrows(IllegalArgumentException.class, () -> new Uuid("invalid-uuid") {});
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when both have the same UUID value")
        void shouldBeEqualWithSameValue() {
            String raw = UUID.randomUUID().toString();
            CustomerId a = new CustomerId(raw);
            CustomerId b = new CustomerId(raw);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("should not be equal when values differ")
        void shouldNotBeEqualWithDifferentValues() {
            CustomerId a = new CustomerId();
            CustomerId b = new CustomerId();

            assertNotEquals(a, b);
        }
    }
}