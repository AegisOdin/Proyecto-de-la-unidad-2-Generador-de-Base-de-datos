export interface Atributo {
  nombreAtributo: string
  tipoAtributo: string
  esForeignKey: boolean
  tablaReferencia: string | null
}

export interface Tabla {
  nombre: string
  atributos: Atributo[]
}

export interface CompiladorResponse {
  errores: string[]
  sql: string
  estructura: string
  baseDatos?: string
  tablas?: Tabla[]
}

export type Estado = 'idle' | 'loading' | 'error' | 'success'

export interface EjecutarResponse {
  exito: boolean
  mensaje: string
}
