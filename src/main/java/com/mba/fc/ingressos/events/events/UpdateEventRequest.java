package com.mba.fc.ingressos.events.events;

import java.time.LocalDate;

public record UpdateEventRequest(String name, String description, LocalDate date) {}
