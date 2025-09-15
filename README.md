# DeliveryTech API

## Descrição

Este projeto é uma API RESTful para um sistema de delivery, desenvolvido como parte do curso qualificaSP - Arquitetura de Software com SpringBoot. A API permite o gerenciamento completo de clientes, restaurantes, produtos e o fluxo de criação de pedidos, com uma camada robusta de observabilidade para monitoramento em tempo real.

## Funcionalidades

✅ Gerenciamento de Clientes: CRUD completo (Criar, Ler, Atualizar, Inativar).

✅ Gerenciamento de Restaurantes: CRUD completo (Criar, Ler, Atualizar, Inativar).

✅ Gerenciamento de Produtos: CRUD completo, com associação a um restaurante.

✅ Fluxo de Pedidos:

  - Criação de um novo pedido.
  - Adição de itens a um pedido existente.
  - Confirmação de pedido com cálculo de totais.
  - Listagem de pedidos por cliente.

✅ Observabilidade Completa:

  - Monitoramento de saúde da aplicação e dependências (`/actuator/health`).
  - Métricas de negócio e performance em formato Prometheus (`/actuator/prometheus`).
  - Logs estruturados em JSON com Correlation ID para rastreamento.
  - Alertas proativos e runbook para resposta a incidentes.

✅ Validação de Dados: Garante a integridade dos dados na entrada da API.

✅ Banco de Dados em Memória: Utiliza H2 para facilitar o desenvolvimento e os testes

## Tecnologias

  - **Linguagem:** Java 21
  - **Framework:** Spring Boot 3
  - **Data:** Spring Data JPA / Hibernate
  - **Banco de Dados:** H2 In-Memory Database
  - **Build Tool:** Maven
  - **Validação:** Jakarta Bean Validation
  - **Utilitários:** Lombok
  - **Monitoramento:** Spring Boot Actuator, Micrometer, Prometheus
  - **Containerização:** Docker, Docker Compose

## Como executar

Existem duas maneiras de executar a aplicação: localmente com Maven ou via Docker, que já inclui o ambiente de monitoramento com Prometheus.

