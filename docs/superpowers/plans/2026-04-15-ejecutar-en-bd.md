# Ejecutar en BD — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Agregar un botón "Ejecutar en BD" que abre un modal de credenciales y usa el backend para crear la base de datos en PostgreSQL local y ejecutar el DDL generado.

**Architecture:** Un nuevo endpoint `POST /api/ejecutar` en Spring Boot recibe usuario, contraseña, nombre de BD y SQL; conecta a PostgreSQL via JDBC en dos pasos (crea la BD, luego ejecuta los CREATE TABLE). El frontend muestra un modal con el formulario y un toast con el resultado.

**Tech Stack:** Java 17, Spring Boot 3.2.5, PostgreSQL JDBC Driver, JUnit 5 + MockMvc (tests), Nuxt 3 / Vue 3 (frontend).

---

## Mapa de archivos

| Acción | Archivo |
|--------|---------|
| Modificar | `backend/pom.xml` |
| Crear | `backend/src/main/java/com/compilador/dto/EjecutarRequest.java` |
| Crear | `backend/src/main/java/com/compilador/dto/EjecutarResponse.java` |
| Crear | `backend/src/main/java/com/compilador/service/EjecutarService.java` |
| Crear | `backend/src/main/java/com/compilador/controller/EjecutarController.java` |
| Crear | `backend/src/test/java/com/compilador/service/EjecutarServiceTest.java` |
| Crear | `backend/src/test/java/com/compilador/controller/EjecutarControllerTest.java` |
| Modificar | `frontend/types/index.ts` |
| Modificar | `frontend/composables/useCompilador.ts` |
| Modificar | `frontend/pages/index.vue` |

---

## Task 1: Agregar dependencias al pom.xml

**Files:**
- Modify: `backend/pom.xml`

El proyecto no tiene el driver JDBC de PostgreSQL ni las dependencias de testing. Se agregan ambas.

- [ ] **Step 1: Abrir `backend/pom.xml` y agregar las dos dependencias dentro de `<dependencies>`**

Agregar después de la dependencia de `antlr-runtime`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Verificar que Maven descarga las dependencias**

```bash
cd backend
./mvnw dependency:resolve -q
```

Resultado esperado: sin errores, termina con `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add backend/pom.xml
git commit -m "feat: add postgresql jdbc driver and test dependencies"
```

---

## Task 2: Crear DTOs EjecutarRequest y EjecutarResponse

**Files:**
- Create: `backend/src/main/java/com/compilador/dto/EjecutarRequest.java`
- Create: `backend/src/main/java/com/compilador/dto/EjecutarResponse.java`

Misma convención que los DTOs existentes: POJO con getters y setters, sin Lombok.

- [ ] **Step 1: Crear `EjecutarRequest.java`**

```java
package com.compilador.dto;

public class EjecutarRequest {
    private String usuario;
    private String contrasena;
    private String baseDatos;
    private String sql;

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getBaseDatos() { return baseDatos; }
    public void setBaseDatos(String baseDatos) { this.baseDatos = baseDatos; }

    public String getSql() { return sql; }
    public void setSql(String sql) { this.sql = sql; }
}
```

- [ ] **Step 2: Crear `EjecutarResponse.java`**

```java
package com.compilador.dto;

public class EjecutarResponse {
    private boolean exito;
    private String mensaje;

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
```

- [ ] **Step 3: Compilar para verificar que no hay errores de sintaxis**

```bash
cd backend
./mvnw compile -q
```

Resultado esperado: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/compilador/dto/EjecutarRequest.java \
        backend/src/main/java/com/compilador/dto/EjecutarResponse.java
git commit -m "feat: add EjecutarRequest and EjecutarResponse DTOs"
```

---

## Task 3: Crear EjecutarService (TDD)

**Files:**
- Create: `backend/src/test/java/com/compilador/service/EjecutarServiceTest.java`
- Create: `backend/src/main/java/com/compilador/service/EjecutarService.java`

El servicio recibe el SQL del parser (que incluye `CREATE DATABASE x;` y `\c x;`). Primero crea la BD conectándose a la BD de mantenimiento `postgres`, luego filtra el SQL para ejecutar solo los `CREATE TABLE` en la BD nueva.

- [ ] **Step 1: Crear el directorio de tests y escribir el test que falla**

Crear `backend/src/test/java/com/compilador/service/EjecutarServiceTest.java`:

```java
package com.compilador.service;

