package com.mba.fc.ingressos.core.events.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EventSection")
class EventSectionTest {

  private static final String VALID_NAME = "Pista";
  private static final String VALID_DESCRIPTION = "Seção pista";
  private static final int VALID_TOTAL_SPOTS = 200;
  private static final BigDecimal VALID_PRICE = new BigDecimal("150.00");

  @Nested
  @DisplayName("Constructor")
  class Construction {

    @Test
    @DisplayName("given no ID, should generate a valid UUID automatically")
    void shouldGenerateIdWhenNotProvided() {
      EventSection section =
          new EventSection(
              VALID_NAME,
              VALID_DESCRIPTION,
              false,
              VALID_TOTAL_SPOTS,
              0,
              VALID_PRICE,
              new LinkedHashSet<>());

      assertNotNull(section.getId());
      assertDoesNotThrow(() -> UUID.fromString(section.getId().getValue()));
    }

    @Test
    @DisplayName("given a String ID, should wrap it into an EventSectionId value object")
    void shouldWrapStringIntoEventSectionId() {
      String rawId = UUID.randomUUID().toString();
      EventSection section =
          new EventSection(
              rawId,
              VALID_NAME,
              VALID_DESCRIPTION,
              false,
              VALID_TOTAL_SPOTS,
              0,
              VALID_PRICE,
              new LinkedHashSet<>());

      assertInstanceOf(EventSectionId.class, section.getId());
      assertEquals(rawId, section.getId().getValue());
    }

    @Test
    @DisplayName("given an EventSectionId, should reuse the same instance")
    void shouldReuseEventSectionId() {
      EventSectionId sectionId = new EventSectionId();
      EventSection section =
          new EventSection(
              sectionId,
              VALID_NAME,
              VALID_DESCRIPTION,
              false,
              VALID_TOTAL_SPOTS,
              0,
              VALID_PRICE,
              new LinkedHashSet<>());

      assertSame(sectionId, section.getId());
    }

    @Test
    @DisplayName("should store all provided fields correctly")
    void shouldStoreAllFields() {
      EventSection section =
          new EventSection(
              VALID_NAME,
              VALID_DESCRIPTION,
              true,
              VALID_TOTAL_SPOTS,
              50,
              VALID_PRICE,
              new LinkedHashSet<>());

      assertEquals(VALID_NAME, section.getName());
      assertEquals(VALID_DESCRIPTION, section.getDescription());
      assertTrue(section.isPublished());
      assertEquals(VALID_TOTAL_SPOTS, section.getTotalSpots());
      assertEquals(50, section.getTotalSpotsReserved());
      assertEquals(VALID_PRICE, section.getPrice());
    }

    @Test
    @DisplayName("should copy provided spots into the section")
    void shouldCopyProvidedSpots() {
      EventSpot s1 = EventSpot.create("A1");
      EventSpot s2 = EventSpot.create("A2");
      Set<EventSpot> spots = new LinkedHashSet<>();
      spots.add(s1);
      spots.add(s2);

      EventSection section =
          new EventSection(
              VALID_NAME, VALID_DESCRIPTION, false, VALID_TOTAL_SPOTS, 0, VALID_PRICE, spots);

      assertEquals(2, section.getSpots().size());
      assertTrue(section.getSpots().contains(s1));
      assertTrue(section.getSpots().contains(s2));
    }
  }

  @Nested
  @DisplayName("Factory method create()")
  class FactoryMethod {

    @Test
    @DisplayName("should create a section with a valid UUID as ID")
    void shouldCreateWithValidUuid() {
      EventSection section =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);

      assertNotNull(section.getId());
      assertDoesNotThrow(() -> UUID.fromString(section.getId().getValue()));
    }

    @Test
    @DisplayName("should create a section with isPublished set to false")
    void shouldCreateUnpublished() {
      EventSection section =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);

