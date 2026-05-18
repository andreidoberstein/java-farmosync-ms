# ADR 001: Padrão Resiliente de Mensageria com Kafka Dead Letter Queue (DLQ)

## Status
**Accepted** (Aceito)

## Data
18 de Maio de 2026

## Contexto
Na arquitetura orientada a eventos do FarmoSync, os microsserviços `prescription-service` e `inventory-service` consomem mensagens críticas assincronamente do Apache Kafka. Erros inesperados podem ocorrer durante o consumo de mensagens por dois motivos principais:
1. **Erros Transientes (Falhas Físicas):** Interrupção temporária do MongoDB, lentidão momentânea da rede, etc.
2. **Erros Não-Transientes (Erros de Negócio/Formato):** Payloads corrompidos, dados inválidos que violam regras estritas (ex: CRM ou paciente inválido).

Se não tratarmos esses erros, o consumidor do Kafka tentará reprocessar a mesma mensagem eternamente (entrando em loop infinito de erro), travando o consumo de mensagens saudáveis subsequentes na partição e causando alto *consumer lag*.

## Decisão
Decidimos implementar o padrão **Dead Letter Queue (DLQ)** com retentativas controladas e isolamento de falhas:
1. **Mecanismo de Retentativa Controlada:** Cada consumidor Kafka tentará reprocessar uma mensagem com falha até **3 vezes**, com um intervalo (*backoff*) de **1 segundo** entre as tentativas.
2. **Uso de Recoverer do Spring Kafka:** Configuração de um `DefaultErrorHandler` acoplado a um `DeadLetterPublishingRecoverer`.
3. **Isolamento de Falha Física (Tópicos .DLT):** Se as 3 tentativas falharem, a mensagem é automaticamente desviada para um tópico de Dead Letter correspondente com o sufixo `.DLT` (ex: `receita-validada-topic.DLT` e `venda-emitida-topic.DLT`).
4. **Continuidade de Fluxo:** Após o desvio da mensagem problemática para o tópico DLT, a partição é liberada, e o consumidor continua processando as mensagens saudáveis seguintes normalmente.

## Consequências

### Positivas
* **Tolerância a Falhas e Resiliência:** Mensagens com erro não derrubam o microsserviço nem travam as partições do Kafka.
* **Sem Perda de Dados:** Mensagens problemáticas ficam seguras no tópico DLT para posterior auditoria, correção e reprocessamento manual ou via script.
* **Garantia de Vazão:** Manutenção do alto throughput e baixo consumer lag da nossa mensageria.

### Negativas
* **Aumento de Complexidade de Tópicos:** Criação e monitoramento de tópicos extras (DLT) no broker do Apache Kafka.
* **Consistência Eventual:** Mensagens que vão para a DLT exigem análise ativa dos operadores para correção manual de dados de negócio inconsistentes.
