<template>
  <div class="app">

    <div class="header">
      <h1>Generador de Base de Datos</h1>
      <p>Escribe tu schema y genera el SQL</p>
    </div>

    <div class="editors">

      <!-- Panel izquierdo: codigo fuente -->
      <div class="panel">
        <div class="panel-title">Codigo Fuente</div>
        <div class="editor-box">
          <MonacoEditor
            v-model="codigo"
            lang="plaintext"
            :options="editorOpciones"
            style="width:100%;height:100%"
          />
        </div>
        <div class="panel-footer">
          <button class="btn compilar" :disabled="estado === 'loading'" @click="handleCompilar">
            {{ estado === 'loading' ? 'Compilando...' : 'Compilar' }}
          </button>
        </div>
      </div>

      <!-- Panel derecho: resultado -->
      <div class="panel">
        <div class="panel-title">
          Resultado
          <span v-if="estado === 'success'" class="badge ok">✓ OK</span>
          <span v-if="estado === 'error'" class="badge err">✗ Error</span>
        </div>
        <div class="editor-box">
          <MonacoEditor
            v-model="outputContent"
            :lang="outputLang"
            :options="outputOpciones"
            style="width:100%;height:100%"
          />
        </div>
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
      </div>

    </div>

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
</template>

<script setup lang="ts">
const defaultCode = `crear empresa
usar empresa

tabla depto
inicio
  nombre    letras
  codigo    letras
  num       numeros
  funciones letras
fin

tabla empleado
inicio
  nombre     letras
  edad       numeros
  fechanac   fecha
  salario    numeros
  depende_de depto
fin

cerrar`

const codigo = ref(defaultCode)
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

const editorOpciones = {
  theme: 'vs-dark',
  fontSize: 14,
  lineNumbers: 'on' as const,
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  automaticLayout: true,
  wordWrap: 'on' as const,
}

const outputOpciones = {
  ...editorOpciones,
  readOnly: true,
}

const outputContent = ref('-- El resultado aparece aqui despues de compilar')

watch(resultado, (r) => {
  if (!r) return
  if (r.errores.length > 0) {
    outputContent.value = r.errores.join('\n')
  } else {
    outputContent.value = r.sql
  }
})

const outputLang = computed(() =>
  estado.value === 'error' ? 'plaintext' : 'sql'
)

function handleCompilar() {
  compilar(codigo.value)
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', sans-serif;
  background: #1e1e1e;
  color: #d4d4d4;
  height: 100vh;
  overflow: hidden;
}

.app {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 16px;
  gap: 12px;
}

.header {
  text-align: center;
}

.header h1 {
  font-size: 1.4rem;
  color: #569cd6;
}

.header p {
  font-size: 0.85rem;
  color: #858585;
  margin-top: 2px;
}

.editors {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.panel {
  display: flex;
  flex-direction: column;
  background: #252526;
  border: 1px solid #3e3e3e;
  border-radius: 6px;
  overflow: hidden;
}

.panel-title {
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 600;
  background: #2d2d2d;
  border-bottom: 1px solid #3e3e3e;
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge {
  font-size: 11px;
  font-weight: 700;
  padding: 1px 8px;
  border-radius: 10px;
}

.badge.ok  { background: rgba(78,201,176,0.2); color: #4ec9b0; }
.badge.err { background: rgba(244,71,71,0.2);  color: #f44747; }

.editor-box {
  flex: 1;
  min-height: 0;
}

.editor-box > * {
  height: 100% !important;
  width: 100% !important;
}

.panel-footer {
  padding: 8px 12px;
  background: #2d2d2d;
  border-top: 1px solid #3e3e3e;
  display: flex;
  gap: 8px;
}

.btn {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.15s;
}

.btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.compilar {
  background: #0e639c;
  color: #fff;
}
.compilar:hover:not(:disabled) { background: #1177bb; }

.descargar {
  background: rgba(78,201,176,0.15);
  color: #4ec9b0;
  border: 1px solid rgba(78,201,176,0.3);
}
.descargar:hover:not(:disabled) { background: rgba(78,201,176,0.25); }

.descargar-alt {
  background: rgba(86,156,214,0.15);
  color: #569cd6;
  border: 1px solid rgba(86,156,214,0.3);
}
.descargar-alt:hover:not(:disabled) { background: rgba(86,156,214,0.25); }

@media (max-width: 768px) {
  .editors { grid-template-columns: 1fr; }
}

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
</style>
