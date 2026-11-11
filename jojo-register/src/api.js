// ============================================================================
// 接口层
// ----------------------------------------------------------------------------
// 原网页通过 fetch 调用 /api/register、/api/login、/api/me、/api/quests。
// 为了让这个纯前端 Vue 项目“开箱即用”（无需后端即可看到觉醒动画与令牌流程），
// 这里默认 USE_MOCK = true，使用本地模拟后端。
//
// 若你已准备好真实后端，只需把 USE_MOCK 改为 false，
// 下方 realRequest 会按原网页的请求格式对接 /api/* 接口。
// ============================================================================

// 已对接真实后端（springBootTest，端口 8081，经 Vite 代理转发 /api）
const USE_MOCK = false

const headers = { 'Content-Type': 'application/json' }

async function realRequest(url, body) {
  const res = await fetch(url, { method: 'POST', headers, body: JSON.stringify(body) })
  return res.json()
}

// 生成一个伪 JWT 风格的令牌（仅用于演示）
function fakeToken(payload) {
  const enc = (obj) => btoa(unescape(encodeURIComponent(JSON.stringify(obj))))
  return ['mock', enc({ alg: 'HS256', typ: 'JWT' }), enc(payload), 'jojo-signature'].join('.')
}

// 内存中的“数据库”，仅用于模拟后端
const db = {
  users: [],
  currentUser: null
}

const SAMPLE_QUESTS = [
  { rank: 'D', title: '杜王町的清晨', desc: '在宁静的小镇中确认替身能力边界。' },
  { rank: 'C', title: '恶人的竞技场', desc: '击败三名虚有其表的替身使者。' },
  { rank: 'B', title: '黄金精神试炼', desc: '于绝境中守护同伴，证明人类的赞歌。' },
  { rank: 'A', title: '命运之轮', desc: '改写一段既定的命运线。' }
]

function mockRequest(url, body) {
  return new Promise((resolve) => {
    setTimeout(() => {
      if (url === '/api/register') {
        const { username, email, password, confirmPassword } = body
        if (db.users.some((u) => u.username === username)) {
          return resolve({ code: 1, msg: '该替身使者名已被觉醒' })
        }
        if (db.users.some((u) => u.email === email)) {
          return resolve({ code: 1, msg: '该邮箱已绑定替身' })
        }
        if (password !== confirmPassword) {
          return resolve({ code: 1, msg: '两次波纹密码不一致' })
        }
        const user = {
          id: db.users.length + 1,
          username,
          email,
          createTime: new Date().toLocaleString('zh-CN')
        }
        db.users.push(user)
        db.currentUser = user
        resolve({ code: 0, data: { token: fakeToken({ uid: user.id, username }), user } })
      } else if (url === '/api/login') {
        const { account, password } = body
        const user = db.users.find(
          (u) => u.username === account || u.email === account
        )
        if (!user) {
          return resolve({ code: 1, msg: '未找到该替身使者' })
        }
        db.currentUser = user
        resolve({ code: 0, data: { token: fakeToken({ uid: user.id, username: user.username }), user } })
      } else if (url === '/api/me') {
        if (!db.currentUser) {
          return resolve({ code: 1, msg: '令牌无效或已过期' })
        }
        resolve({ code: 0, data: db.currentUser })
      } else if (url === '/api/quests') {
        resolve({ code: 0, data: SAMPLE_QUESTS })
      } else {
        resolve({ code: 1, msg: '未知接口' })
      }
    }, 600)
  })
}

export function register(payload) {
  if (USE_MOCK) return mockRequest('/api/register', payload)
  return realRequest('/api/register', payload)
}

export function login(payload) {
  if (USE_MOCK) return mockRequest('/api/login', payload)
  return realRequest('/api/login', payload)
}

export function me(token) {
  if (USE_MOCK) return mockRequest('/api/me', { token })
  return fetch('/api/me', { headers: { Authorization: 'Bearer ' + token } }).then((r) => r.json())
}

export function quests(token) {
  if (USE_MOCK) return mockRequest('/api/quests', { token })
  return fetch('/api/quests', { headers: { Authorization: 'Bearer ' + token } }).then((r) => r.json())
}

