# FarmoSync - Roadmap e Etapas de Desenvolvimento

Este documento descreve a estratégia de desenvolvimento passo a passo do **FarmoSync**, estruturada em fases incrementais e lógicas. A ordem das fases garante que a infraestrutura e os conceitos fundamentais estejam consolidados antes de iniciarmos as integrações complexas.

---

## Fluxo Geral da Arquitetura
Antes de iniciar, o desenvolvedor deve ter em mente o fluxo dos eventos gerados:
1. `PDV Service` registra a intenção de venda -> Emite `venda-emitida`.
2. `Prescription Service` consome `venda-emitida` -> Valida receitas médicas -> Emite `receita-validada`.
3. `Inventory Service` consome `receita-validada` -> Decrementa lotes no MongoDB -> Emite `estoque-atualizado`.

---

## 🚀 Fase 1: Infraestrutura e Ambiente Local
O objetivo desta fase é subir a infraestrutura necessária para suportar microsserviços desacoplados.

- [ ] **1.1. Configuração do Docker Compose**
  - Criar um `docker-compose.yml` na raiz do projeto contendo:
    - **MongoDB:** Banco NoSQL para persistência de vendas e estoque.
    - **Apache Kafka + Zookeeper (ou KRaft):** Broker de mensageria para comunicação orientada a eventos.
    - **Mongo Express / Kafdrop (Opcional):** Interfaces visuais para facilitar a depuração local.
- [ ] **1.2. Validação da Infraestrutura**
  - Subir os containers (`docker compose up -d`) e verificar se os serviços estão respondendo corretamente nas portas padrão.

---

## 📦 Fase 2: pdv-service (O Coração do Sistema)
O primeiro microsserviço a ser desenvolvido é o emissor dos dados de venda.

- [ ] **2.1. Inicialização do Projeto**
  - Criar o projeto Spring Boot com Java 17+ e Maven.
  - Adicionar dependências: Spring Web, Spring Data MongoDB, Spring Kafka, Lombok, Testcontainers, e JUnit 5.
- [ ] **2.2. Modelagem do Domínio (DDD)**
  - Criar a camada `domain` sem dependências do Spring.
  - Definir a entidade raiz de agregado (`Venda`) e seus objetos de valor (`ItemVenda`, `Lote`, `Cpf`).
  - Implementar regras de negócio básicas (cálculo de descontos, validação de campos obrigatórios).
- [ ] **2.3. Casos de Uso (Application/Use Cases)**
  - Implementar o caso de uso `RegistrarVendaUseCase` estruturando a orquestração: buscar dados -> aplicar domínio -> salvar -> emitir evento.
- [ ] **2.4. Persistência de Dados (MongoDB)**
  - Configurar as coleções no MongoDB usando `MongoRepository`.
  - Definir índices para busca rápida de vendas.
- [ ] **2.5. Mensageria (Kafka Producer)**
  - Implementar o produtor Kafka para enviar a mensagem `venda-emitida` no tópico `venda-emitida-topic` de forma assíncrona.
- [ ] **2.6. Testes Automatizados**
  - **Unitários:** Testar regras de cálculo de valores e descontos no domínio com JUnit 5.
  - **Integração:** Usar Testcontainers para subir containers reais do MongoDB e Kafka e validar se o fluxo de persistência + publicação funciona perfeitamente de ponta a ponta.
- [ ] **2.7. API REST (Presentation)**
  - Criar o controller `VendaController` expondo o endpoint `POST /api/v1/vendas` para registrar uma intenção de venda.

---

## 📑 Fase 3: prescription-service (Validação Sanitária)
Este serviço garantirá a segurança na venda de medicamentos controlados.

- [ ] **3.1. Inicialização do Microsserviço**
  - Criar o esqueleto Spring Boot com dependências de Kafka, Mongo (para auditoria de validações) e testes.
- [ ] **3.2. Regras de Validação de Receitas (Domínio)**
  - Definir o modelo de dados de Receita Médica (`CRM`, `NomeMedico`, `AssinaturaDigital`).
  - Implementar regras de negócio: validação do formato do CRM e prazo de validade da receita (ex: antibióticos valem por 10 dias).
- [ ] **3.3. Consumidor Kafka (Consumer)**
  - Consumir mensagens do tópico `venda-emitida-topic`.
  - Diferenciar medicamentos comuns de controlados (apenas controlados exigem validação de receita).
- [ ] **3.4. Validação Assíncrona e Publicação**
  - Executar a validação.
  - Publicar o resultado (`receita-validada`) no tópico `receita-validada-topic` com status `APROVADO` ou `REJEITADO`.
- [ ] **3.5. Testes Automatizados**
  - Testes de integração usando Testcontainers simulando o recebimento da mensagem no Kafka e validando a publicação do resultado correto.

---

## 🩺 Fase 4: inventory-service (Controle Físico por Lote)
Este serviço garante a rastreabilidade física exigida pela ANVISA.

- [ ] **4.1. Inicialização do Microsserviço**
  - Criar o projeto Spring Boot com dependências de MongoDB (rastreabilidade precisa) e Kafka.
- [ ] **4.2. Modelagem do Estoque por Lote**
  - Modelar o agregado do Produto contendo uma lista de `Lotes` (com número, quantidade física em estoque e data de validade).
- [ ] **4.3. Consumidor Kafka (Consumer)**
  - Consumir eventos do tópico `receita-validada-topic`.
  - Processar somente vendas cujo status da receita foi `APROVADO`.
- [ ] **4.4. Baixa Atômica e Idempotente no MongoDB**
  - Atualizar o estoque no MongoDB usando operações atômicas (`$elemMatch`, `$inc`) para prevenir concorrência indesejada (dois caixas vendendo o mesmo lote simultaneamente).
  - Validar idempotência do evento através de uma tabela/coleção auxiliar para evitar baixa duplicada caso o Kafka reenvie a mensagem.
- [ ] **4.5. Testes com Testcontainers**
  - Simular múltiplos caixas vendendo simultaneamente usando threads paralelas nos testes de integração para garantir a consistência das baixas de estoque do MongoDB.

---

## 🛠️ Fase 5: Resiliência, Monitoramento e Polimento
Fase final focada em tornar a arquitetura tolerante a desastres e pronta para produção.

- [ ] **5.1. Implementação de Dead Letter Queue (DLQ)**
  - Configurar tópicos de erro no Kafka para receber mensagens corrompidas ou que falharam em consecutivas tentativas de reprocessamento.
- [ ] **5.2. Padrão Transactional Outbox**
  - Implementar no `PDV Service` a garantia de que a gravação da venda no banco e o disparo do evento no Kafka ocorram sob a mesma transação lógica.
- [ ] **5.3. Monitoramento com Prometheus e Grafana**
  - Monitorar tempos de resposta de consumo no Kafka (consumer lag) e volume de gravações por segundo no MongoDB.
