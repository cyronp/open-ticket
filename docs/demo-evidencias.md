# Evidências da demonstração: outbox, publicador e consumidor idempotente

Todos os dados usados são fictícios (e-mail `@example.com`).
As saídas abaixo foram copiadas da execução real, sem edição. O log completo da aplicação está em [`demo-log.txt`](demo-log.txt).

## 1. Ambiente

| Item | Valor |
|---|---|
| Sistema operacional | Microsoft Windows 10 Education 10.0.19045 build 19045 |
| Data/hora do teste | 2026-11-25, entre ~20:12 e 20:20 (UTC-03:00) |
| Spring Boot | 4.1.1 (`pom.xml`) |
| Java no `pom.xml` | `<java.version>21</java.version>` |
| Maven (wrapper) | Apache Maven 3.9.16 |

Saída de `java -version` (coletada às 2026-11-25 20:19:49 -0300):

```
java version "23.0.2" 2025-01-21
Java(TM) SE Runtime Environment (build 23.0.2+7-58)
Java HotSpot(TM) 64-Bit Server VM (build 23.0.2+7-58, mixed mode, sharing)
```

Saída relevante de `.\mvnw.cmd -v`:

```
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Java version: 23.0.2, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk-23
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
```

> Observação: o projeto declara Java 21, mas o JDK instalado e usado tanto nos testes quanto na execução foi o **23.0.2**. O log da aplicação confirma: `Starting OpenTicketApplication using Java 23.0.2`.

## 2. Resultado do `.\mvnw.cmd clean test`

Executado por volta de 20:13. Resultado: **BUILD SUCCESS**, com **88 testes executados, 0 falhas, 0 erros e 0 ignorados**.

O comando rodou com `-q`, por isso o resumo `Tests run:` do Maven não apareceu na saída capturada. Os números abaixo foram tirados dos relatórios gerados em `target/surefire-reports/*.xml`:

| Classe de teste | Testes | Falhas | Erros | Ignorados | Tempo (s) |
|---|---:|---:|---:|---:|---:|
| com.openticket.OpenTicketApplicationTests | 1 | 0 | 0 | 0 | 12.073 |
| com.openticket.controller.CategoriaControllerTest | 7 | 0 | 0 | 0 | 14.947 |
| com.openticket.controller.ChamadoControllerTest | 10 | 0 | 0 | 0 | 3.642 |
| com.openticket.controller.ClienteControllerTest | 7 | 0 | 0 | 0 | 1.337 |
| com.openticket.controller.TecnicoControllerTest | 7 | 0 | 0 | 0 | 1.07 |
| com.openticket.repository.CategoriaRepositoryTest | 6 | 0 | 0 | 0 | 1.703 |
| com.openticket.repository.ChamadoRepositoryTest | 15 | 0 | 0 | 0 | 0.852 |
| com.openticket.repository.ClienteRepositoryTest | 6 | 0 | 0 | 0 | 0.211 |
| com.openticket.repository.ComentarioRepositoryTest | 5 | 0 | 0 | 0 | 0.302 |
| com.openticket.repository.TecnicoRepositoryTest | 6 | 0 | 0 | 0 | 0.21 |
| com.openticket.web.ChamadoViewControllerTest | 10 | 0 | 0 | 0 | 1.619 |
| com.openticket.web.ClienteViewControllerTest | 8 | 0 | 0 | 0 | 0.621 |
| **Total** | **88** | **0** | **0** | **0** | **38.587** |

Durante os testes aparecem no log avisos `WARN` como `NULL not allowed for column "TEXTO"`, `NULL not allowed for column "PERFIL"` e `Unique index or primary key violation ... 'joao@x.com'`. Eles vêm de testes de repositório que provocam essas violações de propósito, e todos esses testes passaram.

## 3. Arquivos alterados

**Nenhum arquivo foi alterado.** O projeto compilou e todos os testes passaram sem correções. Os únicos arquivos criados foram os desta pasta: `docs/demo-evidencias.md` e `docs/demo-log.txt`.

Sobre a execução da aplicação: a primeira tentativa de `.\mvnw.cmd spring-boot:run` falhou com:

```
APPLICATION FAILED TO START
Web server failed to start. Port 8080 was already in use.
[INFO] BUILD FAILURE
```

A porta 8080 estava ocupada por `wslrelay.exe` (PID 60324), ou seja, um serviço do WSL que não tem relação com este projeto, e ele não foi encerrado. Por isso a aplicação foi iniciada na porta **8081**, sem alterar código nem configuração. A porta foi trocada apenas por argumento de linha de comando:

