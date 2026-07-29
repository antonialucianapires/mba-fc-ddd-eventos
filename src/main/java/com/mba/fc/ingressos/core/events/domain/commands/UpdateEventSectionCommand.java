package com.mba.fc.ingressos.core.events.domain.commands;

import java.math.BigDecimal;
import java.util.Optional;

public record UpdateEventSectionCommand(
    Optional<String> name, Optional<String> description, Optional<BigDecimal> price) {}
