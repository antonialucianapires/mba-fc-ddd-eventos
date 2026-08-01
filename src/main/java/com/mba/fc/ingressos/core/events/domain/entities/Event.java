package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.events.domain.commands.AddSectionCommand;
import com.mba.fc.ingressos.core.events.domain.commands.CreateEventCommand;
import com.mba.fc.ingressos.core.events.domain.commands.UpdateEventSectionCommand;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Event extends AggregateRoot<EventId> {

  private final String name;
  private final String description;
  private final LocalDate date;
  private final boolean isPublished;
  private final AtomicInteger totalSpots;
  private final AtomicInteger totalSpotsReserved;
  private final PartnerId partnerId;
  private final Set<EventSection> sections;

  public Event(
      String name,
      String description,
      LocalDate date,
      boolean isPublished,
      int totalSpots,
      int totalSpotsReserved,
      PartnerId partnerId,
      Set<EventSection> sections) {
    super(new EventId());
    this.name = name;
    this.description = description;
    this.date = date;
    this.isPublished = isPublished;
    this.totalSpots = new AtomicInteger(totalSpots);
    this.totalSpotsReserved = new AtomicInteger(totalSpotsReserved);
    this.partnerId = partnerId;
    this.sections = new LinkedHashSet<>(sections);
  }

  public Event(
      String id,
      String name,
      String description,
      LocalDate date,
      boolean isPublished,
      int totalSpots,
      int totalSpotsReserved,
      PartnerId partnerId,
      Set<EventSection> sections) {
    super(new EventId(id));
    this.name = name;
    this.description = description;
    this.date = date;
    this.isPublished = isPublished;
    this.totalSpots = new AtomicInteger(totalSpots);
    this.totalSpotsReserved = new AtomicInteger(totalSpotsReserved);
    this.partnerId = partnerId;
    this.sections = new LinkedHashSet<>(sections);
  }

  public Event(
      EventId id,
      String name,
      String description,
      LocalDate date,
      boolean isPublished,
      int totalSpots,
      int totalSpotsReserved,
      PartnerId partnerId,
      Set<EventSection> sections) {
    super(id);
    this.name = name;
    this.description = description;
    this.date = date;
    this.isPublished = isPublished;
    this.totalSpots = new AtomicInteger(totalSpots);
    this.totalSpotsReserved = new AtomicInteger(totalSpotsReserved);
    this.partnerId = partnerId;
    this.sections = new LinkedHashSet<>(sections);
  }

  public static Event create(CreateEventCommand command) {
    return new Event(
        new EventId(),
        command.name(),
        command.description(),
        command.date(),
        false,
        command.totalSpots(),
        0,
        command.partnerId(),
        new LinkedHashSet<>());
  }

  public void addSection(AddSectionCommand command) {
    var section = EventSection.create(command);
    if (this.sections.add(section)) {
      this.totalSpots.addAndGet(section.getTotalSpots());
    }
  }

  public void updateSection(EventSectionId sectionId, UpdateEventSectionCommand command) {
    EventSection section = findSection(sectionId);
    EventSection updatedSection = section.changeInfo(command);
    this.sections.remove(section);
    this.sections.add(updatedSection);
  }

  private EventSection findSection(EventSectionId sectionId) {
    return sections.stream()
        .filter(section -> section.getId().equals(sectionId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Section not found"));
  }

  public boolean allowReserveSpot(EventSectionId sectionId, EventSpotId spotId) {
    if (!this.isPublished) {
      return false;
    }
    EventSection section = findSectionOrNull(sectionId);
    if (section == null || !section.isPublished()) {
      return false;
    }
    EventSpot spot = findSpotOrNull(section, spotId);
    return spot != null && spot.isPublished() && !spot.isReserved();
  }

  public void markSpotAsReserved(EventSectionId sectionId, EventSpotId spotId) {
    EventSection section = findSection(sectionId);
    EventSection updatedSection = section.reserveSpot(spotId);
    this.sections.remove(section);
    this.sections.add(updatedSection);
    this.totalSpotsReserved.incrementAndGet();
  }

  public BigDecimal getSectionPrice(EventSectionId sectionId) {
    return findSection(sectionId).getPrice();
  }

  private EventSection findSectionOrNull(EventSectionId sectionId) {
    return sections.stream()
        .filter(section -> section.getId().equals(sectionId))
        .findFirst()
        .orElse(null);
  }

  private EventSpot findSpotOrNull(EventSection section, EventSpotId spotId) {
    return section.getSpots().stream()
        .filter(spot -> spot.getId().equals(spotId))
        .findFirst()
        .orElse(null);
  }

  public Event changeName(String name) {
    return new Event(
        this.id,
        name,
        this.description,
        this.date,
        this.isPublished,
        this.totalSpots.get(),
        this.totalSpotsReserved.get(),
        this.partnerId,
        this.sections);
  }

  public Event changeDescription(String description) {
    return new Event(
        this.id,
        this.name,
        description,
        this.date,
        this.isPublished,
        this.totalSpots.get(),
        this.totalSpotsReserved.get(),
        this.partnerId,
        this.sections);
  }

  public Event changeDate(LocalDate date) {
    return new Event(
        this.id,
        this.name,
        this.description,
        date,
        this.isPublished,
        this.totalSpots.get(),
        this.totalSpotsReserved.get(),
        this.partnerId,
        this.sections);
  }

  public Event publishAll() {
    Set<EventSection> publishedSections = new LinkedHashSet<>();
    for (EventSection section : this.sections) {
      publishedSections.add(section.publishAll());
    }
    return new Event(
        this.id,
        this.name,
        this.description,
        this.date,
        true,
        this.totalSpots.get(),
        this.totalSpotsReserved.get(),
        this.partnerId,
        publishedSections);
  }

  public Event unpublishAll() {
    Set<EventSection> unpublishedSections = new LinkedHashSet<>();
    for (EventSection section : this.sections) {
      unpublishedSections.add(section.unpublishAll());
    }
    return new Event(
        this.id,
        this.name,
        this.description,
        this.date,
        false,
        this.totalSpots.get(),
        this.totalSpotsReserved.get(),
        this.partnerId,
        unpublishedSections);
  }

  public Event publish() {
    return new Event(
        this.id,
        this.name,
        this.description,
        this.date,
        true,
        this.totalSpots.get(),
        this.totalSpotsReserved.get(),
        this.partnerId,
        this.sections);
  }

  public Event unpublish() {
    return new Event(
        this.id,
        this.name,
        this.description,
        this.date,
        false,
        this.totalSpots.get(),
        this.totalSpotsReserved.get(),
        this.partnerId,
        this.sections);
  }

  public EventId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public LocalDate getDate() {
    return date;
  }

  public boolean isPublished() {
    return isPublished;
  }

  public int getTotalSpots() {
    return totalSpots.get();
  }

  public int getTotalSpotsReserved() {
    return totalSpotsReserved.get();
  }

  public PartnerId getPartnerId() {
    return partnerId;
  }

  public Set<EventSection> getSections() {
    return Collections.unmodifiableSet(sections);
  }

  @Override
  public String toString() {
    return "Event{id="
        + id.getValue()
        + ", name="
        + name
        + ", date="
        + date
        + ", partnerId="
        + partnerId.getValue()
        + ", totalSpots="
        + totalSpots.get()
        + ", totalSpotsReserved="
        + totalSpotsReserved.get()
        + ", sections="
        + sections
        + "}";
  }
}
