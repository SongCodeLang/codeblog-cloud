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
    // 注意：SPA 路由（/system/users 等，2 段）与 API（/system/user/page 等，≥3 段）共用 /system 前缀，
    // 必须 bypass 单段路由路径，否则刷新/深链被代理成 API 请求（返回 JSON 当页面，实测坑）
    proxy: {
      '/system': {
        target: 'http://127.0.0.1:9999',
        changeOrigin: true,
        bypass: (req) => {
          const path = (req.url ?? '').split('?')[0]
          const segments = path.split('/').filter(Boolean)
          // /system 或 /system/xxx（页面路由）→ 交回 SPA（index.html）
          if (segments.length <= 2) {
            return '/index.html'
          }
          return undefined
        },
      },
    },
  },
})
