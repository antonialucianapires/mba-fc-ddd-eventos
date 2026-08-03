package com.mba.fc.ingressos.core.common.application;

import com.mba.fc.ingressos.core.common.domain.AggregateRoot;
import com.mba.fc.ingressos.core.common.domain.DomainEventManager;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Classe-base para os casos de uso (application services) que precisam abrir transação, publicar os
 * domain events acumulados nos agregados manipulados e só então fechar a transação.
 *
 * <p>Sem isso, cada service reimplementa a mesma sequência (abrir transação, chamar o repositório,
 * publicar eventos manualmente, dar commit) e mistura duas responsabilidades: a lógica de negócio
 * do caso de uso e a coordenação do fechamento transacional. Esta classe assume só a segunda
 * responsabilidade, via três "fases" do ciclo de vida do processo de negócio:
 *
 * <ul>
 *   <li>{@link #start()} — abre a transação;
 *   <li>{@link #fail(RuntimeException)} — trata erro, desfazendo a transação;
 *   <li>{@link #finish()} — publica os eventos pendentes e só depois fecha a transação com sucesso.
 * </ul>
 *
 * <p>{@link #run(Supplier)} amarra as três fases num template method: chama {@code start}, roda a
 * lógica de negócio dentro de um {@code try/catch}, chama {@code fail} se algo explodir, ou delega
 * para {@code finish} em caso de sucesso. Subclasses (os application services concretos, como
 * {@code PartnerService}) passam a conter só a lógica de negócio — a parte "de infra" fica toda
 * aqui.
 */
public abstract class ApplicationService {

  protected final IUnitOfWork unitOfWork;
  private final DomainEventManager domainEventManager;

  protected ApplicationService(IUnitOfWork unitOfWork, DomainEventManager domainEventManager) {
    this.unitOfWork = unitOfWork;
    this.domainEventManager = domainEventManager;
  }

  /**
   * Marca a abertura do processo de negócio. Hoje só abre a transação, mas é o ponto único onde
   * também caberiam logs/observabilidade de início de caso de uso no futuro.
   */
  protected void start() {
    unitOfWork.beginTransaction();
  }

  /**
   * Concentra o tratamento de erro do processo de negócio: desfaz a transação aberta em {@link
   * #start()}. Quem lança o erro adiante continua sendo o chamador (veja {@link #run(Supplier)}) —
   * este método só cuida do fechamento transacional em caso de falha, mesma ideia do {@code fail}
   * do Node.
   */
  protected void fail(RuntimeException error) {
    unitOfWork.rollbackTransaction();
  }

  /**
   * Coordena o encerramento bem-sucedido do processo de negócio. A ORDEM aqui é o ponto central
   * deste método: primeiro publica os eventos pendentes nos agregados manipulados durante a
   * transação, e só DEPOIS comita. Isso garante que, se um listener reagir a um evento alterando
   * outro agregado (ou registrando novos eventos), essa reação ainda faz parte da mesma transação
   * lógica — nada foi persistido de forma definitiva ainda quando os listeners rodam.
   *
   * <p>É assíncrono (retorna {@code CompletableFuture<Void>}) porque publicar eventos é assíncrono
   * (veja {@link DomainEventManager#publish}) — só depois que a publicação (potencialmente em
   * cascata) terminar é que fazemos {@code unitOfWork.commit()} (flush) e {@code
   * unitOfWork.completeTransaction()} (commit real da transação JPA/JDBC).
   */
  protected CompletableFuture<Void> finish() {
    return publishPendingEvents()
        .thenRun(
            () -> {
              unitOfWork.commit();
              unitOfWork.completeTransaction();
            });
  }

  /**
   * Drena os agregados manipulados do Unit of Work e publica os eventos de cada um, sequencialmente
   * (um agregado só começa a ser publicado depois que o anterior terminou — mesma decisão de
   * previsibilidade já tomada dentro do próprio {@link DomainEventManager}).
   *
   * <p>Depois de publicar o lote atual, drena de novo: se algum listener, ao reagir, manipulou
   * outro agregado (ou o mesmo de novo) e este foi rastreado no Unit of Work, ele aparece nesse
   * segundo drain e também é publicado — e assim por diante, até não sobrar mais nada. É assim que
   * a "cadeia interna de processamento" descrita pelo professor (um fato inicial que desencadeia
   * outros fatos) é absorvida antes do commit, sem que esta classe precise saber de antemão quantos
   * agregados serão tocados.
   */
  private CompletableFuture<Void> publishPendingEvents() {
    List<AggregateRoot<?>> manipulatedAggregates = unitOfWork.drainManipulatedAggregates();
    if (manipulatedAggregates.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }

    CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
    for (AggregateRoot<?> aggregateRoot : manipulatedAggregates) {
      chain = chain.thenCompose(ignoredResult -> domainEventManager.publish(aggregateRoot));
    }
    return chain.thenCompose(ignoredResult -> publishPendingEvents());
  }

  /**
   * Template method: encapsula o padrão {@code start / try-catch / fail-ou-finish} para que o caso
   * de uso concreto só precise fornecer a lógica de negócio em si.
   *
   * <p>A versão Node recebe um callback assíncrono e usa {@code await} internamente; aqui, como o
   * restante da aplicação (controllers Spring MVC) é síncrono/bloqueante, {@code run} bloqueia (via
   * {@link CompletableFuture#join()}) até {@link #finish()} terminar antes de devolver o resultado
   * — ou seja, do ponto de vista de quem chama {@code run}, o comportamento continua síncrono, só
   * que por baixo dos panos a publicação de eventos usa {@code CompletableFuture} (nosso
   * "Promise").
   *
   * @param action lógica de negócio do caso de uso; deve devolver o resultado a ser propagado.
   */
  protected <T> T run(Supplier<T> action) {
    start();
    try {
      T result = action.get();
      finish().join();
      return result;
    } catch (RuntimeException error) {
      fail(error);
      throw error;
    }
  }
}
