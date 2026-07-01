package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.commands.AddSectionCommand;
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

  public static Event create(
      String name,
      String description,
      LocalDate date,
      int totalSpots,
      PartnerId partnerId) {
    return new Event(
        new EventId(),
        name,
        description,
        date,
        false,
        totalSpots,
        0,
        partnerId,
        new LinkedHashSet<>());
  }

  public void addSection(AddSectionCommand command) {
    var section = EventSection.create(command);
    if (this.sections.add(section)) {
      this.totalSpots.addAndGet(section.getTotalSpots());
    }
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
