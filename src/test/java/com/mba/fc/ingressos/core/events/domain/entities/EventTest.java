package com.mba.fc.ingressos.core.events.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.commands.AddSectionCommand;
import com.mba.fc.ingressos.core.events.domain.commands.CreateEventCommand;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Event")
class EventTest {

  private static final String VALID_NAME = "Show de Rock";
  private static final String VALID_DESCRIPTION = "Um grande show";
  private static final LocalDate VALID_DATE = LocalDate.of(2026, 12, 31);
  private static final int VALID_TOTAL_SPOTS = 100;
  private static final PartnerId VALID_PARTNER_ID = new PartnerId();

  private static final CreateEventCommand VALID_COMMAND =
      new CreateEventCommand(
          VALID_NAME, VALID_DESCRIPTION, VALID_DATE, VALID_TOTAL_SPOTS, VALID_PARTNER_ID);

  private static final AddSectionCommand SECTION_COMMAND =
      new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00"));

  @Nested
  @DisplayName("Constructor")
  class Construction {

    @Test
    @DisplayName("given no ID, should generate a valid UUID automatically")
    void shouldGenerateIdWhenNotProvided() {
      Event event =
          new Event(
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              VALID_TOTAL_SPOTS,
              0,
              VALID_PARTNER_ID,
              new LinkedHashSet<>());

      assertNotNull(event.getId());
      assertDoesNotThrow(() -> UUID.fromString(event.getId().getValue()));
    }

    @Test
    @DisplayName("given a String ID, should wrap it into an EventId value object")
    void shouldWrapStringIntoEventId() {
      String rawId = UUID.randomUUID().toString();
      Event event =
          new Event(
              rawId,
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              VALID_TOTAL_SPOTS,
              0,
              VALID_PARTNER_ID,
              new LinkedHashSet<>());

      assertInstanceOf(EventId.class, event.getId());
      assertEquals(rawId, event.getId().getValue());
    }

    @Test
    @DisplayName("given an EventId, should reuse the same instance")
    void shouldReuseEventId() {
      EventId eventId = new EventId();
      Event event =
          new Event(
              eventId,
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              VALID_TOTAL_SPOTS,
              0,
              VALID_PARTNER_ID,
              new LinkedHashSet<>());

      assertSame(eventId, event.getId());
    }

    @Test
    @DisplayName("should store all provided fields correctly")
    void shouldStoreAllFields() {
      Event event =
          new Event(
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              true,
              VALID_TOTAL_SPOTS,
              10,
              VALID_PARTNER_ID,
              new LinkedHashSet<>());

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
      Event event =
          new Event(
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              VALID_TOTAL_SPOTS,
              0,
              VALID_PARTNER_ID,
              new LinkedHashSet<>());

      assertTrue(event.getSections().isEmpty());
    }

    @Test
    @DisplayName("should copy provided sections into the event")
    void shouldCopyProvidedSections() {
      EventSection s1 =
          EventSection.create(new AddSectionCommand("Pista", "Desc", 3, new BigDecimal("50.00")));
      EventSection s2 =
          EventSection.create(new AddSectionCommand("VIP", "Desc", 2, new BigDecimal("200.00")));
      Set<EventSection> sections = new LinkedHashSet<>();
      sections.add(s1);
      sections.add(s2);

      Event event =
          new Event(
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              VALID_TOTAL_SPOTS,
              0,
              VALID_PARTNER_ID,
              sections);

      assertEquals(2, event.getSections().size());
      assertTrue(event.getSections().contains(s1));
      assertTrue(event.getSections().contains(s2));
    }
  }

  @Nested
  @DisplayName("Factory method create(CreateEventCommand)")
  class FactoryMethod {

    @Test
    @DisplayName("should create an event with a valid UUID as ID")
    void shouldCreateWithValidUuid() {
      Event event = Event.create(VALID_COMMAND);

      assertNotNull(event.getId());
      assertDoesNotThrow(() -> UUID.fromString(event.getId().getValue()));
    }

    @Test
    @DisplayName("should create an event with isPublished set to false")
    void shouldCreateUnpublished() {
      assertFalse(Event.create(VALID_COMMAND).isPublished());
    }

