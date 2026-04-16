import type { CompiladorResponse, Estado, EjecutarResponse } from '~/types'

export function useCompilador() {
  const estado = ref<Estado>('idle')
  const resultado = ref<CompiladorResponse | null>(null)
  const errorConexion = ref('')

  async function compilar(codigo: string) {
    estado.value = 'loading'
    errorConexion.value = ''

    try {
      const response = await $fetch<CompiladorResponse>('http://localhost:8080/api/compilar', {
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

  function descargarEstructura() {
    if (resultado.value?.estructura) {
      descargarArchivo(resultado.value.estructura, 'estructura.txt')
    }
  }

  async function ejecutar(usuario: string, contrasena: string, baseDatos: string, sql: string): Promise<EjecutarResponse> {
    const response = await $fetch<EjecutarResponse>('http://localhost:8080/api/ejecutar', {
      method: 'POST',
      body: { usuario, contrasena, baseDatos, sql }
    })
    return response
  }

  return {
    estado,
    resultado,
    errorConexion,
    compilar,
    descargarSQL,
    descargarEstructura,
    ejecutar
  }
}
