package com.mba.fc.ingressos.core.events.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.events.domain.commands.AddSectionCommand;
import com.mba.fc.ingressos.core.events.domain.commands.CreateEventCommand;
import com.mba.fc.ingressos.core.events.domain.commands.UpdateEventCommand;
import com.mba.fc.ingressos.core.events.domain.commands.UpdateEventSectionCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Event;
import com.mba.fc.ingressos.core.events.domain.entities.EventSection;
import com.mba.fc.ingressos.core.events.domain.entities.Partner;
import com.mba.fc.ingressos.core.events.domain.repositories.IEventRepository;
import com.mba.fc.ingressos.core.events.domain.repositories.IPartnerRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

@DisplayName("EventService")
class EventServiceTest {

  private static final String VALID_NAME = "Show de Rock";
  private static final String VALID_DESCRIPTION = "Um grande show";
  private static final LocalDate VALID_DATE = LocalDate.of(2026, 12, 31);
  private static final int VALID_TOTAL_SPOTS = 100;

  private IEventRepository eventRepository;
  private IPartnerRepository partnerRepository;
  private IUnitOfWork unitOfWork;
  private EventService service;
  private Partner partner;
  private CreateEventCommand validCommand;

  @BeforeEach
  void setUp() {
    eventRepository = mock(IEventRepository.class);
    partnerRepository = mock(IPartnerRepository.class);
    unitOfWork = mock(IUnitOfWork.class);
    service = new EventService(eventRepository, partnerRepository, unitOfWork);

    doAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get())
        .when(unitOfWork)
        .runTransaction(any());

