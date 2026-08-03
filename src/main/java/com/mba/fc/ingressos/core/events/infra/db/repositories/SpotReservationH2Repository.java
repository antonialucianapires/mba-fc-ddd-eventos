package com.mba.fc.ingressos.core.events.infra.db.repositories;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.valueobjects.Uuid;
import com.mba.fc.ingressos.core.events.domain.entities.SpotReservation;
import com.mba.fc.ingressos.core.events.domain.repositories.ISpotReservationRepository;
import com.mba.fc.ingressos.core.events.infra.db.mappers.SpotReservationMapper;
import com.mba.fc.ingressos.core.events.infra.db.schemas.SpotReservationSchema;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.stream.Collectors;

public class SpotReservationH2Repository implements ISpotReservationRepository {

  private final EntityManager entityManager;
  private final SpotReservationMapper spotReservationMapper;
  private final IUnitOfWork unitOfWork;

  public SpotReservationH2Repository(
      EntityManager entityManager,
      SpotReservationMapper spotReservationMapper,
      IUnitOfWork unitOfWork) {
    this.entityManager = entityManager;
    this.spotReservationMapper = spotReservationMapper;
    this.unitOfWork = unitOfWork;
  }

  @Override
  public SpotReservation add(SpotReservation entity) {
    // Ver o comentário equivalente em PartnerH2Repository.add(): rastreamos o "entity"
    // recebido porque é ele quem carrega os eventos de domínio, não o objeto reconstruído
    // pelo mapper depois do merge.
    unitOfWork.trackPersisted(entity);
    SpotReservationSchema schema = spotReservationMapper.toSchema(entity);
    SpotReservationSchema merged = entityManager.merge(schema);
    return spotReservationMapper.toDomain(merged);
  }

  @Override
  public SpotReservation findById(Uuid id) {
    SpotReservationSchema schema = entityManager.find(SpotReservationSchema.class, id.getValue());
    if (schema == null) {
      return null;
    }
    return spotReservationMapper.toDomain(schema);
  }

  @Override
  public Set<SpotReservation> findAll() {
    return entityManager
        .createQuery("SELECT r FROM SpotReservationSchema r", SpotReservationSchema.class)
        .getResultStream()
        .collect(Collectors.toSet())
        .stream()
        .map(spotReservationMapper::toDomain)
        .collect(Collectors.toSet());
  }

  @Override
  public void delete(Uuid id) {
    entityManager
        .createQuery("DELETE FROM SpotReservationSchema r WHERE r.spotId = :id")
        .setParameter("id", id.getValue())
        .executeUpdate();
  }
}
