package com.mba.fc.ingressos.core.events.infra.db.repositories;

import com.mba.fc.ingressos.core.common.application.IUnitOfWork;
import com.mba.fc.ingressos.core.common.domain.valueobjects.Uuid;
import com.mba.fc.ingressos.core.events.domain.entities.Order;
import com.mba.fc.ingressos.core.events.domain.repositories.IOrderRepository;
import com.mba.fc.ingressos.core.events.infra.db.mappers.OrderMapper;
import com.mba.fc.ingressos.core.events.infra.db.schemas.OrderSchema;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderH2Repository implements IOrderRepository {

  private final EntityManager entityManager;
  private final OrderMapper orderMapper;
  private final IUnitOfWork unitOfWork;

  public OrderH2Repository(
      EntityManager entityManager, OrderMapper orderMapper, IUnitOfWork unitOfWork) {
    this.entityManager = entityManager;
    this.orderMapper = orderMapper;
    this.unitOfWork = unitOfWork;
  }

  @Override
  public Order add(Order entity) {
    // Ver o comentário equivalente em PartnerH2Repository.add(): rastreamos o "entity"
    // recebido porque é ele quem carrega os eventos de domínio, não o objeto reconstruído
    // pelo mapper depois do merge.
    unitOfWork.trackPersisted(entity);
    OrderSchema schema = orderMapper.toSchema(entity);
    OrderSchema merged = entityManager.merge(schema);
    return orderMapper.toDomain(merged);
  }

  @Override
  public Order findById(Uuid id) {
    OrderSchema schema = entityManager.find(OrderSchema.class, id.getValue());
    if (schema == null) {
      return null;
    }
    return orderMapper.toDomain(schema);
  }

  @Override
  public Set<Order> findAll() {
    return entityManager
        .createQuery("SELECT o FROM OrderSchema o", OrderSchema.class)
        .getResultStream()
        .collect(Collectors.toSet())
        .stream()
        .map(orderMapper::toDomain)
        .collect(Collectors.toSet());
  }

  @Override
  public void delete(Uuid id) {
    entityManager
        .createQuery("DELETE FROM OrderSchema o WHERE o.id = :id")
        .setParameter("id", id.getValue())
        .executeUpdate();
  }
}
