# CLAUDE.md — Normal HTTP

## Project Goal

Treat example HTTP request/response files as source code. Parse them and generate:
1. A **Java server** that handles those requests and returns those responses.
2. A **Java client** that sends those requests and validates those responses.

Everything is plain Java — no frameworks, no Spring, no Netty. Use `com.sun.net.httpserver.HttpServer` for the server and `java.net.HttpURLConnection` (or `java.net.http.HttpClient` if Java 11+) for the client.

---

## Project Structure

```
.
├── CLAUDE.md
├── PROJECT_PLAN.md              # Project spec
├── README.md
├── LICENSE
├── examples/                    # Input: raw HTTP example files
│   ├── get_user.http            # One file per endpoint
│   ├── get_user_not_found.http
│   └── create_item.http
├── src/
│   └── normalhttp/
│       ├── Main.java                  # CLI entry point
│       ├── HttpExampleParser.java     # Parses raw HTTP files (incl. model types)
│       ├── ServerGenerator.java       # Emits server Java source
│       └── ClientGenerator.java       # Emits client Java source
├── generated/
│   ├── src/                     # Output: generated Java source files
│   │   ├── GeneratedServer.java
│   │   └── GeneratedClient.java
│   └── out/                     # Compiled .class files from generated/src/
└── Makefile
```

All `normalhttp` classes live flat under `src/normalhttp/` — no `parser/`, `model/`, or `codegen/` subpackages. The Makefile compiles everything under `src/` into `out/` and packages `normalhttp.jar` with `normalhttp.Main` as the entry point.

---

## HTTP Example File Format

One file per endpoint. Filename becomes the handler name. Format is raw HTTP:

```
### REQUEST
GET /users/42 HTTP/1.1
Host: localhost:8080
Accept: application/json

### RESPONSE
HTTP/1.1 200 OK
Content-Type: application/json

{"id": 42, "name": "Alice"}
```

Multiple examples in one file are separated by `---`.

---

## How It Works

1. **Parse** — `HttpExampleParser` reads each `.http` file in `examples/` and produces a list of `HttpExample` objects (method, path, headers, body for both request and response).
2. **Generate Server** — `ServerGenerator` emits a single `GeneratedServer.java` with one handler per unique `(method, path)` pair. Each handler returns the exact status, headers, and body from the example.
3. **Generate Client** — `ClientGenerator` emits `GeneratedClient.java` with one method per example. Each method fires the request and asserts the response matches.
4. **Compile & Run** — The generated files are plain Java with no dependencies. Compile with `javac`, run with `java`.

---

## CLI Usage

```bash
# Generate server and client from examples/
java -jar normalhttp.jar generate --examples examples/ --out generated/src/

# Run the generated server (port default 8080)
javac generated/src/GeneratedServer.java && java -cp generated/src GeneratedServer

# Run the generated client against a live server
javac generated/src/GeneratedClient.java && java -cp generated/src GeneratedClient http://localhost:8080
```

---

## Build

```bash
make        # compiles the tool (normalhttp.jar)
make gen    # runs codegen against examples/
make test   # starts server, runs client, checks exit code
```

---

## Coding Rules

- **No external dependencies.** stdlib only. No Maven/Gradle (use a plain `Makefile`).
- **No reflection, no annotations.** Generated code is explicit and readable.
- **One class per file** in generated output so it compiles with a single `javac *.java`.
- **Fail fast.** Parser throws `IllegalArgumentException` with the filename and line number on malformed input.
- **Generated code must be human-readable.** It is the primary output of this tool.
- Body matching in the client uses exact string equality after trimming. JSON normalization is out of scope for now.

---

## Key Design Decisions

| Decision | Choice | Reason |
|---|---|---|
| Language | Java (stdlib only) | Matches spec; no dep hell |
| Server runtime | `com.sun.net.httpserver` | Built into JDK, zero config |
| Client runtime | `java.net.http.HttpClient` (Java 11+) | Clean API, no deps |
| Build tool | `Makefile` | No framework overhead |
| Input format | Raw HTTP text files | Human-readable, no schema needed |

---

## Example `.http` File (`examples/create_item.http`)

```
### REQUEST
POST /items HTTP/1.1
Content-Type: application/json

{"name": "widget", "price": 9.99}

### RESPONSE
HTTP/1.1 201 Created
Content-Type: application/json

{"id": 1, "name": "widget", "price": 9.99}
```

---

## Next Steps (in order)

1. Implement `HttpExampleParser` and model classes.
2. Implement `ServerGenerator`.
3. Implement `ClientGenerator`.
4. Wire up `Main.java` CLI (`generate` subcommand).
5. Add `Makefile` targets: `compile`, `gen`, `test`.
6. Write 2–3 example `.http` files covering GET, POST, and a 404.
