# Generador de Base de Datos

Herramienta full-stack que compila un lenguaje de alto nivel propio hacia DDL de PostgreSQL. El editor corre en el navegador con resaltado de sintaxis; el backend procesa el código con un compilador basado en ANTLR3.

## Requisitos previos

| Herramienta | Version minima |
|-------------|----------------|
| Java JDK    | 17+            |
| Maven       | 3.6+           |
| Node.js     | 18+            |
| npm         | 9+             |

No necesitas instalar PostgreSQL para usar el generador — solo produce el SQL, no lo ejecuta.

## Estructura del repositorio

```
.
├── backend/    # Spring Boot 3 — API REST en el puerto 8080
└── frontend/   # Nuxt 3 SPA — editor en el puerto 3000
```

## Setup rapido

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

La primera vez Maven descarga dependencias y genera los archivos del parser ANTLR3 automaticamente. El servidor queda disponible en `http://localhost:8080`.

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

La app queda disponible en `http://localhost:3000`.

Ambos servicios deben estar corriendo al mismo tiempo.

## Uso

1. Abre `http://localhost:3000` en el navegador.
2. Escribe tu esquema en el editor izquierdo usando el lenguaje propio (ver sintaxis abajo).
3. Haz clic en **Compilar**.
4. El panel derecho muestra el SQL generado o los errores de compilacion.

## Sintaxis del lenguaje

```
crear <nombre_bd>
usar <nombre_bd>

tabla <nombre_tabla>
inicio
  <campo> letras        -- VARCHAR(300)
  <campo> numeros       -- INTEGER
  <campo> fecha         -- DATE
  <campo> <otra_tabla>  -- INTEGER + FOREIGN KEY hacia <otra_tabla>
fin

...mas tablas...

cerrar
```

Cada tabla recibe automaticamente una columna `<nombre_tabla>_key SERIAL PRIMARY KEY`.

### Ejemplo

```
crear tienda
usar tienda

tabla cliente
inicio
  nombre letras
  edad   numeros
fin

tabla pedido
inicio
  fecha    fecha
  cliente  cliente
fin

cerrar
```

Genera:

```sql
CREATE DATABASE tienda;

CREATE TABLE cliente (
  cliente_key SERIAL PRIMARY KEY,
  nombre VARCHAR(300),
  edad INTEGER
);

CREATE TABLE pedido (
  pedido_key SERIAL PRIMARY KEY,
  fecha DATE,
  cliente INTEGER,
  FOREIGN KEY (cliente) REFERENCES cliente(cliente_key)
);
```

## Endpoints del backend

| Metodo | Ruta            | Descripcion                        |
|--------|-----------------|------------------------------------|
| POST   | `/api/compilar` | Recibe `{ "codigo": "..." }`, devuelve SQL generado y lista de errores |

## Archivos clave

| Archivo | Rol |
|---------|-----|
| `backend/src/main/antlr3/com/compilador/parser/T.g` | Gramatica ANTLR3 — nucleo del compilador |
| `backend/src/main/java/com/compilador/service/CompiladorService.java` | Instancia lexer/parser y recolecta errores |
| `frontend/composables/useCompilador.ts` | Logica de llamada a la API y estado |
| `frontend/pages/index.vue` | UI completa (editores Monaco + botones) |

## Notas de desarrollo

- **No edites** `TLexer.java` ni `TParser.java` en `target/` — se regeneran en cada build desde `T.g`.
- Para modificar la gramatica edita `T.g` y ejecuta `mvn package` para ver los cambios.
- CORS esta configurado para permitir `http://localhost:3000` → `http://localhost:8080`.
