package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Partner")
class PartnerTest {

    private static final String VALID_NAME = "Acme Events";

    @Nested
    @DisplayName("Constructor")
    class Construction {

        @Test
        @DisplayName("given no ID, should generate a valid UUID automatically")
        void shouldGenerateIdWhenNotProvided() {
            Partner partner = new Partner(VALID_NAME);

            assertNotNull(partner.getId());
            assertNotNull(partner.getId().getValue());
            assertDoesNotThrow(() -> UUID.fromString(partner.getId().getValue()));
        }

        @Test
        @DisplayName("given a String ID, should wrap it into a PartnerId value object")
        void shouldWrapStringIntoPartnerId() {
            String rawId = UUID.randomUUID().toString();
            Partner partner = new Partner(rawId, VALID_NAME);

            assertInstanceOf(PartnerId.class, partner.getId());
            assertEquals(rawId, partner.getId().getValue());
        }

        @Test
        @DisplayName("given a PartnerId, should reuse the same instance")
        void shouldReusePartnerId() {
            PartnerId partnerId = new PartnerId();
            Partner partner = new Partner(partnerId, VALID_NAME);

            assertSame(partnerId, partner.getId());
        }

        @Test
        @DisplayName("given a name, should store it correctly")
        void shouldStoreName() {
            Partner partner = new Partner(VALID_NAME);

            assertEquals(VALID_NAME, partner.getName());
        }
    }

    @Nested
    @DisplayName("Factory method create()")
    class FactoryMethod {

        @Test
        @DisplayName("should create a partner with a valid UUID as ID")
        void shouldCreateWithValidUuid() {
            Partner partner = Partner.create(VALID_NAME);

            assertNotNull(partner.getId());
            assertDoesNotThrow(() -> UUID.fromString(partner.getId().getValue()));
        }

        @Test
        @DisplayName("should store the provided name")
        void shouldStoreName() {
            Partner partner = Partner.create(VALID_NAME);

            assertEquals(VALID_NAME, partner.getName());
        }

        @Test
        @DisplayName("each call should produce a different ID")
        void shouldGenerateDistinctIds() {
            Partner a = Partner.create(VALID_NAME);
            Partner b = Partner.create(VALID_NAME);

            assertNotEquals(a.getId().getValue(), b.getId().getValue());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when both partners share the same ID regardless of other fields")
        void shouldBeEqualWithSameId() {
            String id = UUID.randomUUID().toString();
            Partner a = new Partner(id, "Name A");
            Partner b = new Partner(id, "Name B");

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("should not be equal when partners have different IDs")
        void shouldNotBeEqualWithDifferentIds() {
            Partner a = new Partner(VALID_NAME);
            Partner b = new Partner(VALID_NAME);

            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            Partner partner = new Partner(VALID_NAME);

            assertEquals(partner, partner);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("should contain the partner ID and name")
        void shouldContainIdAndName() {
            Partner partner = Partner.create(VALID_NAME);

            assertTrue(partner.toString().contains(partner.getId().getValue()));
            assertTrue(partner.toString().contains(VALID_NAME));
        }
    }
}
