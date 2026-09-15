# Decisões técnicas

## Arquitetura em camadas

O projeto adota as camadas `controller`, `service`, `repository`, `entity`, `dto`, `client` e `exception`. Cada camada concentra uma responsabilidade específica.

## Nomes em inglês

Classes, atributos, métodos, tabelas e colunas usam inglês.

## Entidades e persistência

As entidades principais são `AgendaItem`, `VotingSession` e `Vote`. O JPA faz o mapeamento entre classes Java e tabelas PostgreSQL.

## Identificadores numéricos

Os identificadores usam `Long` com geração pelo banco. IDs não são controles de segurança; autenticação e autorização são responsáveis pelo controle de acesso.

## Migrations com Flyway

O esquema do banco é versionado com Flyway. Alterações de estrutura são introduzidas por novas migrations, preservando o histórico já aplicado.

## Serviços locais com Docker Compose

O ambiente local utiliza Docker Compose para iniciar PostgreSQL e RabbitMQ. Volumes nomeados preservam os dados entre reinicializações dos containers.

## Datas de criação e votação

`@PrePersist` preenche datas automaticamente na criação das entidades. `OffsetDateTime` registra data, hora e fuso horário.

## Opções de voto como enum

`VoteChoice` limita o voto a `YES` ou `NO`. O valor é salvo como texto no banco para tornar os dados legíveis e evitar valores inválidos.

## Sessão única por pauta

Cada pauta possui no máximo uma sessão de votação. A regra existe no service e é reforçada por uma constraint `UNIQUE` no banco.

## Duração padrão da sessão

A duração é recebida em minutos. Quando não for enviada, a sessão dura um minuto, conforme o requisito do desafio.

## Voto único por CPF e sessão

Um CPF registra no máximo um voto por sessão. A aplicação consulta previamente e o banco mantém uma constraint única para a mesma regra.

## CPF normalizado

Antes de salvar ou consultar, o CPF remove pontos e hífen. Assim, formatos diferentes do mesmo CPF não geram votos duplicados.

## Validação local do CPF

A anotação `@CPF` valida formato e dígitos verificadores antes de executar o service. Ela não confirma identidade nem permissão para votar.

## Serviço externo de elegibilidade

`CpfEligibilityClient` encapsula a integração de elegibilidade e mantém detalhes HTTP fora de `VoteService`. O endpoint Mocky definido pelo desafio é configurado externamente em `application.yml`.

## Indisponibilidade de serviço externo

Falhas de conexão, TLS ou respostas inesperadas do serviço de elegibilidade são mapeadas para `503 Service Unavailable`. Regras de negócio continuam usando `409 Conflict`.

## DTOs para requests e responses

DTOs definem os contratos de entrada e saída da API. Respostas específicas evitam a serialização direta de relações JPA, como no registro de votos e na apuração.

## Carregamento lazy da pauta

`VotingSession` carrega `AgendaItem` sob demanda para reduzir consultas desnecessárias. A consulta de resultado executa em transação somente leitura para carregar os dados da pauta durante a montagem da resposta.

## Resultado apenas após encerrar a sessão

O resultado é final e só pode ser consultado depois de `closesAt`. Antes disso, a API retorna conflito para não confundir resultado final com placar parcial.

## Regra de apuração

O resultado é definido por maioria simples: mais votos `YES` resultam em `APPROVED`, mais votos `NO` resultam em `REJECTED` e empate resulta em `TIED`.

## Publicação do resultado da votação

Quando uma sessão encerra, um scheduler publica seu resultado na fila durável `voting-results` do RabbitMQ. A mensagem é JSON e contém identificadores da sessão e pauta, totais de votos, resultado e horário de encerramento, sem dados de CPF.

## Controle de publicação

`resultPublishedAt` registra a publicação do resultado na sessão. O schheduler consulta apenas sessões encerradas sem esse registro e o preenche após o envio, evitando republicações durante a operação normal.

## Tratamento de erros

`ProblemDetail` padroniza respostas de erro. Requests inválidos retornam `400` com os campos que falharam na validação, recursos inexistentes retornam `404` e conflitos de regra de negócio ou integridade retornam `409`.

## Logs de negócio

Eventos de criação de pauta, abertura de sessão, registro de voto e rejeições são registrados com SLF4J. Os logs utilizam IDs internos e não registram CPF.

## Versionamento da API

As rotas usam o prefixo `/api/v1`, permitindo a coexistência de versões do contrato da API.

## Documentação OpenAPI

A especificação OpenAPI é gerada com Springdoc e disponibilizada pelo Swagger UI. Endpoints são organizados por tags e documentam operações e respostas HTTP esperadas.

## Controle de versão

Os commits seguem mensagens curtas e descritivas, com prefixos como `feat`, `fix`, `test`, `docs` e `chore`. Cada commit agrupa uma alteração coesa e validada seguindo o padrão Conventional Commits.

## Testes automatizados

Regras de negócio dos services e da publicação de resultados são cobertas com testes unitários usando Mockito. Contratos HTTP, validações e respostas de erro dos controllers são cobertos com MockMvc, sem dependência de banco de dados. Testcontainers é utilizado para validar o fluxo principal contra PostgreSQL real, incluindo persistência e migrations.
