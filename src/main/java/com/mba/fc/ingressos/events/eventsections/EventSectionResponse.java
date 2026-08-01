package com.mba.fc.ingressos.events.eventsections;

import com.mba.fc.ingressos.core.events.domain.entities.EventSection;
import com.mba.fc.ingressos.events.eventspots.EventSpotResponse;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

public class EventSectionResponse {

    private final String id;
    private final String name;
    private final String description;
    private final boolean published;
    private final int totalSpots;
    private final int totalSpotsReserved;
    private final BigDecimal price;
    private final Set<EventSpotResponse> spots;

    public EventSectionResponse(EventSection section) {
        this.id = section.getId().getValue();
        this.name = section.getName();
        this.description = section.getDescription();
        this.published = section.isPublished();
        this.totalSpots = section.getTotalSpots();
        this.totalSpotsReserved = section.getTotalSpotsReserved();
        this.price = section.getPrice();
        this.spots = section.getSpots().stream().map(EventSpotResponse::new).collect(Collectors.toSet());
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
        return published;
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

    public Set<EventSpotResponse> getSpots() {
        return spots;
    }
}
