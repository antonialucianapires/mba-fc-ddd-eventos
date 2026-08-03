package com.mba.fc.ingressos.events.events;

import java.time.LocalDate;

public record EventRequest(
    String name, String description, LocalDate date, int totalSpots, String partnerId) {}
