package com.mba.fc.ingressos.core.events.infra.db.repositories;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.entities.Partner;
import com.mba.fc.ingressos.core.events.infra.db.mappers.PartnerMapper;
import com.mba.fc.ingressos.core.events.infra.db.schemas.PartnerSchema;
import jakarta.persistence.EntityManager;
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
@DisplayName("PartnerH2Repository")
class PartnerH2RepositoryTest {

  @Autowired private EntityManager entityManager;

  @Autowired private TestEntityManager testEntityManager;

  private final PartnerMapper partnerMapper = new PartnerMapper();

  private PartnerH2Repository repository;

  @BeforeEach
  void setUp() {
    repository = new PartnerH2Repository(entityManager, partnerMapper);
  }

  @Nested
  @DisplayName("add(Partner)")
  class Add {

    @Test
    @DisplayName("should persist the partner so it can be found directly in the database")
    void shouldPersistPartner() {
      Partner partner = Partner.create("Acme Events");

      repository.add(partner);
      testEntityManager.flush();
      testEntityManager.clear();

      PartnerSchema found = testEntityManager.find(PartnerSchema.class, partner.getId().getValue());

      assertNotNull(found);
      assertEquals(partner.getId().getValue(), found.getId());
      assertEquals("Acme Events", found.getName());
    }

    @Test
    @DisplayName("should return a Partner with the same data that was added")
    void shouldReturnMappedPartner() {
      Partner partner = Partner.create("Acme Events");

      Partner added = repository.add(partner);

      assertEquals(partner.getId().getValue(), added.getId().getValue());
      assertEquals(partner.getName(), added.getName());
    }
  }

  @Nested
  @DisplayName("findById(Uuid)")
  class FindById {

    @Test
    @DisplayName("should return the partner when it exists")
    void shouldReturnExistingPartner() {
      PartnerSchema schema = new PartnerSchema(UUID.randomUUID().toString(), "Acme Events");
      testEntityManager.persistAndFlush(schema);
      testEntityManager.clear();

      Partner found = repository.findById(new PartnerId(schema.getId()));

      assertNotNull(found);
      assertEquals(schema.getId(), found.getId().getValue());
      assertEquals(schema.getName(), found.getName());
    }

    @Test
    @DisplayName("should return null when the partner does not exist")
    void shouldReturnNullWhenNotFound() {
      Partner found = repository.findById(new PartnerId(UUID.randomUUID().toString()));

      assertNull(found);
    }
  }

  @Nested
  @DisplayName("findAll()")
  class FindAll {

    @Test
    @DisplayName("should return every persisted partner")
    void shouldReturnAllPartners() {
      testEntityManager.persistAndFlush(
          new PartnerSchema(UUID.randomUUID().toString(), "Partner A"));
      testEntityManager.persistAndFlush(
          new PartnerSchema(UUID.randomUUID().toString(), "Partner B"));
      testEntityManager.clear();

      Set<Partner> partners = repository.findAll();

      assertEquals(2, partners.size());
    }

    @Test
    @DisplayName("should return an empty set when there are no partners")
    void shouldReturnEmptySetWhenNoPartners() {
      Set<Partner> partners = repository.findAll();

      assertTrue(partners.isEmpty());
    }
  }

  @Nested
  @DisplayName("delete(Uuid)")
  class Delete {

    @Test
    @DisplayName("should remove the partner from the database")
    void shouldRemovePartner() {
      PartnerSchema schema = new PartnerSchema(UUID.randomUUID().toString(), "Acme Events");
      testEntityManager.persistAndFlush(schema);

      repository.delete(new PartnerId(schema.getId()));
      testEntityManager.clear();

      assertNull(testEntityManager.find(PartnerSchema.class, schema.getId()));
    }

    @Test
    @DisplayName("should not throw when the partner does not exist")
    void shouldNotThrowWhenPartnerDoesNotExist() {
      assertDoesNotThrow(() -> repository.delete(new PartnerId(UUID.randomUUID().toString())));
    }
  }
}
