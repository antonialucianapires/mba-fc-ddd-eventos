package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import java.time.Instant;

public class SpotReservation extends AggregateRoot<EventSpotId> {

  private final CustomerId customerId;
  private final Instant reservedAt;

  public SpotReservation(EventSpotId eventSpotId, CustomerId customerId, Instant reservedAt) {
    super(eventSpotId);
    this.customerId = customerId;
    this.reservedAt = reservedAt;
  }

  public SpotReservation(String eventSpotId, CustomerId customerId, Instant reservedAt) {
    super(new EventSpotId(eventSpotId));
    this.customerId = customerId;
    this.reservedAt = reservedAt;
  }

  public static SpotReservation create(EventSpotId eventSpotId, CustomerId customerId) {
    return new SpotReservation(eventSpotId, customerId, Instant.now());
  }

  public EventSpotId getId() {
    return id;
  }

  public CustomerId getCustomerId() {
    return customerId;
  }

  public Instant getReservedAt() {
    return reservedAt;
  }

  @Override
  public String toString() {
    return "SpotReservation{eventSpotId="
        + id.getValue()
        + ", customerId="
        + customerId.getValue()
        + ", reservedAt="
        + reservedAt
        + "}";
  }
}
