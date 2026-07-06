package com.mba.fc.ingressos.core.events.infra.db.repositories;

import com.mba.fc.ingressos.core.common.domain.valueobjects.Uuid;
import com.mba.fc.ingressos.core.events.domain.entities.Partner;
import com.mba.fc.ingressos.core.events.domain.repositories.IPartnerRepository;
import com.mba.fc.ingressos.core.events.infra.db.mappers.PartnerMapper;
import com.mba.fc.ingressos.core.events.infra.db.schemas.PartnerSchema;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.stream.Collectors;

public class PartnerH2Repository implements IPartnerRepository {

  private final EntityManager entityManager;
  private final PartnerMapper partnerMapper;

  public PartnerH2Repository(EntityManager entityManager, PartnerMapper partnerMapper) {
    this.entityManager = entityManager;
    this.partnerMapper = partnerMapper;
  }

  @Override
  public Partner add(Partner entity) {
    PartnerSchema schema = partnerMapper.toSchema(entity);
    entityManager.persist(schema);
    return partnerMapper.toDomain(schema);
  }

  @Override
  public Partner findById(Uuid id) {
    PartnerSchema schema = entityManager.find(PartnerSchema.class, id.getValue());
    if (schema == null) {
      return null;
    }
    return partnerMapper.toDomain(schema);
  }

  @Override
  public Set<Partner> findAll() {
    return entityManager
        .createQuery("SELECT p FROM PartnerSchema p", PartnerSchema.class)
        .getResultStream()
        .collect(Collectors.toSet())
        .stream()
        .map(partnerMapper::toDomain)
        .collect(Collectors.toSet());
  }

  @Override
  public void delete(Uuid id) {
    entityManager
        .createQuery("DELETE FROM PartnerSchema p WHERE p.id = :id")
        .setParameter("id", id.getValue())
        .executeUpdate();
  }
}