import com.compilador.dto.EjecutarRequest;
import com.compilador.dto.EjecutarResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EjecutarServiceTest {

    @Test
    void ejecutar_retornaFalso_cuandoLasCredencialesSonInvalidas() {
        EjecutarService service = new EjecutarService();

        EjecutarRequest request = new EjecutarRequest();
        request.setUsuario("usuario_inexistente");
        request.setContrasena("contrasena_incorrecta");
        request.setBaseDatos("bd_inexistente");
        request.setSql("CREATE TABLE test (id INTEGER);");

        EjecutarResponse response = service.ejecutar(request);

        assertFalse(response.isExito());
        assertNotNull(response.getMensaje());
        assertFalse(response.getMensaje().isEmpty());
    }
}
```

- [ ] **Step 2: Verificar que el test falla (la clase no existe aún)**

```bash
cd backend
./mvnw test -pl . -Dtest=EjecutarServiceTest -q 2>&1 | tail -20
```

Resultado esperado: error de compilación — `EjecutarService` no existe.

- [ ] **Step 3: Crear `EjecutarService.java` con implementación completa**

```java
package com.compilador.service;

import com.compilador.dto.EjecutarRequest;
import com.compilador.dto.EjecutarResponse;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class EjecutarService {

    public EjecutarResponse ejecutar(EjecutarRequest request) {
        EjecutarResponse response = new EjecutarResponse();
        String usuario = request.getUsuario();
        String contrasena = request.getContrasena();
        String baseDatos = request.getBaseDatos();
        String sql = request.getSql();

        // Paso 1: Crear la BD conectándose a la BD de mantenimiento 'postgres'
        String urlPostgres = "jdbc:postgresql://localhost:5432/postgres";
        try (Connection conn = DriverManager.getConnection(urlPostgres, usuario, contrasena);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE " + baseDatos);
        } catch (SQLException e) {
            // Si la BD ya existe, no es un error — continuamos
            if (!e.getMessage().contains("already exists")) {
                response.setExito(false);
                response.setMensaje(e.getMessage());
                return response;
            }
        }

        // Paso 2: Conectarse a la BD y ejecutar los CREATE TABLE
        // El SQL del parser incluye "CREATE DATABASE x;" y "\c x;" que hay que ignorar
        String urlBd = "jdbc:postgresql://localhost:5432/" + baseDatos;
        try (Connection conn = DriverManager.getConnection(urlBd, usuario, contrasena);
             Statement stmt = conn.createStatement()) {

            for (String sentencia : sql.split(";")) {
                String trimmed = sentencia.trim();
                if (trimmed.isEmpty()) continue;
                if (trimmed.startsWith("\\")) continue;
                if (trimmed.toUpperCase().startsWith("CREATE DATABASE")) continue;
                stmt.execute(trimmed);
            }

            response.setExito(true);
            response.setMensaje("Base de datos creada exitosamente");
        } catch (SQLException e) {
            response.setExito(false);
            response.setMensaje(e.getMessage());
        }

        return response;
    }
}
```

- [ ] **Step 4: Correr el test para verificar que pasa**

```bash
cd backend
./mvnw test -Dtest=EjecutarServiceTest -q
```

Resultado esperado: `Tests run: 1, Failures: 0, Errors: 0` — el test pasa porque con credenciales inválidas el driver PostgreSQL lanza `SQLException` y el servicio devuelve `exito=false` con un mensaje no vacío.

- [ ] **Step 5: Commit**

```bash
git add backend/src/test/java/com/compilador/service/EjecutarServiceTest.java \
        backend/src/main/java/com/compilador/service/EjecutarService.java
git commit -m "feat: add EjecutarService with JDBC PostgreSQL execution"
```

---

## Task 4: Crear EjecutarController (TDD)

**Files:**
- Create: `backend/src/test/java/com/compilador/controller/EjecutarControllerTest.java`
- Create: `backend/src/main/java/com/compilador/controller/EjecutarController.java`

- [ ] **Step 1: Escribir el test del controlador**

Crear `backend/src/test/java/com/compilador/controller/EjecutarControllerTest.java`:

```java
package com.compilador.controller;

import com.compilador.dto.EjecutarRequest;
import com.compilador.dto.EjecutarResponse;
import com.compilador.service.EjecutarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EjecutarController.class)
class EjecutarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EjecutarService ejecutarService;

    @Test
    void ejecutar_retornaExitoVerdadero_cuandoElServicioTieneExito() throws Exception {
        EjecutarResponse respuesta = new EjecutarResponse();
        respuesta.setExito(true);
        respuesta.setMensaje("Base de datos creada exitosamente");

        when(ejecutarService.ejecutar(any(EjecutarRequest.class))).thenReturn(respuesta);

        mockMvc.perform(post("/api/ejecutar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"postgres\",\"contrasena\":\"1234\",\"baseDatos\":\"empresa\",\"sql\":\"CREATE TABLE test (id INTEGER);\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.mensaje").value("Base de datos creada exitosamente"));
    }
}
```

- [ ] **Step 2: Verificar que el test falla (el controlador no existe)**

```bash
cd backend
./mvnw test -Dtest=EjecutarControllerTest -q 2>&1 | tail -20
```

Resultado esperado: error de compilación — `EjecutarController` no existe.

- [ ] **Step 3: Crear `EjecutarController.java`**

```java
package com.compilador.controller;