```
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

O log dessa primeira tentativa foi sobrescrito pela segunda execução e não está em `demo-log.txt`.

Inicialização bem-sucedida (trecho do log):

```
2026-11-25T20:15:30.656-03:00  INFO 70744 --- [openticket] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat started on port 8081 (http) with context path '/'
2026-11-25T20:15:30.690-03:00  INFO 70744 --- [openticket] [           main] com.openticket.OpenTicketApplication     : Started OpenTicketApplication in 16.291 seconds (process running for 17.194)
```

## 4. Requisições HTTP (fluxo principal, em ordem)

Base: `http://localhost:8081`. Todas as requisições com corpo usaram `Content-Type: application/json`.

### 4.1 `POST /clientes` (~20:15:43)

Corpo enviado:
```json
{"nome":"Cliente Teste","email":"cliente.teste@example.com","telefone":"(11) 90000-0000"}
```
Status: **201**

Resposta:
```json
{"id":1,"nome":"Cliente Teste","email":"cliente.teste@example.com","telefone":"(11) 90000-0000"}
```

### 4.2 `POST /categorias` (~20:15:43)

Corpo enviado:
```json
{"nome":"Categoria Teste"}
```
Status: **201**

Resposta:
```json
{"id":1,"nome":"Categoria Teste","descricao":null}
```

### 4.3 `POST /chamados` (20:15:43, conforme `dataAbertura`)

Corpo enviado:
```json
{"titulo":"Chamado de teste","descricao":"Descricao ficticia para teste do outbox","prioridade":"MEDIA","clienteId":1,"categoriaId":1}
```
Status: **201**

Resposta:
```json
{"id":1,"titulo":"Chamado de teste","descricao":"Descricao ficticia para teste do outbox","dataAbertura":"2026-11-25T20:15:43.1634591","dataFechamento":null,"prioridade":"MEDIA","status":"ABERTO","cliente":{"id":1,"nome":"Cliente Teste","email":"cliente.teste@example.com","telefone":"(11) 90000-0000"},"categoria":{"id":1,"nome":"Categoria Teste","descricao":null},"tecnico":null}
```

### 4.4 `GET /demo/eventos` (feito ~15s depois do chamado)

Corpo enviado: nenhum.
Status: **200**

Resposta:
```json
[{"tipo":"chamado.aberto","chamadoId":1,"tentativas":1,"eventId":"5e95b0d5-68bf-404b-bfe3-1ea69a3589e8","status":"ENVIADO"}]
```

### 4.5 `POST /demo/eventos/5e95b0d5-68bf-404b-bfe3-1ea69a3589e8/reprocessar` (~20:15:59)

Corpo enviado: nenhum.
Status: **202**

Resposta: sem corpo.

### 4.6 `GET /demo/eventos` (depois do reprocessamento)

Corpo enviado: nenhum.
Status: **200**

Resposta:
```json
[{"tipo":"chamado.aberto","chamadoId":1,"tentativas":0,"eventId":"5e95b0d5-68bf-404b-bfe3-1ea69a3589e8","status":"ENVIADO"}]
```

> Observação: o campo `tentativas` conta apenas as **falhas**. Por isso ele mostra `1` depois de duas tentativas (uma falha e um sucesso). O `reprocessar()` zera o contador, por isso o valor depois é `0`.

## 5. Linhas de log `[PUBLICADOR]` e `[CONSUMIDOR]`, em ordem

As linhas foram copiadas literalmente de `demo-log.txt`. O `�` em "indispon�vel" e "j� processado" está gravado no próprio arquivo de log: a saída do console perdeu os acentos na codificação, e o texto foi mantido como saiu.

### 5.1 Fluxo principal (chamado #1)

Falha na 1ª tentativa, nova tentativa após o backoff (~5s), e-mail simulado e ENVIADO:

```
2026-11-25T20:15:43.407-03:00  INFO 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=5e95b0d5-68bf-404b-bfe3-1ea69a3589e8 tipo=chamado.aberto tentativa=1
2026-11-25T20:15:43.455-03:00  WARN 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=5e95b0d5-68bf-404b-bfe3-1ea69a3589e8 falhou (provedor de e-mail indispon�vel (simulado)). Nova tentativa em 2026-11-25T23:15:48.008823900Z
2026-11-25T20:15:49.552-03:00  INFO 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=5e95b0d5-68bf-404b-bfe3-1ea69a3589e8 tipo=chamado.aberto tentativa=2
2026-11-25T20:15:49.555-03:00  INFO 70744 --- [openticket] [   scheduling-1] c.o.evento.NotificadorEmailSimulado      : [CONSUMIDOR] eventId=5e95b0d5-68bf-404b-bfe3-1ea69a3589e8 -> e-mail enviado (simulado): chamado #1 aberto, link /app/chamados/1
2026-11-25T20:15:49.568-03:00  INFO 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=5e95b0d5-68bf-404b-bfe3-1ea69a3589e8 -> ENVIADO
```

