package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;

import java.time.LocalDate;

public class Event extends AggregateRoot<EventId> {

    private final String name;
    private final String description;
    private final LocalDate date;
    private final boolean isPublished;
    private final int totalSpots;
    private final int totalSpotsReserved;
    private final PartnerId partnerId;

    public Event(String name, String description, LocalDate date, boolean isPublished,
                 int totalSpots, int totalSpotsReserved, PartnerId partnerId) {
        super(new EventId());
        this.name = name;
        this.description = description;
        this.date = date;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsReserved = totalSpotsReserved;
        this.partnerId = partnerId;
    }

    public Event(String id, String name, String description, LocalDate date, boolean isPublished,
                 int totalSpots, int totalSpotsReserved, PartnerId partnerId) {
        super(new EventId(id));
        this.name = name;
        this.description = description;
        this.date = date;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsReserved = totalSpotsReserved;
        this.partnerId = partnerId;
    }

    public Event(EventId id, String name, String description, LocalDate date, boolean isPublished,
                 int totalSpots, int totalSpotsReserved, PartnerId partnerId) {
        super(id);
        this.name = name;
        this.description = description;
        this.date = date;
        this.isPublished = isPublished;
        this.totalSpots = totalSpots;
        this.totalSpotsReserved = totalSpotsReserved;
        this.partnerId = partnerId;
    }

    public static Event create(String name, String description, LocalDate date,
                               int totalSpots, PartnerId partnerId) {
        return new Event(
                new EventId(),
                name,
                description,
                date,
                false,
                totalSpots,
                0,
                partnerId
        );
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
        return totalSpots;
    }

    public int getTotalSpotsReserved() {
        return totalSpotsReserved;
    }

    public PartnerId getPartnerId() {
        return partnerId;
    }

    @Override
    public String toString() {
        return "Event{id=" + id.getValue() + ", name=" + name + ", date=" + date + ", partnerId=" + partnerId.getValue() + "}";
    }
}
