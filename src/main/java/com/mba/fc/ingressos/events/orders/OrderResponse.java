package com.mba.fc.ingressos.events.orders;

import com.mba.fc.ingressos.core.events.domain.entities.Order;
import com.mba.fc.ingressos.core.events.domain.entities.OrderStatus;
import java.math.BigDecimal;

public class OrderResponse {

  private final String id;
  private final String customerId;
  private final BigDecimal amount;
  private final String eventSpotId;
  private final OrderStatus status;

  public OrderResponse(Order order) {
    this.id = order.getId().getValue();
    this.customerId = order.getCustomerId().getValue();
    this.amount = order.getAmount();
    this.eventSpotId = order.getEventSpotId().getValue();
    this.status = order.getStatus();
  }

  public String getId() {
    return id;
  }

  public String getCustomerId() {
    return customerId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getEventSpotId() {
    return eventSpotId;
  }

  public OrderStatus getStatus() {
    return status;
  }
}