// ============================================================================
// 【预留后端接口】首页搜索
// ----------------------------------------------------------------------------
// 后端就绪后对接 GET /api/search?q=<keyword>（携带 Authorization: Bearer <token>），
// 期望返回：{ code: 0, data: [{ rank, title, desc }] }
// ============================================================================
export function search(keyword, token) {
  if (USE_MOCK) {
    const kw = (keyword || '').trim().toLowerCase()
    const data = kw
      ? SAMPLE_QUESTS.filter(
          (q) =>
            q.title.toLowerCase().includes(kw) ||
            q.desc.toLowerCase().includes(kw) ||
            q.rank.toLowerCase() === kw
        )
      : SAMPLE_QUESTS
    return new Promise((resolve) => setTimeout(() => resolve({ code: 0, data }), 400))
  }
  return fetch('/api/search?q=' + encodeURIComponent(keyword || ''), {
    headers: { Authorization: 'Bearer ' + token }
  }).then((r) => r.json())
}

// ============================================================================
// 【预留后端接口】首页导航菜单
// ----------------------------------------------------------------------------
// 后端就绪后对接 GET /api/nav，期望返回：{ code: 0, data: [{ key, label }] }
// 当前前端使用内置导航项，此函数仅作占位，便于后续无缝切换。
// ============================================================================
export function navItems(token) {
  if (USE_MOCK) {
    return new Promise((resolve) =>
      setTimeout(
        () =>
          resolve({
            code: 0,
            data: [
              { key: 'home', label: '首 页' },
              { key: 'quests', label: '奇妙任务' },
              { key: 'stands', label: '替身图鉴' },
              { key: 'archive', label: '冒险档案' }
            ]
          }),
        300
      )
    )
  }
  return fetch('/api/nav', { headers: { Authorization: 'Bearer ' + token } }).then((r) => r.json())
}

// ============================================================================
// 【首页热门关键词】GET /api/hot-keywords
// 期望返回：{ code: 0, data: ['杜王町', '黄金精神', ...] }
// 失败时前端回退到内置热门词，不影响页面。
// ============================================================================
export function hotKeywords() {
  return fetch('/api/hot-keywords').then((r) => r.json())
}

// ============================================================================
// 【替身图鉴】GET /api/stands（公开接口，无需登录）
// 期望返回：{ code: 0, data: [{ id, name, originalName, userName, type,
//   destroyPower, speed, range, endurance, precision, growth, ability, quote }] }
// ============================================================================
export function stands() {
  return fetch('/api/stands').then((r) => r.json())
}

// ============================================================================
// 【冒险档案】GET /api/archives（公开接口，无需登录）
// 期望返回：{ code: 0, data: [{ id, part, title, era, location, hero,
//   villain, summary, highlight }] }
// ============================================================================
export function archives() {
  return fetch('/api/archives').then((r) => r.json())
}

// ============================================================================
// 【名角档案】GET /api/characters（公开接口，无需登录）
// 期望返回：{ code: 0, data: [{ id, name, nameOriginal, alias, roleType,
//   era, nationality, debut, personality, vampireAbility, standName,
//   standOriginal, quotes, biography, highlight, sort }] }
// ============================================================================
export function characters() {
  return fetch('/api/characters').then((r) => r.json())
}

// ============================================================================
// 【名角详情】GET /api/characters/{id}
// 期望返回：{ code: 0, data: {...} }；不存在时 code = 1
// ============================================================================
export function characterDetail(id) {
  return fetch('/api/characters/' + encodeURIComponent(id)).then((r) => r.json())
}

// ============================================================================
// 【搜索联想】GET /api/search/suggest?q=<keyword>
// 期望返回：{ code: 0, data: [{ rank, title, desc }] }，最多 8 条。
// 失败时前端回退到本地按标题/描述过滤。
// ============================================================================
export function suggest(keyword, token) {
  return fetch('/api/search/suggest?q=' + encodeURIComponent(keyword || ''), {
    headers: { Authorization: 'Bearer ' + token }
  }).then((r) => r.json())
}

