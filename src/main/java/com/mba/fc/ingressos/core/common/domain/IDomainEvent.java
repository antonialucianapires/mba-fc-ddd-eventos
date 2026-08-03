package com.mba.fc.ingressos.core.common.domain;

import com.mba.fc.ingressos.core.common.domain.valueobjects.Uuid;
import java.time.LocalDateTime;

public interface IDomainEvent {
  Uuid getAggregateId();

  LocalDateTime getOccurredOn();

  long getEventVersion();
}
