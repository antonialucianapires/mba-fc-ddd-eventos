package com.mba.fc.ingressos.core.common.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import com.mba.fc.ingressos.core.common.domain.DomainEventManager;
import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

@DisplayName("ApplicationService")
class ApplicationServiceTest {

  // Agregado de teste só para ter algo concreto para colocar nas listas devolvidas por
  // drainManipulatedAggregates() — o conteúdo dele não importa, só a identidade.
  private static class StubAggregateRoot extends AggregateRoot<CustomerId> {
    StubAggregateRoot() {
      super(new CustomerId());
    }

    @Override
    public String toString() {
      return "StubAggregateRoot{id=" + id.getValue() + "}";
    }
  }

  // ApplicationService só expõe start/fail/finish/run como protected (são detalhes de
  // implementação para as subclasses concretas, tipo PartnerService) — então o teste precisa de
  // uma subclasse mínima só para chamar run() de fora.
  private static class StubApplicationService extends ApplicationService {
    StubApplicationService(IUnitOfWork unitOfWork, DomainEventManager domainEventManager) {
      super(unitOfWork, domainEventManager);
    }

    <T> T runAction(Supplier<T> action) {
      return run(action);
    }
  }

  private IUnitOfWork unitOfWork;
  private DomainEventManager domainEventManager;
  private StubApplicationService service;

  @BeforeEach
  void setUp() {
    unitOfWork = mock(IUnitOfWork.class);
    domainEventManager = mock(DomainEventManager.class);
    service = new StubApplicationService(unitOfWork, domainEventManager);

    when(domainEventManager.publish(any())).thenReturn(CompletableFuture.completedFuture(null));
  }

  @Nested
  @DisplayName("run(Supplier) — caminho de sucesso")
  class HappyPath {

    @Test
    @DisplayName("should open the transaction before running the action")
    void shouldOpenTransactionBeforeAction() {
      Supplier<String> action =
          () -> {
            verify(unitOfWork).beginTransaction();
            return "result";
          };

      service.runAction(action);
    }

    @Test
    @DisplayName("should return whatever the action returns")
    void shouldReturnActionResult() {
      String result = service.runAction(() -> "partner-created");

      assertEquals("partner-created", result);
    }

    @Test
    @DisplayName("should commit and complete the transaction after the action runs")
    void shouldCommitAndCompleteTransaction() {
      service.runAction(() -> null);

      InOrder inOrder = Mockito.inOrder(unitOfWork);
      inOrder.verify(unitOfWork).beginTransaction();
      inOrder.verify(unitOfWork).commit();
      inOrder.verify(unitOfWork).completeTransaction();
    }

    @Test
    @DisplayName("should publish events for every aggregate manipulated during the action")
    void shouldPublishEventsForManipulatedAggregates() {
      StubAggregateRoot aggregate = new StubAggregateRoot();
      when(unitOfWork.drainManipulatedAggregates()).thenReturn(List.of(aggregate), List.of());

      service.runAction(() -> null);

      verify(domainEventManager).publish(aggregate);
    }

    @Test
    @DisplayName("should publish events before committing")
    void shouldPublishBeforeCommit() {
      StubAggregateRoot aggregate = new StubAggregateRoot();
      when(unitOfWork.drainManipulatedAggregates()).thenReturn(List.of(aggregate), List.of());

      service.runAction(() -> null);

      InOrder inOrder = Mockito.inOrder(domainEventManager, unitOfWork);
      inOrder.verify(domainEventManager).publish(aggregate);
      inOrder.verify(unitOfWork).commit();
    }

    @Test
    @DisplayName(
        "should keep draining and publishing until no new aggregate is tracked (cascata de"
            + " listeners)")
    void shouldDrainUntilEmpty() {
      StubAggregateRoot first = new StubAggregateRoot();
      StubAggregateRoot second = new StubAggregateRoot();
      // Simula um listener que, ao reagir ao evento de "first", manipula "second" — que só
      // aparece no SEGUNDO drain, como se um repositório o tivesse rastreado durante o publish.
      when(unitOfWork.drainManipulatedAggregates())
          .thenReturn(List.of(first), List.of(second), List.of());

      service.runAction(() -> null);

      InOrder inOrder = Mockito.inOrder(domainEventManager);
      inOrder.verify(domainEventManager).publish(first);
      inOrder.verify(domainEventManager).publish(second);
    }

    @Test
    @DisplayName("should not publish anything when no aggregate was manipulated")
    void shouldNotPublishWhenNothingManipulated() {
      service.runAction(() -> null);

      verify(domainEventManager, never()).publish(any());
    }
  }

  @Nested
  @DisplayName("run(Supplier) — caminho de falha")
  class FailurePath {

    @Test
    @DisplayName("should roll back the transaction and rethrow when the action throws")
    void shouldRollBackAndRethrow() {
      RuntimeException boom = new RuntimeException("boom");
      Supplier<Object> action =
          () -> {
            throw boom;
          };

      RuntimeException thrown =
          assertThrows(RuntimeException.class, () -> service.runAction(action));

      assertEquals(boom, thrown);
      verify(unitOfWork).rollbackTransaction();
    }

    @Test
    @DisplayName("should not commit, complete the transaction, or publish events on failure")
    void shouldNotCommitOrPublishOnFailure() {
      Supplier<Object> action =
          () -> {
            throw new RuntimeException("boom");
          };

      assertThrows(RuntimeException.class, () -> service.runAction(action));

      verify(unitOfWork, never()).commit();
      verify(unitOfWork, never()).completeTransaction();
      verify(domainEventManager, never()).publish(any());
    }
  }
}
