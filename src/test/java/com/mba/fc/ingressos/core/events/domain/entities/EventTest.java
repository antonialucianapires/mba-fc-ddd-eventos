package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
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

        @Test
        @DisplayName("should start with empty sections when none are provided")
        void shouldStartWithEmptySections() {
            Event event = new Event(VALID_NAME, VALID_DESCRIPTION, VALID_DATE, false,
                    VALID_TOTAL_SPOTS, 0, VALID_PARTNER_ID);

            assertTrue(event.getSections().isEmpty());
        }

        @Test
        @DisplayName("should copy provided sections into the event")
        void shouldCopyProvidedSections() {
            EventSection s1 = EventSection.create("Pista", "Descrição", 100, new BigDecimal("50.00"));
            EventSection s2 = EventSection.create("VIP", "Descrição VIP", 50, new BigDecimal("200.00"));
            Set<EventSection> sections = new LinkedHashSet<>();
            sections.add(s1);
            sections.add(s2);

            Event event = new Event(VALID_NAME, VALID_DESCRIPTION, VALID_DATE, false,
                    VALID_TOTAL_SPOTS, 0, VALID_PARTNER_ID, sections);

            assertEquals(2, event.getSections().size());
            assertTrue(event.getSections().contains(s1));
            assertTrue(event.getSections().contains(s2));
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

        @Test
        @DisplayName("should create an event with empty sections")
        void shouldCreateWithEmptySections() {
            Event event = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE,
                    VALID_TOTAL_SPOTS, VALID_PARTNER_ID);

            assertTrue(event.getSections().isEmpty());
        }
    }

    @Nested
    @DisplayName("Sections")
    class Sections {

        @Test
        @DisplayName("should maintain insertion order of sections")
        void shouldMaintainInsertionOrder() {
            EventSection first = EventSection.create("Pista", "Desc", 200, new BigDecimal("50.00"));
            EventSection second = EventSection.create("VIP", "Desc", 50, new BigDecimal("200.00"));
            EventSection third = EventSection.create("Camarote", "Desc", 20, new BigDecimal("500.00"));

            Set<EventSection> sections = new LinkedHashSet<>();
            sections.add(first);
            sections.add(second);
            sections.add(third);

            Event event = new Event(VALID_NAME, VALID_DESCRIPTION, VALID_DATE, false,
                    VALID_TOTAL_SPOTS, 0, VALID_PARTNER_ID, sections);

            Iterator<EventSection> it = event.getSections().iterator();
            assertSame(first, it.next());
            assertSame(second, it.next());
            assertSame(third, it.next());
        }

        @Test
        @DisplayName("should not add duplicate sections (same ID)")
        void shouldNotAddDuplicateSections() {
            EventSectionId sharedId = new EventSectionId();
            EventSection original = new EventSection(sharedId, "Pista", "Desc", false, 100, 0, new BigDecimal("50.00"));
            EventSection duplicate = new EventSection(sharedId, "Pista Alterada", "Desc", false, 100, 0, new BigDecimal("50.00"));

            Set<EventSection> sections = new LinkedHashSet<>();
            sections.add(original);
            sections.add(duplicate);

            Event event = new Event(VALID_NAME, VALID_DESCRIPTION, VALID_DATE, false,
                    VALID_TOTAL_SPOTS, 0, VALID_PARTNER_ID, sections);

            assertEquals(1, event.getSections().size());
        }

        @Test
        @DisplayName("getSections should return an unmodifiable view")
        void shouldReturnUnmodifiableSet() {
            Event event = Event.create(VALID_NAME, VALID_DESCRIPTION, VALID_DATE,
                    VALID_TOTAL_SPOTS, VALID_PARTNER_ID);

            EventSection section = EventSection.create("Pista", "Desc", 100, new BigDecimal("50.00"));
            assertThrows(UnsupportedOperationException.class, () -> event.getSections().add(section));
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
