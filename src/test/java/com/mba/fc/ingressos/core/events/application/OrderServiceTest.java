package com.mba.fc.ingressos.core.events.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.OrderId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.commands.AddSectionCommand;
import com.mba.fc.ingressos.core.events.domain.commands.CreateEventCommand;
import com.mba.fc.ingressos.core.events.domain.commands.ReserveSpotCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Customer;
import com.mba.fc.ingressos.core.events.domain.entities.Event;
import com.mba.fc.ingressos.core.events.domain.entities.EventSection;
import com.mba.fc.ingressos.core.events.domain.entities.EventSpot;
import com.mba.fc.ingressos.core.events.domain.entities.Order;
import com.mba.fc.ingressos.core.events.domain.entities.OrderStatus;
import com.mba.fc.ingressos.core.events.domain.entities.SpotReservation;
import com.mba.fc.ingressos.core.events.domain.repositories.ICustomerRepository;
import com.mba.fc.ingressos.core.events.domain.repositories.IEventRepository;
import com.mba.fc.ingressos.core.events.domain.repositories.IOrderRepository;
import com.mba.fc.ingressos.core.events.domain.repositories.ISpotReservationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

@DisplayName("OrderService")
class OrderServiceTest {

  private static final BigDecimal VALID_AMOUNT = new BigDecimal("50.00");
  private static final String VALID_CARD_TOKEN = "tok_visa";

  private IOrderRepository orderRepository;
  private ISpotReservationRepository spotReservationRepository;
  private ICustomerRepository customerRepository;
  private IEventRepository eventRepository;
  private IUnitOfWork unitOfWork;
  private PaymentGateway paymentGateway;
  private OrderService service;

  private Customer customer;
  private Event publishedEvent;
  private EventSection section;
  private EventSpotId spotId;
  private ReserveSpotCommand validCommand;

  @BeforeEach
  void setUp() {
    orderRepository = mock(IOrderRepository.class);
    spotReservationRepository = mock(ISpotReservationRepository.class);
    customerRepository = mock(ICustomerRepository.class);
    eventRepository = mock(IEventRepository.class);
    unitOfWork = mock(IUnitOfWork.class);
    paymentGateway = mock(PaymentGateway.class);
    service =
        new OrderService(
            orderRepository,
            spotReservationRepository,
            customerRepository,
            eventRepository,
            unitOfWork,
            paymentGateway);

    doAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get())
        .when(unitOfWork)
        .runTransaction(any());

    customer = Customer.create("52998224725", "João da Silva");

    Event event =
        Event.create(
            new CreateEventCommand(
                "Show de Rock",
                "Um grande show",
                LocalDate.of(2026, 12, 31),
                100,
                new PartnerId()));
    event.addSection(new AddSectionCommand("Pista", "Seção pista", 3, VALID_AMOUNT));
    section = event.getSections().iterator().next();
    spotId = section.getSpots().iterator().next().getId();
    publishedEvent = event.publishAll();

