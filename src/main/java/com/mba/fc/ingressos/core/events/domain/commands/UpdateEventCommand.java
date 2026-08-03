package com.mba.fc.ingressos.core.events.domain.commands;

import java.time.LocalDate;
import java.util.Optional;

public record UpdateEventCommand(
    Optional<String> name, Optional<String> description, Optional<LocalDate> date) {}
