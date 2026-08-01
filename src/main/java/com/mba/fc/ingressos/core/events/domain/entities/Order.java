package com.mba.fc.ingressos.core.events.domain.entities;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.OrderId;
import java.math.BigDecimal;

public class Order extends AggregateRoot<OrderId> {

  private final CustomerId customerId;
  private final BigDecimal amount;
  private final EventSpotId eventSpotId;
  private final OrderStatus status;

  public Order(
      CustomerId customerId, BigDecimal amount, EventSpotId eventSpotId, OrderStatus status) {
    super(new OrderId());
    this.customerId = customerId;
    this.amount = amount;
    this.eventSpotId = eventSpotId;
    this.status = status;
  }

  public Order(
      String id,
      CustomerId customerId,
      BigDecimal amount,
      EventSpotId eventSpotId,
      OrderStatus status) {
    super(new OrderId(id));
    this.customerId = customerId;
    this.amount = amount;
    this.eventSpotId = eventSpotId;
    this.status = status;
  }

  public Order(
      OrderId id,
      CustomerId customerId,
      BigDecimal amount,
      EventSpotId eventSpotId,
      OrderStatus status) {
    super(id);
    this.customerId = customerId;
    this.amount = amount;
    this.eventSpotId = eventSpotId;
    this.status = status;
  }

  public static Order create(CustomerId customerId, BigDecimal amount, EventSpotId eventSpotId) {
    return new Order(new OrderId(), customerId, amount, eventSpotId, OrderStatus.PENDING);
  }

  public Order pay() {
    if (this.status != OrderStatus.PENDING) {
      throw new IllegalStateException("Only a pending order can be paid");
    }
    return new Order(this.id, this.customerId, this.amount, this.eventSpotId, OrderStatus.PAID);
  }

  public Order cancel() {
    if (this.status != OrderStatus.PENDING) {
      throw new IllegalStateException("Only a pending order can be canceled");
    }
    return new Order(
        this.id, this.customerId, this.amount, this.eventSpotId, OrderStatus.CANCELED);
  }

  public OrderId getId() {
    return id;
  }

  public CustomerId getCustomerId() {
    return customerId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public EventSpotId getEventSpotId() {
    return eventSpotId;
  }

  public OrderStatus getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return "Order{id="
        + id.getValue()
        + ", customerId="
        + customerId.getValue()
        + ", amount="
        + amount
        + ", eventSpotId="
        + eventSpotId.getValue()
        + ", status="
        + status
        + "}";
  }
}
