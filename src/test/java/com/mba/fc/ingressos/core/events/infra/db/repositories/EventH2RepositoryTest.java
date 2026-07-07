package com.mba.fc.ingressos.core.events.infra.db.repositories;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.entities.Event;
import com.mba.fc.ingressos.core.events.infra.db.mappers.EventMapper;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.PartnerSchema;
import jakarta.persistence.EntityManager;
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
@DisplayName("EventH2Repository")
class EventH2RepositoryTest {

  private static final String VALID_NAME = "Rock in Rio";
  private static final String VALID_DESCRIPTION = "Festival de musica";
  private static final LocalDate VALID_DATE = LocalDate.of(2026, 9, 12);

  @Autowired private EntityManager entityManager;

  @Autowired private TestEntityManager testEntityManager;

  private final EventMapper eventMapper = new EventMapper();

  private EventH2Repository repository;

  private PartnerId partnerId;

  @BeforeEach
  void setUp() {
    repository = new EventH2Repository(entityManager, eventMapper);

    PartnerSchema partnerSchema = new PartnerSchema(UUID.randomUUID().toString(), "Acme Events");
    testEntityManager.persistAndFlush(partnerSchema);
    partnerId = new PartnerId(partnerSchema.getId());
  }

  @Nested
  @DisplayName("add(Event)")
  class Add {

    @Test
    @DisplayName("should persist the event so it can be found directly in the database")
    void shouldPersistEvent() {
      Event event =
          new Event(
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              0,
              0,
              partnerId,
              new LinkedHashSet<>());

      repository.add(event);
      testEntityManager.flush();
      testEntityManager.clear();

      EventSchema found = testEntityManager.find(EventSchema.class, event.getId().getValue());

      assertNotNull(found);
      assertEquals(VALID_NAME, found.getName());
      assertEquals(partnerId.getValue(), found.getPartner().getId());
    }

    @Test
    @DisplayName("should return an Event with the same data that was added")
    void shouldReturnMappedEvent() {
      Event event =
          new Event(
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              0,
              0,
              partnerId,
              new LinkedHashSet<>());

      Event added = repository.add(event);

      assertEquals(event.getId().getValue(), added.getId().getValue());
      assertEquals(event.getName(), added.getName());
      assertEquals(event.getPartnerId().getValue(), added.getPartnerId().getValue());
    }
  }

  @Nested
  @DisplayName("findById(Uuid)")
  class FindById {

    @Test
    @DisplayName("should return the event when it exists")
    void shouldReturnExistingEvent() {
      Event event =
          new Event(
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              0,
              0,
              partnerId,
              new LinkedHashSet<>());
      testEntityManager.persistAndFlush(eventMapper.toSchema(event));
      testEntityManager.clear();

      Event found = repository.findById(event.getId());

      assertNotNull(found);
      assertEquals(event.getId().getValue(), found.getId().getValue());
      assertEquals(event.getName(), found.getName());
    }

    @Test
    @DisplayName("should return null when the event does not exist")
    void shouldReturnNullWhenNotFound() {
      Event found = repository.findById(new EventId(UUID.randomUUID().toString()));

      assertNull(found);
    }
  }

  @Nested
  @DisplayName("findAll()")
  class FindAll {

    @Test
    @DisplayName("should return every persisted event")
    void shouldReturnAllEvents() {
      Event event1 =
          new Event(
              "Event A",
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              0,
              0,
              partnerId,
              new LinkedHashSet<>());
      Event event2 =
          new Event(
              "Event B",
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              0,
              0,
              partnerId,
              new LinkedHashSet<>());
      testEntityManager.persistAndFlush(eventMapper.toSchema(event1));
      testEntityManager.persistAndFlush(eventMapper.toSchema(event2));
      testEntityManager.clear();

      Set<Event> events = repository.findAll();

      assertEquals(2, events.size());
    }

    @Test
    @DisplayName("should return an empty set when there are no events")
    void shouldReturnEmptySetWhenNoEvents() {
      Set<Event> events = repository.findAll();

      assertTrue(events.isEmpty());
    }
  }

  @Nested
  @DisplayName("delete(Uuid)")
  class Delete {

    @Test
    @DisplayName("should remove the event from the database")
    void shouldRemoveEvent() {
      Event event =
          new Event(
              VALID_NAME,
              VALID_DESCRIPTION,
              VALID_DATE,
              false,
              0,
              0,
              partnerId,
              new LinkedHashSet<>());
      testEntityManager.persistAndFlush(eventMapper.toSchema(event));

      repository.delete(event.getId());
      testEntityManager.clear();

      assertNull(testEntityManager.find(EventSchema.class, event.getId().getValue()));
    }

    @Test
    @DisplayName("should not throw when the event does not exist")
    void shouldNotThrowWhenEventDoesNotExist() {
      assertDoesNotThrow(() -> repository.delete(new EventId(UUID.randomUUID().toString())));
    }
  }
}
