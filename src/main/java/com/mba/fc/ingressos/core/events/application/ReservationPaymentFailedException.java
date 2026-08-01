package com.mba.fc.ingressos.core.events.application;

import com.mba.fc.ingressos.core.common.domain.valueobjects.OrderId;

public class ReservationPaymentFailedException extends RuntimeException {

  private final OrderId orderId;

  public ReservationPaymentFailedException(String message, OrderId orderId, Throwable cause) {
    super(message, cause);
    this.orderId = orderId;
  }

  public OrderId getOrderId() {
    return orderId;
  }
}
