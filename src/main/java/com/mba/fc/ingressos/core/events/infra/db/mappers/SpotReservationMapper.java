package com.mba.fc.ingressos.core.events.infra.db.mappers;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.events.domain.entities.SpotReservation;
import com.mba.fc.ingressos.core.events.infra.db.schemas.CustomerSchema;
import com.mba.fc.ingressos.core.events.infra.db.schemas.SpotReservationSchema;

public class SpotReservationMapper {

  public SpotReservation toDomain(SpotReservationSchema schema) {
    return new SpotReservation(
        schema.getSpotId(), new CustomerId(schema.getCustomer().getId()), schema.getReservedAt());
  }

  public SpotReservationSchema toSchema(SpotReservation domain) {
    CustomerSchema customerSchema =
        new CustomerSchema(domain.getCustomerId().getValue(), null, null);
    return new SpotReservationSchema(
        domain.getId().getValue(), domain.getReservedAt(), customerSchema);
  }
}
