# ADR 003: Observabilidade e Instrumentação de Métricas com Actuator e Prometheus

## Status
**Accepted** (Aceito)

## Data
18 de Maio de 2026

## Contexto
Em um ecossistema de microsserviços distribuídos como o FarmoSync, monitorar a saúde física e de negócios em tempo real é indispensável. Precisamos responder a perguntas como:
1. Qual é o tempo médio de resposta para registrar uma venda no caixa?
2. Qual é a latência física dos comandos disparados contra o MongoDB?
3. O `inventory-service` está processando mensagens na mesma velocidade que o `pdv-service` as produz, ou há aumento no *consumer lag*?
4. Qual é o consumo de CPU e vazamento de memória JVM nos microsserviços?

## Decisão
Decidimos implementar a coleta padronizada de métricas e exposição de saúde de forma homogênea em todos os microsserviços do FarmoSync:
1. **Instrumentação Nativa:** Inclusão das dependências `spring-boot-starter-actuator` e `micrometer-registry-prometheus` nos arquivos `pom.xml` dos serviços `pdv-service`, `prescription-service` e `inventory-service`.
2. **Exposição Controlada de Endpoints:** Configuração de todos os arquivos `application.yml` dos microsserviços para liberar os endpoints de monitoramento através do parâmetro `management.endpoints.web.exposure.include: health, info, prometheus`.
3. **Identificação Uniforme:** Inclusão de uma tag corporativa unificada chamada `application` associando o respectivo nome de cada serviço (`${spring.application.name}`) a todas as métricas geradas, garantindo indexação e filtragem rápida no Prometheus e Grafana.
4. **Coleta de Métricas Críticas:** O Prometheus raspa automaticamente o endpoint `/actuator/prometheus` (rodando nas portas `8081`, `8082` e `8083` de cada microsserviço) para coletar dados do Garbage Collector, consumo de heap/non-heap JVM, lag do consumidor Kafka, throughput de envio de mensagens do Kafka Producer, conexões ativas no pool do MongoDB e percentis de latência HTTP das APIs REST.
5. **Métricas de Negócio Customizadas (Micrometer):** Instrumentamos o código do `pdv-service` para expor indicadores críticos de desempenho e consistência de negócios:
   - `farmosync.outbox.pending.count` (Gauge) — Contagem instantânea de eventos pendentes no Outbox.
   - `farmosync.outbox.processing.duration` (Timer) — Duração e frequência do processamento do Outbox.
   - `farmosync.vendas.total` (Counter) — Volume acumulado de vendas registradas no PDV.
   - `farmosync.estoque.baixas.erros` (Counter) — Volume de erros de baixa no estoque recebidos via saga compensatória.

## Consequências

### Positivas
* **Visibilidade Operacional Clara:** Acesso em tempo real a todas as métricas de saúde, memória, mensageria e banco de dados de todos os microsserviços simultaneamente.
* **Detecção Precoce de Gargalos:** Capacidade de programar alertas automáticos se o consumer lag do estoque disparar ou se a latência p99 do endpoint de venda ultrapassar limites aceitáveis.
* **Padronização:** Semântica única de observabilidade compartilhada entre todos os microsserviços.

### Negativas
* **Sobrecarga Mínima de CPU/Rede:** Pequena fração de recursos do microsserviço é consumida para a coleta periódica (raspagem) efetuada pelo servidor Prometheus.
* **Exposição de Dados Internos:** Endpoints do Actuator expõem detalhes do sistema operacional. Em ambientes de produção reais, esses endpoints devem ser devidamente protegidos contra acessos externos não-autorizados (por exemplo, via Security rules ou API Gateway).
