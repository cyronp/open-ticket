# open-ticket

Spring Boot project copied from [CaioSouza07/helpdesk-univille](https://github.com/CaioSouza07/helpdesk-univille), commit `cc0427af543251b64cf69aa5e6651537069a13c2`, and renamed to `open-ticket`.

All tracked source files, dependencies, Maven wrapper files, and endpoint behavior from the original repository are preserved. The Maven artifact and application name are `open-ticket`; the Java package is `com.openticket` and the entry point is `OpenTicketApplication`.

## Requirements

- JDK 21 or later, with `JAVA_HOME` pointing to the JDK directory.
- Internet access on the first build to download Maven and dependencies.

## Run

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```sh
sh ./mvnw spring-boot:run
```

The application runs at `http://localhost:8080` and exposes:

- `GET /ola`: returns `Spring boot, funcionando`.
- `GET /name`: returns the original author's name and the current date in `dd/MM/yyyy` format.

## Test and build

```powershell
.\mvnw.cmd verify
```

On macOS / Linux, use `sh ./mvnw verify`.

The executable JAR is generated at `target/open-ticket-0.0.1-SNAPSHOT.jar`:

```sh
java -jar target/open-ticket-0.0.1-SNAPSHOT.jar
```
