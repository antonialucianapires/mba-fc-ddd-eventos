package com.mba.fc.ingressos.core.events.application;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.common.infra.UnitOfWorkJpa;
import com.mba.fc.ingressos.core.events.domain.commands.ReserveSpotCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Order;
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
import com.mba.fc.ingressos.core.events.infra.db.schemas.OrderSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.PartnerSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.SpotReservationSchema;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("OrderService reservation concurrency")
class OrderServiceConcurrencyTest {

  private static final BigDecimal VALID_AMOUNT = new BigDecimal("50.00");

  @Autowired private EntityManager entityManager;

  @Autowired private TestEntityManager testEntityManager;

  @Autowired private PlatformTransactionManager transactionManager;

  private CustomerId customerAId;
  private CustomerId customerBId;
  private EventId eventId;
  private EventSectionId sectionId;
  private EventSpotId spotId;

  @BeforeEach
  void setUp() {
    new TransactionTemplate(transactionManager)
        .executeWithoutResult(
            status -> {
              CustomerSchema customerA =
                  new CustomerSchema(UUID.randomUUID().toString(), "52998224725", "Cliente A");
              CustomerSchema customerB =
                  new CustomerSchema(UUID.randomUUID().toString(), "11144477735", "Cliente B");
              entityManager.persist(customerA);
              entityManager.persist(customerB);

              PartnerSchema partnerSchema =
                  new PartnerSchema(UUID.randomUUID().toString(), "Acme Events");
              entityManager.persist(partnerSchema);

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
              entityManager.persist(eventSchema);

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
              entityManager.persist(sectionSchema);

              EventSpotSchema spotSchema =
                  new EventSpotSchema(
                      UUID.randomUUID().toString(), "A1", false, true, sectionSchema);
              entityManager.persist(spotSchema);

              entityManager.flush();

              customerAId = new CustomerId(customerA.getId());
              customerBId = new CustomerId(customerB.getId());
              eventId = new EventId(eventSchema.getId());
              sectionId = new EventSectionId(sectionSchema.getId());
              spotId = new EventSpotId(spotSchema.getId());
            });
  }

  @Test
  @DisplayName("only one of two concurrent reservations for the same spot should succeed")
  void onlyOneConcurrentReservationShouldSucceed() throws InterruptedException {
    CountDownLatch startLatch = new CountDownLatch(1);
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      Callable<Object> attemptA = () -> attemptReserve(customerAId, "tok_a", startLatch);
      Callable<Object> attemptB = () -> attemptReserve(customerBId, "tok_b", startLatch);

      Future<Object> futureA = executor.submit(attemptA);
      Future<Object> futureB = executor.submit(attemptB);

      startLatch.countDown();

      List<Object> results = new ArrayList<>();
      for (Future<Object> future : List.of(futureA, futureB)) {
        try {
          results.add(future.get());
        } catch (ExecutionException e) {
          results.add(e.getCause());
        }
      }

      long successes = results.stream().filter(Order.class::isInstance).count();
      long failures =
          results.stream().filter(SpotAlreadyReservedException.class::isInstance).count();

      assertEquals(1, successes, "exactly one reservation should succeed: " + results);
      assertEquals(
          1,
          failures,
          "the other attempt should fail with SpotAlreadyReservedException: " + results);
    } finally {
      executor.shutdown();
    }

    new TransactionTemplate(transactionManager)
        .executeWithoutResult(
            status -> {
              List<SpotReservationSchema> reservations =
                  entityManager
                      .createQuery(
                          "SELECT r FROM SpotReservationSchema r", SpotReservationSchema.class)
                      .getResultList();
              assertEquals(
                  1, reservations.size(), "exactly one spot reservation should be persisted");

              List<OrderSchema> orders =
                  entityManager
                      .createQuery("SELECT o FROM OrderSchema o", OrderSchema.class)
                      .getResultList();
              assertEquals(
                  1,
                  orders.size(),
                  "exactly one order should be persisted for the winning attempt");
            });
  }

  private Object attemptReserve(
      CustomerId customerId, String cardToken, CountDownLatch startLatch) {
    OrderService service =
        new OrderService(
            new OrderH2Repository(entityManager, new OrderMapper()),
            new SpotReservationH2Repository(entityManager, new SpotReservationMapper()),
            new CustomerH2Repository(entityManager, new CustomerMapper()),
            new EventH2Repository(entityManager, new EventMapper()),
            new UnitOfWorkJpa(entityManager, transactionManager),
            (token, amount) -> {});

    ReserveSpotCommand command =
        new ReserveSpotCommand(eventId, sectionId, spotId, customerId, cardToken);

    try {
      startLatch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }

    return service.reserve(command);
  }
}