    validCommand =
        new ReserveSpotCommand(
            publishedEvent.getId(), section.getId(), spotId, customer.getId(), VALID_CARD_TOKEN);
  }

  private void stubHappyPath() {
    when(customerRepository.findById(customer.getId())).thenReturn(customer);
    when(eventRepository.findById(publishedEvent.getId())).thenReturn(publishedEvent);
    when(spotReservationRepository.findById(spotId)).thenReturn(null);
    when(orderRepository.add(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Nested
  @DisplayName("reserve(ReserveSpotCommand)")
  class Reserve {

    @Test
    @DisplayName("should create a paid order bound to the customer, section price and spot")
    void shouldCreatePaidOrderWithGivenData() {
      stubHappyPath();

      Order created = service.reserve(validCommand);

      assertEquals(customer.getId(), created.getCustomerId());
      assertEquals(section.getPrice(), created.getAmount());
      assertEquals(spotId, created.getEventSpotId());
      assertEquals(OrderStatus.PAID, created.getStatus());
    }

    @Test
    @DisplayName("should run the reservation inside a transaction")
    void shouldRunInsideTransaction() {
      stubHappyPath();

      service.reserve(validCommand);

      verify(unitOfWork).runTransaction(any());
    }

    @Test
    @DisplayName("should charge the card with the section price")
    void shouldChargeCardWithSectionPrice() {
      stubHappyPath();

      service.reserve(validCommand);

      verify(paymentGateway).payment(VALID_CARD_TOKEN, section.getPrice());
    }

    @Test
    @DisplayName("should add a spot reservation for the given spot and customer")
    void shouldAddSpotReservation() {
      stubHappyPath();

      service.reserve(validCommand);

      verify(spotReservationRepository).add(any(SpotReservation.class));
    }

    @Test
    @DisplayName("should mark the spot as reserved on the event and save it")
    void shouldMarkSpotAsReservedOnEvent() {
      stubHappyPath();

      service.reserve(validCommand);

      EventSection updatedSection = publishedEvent.getSections().iterator().next();
      EventSpot spot =
          updatedSection.getSpots().stream()
              .filter(s -> s.getId().equals(spotId))
              .findFirst()
              .orElseThrow();
      assertTrue(spot.isReserved());
      verify(eventRepository).add(publishedEvent);
    }

    @Test
    @DisplayName("should throw when no customer is found for the given id")
    void shouldThrowWhenCustomerNotFound() {
      when(customerRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.reserve(validCommand));

      verifyNoInteractions(
          eventRepository, spotReservationRepository, orderRepository, paymentGateway);
      verify(unitOfWork, never()).commit();
    }

    @Test
    @DisplayName("should throw when no event is found for the given id")
    void shouldThrowWhenEventNotFound() {
      when(customerRepository.findById(customer.getId())).thenReturn(customer);
      when(eventRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.reserve(validCommand));

      verifyNoInteractions(spotReservationRepository, orderRepository, paymentGateway);
      verify(unitOfWork, never()).commit();
    }

    @Test
    @DisplayName("should throw SpotNotAvailableException when the spot is not available")
    void shouldThrowWhenSpotNotAvailable() {
      Event unpublishedEvent = publishedEvent.unpublishAll();
      when(customerRepository.findById(customer.getId())).thenReturn(customer);
      when(eventRepository.findById(publishedEvent.getId())).thenReturn(unpublishedEvent);

      assertThrows(SpotNotAvailableException.class, () -> service.reserve(validCommand));

      verifyNoInteractions(spotReservationRepository, orderRepository, paymentGateway);
      verify(unitOfWork, never()).commit();
    }

    @Test
    @DisplayName("should throw SpotAlreadyReservedException when a reservation already exists")
    void shouldThrowWhenSpotAlreadyReserved() {
      when(customerRepository.findById(customer.getId())).thenReturn(customer);
      when(eventRepository.findById(publishedEvent.getId())).thenReturn(publishedEvent);
      when(spotReservationRepository.findById(spotId))
          .thenReturn(
              SpotReservation.create(spotId, Customer.create("11144477735", "Outro").getId()));

      assertThrows(SpotAlreadyReservedException.class, () -> service.reserve(validCommand));

      verifyNoInteractions(orderRepository, paymentGateway);
      verify(unitOfWork, never()).commit();
    }

    @Test
    @DisplayName(
        "should translate a technical failure while locking the spot into SpotAlreadyReservedException")
    void shouldTranslateReservationFailureIntoBusinessException() {
      when(customerRepository.findById(customer.getId())).thenReturn(customer);
      when(eventRepository.findById(publishedEvent.getId())).thenReturn(publishedEvent);
      when(spotReservationRepository.findById(spotId)).thenReturn(null);
      doThrow(new RuntimeException("constraint violation"))
          .when(spotReservationRepository)
          .add(any(SpotReservation.class));

      assertThrows(SpotAlreadyReservedException.class, () -> service.reserve(validCommand));

      verifyNoInteractions(paymentGateway, orderRepository);
    }

    @Test
    @DisplayName("should commit (flush) after locking the spot and again after payment")
    void shouldCommitTwice() {
      stubHappyPath();

      service.reserve(validCommand);

      verify(unitOfWork, times(2)).commit();
    }

    @Test
    @DisplayName("should lock the spot reservation before charging the card")
    void shouldLockSpotBeforeCharging() {
      stubHappyPath();

      service.reserve(validCommand);

      InOrder inOrder = inOrder(spotReservationRepository, unitOfWork, paymentGateway);
      inOrder.verify(spotReservationRepository).add(any(SpotReservation.class));
      inOrder.verify(unitOfWork).commit();
      inOrder.verify(paymentGateway).payment(anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("should throw ReservationPaymentFailedException when the payment is declined")
    void shouldThrowWhenPaymentFails() {
      when(customerRepository.findById(customer.getId())).thenReturn(customer);
      when(eventRepository.findById(publishedEvent.getId())).thenReturn(publishedEvent);
      when(spotReservationRepository.findById(spotId)).thenReturn(null);
      when(orderRepository.add(any(Order.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
      doThrow(new PaymentFailedException("card declined"))
          .when(paymentGateway)
          .payment(anyString(), any(BigDecimal.class));

      assertThrows(ReservationPaymentFailedException.class, () -> service.reserve(validCommand));
    }

    @Test
    @DisplayName("should still persist a canceled order when the payment is declined")
    void shouldPersistCanceledOrderWhenPaymentFails() {
      when(customerRepository.findById(customer.getId())).thenReturn(customer);
      when(eventRepository.findById(publishedEvent.getId())).thenReturn(publishedEvent);
      when(spotReservationRepository.findById(spotId)).thenReturn(null);
      when(orderRepository.add(any(Order.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
      doThrow(new PaymentFailedException("card declined"))
          .when(paymentGateway)
          .payment(anyString(), any(BigDecimal.class));

      assertThrows(ReservationPaymentFailedException.class, () -> service.reserve(validCommand));

      verify(orderRepository)
          .add(argThat(order -> order != null && order.getStatus() == OrderStatus.CANCELED));
    }

    @Test
    @DisplayName("should not mark the spot as reserved on the event when payment fails")
    void shouldNotMarkSpotAsReservedWhenPaymentFails() {
      when(customerRepository.findById(customer.getId())).thenReturn(customer);
      when(eventRepository.findById(publishedEvent.getId())).thenReturn(publishedEvent);
      when(spotReservationRepository.findById(spotId)).thenReturn(null);
      when(orderRepository.add(any(Order.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
      doThrow(new PaymentFailedException("card declined"))
          .when(paymentGateway)
          .payment(anyString(), any(BigDecimal.class));

      assertThrows(ReservationPaymentFailedException.class, () -> service.reserve(validCommand));

      verify(eventRepository, never()).add(any());
    }
  }

  @Nested
  @DisplayName("pay(OrderId)")
  class Pay {

    @Test
    @DisplayName("should change the order status to PAID")
    void shouldChangeStatusToPaid() {
      Order order = Order.create(customer.getId(), VALID_AMOUNT, spotId);
      when(orderRepository.findById(order.getId())).thenReturn(order);
      when(orderRepository.add(any(Order.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Order paid = service.pay(order.getId());

      assertEquals(OrderStatus.PAID, paid.getStatus());
    }

    @Test
    @DisplayName("should throw when no order is found for the given id")
    void shouldThrowWhenOrderNotFound() {
      when(orderRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.pay(new OrderId()));
    }

    @Test
    @DisplayName("should not add or commit when the order is not found")
    void shouldNotAddOrCommitWhenOrderNotFound() {
      when(orderRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.pay(new OrderId()));

      verify(orderRepository, never()).add(any());
      verify(unitOfWork, never()).commit();
    }

    @Test
    @DisplayName("should throw when the order is not pending")
    void shouldThrowWhenOrderNotPending() {
      Order order = Order.create(customer.getId(), VALID_AMOUNT, spotId).pay();
      when(orderRepository.findById(order.getId())).thenReturn(order);

      assertThrows(IllegalStateException.class, () -> service.pay(order.getId()));
    }

    @Test
    @DisplayName("should commit the unit of work after paying the order")
    void shouldCommitUnitOfWork() {
      Order order = Order.create(customer.getId(), VALID_AMOUNT, spotId);
      when(orderRepository.findById(order.getId())).thenReturn(order);
      when(orderRepository.add(any(Order.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.pay(order.getId());

      verify(unitOfWork).commit();
    }
  }

  @Nested
  @DisplayName("cancel(OrderId)")
  class Cancel {

    @Test
    @DisplayName("should change the order status to CANCELED")
    void shouldChangeStatusToCanceled() {
      Order order = Order.create(customer.getId(), VALID_AMOUNT, spotId);
      when(orderRepository.findById(order.getId())).thenReturn(order);
      when(orderRepository.add(any(Order.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Order canceled = service.cancel(order.getId());

      assertEquals(OrderStatus.CANCELED, canceled.getStatus());
    }

    @Test
    @DisplayName("should throw when no order is found for the given id")
    void shouldThrowWhenOrderNotFound() {
      when(orderRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.cancel(new OrderId()));
    }

    @Test
    @DisplayName("should not add or commit when the order is not found")
    void shouldNotAddOrCommitWhenOrderNotFound() {
      when(orderRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.cancel(new OrderId()));

      verify(orderRepository, never()).add(any());
      verify(unitOfWork, never()).commit();
    }

    @Test
    @DisplayName("should throw when the order is not pending")
    void shouldThrowWhenOrderNotPending() {
      Order order = Order.create(customer.getId(), VALID_AMOUNT, spotId).cancel();
      when(orderRepository.findById(order.getId())).thenReturn(order);

      assertThrows(IllegalStateException.class, () -> service.cancel(order.getId()));
    }

    @Test
    @DisplayName("should commit the unit of work after canceling the order")
    void shouldCommitUnitOfWork() {
      Order order = Order.create(customer.getId(), VALID_AMOUNT, spotId);
      when(orderRepository.findById(order.getId())).thenReturn(order);
      when(orderRepository.add(any(Order.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.cancel(order.getId());

      verify(unitOfWork).commit();
    }
  }
}
