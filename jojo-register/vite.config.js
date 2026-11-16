import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '192.168.68.34',
    port: 5173,
    open: true,
    // 后端接口代理：springBootTest 运行在 192.168.68.34:8081
    proxy: {
      '/api': {
        target: 'http://192.168.68.34:8081',
        changeOrigin: true
      }
    }
  }
})
