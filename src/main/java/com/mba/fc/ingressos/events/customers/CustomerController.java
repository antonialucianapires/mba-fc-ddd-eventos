package com.mba.fc.ingressos.events.customers;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.events.application.CustomerService;
import com.mba.fc.ingressos.core.events.domain.commands.UpdateCustomerCommand;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

  private final CustomerService customerService;

  public CustomerController(CustomerService customerService) {
    this.customerService = customerService;
  }

  @GetMapping
  public Set<CustomerResponse> getCustomers() {
    return customerService.list().stream().map(CustomerResponse::new).collect(Collectors.toSet());
  }

  @PostMapping
  public CustomerResponse createCustomer(@RequestBody CustomerRequest request) {
    return new CustomerResponse(customerService.create(request.cpf(), request.name()));
  }

  @PutMapping("/{id}")
  public CustomerResponse updateCustomer(
      @PathVariable String id, @RequestBody UpdateCustomerRequest request) {
    UpdateCustomerCommand command = new UpdateCustomerCommand(Optional.ofNullable(request.name()));
    return new CustomerResponse(customerService.update(new CustomerId(id), command));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCustomer(@PathVariable String id) {
    customerService.delete(new CustomerId(id));
  }
}
