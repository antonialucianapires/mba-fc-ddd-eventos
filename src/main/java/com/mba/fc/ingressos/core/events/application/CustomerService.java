package com.mba.fc.ingressos.core.events.application;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.events.domain.entities.Customer;
import com.mba.fc.ingressos.core.events.domain.repositories.ICustomerRepository;
import java.util.Set;

public class CustomerService {

  private final ICustomerRepository customerRepository;
  private final IUnitOfWork unitOfWork;

  public CustomerService(ICustomerRepository customerRepository, IUnitOfWork unitOfWork) {
    this.customerRepository = customerRepository;
    this.unitOfWork = unitOfWork;
  }

  public Set<Customer> list() {
    return customerRepository.findAll();
  }

  public Customer create(String cpf, String name) {
    Customer customer = Customer.create(cpf, name);
    Customer customerSaved = customerRepository.add(customer);
    unitOfWork.commit();
    return customerSaved;
  }
}
