package com.mba.fc.ingressos.core.events.infra.db.mappers;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSpotId;
import com.mba.fc.ingressos.core.events.domain.entities.Order;
import com.mba.fc.ingressos.core.events.infra.db.schemas.CustomerSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSpotSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.OrderSchema;

public class OrderMapper {

  public Order toDomain(OrderSchema schema) {
    return new Order(
        schema.getId(),
        new CustomerId(schema.getCustomer().getId()),
        schema.getAmount(),
        new EventSpotId(schema.getEventSpot().getId()),
        schema.getStatus());
  }

  public OrderSchema toSchema(Order domain) {
    CustomerSchema customerSchema =
        new CustomerSchema(domain.getCustomerId().getValue(), null, null);
    EventSpotSchema eventSpotSchema =
        new EventSpotSchema(domain.getEventSpotId().getValue(), null, false, false, null);
    return new OrderSchema(
        domain.getId().getValue(),
        domain.getAmount(),
        domain.getStatus(),
        customerSchema,
        eventSpotSchema);
  }
}