    partner = Partner.create("Acme Corp");
    validCommand =
        new CreateEventCommand(
            VALID_NAME, VALID_DESCRIPTION, VALID_DATE, VALID_TOTAL_SPOTS, partner.getId());
  }

  @Nested
  @DisplayName("list()")
  class List {

    @Test
    @DisplayName("should return every event from the repository")
    void shouldReturnAllEventsFromRepository() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findAll()).thenReturn(Set.of(event));

      Set<Event> events = service.list();

      assertEquals(Set.of(event), events);
    }

    @Test
    @DisplayName("should return an empty set when the repository has no events")
    void shouldReturnEmptySetWhenRepositoryIsEmpty() {
      when(eventRepository.findAll()).thenReturn(Set.of());

      Set<Event> events = service.list();

      assertTrue(events.isEmpty());
    }

    @Test
    @DisplayName("should not interact with the unit of work")
    void shouldNotInteractWithUnitOfWork() {
      when(eventRepository.findAll()).thenReturn(Set.of());

      service.list();

      verifyNoInteractions(unitOfWork);
    }
  }

  @Nested
  @DisplayName("create(CreateEventCommand)")
  class Create {

    @Test
    @DisplayName("should create an Event bound to the partner from the command")
    void shouldCreateEventWithGivenData() {
      when(partnerRepository.findById(partner.getId())).thenReturn(partner);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Event created = service.create(validCommand);

      assertEquals(VALID_NAME, created.getName());
      assertEquals(partner.getId(), created.getPartnerId());
    }

    @Test
    @DisplayName("should pass the created event to the repository")
    void shouldAddCreatedEventToRepository() {
      when(partnerRepository.findById(partner.getId())).thenReturn(partner);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(validCommand);

      verify(eventRepository).add(any(Event.class));
    }

    @Test
    @DisplayName("should throw when the partner from the command is not found")
    void shouldThrowWhenPartnerNotFound() {
      when(partnerRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.create(validCommand));
    }

    @Test
    @DisplayName("should not add or commit when the partner is not found")
    void shouldNotAddOrCommitWhenPartnerNotFound() {
      when(partnerRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.create(validCommand));

      verify(eventRepository, never()).add(any());
      verify(unitOfWork, never()).commit();
    }

    @Test
    @DisplayName("should commit the unit of work after adding the event")
    void shouldCommitUnitOfWork() {
      when(partnerRepository.findById(partner.getId())).thenReturn(partner);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(validCommand);

      verify(unitOfWork).commit();
    }

    @Test
    @DisplayName("should commit only after the event was added to the repository")
    void shouldCommitAfterAddingToRepository() {
      when(partnerRepository.findById(partner.getId())).thenReturn(partner);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(validCommand);

      InOrder inOrder = inOrder(eventRepository, unitOfWork);
      inOrder.verify(eventRepository).add(any(Event.class));
      inOrder.verify(unitOfWork).commit();
    }
  }

  @Nested
  @DisplayName("update(EventId, UpdateEventCommand)")
  class Update {

    @Test
    @DisplayName("should change only the fields present in the command")
    void shouldChangeOnlyFieldsPresentInCommand() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findById(event.getId())).thenReturn(event);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Event updated =
          service.update(
              event.getId(),
              new UpdateEventCommand(Optional.of("New Name"), Optional.empty(), Optional.empty()));

      assertEquals("New Name", updated.getName());
      assertEquals(VALID_DESCRIPTION, updated.getDescription());
      assertEquals(VALID_DATE, updated.getDate());
    }

    @Test
    @DisplayName("should keep every field when the command has none present")
    void shouldKeepFieldsWhenCommandIsEmpty() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findById(event.getId())).thenReturn(event);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Event updated =
          service.update(
              event.getId(),
              new UpdateEventCommand(Optional.empty(), Optional.empty(), Optional.empty()));

      assertEquals(VALID_NAME, updated.getName());
      assertEquals(VALID_DESCRIPTION, updated.getDescription());
      assertEquals(VALID_DATE, updated.getDate());
    }

    @Test
    @DisplayName("should throw when no event is found for the given id")
    void shouldThrowWhenEventNotFound() {
      when(eventRepository.findById(any())).thenReturn(null);

      assertThrows(
          IllegalArgumentException.class,
          () ->
              service.update(
                  new EventId(),
                  new UpdateEventCommand(
                      Optional.of("New Name"), Optional.empty(), Optional.empty())));
    }

    @Test
    @DisplayName("should commit the unit of work after updating the event")
    void shouldCommitUnitOfWork() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findById(event.getId())).thenReturn(event);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.update(
          event.getId(),
          new UpdateEventCommand(Optional.of("New Name"), Optional.empty(), Optional.empty()));

      verify(unitOfWork).commit();
    }
  }

  @Nested
  @DisplayName("addSection(EventId, AddSectionCommand)")
  class AddSection {

    @Test
    @DisplayName("should add a new section to the event")
    void shouldAddSectionToEvent() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findById(event.getId())).thenReturn(event);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Event updated =
          service.addSection(
              event.getId(),
              new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00")));

      assertEquals(1, updated.getSections().size());
      EventSection addedSection = updated.getSections().iterator().next();
      assertEquals("Pista", addedSection.getName());
    }

    @Test
    @DisplayName("should throw when no event is found for the given id")
    void shouldThrowWhenEventNotFound() {
      when(eventRepository.findById(any())).thenReturn(null);

      assertThrows(
          IllegalArgumentException.class,
          () ->
              service.addSection(
                  new EventId(),
                  new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00"))));
    }

    @Test
    @DisplayName("should not add or commit when the event is not found")
    void shouldNotAddOrCommitWhenEventNotFound() {
      when(eventRepository.findById(any())).thenReturn(null);

      assertThrows(
          IllegalArgumentException.class,
          () ->
              service.addSection(
                  new EventId(),
                  new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00"))));

      verify(eventRepository, never()).add(any());
      verify(unitOfWork, never()).commit();
    }

    @Test
    @DisplayName("should commit the unit of work after adding the section")
    void shouldCommitUnitOfWork() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findById(event.getId())).thenReturn(event);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.addSection(
          event.getId(),
          new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00")));

      verify(unitOfWork).commit();
    }

    @Test
    @DisplayName("should commit only after the section was added to the repository")
    void shouldCommitAfterAddingToRepository() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findById(event.getId())).thenReturn(event);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.addSection(
          event.getId(),
          new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00")));

      InOrder inOrder = inOrder(eventRepository, unitOfWork);
      inOrder.verify(eventRepository).add(any(Event.class));
      inOrder.verify(unitOfWork).commit();
    }
  }

  @Nested
  @DisplayName("updateSection(EventId, EventSectionId, UpdateEventSectionCommand)")
  class UpdateSection {

    @Test
    @DisplayName("should change only the fields present in the command")
    void shouldChangeOnlyFieldsPresentInCommand() {
      Event event = partner.initEvent(validCommand);
      event.addSection(new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00")));
      EventSection section = event.getSections().iterator().next();
      when(eventRepository.findById(event.getId())).thenReturn(event);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Event updated =
          service.updateSection(
              event.getId(),
              section.getId(),
              new UpdateEventSectionCommand(Optional.of("VIP"), Optional.empty(), Optional.empty()));

      EventSection updatedSection = updated.getSections().iterator().next();
      assertEquals("VIP", updatedSection.getName());
    }

    @Test
    @DisplayName("should throw when no event is found for the given id")
    void shouldThrowWhenEventNotFound() {
      when(eventRepository.findById(any())).thenReturn(null);

      assertThrows(
          IllegalArgumentException.class,
          () ->
              service.updateSection(
                  new EventId(),
                  new EventSectionId(),
                  new UpdateEventSectionCommand(
                      Optional.of("VIP"), Optional.empty(), Optional.empty())));
    }

    @Test
    @DisplayName("should throw when no section is found for the given id")
    void shouldThrowWhenSectionNotFound() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findById(event.getId())).thenReturn(event);

      assertThrows(
          IllegalArgumentException.class,
          () ->
              service.updateSection(
                  event.getId(),
                  new EventSectionId(),
                  new UpdateEventSectionCommand(
                      Optional.of("VIP"), Optional.empty(), Optional.empty())));
    }

    @Test
    @DisplayName("should commit the unit of work after updating the section")
    void shouldCommitUnitOfWork() {
      Event event = partner.initEvent(validCommand);
      event.addSection(new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00")));
      EventSection section = event.getSections().iterator().next();
      when(eventRepository.findById(event.getId())).thenReturn(event);
      when(eventRepository.add(any(Event.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.updateSection(
          event.getId(),
          section.getId(),
          new UpdateEventSectionCommand(Optional.of("VIP"), Optional.empty(), Optional.empty()));

      verify(unitOfWork).commit();
    }
  }

  @Nested
  @DisplayName("listSections(EventId)")
  class ListSections {

    @Test
    @DisplayName("should return the sections of the event")
    void shouldReturnSectionsOfEvent() {
      Event event = partner.initEvent(validCommand);
      event.addSection(new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00")));
      when(eventRepository.findById(event.getId())).thenReturn(event);

      Set<EventSection> sections = service.listSections(event.getId());

      assertEquals(event.getSections(), sections);
    }

    @Test
    @DisplayName("should return an empty set when the event has no sections")
    void shouldReturnEmptySetWhenEventHasNoSections() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findById(event.getId())).thenReturn(event);

      Set<EventSection> sections = service.listSections(event.getId());

      assertTrue(sections.isEmpty());
    }

    @Test
    @DisplayName("should throw when no event is found for the given id")
    void shouldThrowWhenEventNotFound() {
      when(eventRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.listSections(new EventId()));
    }

    @Test
    @DisplayName("should not interact with the unit of work")
    void shouldNotInteractWithUnitOfWork() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findById(event.getId())).thenReturn(event);

      service.listSections(event.getId());

      verifyNoInteractions(unitOfWork);
    }
  }

  @Nested
  @DisplayName("delete(EventId)")
  class Delete {

    @Test
    @DisplayName("should delete the event from the repository")
    void shouldDeleteEventFromRepository() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findById(event.getId())).thenReturn(event);

      service.delete(event.getId());

      verify(eventRepository).delete(event.getId());
    }

    @Test
    @DisplayName("should commit the unit of work after deleting the event")
    void shouldCommitUnitOfWork() {
      Event event = partner.initEvent(validCommand);
      when(eventRepository.findById(event.getId())).thenReturn(event);

      service.delete(event.getId());

      verify(unitOfWork).commit();
    }

    @Test
    @DisplayName("should throw when no event is found for the given id")
    void shouldThrowWhenEventNotFound() {
      when(eventRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.delete(new EventId()));
    }

    @Test
    @DisplayName("should not delete or commit when the event is not found")
    void shouldNotDeleteOrCommitWhenEventNotFound() {
      when(eventRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.delete(new EventId()));

      verify(eventRepository, never()).delete(any());
      verify(unitOfWork, never()).commit();
    }
  }
}
