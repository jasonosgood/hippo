# Normal HTTP — Project Plan

## Overview

**normal-http** is a Java command-line tool that treats raw HTTP request/response
files as source code and generates a matching server and client. Zero runtime
dependencies; generated code runs on plain JDK 11+.

- **Repository:** `github.com/your-org/normal-http`
- **License:** Apache 2.0
- **Language:** Java 11+
- **Build:** GNU Make (no Maven/Gradle)
- **Status:** Pre-release / active development

---

## Milestones

### v0.1 — Working Skeleton  _(current)_

Goal: end-to-end pipeline works for simple cases; nothing is hardened.

- [x] Project layout and `CLAUDE.md`
- [x] `HttpExampleParser` — parses raw HTTP files
- [x] `ServerGenerator` — emits `GeneratedServer.java`
- [x] `ClientGenerator` — emits `GeneratedClient.java`
- [x] `Main.java` CLI (`generate` subcommand)
- [x] `Makefile` (`compile`, `jar`, `gen`, `test`)
- [x] Three starter `.http` example files
- [x] Verify end-to-end: `make test` passes green

---

### v0.2 — Parser Hardening & Example Coverage

Goal: parser handles real-world HTTP quirks without crashing; examples
exercise the full range of shapes the codegen has to handle.

- [ ] Petstore example set under `examples/petstore/` — GET/POST/PUT/DELETE,
      path params (`/pets/{id}`), list + detail + error responses,
      Authorization header
- [ ] Multi-example files (separator `---`)
- [ ] Headers with folded values (RFC 7230 §3.2.6)
- [ ] `\r\n` line endings (Windows / curl output)
- [ ] Empty body vs. absent body distinction
- [ ] Query strings in the request path (`/users?page=2`)
- [ ] Comments in `.http` files (`# this line is ignored`)
- [ ] External body files (`@./payloads/create_pet.json`) — keep large or
      binary bodies out of the `.http` file
- [ ] Parser unit tests (plain `assert` or JUnit 4, no test framework dep)
- [ ] Error messages include filename + line number

---

### v0.3 — Codegen Quality

Goal: generated code is clean, compiles cleanly, handles edge cases.

**Architecture:**

- [ ] Template-based codegen — replace `StringBuilder.append(...)` chains in
      `ServerGenerator` / `ClientGenerator` with a small in-house template
      helper (Java text blocks + simple `{{name}}` substitution; no external
      dep). Templates live in `src/normalhttp/templates/` as `.java.tmpl`.
- [ ] Parameterize target package: `--package com.example.api` puts a
      `package` declaration on every generated file and matches the
      directory layout under `generated/src/`. Default: no package
      (current behavior, for back-compat in v0.x).
- [ ] URL router — a small generated `Router` class that owns
      `(method, pathPattern) → handler` dispatch, path-param extraction,
      and `405 Method Not Allowed` with `Allow:` header. The per-path
      handler methods stay, but `createContext` calls go through the
      router instead of one context per literal path.
- [ ] Codegen golden tests — snapshot expected output of generators for
      `examples/petstore/` under `tests/golden/`; CI fails if drift.

**Correctness:**

- [ ] Path parameters: `/users/{id}` → extracted and passed to handler
- [ ] Response header passthrough (all headers, not just Content-Type)
- [ ] Correct `Content-Length` for binary-safe bodies
- [ ] Multi-example dispatch within the same `(method, path)` — match on
      request body or headers to pick the right response (e.g. valid
      payload → 201, malformed → 400)
- [ ] Preserve response status text (parser already captures it; emit it
      via `Headers` workaround — JDK HttpServer doesn't expose it directly)
- [ ] Generated code passes `javac -Xlint:all` without warnings
- [ ] Generated server: graceful shutdown on SIGTERM
- [ ] Generated client: clearer assertion diffs on body mismatch
      (show first differing line, not the full payload)

---

### v0.4 — CLI Completeness

Goal: the tool is usable from a shell without reading source.

- [ ] `normalhttp help` — prints usage
- [ ] `normalhttp generate` — current behavior
- [ ] `normalhttp validate --examples <dir>` — parse-only, no codegen; exits 0/1
- [ ] `normalhttp run --examples <dir> --port <n>` — generate + compile + start server in one step
- [ ] Read a single `.http` file from stdin (`normalhttp generate -`)
- [ ] `--verbose` / `--quiet` flags
- [ ] Exit codes documented in `README.md`
- [ ] Shell completion script (bash + zsh)

---

### v0.5 — Distribution

Goal: someone can install and run this without cloning the repo.

- [ ] Fat JAR (no-dependency single-file distribution)
- [ ] `install` Makefile target → copies jar + wrapper script to `~/.local/bin`
- [ ] Homebrew formula (`Formula/normal-http.rb`)
- [ ] GitHub Actions CI: build + test on ubuntu-latest / macos-latest / windows-latest
- [ ] GitHub Actions release: tag `v*` → publish JAR to GitHub Releases
- [ ] `CHANGELOG.md` seeded with v0.1–v0.5 entries

---

### v1.0 — Stable Release

Goal: API and file format are frozen; safe for others to build on.

- [ ] File format specification in `SPEC.md` (versioned)
- [ ] Full `README.md` with install, quickstart, format reference
- [ ] `CONTRIBUTING.md` — how to file issues, submit patches
- [ ] All public-facing behavior covered by integration tests
- [ ] No known parser crashes on any valid HTTP/1.1 message
- [ ] Semantic versioning commitment documented

---

## Backlog (post-1.0, no commitment)

| Item | Notes |
|------|-------|
| HTTP/2 framing support | Low value; HTTP/1.1 covers 95% of use cases |
| JSON body normalization in client assertions | Useful but scope-creep risk |
| OpenAPI → `.http` importer | Nice onramp for existing APIs |
| `.http` file formatter / linter | `normalhttp fmt` — round-trip parse → emit |
| JSON-aware body matching | optional `--json` flag; current exact-string match stays default |
| Pluggable codegen targets | Go server, Python client, etc. — templates already exist by v0.3 |
| Watch mode (`--watch`) | Re-gen on file change |
| Native binary via GraalVM native-image | Faster startup; separate release artifact |
| VS Code extension: syntax highlighting for `.http` | Third-party contribution welcome |
| Maven/Gradle plugin | Only if there is real demand |

---

## Versioning Policy

`MAJOR.MINOR.PATCH` per [Semantic Versioning 2.0](https://semver.org/).

- PATCH: bug fixes, no behaviour change.
- MINOR: new features, backwards-compatible.
- MAJOR: breaking change to CLI flags or `.http` file format.

The `.http` file format is **not** considered stable until v1.0.

---

## Issue Labels

| Label | Meaning |
|-------|---------|
| `bug` | Something is broken |
| `enhancement` | New feature or improvement |
| `parser` | Related to `.http` file parsing |
| `codegen` | Related to generated Java output |
| `cli` | Related to the `Main` entry point / flags |
| `infra` | Build, CI, distribution |
| `good first issue` | Small, well-scoped, mentor available |
| `help wanted` | Needs a contributor |

---

## Contributing

1. Fork → feature branch → PR against `main`.
2. All new behaviour must be exercised by an `.http` example file under `examples/`.
3. Run `make test` locally before opening a PR; CI must be green.
4. Keep generated code readable — it is a first-class output of this project.
5. No new runtime dependencies without a discussion issue first.
