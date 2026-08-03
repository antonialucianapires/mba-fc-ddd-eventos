package com.mba.fc.ingressos.core.common.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AggregateRoot<ID> extends Entity<ID> {
  Set<IDomainEvent> events = new LinkedHashSet<>();

  protected void addEvent(IDomainEvent event) {
    this.events.add(event);
  }

  protected void clearEvents() {
    this.events.clear();
  }

  public Set<IDomainEvent> getEvents() {
    return Collections.unmodifiableSet(this.events);
  }

  protected AggregateRoot(ID id) {
    super(id);
  }
}
