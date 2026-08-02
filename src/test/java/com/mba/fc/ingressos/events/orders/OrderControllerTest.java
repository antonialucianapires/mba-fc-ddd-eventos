package com.mba.fc.ingressos.events.orders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.OrderId;
import com.mba.fc.ingressos.core.events.application.OrderService;
import com.mba.fc.ingressos.core.events.domain.commands.ReserveSpotCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Order;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@DisplayName("OrderController")
class OrderControllerTest {

  private static final BigDecimal VALID_AMOUNT = new BigDecimal("50.00");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private OrderService orderService;

  private Order newOrder() {
    return Order.create(new CustomerId(), VALID_AMOUNT, new EventSpotId());
  }

  @Test
  @DisplayName("GET /orders should return every order")
  void shouldListOrders() throws Exception {
    Order order = newOrder();
    when(orderService.list()).thenReturn(Set.of(order));

    mockMvc
        .perform(get("/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(order.getId().getValue()))
        .andExpect(jsonPath("$[0].status").value("PENDING"));
  }

  @Test
  @DisplayName("POST /orders should reserve a spot and return the order")
  void shouldReserveSpot() throws Exception {
    Order order = newOrder().pay();
    when(orderService.reserve(any(ReserveSpotCommand.class))).thenReturn(order);

    mockMvc
        .perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"eventId\":\""
                        + new com.mba.fc.ingressos.core.common.domain.valueobjects.EventId().getValue()
                        + "\",\"sectionId\":\""
                        + new com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId()
                            .getValue()
                        + "\",\"spotId\":\""
                        + new EventSpotId().getValue()
                        + "\",\"customerId\":\""
                        + new CustomerId().getValue()
                        + "\",\"cardToken\":\"tok_visa\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PAID"));

    verify(orderService).reserve(any(ReserveSpotCommand.class));
  }

  @Test
  @DisplayName("POST /orders/{id}/pay should pay the order")
  void shouldPayOrder() throws Exception {
    Order order = newOrder().pay();
    String id = order.getId().getValue();
    when(orderService.pay(new OrderId(id))).thenReturn(order);

    mockMvc
        .perform(post("/orders/{id}/pay", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PAID"));

    verify(orderService).pay(new OrderId(id));
  }

  @Test
  @DisplayName("POST /orders/{id}/cancel should cancel the order")
  void shouldCancelOrder() throws Exception {
    Order order = newOrder().cancel();
    String id = order.getId().getValue();
    when(orderService.cancel(new OrderId(id))).thenReturn(order);

    mockMvc
        .perform(post("/orders/{id}/cancel", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELED"));

    verify(orderService).cancel(new OrderId(id));
  }
}
