package com.mba.fc.ingressos.events.customers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.events.application.CustomerService;
import com.mba.fc.ingressos.core.events.domain.commands.UpdateCustomerCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Customer;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
@DisplayName("CustomerController")
class CustomerControllerTest {

  private static final String VALID_CPF = "52998224725";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CustomerService customerService;

  @Test
  @DisplayName("GET /customers should return every customer")
  void shouldListCustomers() throws Exception {
    Customer customer = Customer.create(VALID_CPF, "John Doe");
    when(customerService.list()).thenReturn(Set.of(customer));

    mockMvc
        .perform(get("/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(customer.getId().getValue()))
        .andExpect(jsonPath("$[0].cpf").value(VALID_CPF))
        .andExpect(jsonPath("$[0].name").value("John Doe"));
  }

  @Test
  @DisplayName("POST /customers should create a customer")
  void shouldCreateCustomer() throws Exception {
    Customer customer = Customer.create(VALID_CPF, "John Doe");
    when(customerService.create(VALID_CPF, "John Doe")).thenReturn(customer);

    mockMvc
        .perform(
            post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cpf\":\"" + VALID_CPF + "\",\"name\":\"John Doe\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("John Doe"));

    verify(customerService).create(VALID_CPF, "John Doe");
  }

  @Test
  @DisplayName("PUT /customers/{id} should update the customer name")
  void shouldUpdateCustomer() throws Exception {
    Customer updated = Customer.create(VALID_CPF, "John Updated");
    String id = updated.getId().getValue();
    when(customerService.update(eq(new CustomerId(id)), any(UpdateCustomerCommand.class)))
        .thenReturn(updated);

    mockMvc
        .perform(
            put("/customers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Updated\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("John Updated"));

    verify(customerService)
        .update(eq(new CustomerId(id)), eq(new UpdateCustomerCommand(Optional.of("John Updated"))));
  }

  @Test
  @DisplayName("DELETE /customers/{id} should return 204")
  void shouldDeleteCustomer() throws Exception {
    String id = new CustomerId().getValue();

    mockMvc.perform(delete("/customers/{id}", id)).andExpect(status().isNoContent());

    verify(customerService).delete(new CustomerId(id));
  }
}
