export interface CompiladorResponse {
  errores: string[]
  sql: string
  estructura: string
}

export type Estado = 'idle' | 'loading' | 'error' | 'success'
