package com.mba.fc.ingressos.core.common.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * Infraestrutura mínima de despacho de domain events.
 *
 * <p>Só tem duas responsabilidades: {@link #register} guarda quem quer ouvir o quê, e {@link
 * #publish} pega os eventos já acumulados num {@link AggregateRoot} (via {@code addEvent} no
 * agregado) e os entrega para os handlers inscritos. O agregado nunca conhece esta classe nem os
 * handlers — ele só registra fatos (ex: {@code Partner.create()} chamando {@code addEvent(new
 * PartnerCreated(...))}). Quem liga os pontos é esta classe.
 *
 * <p>A versão original (do professor) é em Node e usa a lib {@code EventEmitter2} com {@code
 * wildcard: true} para ganhar de graça a inscrição por padrão (ex: "Partner.*") e a emissão
 * assíncrona (métodos que retornam Promise). Java não tem um EventEmitter na stdlib nem essa lib,
 * então esta classe reimplementa o pedaço que a gente precisa:
 *
 * <ul>
 *   <li>um {@code Map<String, List<Handler>>} no lugar do emitter interno da lib;
 *   <li>um matcher de wildcard simples (suporta {@code *}) no lugar do {@code wildcard: true};
 *   <li>{@link CompletableFuture} no lugar de {@code Promise}, para handlers assíncronos;
 *   <li>encadeamento de {@code CompletableFuture} (sem bloquear thread) no lugar do {@code await}
 *       dentro do {@code for} do publish, para manter a emissão sequencial.
 * </ul>
 *
 * <p><b>O que este código ainda NÃO faz</b> (de propósito, é o próximo passo): não é chamado
 * automaticamente por ninguém. O Unit of Work é o ponto natural para, no fechamento da transação,
 * pegar os agregados tocados e chamar {@code publish} para cada um — mas essa ligação ainda não
 * existe aqui.
 */
public class DomainEventManager {

  /**
   * Contrato do handler. Equivalente ao {@code handler} do Node: uma função que recebe o evento
   * publicado. Ela retorna {@code CompletableFuture<Void>} (o "Promise" do Java) porque handlers de
   * domínio tipicamente fazem I/O (gravar em outra tabela, chamar um serviço, publicar numa fila) —
   * então a infraestrutura já assume, desde o início, que a reação pode ser assíncrona. Um handler
   * síncrono simplesmente devolve {@link CompletableFuture#completedFuture}.
   */
  @FunctionalInterface
  public interface DomainEventHandler {
    CompletableFuture<Void> handle(IDomainEvent event);
  }

  /**
   * Mapa entre o pattern registrado (a string passada em {@link #register}) e a lista de handlers
   * inscritos nesse pattern. Note que a chave é a STRING do pattern, não a classe do evento — é
   * isso que permite registrar tanto "PartnerCreated" (um evento específico) quanto algo como
   * "Partner*" (todos os eventos cujo nome começa com "Partner"), sem que o registro precise
   * conhecer as classes concretas de evento previamente.
   *
   * <p>{@link LinkedHashMap} preserva a ordem de registro dos patterns, e {@link
   * CopyOnWriteArrayList} deixa a lista de handlers segura para leitura concorrente (publish pode
   * rodar a partir de threads diferentes sem precisarmos sincronizar manualmente aqui).
   */
  private final Map<String, List<DomainEventHandler>> listenersByPattern = new LinkedHashMap<>();

  /**
   * Equivalente a {@code emitter.on(pattern, handler)} no Node. Um mesmo pattern pode ter vários
   * handlers — todos serão chamados quando um evento compatível for publicado.
   *
   * @param pattern nome exato de um evento (ex: "PartnerCreated") ou um padrão com {@code *} (ex:
   *     "Partner*", "*Created", ou até "*" para ouvir tudo — útil para auditoria/observabilidade).
   * @param handler reação a executar quando um evento compatível com o pattern for publicado.
   */
  public void register(String pattern, DomainEventHandler handler) {
    listenersByPattern
        .computeIfAbsent(pattern, ignoredKey -> new CopyOnWriteArrayList<>())
        .add(handler);
  }

