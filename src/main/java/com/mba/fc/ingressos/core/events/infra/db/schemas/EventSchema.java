package com.mba.fc.ingressos.core.events.infra.db.schemas;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "events")
public class EventSchema {

  @Id
  @Column(name = "id", nullable = false, unique = true)
  private String id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "date", nullable = false)
  private LocalDate date;

  @Column(name = "is_published", nullable = false)
  private boolean isPublished;

  @Column(name = "total_spots", nullable = false)
  private int totalSpots;

  @Column(name = "total_spots_reserved", nullable = false)
  private int totalSpotsReserved;

  @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private PartnerSchema partner;

  @OneToMany(
      mappedBy = "event",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<EventSectionSchema> sections;

  public EventSchema() {}

  public EventSchema(
      String id,
      String name,
      String description,
      LocalDate date,
      boolean isPublished,
      int totalSpots,
      int totalSpotsReserved,
      PartnerSchema partner,
      Set<EventSectionSchema> sections) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.date = date;
    this.isPublished = isPublished;
    this.totalSpots = totalSpots;
    this.totalSpotsReserved = totalSpotsReserved;
    this.partner = partner;
    this.sections = sections;
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
    return isPublished;
  }

  public int getTotalSpots() {
    return totalSpots;
  }

  public int getTotalSpotsReserved() {
    return totalSpotsReserved;
  }

  public PartnerSchema getPartner() {
    return partner;
  }

  public Set<EventSectionSchema> getSections() {
    return sections;
  }
}
