package com.mba.fc.ingressos.core.events.application;

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
import java.util.Set;

public class EventService {

  private final IEventRepository eventRepository;
  private final IPartnerRepository partnerRepository;
  private final IUnitOfWork unitOfWork;

  public EventService(
      IEventRepository eventRepository,
      IPartnerRepository partnerRepository,
      IUnitOfWork unitOfWork) {
    this.eventRepository = eventRepository;
    this.partnerRepository = partnerRepository;
    this.unitOfWork = unitOfWork;
  }

  public Set<Event> list() {
    return eventRepository.findAll();
  }

  public Event create(CreateEventCommand command) {
    return unitOfWork.runTransaction(
        () -> {
          Partner partner = partnerRepository.findById(command.partnerId());
          if (partner == null) {
            throw new IllegalArgumentException("Partner not found");
          }
          Event event = partner.initEvent(command);
          Event eventSaved = eventRepository.add(event);
          unitOfWork.commit();
          return eventSaved;
        });
  }

  public Event update(EventId id, UpdateEventCommand command) {
    return unitOfWork.runTransaction(
        () -> {
          Event event = eventRepository.findById(id);
          if (event == null) {
            throw new IllegalArgumentException("Event not found");
          }
          event = command.name().map(event::changeName).orElse(event);
          event = command.description().map(event::changeDescription).orElse(event);
          event = command.date().map(event::changeDate).orElse(event);
          Event eventUpdated = eventRepository.add(event);
          unitOfWork.commit();
          return eventUpdated;
        });
  }

  public Event addSection(EventId id, AddSectionCommand command) {
    return unitOfWork.runTransaction(
        () -> {
          Event event = eventRepository.findById(id);
          if (event == null) {
            throw new IllegalArgumentException("Event not found");
          }
          event.addSection(command);
          Event eventUpdated = eventRepository.add(event);
          unitOfWork.commit();
          return eventUpdated;
        });
  }

  public Event updateSection(
      EventId id, EventSectionId sectionId, UpdateEventSectionCommand command) {
    return unitOfWork.runTransaction(
        () -> {
          Event event = eventRepository.findById(id);
          if (event == null) {
            throw new IllegalArgumentException("Event not found");
          }
          event.updateSection(sectionId, command);
          Event eventUpdated = eventRepository.add(event);
          unitOfWork.commit();
          return eventUpdated;
        });
  }

  public Set<EventSection> listSections(EventId id) {
    Event event = eventRepository.findById(id);
    if (event == null) {
      throw new IllegalArgumentException("Event not found");
    }
    return event.getSections();
  }

  public void delete(EventId id) {
    unitOfWork.runTransaction(
        () -> {
          Event event = eventRepository.findById(id);
          if (event == null) {
            throw new IllegalArgumentException("Event not found");
          }
          eventRepository.delete(id);
          unitOfWork.commit();
          return null;
        });
  }
}
