package com.mba.fc.ingressos.core.common.application;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import java.util.List;
import java.util.function.Supplier;

public interface IUnitOfWork {

  void commit();

  void rollback();

  void beginTransaction();

  void completeTransaction();

  void rollbackTransaction();

  <T> T runTransaction(Supplier<T> action);

  /**
   * Registra um agregado que foi persistido (criado ou alterado) durante a transação atual.
   * Repositórios chamam isto dentro de {@code add(entity)}, sempre com o objeto de domínio que veio
   * como parâmetro (não com o que é reconstruído a partir do banco depois do merge) — é nele, e só
   * nele, que os eventos de domínio levantados pelo caso de uso ainda existem.
   *
   * <p>Equivalente ao "push" no {@code persistStack} da versão Node.
   */
  void trackPersisted(AggregateRoot<?> aggregateRoot);

  /**
   * Registra um agregado que foi removido durante a transação atual. Equivalente ao "push" no
   * {@code removeStack} da versão Node. Nada chama este método ainda porque nosso {@code
   * Repository.delete(Uuid id)} só recebe o ID, não o agregado — falta um passo futuro para
   * carregar o agregado antes de remover, se algum dia precisarmos publicar eventos de remoção.
   */
  void trackRemoved(AggregateRoot<?> aggregateRoot);

  /**
   * Devolve todos os agregados acompanhados até agora (persistStack + removeStack combinados, como
   * um único array) e ESVAZIA as duas listas internas — por isso "drain" (drenar) e não apenas
   * "get". Esse esvaziamento é o que permite ao {@code ApplicationService.finish()} ficar chamando
   * este método em loop até ele voltar vazio: se um listener, ao reagir a um evento, manipular
   * outro agregado (ou o mesmo de novo), esse novo agregado entra na lista na próxima chamada, e a
   * "cascata" de fatos de domínio é publicada por inteiro antes do commit.
   */
  List<AggregateRoot<?>> drainManipulatedAggregates();
}
