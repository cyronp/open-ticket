# Open Ticket

Sistema de chamados (help desk) desenvolvido com Spring Boot. Permite cadastrar clientes, técnicos e categorias, abrir e acompanhar chamados, atribuí-los a técnicos, alterar o status e registrar comentários. O projeto oferece uma API REST e também telas web (Thymeleaf) com um painel de visão geral.

## Tecnologias

- Java 21
- Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, Thymeleaf)
- Banco de dados H2 em memória
- Maven (via Maven Wrapper, não é necessário instalar o Maven)

## Pré-requisitos

- **JDK 21** ou superior instalado
- A variável de ambiente `JAVA_HOME` apontando para o JDK (recomendado)

Para conferir a versão do Java instalada:

```bash
java -version
```

## Como executar

1. Clone o repositório e entre na pasta do projeto:

   ```bash
   git clone https://github.com/cyronp/open-ticket.git
   cd open-ticket
   ```

2. Inicie a aplicação usando o Maven Wrapper:

   **Linux / macOS / Git Bash**

   ```bash
   ./mvnw spring-boot:run
   ```

   **Windows (PowerShell ou CMD)**

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

3. Aguarde a mensagem de que a aplicação foi iniciada e acesse no navegador:

   - Interface web: <http://localhost:8080/app>
   - Console do banco H2: <http://localhost:8080/h2-console>

Para encerrar a aplicação, pressione `Ctrl + C` no terminal.

### Executando pelo JAR

Também é possível gerar o pacote e executá-lo diretamente:

```bash
./mvnw clean package
java -jar target/open-ticket-0.0.1-SNAPSHOT.jar
```

(No Windows, use `.\mvnw.cmd` no lugar de `./mvnw`.)

## Executando os testes

```bash
./mvnw test
```

## Acessos úteis

| Recurso | URL |
| --- | --- |
| Painel (visão geral dos chamados) | <http://localhost:8080/app> |
| Chamados (telas) | <http://localhost:8080/app/chamados> |
| Clientes (telas) | <http://localhost:8080/app/clientes> |
| Técnicos (telas) | <http://localhost:8080/app/tecnicos> |
| Categorias (telas) | <http://localhost:8080/app/categorias> |
| API REST | <http://localhost:8080/chamados>, `/clientes`, `/tecnicos`, `/categorias` |
| Console H2 | <http://localhost:8080/h2-console> |

### Console do H2

Na tela de login do console, use:

- **JDBC URL:** `jdbc:h2:mem:openticketdb`
- **User Name:** `sa`
- **Password:** (deixe em branco)

> O banco é **em memória**: os dados são recriados a cada inicialização e perdidos ao encerrar a aplicação.

## Solução de problemas

- **Porta 8080 já em uso:** inicie em outra porta, por exemplo
  `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`
- **Erro de versão do Java:** verifique se `java -version` indica a versão 21 ou superior e se o `JAVA_HOME` aponta para o JDK correto.
- **Permissão negada ao executar `./mvnw` (Linux/macOS):** execute `chmod +x mvnw`.
