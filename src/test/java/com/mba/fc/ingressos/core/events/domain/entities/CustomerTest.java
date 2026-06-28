package com.mba.fc.ingressos.core.events.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.Cpf;
import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Customer")
class CustomerTest {

  private static final String VALID_CPF = "52998224725";
  private static final String VALID_NAME = "Lu";

  @Nested
  @DisplayName("Constructor")
  class Construction {

    @Test
    @DisplayName("given no ID, should generate a valid UUID automatically")
    void shouldGenerateIdWhenNotProvided() {
      Customer customer = new Customer(new Cpf(VALID_CPF), VALID_NAME);

      assertNotNull(customer.getId());
      assertNotNull(customer.getId().getValue());
      assertDoesNotThrow(() -> UUID.fromString(customer.getId().getValue()));
    }

    @Test
    @DisplayName("given a String ID, should wrap it into a CustomerId value object")
    void shouldWrapStringIntoCustomerId() {
      String rawId = UUID.randomUUID().toString();
      Customer customer = new Customer(rawId, new Cpf(VALID_CPF), VALID_NAME);

      assertInstanceOf(CustomerId.class, customer.getId());
      assertEquals(rawId, customer.getId().getValue());
    }

    @Test
    @DisplayName("given a CustomerId, should reuse the same instance")
    void shouldReuseCustomerId() {
      CustomerId customerId = new CustomerId();
      Customer customer = new Customer(customerId, new Cpf(VALID_CPF), VALID_NAME);

      assertSame(customerId, customer.getId());
    }

    @Test
    @DisplayName("given a valid CPF string, should store it as a Cpf value object")
    void shouldStoreCpfAsValueObject() {
      Cpf cpf = new Cpf(VALID_CPF);
      Customer customer = new Customer(cpf, VALID_NAME);

      assertInstanceOf(Cpf.class, customer.getCpf());
      assertEquals(VALID_CPF, customer.getCpf().getValue());
    }

    @Test
    @DisplayName("given a name, should store it correctly")
    void shouldStoreName() {
      Customer customer = new Customer(new Cpf(VALID_CPF), VALID_NAME);

      assertEquals(VALID_NAME, customer.getName());
    }
  }

  @Nested
  @DisplayName("Factory method create()")
  class FactoryMethod {

    @Test
    @DisplayName("should create a customer with a valid UUID as ID")
    void shouldCreateWithValidUuid() {
      Customer customer = Customer.create(VALID_CPF, VALID_NAME);

      assertNotNull(customer.getId());
      assertDoesNotThrow(() -> UUID.fromString(customer.getId().getValue()));
    }

    @Test
    @DisplayName("should wrap the CPF string into a Cpf value object")
    void shouldWrapCpfStringIntoValueObject() {
      Customer customer = Customer.create(VALID_CPF, VALID_NAME);

      assertInstanceOf(Cpf.class, customer.getCpf());
      assertEquals(VALID_CPF, customer.getCpf().getValue());
    }

    @Test
    @DisplayName("should throw when CPF has all equal digits")
    void shouldThrowForAllEqualDigitsCpf() {
      assertThrows(
          IllegalArgumentException.class, () -> Customer.create("00000000000", VALID_NAME));
    }

    @Test
    @DisplayName("should throw when CPF length is invalid")
    void shouldThrowForInvalidCpfLength() {
      assertThrows(IllegalArgumentException.class, () -> Customer.create("123", VALID_NAME));
    }

    @Test
    @DisplayName("should throw when CPF check digits are invalid")
    void shouldThrowForInvalidCheckDigits() {
      assertThrows(
          IllegalArgumentException.class, () -> Customer.create("11122233344", VALID_NAME));
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal when both customers share the same ID regardless of other fields")
    void shouldBeEqualWithSameId() {
      String id = UUID.randomUUID().toString();
      Customer a = new Customer(id, new Cpf(VALID_CPF), "Lu");
      Customer b = new Customer(id, new Cpf(VALID_CPF), "Different Name");

      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("should not be equal when customers have different IDs")
    void shouldNotBeEqualWithDifferentIds() {
      Customer a = new Customer(new Cpf(VALID_CPF), VALID_NAME);
      Customer b = new Customer(new Cpf(VALID_CPF), VALID_NAME);

      assertNotEquals(a, b);
    }

    @Test
    @DisplayName("should be equal to itself")
    void shouldBeEqualToItself() {
      Customer customer = new Customer(new Cpf(VALID_CPF), VALID_NAME);

      assertEquals(customer, customer);
    }
  }

  @Nested
  @DisplayName("toString")
  class ToStringTest {

    @Test
    @DisplayName("should not expose CPF to comply with LGPD")
    void shouldNotExposeCpf() {
      Customer customer = Customer.create(VALID_CPF, VALID_NAME);

      assertFalse(customer.toString().contains(VALID_CPF));
    }

    @Test
    @DisplayName("should contain the customer ID and name")
    void shouldContainIdAndName() {
      Customer customer = Customer.create(VALID_CPF, VALID_NAME);

      assertTrue(customer.toString().contains(customer.getId().getValue()));
      assertTrue(customer.toString().contains(VALID_NAME));
    }
  }
}