### 5.2 Depois do `POST /demo/eventos/{eventId}/reprocessar`

O mesmo `eventId` é descartado pelo consumidor:

```
2026-11-25T20:15:59.630-03:00  INFO 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=5e95b0d5-68bf-404b-bfe3-1ea69a3589e8 tipo=chamado.aberto tentativa=1
2026-11-25T20:15:59.633-03:00  INFO 70744 --- [openticket] [   scheduling-1] c.o.evento.NotificadorEmailSimulado      : [CONSUMIDOR] eventId=5e95b0d5-68bf-404b-bfe3-1ea69a3589e8 j� processado -> DUPLICADO DESCARTADO
2026-11-25T20:15:59.634-03:00  INFO 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=5e95b0d5-68bf-404b-bfe3-1ea69a3589e8 -> ENVIADO
```

### 5.3 Eventos gerados pelo teste extra (seção 6)

```
2026-11-25T20:19:59.307-03:00  INFO 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=e248ffa8-6141-4a1f-97c9-b3718bcbf32d tipo=chamado.aberto tentativa=1
2026-11-25T20:19:59.312-03:00  WARN 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=e248ffa8-6141-4a1f-97c9-b3718bcbf32d falhou (provedor de e-mail indispon�vel (simulado)). Nova tentativa em 2026-11-25T23:20:04.855393200Z
2026-11-25T20:19:59.322-03:00  INFO 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=57c5ea54-77f7-4f96-af9d-fae5a95e325c tipo=chamado.aberto tentativa=1
2026-11-25T20:19:59.326-03:00  WARN 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=57c5ea54-77f7-4f96-af9d-fae5a95e325c falhou (provedor de e-mail indispon�vel (simulado)). Nova tentativa em 2026-11-25T23:20:05.011005700Z
2026-11-25T20:20:05.405-03:00  INFO 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=e248ffa8-6141-4a1f-97c9-b3718bcbf32d tipo=chamado.aberto tentativa=2
2026-11-25T20:20:05.411-03:00  INFO 70744 --- [openticket] [   scheduling-1] c.o.evento.NotificadorEmailSimulado      : [CONSUMIDOR] eventId=e248ffa8-6141-4a1f-97c9-b3718bcbf32d -> e-mail enviado (simulado): chamado #2 aberto, link /app/chamados/2
2026-11-25T20:20:05.420-03:00  INFO 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=e248ffa8-6141-4a1f-97c9-b3718bcbf32d -> ENVIADO
2026-11-25T20:20:05.432-03:00  INFO 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=57c5ea54-77f7-4f96-af9d-fae5a95e325c tipo=chamado.aberto tentativa=2
2026-11-25T20:20:05.438-03:00  INFO 70744 --- [openticket] [   scheduling-1] c.o.evento.NotificadorEmailSimulado      : [CONSUMIDOR] eventId=57c5ea54-77f7-4f96-af9d-fae5a95e325c -> e-mail enviado (simulado): chamado #3 aberto, link /app/chamados/3
2026-11-25T20:20:05.444-03:00  INFO 70744 --- [openticket] [   scheduling-1] com.openticket.evento.PublicadorOutbox   : [PUBLICADOR] eventId=57c5ea54-77f7-4f96-af9d-fae5a95e325c -> ENVIADO
```

## 6. Teste extra de duplicidade em `POST /chamados`

O mesmo corpo foi enviado duas vezes seguidas às 20:19:59, seguido de `GET /chamados`.

Corpo enviado (idêntico nas duas requisições):
```json
{"titulo":"Chamado de teste","descricao":"Descricao ficticia para teste do outbox","prioridade":"MEDIA","clienteId":1,"categoriaId":1}
```

### 6.1 `POST /chamados` (1º envio)

Status: **201**. ID gerado: **2**.

