package com.mba.fc.ingressos.core.events.infra.db.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.OrderId;
import com.mba.fc.ingressos.core.events.domain.entities.Order;
import com.mba.fc.ingressos.core.events.domain.entities.OrderStatus;
import com.mba.fc.ingressos.core.events.infra.db.schemas.CustomerSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSpotSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.OrderSchema;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("OrderMapper")
class OrderMapperTest {

  private static final String VALID_ID = UUID.randomUUID().toString();
  private static final String VALID_CUSTOMER_ID = UUID.randomUUID().toString();
  private static final String VALID_SPOT_ID = UUID.randomUUID().toString();
  private static final BigDecimal VALID_AMOUNT = new BigDecimal("50.00");

  private final OrderMapper mapper = new OrderMapper();

  @Nested
  @DisplayName("toDomain(OrderSchema)")
  class ToDomain {

    @Test
    @DisplayName("should wrap the schema fields into their respective value objects")
    void shouldWrapIdsIntoValueObjects() {
      OrderSchema schema =
          new OrderSchema(
              VALID_ID,
              VALID_AMOUNT,
              OrderStatus.PAID,
              new CustomerSchema(VALID_CUSTOMER_ID, "52998224725", "John Doe"),
              new EventSpotSchema(VALID_SPOT_ID, "A1", true, true, null));

      Order order = mapper.toDomain(schema);

      assertInstanceOf(OrderId.class, order.getId());
      assertInstanceOf(CustomerId.class, order.getCustomerId());
      assertInstanceOf(EventSpotId.class, order.getEventSpotId());
      assertEquals(VALID_ID, order.getId().getValue());
      assertEquals(VALID_CUSTOMER_ID, order.getCustomerId().getValue());
      assertEquals(VALID_SPOT_ID, order.getEventSpotId().getValue());
    }

    @Test
    @DisplayName("each schema field should survive the mapping independently")
    void shouldMapAllFields() {
      OrderSchema schema =
          new OrderSchema(
              VALID_ID,
              VALID_AMOUNT,
              OrderStatus.CANCELED,
              new CustomerSchema(VALID_CUSTOMER_ID, "52998224725", "John Doe"),
              new EventSpotSchema(VALID_SPOT_ID, "A1", true, true, null));

      Order order = mapper.toDomain(schema);

      assertEquals(VALID_AMOUNT, order.getAmount());
      assertEquals(OrderStatus.CANCELED, order.getStatus());
    }
  }

  @Nested
  @DisplayName("toSchema(Order)")
  class ToSchema {

    @Test
    @DisplayName("should return an OrderSchema with the same IDs as the order")
    void shouldMapIdsFromOrder() {
      Order order =
          new Order(
              VALID_ID,
              new CustomerId(VALID_CUSTOMER_ID),
              VALID_AMOUNT,
              new EventSpotId(VALID_SPOT_ID),
              OrderStatus.PENDING);

      OrderSchema schema = mapper.toSchema(order);

      assertEquals(VALID_ID, schema.getId());
      assertEquals(VALID_CUSTOMER_ID, schema.getCustomer().getId());
      assertEquals(VALID_SPOT_ID, schema.getEventSpot().getId());
    }

    @Test
    @DisplayName("each order field should survive the mapping independently")
    void shouldMapAllFields() {
      Order order =
          new Order(
              VALID_ID,
              new CustomerId(VALID_CUSTOMER_ID),
              VALID_AMOUNT,
              new EventSpotId(VALID_SPOT_ID),
              OrderStatus.PAID);

      OrderSchema schema = mapper.toSchema(order);

      assertEquals(VALID_AMOUNT, schema.getAmount());
      assertEquals(OrderStatus.PAID, schema.getStatus());
    }
  }

  @Nested
  @DisplayName("Round-trip")
  class RoundTrip {

    @Test
    @DisplayName("toDomain(toSchema(order)) should preserve all fields")
    void domainToSchemaAndBackShouldPreserveFields() {
      Order original =
          new Order(
              VALID_ID,
              new CustomerId(VALID_CUSTOMER_ID),
              VALID_AMOUNT,
              new EventSpotId(VALID_SPOT_ID),
              OrderStatus.PAID);

      Order roundTripped = mapper.toDomain(mapper.toSchema(original));

      assertEquals(original.getId().getValue(), roundTripped.getId().getValue());
      assertEquals(original.getCustomerId().getValue(), roundTripped.getCustomerId().getValue());
      assertEquals(original.getAmount(), roundTripped.getAmount());
      assertEquals(original.getEventSpotId().getValue(), roundTripped.getEventSpotId().getValue());
      assertEquals(original.getStatus(), roundTripped.getStatus());
    }
  }
}
