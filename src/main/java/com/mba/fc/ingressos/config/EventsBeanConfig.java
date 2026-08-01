package com.mba.fc.ingressos.config;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.infra.UnitOfWorkJpa;
import com.mba.fc.ingressos.core.events.application.CustomerService;
import com.mba.fc.ingressos.core.events.application.EventService;
import com.mba.fc.ingressos.core.events.application.OrderService;
import com.mba.fc.ingressos.core.events.application.PartnerService;
import com.mba.fc.ingressos.core.events.application.PaymentGateway;
import com.mba.fc.ingressos.core.events.domain.repositories.ICustomerRepository;
import com.mba.fc.ingressos.core.events.domain.repositories.IEventRepository;
import com.mba.fc.ingressos.core.events.domain.repositories.IOrderRepository;
import com.mba.fc.ingressos.core.events.domain.repositories.IPartnerRepository;
import com.mba.fc.ingressos.core.events.domain.repositories.ISpotReservationRepository;
import com.mba.fc.ingressos.core.events.infra.db.mappers.CustomerMapper;
import com.mba.fc.ingressos.core.events.infra.db.mappers.EventMapper;
import com.mba.fc.ingressos.core.events.infra.db.mappers.OrderMapper;
import com.mba.fc.ingressos.core.events.infra.db.mappers.PartnerMapper;
import com.mba.fc.ingressos.core.events.infra.db.mappers.SpotReservationMapper;
import com.mba.fc.ingressos.core.events.infra.db.repositories.CustomerH2Repository;
import com.mba.fc.ingressos.core.events.infra.db.repositories.EventH2Repository;
import com.mba.fc.ingressos.core.events.infra.db.repositories.OrderH2Repository;
import com.mba.fc.ingressos.core.events.infra.db.repositories.PartnerH2Repository;
import com.mba.fc.ingressos.core.events.infra.db.repositories.SpotReservationH2Repository;
import com.mba.fc.ingressos.core.events.infra.payment.NoopPaymentGateway;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Fábrica dos beans do módulo core.events. O core segue DDD e não conhece o Spring, então é aqui
 * que instanciamos manualmente mappers, repositórios e services e os expomos ao container.
 */
@Configuration
public class EventsBeanConfig {

  @Bean
  public EventMapper eventMapper() {
    return new EventMapper();
  }

  @Bean
  public PartnerMapper partnerMapper() {
    return new PartnerMapper();
  }

  @Bean
  public CustomerMapper customerMapper() {
    return new CustomerMapper();
  }

  @Bean
  public OrderMapper orderMapper() {
    return new OrderMapper();
  }

  @Bean
  public SpotReservationMapper spotReservationMapper() {
    return new SpotReservationMapper();
  }

  @Bean
  public IUnitOfWork unitOfWork(
      EntityManager entityManager, PlatformTransactionManager transactionManager) {
    return new UnitOfWorkJpa(entityManager, transactionManager);
  }

  @Bean
  public IEventRepository eventRepository(EntityManager entityManager, EventMapper eventMapper) {
    return new EventH2Repository(entityManager, eventMapper);
  }

  @Bean
  public IPartnerRepository partnerRepository(
      EntityManager entityManager, PartnerMapper partnerMapper) {
    return new PartnerH2Repository(entityManager, partnerMapper);
  }

  @Bean
  public ICustomerRepository customerRepository(
      EntityManager entityManager, CustomerMapper customerMapper) {
    return new CustomerH2Repository(entityManager, customerMapper);
  }

  @Bean
  public IOrderRepository orderRepository(EntityManager entityManager, OrderMapper orderMapper) {
    return new OrderH2Repository(entityManager, orderMapper);
  }

  @Bean
  public ISpotReservationRepository spotReservationRepository(
      EntityManager entityManager, SpotReservationMapper spotReservationMapper) {
    return new SpotReservationH2Repository(entityManager, spotReservationMapper);
  }

  @Bean
  public PaymentGateway paymentGateway() {
    return new NoopPaymentGateway();
  }

  @Bean
  public EventService eventService(
      IEventRepository eventRepository, IPartnerRepository partnerRepository, IUnitOfWork unitOfWork) {
    return new EventService(eventRepository, partnerRepository, unitOfWork);
  }

  @Bean
  public PartnerService partnerService(IPartnerRepository partnerRepository, IUnitOfWork unitOfWork) {
    return new PartnerService(partnerRepository, unitOfWork);
  }

  @Bean
  public CustomerService customerService(
      ICustomerRepository customerRepository, IUnitOfWork unitOfWork) {
    return new CustomerService(customerRepository, unitOfWork);
  }

  @Bean
  public OrderService orderService(
      IOrderRepository orderRepository,
      ISpotReservationRepository spotReservationRepository,
      ICustomerRepository customerRepository,
      IEventRepository eventRepository,
      IUnitOfWork unitOfWork,
      PaymentGateway paymentGateway) {
    return new OrderService(
        orderRepository,
        spotReservationRepository,
        customerRepository,
        eventRepository,
        unitOfWork,
        paymentGateway);
  }
}
