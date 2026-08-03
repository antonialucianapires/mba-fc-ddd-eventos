package com.mba.fc.ingressos.events.events;

import com.mba.fc.ingressos.core.events.domain.entities.Event;
import com.mba.fc.ingressos.events.eventsections.EventSectionResponse;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public class EventResponse {

  private final String id;
  private final String name;
  private final String description;
  private final LocalDate date;
  private final boolean published;
  private final int totalSpots;
  private final int totalSpotsReserved;
  private final String partnerId;
  private final Set<EventSectionResponse> sections;

  public EventResponse(Event event) {
    this.id = event.getId().getValue();
    this.name = event.getName();
    this.description = event.getDescription();
    this.date = event.getDate();
    this.published = event.isPublished();
    this.totalSpots = event.getTotalSpots();
    this.totalSpotsReserved = event.getTotalSpotsReserved();
    this.partnerId = event.getPartnerId().getValue();
    this.sections =
        event.getSections().stream().map(EventSectionResponse::new).collect(Collectors.toSet());
  }

  public String getId() {
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
    return published;
  }

  public int getTotalSpots() {
    return totalSpots;
  }

  public int getTotalSpotsReserved() {
    return totalSpotsReserved;
  }

  public String getPartnerId() {
    return partnerId;
  }

  public Set<EventSectionResponse> getSections() {
    return sections;
  }
}
