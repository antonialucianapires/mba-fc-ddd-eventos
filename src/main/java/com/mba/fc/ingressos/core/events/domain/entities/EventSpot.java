package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.Entity;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;

public class EventSpot extends Entity<EventSpotId> {

  private final String location;
  private final boolean isReserved;
  private final boolean isPublished;

  public EventSpot(String location, boolean isReserved, boolean isPublished) {
    super(new EventSpotId());
    this.location = location;
    this.isReserved = isReserved;
    this.isPublished = isPublished;
  }

  public EventSpot(String id, String location, boolean isReserved, boolean isPublished) {
    super(new EventSpotId(id));
    this.location = location;
    this.isReserved = isReserved;
    this.isPublished = isPublished;
  }

  public EventSpot(EventSpotId id, String location, boolean isReserved, boolean isPublished) {
    super(id);
    this.location = location;
    this.isReserved = isReserved;
    this.isPublished = isPublished;
  }

  public static EventSpot create(String location) {
    return new EventSpot(new EventSpotId(), location, false, false);
  }

  public EventSpot changeLocation(String location) {
    return new EventSpot(this.id, location, this.isReserved, this.isPublished);
  }

  public EventSpot reserve() {
    return new EventSpot(this.id, this.location, true, this.isPublished);
  }

  public EventSpot publish() {
    return new EventSpot(this.id, this.location, this.isReserved, true);
  }

  public EventSpot unpublish() {
    return new EventSpot(this.id, this.location, this.isReserved, false);
  }

  public EventSpotId getId() {
    return id;
  }

  public String getLocation() {
    return location;
  }

  public boolean isReserved() {
    return isReserved;
  }

  public boolean isPublished() {
    return isPublished;
  }

  @Override
  public String toString() {
    return "EventSpot{id="
        + id.getValue()
        + ", location="
        + location
        + ", isReserved="
        + isReserved
        + ", isPublished="
        + isPublished
        + "}";
  }
}
