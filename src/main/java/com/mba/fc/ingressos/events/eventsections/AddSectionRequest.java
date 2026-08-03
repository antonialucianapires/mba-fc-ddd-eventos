package com.mba.fc.ingressos.events.eventsections;

import java.math.BigDecimal;

public record AddSectionRequest(
    String name, String description, int totalSpots, BigDecimal price) {}
