package com.mba.fc.ingressos.core.events.application;

import com.mba.fc.ingressos.core.events.domain.entities.Customer;
import com.mba.fc.ingressos.core.events.domain.repositories.ICustomerRepository;
import java.util.Set;

public class CustomerService {

  private final ICustomerRepository customerRepository;

  public CustomerService(ICustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  public Set<Customer> list() {
    return customerRepository.findAll();
  }

  public Customer create(String cpf, String name) {
    Customer customer = Customer.create(cpf, name);
    return customerRepository.add(customer);
  }
}
