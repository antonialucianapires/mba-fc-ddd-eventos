package com.mba.fc.ingressos.core.events.domain.domainevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PartnerCreated")
class PartnerCreatedTest {

  private static final String VALID_NAME = "Acme Events";

  @Test
  @DisplayName("should store the given aggregate ID and name")
  void shouldStoreAggregateIdAndName() {
    PartnerId partnerId = new PartnerId();

    PartnerCreated event = new PartnerCreated(partnerId, VALID_NAME);

    assertSame(partnerId, event.getAggregateId());
    assertEquals(VALID_NAME, event.getName());
  }

  @Test
  @DisplayName("should set occurredOn to the moment of creation")
  void shouldSetOccurredOnToNow() {
    LocalDateTime before = LocalDateTime.now();

    PartnerCreated event = new PartnerCreated(new PartnerId(), VALID_NAME);

    LocalDateTime after = LocalDateTime.now();
    assertNotNull(event.getOccurredOn());
    assertTrue(!event.getOccurredOn().isBefore(before) && !event.getOccurredOn().isAfter(after));
  }

  @Test
  @DisplayName("should always report event version 1")
  void shouldReportEventVersionOne() {
    PartnerCreated event = new PartnerCreated(new PartnerId(), VALID_NAME);

    assertEquals(1L, event.getEventVersion());
  }
}
