package com.mba.fc.ingressos.core.events.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.commands.UpdatePartnerCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Partner;
import com.mba.fc.ingressos.core.events.domain.repositories.IPartnerRepository;
import java.util.Optional;
import java.util.Set;
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
  private PartnerService service;

  @BeforeEach
  void setUp() {
    partnerRepository = mock(IPartnerRepository.class);
    unitOfWork = mock(IUnitOfWork.class);
    service = new PartnerService(partnerRepository, unitOfWork);
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
      verifyNoInteractions(unitOfWork);
    }
  }
}
