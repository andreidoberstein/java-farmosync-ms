# ADR 007: Estruturação de Build com Maven Multi-Módulo e Parent POM

## Status
**Accepted** (Aceito)

## Data
19 de Maio de 2026

## Contexto
Anteriormente, o backend do FarmoSync era composto por 3 microsserviços Java independentes no mesmo repositório: `pdv-service`, `prescription-service` e `inventory-service`. Cada projeto possuía seu próprio arquivo `pom.xml` apontando isoladamente para o `spring-boot-starter-parent`.
Essa abordagem causava os seguintes problemas:
1. **Duplicação de Boilerplate:** Cada microsserviço repetia definições de dependências comuns (Lombok, Testcontainers, Springdoc) e configurações idênticas de plugins de compilação e teste (`failsafe`, `compiler`, `spring-boot-maven-plugin`).
2. **Divergência de Versões:** Alto risco de desalinhamento de versões de bibliotecas e drivers em produção, gerando problemas sutis de compatibilidade.
3. **Pipeline Complexo:** Dificuldade na automação de CI/CD, exigindo que o pipeline executasse builds sequenciais navegando manualmente para dentro do diretório de cada microsserviço.

## Decisão
Decidimos refatorar e centralizar a infraestrutura de builds do backend adotando um padrão **Maven Multi-Módulo**:
1. **Criação de Parent POM Raiz:** Criamos o arquivo `back/pom.xml` atuando como a raiz do backend com o tipo de empacotamento `<packaging>pom</packaging>` e contendo a declaração de módulos para os 3 serviços.
2. **Centralização e Herança:** O parent POM herda as configurações do Spring Boot Starter Parent, define as versões centralizadas das ferramentas em `<properties>` e controla as dependências e plugins através de blocos `<dependencyManagement>` e `<pluginManagement>`.
3. **Limpeza dos POMs Filhos:** Os arquivos `pom.xml` de cada microsserviço foram refatorados para herdar do `farmosync-parent` (usando `<relativePath>../pom.xml</relativePath>`). Versões duplicadas, tags de propriedades redundantes e blocos complexos de plugins foram removidos.
4. **Ciclo de Build Consistente:** O comando de build unificado `mvn clean verify` executado a partir do diretório raiz `back/` agora compila, testa e empacota todo o ecossistema backend em uma única execução gerenciada pelo Maven Reactor.

## Consequências

### Positivas
* **Eliminação de Duplicações:** Redução significativa no tamanho e na verbosidade dos arquivos de configuração do Maven dos microsserviços.
* **Manutenibilidade Aprimorada:** Atualizações de dependências críticas (como o Spring Boot ou Testcontainers) passam a ser feitas em um único ponto central no parent POM.
* **Simplificação do Pipeline de CI/CD:** O workflow do GitHub Actions executa apenas um comando de build centralizado (`mvn clean verify`), reduzindo o tempo e facilitando a configuração da esteira de automação.

### Negativas
* **Acoplamento do Ciclo de Build:** Falhas de compilação ou quebras de testes em um único microsserviço interrompem o build completo dos outros módulos sob o gerenciamento do reactor. Contudo, em uma arquitetura de monorepo, isso é desejado para evitar deploys inconsistentes.
