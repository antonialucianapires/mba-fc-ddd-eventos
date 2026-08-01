package com.mba.fc.ingressos.core.events.infra.db.schemas;

import com.mba.fc.ingressos.core.events.domain.entities.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class OrderSchema {

  @Id
  @Column(name = "id", nullable = false, unique = true)
  private String id;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OrderStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
  private CustomerSchema customer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_spot_id", referencedColumnName = "id", nullable = false)
  private EventSpotSchema eventSpot;

  public OrderSchema() {}

  public OrderSchema(
      String id,
      BigDecimal amount,
      OrderStatus status,
      CustomerSchema customer,
      EventSpotSchema eventSpot) {
    this.id = id;
    this.amount = amount;
    this.status = status;
    this.customer = customer;
    this.eventSpot = eventSpot;
  }

  public String getId() {
    return id;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public CustomerSchema getCustomer() {
    return customer;
  }

  public EventSpotSchema getEventSpot() {
    return eventSpot;
  }
}
