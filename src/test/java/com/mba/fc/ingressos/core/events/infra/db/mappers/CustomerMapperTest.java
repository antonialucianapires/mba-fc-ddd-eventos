package com.mba.fc.ingressos.core.events.infra.db.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.Cpf;
import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.events.domain.entities.Customer;
import com.mba.fc.ingressos.core.events.infra.db.schemas.CustomerSchema;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CustomerMapper")
class CustomerMapperTest {

  private static final String VALID_ID = UUID.randomUUID().toString();
  private static final String VALID_CPF = "52998224725";
  private static final String VALID_NAME = "John Doe";

  private final CustomerMapper mapper = new CustomerMapper();

  @Nested
  @DisplayName("toDomain(CustomerSchema)")
  class ToDomain {

    @Test
    @DisplayName("should wrap the schema ID into a CustomerId value object")
    void shouldWrapIdIntoCustomerId() {
      CustomerSchema schema = new CustomerSchema(VALID_ID, VALID_CPF, VALID_NAME);

      Customer customer = mapper.toDomain(schema);

      assertInstanceOf(CustomerId.class, customer.getId());
      assertEquals(VALID_ID, customer.getId().getValue());
    }

    @Test
    @DisplayName("should wrap the schema cpf into a Cpf value object")
    void shouldWrapCpfIntoCpf() {
      CustomerSchema schema = new CustomerSchema(VALID_ID, VALID_CPF, VALID_NAME);

      Customer customer = mapper.toDomain(schema);

      assertInstanceOf(Cpf.class, customer.getCpf());
      assertEquals(VALID_CPF, customer.getCpf().getValue());
    }

    @Test
    @DisplayName("each schema field should survive the mapping independently")
    void shouldMapAllFields() {
      CustomerSchema schema = new CustomerSchema(VALID_ID, VALID_CPF, VALID_NAME);

      Customer customer = mapper.toDomain(schema);

      assertEquals(VALID_ID, customer.getId().getValue());
      assertEquals(VALID_CPF, customer.getCpf().getValue());
      assertEquals(VALID_NAME, customer.getName());
    }
  }

  @Nested
  @DisplayName("toSchema(Customer)")
  class ToSchema {

    @Test
    @DisplayName("should return a CustomerSchema with the same ID as the customer")
    void shouldMapIdFromCustomer() {
      Customer customer = new Customer(VALID_ID, new Cpf(VALID_CPF), VALID_NAME);

      CustomerSchema schema = mapper.toSchema(customer);

      assertEquals(VALID_ID, schema.getId());
    }

    @Test
    @DisplayName("should store the customer cpf as a plain String, not a Cpf object")
    void shouldStoreCpfAsString() {
      Customer customer = new Customer(VALID_ID, new Cpf(VALID_CPF), VALID_NAME);

      CustomerSchema schema = mapper.toSchema(customer);

      assertInstanceOf(String.class, schema.getCpf());
      assertEquals(VALID_CPF, schema.getCpf());
    }

    @Test
    @DisplayName("each customer field should survive the mapping independently")
    void shouldMapAllFields() {
      Customer customer = new Customer(VALID_ID, new Cpf(VALID_CPF), VALID_NAME);

      CustomerSchema schema = mapper.toSchema(customer);

      assertEquals(VALID_ID, schema.getId());
      assertEquals(VALID_CPF, schema.getCpf());
      assertEquals(VALID_NAME, schema.getName());
    }
  }

  @Nested
  @DisplayName("Round-trip")
  class RoundTrip {

    @Test
    @DisplayName("toDomain(toSchema(customer)) should preserve all fields")
    void domainToSchemaAndBackShouldPreserveFields() {
      Customer original = new Customer(VALID_ID, new Cpf(VALID_CPF), VALID_NAME);

      Customer roundTripped = mapper.toDomain(mapper.toSchema(original));

      assertEquals(original.getId().getValue(), roundTripped.getId().getValue());
      assertEquals(original.getCpf().getValue(), roundTripped.getCpf().getValue());
      assertEquals(original.getName(), roundTripped.getName());
    }
  }
}