### Pré-requisitos

  - [JDK (Java Development Kit)](https://www.oracle.com/java/technologies/downloads/) - Versão 21 ou superior.
  - [Maven](https://maven.apache.org/download.cgi) - Versão 3.8 ou superior.
  - [Docker e Docker Compose](https://www.docker.com/products/docker-desktop/) - Para a execução em container.

### Executando com Docker (Recomendado)

Este método irá iniciar a API e o serviço de monitoramento Prometheus.

1.  **Clone o repositório:**
    `git clone [URL_DO_SEU_REPOSITORIO_AQUI]`

2.  **Navegue até a pasta do projeto:**
    `cd deliverytech`

3.  **Construa o pacote da aplicação com Maven:**
    (Este passo só é necessário na primeira vez ou após alterações no código)
    `./mvnw clean package`

4.  **Inicie os containers com Docker Compose:**
    `docker-compose up --build`

5.  **Acesse os serviços:**

      - **API:** `http://localhost:8080`
      - **UI do Prometheus:** `http://localhost:9090`
      - **Console do H2:** `http://localhost:8080/h2-console`

### Executando Localmente com Maven

1.  **Clone e navegue** até a pasta do projeto (passos 1 e 2 acima).
2.  **Execute a aplicação com o Maven:**
    `mvn spring-boot:run`
3.  **Acesse a API:**
      - O servidor estará rodando em `http://localhost:8080`.

-----

## Observabilidade e Monitoramento

Para garantir a saúde, performance e confiabilidade da aplicação, foi implementada uma estratégia de observabilidade baseada nos três pilares: **Métricas, Logs e Traces**. Esta seção documenta as ferramentas e configurações implementadas.

### Lista de Métricas Implementadas

As seguintes métricas de negócio e performance foram implementadas usando Micrometer e estão disponíveis no endpoint `/actuator/prometheus`.

| Métrica (Nome) | Tipo | Propósito |
| :--- | :--- | :--- |
| `delivery_pedidos_total` | `Counter` | Conta o número total de pedidos processados desde a inicialização da API. |
| `delivery_pedidos_sucesso_total` | `Counter` | Conta especificamente os pedidos que foram processados com sucesso. |
| `delivery_pedidos_erro_total` | `Counter` | Conta os pedidos que resultaram em erro durante o processamento. |
| `delivery_pedido_processamento_seconds` | `Timer` | Mede a latência (tempo de duração) do processamento de um pedido. |
| `delivery_usuarios_ativos_total` | `Gauge` | Mostra o número de usuários ativos na plataforma em um dado momento (valor simulado). |

### Guia de Interpretação dos Health Checks

O status de saúde da aplicação e de suas dependências pode ser verificado em tempo real através do endpoint `/actuator/health`.

O status geral pode ser:

  - **`UP`**: A aplicação e todos os seus componentes essenciais estão funcionando corretamente.
  - **`DOWN`**: Um ou mais componentes críticos falharam, indicando um problema que pode impactar os usuários.

Abaixo estão os componentes monitorados e o que cada status significa:

| Componente | Status `UP` Significa... | Status `DOWN` Significa... |
| :--- | :--- | :--- |
| **`database`** | A aplicação conseguiu se conectar e validar a conexão com o banco de dados H2. | A conexão com o banco de dados falhou ou não é mais válida. |
| **`externalService`** | O serviço externo simulado (ex: Gateway de Pagamento) foi contatado com sucesso e respondeu. | Houve uma falha de comunicação com o serviço externo (timeout, erro 5xx, etc.). |
| **`diskSpace`** | O espaço em disco no servidor onde a aplicação está rodando está acima do limite mínimo configurado. | O espaço livre em disco está perigosamente baixo, arriscando falhas de escrita (logs, etc.). |

### Documentação dos Correlation IDs

#### O que é?

O **Correlation ID** (ID de Correlação) é um identificador único atribuído a cada requisição que chega na API. Ele persiste durante todo o ciclo de vida daquela requisição.

#### Por que é importante?

Ele permite rastrear todas as operações, logs e eventos relacionados a uma única requisição. Se um usuário reporta um erro, podemos usar o `Correlation ID` da sua requisição para encontrar *exatamente* todos os logs gerados por ela, facilitando drasticamente a depuração.

#### Como funciona na prática?

1.  Um filtro (`CorrelationIdFilter`) intercepta todas as requisições.
2.  Ele verifica se a requisição já possui o header HTTP `X-Correlation-ID`. Se não, um novo UUID é gerado.
3.  Este ID é adicionado ao **MDC (Mapped Diagnostic Context)** do SLF4J.
4.  Nossa configuração do `logback-spring.xml` está programada para incluir automaticamente o valor do `correlationId` do MDC em cada linha de log formatada como JSON.
5.  O ID também é retornado no header `X-Correlation-ID` da resposta, para que o cliente (frontend, por exemplo) possa exibi-lo ou registrá-lo.

#### Como visualizar?

No console da aplicação, cada log em JSON conterá o campo `"correlationId"`, como no exemplo:

```json
{
  "timestamp": "2025-09-10 22:20:05.123",
  "level": "INFO",
  "thread": "http-nio-8080-exec-1",
  "logger": "com.delivery_api.controller.RestauranteController",
  "correlationId": "a3f5b1c8d7e6f4a2",
  "message": "Buscando restaurante com ID: 1"
}
```

### Runbook Básico para Resposta a Alertas

Este runbook descreve os passos a serem seguidos quando um alerta (atualmente logado no console pelo `AlertService`) é disparado.

-----

#### Alerta 1: `HIGH_ERROR_RATE`

  * **Severidade:** `CRITICAL`
  * **Descrição:** A taxa de pedidos com erro ultrapassou o limite configurado (ex: 10%) em um curto período.
  * **Possíveis Causas (Diagnóstico):**
      * Um bug foi introduzido em um deploy recente.
      * Um serviço externo crítico (ex: gateway de pagamento, consulta de CEP) está instável ou fora do ar.
      * Uma validação de dados está falhando para um grande número de usuários (ex: mudança no formato de entrada esperado).
      * Problemas de infraestrutura (banco de dados sobrecarregado, falta de memória).
  * **Passos de Ação (Resposta):**
    1.  **Analisar os Logs:** Verifique os logs da aplicação no momento em que o alerta começou. Filtre por logs de nível `ERROR` para encontrar exceções e stack traces.
    2.  **Rastrear com Correlation ID:** Use o `Correlation ID` de algumas requisições que falharam para entender o fluxo completo e identificar em que ponto a falha ocorreu.
    3.  **Verificar o Health Check:** Acesse o endpoint `/actuator/health` para ver se alguma dependência (`database`, `externalService`) está com status `DOWN`.
    4.  **Analisar Commits Recentes:** Verifique o histórico de commits no Git para identificar mudanças recentes que possam ter causado o problema.
    5.  **Comunicar a Equipe:** Informe a equipe sobre o incidente e as ações que estão sendo tomadas.

-----

#### Alerta 2: `HIGH_RESPONSE_TIME`

  * **Severidade:** `WARNING`
  * **Descrição:** O tempo médio de resposta das requisições ultrapassou o limite aceitável (ex: 1000ms).
  * **Possíveis Causas (Diagnóstico):**
      * Uma query ao banco de dados está lenta ou ineficiente (falta de um índice, consulta complexa).
      * A aplicação está com sobrecarga de requisições.
      * Um serviço externo está demorando para responder, causando lentidão em cascata.
      * Consumo excessivo de CPU ou memória (ex: um loop infinito, memory leak).
  * **Passos de Ação (Resposta):**
    1.  **Analisar Métricas de Performance:** Verifique as métricas do Prometheus/Grafana (se configurado) para identificar gargalos (CPU, memória, latência de queries do JPA/Hibernate).
    2.  **Identificar Endpoints Lentos:** Analise os logs ou traces (se o Zipkin estiver configurado) para ver quais endpoints específicos estão lentos. A métrica `delivery_pedido_processamento_seconds` ajuda a confirmar isso.
    3.  **Analisar Logs de Query:** Verifique os logs do Hibernate (`logging.level.org.hibernate.SQL=DEBUG`) para ver as queries que estão sendo executadas e seu tempo de duração.
    4.  **Considerar Escalabilidade:** Se a causa for alta demanda, avalie a necessidade de escalar a aplicação (aumentar instâncias).

-----

## Documentação da API

A documentação completa e interativa da API está disponível via Swagger UI em:
`http://localhost:8080/swagger-ui.html`

### Endpoints Principais

(Tabelas de Clientes, Restaurantes, Produtos e Pedidos como você já tinha)
...

### Endpoints de Monitoramento e Observabilidade

| Método HTTP | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/actuator` | Lista todos os endpoints de monitoramento disponíveis. |
| `GET` | `/actuator/health` | Mostra o status de saúde detalhado da aplicação e suas dependências. |
| `GET` | `/actuator/info` | Exibe informações customizadas sobre a aplicação. |
| `GET` | `/actuator/prometheus`| Expõe as métricas no formato compatível com o Prometheus. |
| `GET` | `/dashboard` | Apresenta um dashboard web simples com métricas em tempo real. |

-----

## Estrutura do Projeto

O projeto está organizado em uma arquitetura de camadas para separação de responsabilidades:

  - **`com.delivery_api.controller`**: Camada de API REST, responsável por receber requisições HTTP.
  - **`com.delivery_api.service`**: Camada de serviço, onde reside a lógica de negócio.
  - **`com.delivery_api.repository`**: Camada de acesso a dados (Data Access Layer), usando Spring Data JPA.
  - **`com.delivery_api.model`**: As entidades JPA que modelam o banco de dados.
  - **`com.delivery_api.enums`**: Enumerações usadas no projeto, como `StatusPedido`.
  - **`com.delivery_api.health`**: Health checks customizados para o Actuator.
  - **`com.delivery_api.filter`**: Filtros de requisição, como o `CorrelationIdFilter`.

## Desenvolvedor

Henrique Radzevicius Toledo - TI 56 - Arquitetura de Sistemas
Desenvolvido com JDK 21 e Spring Boot 3.5.5