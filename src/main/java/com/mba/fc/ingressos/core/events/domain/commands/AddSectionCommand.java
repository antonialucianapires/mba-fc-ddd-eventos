package com.mba.fc.ingressos.core.events.domain.commands;

import java.math.BigDecimal;

public record AddSectionCommand(
    String name, String description, int totalSpots, BigDecimal price) {}