  /**
   * Equivalente ao {@code async publish(aggregateRoot)} do Node. Percorre {@code
   * aggregateRoot.getEvents()} (os fatos que o agregado acumulou durante a operação de negócio) e
   * emite cada um, na ordem em que foram registrados.
   *
   * <p>A emissão é SEQUENCIAL: o evento N+1 só começa a ser emitido depois que todos os handlers do
   * evento N terminarem (equivalente ao {@code await} dentro do {@code for} da versão Node). Isso é
   * feito encadeando os {@link CompletableFuture} com {@code thenCompose} — sem bloquear nenhuma
   * thread com {@code .join()}/{@code .get()}, então o método continua "assíncrono" de ponta a
   * ponta, só que sem paralelismo entre eventos nem entre handlers. Execução paralela é uma
   * estratégia diferente e deliberadamente não é o que este método faz: previsibilidade vem antes
   * de performance neste primeiro desenho.
   *
   * <p>Este método não limpa {@code aggregateRoot}'s events (não chama {@code clearEvents()}) —
   * isso fica por conta de quem orquestra a publicação (o futuro encaixe com o Unit of Work), para
   * que o próprio agregado nunca precise saber quando "já foi publicado".
   *
   * @param aggregateRoot agregado cujos eventos acumulados serão despachados.
   * @return future que completa quando todos os eventos (e todos os handlers de cada um) tiverem
   *     terminado de rodar.
   */
  public CompletableFuture<Void> publish(AggregateRoot<?> aggregateRoot) {
    CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
    for (IDomainEvent event : aggregateRoot.getEvents()) {
      chain = chain.thenCompose(ignoredResult -> emit(event));
    }
    return chain;
  }

  /**
   * Emite um único evento para todos os patterns compatíveis, também sequencialmente.
   *
   * <p>A chave usada para casar o evento com os patterns registrados é {@code
   * event.getClass().getSimpleName()} — o equivalente Java a {@code event.constructor.name} no
   * Node. Ou seja, se o objeto publicado é uma instância de {@code PartnerCreated}, a chave de
   * emissão é a string {@code "PartnerCreated"}, e não um campo manual dentro do evento. Isso evita
   * duplicação (não precisamos de um {@code getEventName()} em cada classe de evento) e garante que
   * o nome publicado está sempre alinhado com a classe real do objeto.
   */
  private CompletableFuture<Void> emit(IDomainEvent event) {
    String eventName = event.getClass().getSimpleName();

    CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
    for (Map.Entry<String, List<DomainEventHandler>> entry : listenersByPattern.entrySet()) {
      if (!matches(entry.getKey(), eventName)) {
        continue;
      }
      for (DomainEventHandler handler : entry.getValue()) {
        chain = chain.thenCompose(ignoredResult -> handler.handle(event));
      }
    }
    return chain;
  }

  /**
   * Matcher de wildcard equivalente, na prática, ao {@code wildcard: true} do EventEmitter2: o
   * caractere {@code *} no pattern casa com qualquer trecho (inclusive vazio) do nome do evento.
   *
   * <p>Sem {@code *} no pattern, a comparação é exata (mesmo comportamento de um {@code emitter.on}
   * "normal", sem wildcard). Com {@code *}, o pattern é quebrado nos pedaços fixos ao redor de cada
   * {@code *}, cada pedaço é escapado com {@link Pattern#quote} (para caracteres especiais de regex
   * no nome do evento não terem efeito nenhum) e os pedaços são remontados como uma regex ancorada
   * ({@code ^...$}) com {@code .*} no lugar de cada {@code *}.
   *
   * <p>Exemplos: {@code "PartnerCreated"} casa só com {@code "PartnerCreated"}; {@code "Partner*"}
   * casa com {@code "PartnerCreated"}, {@code "PartnerRenamed"} etc.; {@code "*"} casa com qualquer
   * evento (útil para um listener de auditoria/log que quer ver tudo).
   */
  private boolean matches(String pattern, String eventName) {
    if (!pattern.contains("*")) {
      return pattern.equals(eventName);
    }

    String[] literalParts = pattern.split("\\*", -1);
    StringBuilder regex = new StringBuilder("^");
    for (int i = 0; i < literalParts.length; i++) {
      regex.append(Pattern.quote(literalParts[i]));
      if (i < literalParts.length - 1) {
        regex.append(".*");
      }
    }
    regex.append("$");

    return eventName.matches(regex.toString());
  }
}
