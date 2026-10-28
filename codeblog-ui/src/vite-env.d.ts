/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 接口基地址（开发环境为空串走 vite 代理） */
  readonly VITE_API_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
