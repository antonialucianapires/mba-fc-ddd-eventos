package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.Entity;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;

import java.math.BigDecimal;

public class EventSection extends Entity<EventSectionId> {

    private final String name;
    private final String description;
    private final boolean isPublished;
    private final int totalSpots;
    private final int totalSpotsReserved;
    private final BigDecimal price;

    public EventSection(String name, String description, boolean isPublished,
                        int totalSpots, int totalSpotsReserved, BigDecimal price) {
        super(new EventSectionId());
        this.name = name;
        this.description = description;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsReserved = totalSpotsReserved;
        this.price = price;
    }

    public EventSection(String id, String name, String description, boolean isPublished,
                        int totalSpots, int totalSpotsReserved, BigDecimal price) {
        super(new EventSectionId(id));
        this.name = name;
        this.description = description;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsReserved = totalSpotsReserved;
        this.price = price;
    }

    public EventSection(EventSectionId id, String name, String description, boolean isPublished,
                        int totalSpots, int totalSpotsReserved, BigDecimal price) {
        super(id);
        this.name = name;
        this.description = description;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsReserved = totalSpotsReserved;
        this.price = price;
    }

    public static EventSection create(String name, String description, int totalSpots, BigDecimal price) {
        return new EventSection(
                new EventSectionId(),
                name,
                description,
                false,
                totalSpots,
                0,
                price
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

    @Override
    public String toString() {
        return "EventSection{id=" + id.getValue() + ", name=" + name + ", price=" + price + "}";
    }
}
