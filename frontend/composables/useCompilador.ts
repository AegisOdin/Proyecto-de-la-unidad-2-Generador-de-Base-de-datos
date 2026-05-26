import type { CompiladorResponse, Estado, EjecutarResponse, Tabla } from '~/types'

const API_BASE = 'http://localhost:8080'

export function useCompilador() {
  const estado = ref<Estado>('idle')
  const resultado = ref<CompiladorResponse | null>(null)
  const errorConexion = ref('')

  async function compilar(codigo: string) {
    estado.value = 'loading'
    errorConexion.value = ''

    try {
      const response = await $fetch<CompiladorResponse>(`${API_BASE}/api/compilar`, {
        method: 'POST',
        body: { codigo }
      })

      resultado.value = response

      if (response.errores && response.errores.length > 0) {
        estado.value = 'error'
      } else {
        estado.value = 'success'
      }
    } catch (e: unknown) {
      estado.value = 'error'
      const message = e instanceof Error ? e.message : 'Error de conexion con el servidor'
      errorConexion.value = message
      resultado.value = {
        errores: [message],
        sql: '',
        estructura: ''
      }
    }
  }

  function descargarArchivo(contenido: string, nombre: string) {
    const blob = new Blob([contenido], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = nombre
    a.click()
    URL.revokeObjectURL(url)
  }

  function descargarSQL() {
    if (resultado.value?.sql) {
      descargarArchivo(resultado.value.sql, 'base_de_datos.sql')
    }
  }

  async function ejecutar(usuario: string, contrasena: string, sql: string): Promise<EjecutarResponse> {
    return await $fetch<EjecutarResponse>(`${API_BASE}/api/ejecutar`, {
      method: 'POST',
      body: { usuario, contrasena, sql }
    })
  }

  async function generarCrud(baseDatos: string, tabla: Tabla) {
    const blob = await $fetch<Blob>(`${API_BASE}/api/generar`, {
      method: 'POST',
      body: { baseDatos, tabla },
      responseType: 'blob'
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `crud_${tabla.nombre}.zip`
    a.click()
    URL.revokeObjectURL(url)
  }

  return {
    estado,
    resultado,
    errorConexion,
    compilar,
    descargarSQL,
    ejecutar,
    generarCrud
  }
}