    @Test
    @DisplayName("should create an event with totalSpotsReserved set to zero")
    void shouldCreateWithZeroReservedSpots() {
      assertEquals(0, Event.create(VALID_COMMAND).getTotalSpotsReserved());
    }

    @Test
    @DisplayName("should store fields from the command")
    void shouldStoreCommandFields() {
      Event event = Event.create(VALID_COMMAND);

      assertEquals(VALID_NAME, event.getName());
      assertEquals(VALID_DESCRIPTION, event.getDescription());
      assertEquals(VALID_DATE, event.getDate());
      assertEquals(VALID_TOTAL_SPOTS, event.getTotalSpots());
      assertSame(VALID_PARTNER_ID, event.getPartnerId());
    }

    @Test
    @DisplayName("each call should produce a different ID")
    void shouldGenerateDistinctIds() {
      assertNotEquals(
          Event.create(VALID_COMMAND).getId().getValue(),
          Event.create(VALID_COMMAND).getId().getValue());
    }

    @Test
    @DisplayName("should create an event with empty sections")
    void shouldCreateWithEmptySections() {
      assertTrue(Event.create(VALID_COMMAND).getSections().isEmpty());
    }
  }

  @Nested
  @DisplayName("changeName")
  class ChangeName {

    @Test
    @DisplayName("should return a new instance with the updated name")
    void shouldReturnNewInstanceWithUpdatedName() {
      Event event = Event.create(VALID_COMMAND);
      Event changed = event.changeName("Novo Nome");

      assertNotSame(event, changed);
      assertEquals("Novo Nome", changed.getName());
    }

    @Test
    @DisplayName("should preserve ID, sections and other fields")
    void shouldPreserveOtherFields() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);
      Event changed = event.changeName("Novo Nome");

      assertEquals(event.getId().getValue(), changed.getId().getValue());
      assertEquals(1, changed.getSections().size());
      assertEquals(VALID_DATE, changed.getDate());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      Event event = Event.create(VALID_COMMAND);
      event.changeName("Novo Nome");

