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
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
const defaultCode = `crear empresa
usar empresa
tabla depto
inicio
nombre letras
codigo letras
num numeros
funciones letras
fin
tabla empleado
inicio
nombre letras
edad numeros
fechanac fecha
salario numeros
depende_de depto
fin
cerrar`

const codigo = ref(defaultCode)
const { estado, resultado, compilar, descargarSQL, descargarEstructura } = useCompilador()

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
</style>
