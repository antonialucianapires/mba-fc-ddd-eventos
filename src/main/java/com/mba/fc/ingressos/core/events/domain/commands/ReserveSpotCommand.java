package com.mba.fc.ingressos.core.events.domain.commands;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;

public record ReserveSpotCommand(
    EventId eventId,
    EventSectionId sectionId,
    EventSpotId spotId,
    CustomerId customerId,
    String cardToken) {}
