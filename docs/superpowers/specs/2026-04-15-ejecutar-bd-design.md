# Diseño: Ejecutar SQL en PostgreSQL desde el Frontend

**Fecha:** 2026-04-15  
**Estado:** Aprobado

## Resumen

Agregar un botón "Ejecutar en BD" en el panel de resultado. Al hacer click, abre un modal que pide credenciales (usuario, contraseña, nombre de BD), luego llama a un nuevo endpoint del backend que se conecta a PostgreSQL via JDBC y ejecuta el SQL generado. El resultado se muestra como un toast con botón de cierre.

---

## Backend

### Nuevo endpoint: `POST /api/ejecutar`

**Request body (`EjecutarRequest`):**
```json
{
  "usuario": "postgres",
  "contrasena": "1234",
  "baseDatos": "empresa",
  "sql": "CREATE TABLE ..."
}
```

**Response body (`EjecutarResponse`):**
```json
{ "exito": true,  "mensaje": "Base de datos creada exitosamente" }
{ "exito": false, "mensaje": "FATAL: password authentication failed for user \"postgres\"" }
```

### Nuevos archivos

| Archivo | Responsabilidad |
|---------|----------------|
| `dto/EjecutarRequest.java` | POJO con campos: `usuario`, `contrasena`, `baseDatos`, `sql` |
| `dto/EjecutarResponse.java` | POJO con campos: `exito` (boolean), `mensaje` (String) |
| `service/EjecutarService.java` | Conecta via `DriverManager.getConnection("jdbc:postgresql://localhost:5432/{baseDatos}", usuario, contrasena)`, ejecuta el SQL con `Statement.execute()`, captura `SQLException` |
| `controller/EjecutarController.java` | Expone `POST /api/ejecutar`, delega a `EjecutarService` |

### Dependencia JDBC

Agregar en `pom.xml`:
```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

### Manejo de errores

- `SQLException` → capturada, su `getMessage()` va al campo `mensaje` de la respuesta con `exito: false`.
- El servicio cierra la conexión en el bloque `finally`.

---

## Frontend

### Botón "Ejecutar en BD"

- Ubicado en el `panel-footer` del panel derecho, junto a los botones "Descargar SQL" y "Descargar Estructura".
- Habilitado solo cuando `estado === 'success'` (hay SQL generado sin errores).
- Estilo coherente con los botones existentes (variante nueva de color, e.g. naranja/amarillo).

### Modal de credenciales

Aparece sobre la pantalla con un overlay oscuro semitransparente al hacer click en "Ejecutar en BD".

**Campos del formulario:**
| Campo | Placeholder / Default | Tipo |
|-------|-----------------------|------|
| Usuario | `postgres` | text |
| Contraseña | *(vacío)* | password |
| Base de datos | *(vacío)* | text |

**Botones:**
- "Ejecutar" — llama a `ejecutar()`, muestra "Ejecutando..." y se deshabilita mientras espera.
- "Cancelar" — cierra el modal sin hacer nada.

### Toast de resultado

- `<div>` fijo en la esquina inferior derecha.
- Fondo verde + texto "Base de datos creada exitosamente" si `exito: true`.
- Fondo rojo + mensaje de error de PostgreSQL si `exito: false`.
- Incluye un botón "✕" que cierra el toast manualmente.
- No desaparece automáticamente (sin `setTimeout`).

### Cambios en archivos existentes

| Archivo | Cambio |
|---------|--------|
| `useCompilador.ts` | Nueva función `ejecutar(usuario, contrasena, baseDatos, sql)` que hace `POST /api/ejecutar` y retorna `EjecutarResponse` |
| `index.vue` | Botón nuevo, template del modal, template del toast; estado local con `ref` (`modalAbierto`, `ejecutando`, `toast`) |

### Estado local en `index.vue`

```ts
const modalAbierto = ref(false)
const ejecutando = ref(false)
const toast = ref<{ visible: boolean; exito: boolean; mensaje: string }>({
  visible: false, exito: false, mensaje: ''
})
```

---

## Flujo completo

```
Usuario compila → estado = 'success' → botón "Ejecutar en BD" activo
  → click → modal abre
  → llena usuario/contraseña/baseDatos → click "Ejecutar"
  → frontend llama POST /api/ejecutar con {usuario, contrasena, baseDatos, sql}
  → backend conecta a PostgreSQL localhost:5432 via JDBC
  → ejecuta el DDL
  → devuelve {exito, mensaje}
  → modal cierra
  → toast aparece (verde o rojo)
  → usuario lee resultado y cierra toast con ✕
```

---

## Lo que NO incluye este diseño

- Soporte para host/puerto personalizado (siempre `localhost:5432`).
- Historial de ejecuciones.
- Creación automática de la base de datos si no existe.
- Apertura de pgAdmin.
