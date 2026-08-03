package com.mba.fc.ingressos.events.partners;

import com.mba.fc.ingressos.core.events.domain.entities.Partner;

public class PartnerResponse {

  private final String id;
  private final String name;

  public PartnerResponse(Partner partner) {
    this.id = partner.getId().getValue();
    this.name = partner.getName();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