import com.compilador.dto.EjecutarRequest;
import com.compilador.dto.EjecutarResponse;
import com.compilador.service.EjecutarService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EjecutarController {

    private final EjecutarService ejecutarService;

    public EjecutarController(EjecutarService ejecutarService) {
        this.ejecutarService = ejecutarService;
    }

    @PostMapping("/ejecutar")
    public EjecutarResponse ejecutar(@RequestBody EjecutarRequest request) {
        return ejecutarService.ejecutar(request);
    }
}
```

- [ ] **Step 4: Correr todos los tests**

```bash
cd backend
./mvnw test -q
```

Resultado esperado: todos los tests pasan (`Tests run: 2, Failures: 0, Errors: 0`).

- [ ] **Step 5: Commit**

```bash
git add backend/src/test/java/com/compilador/controller/EjecutarControllerTest.java \
        backend/src/main/java/com/compilador/controller/EjecutarController.java
git commit -m "feat: add EjecutarController exposing POST /api/ejecutar"
```

---

## Task 5: Agregar tipo EjecutarResponse y función ejecutar() al frontend

**Files:**
- Modify: `frontend/types/index.ts`
- Modify: `frontend/composables/useCompilador.ts`

- [ ] **Step 1: Agregar `EjecutarResponse` a `frontend/types/index.ts`**

Agregar al final del archivo:

```ts
export interface EjecutarResponse {
  exito: boolean
  mensaje: string
}
```

El archivo completo queda:

```ts
export interface CompiladorResponse {
  errores: string[]
  sql: string
  estructura: string
}

export type Estado = 'idle' | 'loading' | 'error' | 'success'

export interface EjecutarResponse {
  exito: boolean
  mensaje: string
}
```

- [ ] **Step 2: Agregar la función `ejecutar` en `useCompilador.ts`**

Agregar el import del tipo al principio del archivo:

```ts
import type { CompiladorResponse, Estado, EjecutarResponse } from '~/types'
```

Agregar la función `ejecutar` dentro del cuerpo de `useCompilador`, después de `descargarEstructura`:

```ts
async function ejecutar(usuario: string, contrasena: string, baseDatos: string, sql: string): Promise<EjecutarResponse> {
  const response = await $fetch<EjecutarResponse>('http://localhost:8080/api/ejecutar', {
    method: 'POST',
    body: { usuario, contrasena, baseDatos, sql }
  })
  return response
}
```

Agregar `ejecutar` al objeto retornado por `useCompilador`:

```ts
return {
  estado,
  resultado,
  errorConexion,
  compilar,
  descargarSQL,
  descargarEstructura,
  ejecutar
}
```

- [ ] **Step 3: Verificar que el frontend compila sin errores de tipos**

```bash
cd frontend
npx nuxi typecheck
```

Resultado esperado: sin errores de TypeScript.

- [ ] **Step 4: Commit**

```bash
git add frontend/types/index.ts frontend/composables/useCompilador.ts
git commit -m "feat: add ejecutar() to useCompilador and EjecutarResponse type"
```

---

## Task 6: Agregar botón, modal y toast en index.vue

**Files:**
- Modify: `frontend/pages/index.vue`

- [ ] **Step 1: Agregar el estado local y la función `handleEjecutar` en el `<script setup>`**

En `index.vue`, en el bloque `<script setup>`, agregar después de la línea que desestructura `useCompilador`:

```ts
const { estado, resultado, compilar, descargarSQL, descargarEstructura, ejecutar } = useCompilador()

const modalAbierto = ref(false)
const ejecutando = ref(false)
const formUsuario = ref('postgres')
const formContrasena = ref('')
const formBaseDatos = ref('')
const toast = ref({ visible: false, exito: false, mensaje: '' })

async function handleEjecutar() {
  ejecutando.value = true
  try {
    const res = await ejecutar(formUsuario.value, formContrasena.value, formBaseDatos.value, resultado.value!.sql)
    toast.value = { visible: true, exito: res.exito, mensaje: res.mensaje }
    modalAbierto.value = false
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : 'Error de conexión con el servidor'
    toast.value = { visible: true, exito: false, mensaje: msg }
    modalAbierto.value = false
  } finally {
    ejecutando.value = false
  }
}
```

Nota: la línea original `const { estado, resultado, compilar, descargarSQL, descargarEstructura } = useCompilador()` debe reemplazarse por la versión de arriba que incluye `ejecutar`.

- [ ] **Step 2: Agregar el botón "Ejecutar en BD" en el panel-footer del panel derecho**

En el template, en el `<div class="panel-footer">` del panel derecho (el que tiene el botón "Descargar SQL"), agregar el tercer botón:

```html
<div class="panel-footer">
  <button class="btn descargar" :disabled="!resultado?.sql" @click="descargarSQL">
    Descargar SQL
  </button>
  <button class="btn descargar-alt" :disabled="!resultado?.estructura" @click="descargarEstructura">
    Descargar Estructura
  </button>
  <button class="btn ejecutar" :disabled="estado !== 'success'" @click="modalAbierto = true">
    Ejecutar en BD
  </button>