// ============================================================================
// 【任务详情】GET /api/quests/{id}
// 期望返回：{ code: 0, data: { rank, title, desc } }；不存在时 code = 1
// ============================================================================
export function questDetail(id, token) {
  return fetch('/api/quests/' + encodeURIComponent(id), {
    headers: { Authorization: 'Bearer ' + token }
  }).then((r) => r.json())
}

// ============================================================================
// 【后台管理系统接口】/api/admin
// ----------------------------------------------------------------------------
// 仅登录（无注册），登录后凭管理员令牌对首页「奇妙任务」数据进行增删改查。
// 统一返回 { code, msg, data }，与后端 Result 一致。
// ============================================================================
function authHeaders(token) {
  return { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token }
}

/** 管理员登录（仅登录，无注册） */
export function adminLogin(payload) {
  return realRequest('/api/admin/login', payload)
}

/** 任务列表（后台管理表格） */
export function adminQuests(token) {
  return fetch('/api/admin/quests', { headers: { Authorization: 'Bearer ' + token } }).then((r) => r.json())
}

/** 新增任务 */
export function adminCreateQuest(payload, token) {
  return fetch('/api/admin/quests', {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }).then((r) => r.json())
}

/** 修改任务 */
export function adminUpdateQuest(id, payload, token) {
  return fetch('/api/admin/quests/' + id, {
    method: 'PUT',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }).then((r) => r.json())
}

/** 删除任务 */
export function adminDeleteQuest(id, token) {
  return fetch('/api/admin/quests/' + id, {
    method: 'DELETE',
    headers: { Authorization: 'Bearer ' + token }
  }).then((r) => r.json())
}

// ----------------------------------------------------------------------------
// 后台通用请求：GET / POST / PUT / DELETE 一个入口，减少重复代码
// ----------------------------------------------------------------------------
function adminRequest(path, { method = 'GET', body, token } = {}) {
  const opts = { method, headers: { Authorization: 'Bearer ' + token } }
  if (body !== undefined) {
    opts.headers['Content-Type'] = 'application/json'
    opts.body = JSON.stringify(body)
  }
  return fetch('/api/admin' + path, opts).then((r) => r.json())
}

/** 后台概览统计：任务数 / 关键词数 / 导航项数 / 用户数 */
export function adminStats(token) {
  return adminRequest('/stats', { token })
}

/** 热门关键词列表 */
export function adminHotKeywords(token) {
  return adminRequest('/hot-keywords', { token })
}

/** 新增热门关键词 */
export function adminCreateKeyword(payload, token) {
  return adminRequest('/hot-keywords', { method: 'POST', body: payload, token })
}

/** 修改热门关键词 */
export function adminUpdateKeyword(id, payload, token) {
  return adminRequest('/hot-keywords/' + id, { method: 'PUT', body: payload, token })
}

/** 删除热门关键词 */
export function adminDeleteKeyword(id, token) {
  return adminRequest('/hot-keywords/' + id, { method: 'DELETE', token })
}

/** 导航菜单列表 */
export function adminNavItems(token) {
  return adminRequest('/nav', { token })
}

/** 新增导航菜单 */
export function adminCreateNavItem(payload, token) {
  return adminRequest('/nav', { method: 'POST', body: payload, token })
}

/** 修改导航菜单 */
export function adminUpdateNavItem(id, payload, token) {
  return adminRequest('/nav/' + id, { method: 'PUT', body: payload, token })
}

/** 删除导航菜单 */
export function adminDeleteNavItem(id, token) {
  return adminRequest('/nav/' + id, { method: 'DELETE', token })
}

// ----------------------------------------------------------------------------
// 名角档案（后台管理：增删改查）
// ----------------------------------------------------------------------------
/** 名角档案列表 */
export function adminCharacters(token) {
  return adminRequest('/characters', { token })
}

/** 新增名角档案 */
export function adminCreateCharacter(payload, token) {
  return adminRequest('/characters', { method: 'POST', body: payload, token })
}

/** 修改名角档案 */
export function adminUpdateCharacter(id, payload, token) {
  return adminRequest('/characters/' + id, { method: 'PUT', body: payload, token })
}

/** 删除名角档案 */
export function adminDeleteCharacter(id, token) {
  return adminRequest('/characters/' + id, { method: 'DELETE', token })
}

