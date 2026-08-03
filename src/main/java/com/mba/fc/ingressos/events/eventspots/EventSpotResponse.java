package com.mba.fc.ingressos.events.eventspots;

import com.mba.fc.ingressos.core.events.domain.entities.EventSpot;

public class EventSpotResponse {

  private final String id;
  private final String location;
  private final boolean reserved;
  private final boolean published;

  public EventSpotResponse(EventSpot spot) {
    this.id = spot.getId().getValue();
    this.location = spot.getLocation();
    this.reserved = spot.isReserved();
    this.published = spot.isPublished();
  }

  public String getId() {
    return id;
  }

  public String getLocation() {
    return location;
  }

  public boolean isReserved() {
    return reserved;
  }

  public boolean isPublished() {
    return published;
  }
}
