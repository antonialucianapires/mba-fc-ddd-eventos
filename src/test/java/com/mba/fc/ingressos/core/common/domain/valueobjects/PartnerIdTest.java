package com.mba.fc.ingressos.core.common.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PartnerId")
class PartnerIdTest {

    @Nested
    @DisplayName("Constructor")
    class Construction {

        @Test
        @DisplayName("should generate a valid UUID when no value is provided")
        void shouldGenerateValidUuid() {
            PartnerId id = new PartnerId();

            assertNotNull(id.getValue());
            assertDoesNotThrow(() -> UUID.fromString(id.getValue()));
        }

        @Test
        @DisplayName("should accept a valid UUID string")
        void shouldAcceptValidUuidString() {
            String raw = UUID.randomUUID().toString();
            PartnerId id = new PartnerId(raw);

            assertEquals(raw, id.getValue());
        }

        @Test
        @DisplayName("should throw when UUID string is invalid")
        void shouldThrowForInvalidUuid() {
            assertThrows(IllegalArgumentException.class, () -> new PartnerId("invalid-uuid"));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when both have the same UUID value")
        void shouldBeEqualWithSameValue() {
            String raw = UUID.randomUUID().toString();
            PartnerId a = new PartnerId(raw);
            PartnerId b = new PartnerId(raw);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("should not be equal when values differ")
        void shouldNotBeEqualWithDifferentValues() {
            PartnerId a = new PartnerId();
            PartnerId b = new PartnerId();

            assertNotEquals(a, b);
        }
    }
}
