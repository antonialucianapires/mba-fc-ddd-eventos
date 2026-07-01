package com.mba.fc.ingressos.core.events.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EventSpot")
class EventSpotTest {

  private static final String VALID_LOCATION = "A1";

  @Nested
  @DisplayName("Constructor")
  class Construction {

    @Test
    @DisplayName("given no ID, should generate a valid UUID automatically")
    void shouldGenerateIdWhenNotProvided() {
      EventSpot spot = new EventSpot(VALID_LOCATION, false, false);

      assertNotNull(spot.getId());
      assertDoesNotThrow(() -> UUID.fromString(spot.getId().getValue()));
    }

    @Test
    @DisplayName("given a String ID, should wrap it into an EventSpotId value object")
    void shouldWrapStringIntoEventSpotId() {
      String rawId = UUID.randomUUID().toString();
      EventSpot spot = new EventSpot(rawId, VALID_LOCATION, false, false);

      assertInstanceOf(EventSpotId.class, spot.getId());
      assertEquals(rawId, spot.getId().getValue());
    }

    @Test
    @DisplayName("given an EventSpotId, should reuse the same instance")
    void shouldReuseEventSpotId() {
      EventSpotId spotId = new EventSpotId();
      EventSpot spot = new EventSpot(spotId, VALID_LOCATION, false, false);

      assertSame(spotId, spot.getId());
    }

    @Test
    @DisplayName("should store all provided fields correctly")
    void shouldStoreAllFields() {
      EventSpot spot = new EventSpot(VALID_LOCATION, true, true);

      assertEquals(VALID_LOCATION, spot.getLocation());
      assertTrue(spot.isReserved());
      assertTrue(spot.isPublished());
    }
  }

  @Nested
  @DisplayName("Factory method create()")
  class FactoryMethod {

    @Test
    @DisplayName("should create a spot with a valid UUID as ID")
    void shouldCreateWithValidUuid() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);

      assertNotNull(spot.getId());
      assertDoesNotThrow(() -> UUID.fromString(spot.getId().getValue()));
    }

    @Test
    @DisplayName("should create a spot with isReserved set to false")
    void shouldCreateUnreserved() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);

      assertFalse(spot.isReserved());
    }

    @Test
    @DisplayName("should create a spot with isPublished set to false")
    void shouldCreateUnpublished() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);

      assertFalse(spot.isPublished());
    }

    @Test
    @DisplayName("should store the provided location")
    void shouldStoreLocation() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);

      assertEquals(VALID_LOCATION, spot.getLocation());
    }

    @Test
    @DisplayName("each call should produce a different ID")
    void shouldGenerateDistinctIds() {
      EventSpot a = EventSpot.create(VALID_LOCATION);
      EventSpot b = EventSpot.create(VALID_LOCATION);

      assertNotEquals(a.getId().getValue(), b.getId().getValue());
    }
  }

  @Nested
  @DisplayName("changeLocation")
  class ChangeLocation {

    @Test
    @DisplayName("should return a new instance with the updated location")
    void shouldReturnNewInstanceWithUpdatedLocation() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);
      EventSpot changed = spot.changeLocation("B2");

      assertNotSame(spot, changed);
      assertEquals("B2", changed.getLocation());
    }

    @Test
    @DisplayName("should preserve the same ID")
    void shouldPreserveId() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);
      EventSpot changed = spot.changeLocation("B2");

      assertEquals(spot.getId().getValue(), changed.getId().getValue());
    }

    @Test
    @DisplayName("should preserve isReserved and isPublished")
    void shouldPreserveOtherFields() {
      EventSpot spot = new EventSpot(VALID_LOCATION, true, true);
      EventSpot changed = spot.changeLocation("B2");

      assertTrue(changed.isReserved());
      assertTrue(changed.isPublished());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);
      spot.changeLocation("B2");

      assertEquals(VALID_LOCATION, spot.getLocation());
    }
  }

  @Nested
  @DisplayName("reserve")
  class Reserve {

    @Test
    @DisplayName("should return a new instance with isReserved set to true")
    void shouldReturnNewInstanceWithIsReservedTrue() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);
      EventSpot reserved = spot.reserve();

      assertNotSame(spot, reserved);
      assertTrue(reserved.isReserved());
    }

    @Test
    @DisplayName("should preserve ID, location and isPublished")
    void shouldPreserveOtherFields() {
      EventSpot spot = new EventSpot(VALID_LOCATION, false, true);
      EventSpot reserved = spot.reserve();

      assertEquals(spot.getId().getValue(), reserved.getId().getValue());
      assertEquals(VALID_LOCATION, reserved.getLocation());
      assertTrue(reserved.isPublished());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);
      spot.reserve();

      assertFalse(spot.isReserved());
    }
  }

  @Nested
  @DisplayName("publish")
  class Publish {

    @Test
    @DisplayName("should return a new instance with isPublished set to true")
    void shouldReturnNewInstanceWithIsPublishedTrue() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);
      EventSpot published = spot.publish();

      assertNotSame(spot, published);
      assertTrue(published.isPublished());
    }

    @Test
    @DisplayName("should preserve ID, location and isReserved")
    void shouldPreserveOtherFields() {
      EventSpot spot = new EventSpot(VALID_LOCATION, true, false);
      EventSpot published = spot.publish();

      assertEquals(spot.getId().getValue(), published.getId().getValue());
      assertEquals(VALID_LOCATION, published.getLocation());
      assertTrue(published.isReserved());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);
      spot.publish();

      assertFalse(spot.isPublished());
    }
  }

  @Nested
  @DisplayName("unpublish")
  class Unpublish {

    @Test
    @DisplayName("should return a new instance with isPublished set to false")
    void shouldReturnNewInstanceWithIsPublishedFalse() {
      EventSpot spot = new EventSpot(VALID_LOCATION, false, true);
      EventSpot unpublished = spot.unpublish();

      assertNotSame(spot, unpublished);
      assertFalse(unpublished.isPublished());
    }

    @Test
    @DisplayName("should preserve ID, location and isReserved")
    void shouldPreserveOtherFields() {
      EventSpot spot = new EventSpot(VALID_LOCATION, true, true);
      EventSpot unpublished = spot.unpublish();

      assertEquals(spot.getId().getValue(), unpublished.getId().getValue());
      assertEquals(VALID_LOCATION, unpublished.getLocation());
      assertTrue(unpublished.isReserved());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      EventSpot spot = new EventSpot(VALID_LOCATION, false, true);
      spot.unpublish();

      assertTrue(spot.isPublished());
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal when both spots share the same ID regardless of other fields")
    void shouldBeEqualWithSameId() {
      String id = UUID.randomUUID().toString();
      EventSpot a = new EventSpot(id, "A1", false, false);
      EventSpot b = new EventSpot(id, "B2", true, true);

      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("should not be equal when spots have different IDs")
    void shouldNotBeEqualWithDifferentIds() {
      EventSpot a = EventSpot.create(VALID_LOCATION);
      EventSpot b = EventSpot.create(VALID_LOCATION);

      assertNotEquals(a, b);
    }

    @Test
    @DisplayName("should be equal to itself")
    void shouldBeEqualToItself() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);

      assertEquals(spot, spot);
    }
  }

  @Nested
  @DisplayName("toString")
  class ToStringTest {

    @Test
    @DisplayName("should contain the spot ID and location")
    void shouldContainRelevantFields() {
      EventSpot spot = EventSpot.create(VALID_LOCATION);

      assertTrue(spot.toString().contains(spot.getId().getValue()));
      assertTrue(spot.toString().contains(VALID_LOCATION));
    }
  }
}