```json
{"id":2,"titulo":"Chamado de teste","descricao":"Descricao ficticia para teste do outbox","dataAbertura":"2026-11-25T20:19:59.1502076","dataFechamento":null,"prioridade":"MEDIA","status":"ABERTO","cliente":{"id":1,"nome":"Cliente Teste","email":"cliente.teste@example.com","telefone":"(11) 90000-0000"},"categoria":{"id":1,"nome":"Categoria Teste","descricao":null},"tecnico":null}
```

### 6.2 `POST /chamados` (2º envio, mesmo corpo)

Status: **201**. ID gerado: **3**.

```json
{"id":3,"titulo":"Chamado de teste","descricao":"Descricao ficticia para teste do outbox","dataAbertura":"2026-11-25T20:19:59.2648171","dataFechamento":null,"prioridade":"MEDIA","status":"ABERTO","cliente":{"id":1,"nome":"Cliente Teste","email":"cliente.teste@example.com","telefone":"(11) 90000-0000"},"categoria":{"id":1,"nome":"Categoria Teste","descricao":null},"tecnico":null}
```

### 6.3 `GET /chamados`

Corpo enviado: nenhum.
Status: **200**

```json
{"content":[{"id":3,"titulo":"Chamado de teste","descricao":"Descricao ficticia para teste do outbox","dataAbertura":"2026-11-25T20:19:59.264817","dataFechamento":null,"prioridade":"MEDIA","status":"ABERTO","cliente":{"id":1,"nome":"Cliente Teste","email":"cliente.teste@example.com","telefone":"(11) 90000-0000"},"categoria":{"id":1,"nome":"Categoria Teste","descricao":null},"tecnico":null},{"id":2,"titulo":"Chamado de teste","descricao":"Descricao ficticia para teste do outbox","dataAbertura":"2026-11-25T20:19:59.150208","dataFechamento":null,"prioridade":"MEDIA","status":"ABERTO","cliente":{"id":1,"nome":"Cliente Teste","email":"cliente.teste@example.com","telefone":"(11) 90000-0000"},"categoria":{"id":1,"nome":"Categoria Teste","descricao":null},"tecnico":null},{"id":1,"titulo":"Chamado de teste","descricao":"Descricao ficticia para teste do outbox","dataAbertura":"2026-11-25T20:15:43.163459","dataFechamento":null,"prioridade":"MEDIA","status":"ABERTO","cliente":{"id":1,"nome":"Cliente Teste","email":"cliente.teste@example.com","telefone":"(11) 90000-0000"},"categoria":{"id":1,"nome":"Categoria Teste","descricao":null},"tecnico":null}],"page":{"size":10,"number":0,"totalElements":3,"totalPages":1}}
```

### 6.4 `GET /demo/eventos` (20:20:09, depois que os eventos dos chamados 2 e 3 foram publicados)

Status: **200**

```json
[{"tipo":"chamado.aberto","chamadoId":1,"tentativas":0,"eventId":"5e95b0d5-68bf-404b-bfe3-1ea69a3589e8","status":"ENVIADO"},{"tipo":"chamado.aberto","chamadoId":2,"tentativas":1,"eventId":"e248ffa8-6141-4a1f-97c9-b3718bcbf32d","status":"ENVIADO"},{"tipo":"chamado.aberto","chamadoId":3,"tentativas":1,"eventId":"57c5ea54-77f7-4f96-af9d-fae5a95e325c","status":"ENVIADO"}]
```

### 6.5 Resultado observado

Os dois envios idênticos criaram **dois chamados distintos (IDs 2 e 3)**, e `GET /chamados` passou a listar 3 chamados (`totalElements: 3`).

Cada chamado gerou um evento próprio no outbox, com `eventId` diferente: `e248ffa8-...` para o chamado 2 e `57c5ea54-...` para o chamado 3. Por isso o consumidor enviou **dois e-mails (simulados)**: para ele, são eventos diferentes.

A idempotência do consumidor protege contra a **reentrega do mesmo evento** (seção 5.2). Ela **não** protege contra uma requisição HTTP repetida pelo cliente: `POST /chamados` não tem chave de idempotência nem verificação de duplicidade.

## Arquivo de log

`docs/demo-log.txt` é uma cópia byte a byte (UTF-8 com BOM, finais de linha CRLF) da saída de `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"`, tirada às 2026-11-25 20:20:27 -0300, com 307 linhas. A aplicação continuava rodando nesse momento. A maior parte das linhas são selects do Hibernate (`spring.jpa.show-sql=true`) que o publicador dispara a cada 2 segundos.

O log contém caminhos locais da máquina, como o diretório do projeto e do usuário, nas linhas de inicialização do Maven e do Spring.
