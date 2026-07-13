package com.mba.fc.ingressos.core.events.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.events.domain.commands.UpdateCustomerCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Customer;
import com.mba.fc.ingressos.core.events.domain.repositories.ICustomerRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

@DisplayName("CustomerService")
class CustomerServiceTest {

  private static final String VALID_CPF = "52998224725";
  private static final String VALID_NAME = "John Doe";

  private ICustomerRepository customerRepository;
  private IUnitOfWork unitOfWork;
  private CustomerService service;

  @BeforeEach
  void setUp() {
    customerRepository = mock(ICustomerRepository.class);
    unitOfWork = mock(IUnitOfWork.class);
    service = new CustomerService(customerRepository, unitOfWork);
  }

  @Nested
  @DisplayName("list()")
  class List {

    @Test
    @DisplayName("should return every customer from the repository")
    void shouldReturnAllCustomersFromRepository() {
      Customer customer = Customer.create(VALID_CPF, VALID_NAME);
      when(customerRepository.findAll()).thenReturn(Set.of(customer));

      Set<Customer> customers = service.list();

      assertEquals(Set.of(customer), customers);
    }

    @Test
    @DisplayName("should return an empty set when the repository has no customers")
    void shouldReturnEmptySetWhenRepositoryIsEmpty() {
      when(customerRepository.findAll()).thenReturn(Set.of());

      Set<Customer> customers = service.list();

      assertTrue(customers.isEmpty());
    }

    @Test
    @DisplayName("should not interact with the unit of work")
    void shouldNotInteractWithUnitOfWork() {
      when(customerRepository.findAll()).thenReturn(Set.of());

      service.list();

      verifyNoInteractions(unitOfWork);
    }
  }

  @Nested
  @DisplayName("create(String, String)")
  class Create {

    @Test
    @DisplayName("should create a Customer with the given cpf and name")
    void shouldCreateCustomerWithGivenData() {
      when(customerRepository.add(any(Customer.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Customer created = service.create(VALID_CPF, VALID_NAME);

      assertEquals(VALID_CPF, created.getCpf().getValue());
      assertEquals(VALID_NAME, created.getName());
    }

    @Test
    @DisplayName("should pass the created customer to the repository")
    void shouldAddCreatedCustomerToRepository() {
      when(customerRepository.add(any(Customer.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(VALID_CPF, VALID_NAME);

      verify(customerRepository).add(any(Customer.class));
    }

    @Test
    @DisplayName("should return whatever the repository returns")
    void shouldReturnRepositoryResult() {
      Customer persisted = Customer.create(VALID_CPF, VALID_NAME);
      when(customerRepository.add(any(Customer.class))).thenReturn(persisted);

      Customer created = service.create(VALID_CPF, VALID_NAME);

      assertSame(persisted, created);
    }

    @Test
    @DisplayName("should commit the unit of work after adding the customer")
    void shouldCommitUnitOfWork() {
      when(customerRepository.add(any(Customer.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(VALID_CPF, VALID_NAME);

      verify(unitOfWork).commit();
    }

    @Test
    @DisplayName("should commit only after the customer was added to the repository")
    void shouldCommitAfterAddingToRepository() {
      when(customerRepository.add(any(Customer.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(VALID_CPF, VALID_NAME);

      InOrder inOrder = inOrder(customerRepository, unitOfWork);
      inOrder.verify(customerRepository).add(any(Customer.class));
      inOrder.verify(unitOfWork).commit();
    }

    @Test
    @DisplayName("should never roll back the unit of work")
    void shouldNeverRollBackUnitOfWork() {
      when(customerRepository.add(any(Customer.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(VALID_CPF, VALID_NAME);

      verify(unitOfWork, never()).rollback();
    }
  }

  @Nested
  @DisplayName("update(CustomerId, UpdateCustomerCommand)")
  class Update {

    @Test
    @DisplayName("should change the name when the command has a name present")
    void shouldChangeNameWhenPresent() {
      Customer customer = Customer.create(VALID_CPF, VALID_NAME);
      when(customerRepository.findById(customer.getId())).thenReturn(customer);
      when(customerRepository.add(any(Customer.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Customer updated =
          service.update(customer.getId(), new UpdateCustomerCommand(Optional.of("Jane Doe")));

      assertEquals("Jane Doe", updated.getName());
    }

    @Test
    @DisplayName("should keep the current name when the command has no name")
    void shouldKeepNameWhenAbsent() {
      Customer customer = Customer.create(VALID_CPF, VALID_NAME);
      when(customerRepository.findById(customer.getId())).thenReturn(customer);
      when(customerRepository.add(any(Customer.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Customer updated =
          service.update(customer.getId(), new UpdateCustomerCommand(Optional.empty()));

      assertEquals(VALID_NAME, updated.getName());
    }

    @Test
    @DisplayName("should throw when no customer is found for the given id")
    void shouldThrowWhenCustomerNotFound() {
      when(customerRepository.findById(any())).thenReturn(null);

      assertThrows(
          IllegalArgumentException.class,
          () ->
              service.update(new CustomerId(), new UpdateCustomerCommand(Optional.of("Jane Doe"))));
    }

    @Test
    @DisplayName("should commit the unit of work after updating the customer")
    void shouldCommitUnitOfWork() {
      Customer customer = Customer.create(VALID_CPF, VALID_NAME);
      when(customerRepository.findById(customer.getId())).thenReturn(customer);
      when(customerRepository.add(any(Customer.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.update(customer.getId(), new UpdateCustomerCommand(Optional.of("Jane Doe")));

      verify(unitOfWork).commit();
    }
  }
}
