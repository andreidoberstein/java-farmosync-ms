# ADR 004: Boas Práticas de Logging Estruturado e Resiliente nos Microsserviços

## Status
**Accepted** (Aceito)

## Data
18 de Maio de 2026

## Contexto
Durante o desenvolvimento e análise dos microsserviços do FarmoSync, identificou-se a falta de logs informativos e um anti-padrão grave de infraestrutura: blocos `catch` capturavam exceções físicas críticas (como indisponibilidade temporária do broker Kafka) e as engoliam em silêncio operacional absoluto sem registrar o erro. Isso impossibilitava a depuração rápida, a rastreabilidade em tempo real de mensagens consumidas/publicadas e a identificação precoce de falhas em produção.

## Decisão
Decidimos estabelecer e aplicar uma política rigorosa de Boas Práticas de Logging em todos os microsserviços:
1. **Instrumentação e Fachada de Log:** Injeção padronizada da anotação `@Slf4j` do Lombok em todos os publishers de eventos, agendadores de Outbox e consumidores do Kafka, garantindo o uso thread-safe e unificado do SLF4J (Logback).
2. **Eliminação do Silêncio de Exceções:** Proibição de engolir exceções no bloco `catch` sem logs apropriados. Toda falha física de infraestrutura ou negócio deve ser devidamente logada.
3. **Uso Apropriado dos Níveis de Log:**
   * **`INFO`:** Entrada de requisições de negócio significativas (ex: solicitação de publicação de evento) e conclusão com sucesso de tarefas (ex: processamento com sucesso de validação de receita ou baixa de estoque).
   * **`WARN`:** Tentativas fracassadas, porém toleradas devido à resiliência automática (ex: falhas temporárias no envio ao Kafka registradas com o contador de tentativa `Tentativa 3/5. Causa: ...`).
   * **`ERROR`:** Falhas definitivas que impedem a continuidade imediata da transação (ex: evento outbox que atingiu o limite de 5 tentativas e faliu fisicamente, incluindo a StackTrace completa da exceção no log para depuração posterior).
   * **`DEBUG`:** Detalhes de fluxo fino úteis apenas para depuração local em desenvolvimento (ex: envio de chunks de dados para o Kafka).
4. **Acoplamento com a Coleta Centralizada (Grafana Loki):** Escrita de todos os logs diretamente na saída padrão (`stdout`/`stderr`). Isso habilita coletores de logs no nó (como Grafana Promtail) a lerem as saídas de forma não-invasiva e enviá-las para o Grafana Loki, onde os labels indexados coincidem com as tags do Prometheus para correlação imediata de gráficos e logs.

## Consequências

### Positivas
* **Auditabilidade e Diagnóstico Rápido:** Investigação instantânea do motivo de falhas em schedulers de outbox ou no consumo de mensagens.
* **Correlacionamento Inteligente:** Possibilidade de abrir os logs de um milissegundo específico clicando direto nos picos de métricas do Prometheus no Grafana.
* **Manutenibilidade:** Código legível e depurável, respeitando as melhores práticas de Clean Code e Clean Architecture.

### Negativas
* **Aumento no Volume de Armazenamento:** Logs mais verbosos de sucesso e tentativas geram maior consumo de armazenamento em disco no repositório de logs centralizado, exigindo a definição de políticas claras de retenção (ex: expirar logs após 14 dias).
