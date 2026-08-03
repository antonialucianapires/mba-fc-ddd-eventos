package com.mba.fc.ingressos.core.events.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.DomainEventManager;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.commands.UpdatePartnerCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Partner;
import com.mba.fc.ingressos.core.events.domain.repositories.IPartnerRepository;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

@DisplayName("PartnerService")
class PartnerServiceTest {

  private static final String VALID_NAME = "Acme Corp";

  private IPartnerRepository partnerRepository;
  private IUnitOfWork unitOfWork;
  private DomainEventManager domainEventManager;
  private PartnerService service;

  @BeforeEach
  void setUp() {
    partnerRepository = mock(IPartnerRepository.class);
    unitOfWork = mock(IUnitOfWork.class);
    domainEventManager = mock(DomainEventManager.class);
    service = new PartnerService(partnerRepository, unitOfWork, domainEventManager);

    // create/update passam a usar ApplicationService.run() (start/finish/fail), não mais
    // unitOfWork.runTransaction() diretamente — mas delete() ainda usa, então o stub continua
    // necessário para os testes de delete().
    doAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get())
        .when(unitOfWork)
        .runTransaction(any());

    // finish() só chama domainEventManager.publish(...) para os agregados que
    // drainManipulatedAggregates() devolver; por padrão (sem stub extra) a lista vem vazia,
    // então publish nunca é chamado a menos que um teste específico configure o contrário.
    when(domainEventManager.publish(any())).thenReturn(CompletableFuture.completedFuture(null));
  }

  @Nested
  @DisplayName("list()")
  class List {

    @Test
    @DisplayName("should return every partner from the repository")
    void shouldReturnAllPartnersFromRepository() {
      Partner partner = Partner.create(VALID_NAME);
      when(partnerRepository.findAll()).thenReturn(Set.of(partner));

      Set<Partner> partners = service.list();

      assertEquals(Set.of(partner), partners);
    }

    @Test
    @DisplayName("should return an empty set when the repository has no partners")
    void shouldReturnEmptySetWhenRepositoryIsEmpty() {
      when(partnerRepository.findAll()).thenReturn(Set.of());

      Set<Partner> partners = service.list();

      assertTrue(partners.isEmpty());
    }

    @Test
    @DisplayName("should not interact with the unit of work")
    void shouldNotInteractWithUnitOfWork() {
      when(partnerRepository.findAll()).thenReturn(Set.of());

      service.list();

      verifyNoInteractions(unitOfWork);
    }
  }

  @Nested
  @DisplayName("create(String)")
  class Create {

    @Test
    @DisplayName("should create a Partner with the given name")
    void shouldCreatePartnerWithGivenData() {
      when(partnerRepository.add(any(Partner.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Partner created = service.create(VALID_NAME);

      assertEquals(VALID_NAME, created.getName());
    }

    @Test
    @DisplayName("should pass the created partner to the repository")
    void shouldAddCreatedPartnerToRepository() {
      when(partnerRepository.add(any(Partner.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(VALID_NAME);

      verify(partnerRepository).add(any(Partner.class));
    }

    @Test
    @DisplayName("should return whatever the repository returns")
    void shouldReturnRepositoryResult() {
      Partner persisted = Partner.create(VALID_NAME);
      when(partnerRepository.add(any(Partner.class))).thenReturn(persisted);

      Partner created = service.create(VALID_NAME);

      assertSame(persisted, created);
    }

    @Test
    @DisplayName("should commit the unit of work after adding the partner")
    void shouldCommitUnitOfWork() {
      when(partnerRepository.add(any(Partner.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(VALID_NAME);

      verify(unitOfWork).commit();
    }

    @Test
    @DisplayName("should commit only after the partner was added to the repository")
    void shouldCommitAfterAddingToRepository() {
      when(partnerRepository.add(any(Partner.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(VALID_NAME);

      InOrder inOrder = inOrder(partnerRepository, unitOfWork);
      inOrder.verify(partnerRepository).add(any(Partner.class));
      inOrder.verify(unitOfWork).commit();
    }

    @Test
    @DisplayName("should never roll back the unit of work")
    void shouldNeverRollBackUnitOfWork() {
      when(partnerRepository.add(any(Partner.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(VALID_NAME);

      verify(unitOfWork, never()).rollback();
    }

    @Test
    @DisplayName("should open and complete the transaction around the use case")
    void shouldOpenAndCompleteTransaction() {
      when(partnerRepository.add(any(Partner.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.create(VALID_NAME);

      InOrder inOrder = inOrder(unitOfWork);
      inOrder.verify(unitOfWork).beginTransaction();
      inOrder.verify(unitOfWork).commit();
      inOrder.verify(unitOfWork).completeTransaction();
    }

    @Test
    @DisplayName("should publish the events accumulated on the created partner before committing")
    void shouldPublishAccumulatedEventsBeforeCommit() {
      Partner persisted = Partner.create(VALID_NAME);
      when(partnerRepository.add(any(Partner.class))).thenReturn(persisted);
      // Simula o que o PartnerH2Repository real faria: rastrear o partner criado (com o
      // PartnerCreated que Partner.create() registrou) no primeiro drain, e nada nos seguintes.
      when(unitOfWork.drainManipulatedAggregates())
          .thenReturn(java.util.List.of(persisted), java.util.List.of());

      service.create(VALID_NAME);

      InOrder inOrder = inOrder(domainEventManager, unitOfWork);
      inOrder.verify(domainEventManager).publish(persisted);
      inOrder.verify(unitOfWork).commit();
    }

    @Test
    @DisplayName("should roll back the transaction and not commit when the repository fails")
    void shouldRollBackTransactionOnFailure() {
      when(partnerRepository.add(any(Partner.class))).thenThrow(new RuntimeException("boom"));

      assertThrows(RuntimeException.class, () -> service.create(VALID_NAME));

      verify(unitOfWork).rollbackTransaction();
      verify(unitOfWork, never()).commit();
      verify(unitOfWork, never()).completeTransaction();
    }
  }

  @Nested
  @DisplayName("update(PartnerId, UpdatePartnerCommand)")
  class Update {

    @Test
    @DisplayName("should change the name when the command has a name present")
    void shouldChangeNameWhenPresent() {
      Partner partner = Partner.create(VALID_NAME);
      when(partnerRepository.findById(partner.getId())).thenReturn(partner);
      when(partnerRepository.add(any(Partner.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Partner updated =
          service.update(partner.getId(), new UpdatePartnerCommand(Optional.of("New Name")));

      assertEquals("New Name", updated.getName());
    }

    @Test
    @DisplayName("should keep the current name when the command has no name")
    void shouldKeepNameWhenAbsent() {
      Partner partner = Partner.create(VALID_NAME);
      when(partnerRepository.findById(partner.getId())).thenReturn(partner);
      when(partnerRepository.add(any(Partner.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Partner updated = service.update(partner.getId(), new UpdatePartnerCommand(Optional.empty()));

      assertEquals(VALID_NAME, updated.getName());
    }

    @Test
    @DisplayName("should throw when no partner is found for the given id")
    void shouldThrowWhenPartnerNotFound() {
      when(partnerRepository.findById(any())).thenReturn(null);

      assertThrows(
          IllegalArgumentException.class,
          () -> service.update(new PartnerId(), new UpdatePartnerCommand(Optional.of("New Name"))));
    }

    @Test
    @DisplayName("should commit the unit of work after updating the partner")
    void shouldCommitUnitOfWork() {
      Partner partner = Partner.create(VALID_NAME);
      when(partnerRepository.findById(partner.getId())).thenReturn(partner);
      when(partnerRepository.add(any(Partner.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.update(partner.getId(), new UpdatePartnerCommand(Optional.of("New Name")));

      verify(unitOfWork).commit();
    }

    @Test
    @DisplayName("should roll back the transaction and not commit when the partner is not found")
    void shouldRollBackTransactionWhenPartnerNotFound() {
      when(partnerRepository.findById(any())).thenReturn(null);

      assertThrows(
          IllegalArgumentException.class,
          () -> service.update(new PartnerId(), new UpdatePartnerCommand(Optional.of("New Name"))));

      verify(unitOfWork).rollbackTransaction();
      verify(unitOfWork, never()).commit();
    }
  }

  @Nested
  @DisplayName("delete(PartnerId)")
  class Delete {

    @Test
    @DisplayName("should delete the partner from the repository")
    void shouldDeletePartnerFromRepository() {
      Partner partner = Partner.create(VALID_NAME);
      when(partnerRepository.findById(partner.getId())).thenReturn(partner);

      service.delete(partner.getId());

      verify(partnerRepository).delete(partner.getId());
    }

    @Test
    @DisplayName("should commit the unit of work after deleting the partner")
    void shouldCommitUnitOfWork() {
      Partner partner = Partner.create(VALID_NAME);
      when(partnerRepository.findById(partner.getId())).thenReturn(partner);

      service.delete(partner.getId());

      verify(unitOfWork).commit();
    }

    @Test
    @DisplayName("should throw when no partner is found for the given id")
    void shouldThrowWhenPartnerNotFound() {
      when(partnerRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.delete(new PartnerId()));
    }

    @Test
    @DisplayName("should not delete or commit when the partner is not found")
    void shouldNotDeleteOrCommitWhenPartnerNotFound() {
      when(partnerRepository.findById(any())).thenReturn(null);

      assertThrows(IllegalArgumentException.class, () -> service.delete(new PartnerId()));

      verify(partnerRepository, never()).delete(any());
      verify(unitOfWork, never()).commit();
    }
  }
}
