package com.mba.fc.ingressos.core.events.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.OrderId;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Order")
class OrderTest {

  private static final BigDecimal VALID_AMOUNT = new BigDecimal("50.00");

  @Nested
  @DisplayName("Constructor")
  class Construction {

    @Test
    @DisplayName("given no ID, should generate a valid UUID automatically")
    void shouldGenerateIdWhenNotProvided() {
      Order order = new Order(new CustomerId(), VALID_AMOUNT, new EventSpotId(), OrderStatus.PENDING);

      assertNotNull(order.getId());
      assertDoesNotThrow(() -> UUID.fromString(order.getId().getValue()));
    }

    @Test
    @DisplayName("given a String ID, should wrap it into an OrderId value object")
    void shouldWrapStringIntoOrderId() {
      String rawId = UUID.randomUUID().toString();
      Order order =
          new Order(rawId, new CustomerId(), VALID_AMOUNT, new EventSpotId(), OrderStatus.PENDING);

      assertInstanceOf(OrderId.class, order.getId());
      assertEquals(rawId, order.getId().getValue());
    }

    @Test
    @DisplayName("given an OrderId, should reuse the same instance")
    void shouldReuseOrderId() {
      OrderId orderId = new OrderId();
      Order order =
          new Order(orderId, new CustomerId(), VALID_AMOUNT, new EventSpotId(), OrderStatus.PENDING);

      assertSame(orderId, order.getId());
    }

    @Test
    @DisplayName("should store all provided fields correctly")
    void shouldStoreAllFields() {
      CustomerId customerId = new CustomerId();
      EventSpotId eventSpotId = new EventSpotId();
      Order order = new Order(customerId, VALID_AMOUNT, eventSpotId, OrderStatus.PAID);

      assertEquals(customerId, order.getCustomerId());
      assertEquals(VALID_AMOUNT, order.getAmount());
      assertEquals(eventSpotId, order.getEventSpotId());
      assertEquals(OrderStatus.PAID, order.getStatus());
    }
  }

  @Nested
  @DisplayName("Factory method create()")
  class FactoryMethod {

    @Test
    @DisplayName("should create an order with a valid UUID as ID")
    void shouldCreateWithValidUuid() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());

      assertNotNull(order.getId());
      assertDoesNotThrow(() -> UUID.fromString(order.getId().getValue()));
    }

    @Test
    @DisplayName("should create an order with status PENDING")
    void shouldCreateWithPendingStatus() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());

      assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    @DisplayName("should store the provided customer, amount and spot")
    void shouldStoreProvidedData() {
      CustomerId customerId = new CustomerId();
      EventSpotId eventSpotId = new EventSpotId();
      Order order = Order.create(customerId, VALID_AMOUNT, eventSpotId);

      assertEquals(customerId, order.getCustomerId());
      assertEquals(VALID_AMOUNT, order.getAmount());
      assertEquals(eventSpotId, order.getEventSpotId());
    }

    @Test
    @DisplayName("each call should produce a different ID")
    void shouldGenerateDistinctIds() {
      Order a = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());
      Order b = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());

      assertNotEquals(a.getId().getValue(), b.getId().getValue());
    }
  }

  @Nested
  @DisplayName("pay")
  class Pay {

    @Test
    @DisplayName("should return a new instance with status PAID")
    void shouldReturnNewInstanceWithPaidStatus() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());
      Order paid = order.pay();

      assertNotSame(order, paid);
      assertEquals(OrderStatus.PAID, paid.getStatus());
    }

    @Test
    @DisplayName("should preserve ID, customer, amount and spot")
    void shouldPreserveOtherFields() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());
      Order paid = order.pay();

      assertEquals(order.getId().getValue(), paid.getId().getValue());
      assertEquals(order.getCustomerId(), paid.getCustomerId());
      assertEquals(order.getAmount(), paid.getAmount());
      assertEquals(order.getEventSpotId(), paid.getEventSpotId());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());
      order.pay();

      assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    @DisplayName("should throw when the order is not pending")
    void shouldThrowWhenNotPending() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId()).pay();

      assertThrows(IllegalStateException.class, order::pay);
    }
  }

  @Nested
  @DisplayName("cancel")
  class Cancel {

    @Test
    @DisplayName("should return a new instance with status CANCELED")
    void shouldReturnNewInstanceWithCanceledStatus() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());
      Order canceled = order.cancel();

      assertNotSame(order, canceled);
      assertEquals(OrderStatus.CANCELED, canceled.getStatus());
    }

    @Test
    @DisplayName("should preserve ID, customer, amount and spot")
    void shouldPreserveOtherFields() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());
      Order canceled = order.cancel();

      assertEquals(order.getId().getValue(), canceled.getId().getValue());
      assertEquals(order.getCustomerId(), canceled.getCustomerId());
      assertEquals(order.getAmount(), canceled.getAmount());
      assertEquals(order.getEventSpotId(), canceled.getEventSpotId());
    }

    @Test
    @DisplayName("should not mutate the original instance")
    void shouldNotMutateOriginal() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());
      order.cancel();

      assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    @DisplayName("should throw when the order is not pending")
    void shouldThrowWhenNotPending() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId()).cancel();

      assertThrows(IllegalStateException.class, order::cancel);
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal when both orders share the same ID regardless of other fields")
    void shouldBeEqualWithSameId() {
      String id = UUID.randomUUID().toString();
      Order a = new Order(id, new CustomerId(), VALID_AMOUNT, new EventSpotId(), OrderStatus.PENDING);
      Order b =
          new Order(id, new CustomerId(), new BigDecimal("99.00"), new EventSpotId(), OrderStatus.PAID);

      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("should not be equal when orders have different IDs")
    void shouldNotBeEqualWithDifferentIds() {
      Order a = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());
      Order b = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());

      assertNotEquals(a, b);
    }

    @Test
    @DisplayName("should be equal to itself")
    void shouldBeEqualToItself() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());

      assertEquals(order, order);
    }
  }

  @Nested
  @DisplayName("toString")
  class ToStringTest {

    @Test
    @DisplayName("should contain the order ID, customer ID, amount and status")
    void shouldContainRelevantFields() {
      Order order = Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());

      assertTrue(order.toString().contains(order.getId().getValue()));
      assertTrue(order.toString().contains(order.getCustomerId().getValue()));
      assertTrue(order.toString().contains(VALID_AMOUNT.toString()));
      assertTrue(order.toString().contains("PENDING"));
    }
  }
}