</div>
```

- [ ] **Step 3: Agregar el modal y el toast al template, justo antes del cierre de `<div class="app">`**

```html
    <!-- Modal de credenciales -->
    <div v-if="modalAbierto" class="modal-overlay">
      <div class="modal">
        <div class="modal-title">Conectar a PostgreSQL</div>
        <div class="modal-body">
          <label>Usuario</label>
          <input v-model="formUsuario" type="text" placeholder="postgres" />
          <label>Contraseña</label>
          <input v-model="formContrasena" type="password" placeholder="Contraseña" />
          <label>Base de datos</label>
          <input v-model="formBaseDatos" type="text" placeholder="Nombre de la base de datos" />
        </div>
        <div class="modal-footer">
          <button class="btn compilar" :disabled="ejecutando" @click="handleEjecutar">
            {{ ejecutando ? 'Ejecutando...' : 'Ejecutar' }}
          </button>
          <button class="btn descargar" :disabled="ejecutando" @click="modalAbierto = false">
            Cancelar
          </button>
        </div>
      </div>
    </div>

    <!-- Toast de resultado -->
    <div v-if="toast.visible" :class="['toast', toast.exito ? 'toast-ok' : 'toast-err']">
      <span>{{ toast.mensaje }}</span>
      <button class="toast-close" @click="toast.visible = false">✕</button>
    </div>

  </div>
```

- [ ] **Step 4: Agregar los estilos del botón, modal y toast en el `<style>`**

Agregar al final del bloque `<style>`, antes del cierre `</style>`:

```css
.ejecutar {
  background: rgba(255,165,0,0.15);
  color: #ffa500;
  border: 1px solid rgba(255,165,0,0.3);
}
.ejecutar:hover:not(:disabled) { background: rgba(255,165,0,0.25); }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: #252526;
  border: 1px solid #3e3e3e;
  border-radius: 8px;
  width: 340px;
  overflow: hidden;
}

.modal-title {
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 600;
  background: #2d2d2d;
  border-bottom: 1px solid #3e3e3e;
}

.modal-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.modal-body label {
  font-size: 12px;
  color: #858585;
}

.modal-body input {
  background: #1e1e1e;
  border: 1px solid #3e3e3e;
  border-radius: 4px;
  color: #d4d4d4;
  padding: 6px 10px;
  font-size: 13px;
  outline: none;
}

.modal-body input:focus {
  border-color: #569cd6;
}

.modal-footer {
  padding: 12px 16px;
  background: #2d2d2d;
  border-top: 1px solid #3e3e3e;
  display: flex;
  gap: 8px;
}

.toast {
  position: fixed;
  bottom: 24px;
  right: 24px;
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 200;
  max-width: 360px;
}

.toast-ok {
  background: rgba(78,201,176,0.2);
  color: #4ec9b0;
  border: 1px solid rgba(78,201,176,0.3);
}

.toast-err {
  background: rgba(244,71,71,0.2);
  color: #f44747;
  border: 1px solid rgba(244,71,71,0.3);
}

.toast-close {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  margin-left: auto;
}
```

- [ ] **Step 5: Verificar que el frontend inicia sin errores**

```bash
cd frontend
npm run dev
```

Resultado esperado: servidor iniciado en `http://localhost:3000` sin errores en consola.

- [ ] **Step 6: Verificar manualmente el flujo golden path**

1. Abrir `http://localhost:3000` en el navegador.
2. Hacer click en "Compilar" con el código de ejemplo — verificar que aparece el badge "✓ OK".
3. Verificar que el botón "Ejecutar en BD" está activo (no gris).
4. Hacer click en "Ejecutar en BD" — verificar que el modal aparece con el campo Usuario pre-cargado con "postgres".
5. Llenar contraseña y nombre de BD, hacer click "Cancelar" — verificar que el modal se cierra y no aparece toast.
6. Abrir el modal de nuevo, hacer click "Ejecutar" con campos incompletos — verificar que aparece el toast rojo con el mensaje de error.
7. Verificar que el botón "✕" del toast lo cierra.

- [ ] **Step 7: Commit final**

```bash
git add frontend/pages/index.vue
git commit -m "feat: add ejecutar-en-BD button, modal and toast in index.vue"
```
