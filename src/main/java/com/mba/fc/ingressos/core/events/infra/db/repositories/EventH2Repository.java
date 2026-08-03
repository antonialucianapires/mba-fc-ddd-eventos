package com.mba.fc.ingressos.core.events.infra.db.repositories;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.valueobjects.Uuid;
import com.mba.fc.ingressos.core.events.domain.entities.Event;
import com.mba.fc.ingressos.core.events.domain.repositories.IEventRepository;
import com.mba.fc.ingressos.core.events.infra.db.mappers.EventMapper;
import com.mba.fc.ingressos.core.events.infra.db.schemas.EventSchema;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.stream.Collectors;

public class EventH2Repository implements IEventRepository {

  private final EntityManager entityManager;
  private final EventMapper eventMapper;
  private final IUnitOfWork unitOfWork;

  public EventH2Repository(
      EntityManager entityManager, EventMapper eventMapper, IUnitOfWork unitOfWork) {
    this.entityManager = entityManager;
    this.eventMapper = eventMapper;
    this.unitOfWork = unitOfWork;
  }

  @Override
  public Event add(Event entity) {
    // Ver o comentário equivalente em PartnerH2Repository.add(): rastreamos o "entity"
    // recebido porque é ele quem carrega os eventos de domínio, não o objeto reconstruído
    // pelo mapper depois do merge.
    unitOfWork.trackPersisted(entity);
    EventSchema schema = eventMapper.toSchema(entity);
    EventSchema merged = entityManager.merge(schema);
    return eventMapper.toDomain(merged);
  }

  @Override
  public Event findById(Uuid id) {
    EventSchema schema = entityManager.find(EventSchema.class, id.getValue());
    if (schema == null) {
      return null;
    }
    return eventMapper.toDomain(schema);
  }

  @Override
  public Set<Event> findAll() {
    return entityManager
        .createQuery("SELECT e FROM EventSchema e", EventSchema.class)
        .getResultStream()
        .collect(Collectors.toSet())
        .stream()
        .map(eventMapper::toDomain)
        .collect(Collectors.toSet());
  }

  @Override
  public void delete(Uuid id) {
    EventSchema schema = entityManager.find(EventSchema.class, id.getValue());
    if (schema != null) {
      entityManager.remove(schema);
      entityManager.flush();
    }
  }
}
