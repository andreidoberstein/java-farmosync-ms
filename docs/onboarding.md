# FarmoSync - Manual de Onboarding e Desenvolvimento

Bem-vindo ao **FarmoSync**! Este documento serve como ponto de partida para qualquer desenvolvedor (ou Inteligência Artificial) que esteja ingressando no desenvolvimento do ecossistema de microsserviços do PDV de Farmácia.

---

## 1. Visão Geral do Sistema

O FarmoSync é um sistema para Ponto de Venda (PDV) de farmácia de alta performance, projetado com uma arquitetura **desacoplada, orientada a eventos (Event-Driven)** e baseada em **DDD (Domain-Driven Design)**.

### A Stack Tecnológica:
*   **Backend:** Java 17+ com Spring Boot 3.x
*   **Banco de Dados:** MongoDB (Persistência rápida e flexível de cupons e inventário)
*   **Mensageria:** Apache Kafka (Comunicação assíncrona tolerante a falhas)
*   **Containers:** Docker & Docker Compose
*   **Testes:** JUnit 5, Mockito e **Testcontainers**

---

## 2. Pré-requisitos do Ambiente

Para rodar e desenvolver este projeto localmente, você precisará ter instalado:

1.  **Java Development Kit (JDK) 17 ou superior** (Recomendado: Eclipse Temurin OpenJDK 17).
2.  **Docker & Docker Compose** (Para subir a infraestrutura de Kafka e MongoDB locais).
3.  **Git** (Para controle de versão).
4.  **IDE de sua preferência** (IntelliJ IDEA, VS Code com Extensões de Java, ou Eclipse).

---

## 3. Estrutura do Repositório

O repositório é organizado de forma modular. À medida que formos criando os microsserviços, a raiz terá a seguinte estrutura:

```text
java-ms/
├── docs/                 # Documentações de arquitetura, onboarding e decisões (ADRs)
│   ├── decisions/        # Architecture Decision Records (ADRs)
│   └── onboarding.md     # Este guia
├── pdv-service/          # Microsserviço de Vendas (Spring Boot + MongoDB)
├── prescription-service/ # Microsserviço de Validação de Receitas
├── inventory-service/    # Microsserviço de Controle de Lotes e Estoque
└── docker-compose.yml    # Orquestração local de MongoDB, Kafka e Zookeeper
```

---

## 4. Diretrizes de Desenvolvimento e Git Flow

Para manter a qualidade e o histórico limpo do projeto, seguimos regras rígidas:

### 4.1. Regras de Código Limpo (Clean Code)
1.  **Sem comentários temporários ou console logs:** Comentários explicativos só devem existir se o código não for autoexplicativo por si só devido a alguma complexidade de negócio. Códigos comentados ou `System.out.println` temporários devem ser apagados antes de qualquer commit.
2.  **Foco no Domínio:** A lógica de negócio principal (como validações sanitárias, cálculos de impostos) reside no pacote `domain` do microsserviço e não deve possuir dependência direta de frameworks (Spring/Mongo).

### 4.2. Estratégia de Commits (Conventional Commits)
Nós utilizamos **commits atômicos** e a padronização de mensagens de commit em **inglês**. As mensagens devem seguir o formato:

`type(scope): description`

*   `feat`: Nova funcionalidade (ex: `feat(pdv): implement sale entity inside domain layer`)
*   `fix`: Correção de bug (ex: `fix(inventory): resolve nullpointer on batch validation`)
*   `docs`: Alteração em arquivos de documentação (ex: `docs(onboarding): update running instructions`)
*   `infra`: Configurações de infraestrutura/Docker (ex: `infra(kafka): add custom replication configurations`)

> [!IMPORTANT]
> **Fluxo de Commit:** O desenvolvedor solicita o commit, o assistente prepara a mensagem e aguarda a **confirmação manual** do desenvolvedor antes de executá-la no Git.

---

## 5. Como rodar o projeto localmente (Próximo Passo)

1. Clone o repositório.
2. Suba a infraestrutura base rodando `docker-compose up -d` na raiz do projeto (arquivo a ser criado).
3. Importe os microsserviços na sua IDE de preferência como projetos Maven independentes.
