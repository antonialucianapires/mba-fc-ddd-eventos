package com.mba.fc.ingressos.core.events.infra.db.repositories;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.events.domain.entities.Customer;
import com.mba.fc.ingressos.core.events.infra.db.mappers.CustomerMapper;
import com.mba.fc.ingressos.core.events.infra.db.schemas.CustomerSchema;
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
@DisplayName("CustomerH2Repository")
class CustomerH2RepositoryTest {

  private static final String VALID_CPF = "52998224725";
  private static final String VALID_NAME = "John Doe";

  @Autowired private EntityManager entityManager;

  @Autowired private TestEntityManager testEntityManager;

  private final CustomerMapper customerMapper = new CustomerMapper();

  private CustomerH2Repository repository;

  @BeforeEach
  void setUp() {
    repository = new CustomerH2Repository(entityManager, customerMapper);
  }

  @Nested
  @DisplayName("add(Customer)")
  class Add {

    @Test
    @DisplayName("should persist the customer so it can be found directly in the database")
    void shouldPersistCustomer() {
      Customer customer = Customer.create(VALID_CPF, VALID_NAME);

      repository.add(customer);
      testEntityManager.flush();
      testEntityManager.clear();

      CustomerSchema found =
          testEntityManager.find(CustomerSchema.class, customer.getId().getValue());

      assertNotNull(found);
      assertEquals(VALID_CPF, found.getCpf());
      assertEquals(VALID_NAME, found.getName());
    }

    @Test
    @DisplayName("should return a Customer with the same data that was added")
    void shouldReturnMappedCustomer() {
      Customer customer = Customer.create(VALID_CPF, VALID_NAME);

      Customer added = repository.add(customer);

      assertEquals(customer.getId().getValue(), added.getId().getValue());
      assertEquals(customer.getCpf().getValue(), added.getCpf().getValue());
      assertEquals(customer.getName(), added.getName());
    }
  }

  @Nested
  @DisplayName("findById(Uuid)")
  class FindById {

    @Test
    @DisplayName("should return the customer when it exists")
    void shouldReturnExistingCustomer() {
      CustomerSchema schema =
          new CustomerSchema(UUID.randomUUID().toString(), VALID_CPF, VALID_NAME);
      testEntityManager.persistAndFlush(schema);
      testEntityManager.clear();

      Customer found = repository.findById(new CustomerId(schema.getId()));

      assertNotNull(found);
      assertEquals(schema.getId(), found.getId().getValue());
      assertEquals(schema.getName(), found.getName());
    }

    @Test
    @DisplayName("should return null when the customer does not exist")
    void shouldReturnNullWhenNotFound() {
      Customer found = repository.findById(new CustomerId(UUID.randomUUID().toString()));

      assertNull(found);
    }
  }

  @Nested
  @DisplayName("findAll()")
  class FindAll {

    @Test
    @DisplayName("should return every persisted customer")
    void shouldReturnAllCustomers() {
      testEntityManager.persistAndFlush(
          new CustomerSchema(UUID.randomUUID().toString(), "52998224725", "Customer A"));
      testEntityManager.persistAndFlush(
          new CustomerSchema(UUID.randomUUID().toString(), "11144477735", "Customer B"));
      testEntityManager.clear();

      Set<Customer> customers = repository.findAll();

      assertEquals(2, customers.size());
    }

    @Test
    @DisplayName("should return an empty set when there are no customers")
    void shouldReturnEmptySetWhenNoCustomers() {
      Set<Customer> customers = repository.findAll();

      assertTrue(customers.isEmpty());
    }
  }

  @Nested
  @DisplayName("delete(Uuid)")
  class Delete {

    @Test
    @DisplayName("should remove the customer from the database")
    void shouldRemoveCustomer() {
      CustomerSchema schema =
          new CustomerSchema(UUID.randomUUID().toString(), VALID_CPF, VALID_NAME);
      testEntityManager.persistAndFlush(schema);

      repository.delete(new CustomerId(schema.getId()));
      testEntityManager.clear();

      assertNull(testEntityManager.find(CustomerSchema.class, schema.getId()));
    }

    @Test
    @DisplayName("should not throw when the customer does not exist")
    void shouldNotThrowWhenCustomerDoesNotExist() {
      assertDoesNotThrow(() -> repository.delete(new CustomerId(UUID.randomUUID().toString())));
    }
  }
}
