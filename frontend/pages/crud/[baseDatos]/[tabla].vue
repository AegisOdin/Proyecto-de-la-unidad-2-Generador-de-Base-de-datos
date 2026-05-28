<template>
  <div class="crud">
    <div class="topbar">
      <NuxtLink to="/" class="back">← Volver al IDE</NuxtLink>
      <h1>CRUD: {{ tabla }} <span class="bd">@ {{ baseDatos }}</span></h1>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <form class="form" @submit.prevent="guardar">
      <h2>{{ editandoId ? 'Editar registro #' + editandoId : 'Nuevo registro' }}</h2>
      <div class="grid">
        <div v-for="col in columnas" :key="col" class="campo">
          <label>{{ col }}</label>
          <input v-model="formData[col]" type="text" />
        </div>
      </div>
      <div class="form-actions">
        <button type="submit" class="btn primary">{{ editandoId ? 'Actualizar' : 'Crear' }}</button>
        <button v-if="editandoId" type="button" class="btn ghost" @click="cancelar">Cancelar</button>
      </div>
    </form>

    <div class="tabla-box">
      <table v-if="filas.length">
        <thead>
          <tr>
            <th v-for="col in columnasConPk" :key="col">{{ col }}</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="fila in filas" :key="fila[pk]">
            <td v-for="col in columnasConPk" :key="col">{{ fila[col] ?? '' }}</td>
            <td class="acciones">
              <button class="btn small" @click="editar(fila)">Editar</button>
              <button class="btn small danger" @click="borrar(fila[pk])">Borrar</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">Sin registros</div>
    </div>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const baseDatos = route.params.baseDatos as string
const tabla = route.params.tabla as string
const pk = computed(() => `${tabla}_key`)

const API = `http://localhost:8080/api/crud/${baseDatos}/${tabla}`

const filas = ref<Record<string, any>[]>([])
const columnasMeta = ref<string[]>([])
const error = ref('')
const editandoId = ref<number | null>(null)
const formData = ref<Record<string, any>>({})

const columnasConPk = computed(() => {
  if (filas.value.length) return Object.keys(filas.value[0])
  return columnasMeta.value
})

const columnas = computed(() => columnasConPk.value.filter(c => c !== pk.value))

async function listar() {
  error.value = ''
  try {
    filas.value = await $fetch(API)
    if (!filas.value.length && !Object.keys(formData.value).length) {
      await inicializarFormDesdeMeta()
    }
  } catch (e: any) {
    error.value = e?.data?.mensaje || e.message || 'Error listando'
  }
}

async function inicializarFormDesdeMeta() {
  try {
    const cols = await $fetch<any>(`http://localhost:8080/api/crud/${baseDatos}/${tabla}/columnas`)
    if (Array.isArray(cols)) {
      columnasMeta.value = cols
      formData.value = {}
      for (const c of cols) {
        if (c !== pk.value) formData.value[c] = ''
      }
    }
  } catch {}
}

function editar(fila: Record<string, any>) {
  editandoId.value = fila[pk.value]
  formData.value = {}
  for (const c of columnas.value) formData.value[c] = fila[c]
}

function cancelar() {
  editandoId.value = null
  formData.value = {}
  for (const c of columnas.value) formData.value[c] = ''
}

async function guardar() {
  error.value = ''
  const data: Record<string, any> = {}
  for (const c of columnas.value) {
    const v = formData.value[c]
    data[c] = (v === '' || v === undefined) ? null : v
  }
  try {
    if (editandoId.value) {
      await $fetch(`${API}/${editandoId.value}`, { method: 'PUT', body: data })
    } else {
      await $fetch(API, { method: 'POST', body: data })
    }
    cancelar()
    await listar()
  } catch (e: any) {
    error.value = e?.data?.mensaje || e?.response?._data?.mensaje || e.message || 'Error guardando'
  }
}

async function borrar(id: number) {
  if (!confirm(`Borrar registro ${id}?`)) return
  error.value = ''
  try {
    await $fetch(`${API}/${id}`, { method: 'DELETE' })
    await listar()
  } catch (e: any) {
    error.value = e?.data?.mensaje || e.message || 'Error borrando'
  }
}

onMounted(listar)

watch(columnas, (cols) => {
  if (cols.length && Object.keys(formData.value).length === 0) {
    for (const c of cols) formData.value[c] = ''
  }
})
</script>

<style scoped>
.crud {
  min-height: 100vh;
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 20px;
  font-family: 'Segoe UI', sans-serif;
}

.topbar { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.back { color: #569cd6; text-decoration: none; font-size: 13px; }
.back:hover { text-decoration: underline; }
h1 { font-size: 1.3rem; color: #d4d4d4; }
.bd { color: #858585; font-size: 0.85rem; font-weight: 400; }

.error {
  background: rgba(244,71,71,0.15); color: #f44747;
  border: 1px solid rgba(244,71,71,0.3);
  padding: 10px 14px; border-radius: 6px; margin-bottom: 14px; font-size: 13px;
}

.form {
  background: #252526; border: 1px solid #3e3e3e; border-radius: 6px;
  padding: 16px; margin-bottom: 20px;
}
.form h2 { font-size: 14px; color: #569cd6; margin-bottom: 12px; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 10px; }
.campo { display: flex; flex-direction: column; gap: 4px; }
.campo label { font-size: 11px; color: #858585; }
.campo input {
  background: #1e1e1e; border: 1px solid #3e3e3e; color: #d4d4d4;
  padding: 6px 10px; border-radius: 4px; font-size: 13px; outline: none;
}
.campo input:focus { border-color: #569cd6; }

.form-actions { margin-top: 12px; display: flex; gap: 8px; }

.btn {
  border: none; padding: 6px 14px; border-radius: 4px;
  font-size: 13px; font-weight: 600; cursor: pointer;
}
.btn.primary { background: #0e639c; color: #fff; }
.btn.primary:hover { background: #1177bb; }
.btn.ghost {
  background: transparent; color: #d4d4d4; border: 1px solid #3e3e3e;
}
.btn.small { padding: 4px 10px; font-size: 11px; }
.btn.danger { background: rgba(244,71,71,0.2); color: #f44747; border: 1px solid rgba(244,71,71,0.3); }
.btn.danger:hover { background: rgba(244,71,71,0.3); }

.tabla-box {
  background: #252526; border: 1px solid #3e3e3e; border-radius: 6px; overflow: hidden;
}
table { width: 100%; border-collapse: collapse; }
th, td {
  text-align: left; padding: 8px 12px;
  border-bottom: 1px solid #3e3e3e; font-size: 13px;
}
th { background: #2d2d2d; color: #569cd6; font-weight: 600; }
tr:hover { background: #2a2a2a; }
.acciones { display: flex; gap: 6px; }
.empty { padding: 24px; text-align: center; color: #858585; font-size: 13px; }
</style>
