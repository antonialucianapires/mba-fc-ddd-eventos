package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.Entity;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;

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
    private final Set<EventSpot> spots = new LinkedHashSet<>();

    public EventSection(String name, String description, boolean isPublished,
                        int totalSpots, int totalSpotsReserved, BigDecimal price, Set<EventSpot> spots) {
        super(new EventSectionId());
        this.name = name;
        this.description = description;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsReserved = totalSpotsReserved;
        this.price = price;
        this.spots.addAll(spots);
    }

    public EventSection(String id, String name, String description, boolean isPublished,
                        int totalSpots, int totalSpotsReserved, BigDecimal price, Set<EventSpot> spots) {
        super(new EventSectionId(id));
        this.name = name;
        this.description = description;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsReserved = totalSpotsReserved;
        this.price = price;
        this.spots.addAll(spots);
    }

    public EventSection(EventSectionId id, String name, String description, boolean isPublished,
                        int totalSpots, int totalSpotsReserved, BigDecimal price, Set<EventSpot> spots) {
        super(id);
        this.name = name;
        this.description = description;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsReserved = totalSpotsReserved;
        this.price = price;
        this.spots.addAll(spots);
    }

    public static EventSection create(String name, String description, int totalSpots, BigDecimal price) {
        return new EventSection(
                new EventSectionId(),
                name,
                description,
                false,
                totalSpots,
                0,
                price,
                new LinkedHashSet<>()
        );
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
        return "EventSection{id=" + id.getValue() + ", name=" + name + ", price=" + price + "}";
    }
}
