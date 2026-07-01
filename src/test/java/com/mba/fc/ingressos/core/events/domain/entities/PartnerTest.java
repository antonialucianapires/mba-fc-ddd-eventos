package com.mba.fc.ingressos.core.events.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.commands.CreateEventCommand;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
  @DisplayName("changeName")
  class ChangeName {

    @Test
    @DisplayName("should return a new Partner instance with the updated name")
    void shouldReturnNewInstance() {
      Partner original = Partner.create(VALID_NAME);
      Partner renamed = original.changeName("Novo Nome");

      assertNotSame(original, renamed);
      assertEquals("Novo Nome", renamed.getName());
    }

    @Test
    @DisplayName("should preserve the same ID on the returned instance")
    void shouldPreserveId() {
      Partner original = Partner.create(VALID_NAME);
      Partner renamed = original.changeName("Novo Nome");

      assertEquals(original.getId().getValue(), renamed.getId().getValue());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      Partner original = Partner.create(VALID_NAME);
      original.changeName("Novo Nome");

      assertEquals(VALID_NAME, original.getName());
    }
  }

  @Nested
  @DisplayName("initEvent")
  class InitEvent {

    private static final CreateEventCommand VALID_EVENT_COMMAND =
        new CreateEventCommand(
            "Show de Rock", "Um grande show", LocalDate.of(2026, 12, 31), 100, new PartnerId());

    @Test
    @DisplayName("should return an Event with fields from the command")
    void shouldReturnEventWithCommandFields() {
      Partner partner = Partner.create(VALID_NAME);
      Event event = partner.initEvent(VALID_EVENT_COMMAND);

      assertEquals("Show de Rock", event.getName());
      assertEquals("Um grande show", event.getDescription());
      assertEquals(LocalDate.of(2026, 12, 31), event.getDate());
      assertEquals(100, event.getTotalSpots());
    }

    @Test
    @DisplayName("should set the event partnerId to this partner's ID, ignoring command partnerId")
    void shouldUsePartnerOwnId() {
      Partner partner = Partner.create(VALID_NAME);
      Event event = partner.initEvent(VALID_EVENT_COMMAND);

      assertEquals(partner.getId().getValue(), event.getPartnerId().getValue());
    }

    @Test
    @DisplayName("should create the event with isPublished set to false")
    void shouldCreateUnpublished() {
      Partner partner = Partner.create(VALID_NAME);
      Event event = partner.initEvent(VALID_EVENT_COMMAND);

      assertFalse(event.isPublished());
    }

    @Test
    @DisplayName("should create the event with empty sections")
    void shouldCreateWithEmptySections() {
      Partner partner = Partner.create(VALID_NAME);
      Event event = partner.initEvent(VALID_EVENT_COMMAND);

      assertTrue(event.getSections().isEmpty());
    }

    @Test
    @DisplayName("each call should produce a different event ID")
    void shouldGenerateDistinctEventIds() {
      Partner partner = Partner.create(VALID_NAME);
      Event a = partner.initEvent(VALID_EVENT_COMMAND);
      Event b = partner.initEvent(VALID_EVENT_COMMAND);

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
