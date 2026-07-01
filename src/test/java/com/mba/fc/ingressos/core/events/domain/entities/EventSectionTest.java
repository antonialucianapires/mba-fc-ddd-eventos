package com.mba.fc.ingressos.core.events.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.events.domain.commands.AddSectionCommand;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EventSection")
class EventSectionTest {

  private static final String VALID_NAME = "Pista";
  private static final String VALID_DESCRIPTION = "Seção pista";
  private static final BigDecimal VALID_PRICE = new BigDecimal("150.00");
  private static final AddSectionCommand VALID_COMMAND =
      new AddSectionCommand(VALID_NAME, VALID_DESCRIPTION, 3, VALID_PRICE);

  @Nested
  @DisplayName("Constructor")
  class Construction {

    @Test
    @DisplayName("given no ID, should generate a valid UUID automatically")
    void shouldGenerateIdWhenNotProvided() {
      EventSection section =
          new EventSection(
              VALID_NAME, VALID_DESCRIPTION, false, 3, 0, VALID_PRICE, new LinkedHashSet<>());

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
              3,
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
              3,
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
              VALID_NAME, VALID_DESCRIPTION, true, 3, 1, VALID_PRICE, new LinkedHashSet<>());

      assertEquals(VALID_NAME, section.getName());
      assertEquals(VALID_DESCRIPTION, section.getDescription());
      assertTrue(section.isPublished());
      assertEquals(3, section.getTotalSpots());
      assertEquals(1, section.getTotalSpotsReserved());
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
          new EventSection(VALID_NAME, VALID_DESCRIPTION, false, 3, 0, VALID_PRICE, spots);

