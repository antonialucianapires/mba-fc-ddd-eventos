package com.mba.fc.ingressos.core.events.application;

import com.mba.fc.ingressos.core.common.application.ApplicationService;
import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.DomainEventManager;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.commands.UpdatePartnerCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Partner;
import com.mba.fc.ingressos.core.events.domain.repositories.IPartnerRepository;
import java.util.Set;

public class PartnerService extends ApplicationService {

  private final IPartnerRepository partnerRepository;

  public PartnerService(
      IPartnerRepository partnerRepository,
      IUnitOfWork unitOfWork,
      DomainEventManager domainEventManager) {
    super(unitOfWork, domainEventManager);
    this.partnerRepository = partnerRepository;
  }

  public Set<Partner> list() {
    return partnerRepository.findAll();
  }

  // create/update usam o ciclo start/finish/fail herdado de ApplicationService (via run()).
  // O caso de uso não chama mais unitOfWork.commit() nem sabe que existe um DomainEventManager
  // por trás: ele só cria/atualiza o Partner e devolve o resultado. Quem decide QUANDO publicar
  // os eventos acumulados no Partner e QUANDO comitar é o finish() da classe-base — sempre nessa
  // ordem (publica, depois comita), então se algum listener futuro reagir ao evento e mexer em
  // outro agregado, essa reação ainda faz parte da mesma transação.
  public Partner create(String name) {
    return run(
        () -> {
          Partner partner = Partner.create(name);
          return partnerRepository.add(partner);
        });
  }

  public Partner update(PartnerId id, UpdatePartnerCommand command) {
    return run(
        () -> {
          Partner partner = partnerRepository.findById(id);
          if (partner == null) {
            throw new IllegalArgumentException("Partner not found");
          }
          Partner partnerToUpdate = command.name().map(partner::changeName).orElse(partner);
          return partnerRepository.add(partnerToUpdate);
        });
  }

  public void delete(PartnerId id) {
    unitOfWork.runTransaction(
        () -> {
          Partner partner = partnerRepository.findById(id);
          if (partner == null) {
            throw new IllegalArgumentException("Partner not found");
          }
          partnerRepository.delete(id);
          unitOfWork.commit();
          return null;
        });
  }
}
