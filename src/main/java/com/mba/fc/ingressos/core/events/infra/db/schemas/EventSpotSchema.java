package com.mba.fc.ingressos.core.events.infra.db.schemas;

import jakarta.persistence.*;

@Entity
@Table(name = "event_spots")
public class EventSpotSchema {

  @Id
  @Column(name = "id", nullable = false, unique = true)
  private String id;

  @Column(name = "location", nullable = false)
  private String location;

  @Column(name = "is_reserved", nullable = false)
  private boolean isReserved;

  @Column(name = "is_published", nullable = false)
  private boolean isPublished;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "section_id", referencedColumnName = "id", nullable = false)
  private EventSectionSchema eventSection;

  public EventSpotSchema() {}

  public EventSpotSchema(
      String id,
      String location,
      boolean isReserved,
      boolean isPublished,
      EventSectionSchema eventSection) {
    this.id = id;
    this.location = location;
    this.isReserved = isReserved;
    this.isPublished = isPublished;
    this.eventSection = eventSection;
  }

  public String getId() {
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

  public EventSectionSchema getEventSection() {
    return eventSection;
  }
}
