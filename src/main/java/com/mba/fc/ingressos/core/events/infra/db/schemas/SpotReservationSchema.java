package com.mba.fc.ingressos.core.events.infra.db.schemas;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "spot_reservations")
public class SpotReservationSchema {

  @Id
  @Column(name = "spot_id", nullable = false, unique = true)
  private String spotId;

  @Column(name = "reserved_at", nullable = false)
  private Instant reservedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
  private CustomerSchema customer;

  public SpotReservationSchema() {}

  public SpotReservationSchema(String spotId, Instant reservedAt, CustomerSchema customer) {
    this.spotId = spotId;
    this.reservedAt = reservedAt;
    this.customer = customer;
  }

  public String getSpotId() {
    return spotId;
  }

  public Instant getReservedAt() {
    return reservedAt;
  }

  public CustomerSchema getCustomer() {
    return customer;
  }
}
