package com.mba.fc.ingressos.core.events.infra.payment;

import com.mba.fc.ingressos.core.events.application.PaymentGateway;
import java.math.BigDecimal;

/**
 * Implementação provisória que sempre aprova o pagamento. Deve ser substituída por uma
 * integração real com um gateway de pagamento.
 */
public class NoopPaymentGateway implements PaymentGateway {

  @Override
  public void payment(String cardToken, BigDecimal amount) {}
}
