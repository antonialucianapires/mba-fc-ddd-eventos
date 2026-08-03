package com.mba.fc.ingressos.core.common.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.Uuid;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AggregateRoot")
class AggregateRootTest {

  private static class StubAggregateRoot extends AggregateRoot<CustomerId> {
    StubAggregateRoot(CustomerId id) {
      super(id);
    }

    void raise(IDomainEvent event) {
      addEvent(event);
    }

    void clear() {
      clearEvents();
    }

    @Override
    public String toString() {
      return "StubAggregateRoot{id=" + id.getValue() + "}";
    }
  }

  private static class StubDomainEvent implements IDomainEvent {
    private final Uuid aggregateId;

    StubDomainEvent(Uuid aggregateId) {
      this.aggregateId = aggregateId;
    }

    @Override
    public Uuid getAggregateId() {
      return aggregateId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
      return LocalDateTime.now();
    }

    @Override
    public long getEventVersion() {
      return 1L;
    }
  }

  @Nested
  @DisplayName("getEvents()")
  class GetEvents {

    @Test
    @DisplayName("should start empty when no event was raised")
    void shouldStartEmpty() {
      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());

      assertTrue(aggregate.getEvents().isEmpty());
    }

    @Test
    @DisplayName("should contain events added via addEvent")
    void shouldContainRaisedEvents() {
      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());
      StubDomainEvent event = new StubDomainEvent(aggregate.id);

      aggregate.raise(event);

      assertEquals(1, aggregate.getEvents().size());
      assertTrue(aggregate.getEvents().contains(event));
    }

    @Test
    @DisplayName("should return an unmodifiable view")
    void shouldBeUnmodifiable() {
      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());

      assertThrows(
          UnsupportedOperationException.class,
          () -> aggregate.getEvents().add(new StubDomainEvent(aggregate.id)));
    }
  }

  @Nested
  @DisplayName("clearEvents()")
  class ClearEvents {

    @Test
    @DisplayName("should remove every previously raised event")
    void shouldRemoveEvents() {
      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());
      aggregate.raise(new StubDomainEvent(aggregate.id));

      aggregate.clear();

      assertTrue(aggregate.getEvents().isEmpty());
    }
  }
}
