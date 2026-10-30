# JOJO 替身使者觉醒 · Vue 项目

将原始单文件 `jojo-register.html` 转换为 **Vue 3 + Vite** 项目，整体视觉风格与交互行为与原网页保持一致。

## 运行

```bash
npm install
npm run dev      # 启动开发服务器（默认 http://localhost:5173）
npm run build    # 构建生产版本到 dist/
npm run preview  # 预览生产构建
```

## 功能

- 「觉醒注册 / 替身登录」标签页切换
- 实时表单校验（用户名、邮箱、密码强度、确认密码、协议勾选）
- 波纹密码强度条
- 注册 / 登录成功后「觉醒」遮罩动画与替身令牌（JWT）展示
- 「验证令牌 · 领取任务」调用 `/api/me` 与受保护的 `/api/quests`

## 后端对接

原网页通过 `fetch` 调用 `/api/register`、`/api/login`、`/api/me`、`/api/quests`。
本项目在 `src/api.js` 中默认开启 `USE_MOCK = true`，使用本地模拟后端，无需后端即可完整体验。

若要接入真实后端，把 `src/api.js` 顶部的 `USE_MOCK` 改为 `false`，并确保后端提供对应接口（返回 `{ code: 0, data: ... }`）。