      assertFalse(section.isPublished());
    }

    @Test
    @DisplayName("should create a section with totalSpotsReserved set to zero")
    void shouldCreateWithZeroReservedSpots() {
      EventSection section =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);

      assertEquals(0, section.getTotalSpotsReserved());
    }

    @Test
    @DisplayName("should store name, description, totalSpots and price")
    void shouldStoreProvidedFields() {
      EventSection section =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);

      assertEquals(VALID_NAME, section.getName());
      assertEquals(VALID_DESCRIPTION, section.getDescription());
      assertEquals(VALID_TOTAL_SPOTS, section.getTotalSpots());
      assertEquals(VALID_PRICE, section.getPrice());
    }

    @Test
    @DisplayName("each call should produce a different ID")
    void shouldGenerateDistinctIds() {
      EventSection a =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);
      EventSection b =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);

      assertNotEquals(a.getId().getValue(), b.getId().getValue());
    }

    @Test
    @DisplayName("should create a section with empty spots")
    void shouldCreateWithEmptySpots() {
      EventSection section =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);

      assertTrue(section.getSpots().isEmpty());
    }
  }

  @Nested
  @DisplayName("Spots")
  class Spots {

    @Test
    @DisplayName("should maintain insertion order of spots")
    void shouldMaintainInsertionOrder() {
      EventSpot first = EventSpot.create("A1");
      EventSpot second = EventSpot.create("A2");
      EventSpot third = EventSpot.create("A3");

      Set<EventSpot> spots = new LinkedHashSet<>();
      spots.add(first);
      spots.add(second);
      spots.add(third);

      EventSection section =
          new EventSection(
              VALID_NAME, VALID_DESCRIPTION, false, VALID_TOTAL_SPOTS, 0, VALID_PRICE, spots);

      Iterator<EventSpot> it = section.getSpots().iterator();
      assertSame(first, it.next());
      assertSame(second, it.next());
      assertSame(third, it.next());
    }

    @Test
    @DisplayName("should not add duplicate spots (same ID)")
    void shouldNotAddDuplicateSpots() {
      EventSpotId sharedId = new EventSpotId();
      EventSpot original = new EventSpot(sharedId, "A1", false, false);
      EventSpot duplicate = new EventSpot(sharedId, "A1-alt", false, false);

      Set<EventSpot> spots = new LinkedHashSet<>();
      spots.add(original);
      spots.add(duplicate);

      EventSection section =
          new EventSection(
              VALID_NAME, VALID_DESCRIPTION, false, VALID_TOTAL_SPOTS, 0, VALID_PRICE, spots);

      assertEquals(1, section.getSpots().size());
    }

    @Test
    @DisplayName("getSpots should return an unmodifiable view")
    void shouldReturnUnmodifiableSet() {
      EventSection section =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);

      EventSpot spot = EventSpot.create("A1");
      assertThrows(UnsupportedOperationException.class, () -> section.getSpots().add(spot));
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal when both sections share the same ID regardless of other fields")
    void shouldBeEqualWithSameId() {
      String id = UUID.randomUUID().toString();
      EventSection a =
          new EventSection(
              id, "VIP", "Desc A", true, 50, 0, new BigDecimal("500.00"), new LinkedHashSet<>());
      EventSection b =
          new EventSection(
              id,
              "Pista",
              "Desc B",
              false,
              200,
              10,
              new BigDecimal("100.00"),
              new LinkedHashSet<>());

      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("should not be equal when sections have different IDs")
    void shouldNotBeEqualWithDifferentIds() {
      EventSection a =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);
      EventSection b =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);

      assertNotEquals(a, b);
    }

    @Test
    @DisplayName("should be equal to itself")
    void shouldBeEqualToItself() {
      EventSection section =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);

      assertEquals(section, section);
    }
  }

  @Nested
  @DisplayName("toString")
  class ToStringTest {

    @Test
    @DisplayName("should contain the section ID, name and price")
    void shouldContainRelevantFields() {
      EventSection section =
          EventSection.create(VALID_NAME, VALID_DESCRIPTION, VALID_TOTAL_SPOTS, VALID_PRICE);

      assertTrue(section.toString().contains(section.getId().getValue()));
      assertTrue(section.toString().contains(VALID_NAME));
      assertTrue(section.toString().contains(VALID_PRICE.toString()));
    }
  }
}