      assertEquals(2, section.getSpots().size());
      assertTrue(section.getSpots().contains(s1));
      assertTrue(section.getSpots().contains(s2));
    }
  }

  @Nested
  @DisplayName("Factory method create(AddSectionCommand)")
  class FactoryMethod {

    @Test
    @DisplayName("should create a section with a valid UUID as ID")
    void shouldCreateWithValidUuid() {
      EventSection section = EventSection.create(VALID_COMMAND);

      assertNotNull(section.getId());
      assertDoesNotThrow(() -> UUID.fromString(section.getId().getValue()));
    }

    @Test
    @DisplayName("should create a section with isPublished set to false")
    void shouldCreateUnpublished() {
      EventSection section = EventSection.create(VALID_COMMAND);

      assertFalse(section.isPublished());
    }

    @Test
    @DisplayName("should create a section with totalSpotsReserved set to zero")
    void shouldCreateWithZeroReservedSpots() {
      EventSection section = EventSection.create(VALID_COMMAND);

      assertEquals(0, section.getTotalSpotsReserved());
    }

    @Test
    @DisplayName("should store fields from the command")
    void shouldStoreCommandFields() {
      EventSection section = EventSection.create(VALID_COMMAND);

      assertEquals(VALID_NAME, section.getName());
      assertEquals(VALID_DESCRIPTION, section.getDescription());
      assertEquals(3, section.getTotalSpots());
      assertEquals(VALID_PRICE, section.getPrice());
    }

    @Test
    @DisplayName("each call should produce a different ID")
    void shouldGenerateDistinctIds() {
      EventSection a = EventSection.create(VALID_COMMAND);
      EventSection b = EventSection.create(VALID_COMMAND);

      assertNotEquals(a.getId().getValue(), b.getId().getValue());
    }

    @Test
    @DisplayName("should initialize exactly totalSpots spots")
    void shouldInitializeExactNumberOfSpots() {
      EventSection section =
          EventSection.create(new AddSectionCommand(VALID_NAME, VALID_DESCRIPTION, 5, VALID_PRICE));

      assertEquals(5, section.getSpots().size());
    }

    @Test
    @DisplayName("should initialize spots with sequential locations starting at 'Spot 1'")
    void shouldInitializeSpotsWithSequentialLocations() {
      EventSection section = EventSection.create(VALID_COMMAND);

      List<String> locations = section.getSpots().stream().map(EventSpot::getLocation).toList();
      assertEquals(List.of("Spot 1", "Spot 2", "Spot 3"), locations);
    }

    @Test
    @DisplayName("should initialize spots with isReserved and isPublished set to false")
    void shouldInitializeSpotsUnreservedAndUnpublished() {
      EventSection section = EventSection.create(VALID_COMMAND);

      section
          .getSpots()
          .forEach(
              spot -> {
                assertFalse(spot.isReserved());
                assertFalse(spot.isPublished());
              });
    }

    @Test
    @DisplayName("should initialize spots in insertion order")
    void shouldInitializeSpotsInOrder() {
      EventSection section = EventSection.create(VALID_COMMAND);

      Iterator<EventSpot> it = section.getSpots().iterator();
      assertEquals("Spot 1", it.next().getLocation());
      assertEquals("Spot 2", it.next().getLocation());
      assertEquals("Spot 3", it.next().getLocation());
    }

    @Test
    @DisplayName("should create no spots when totalSpots is zero")
    void shouldCreateNoSpotsWhenTotalSpotsIsZero() {
      EventSection section =
          EventSection.create(new AddSectionCommand(VALID_NAME, VALID_DESCRIPTION, 0, VALID_PRICE));

      assertTrue(section.getSpots().isEmpty());
    }
  }

  @Nested
  @DisplayName("changeName")
  class ChangeName {

    @Test
    @DisplayName("should return a new instance with the updated name")
    void shouldReturnNewInstanceWithUpdatedName() {
      EventSection section = EventSection.create(VALID_COMMAND);
      EventSection changed = section.changeName("VIP");

      assertNotSame(section, changed);
      assertEquals("VIP", changed.getName());
    }

    @Test
    @DisplayName("should preserve the same ID")
    void shouldPreserveId() {
      EventSection section = EventSection.create(VALID_COMMAND);
      EventSection changed = section.changeName("VIP");

      assertEquals(section.getId().getValue(), changed.getId().getValue());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      EventSection section = EventSection.create(VALID_COMMAND);
      section.changeName("VIP");

      assertEquals(VALID_NAME, section.getName());
    }
  }

  @Nested
  @DisplayName("changeDescription")
  class ChangeDescription {

    @Test
    @DisplayName("should return a new instance with the updated description")
    void shouldReturnNewInstanceWithUpdatedDescription() {
      EventSection section = EventSection.create(VALID_COMMAND);
      EventSection changed = section.changeDescription("Nova descrição");

      assertNotSame(section, changed);
      assertEquals("Nova descrição", changed.getDescription());
    }

    @Test
    @DisplayName("should preserve the same ID")
    void shouldPreserveId() {
      EventSection section = EventSection.create(VALID_COMMAND);
      EventSection changed = section.changeDescription("Nova descrição");

      assertEquals(section.getId().getValue(), changed.getId().getValue());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      EventSection section = EventSection.create(VALID_COMMAND);
      section.changeDescription("Nova descrição");

      assertEquals(VALID_DESCRIPTION, section.getDescription());
    }
  }

  @Nested
  @DisplayName("changePrice")
  class ChangePrice {

    @Test
    @DisplayName("should return a new instance with the updated price")
    void shouldReturnNewInstanceWithUpdatedPrice() {
      EventSection section = EventSection.create(VALID_COMMAND);
      BigDecimal newPrice = new BigDecimal("300.00");
      EventSection changed = section.changePrice(newPrice);

      assertNotSame(section, changed);
      assertEquals(newPrice, changed.getPrice());
    }

    @Test
    @DisplayName("should preserve the same ID")
    void shouldPreserveId() {
      EventSection section = EventSection.create(VALID_COMMAND);
      EventSection changed = section.changePrice(new BigDecimal("300.00"));

      assertEquals(section.getId().getValue(), changed.getId().getValue());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      EventSection section = EventSection.create(VALID_COMMAND);
      section.changePrice(new BigDecimal("300.00"));

      assertEquals(VALID_PRICE, section.getPrice());
    }
  }

  @Nested
  @DisplayName("publish")
  class Publish {

    @Test
    @DisplayName("should return a new instance with isPublished set to true")
    void shouldReturnNewInstanceWithIsPublishedTrue() {
      EventSection section = EventSection.create(VALID_COMMAND);
      EventSection published = section.publish();

      assertNotSame(section, published);
      assertTrue(published.isPublished());
    }

    @Test
    @DisplayName("should preserve ID and spots")
    void shouldPreserveIdAndSpots() {
      EventSection section = EventSection.create(VALID_COMMAND);
      EventSection published = section.publish();

      assertEquals(section.getId().getValue(), published.getId().getValue());
      assertEquals(section.getSpots().size(), published.getSpots().size());
    }

    @Test
    @DisplayName("should not change spot publication state")
    void shouldNotChangeSpotPublicationState() {
      EventSection section = EventSection.create(VALID_COMMAND);
      EventSection published = section.publish();

      published.getSpots().forEach(spot -> assertFalse(spot.isPublished()));
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      EventSection section = EventSection.create(VALID_COMMAND);
      section.publish();

      assertFalse(section.isPublished());
    }
  }

  @Nested
  @DisplayName("unpublish")
  class Unpublish {

    @Test
    @DisplayName("should return a new instance with isPublished set to false")
    void shouldReturnNewInstanceWithIsPublishedFalse() {
      EventSection section = EventSection.create(VALID_COMMAND).publish();
      EventSection unpublished = section.unpublish();

      assertNotSame(section, unpublished);
      assertFalse(unpublished.isPublished());
    }

    @Test
    @DisplayName("should preserve ID and spots")
    void shouldPreserveIdAndSpots() {
      EventSection section = EventSection.create(VALID_COMMAND).publish();
      EventSection unpublished = section.unpublish();

      assertEquals(section.getId().getValue(), unpublished.getId().getValue());
      assertEquals(section.getSpots().size(), unpublished.getSpots().size());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      EventSection section = EventSection.create(VALID_COMMAND).publish();
      section.unpublish();

      assertTrue(section.isPublished());
    }
  }

  @Nested
  @DisplayName("publishAll")
  class PublishAll {

    @Test
    @DisplayName("should return a new instance with isPublished set to true")
    void shouldPublishSection() {
      EventSection section = EventSection.create(VALID_COMMAND);
      EventSection result = section.publishAll();

      assertNotSame(section, result);
      assertTrue(result.isPublished());
    }

    @Test
    @DisplayName("should publish all spots")
    void shouldPublishAllSpots() {
      EventSection section = EventSection.create(VALID_COMMAND);
      EventSection result = section.publishAll();

      result.getSpots().forEach(spot -> assertTrue(spot.isPublished()));
    }

    @Test
    @DisplayName("should preserve ID and spot count")
    void shouldPreserveIdAndSpotCount() {
      EventSection section = EventSection.create(VALID_COMMAND);
      EventSection result = section.publishAll();

      assertEquals(section.getId().getValue(), result.getId().getValue());
      assertEquals(section.getSpots().size(), result.getSpots().size());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      EventSection section = EventSection.create(VALID_COMMAND);
      section.publishAll();

      assertFalse(section.isPublished());
      section.getSpots().forEach(spot -> assertFalse(spot.isPublished()));
    }
  }

  @Nested
  @DisplayName("unpublishAll")
  class UnpublishAll {

    @Test
    @DisplayName("should return a new instance with isPublished set to false")
    void shouldUnpublishSection() {
      EventSection section = EventSection.create(VALID_COMMAND).publishAll();
      EventSection result = section.unpublishAll();

      assertNotSame(section, result);
      assertFalse(result.isPublished());
    }

    @Test
    @DisplayName("should unpublish all spots")
    void shouldUnpublishAllSpots() {
      EventSection section = EventSection.create(VALID_COMMAND).publishAll();
      EventSection result = section.unpublishAll();

      result.getSpots().forEach(spot -> assertFalse(spot.isPublished()));
    }

    @Test
    @DisplayName("should preserve ID and spot count")
    void shouldPreserveIdAndSpotCount() {
      EventSection section = EventSection.create(VALID_COMMAND).publishAll();
      EventSection result = section.unpublishAll();

      assertEquals(section.getId().getValue(), result.getId().getValue());
      assertEquals(section.getSpots().size(), result.getSpots().size());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      EventSection section = EventSection.create(VALID_COMMAND).publishAll();
      section.unpublishAll();

      assertTrue(section.isPublished());
      section.getSpots().forEach(spot -> assertTrue(spot.isPublished()));
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
          new EventSection(VALID_NAME, VALID_DESCRIPTION, false, 3, 0, VALID_PRICE, spots);

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
          new EventSection(VALID_NAME, VALID_DESCRIPTION, false, 3, 0, VALID_PRICE, spots);

      assertEquals(1, section.getSpots().size());
    }

    @Test
    @DisplayName("getSpots should return an unmodifiable view")
    void shouldReturnUnmodifiableSet() {
      EventSection section =
          new EventSection(
              VALID_NAME, VALID_DESCRIPTION, false, 0, 0, VALID_PRICE, new LinkedHashSet<>());

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
      EventSection a = EventSection.create(VALID_COMMAND);
      EventSection b = EventSection.create(VALID_COMMAND);

      assertNotEquals(a, b);
    }

    @Test
    @DisplayName("should be equal to itself")
    void shouldBeEqualToItself() {
      EventSection section = EventSection.create(VALID_COMMAND);

      assertEquals(section, section);
    }
  }

  @Nested
  @DisplayName("toString")
  class ToStringTest {

    @Test
    @DisplayName("should contain the section ID, name and price")
    void shouldContainRelevantFields() {
      EventSection section = EventSection.create(VALID_COMMAND);

      assertTrue(section.toString().contains(section.getId().getValue()));
      assertTrue(section.toString().contains(VALID_NAME));
      assertTrue(section.toString().contains(VALID_PRICE.toString()));
    }
  }
}
