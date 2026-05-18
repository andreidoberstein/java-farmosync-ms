# ADR 005: Estratégia de Segurança Corporativa, Autenticação JWT e Proteção de Mensageria

## Status
**Accepted** (Aceito)

## Data
18 de Maio de 2026

## Contexto
O ecossistema do FarmoSync manipula dados de extrema criticidade regulatória (como dados pessoais de pacientes de acordo com a LGPD, registros de CRM de médicos e transações de venda de medicamentos controlados). A exposição pública direta das APIs dos microsserviços, o tráfego de mensageria em texto puro e bancos de dados sem autenticação expõem o sistema a vazamentos de dados, invasões laterais, falsificação de identidade (*spoofing*) e fraudes transacionais.

## Decisão
Decidimos estabelecer um modelo de segurança corporativa robusto e multicamadas (*Defense in Depth*) composto por cinco pilares fundamentais:

1. **Autenticação Centralizada e Unificada (Edge Security):**
   * Implementação de um **API Gateway** (ex: *Spring Cloud Gateway* ou *Kong*) como a única porta de entrada pública para a internet.
   * Acoplamento do Gateway a um **Identity Provider (IdP)** corporativo baseado em padrões de mercado (OAuth2 / OpenID Connect com **Keycloak** ou *Okta*).
   * O cliente autentica-se no Keycloak e obtém um token criptografado **JWT (JSON Web Token)** assinado digitalmente. O Gateway valida a assinatura do JWT no *edge* e repassa o token de forma segura na cadeia HTTP interna.

2. **Autorização Granular por Serviço (Spring Security + RBAC):**
   * Inclusão do **`spring-boot-starter-security`** em cada microsserviço individual para controle de autorização distribuída.
   * Configuração de uma `SecurityFilterChain` stateless para decodificar e ler as *roles* (papéis) dentro das reivindicações (*claims*) do JWT.
   * Aplicação do controle de acesso baseado em papéis (**Role-Based Access Control - RBAC**), restringindo endpoints por meio de anotações declarativas (ex: `@PreAuthorize("hasRole('ROLE_FARMACEUTICO')")` para operações estritas do `prescription-service`).

3. **Criptografia Interna e Isolamento (mTLS):**
   * Implantação dos microsserviços em uma rede privada isolada (**VPC - Virtual Private Cloud**), sem IPs públicos diretos.
   * Utilização de criptografia em trânsito ponto a ponto por meio de **mTLS (Mutual TLS)** de forma transparente na camada de rede (usando um Service Mesh como *Istio* ou *Linkerd*), eliminando tráfego interno em texto puro.

4. **Segurança de Dados e Mensageria (Kafka SASL & SSL):**
   * Configuração do Apache Kafka para proibir conexões anônimas em produção.
   * Ativação de criptografia de tráfego interna via **SSL** e autenticação de microsserviços via **SASL/SCRAM** (ou Kerberos).
   * Aplicação rígida de **ACLs (Access Control Lists)** por tópico (ex: `pdv-service` tem permissão exclusiva de *Write* no `venda-emitida-topic`, enquanto `prescription-service` tem permissão exclusiva de *Read*).

5. **Endurecimento de Banco de Dados NoSQL:**
   * Ativação de autenticação forte no MongoDB (`security.authorization: enabled`) e injeção de credenciais de serviço exclusivas via variáveis de ambiente criptografadas.

## Consequências

### Positivas
* **Conformidade Legal Absoluta:** Alinhamento completo com os requisitos estritos da LGPD (Lei Geral de Proteção de Dados) no Brasil e ANVISA para receitas controladas.
* **Segurança Centralizada e Robusta:** A lógica complexa de autenticação de usuários fica isolada no Keycloak, mantendo os microsserviços Java focados apenas na lógica de negócio fina de autorização.
* **Prevenção de Ataques Laterais:** A criptografia mTLS e as ACLs do Kafka impedem que a invasão de um contêiner vulnerável dê acesso irrestrito ao banco de dados ou a tópicos de outros microsserviços.

### Negativas
* **Aumento na Latência de Rede Interna:** O processo de criptografia e decriptografia TLS em todas as comunicações ponto a ponto mTLS adiciona um overhead mínimo de latência de rede interna.
* **Complexidade Operacional:** Exigência de infraestrutura especializada de contêineres e segurança (Kubernetes, Service Mesh, Keycloak, certificados de chaves públicas/privadas).
