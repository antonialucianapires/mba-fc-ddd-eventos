package com.mba.fc.ingressos.core.events.infra.db.repositories;

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

  public EventH2Repository(EntityManager entityManager, EventMapper eventMapper) {
    this.entityManager = entityManager;
    this.eventMapper = eventMapper;
  }

  @Override
  public Event add(Event entity) {
    EventSchema schema = eventMapper.toSchema(entity);
    entityManager.persist(schema);
    return eventMapper.toDomain(schema);
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
    entityManager
        .createQuery("DELETE FROM EventSchema e WHERE e.id = :id")
        .setParameter("id", id.getValue())
        .executeUpdate();
  }
}
