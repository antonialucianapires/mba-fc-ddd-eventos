package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.Entity;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.events.domain.commands.AddSectionCommand;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class EventSection extends Entity<EventSectionId> {

  private final String name;
  private final String description;
  private final boolean isPublished;
  private final int totalSpots;
  private final int totalSpotsReserved;
  private final BigDecimal price;
  private final Set<EventSpot> spots;

  public EventSection(
      String name,
      String description,
      boolean isPublished,
      int totalSpots,
      int totalSpotsReserved,
      BigDecimal price,
      Set<EventSpot> spots) {
    super(new EventSectionId());
    this.name = name;
    this.description = description;
    this.isPublished = isPublished;
    this.totalSpots = totalSpots;
    this.totalSpotsReserved = totalSpotsReserved;
    this.price = price;
    this.spots = new LinkedHashSet<>(spots);
  }

  public EventSection(
      String id,
      String name,
      String description,
      boolean isPublished,
      int totalSpots,
      int totalSpotsReserved,
      BigDecimal price,
      Set<EventSpot> spots) {
    super(new EventSectionId(id));
    this.name = name;
    this.description = description;
    this.isPublished = isPublished;
    this.totalSpots = totalSpots;
    this.totalSpotsReserved = totalSpotsReserved;
    this.price = price;
    this.spots = new LinkedHashSet<>(spots);
  }

  public EventSection(
      EventSectionId id,
      String name,
      String description,
      boolean isPublished,
      int totalSpots,
      int totalSpotsReserved,
      BigDecimal price,
      Set<EventSpot> spots) {
    super(id);
    this.name = name;
    this.description = description;
    this.isPublished = isPublished;
    this.totalSpots = totalSpots;
    this.totalSpotsReserved = totalSpotsReserved;
    this.price = price;
    this.spots = new LinkedHashSet<>(spots);
  }

  public static EventSection create(AddSectionCommand command) {
    var section =
        new EventSection(
            new EventSectionId(),
            command.name(),
            command.description(),
            false,
            command.totalSpots(),
            0,
            command.price(),
            new LinkedHashSet<>());
    section.initSpots();
    return section;
  }

  private void initSpots() {
    for (int i = 1; i <= totalSpots; i++) {
      String location = "Spot " + i;
      EventSpot spot = EventSpot.create(location);
      spots.add(spot);
    }
  }

  public EventSection changeName(String name) {
    return new EventSection(
        this.id,
        name,
        this.description,
        this.isPublished,
        this.totalSpots,
        this.totalSpotsReserved,
        this.price,
        this.spots);
  }

  public EventSection changeDescription(String description) {
    return new EventSection(
        this.id,
        this.name,
        description,
        this.isPublished,
        this.totalSpots,
        this.totalSpotsReserved,
        this.price,
        this.spots);
  }

  public EventSection changePrice(BigDecimal price) {
    return new EventSection(
        this.id,
        this.name,
        this.description,
        this.isPublished,
        this.totalSpots,
        this.totalSpotsReserved,
        price,
        this.spots);
  }

  public EventSection publish() {
    return new EventSection(
        this.id,
        this.name,
        this.description,
        true,
        this.totalSpots,
        this.totalSpotsReserved,
        this.price,
        this.spots);
  }

  public EventSection unpublish() {
    return new EventSection(
        this.id,
        this.name,
        this.description,
        false,
        this.totalSpots,
        this.totalSpotsReserved,
        this.price,
        this.spots);
  }

  public EventSection publishAll() {
    Set<EventSpot> publishedSpots = new LinkedHashSet<>();
    for (EventSpot spot : spots) {
      publishedSpots.add(spot.publish());
    }
    return new EventSection(
        this.id,
        this.name,
        this.description,
        true,
        this.totalSpots,
        this.totalSpotsReserved,
        this.price,
        publishedSpots);
  }

  public EventSection unpublishAll() {
    Set<EventSpot> unpublishedSpots = new LinkedHashSet<>();
    for (EventSpot spot : spots) {
      unpublishedSpots.add(spot.unpublish());
    }
    return new EventSection(
        this.id,
        this.name,
        this.description,
        false,
        this.totalSpots,
        this.totalSpotsReserved,
        this.price,
        unpublishedSpots);
  }

  public EventSectionId getId() {
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

  public BigDecimal getPrice() {
    return price;
  }

  public Set<EventSpot> getSpots() {
    return Collections.unmodifiableSet(spots);
  }

  @Override
  public String toString() {
    return "EventSection{id="
        + id.getValue()
        + ", name="
        + name
        + ", price="
        + price
        + ", totalSpots="
        + totalSpots
        + ", totalSpotsReserved="
        + totalSpotsReserved
        + ", spots="
        + spots
        + "}";
  }
}
