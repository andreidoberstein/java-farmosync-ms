# ADR 006: Coreografia de Saga Compensatória para Resolução e Rejeição de Vendas

## Status
**Accepted** (Aceito)

## Data
19 de Maio de 2026

## Contexto
No FarmoSync, o processo de venda envolve múltiplos microsserviços de forma assíncrona: `pdv-service` (criação da venda), `prescription-service` (validação de receita controlada) e `inventory-service` (baixa de estoque). 
A venda é inicialmente persistida com o status de `PENDENTE`. Se a receita associada for considerada inválida, ou se o saldo de estoque físico do medicamento for insuficiente, o fluxo assíncrono falhava silenciosamente e a venda permanecia como `PENDENTE` indefinidamente. 
Precisávamos garantir que o ciclo de vida da venda fosse concluído com consistência eventual: transicionando para `PROCESSADA` (sucesso absoluto das validações) ou `REJEITADA` (quando ocorrem falhas no estoque ou na receita), atuando como uma saga compensatória.

## Decisão
Decidimos implementar o padrão **Saga Baseada em Coreografia** com tópicos de confirmação e reversão:
1. **Criação de Tópico de Desfecho:** O `inventory-service` (último elo da cadeia de sucesso) passa a publicar o resultado da transação no tópico `estoque-atualizado-topic`, carregando o payload `EstoqueAtualizadoEvent` com a indicação de status (`SUCESSO` ou `ERRO`) e o motivo correspondente da rejeição.
2. **Consumidor no PDV:** No `pdv-service`, implementamos a classe `KafkaEstoqueAtualizadoConsumer` para escutar e processar de forma resiliente as mensagens do tópico de desfecho do inventário.
3. **Transição de Domínio e Persistência:** O consumer repassa o comando ao Use Case `AtualizarStatusVendaUseCase` que busca a venda no banco de dados e aplica as regras de negócio de domínio:
   - Se o evento for de `SUCESSO` -> invoca `venda.finalizar()` (altera status para `PROCESSADA`).
   - Se o evento for de `ERRO` -> invoca `venda.rejeitar()` (altera status para `REJEITADA`).
4. **Idempotência e Segurança:** A busca e gravação ocorrem de forma atômica no banco do PDV, garantindo que eventos duplicados ou fora de ordem não quebrem as transições de status permitidas pela máquina de estados do domínio.

## Consequências

### Positivas
* **Consistência Eventual Garantida:** Garante que o status final da venda no banco de dados reflita com precisão o desfecho das validações físicas de estoque e receita, sem manter vendas "presas" no estado pendente.
* **Resiliência e Desacoplamento:** O fluxo continua assíncrono e baseado em eventos, sem a necessidade de chamadas HTTP síncronas bloqueantes entre microsserviços.
* **Rastreabilidade de Falhas:** O motivo da rejeição do estoque/receita fica registrado na venda, facilitando auditoria e feedback no frontend.

### Negativas
* **Maior Complexidade do Fluxo:** A ausência de um orquestrador centralizado (Saga baseada em Orquestração) exige maior rigor no monitoramento de logs de eventos para rastrear transações que falharam silenciosamente entre os passos intermediários do pipeline.
* **Necessidade de Monitoramento de Falhas Finais:** Se o `pdv-service` falhar gravemente ao receber o evento de desfecho, o status ficará pendente, exigindo rotinas automáticas de reconciliação ou monitoramento (Dead Letter Queues).
