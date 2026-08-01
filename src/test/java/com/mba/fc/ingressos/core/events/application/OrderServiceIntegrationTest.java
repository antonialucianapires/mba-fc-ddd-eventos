package com.mba.fc.ingressos.core.events.application;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.common.infra.UnitOfWorkJpa;
import com.mba.fc.ingressos.core.events.domain.commands.ReserveSpotCommand;
import com.mba.fc.ingressos.core.events.domain.entities.OrderStatus;
import com.mba.fc.ingressos.core.events.infra.db.mappers.CustomerMapper;
import com.mba.fc.ingressos.core.events.infra.db.mappers.EventMapper;
import com.mba.fc.ingressos.core.events.infra.db.mappers.OrderMapper;
import com.mba.fc.ingressos.core.events.infra.db.mappers.SpotReservationMapper;
import com.mba.fc.ingressos.core.events.infra.db.repositories.CustomerH2Repository;
import com.mba.fc.ingressos.core.events.infra.db.repositories.EventH2Repository;
import com.mba.fc.ingressos.core.events.infra.db.repositories.OrderH2Repository;
import com.mba.fc.ingressos.core.events.infra.db.repositories.SpotReservationH2Repository;
import com.mba.fc.ingressos.core.events.infra.db.schemas.CustomerSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSectionSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSpotSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.PartnerSchema;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.transaction.PlatformTransactionManager;

@DataJpaTest
@DisplayName("OrderService (integration)")
class OrderServiceIntegrationTest {

  private static final BigDecimal VALID_AMOUNT = new BigDecimal("50.00");
  private static final String VALID_CARD_TOKEN = "tok_visa";

  @Autowired private EntityManager entityManager;

  @Autowired private TestEntityManager testEntityManager;

  @Autowired private PlatformTransactionManager transactionManager;

  private OrderService service;

  private CustomerId customerId;
  private EventId eventId;
  private EventSectionId sectionId;
  private EventSpotId spotId;

  @BeforeEach
  void setUp() {
    CustomerSchema customerSchema =
        new CustomerSchema(UUID.randomUUID().toString(), "52998224725", "João da Silva");
    testEntityManager.persistAndFlush(customerSchema);
    customerId = new CustomerId(customerSchema.getId());

    PartnerSchema partnerSchema = new PartnerSchema(UUID.randomUUID().toString(), "Acme Events");
    testEntityManager.persistAndFlush(partnerSchema);

    EventSchema eventSchema =
        new EventSchema(
            UUID.randomUUID().toString(),
            "Show de Rock",
            "Um grande show",
            LocalDate.of(2026, 12, 31),
            true,
            1,
            0,
            partnerSchema,
            new LinkedHashSet<>());
    testEntityManager.persistAndFlush(eventSchema);
    eventId = new EventId(eventSchema.getId());

    EventSectionSchema sectionSchema =
        new EventSectionSchema(
            UUID.randomUUID().toString(),
            "Pista",
            "Seção pista",
            true,
            1,
            0,
            VALID_AMOUNT.doubleValue(),
            eventSchema,
            new LinkedHashSet<>());
    testEntityManager.persistAndFlush(sectionSchema);
    sectionId = new EventSectionId(sectionSchema.getId());

    EventSpotSchema spotSchema =
        new EventSpotSchema(UUID.randomUUID().toString(), "A1", false, true, sectionSchema);
    testEntityManager.persistAndFlush(spotSchema);
    spotId = new EventSpotId(spotSchema.getId());

    testEntityManager.clear();

    service =
        new OrderService(
            new OrderH2Repository(entityManager, new OrderMapper()),
            new SpotReservationH2Repository(entityManager, new SpotReservationMapper()),
            new CustomerH2Repository(entityManager, new CustomerMapper()),
            new EventH2Repository(entityManager, new EventMapper()),
            new UnitOfWorkJpa(entityManager, transactionManager),
            (cardToken, amount) -> {});
  }

  @Test
  @DisplayName("should persist the order, the spot reservation, and mark the spot as reserved")
  void shouldReserveSpotEndToEnd() {
    ReserveSpotCommand command =
        new ReserveSpotCommand(eventId, sectionId, spotId, customerId, VALID_CARD_TOKEN);

    var order = service.reserve(command);

    assertEquals(OrderStatus.PAID, order.getStatus());
    assertEquals(0, VALID_AMOUNT.compareTo(order.getAmount()));

    testEntityManager.clear();

    EventSpotSchema persistedSpot = testEntityManager.find(EventSpotSchema.class, spotId.getValue());
    assertTrue(persistedSpot.isReserved());

    assertNotNull(
        testEntityManager.find(
            com.mba.fc.ingressos.core.events.infra.db.schemas.SpotReservationSchema.class,
            spotId.getValue()));
    assertNotNull(
        testEntityManager.find(
            com.mba.fc.ingressos.core.events.infra.db.schemas.OrderSchema.class,
            order.getId().getValue()));
  }
}
