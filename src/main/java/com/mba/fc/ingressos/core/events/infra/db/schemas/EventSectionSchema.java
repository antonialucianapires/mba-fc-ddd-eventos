package com.mba.fc.ingressos.core.events.infra.db.schemas;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "event_sections")
public class EventSectionSchema {

  @Id
  @Column(name = "id", nullable = false, unique = true)
  private String id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "is_published", nullable = false)
  private boolean isPublished;

  @Column(name = "total_spots", nullable = false)
  private int totalSpots;

  @Column(name = "total_spots_reserved", nullable = false)
  private int totalSpotsReserved;

  @Column(name = "price", nullable = false)
  private double price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
  private EventSchema event;

  @OneToMany(
      mappedBy = "eventSection",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<EventSpotSchema> spots = new LinkedHashSet<>();

  public EventSectionSchema() {}

  public EventSectionSchema(
      String id,
      String name,
      String description,
      boolean isPublished,
      int totalSpots,
      int totalSpotsReserved,
      double price,
      EventSchema event,
      Set<EventSpotSchema> spots) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.isPublished = isPublished;
    this.totalSpots = totalSpots;
    this.totalSpotsReserved = totalSpotsReserved;
    this.price = price;
    this.event = event;
    this.spots = spots;
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

  public boolean isPublished() {
    return isPublished;
  }

  public int getTotalSpots() {
    return totalSpots;
  }

  public int getTotalSpotsReserved() {
    return totalSpotsReserved;
  }

  public double getPrice() {
    return price;
  }

  public EventSchema getEvent() {
    return event;
  }

  public Set<EventSpotSchema> getSpots() {
    return spots;
  }
}
