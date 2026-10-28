import react from '@vitejs/plugin-react'
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    // 开发期代理：/system/** → 网关 9999（网关 StripPrefix 后转发系统管理服务）
    proxy: {
      '/system': {
        target: 'http://127.0.0.1:9999',
        changeOrigin: true,
      },
    },
  },
})
