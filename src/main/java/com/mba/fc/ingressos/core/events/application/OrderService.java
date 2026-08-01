package com.mba.fc.ingressos.core.events.application;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.valueobjects.OrderId;
import com.mba.fc.ingressos.core.events.domain.commands.ReserveSpotCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Customer;
import com.mba.fc.ingressos.core.events.domain.entities.Event;
import com.mba.fc.ingressos.core.events.domain.entities.Order;
import com.mba.fc.ingressos.core.events.domain.entities.OrderStatus;
import com.mba.fc.ingressos.core.events.domain.entities.SpotReservation;
import com.mba.fc.ingressos.core.events.domain.repositories.ICustomerRepository;
import com.mba.fc.ingressos.core.events.domain.repositories.IEventRepository;
import com.mba.fc.ingressos.core.events.domain.repositories.IOrderRepository;
import com.mba.fc.ingressos.core.events.domain.repositories.ISpotReservationRepository;
import java.math.BigDecimal;
import java.util.Set;

public class OrderService {

  private final IOrderRepository orderRepository;
  private final ISpotReservationRepository spotReservationRepository;
  private final ICustomerRepository customerRepository;
  private final IEventRepository eventRepository;
  private final IUnitOfWork unitOfWork;
  private final PaymentGateway paymentGateway;

  public OrderService(
      IOrderRepository orderRepository,
      ISpotReservationRepository spotReservationRepository,
      ICustomerRepository customerRepository,
      IEventRepository eventRepository,
      IUnitOfWork unitOfWork,
      PaymentGateway paymentGateway) {
    this.orderRepository = orderRepository;
    this.spotReservationRepository = spotReservationRepository;
    this.customerRepository = customerRepository;
    this.eventRepository = eventRepository;
    this.unitOfWork = unitOfWork;
    this.paymentGateway = paymentGateway;
  }

  public Set<Order> list() {
    return orderRepository.findAll();
  }

  public Order reserve(ReserveSpotCommand command) {
    Order order = unitOfWork.runTransaction(() -> reserveWithinTransaction(command));

    if (order.getStatus() == OrderStatus.CANCELED) {
      throw new ReservationPaymentFailedException(
          "Não foi possível concluir a reserva do seu lugar", order.getId(), null);
    }
    return order;
  }

  private Order reserveWithinTransaction(ReserveSpotCommand command) {
    Customer customer = customerRepository.findById(command.customerId());
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found");
    }
    Event event = eventRepository.findById(command.eventId());
    if (event == null) {
      throw new IllegalArgumentException("Event not found");
    }
    if (!event.allowReserveSpot(command.sectionId(), command.spotId())) {
      throw new SpotNotAvailableException("Spot not available");
    }
    if (spotReservationRepository.findById(command.spotId()) != null) {
      throw new SpotAlreadyReservedException("Spot is already reserved");
    }

    SpotReservation reservation = SpotReservation.create(command.spotId(), customer.getId());
    try {
      spotReservationRepository.add(reservation);
      unitOfWork.commit();
    } catch (RuntimeException e) {
      throw new SpotAlreadyReservedException("Spot is already reserved", e);
    }

    BigDecimal amount = event.getSectionPrice(command.sectionId());

    try {
      paymentGateway.payment(command.cardToken(), amount);

      Order order = Order.create(customer.getId(), amount, command.spotId()).pay();
      orderRepository.add(order);

      event.markSpotAsReserved(command.sectionId(), command.spotId());
      eventRepository.add(event);

      unitOfWork.commit();
      return order;
    } catch (PaymentFailedException e) {
      Order order = Order.create(customer.getId(), amount, command.spotId()).cancel();
      orderRepository.add(order);
      unitOfWork.commit();
      return order;
    }
  }

  public Order pay(OrderId id) {
    Order order = orderRepository.findById(id);
    if (order == null) {
      throw new IllegalArgumentException("Order not found");
    }
    Order orderPaid = orderRepository.add(order.pay());
    unitOfWork.commit();
    return orderPaid;
  }

  public Order cancel(OrderId id) {
    Order order = orderRepository.findById(id);
    if (order == null) {
      throw new IllegalArgumentException("Order not found");
    }
    Order orderCanceled = orderRepository.add(order.cancel());
    unitOfWork.commit();
    return orderCanceled;
  }
}
