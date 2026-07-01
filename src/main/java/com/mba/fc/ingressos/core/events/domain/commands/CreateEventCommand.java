package com.mba.fc.ingressos.core.events.domain.commands;

import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import java.time.LocalDate;

public record CreateEventCommand(
    String name,
    String description,
    LocalDate date,
    int totalSpots,
    PartnerId partnerId) {}
