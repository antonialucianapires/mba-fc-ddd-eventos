package com.mba.fc.ingressos.core.events.application;

import java.math.BigDecimal;

public interface PaymentGateway {

  void payment(String cardToken, BigDecimal amount);
}
