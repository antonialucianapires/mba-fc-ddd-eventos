package com.mba.fc.ingressos.events.eventsections;

import java.math.BigDecimal;

public record UpdateEventSectionRequest(String name, String description, BigDecimal price) {
}
