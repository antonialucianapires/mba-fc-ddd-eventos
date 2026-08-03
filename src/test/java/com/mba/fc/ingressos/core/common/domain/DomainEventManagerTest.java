package com.mba.fc.ingressos.core.common.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.Uuid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DomainEventManager")
class DomainEventManagerTest {

  // Agregado de teste, igual ao StubAggregateRoot do AggregateRootTest: precisamos de uma
  // subclasse concreta porque AggregateRoot é abstrata, e precisamos expor addEvent (protected)
  // para simular um agregado que já acumulou eventos, como o Partner faria no create().
  private static class StubAggregateRoot extends AggregateRoot<CustomerId> {
    StubAggregateRoot(CustomerId id) {
      super(id);
    }

    void raise(IDomainEvent event) {
      addEvent(event);
    }

    @Override
    public String toString() {
      return "StubAggregateRoot{id=" + id.getValue() + "}";
    }
  }

  // Dois tipos de evento distintos só para poder testar o matching por nome de classe
  // (event.getClass().getSimpleName()) e o wildcard. O "occurredOn"/"eventVersion" não importam
  // aqui, só o necessário para satisfazer a interface.
  private static class FirstStubEvent implements IDomainEvent {
    private final Uuid aggregateId;

    FirstStubEvent(Uuid aggregateId) {
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

  private static class SecondStubEvent implements IDomainEvent {
    private final Uuid aggregateId;

    SecondStubEvent(Uuid aggregateId) {
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
  @DisplayName("register + publish with exact pattern")
  class ExactPattern {

    @Test
    @DisplayName("should call the handler registered for the exact event class name")
    void shouldCallHandlerForExactMatch() {
      DomainEventManager manager = new DomainEventManager();
      List<IDomainEvent> received = new CopyOnWriteArrayList<>();
      manager.register(
          "FirstStubEvent",
          event -> {
            received.add(event);
            return CompletableFuture.completedFuture(null);
          });

      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());
      FirstStubEvent event = new FirstStubEvent(aggregate.id);
      aggregate.raise(event);

      manager.publish(aggregate);

      assertEquals(List.of(event), received);
    }

    @Test
    @DisplayName("should not call a handler registered for a different event class name")
    void shouldNotCallHandlerForDifferentEvent() {
      DomainEventManager manager = new DomainEventManager();
      List<IDomainEvent> received = new CopyOnWriteArrayList<>();
      manager.register(
          "SecondStubEvent",
          event -> {
            received.add(event);
            return CompletableFuture.completedFuture(null);
          });

      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());
      aggregate.raise(new FirstStubEvent(aggregate.id));

      manager.publish(aggregate);

      assertTrue(received.isEmpty());
    }

    @Test
    @DisplayName("should call every handler registered for the same pattern")
    void shouldCallEveryHandlerForSamePattern() {
      DomainEventManager manager = new DomainEventManager();
      List<String> calledHandlers = new CopyOnWriteArrayList<>();
      manager.register(
          "FirstStubEvent",
          event -> {
            calledHandlers.add("first-handler");
            return CompletableFuture.completedFuture(null);
          });
      manager.register(
          "FirstStubEvent",
          event -> {
            calledHandlers.add("second-handler");
            return CompletableFuture.completedFuture(null);
          });

      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());
      aggregate.raise(new FirstStubEvent(aggregate.id));

      manager.publish(aggregate);

      assertEquals(List.of("first-handler", "second-handler"), calledHandlers);
    }
  }

  @Nested
  @DisplayName("register + publish with wildcard pattern")
  class WildcardPattern {

    @Test
    @DisplayName("a trailing '*' should match every event whose name starts with the prefix")
    void shouldMatchPrefixWildcard() {
      DomainEventManager manager = new DomainEventManager();
      List<IDomainEvent> received = new CopyOnWriteArrayList<>();
      manager.register(
          "First*",
          event -> {
            received.add(event);
            return CompletableFuture.completedFuture(null);
          });

      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());
      aggregate.raise(new FirstStubEvent(aggregate.id));
      aggregate.raise(new SecondStubEvent(aggregate.id));

      manager.publish(aggregate);

      assertEquals(1, received.size());
      assertTrue(received.get(0) instanceof FirstStubEvent);
    }

    @Test
    @DisplayName("a lone '*' should match every event, regardless of type")
    void shouldMatchEverythingWithLoneWildcard() {
      DomainEventManager manager = new DomainEventManager();
      List<IDomainEvent> received = new CopyOnWriteArrayList<>();
      manager.register(
          "*",
          event -> {
            received.add(event);
            return CompletableFuture.completedFuture(null);
          });

      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());
      aggregate.raise(new FirstStubEvent(aggregate.id));
      aggregate.raise(new SecondStubEvent(aggregate.id));

      manager.publish(aggregate);

      assertEquals(2, received.size());
    }
  }

  @Nested
  @DisplayName("publish ordering and side effects")
  class PublishOrdering {

    @Test
    @DisplayName("should emit events in the same order they were raised on the aggregate")
    void shouldEmitInRaisedOrder() {
      DomainEventManager manager = new DomainEventManager();
      List<String> emittedEventNames = new CopyOnWriteArrayList<>();
      manager.register(
          "*",
          event -> {
            emittedEventNames.add(event.getClass().getSimpleName());
            return CompletableFuture.completedFuture(null);
          });

      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());
      aggregate.raise(new FirstStubEvent(aggregate.id));
      aggregate.raise(new SecondStubEvent(aggregate.id));

      manager.publish(aggregate);

      assertEquals(List.of("FirstStubEvent", "SecondStubEvent"), emittedEventNames);
    }

    @Test
    @DisplayName("should not clear the aggregate's events after publishing")
    void shouldNotClearAggregateEvents() {
      DomainEventManager manager = new DomainEventManager();
      manager.register("FirstStubEvent", event -> CompletableFuture.completedFuture(null));

      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());
      aggregate.raise(new FirstStubEvent(aggregate.id));

      manager.publish(aggregate);

      assertFalse(aggregate.getEvents().isEmpty());
    }

    @Test
    @DisplayName("the returned future should only complete once every async handler settles")
    void shouldWaitForAsyncHandlersToSettle() {
      DomainEventManager manager = new DomainEventManager();
      CompletableFuture<Void> pendingHandlerWork = new CompletableFuture<>();
      manager.register("FirstStubEvent", event -> pendingHandlerWork);

      StubAggregateRoot aggregate = new StubAggregateRoot(new CustomerId());
      aggregate.raise(new FirstStubEvent(aggregate.id));

      CompletableFuture<Void> publishResult = manager.publish(aggregate);

      assertFalse(publishResult.isDone());

      pendingHandlerWork.complete(null);

      assertTrue(publishResult.isDone());
    }
  }
}
