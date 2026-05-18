# ADR 002: Consistência Atômica com Transactional Outbox e Transações ACID no MongoDB

## Status
**Accepted** (Aceito)

## Data
18 de Maio de 2026

## Contexto
No microsserviço de vendas (`pdv-service`), a gravação física da venda no MongoDB e o disparo correspondente do evento `VendaEmitida` para o Apache Kafka precisam ser garantidos sob uma semântica de **Tudo-ou-Nada (Atomicidade)**.
Se salvassemos a venda no MongoDB e fizessemos uma chamada síncrona de rede ao Kafka:
1. Uma queda temporária da rede ou do cluster Kafka causaria a perda definitiva do evento, quebrando a integridade de estoque (`inventory-service`) e de receitas (`prescription-service`).
2. Se o Kafka disparasse o evento, mas o banco de dados MongoDB falhasse ao salvar a venda, teríamos eventos de baixa de estoque e validação para uma venda inexistente.

## Decisão
Decidimos implementar o padrão **Transactional Outbox** integrado às transações nativas multi-documento do MongoDB:
1. **Gravação Unificada:** A venda (`Venda`) e o registro de intenção de disparo do evento (`OutboxEventDocument`) são persistidos na mesma transação lógica/física de banco de dados.
2. **Uso de Transações ACID no MongoDB:** Declaração de um bean `MongoTransactionManager` no Spring Boot e anotação do caso de uso `RegistrarVendaUseCase.executar(...)` com `@Transactional`. Qualquer falha física ou lógica em qualquer uma das gravações dispara um rollback atômico completo do banco de dados.
3. **Mapeamento de Infraestrutura (Replica Set local):** Atualização do `docker-compose.yml` para rodar o MongoDB local em modo Single-Node Replica Set (`--replSet rs0`) com inicialização automatizada via container utilitário `mongodb-init` (executando `rs.initiate(...)`).
4. **Dispatcher Resiliente Assíncrono:** Criação do `OutboxEventScheduler` rodando em background no `pdv-service` (ativado por `@EnableScheduling`). O agendador varre continuamente registros com status `PENDING`, realiza o envio seguro ao Kafka exigindo confirmação física de recebimento (síncrono/ACK), e altera o status para `PROCESSED` de forma resiliente com tratamento de erros.

## Consequências

### Positivas
* **Consistência Atômica Absoluta:** O evento de venda no Kafka e o registro físico de venda no MongoDB estão 100% acoplados e protegidos por garantias ACID clássicas.
* **Garantia de Entrega At-Least-Once:** Em caso de indisponibilidade física ou oscilação temporária do broker Kafka, o `OutboxEventScheduler` tentará reenviar as intenções `PENDING` indefinidamente até obter sucesso, eliminando qualquer risco de perda de mensagens.
* **Desacoplamento de Latência:** O fluxo principal HTTP de finalização de venda não sofre com a latência ou queda física do Kafka. O caixa da farmácia registra a venda em milissegundos.

### Negativas
* **Exigência de Replica Set no MongoDB:** O uso de transações ACID requer obrigatoriamente que a instância do MongoDB execute como um Replica Set (mesmo em desenvolvimento local), aumentando a complexidade da receita de containers.
* **Possível Duplicidade (Consumidor Idempotente):** A semântica *at-least-once* do Outbox implica que, em oscilações raras de confirmação, um evento de venda possa ser disparado mais de uma vez. Os microsserviços consumidores (`prescription-service` e `inventory-service`) devem implementar proteção de idempotência.
