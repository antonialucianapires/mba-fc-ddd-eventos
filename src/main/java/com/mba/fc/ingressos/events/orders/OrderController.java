package com.mba.fc.ingressos.events.orders;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.OrderId;
import com.mba.fc.ingressos.core.events.application.OrderService;
import com.mba.fc.ingressos.core.events.domain.commands.ReserveSpotCommand;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  public Set<OrderResponse> getOrders() {
    return orderService.list().stream().map(OrderResponse::new).collect(Collectors.toSet());
  }

  @PostMapping
  public OrderResponse reserveSpot(@RequestBody ReserveSpotRequest request) {
    ReserveSpotCommand command =
        new ReserveSpotCommand(
            new EventId(request.eventId()),
            new EventSectionId(request.sectionId()),
            new EventSpotId(request.spotId()),
            new CustomerId(request.customerId()),
            request.cardToken());
    return new OrderResponse(orderService.reserve(command));
  }

  @PostMapping("/{id}/pay")
  public OrderResponse payOrder(@PathVariable String id) {
    return new OrderResponse(orderService.pay(new OrderId(id)));
  }

  @PostMapping("/{id}/cancel")
  public OrderResponse cancelOrder(@PathVariable String id) {
    return new OrderResponse(orderService.cancel(new OrderId(id)));
  }
}
