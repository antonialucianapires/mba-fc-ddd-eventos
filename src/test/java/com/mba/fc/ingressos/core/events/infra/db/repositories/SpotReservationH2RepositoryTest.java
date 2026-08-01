package com.mba.fc.ingressos.core.events.infra.db.repositories;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.events.domain.entities.SpotReservation;
import com.mba.fc.ingressos.core.events.infra.db.mappers.SpotReservationMapper;
import com.mba.fc.ingressos.core.events.infra.db.schemas.CustomerSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSectionSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSpotSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.PartnerSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.SpotReservationSchema;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
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
@DisplayName("SpotReservationH2Repository")
class SpotReservationH2RepositoryTest {

  @Autowired private EntityManager entityManager;

  @Autowired private TestEntityManager testEntityManager;

  private final SpotReservationMapper spotReservationMapper = new SpotReservationMapper();

  private SpotReservationH2Repository repository;

  private CustomerId customerId;
  private EventSpotId spotId;

  @BeforeEach
  void setUp() {
    repository = new SpotReservationH2Repository(entityManager, spotReservationMapper);

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
            new BigDecimal("50.00").doubleValue(),
            eventSchema,
            new LinkedHashSet<>());
    testEntityManager.persistAndFlush(sectionSchema);

    EventSpotSchema spotSchema =
        new EventSpotSchema(UUID.randomUUID().toString(), "A1", false, true, sectionSchema);
    testEntityManager.persistAndFlush(spotSchema);
    spotId = new EventSpotId(spotSchema.getId());
  }

  @Nested
  @DisplayName("add(SpotReservation)")
  class Add {

    @Test
    @DisplayName("should persist the reservation using the spot ID as primary key")
    void shouldPersistReservation() {
      SpotReservation reservation = SpotReservation.create(spotId, customerId);

      repository.add(reservation);
      testEntityManager.flush();
      testEntityManager.clear();

      SpotReservationSchema found =
          testEntityManager.find(SpotReservationSchema.class, spotId.getValue());

      assertNotNull(found);
      assertEquals(customerId.getValue(), found.getCustomer().getId());
    }

    @Test
    @DisplayName("should reject a second reservation for the same spot")
    void shouldRejectDuplicateReservationForSameSpot() {
      testEntityManager.persistAndFlush(
          spotReservationMapper.toSchema(SpotReservation.create(spotId, customerId)));
      testEntityManager.clear();

      SpotReservation duplicate = SpotReservation.create(spotId, customerId);
      entityManager.persist(spotReservationMapper.toSchema(duplicate));

      assertThrows(PersistenceException.class, () -> entityManager.flush());
    }
  }

  @Nested
  @DisplayName("findById(Uuid)")
  class FindById {

    @Test
    @DisplayName("should return the reservation when it exists")
    void shouldReturnExistingReservation() {
      SpotReservation reservation = SpotReservation.create(spotId, customerId);
      testEntityManager.persistAndFlush(spotReservationMapper.toSchema(reservation));
      testEntityManager.clear();

      SpotReservation found = repository.findById(spotId);

      assertNotNull(found);
      assertEquals(spotId.getValue(), found.getId().getValue());
    }

    @Test
    @DisplayName("should return null when there is no reservation for the spot")
    void shouldReturnNullWhenNotFound() {
      SpotReservation found = repository.findById(spotId);

      assertNull(found);
    }
  }

  @Nested
  @DisplayName("findAll()")
  class FindAll {

    @Test
    @DisplayName("should return every persisted reservation")
    void shouldReturnAllReservations() {
      testEntityManager.persistAndFlush(
          spotReservationMapper.toSchema(SpotReservation.create(spotId, customerId)));
      testEntityManager.clear();

      Set<SpotReservation> reservations = repository.findAll();

      assertEquals(1, reservations.size());
    }

    @Test
    @DisplayName("should return an empty set when there are no reservations")
    void shouldReturnEmptySetWhenNoReservations() {
      Set<SpotReservation> reservations = repository.findAll();

      assertTrue(reservations.isEmpty());
    }
  }

  @Nested
  @DisplayName("delete(Uuid)")
  class Delete {

    @Test
    @DisplayName("should remove the reservation from the database")
    void shouldRemoveReservation() {
      SpotReservation reservation = SpotReservation.create(spotId, customerId);
      testEntityManager.persistAndFlush(spotReservationMapper.toSchema(reservation));

      repository.delete(spotId);
      testEntityManager.clear();

      assertNull(testEntityManager.find(SpotReservationSchema.class, spotId.getValue()));
    }

    @Test
    @DisplayName("should not throw when there is no reservation for the spot")
    void shouldNotThrowWhenReservationDoesNotExist() {
      assertDoesNotThrow(() -> repository.delete(spotId));
    }
  }
}
