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

const USE_MOCK = true

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
