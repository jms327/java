// 本地预览用临时配置：覆盖原配置中固定绑定的 192.168.68.34
// 不修改项目原有 vite.config.js，仅用于当前机器启动预览
import baseConfig from './vite.config.js'

export default {
  ...baseConfig,
  server: {
    ...baseConfig.server,
    host: '127.0.0.1',
    port: 5173,
    open: false,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true
      }
    }
  }
}
