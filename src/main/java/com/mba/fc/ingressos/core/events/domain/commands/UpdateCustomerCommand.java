package com.mba.fc.ingressos.core.events.domain.commands;

import java.util.Optional;

public record UpdateCustomerCommand(Optional<String> name) {}
