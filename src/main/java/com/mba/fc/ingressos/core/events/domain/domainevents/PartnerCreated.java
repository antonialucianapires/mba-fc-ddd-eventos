package com.mba.fc.ingressos.core.events.domain.domainevents;

import com.mba.fc.ingressos.core.common.domain.IDomainEvent;
import com.mba.fc.ingressos.core.common.domain.valueobjects.Uuid;
import java.time.LocalDateTime;

public class PartnerCreated implements IDomainEvent {

  private final long eventVersion = 1L;
  private final Uuid aggregateId;
  private final String name;
  private final LocalDateTime occurredOn;

  public PartnerCreated(Uuid aggregateId, String name) {
    this.aggregateId = aggregateId;
    this.name = name;
    this.occurredOn = LocalDateTime.now();
  }

  @Override
  public Uuid getAggregateId() {
    return this.aggregateId;
  }

  @Override
  public LocalDateTime getOccurredOn() {
    return this.occurredOn;
  }

  @Override
  public long getEventVersion() {
    return this.eventVersion;
  }

  public String getName() {
    return name;
  }
}
