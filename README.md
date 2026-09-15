# Voting API Challenge

API REST para cadastro de pautas, abertura de sessões de votação, registro de votos e apuração do resultado.

Cada pauta pode ter uma sessão de votação. Um associado, identificado pelo CPF, pode votar uma única vez na sessão. Os votos aceitos são `YES` e `NO`.

## Tecnologias

- Java 17
- Spring Boot
- Spring Web MVC e Spring Validation
- Spring Data JPA
- PostgreSQL
- RabbitMQ e Spring AMQP
- Flyway
- Springdoc OpenAPI / Swagger UI
- JUnit 5, Mockito, MockMvc e Testcontainers

## Pré-requisitos

- JDK 17
- Docker Engine acessível (Docker Desktop ou Docker no WSL2)
- IntelliJ IDEA

## Serviços locais

O arquivo `docker-compose.yml` inicia PostgreSQL e RabbitMQ. Para iniciar ambos, execute na raiz do projeto:

```bash
docker compose up -d
```

O PostgreSQL utiliza os mesmos valores configurados pela aplicação:

```yaml
url: jdbc:postgresql://localhost:5432/voting_api
username: voting_user
password: voting_password
```

Os dados ficam preservados nos volumes Docker `postgres_data` e `rabbitmq_data`. Para encerrar os serviços sem remover os dados, execute:

```bash
docker compose down
```

Essas são as credenciais padrão usadas pela aplicação no ambiente local.

## Executando a aplicação

Primeiro, inicie os serviços locais:

```bash
docker compose up -d
```

### Linha de comando

O projeto pode ser executado sem uma IDE pelo Maven Wrapper.

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No macOS ou Linux:

```bash
./mvnw spring-boot:run
```

Se o Maven Wrapper não estiver disponível no ambiente, use uma instalação local do Maven:

```bash
mvn spring-boot:run
```

### IntelliJ IDEA

Como alternativa, abra `VotingApiApplication` e clique em **Run** ao lado do método `main`.

### Migrations

Durante a inicialização, o Flyway conecta no PostgreSQL e executa automaticamente as migrations pendentes localizadas em `src/main/resources/db/migration`. As migrations aplicadas são registradas na tabela `flyway_schema_history`; por isso, cada versão é executada uma única vez para o mesmo banco.

Com a aplicação em execução, os endereços locais são:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificação OpenAPI: `http://localhost:8080/v3/api-docs`
- RabbitMQ Management: `http://localhost:15672`

As credenciais locais do RabbitMQ são:

```text
Usuário: voting_user
Senha: voting_password
```

## Endpoints

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/v1/agenda-items` | Cria uma pauta. |
| `POST` | `/api/v1/voting-sessions` | Abre uma sessão para uma pauta. |
| `POST` | `/api/v1/votes` | Registra um voto em uma sessão aberta. |
| `GET` | `/api/v1/voting-sessions/{votingSessionId}/result` | Retorna a apuração final. |

### 1. Criar uma pauta

```http
POST /api/v1/agenda-items
Content-Type: application/json
```

```json
{
  "title": "Approve annual budget",
  "description": "Voting on the annual budget proposal."
}
```

Resposta: `201 Created`.

### 2. Abrir uma sessão de votação

```http
POST /api/v1/voting-sessions
Content-Type: application/json
```

```json
{
  "agendaItemId": 1,
  "durationMinutes": 5
}
```

`durationMinutes` é opcional. Quando não informado, a sessão fica aberta por um minuto.

Resposta: `201 Created`.

### 3. Registrar um voto

```http
POST /api/v1/votes
Content-Type: application/json
```

```json
{
  "votingSessionId": 1,
  "associateCpf": "620.940.790-07",
  "choice": "YES"
}
```

O CPF é validado localmente e normalizado antes da persistência. Em seguida, a API consulta o serviço externo de elegibilidade. Apenas a resposta `ABLE_TO_VOTE` permite registrar o voto.

Resposta: `201 Created`.

### 4. Consultar o resultado

```http
GET /api/v1/voting-sessions/1/result
```

O resultado é disponibilizado somente após o encerramento da sessão. A apuração usa maioria simples:

- Mais votos `YES`: `APPROVED`.
- Mais votos `NO`: `REJECTED`.
- Mesma quantidade: `TIED`.

## Mensageria do resultado

Quando uma sessão encerra, a aplicação publica o resultado na fila durável `voting-results` do RabbitMQ. Um agendador executa a verificação a cada 10 segundos e processa somente sessões cujo resultado ainda não foi publicado.

A API atua como produtora do evento. O consumo é responsabilidade de outros serviços da plataforma. A mensagem não inclui CPF.

Exemplo de evento publicado:

```json
{
  "votingSessionId": 1,
  "agendaItemId": 1,
  "agendaItemTitle": "Approve annual budget",
  "yesVotes": 3,
  "noVotes": 1,
  "totalVotes": 4,
  "result": "APPROVED",
  "closedAt": "2026-09-14T22:00:00Z"
}
```

Para verificar localmente, abra o RabbitMQ Management, acesse a fila `voting-results` e use a opção **Get messages**. Para visualizar sem remover a mensagem da fila, selecione **Nack message requeue true**.

## Integração de elegibilidade por CPF

O bônus de integração externa usa o endpoint Mocky fornecido no enunciado. A URL-base está configurada em `application.yml` e a chamada final segue o formato:

```text
https://run.mocky.io/v3/57f23672-c15f-48f8-90d3-d84ce00250b8/users/{cpf}
```

O serviço pode retornar `ABLE_TO_VOTE`, `UNABLE_TO_VOTE` ou `404` para CPF não encontrado.

### Limitação conhecida

Durante a validação manual, o endpoint Mocky fornecido pelo enunciado apresentou certificado TLS não confiável em redes independentes. A URL original permanece configurada conforme o requisito e a validação de certificado HTTPS não foi desabilitada. Enquanto o serviço estiver indisponível, a API retorna `503 Service Unavailable` ao registrar um voto.

## Tratamento de erros

A API utiliza `ProblemDetail` para padronizar erros:

| Status | Situação |
| --- | --- |
| `400 Bad Request` | Campos inválidos ou CPF em formato inválido. |
| `404 Not Found` | Pauta, sessão ou CPF externo não encontrado. |
| `409 Conflict` | Sessão fechada, voto duplicado, associado não elegível ou pauta já com sessão. |
| `503 Service Unavailable` | Serviço externo de elegibilidade indisponível. |

## Testes

Os testes unitários cobrem regras dos services e da publicação de resultados com Mockito. Os controllers são testados com MockMvc. O fluxo principal também possui teste de integração com PostgreSQL real via Testcontainers.

Antes de rodar todos os testes, confirme que o Docker Engine está em execução. No IntelliJ:

1. Acesse **File > Settings > Build, Execution, Deployment > Build Tools > Maven**.
2. Em **Maven home path**, selecione **Bundled (Maven 3)**.
3. Na janela **Maven**, execute `Lifecycle > test`.

## Decisões técnicas

As decisões de implementação estão registradas em [devNotes.md](devNotes.md).