      assertEquals(VALID_NAME, event.getName());
    }
  }

  @Nested
  @DisplayName("changeDescription")
  class ChangeDescription {

    @Test
    @DisplayName("should return a new instance with the updated description")
    void shouldReturnNewInstanceWithUpdatedDescription() {
      Event event = Event.create(VALID_COMMAND);
      Event changed = event.changeDescription("Nova descrição");

      assertNotSame(event, changed);
      assertEquals("Nova descrição", changed.getDescription());
    }

    @Test
    @DisplayName("should preserve ID and other fields")
    void shouldPreserveOtherFields() {
      Event event = Event.create(VALID_COMMAND);
      Event changed = event.changeDescription("Nova descrição");

      assertEquals(event.getId().getValue(), changed.getId().getValue());
      assertEquals(VALID_NAME, changed.getName());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      Event event = Event.create(VALID_COMMAND);
      event.changeDescription("Nova descrição");

      assertEquals(VALID_DESCRIPTION, event.getDescription());
    }
  }

  @Nested
  @DisplayName("changeDate")
  class ChangeDate {

    @Test
    @DisplayName("should return a new instance with the updated date")
    void shouldReturnNewInstanceWithUpdatedDate() {
      Event event = Event.create(VALID_COMMAND);
      LocalDate newDate = LocalDate.of(2027, 6, 15);
      Event changed = event.changeDate(newDate);

      assertNotSame(event, changed);
      assertEquals(newDate, changed.getDate());
    }

    @Test
    @DisplayName("should preserve ID and other fields")
    void shouldPreserveOtherFields() {
      Event event = Event.create(VALID_COMMAND);
      Event changed = event.changeDate(LocalDate.of(2027, 6, 15));

      assertEquals(event.getId().getValue(), changed.getId().getValue());
      assertEquals(VALID_NAME, changed.getName());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      Event event = Event.create(VALID_COMMAND);
      event.changeDate(LocalDate.of(2027, 6, 15));

      assertEquals(VALID_DATE, event.getDate());
    }
  }

  @Nested
  @DisplayName("publish")
  class Publish {

    @Test
    @DisplayName("should return a new instance with isPublished set to true")
    void shouldReturnNewInstanceWithIsPublishedTrue() {
      Event event = Event.create(VALID_COMMAND);
      Event published = event.publish();

      assertNotSame(event, published);
      assertTrue(published.isPublished());
    }

    @Test
    @DisplayName("should preserve ID and sections")
    void shouldPreserveIdAndSections() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);
      Event published = event.publish();

      assertEquals(event.getId().getValue(), published.getId().getValue());
      assertEquals(1, published.getSections().size());
    }

    @Test
    @DisplayName("should not change section publication state")
    void shouldNotChangeSectionPublicationState() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);
      Event published = event.publish();

      published.getSections().forEach(s -> assertFalse(s.isPublished()));
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      Event event = Event.create(VALID_COMMAND);
      event.publish();

      assertFalse(event.isPublished());
    }
  }

  @Nested
  @DisplayName("unpublish")
  class Unpublish {

    @Test
    @DisplayName("should return a new instance with isPublished set to false")
    void shouldReturnNewInstanceWithIsPublishedFalse() {
      Event event = Event.create(VALID_COMMAND).publish();
      Event unpublished = event.unpublish();

      assertNotSame(event, unpublished);
      assertFalse(unpublished.isPublished());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      Event event = Event.create(VALID_COMMAND).publish();
      event.unpublish();

      assertTrue(event.isPublished());
    }
  }

  @Nested
  @DisplayName("publishAll")
  class PublishAll {

    @Test
    @DisplayName("should return a new event with isPublished set to true")
    void shouldPublishEvent() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);
      Event result = event.publishAll();

      assertNotSame(event, result);
      assertTrue(result.isPublished());
    }

    @Test
    @DisplayName("should publish all sections")
    void shouldPublishAllSections() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);
      event.addSection(new AddSectionCommand("VIP", "Desc", 2, new BigDecimal("200.00")));
      Event result = event.publishAll();

      result.getSections().forEach(s -> assertTrue(s.isPublished()));
    }

    @Test
    @DisplayName("should preserve ID and section count")
    void shouldPreserveIdAndSectionCount() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);
      Event result = event.publishAll();

      assertEquals(event.getId().getValue(), result.getId().getValue());
      assertEquals(1, result.getSections().size());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);
      event.publishAll();

      assertFalse(event.isPublished());
      event.getSections().forEach(s -> assertFalse(s.isPublished()));
    }
  }

  @Nested
  @DisplayName("unpublishAll")
  class UnpublishAll {

    @Test
    @DisplayName("should return a new event with isPublished set to false")
    void shouldUnpublishEvent() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);
      Event published = event.publishAll();
      Event result = published.unpublishAll();

      assertNotSame(published, result);
      assertFalse(result.isPublished());
    }

    @Test
    @DisplayName("should unpublish all sections")
    void shouldUnpublishAllSections() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);
      Event result = event.publishAll().unpublishAll();

      result.getSections().forEach(s -> assertFalse(s.isPublished()));
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);
      Event published = event.publishAll();
      published.unpublishAll();

      assertTrue(published.isPublished());
      published.getSections().forEach(s -> assertTrue(s.isPublished()));
    }
  }

  @Nested
  @DisplayName("addSection")
  class AddSection {

    @Test
    @DisplayName("should add a section created from the command")
    void shouldAddSectionFromCommand() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);

      assertEquals(1, event.getSections().size());
    }

    @Test
    @DisplayName("should create section with data from the command")
    void shouldCreateSectionWithCommandData() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);

      EventSection section = event.getSections().iterator().next();
      assertEquals("Pista", section.getName());
      assertEquals(3, section.getTotalSpots());
      assertFalse(section.isPublished());
    }

    @Test
    @DisplayName("should maintain insertion order across multiple addSection calls")
    void shouldMaintainInsertionOrder() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(new AddSectionCommand("Pista", "Desc", 3, new BigDecimal("50.00")));
      event.addSection(new AddSectionCommand("VIP", "Desc", 2, new BigDecimal("200.00")));
      event.addSection(new AddSectionCommand("Camarote", "Desc", 1, new BigDecimal("500.00")));

      Iterator<EventSection> it = event.getSections().iterator();
      assertEquals("Pista", it.next().getName());
      assertEquals("VIP", it.next().getName());
      assertEquals("Camarote", it.next().getName());
    }

    @Test
    @DisplayName("each addSection call should produce a section with a unique ID")
    void shouldGenerateUniqueIdPerSection() {
      Event event = Event.create(VALID_COMMAND);
      event.addSection(SECTION_COMMAND);
      event.addSection(SECTION_COMMAND);

      assertEquals(2, event.getSections().size());
    }
  }

  @Nested
  @DisplayName("Sections")
  class Sections {

    @Test
    @DisplayName("should maintain insertion order of sections")
    void shouldMaintainInsertionOrder() {
      EventSection first =
          EventSection.create(new AddSectionCommand("Pista", "Desc", 3, new BigDecimal("50.00")));
      EventSection second =
          EventSection.create(new AddSectionCommand("VIP", "Desc", 2, new BigDecimal("200.00")));
      EventSection third =
          EventSection.create(
              new AddSectionCommand("Camarote", "Desc", 1, new BigDecimal("500.00")));

      Set<EventSection> sections = new LinkedHashSet<>();
      sections.add(first);
      sections.add(second);
      sections.add(third);

      Event event =
          new Event(
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              VALID_TOTAL_SPOTS,
              0,
              VALID_PARTNER_ID,
              sections);

      Iterator<EventSection> it = event.getSections().iterator();
      assertSame(first, it.next());
      assertSame(second, it.next());
      assertSame(third, it.next());
    }

    @Test
    @DisplayName("should not add duplicate sections (same ID)")
    void shouldNotAddDuplicateSections() {
      EventSectionId sharedId = new EventSectionId();
      EventSection original =
          new EventSection(
              sharedId,
              "Pista",
              "Desc",
              false,
              3,
              0,
              new BigDecimal("50.00"),
              new LinkedHashSet<>());
      EventSection duplicate =
          new EventSection(
              sharedId,
              "Pista Alterada",
              "Desc",
              false,
              3,
              0,
              new BigDecimal("50.00"),
              new LinkedHashSet<>());

      Set<EventSection> sections = new LinkedHashSet<>();
      sections.add(original);
      sections.add(duplicate);

      Event event =
          new Event(
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              VALID_TOTAL_SPOTS,
              0,
              VALID_PARTNER_ID,
              sections);

      assertEquals(1, event.getSections().size());
    }

    @Test
    @DisplayName("getSections should return an unmodifiable view")
    void shouldReturnUnmodifiableSet() {
      Event event = Event.create(VALID_COMMAND);
      EventSection section = EventSection.create(SECTION_COMMAND);

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
      Event a =
          new Event(
              id,
              "Name A",
              "Desc A",
              VALID_DATE,
              false,
              50,
              0,
              VALID_PARTNER_ID,
              new LinkedHashSet<>());
      Event b =
          new Event(
              id,
              "Name B",
              "Desc B",
              VALID_DATE,
              true,
              200,
              10,
              VALID_PARTNER_ID,
              new LinkedHashSet<>());

      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("should not be equal when events have different IDs")
    void shouldNotBeEqualWithDifferentIds() {
      assertNotEquals(Event.create(VALID_COMMAND), Event.create(VALID_COMMAND));
    }

    @Test
    @DisplayName("should be equal to itself")
    void shouldBeEqualToItself() {
      Event event = Event.create(VALID_COMMAND);

      assertEquals(event, event);
    }
  }

  @Nested
  @DisplayName("toString")
  class ToStringTest {

    @Test
    @DisplayName("should contain the event ID, name, date and partnerId")
    void shouldContainRelevantFields() {
      Event event = Event.create(VALID_COMMAND);

      assertTrue(event.toString().contains(event.getId().getValue()));
      assertTrue(event.toString().contains(VALID_NAME));
      assertTrue(event.toString().contains(VALID_DATE.toString()));
      assertTrue(event.toString().contains(VALID_PARTNER_ID.getValue()));
    }
  }
}
