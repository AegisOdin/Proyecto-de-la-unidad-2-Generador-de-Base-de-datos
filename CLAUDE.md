# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A full-stack database schema generator. Users write code in a custom high-level language; the backend compiles it via an ANTLR3 grammar into PostgreSQL DDL. The frontend provides a Monaco-editor IDE for writing and previewing the output.

## Architecture

Two independent services that must run simultaneously:

- **`backend/`** — Spring Boot 3 (Java 17) REST API. Exposes `POST /api/compilar`. The ANTLR3 grammar (`T.g`) is compiled by the Maven plugin at build time into `TLexer.java` and `TParser.java` under `target/`. The service wires `TLexer → CommonTokenStream → TParser` and calls `parser.inicio()` to run the full parse.
- **`frontend/`** — Nuxt 3 (Vue 3, SSR disabled) SPA. Hardcoded to call `http://localhost:8080/api/compilar`. The `useCompilador` composable owns all API state; the single page (`pages/index.vue`) only handles UI.

## Custom Language Grammar

The grammar (`backend/src/main/antlr3/com/compilador/parser/T.g`) defines a fixed structure:

```
crear <db_name>
usar <db_name>
tabla <table_name>
inicio
  <field_name> (letras | numeros | fecha | <other_table_name>)
  ...
fin
...
cerrar
```

- `letras` → `VARCHAR(300)`, `numeros` → `INTEGER`, `fecha` → `DATE`
- Referencing another table name as a field type creates a foreign key (`INTEGER` + `FOREIGN KEY` constraint)
- Each table automatically gets a `<table_name>_key SERIAL PRIMARY KEY` column

## Commands

### Backend
```bash
cd backend
./mvnw spring-boot:run          # Run dev server (port 8080)
./mvnw package                  # Build JAR (also regenerates ANTLR parser)
./mvnw package -DskipTests      # Build without tests
```

ANTLR source files are generated automatically during `mvn compile`/`package` — do not edit `TLexer.java` or `TParser.java` in `target/` directly; edit `T.g` instead.

### Frontend
```bash
cd frontend
npm install
npm run dev       # Dev server (default port 3000)
npm run build     # Production build
npm run generate  # Static generation
npm run preview   # Preview production build
```

## Key Files

| File | Purpose |
|------|---------|
| `backend/src/main/antlr3/com/compilador/parser/T.g` | Grammar definition — the core of the compiler |
| `backend/src/main/java/com/compilador/service/CompiladorService.java` | Instantiates lexer/parser, collects errors |
| `frontend/composables/useCompilador.ts` | All API call logic and state management |
| `frontend/pages/index.vue` | Entire UI (Monaco editors + buttons + styles) |
| `frontend/types/index.ts` | Shared TypeScript types (`CompiladorResponse`, `Estado`) |

## CORS

`WebConfig.java` must allow `http://localhost:3000` for frontend ↔ backend communication in development.
