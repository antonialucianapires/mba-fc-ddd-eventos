package com.mba.fc.ingressos.core.events.application;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.commands.UpdatePartnerCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Partner;
import com.mba.fc.ingressos.core.events.domain.repositories.IPartnerRepository;
import java.util.Set;

public class PartnerService {

  private final IPartnerRepository partnerRepository;
  private final IUnitOfWork unitOfWork;

  public PartnerService(IPartnerRepository partnerRepository, IUnitOfWork unitOfWork) {
    this.partnerRepository = partnerRepository;
    this.unitOfWork = unitOfWork;
  }

  public Set<Partner> list() {
    return partnerRepository.findAll();
  }

  public Partner create(String name) {
    Partner partner = Partner.create(name);
    Partner partnerSaved = partnerRepository.add(partner);
    unitOfWork.commit();
    return partnerSaved;
  }

  public Partner update(PartnerId id, UpdatePartnerCommand command) {
    Partner partner = partnerRepository.findById(id);
    if (partner == null) {
      throw new IllegalArgumentException("Partner not found");
    }
    partner = command.name().map(partner::changeName).orElse(partner);
    Partner partnerUpdated = partnerRepository.add(partner);
    unitOfWork.commit();
    return partnerUpdated;
  }

  public void delete(PartnerId id) {
    Partner partner = partnerRepository.findById(id);
    if (partner == null) {
      throw new IllegalArgumentException("Partner not found");
    }
    partnerRepository.delete(id);
    unitOfWork.commit();
  }
}
