package com.mba.fc.ingressos.core.events.infra.db.repositories;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.OrderId;
import com.mba.fc.ingressos.core.events.domain.entities.Order;
import com.mba.fc.ingressos.core.events.domain.entities.OrderStatus;
import com.mba.fc.ingressos.core.events.infra.db.mappers.OrderMapper;
import com.mba.fc.ingressos.core.events.infra.db.schemas.CustomerSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSectionSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSpotSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.OrderSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.PartnerSchema;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
@DisplayName("OrderH2Repository")
class OrderH2RepositoryTest {

  private static final BigDecimal VALID_AMOUNT = new BigDecimal("50.00");

  @Autowired private EntityManager entityManager;

  @Autowired private TestEntityManager testEntityManager;

  private final OrderMapper orderMapper = new OrderMapper();

  private OrderH2Repository repository;

  private CustomerId customerId;
  private EventSpotId spotId;

  @BeforeEach
  void setUp() {
    repository = new OrderH2Repository(entityManager, orderMapper);

    CustomerSchema customerSchema =
        new CustomerSchema(UUID.randomUUID().toString(), "52998224725", "John Doe");
    testEntityManager.persistAndFlush(customerSchema);
    customerId = new CustomerId(customerSchema.getId());

    PartnerSchema partnerSchema = new PartnerSchema(UUID.randomUUID().toString(), "Acme Events");
    testEntityManager.persistAndFlush(partnerSchema);

    EventSchema eventSchema =
        new EventSchema(
            UUID.randomUUID().toString(),
            "Rock in Rio",
            "Festival de musica",
            LocalDate.of(2026, 9, 12),
            true,
            10,
            0,
            partnerSchema,
            new LinkedHashSet<>());
    testEntityManager.persistAndFlush(eventSchema);

    EventSectionSchema sectionSchema =
        new EventSectionSchema(
            UUID.randomUUID().toString(),
            "Pista",
            "Seção pista",
            true,
            10,
            0,
            VALID_AMOUNT.doubleValue(),
            eventSchema,
            new LinkedHashSet<>());
    testEntityManager.persistAndFlush(sectionSchema);

    EventSpotSchema spotSchema =
        new EventSpotSchema(UUID.randomUUID().toString(), "A1", false, true, sectionSchema);
    testEntityManager.persistAndFlush(spotSchema);
    spotId = new EventSpotId(spotSchema.getId());
  }

  @Nested
  @DisplayName("add(Order)")
  class Add {

    @Test
    @DisplayName("should persist the order so it can be found directly in the database")
    void shouldPersistOrder() {
      Order order = Order.create(customerId, VALID_AMOUNT, spotId);

      repository.add(order);
      testEntityManager.flush();
      testEntityManager.clear();

      OrderSchema found = testEntityManager.find(OrderSchema.class, order.getId().getValue());

      assertNotNull(found);
      assertEquals(0, VALID_AMOUNT.compareTo(found.getAmount()));
      assertEquals(OrderStatus.PENDING, found.getStatus());
      assertEquals(customerId.getValue(), found.getCustomer().getId());
      assertEquals(spotId.getValue(), found.getEventSpot().getId());
    }

    @Test
    @DisplayName("should return an Order with the same data that was added")
    void shouldReturnMappedOrder() {
      Order order = Order.create(customerId, VALID_AMOUNT, spotId).pay();

      Order added = repository.add(order);

      assertEquals(order.getId().getValue(), added.getId().getValue());
      assertEquals(OrderStatus.PAID, added.getStatus());
    }
  }

  @Nested
  @DisplayName("findById(Uuid)")
  class FindById {

    @Test
    @DisplayName("should return the order when it exists")
    void shouldReturnExistingOrder() {
      Order order = Order.create(customerId, VALID_AMOUNT, spotId);
      testEntityManager.persistAndFlush(orderMapper.toSchema(order));
      testEntityManager.clear();

      Order found = repository.findById(order.getId());

      assertNotNull(found);
      assertEquals(order.getId().getValue(), found.getId().getValue());
    }

    @Test
    @DisplayName("should return null when the order does not exist")
    void shouldReturnNullWhenNotFound() {
      Order found = repository.findById(new OrderId(UUID.randomUUID().toString()));

      assertNull(found);
    }
  }

  @Nested
  @DisplayName("findAll()")
  class FindAll {

    @Test
    @DisplayName("should return every persisted order")
    void shouldReturnAllOrders() {
      testEntityManager.persistAndFlush(
          orderMapper.toSchema(Order.create(customerId, VALID_AMOUNT, spotId)));
      testEntityManager.clear();

      Set<Order> orders = repository.findAll();

      assertEquals(1, orders.size());
    }

    @Test
    @DisplayName("should return an empty set when there are no orders")
    void shouldReturnEmptySetWhenNoOrders() {
      Set<Order> orders = repository.findAll();

      assertTrue(orders.isEmpty());
    }
  }

  @Nested
  @DisplayName("delete(Uuid)")
  class Delete {

    @Test
    @DisplayName("should remove the order from the database")
    void shouldRemoveOrder() {
      Order order = Order.create(customerId, VALID_AMOUNT, spotId);
      testEntityManager.persistAndFlush(orderMapper.toSchema(order));

      repository.delete(order.getId());
      testEntityManager.clear();

      assertNull(testEntityManager.find(OrderSchema.class, order.getId().getValue()));
    }

    @Test
    @DisplayName("should not throw when the order does not exist")
    void shouldNotThrowWhenOrderDoesNotExist() {
      assertDoesNotThrow(() -> repository.delete(new OrderId(UUID.randomUUID().toString())));
    }
  }
}
