# JOJO 替身使者觉醒 · Vue 项目

将原始单文件 `jojo-register.html` 转换为 **Vue 3 + Vite** 项目，整体视觉风格与交互行为与原网页保持一致。

## 运行

```bash
npm install
npm run dev      # 启动开发服务器（http://192.168.68.34:5173，局域网可访问）
npm run build    # 构建生产版本到 dist/
npm run preview  # 预览生产构建
```

## 功能

- 「觉醒注册 / 替身登录」标签页切换
- 实时表单校验（用户名、邮箱、密码强度、确认密码、协议勾选）与波纹密码强度条
- 注册 / 登录成功后「觉醒」遮罩动画与替身令牌（JWT）展示
- 「验证令牌 · 领取任务」调用 `/api/me` 与受保护的 `/api/quests`
- **奇妙冒险任务详档**：卡片显示所属篇章与地点，详情弹层含任务背景、编号行动目标清单、报酬与风险提示
- **替身图鉴详档**：六维属性条之外，另有能力详解、弱点与发动限制、进化形态、代表战绩与经典台词
- 搜索 / 联想覆盖标题、描述、等级、篇章、地点、行动目标与背景长文
- 后台管理系统：任务（含全部详档字段）、热门关键词、导航菜单的增删改查
- 详档字段均为可选，留空时前端自动隐藏对应区块；接口不可用时首页会回退到内置详档数据

## 后端对接

`src/api.js` 中 `USE_MOCK = false`，**已对接真实后端** `springBootTest`（`192.168.68.34:8081`）；Vite 的 `/api` 代理转发到该地址。后端依赖本机 MySQL 的 `jojo` 库，首次启动会自动建表并写入种子数据。

主要接口：`/api/register`、`/api/login`、`/api/me`、`/api/quests`、`/api/quests/{id}`、`/api/search`、`/api/search/suggest`、`/api/nav`、`/api/hot-keywords`、`/api/stands`、`/api/stands/{id}`、`/api/archives`、`/api/characters`、`/api/admin/*`，统一返回 `{ code, msg, data }`。

若要脱离后端单独预览，把 `src/api.js` 顶部的 `USE_MOCK` 改回 `true` 即可使用本地模拟后端。
