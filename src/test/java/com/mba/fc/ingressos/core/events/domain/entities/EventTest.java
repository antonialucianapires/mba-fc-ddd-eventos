package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Event")
class EventTest {

    private static final String VALID_NAME = "Show de Rock";
    private static final String VALID_DESCRIPTION = "Um grande show";
    private static final LocalDate VALID_DATE = LocalDate.of(2026, 12, 31);
    private static final int VALID_TOTAL_SPOTS = 100;
    private static final PartnerId VALID_PARTNER_ID = new PartnerId();

    @Nested
    @DisplayName("Constructor")
    class Construction {

        @Test
        @DisplayName("given no ID, should generate a valid UUID automatically")
        void shouldGenerateIdWhenNotProvided() {
            Event event = new Event(VALID_NAME, VALID_DESCRIPTION, VALID_DATE, false,
                    VALID_TOTAL_SPOTS, 0, VALID_PARTNER_ID);

            assertNotNull(event.getId());
            assertDoesNotThrow(() -> UUID.fromString(event.getId().getValue()));
        }

        @Test
        @DisplayName("given a String ID, should wrap it into an EventId value object")
        void shouldWrapStringIntoEventId() {
            String rawId = UUID.randomUUID().toString();
            Event event = new Event(rawId, VALID_NAME, VALID_DESCRIPTION, VALID_DATE, false,
                    VALID_TOTAL_SPOTS, 0, VALID_PARTNER_ID);

            assertInstanceOf(EventId.class, event.getId());
            assertEquals(rawId, event.getId().getValue());
        }

        @Test
        @DisplayName("given an EventId, should reuse the same instance")
        void shouldReuseEventId() {
            EventId eventId = new EventId();
            Event event = new Event(eventId, VALID_NAME, VALID_DESCRIPTION, VALID_DATE, false,
                    VALID_TOTAL_SPOTS, 0, VALID_PARTNER_ID);

            assertSame(eventId, event.getId());
        }

        @Test
        @DisplayName("should store all provided fields correctly")
        void shouldStoreAllFields() {
            Event event = new Event(VALID_NAME, VALID_DESCRIPTION, VALID_DATE, true,
                    VALID_TOTAL_SPOTS, 10, VALID_PARTNER_ID);

            assertEquals(VALID_NAME, event.getName());
            assertEquals(VALID_DESCRIPTION, event.getDescription());
            assertEquals(VALID_DATE, event.getDate());
            assertTrue(event.isPublished());
            assertEquals(VALID_TOTAL_SPOTS, event.getTotalSpots());
            assertEquals(10, event.getTotalSpotsReserved());
            assertSame(VALID_PARTNER_ID, event.getPartnerId());
        }
    }

    @Nested
    @DisplayName("Factory method create()")
    class FactoryMethod {

        @Test
        @DisplayName("should create an event with a valid UUID as ID")
        void shouldCreateWithValidUuid() {
            Event event = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE,
                    VALID_TOTAL_SPOTS, VALID_PARTNER_ID);

            assertNotNull(event.getId());
            assertDoesNotThrow(() -> UUID.fromString(event.getId().getValue()));
        }

        @Test
        @DisplayName("should create an event with isPublished set to false")
        void shouldCreateUnpublished() {
            Event event = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE,
                    VALID_TOTAL_SPOTS, VALID_PARTNER_ID);

            assertFalse(event.isPublished());
        }

        @Test
        @DisplayName("should create an event with totalSpotsReserved set to zero")
        void shouldCreateWithZeroReservedSpots() {
            Event event = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE,
                    VALID_TOTAL_SPOTS, VALID_PARTNER_ID);

            assertEquals(0, event.getTotalSpotsReserved());
        }

        @Test
        @DisplayName("should store name, description, date, totalSpots and partnerId")
        void shouldStoreProvidedFields() {
            Event event = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE,
                    VALID_TOTAL_SPOTS, VALID_PARTNER_ID);

            assertEquals(VALID_NAME, event.getName());
            assertEquals(VALID_DESCRIPTION, event.getDescription());
            assertEquals(VALID_DATE, event.getDate());
            assertEquals(VALID_TOTAL_SPOTS, event.getTotalSpots());
            assertSame(VALID_PARTNER_ID, event.getPartnerId());
        }

        @Test
        @DisplayName("each call should produce a different ID")
        void shouldGenerateDistinctIds() {
            Event a = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE,
                    VALID_TOTAL_SPOTS, VALID_PARTNER_ID);
            Event b = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE,
                    VALID_TOTAL_SPOTS, VALID_PARTNER_ID);

            assertNotEquals(a.getId().getValue(), b.getId().getValue());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when both events share the same ID regardless of other fields")
        void shouldBeEqualWithSameId() {
            String id = UUID.randomUUID().toString();
            Event a = new Event(id, "Name A", "Desc A", VALID_DATE, false, 50, 0, VALID_PARTNER_ID);
            Event b = new Event(id, "Name B", "Desc B", VALID_DATE, true, 200, 10, VALID_PARTNER_ID);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("should not be equal when events have different IDs")
        void shouldNotBeEqualWithDifferentIds() {
            Event a = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE, VALID_TOTAL_SPOTS, VALID_PARTNER_ID);
            Event b = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE, VALID_TOTAL_SPOTS, VALID_PARTNER_ID);

            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            Event event = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE, VALID_TOTAL_SPOTS, VALID_PARTNER_ID);

            assertEquals(event, event);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("should contain the event ID, name, date and partnerId")
        void shouldContainRelevantFields() {
            Event event = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE, VALID_TOTAL_SPOTS, VALID_PARTNER_ID);

            assertTrue(event.toString().contains(event.getId().getValue()));
            assertTrue(event.toString().contains(VALID_NAME));
            assertTrue(event.toString().contains(VALID_DATE.toString()));
            assertTrue(event.toString().contains(VALID_PARTNER_ID.getValue()));
        }
    }
}
