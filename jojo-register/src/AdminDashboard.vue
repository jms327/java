<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  adminStats,
  adminQuests,
  adminCreateQuest,
  adminUpdateQuest,
  adminDeleteQuest,
  adminHotKeywords,
  adminCreateKeyword,
  adminUpdateKeyword,
  adminDeleteKeyword,
  adminNavItems,
  adminCreateNavItem,
  adminUpdateNavItem,
  adminDeleteNavItem
} from './api.js'

const emit = defineEmits(['admin-logout', 'back-home', 'admin-expired'])

const token = localStorage.getItem('jojo_admin_token') || ''
const adminName = ref(localStorage.getItem('jojo_admin_name') || 'ADMIN')
// 管理员常从后台独立登录，此时没有普通用户令牌，回不去首页，按钮文案要跟着变
const hasUserSession = !!localStorage.getItem('jojo_token')

/* ============================================================================
 * 模块切换：概览 / 奇妙任务 / 热门关键词 / 导航菜单
 * 这三块正好对应首页展示的数据：卡片列表、搜索框下的热门标签、顶部导航栏。
 * ========================================================================== */
const tab = ref('overview')
const tabs = [
  { key: 'overview', label: '数据概览', jp: 'OVERVIEW', icon: '◈' },
  { key: 'quests', label: '奇妙任务', jp: 'QUESTS', icon: '★' },
  { key: 'keywords', label: '热门关键词', jp: 'KEYWORDS', icon: '❖' },
  { key: 'nav', label: '导航菜单', jp: 'NAVIGATION', icon: '≡' }
]

const stats = ref({ questCount: 0, keywordCount: 0, navCount: 0, userCount: 0 })
const questList = ref([])
const keywordList = ref([])
const navList = ref([])
const loading = ref(true)

/* ============================================================================
 * 表格筛选与分页
 * 数据量小的时候看不出来，但任务一旦过百条，没有筛选会很难用。
 * 目前是前端过滤（一次拉全量），数据量再涨可以改成后端分页。
 * ========================================================================== */
const PAGE_SIZE = 10
const filters = ref({ questKw: '', questRank: 'ALL', keywordKw: '' })
const pages = ref({ quests: 1, keywords: 1, nav: 1 })

const filteredQuests = computed(() => {
  const kw = filters.value.questKw.trim().toLowerCase()
  const rank = filters.value.questRank
  return questList.value.filter((q) => {
    if (rank !== 'ALL' && (q.rank || '').toUpperCase() !== rank) return false
    if (!kw) return true
    return (
      (q.title || '').toLowerCase().includes(kw) ||
      (q.desc || '').toLowerCase().includes(kw)
    )
  })
})

const filteredKeywords = computed(() => {
  const kw = filters.value.keywordKw.trim().toLowerCase()
  if (!kw) return keywordList.value
  return keywordList.value.filter((k) => (k.keyword || '').toLowerCase().includes(kw))
})

/** 通用分页切片 */
function paginate(list, page) {
  const total = list.length
  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE))
  const current = Math.min(Math.max(1, page), totalPages)
  const start = (current - 1) * PAGE_SIZE
  return { rows: list.slice(start, start + PAGE_SIZE), total, totalPages, current }
}

const questPage = computed(() => paginate(filteredQuests.value, pages.value.quests))
const keywordPage = computed(() => paginate(filteredKeywords.value, pages.value.keywords))
const navPage = computed(() => paginate(navList.value, pages.value.nav))

/** 筛选条件变化后回到第一页，否则可能停在一个空页上 */
function resetPage(kind) {
  pages.value[kind] = 1
}
function goPage(kind, p) {
  pages.value[kind] = p
}
/** 页码过多时只显示当前页附近的窗口，避免按钮铺满一行 */
function pageWindow(totalPages, current) {
  const span = 2
  const list = []
  for (let i = 1; i <= totalPages; i++) {
    if (i === 1 || i === totalPages || Math.abs(i - current) <= span) list.push(i)
    else if (list[list.length - 1] !== '...') list.push('...')
  }
  return list
}

/* ===== 轻提示 ===== */
const msg = ref('')
const msgType = ref('ok')
let msgTimer = null
function flash(text, type = 'ok') {
  msg.value = text
  msgType.value = type
  clearTimeout(msgTimer)
  msgTimer = setTimeout(() => (msg.value = ''), 3200)
}

/* ===== 登录失效统一处理 ===== */
let expiredNotified = false
function handleRes(res, fallback) {
  if (res && res.code === 0) return true
  const text = (res && res.msg) || fallback || '操作失败'
  if (text.indexOf('无权限') >= 0 || text.indexOf('过期') >= 0) {
    if (!expiredNotified) {
      expiredNotified = true
      flash('登录已过期，请重新登录', 'err')
      setTimeout(() => emit('admin-expired'), 900)
    }
    return false
  }
  flash(text, 'err')
  return false
}

/* ============================================================================
 * 编辑器（三块数据共用一套表单弹层：kind 决定字段与提交接口）
 * ========================================================================== */
const editor = ref({ open: false, kind: '', id: null, data: {} })
const saving = ref(false)

const editorTitle = computed(() => {
  const isEdit = editor.value.id !== null
  const names = { quests: '任务', keywords: '关键词', nav: '导航项' }
  return (isEdit ? '编辑' : '新增') + (names[editor.value.kind] || '')
})

function openCreate(kind) {
  if (kind === 'quests') {
    editor.value = { open: true, kind, id: null, data: { rank: 'D', title: '', desc: '', sort: questList.value.length + 1 } }
  } else if (kind === 'keywords') {
    editor.value = { open: true, kind, id: null, data: { keyword: '', weight: 50, sort: keywordList.value.length + 1 } }
  } else {
    editor.value = { open: true, kind, id: null, data: { itemKey: '', label: '', sort: navList.value.length + 1 } }
  }
}

function openEdit(kind, row) {
  if (kind === 'quests') {
    editor.value = { open: true, kind, id: row.id, data: { rank: row.rank, title: row.title, desc: row.desc, sort: row.sort } }
  } else if (kind === 'keywords') {
    editor.value = { open: true, kind, id: row.id, data: { keyword: row.keyword, weight: row.weight, sort: row.sort } }
  } else {
    editor.value = { open: true, kind, id: row.id, data: { itemKey: row.itemKey, label: row.label, sort: row.sort } }
  }
}

function closeEditor() {
  editor.value.open = false
  editor.value.id = null
}

function validate() {
  const { kind, data } = editor.value
  if (kind === 'quests') {
    if (!data.title || !data.title.trim()) return '任务标题不能为空'
    if (!data.desc || !data.desc.trim()) return '任务描述不能为空'
  } else if (kind === 'keywords') {
    if (!data.keyword || !data.keyword.trim()) return '关键词不能为空'
  } else {
    if (!data.itemKey || !data.itemKey.trim()) return '菜单标识不能为空'
    if (!data.label || !data.label.trim()) return '展示文案不能为空'
  }
  return ''
}

async function saveEditor() {
  const err = validate()
  if (err) return flash(err, 'err')

  const { kind, id, data } = editor.value
  const payload =
    kind === 'quests'
      ? { rank: data.rank, title: data.title.trim(), desc: data.desc.trim(), sort: Number(data.sort) || 0 }
      : kind === 'keywords'
        ? { keyword: data.keyword.trim(), weight: Number(data.weight) || 0, sort: Number(data.sort) || 0 }
        : { itemKey: data.itemKey.trim(), label: data.label.trim(), sort: Number(data.sort) || 0 }

  saving.value = true
  try {
    let res
    if (kind === 'quests') {
      res = id ? await adminUpdateQuest(id, payload, token) : await adminCreateQuest(payload, token)
    } else if (kind === 'keywords') {
      res = id ? await adminUpdateKeyword(id, payload, token) : await adminCreateKeyword(payload, token)
    } else {
      res = id ? await adminUpdateNavItem(id, payload, token) : await adminCreateNavItem(payload, token)
    }
    if (handleRes(res, '保存失败')) {
      flash(id ? '修改已保存，首页同步生效' : '新增成功，首页同步生效')
      closeEditor()
      await loadAll()
    }
  } catch (e) {
    flash('网络异常，无法连接后台服务', 'err')
  } finally {
    saving.value = false
  }
}

async function removeRow(kind, row) {
  const name = kind === 'quests' ? row.title : kind === 'keywords' ? row.keyword : row.label
  if (!window.confirm('确定删除「' + name + '」？该操作不可恢复，首页将立即同步。')) return
  try {
    let res
    if (kind === 'quests') res = await adminDeleteQuest(row.id, token)
    else if (kind === 'keywords') res = await adminDeleteKeyword(row.id, token)
    else res = await adminDeleteNavItem(row.id, token)
    if (handleRes(res, '删除失败')) {
      flash('已删除，首页同步生效')
      await loadAll()
    }
  } catch (e) {
    flash('网络异常，无法连接后台服务', 'err')
  }
}

/* ===== 数据加载 ===== */
async function loadAll() {
  loading.value = true
  try {
    const [s, q, k, n] = await Promise.all([
      adminStats(token),
      adminQuests(token),
      adminHotKeywords(token),
      adminNavItems(token)
    ])
    if (s && s.code === 0 && s.data) stats.value = s.data
    else handleRes(s, '统计加载失败')

    if (q && q.code === 0) questList.value = q.data || []
    else handleRes(q, '任务加载失败')

    if (k && k.code === 0) keywordList.value = k.data || []
    else handleRes(k, '关键词加载失败')

    if (n && n.code === 0) navList.value = n.data || []
    else handleRes(n, '导航加载失败')
  } catch (e) {
    flash('网络异常，无法连接后台服务', 'err')
  } finally {
    loading.value = false
  }
}

function rankClass(r) {
  return 'rk rk-' + (r || 'D').toLowerCase()
}

function onLogout() {
  if (window.confirm('确定退出后台管理？')) emit('admin-logout')
}

onMounted(loadAll)
</script>

<template>
  <div class="console">
    <!-- 顶栏 -->
    <header class="topbar">
      <div class="brand">
        <span class="logo">JOJO</span>
        <span class="divider"></span>
        <span class="sub">后 台 管 理 系 统</span>
      </div>
      <div class="top-right">
        <span class="who"><i></i>{{ adminName }}</span>
        <button type="button" class="ghost" @click="emit('back-home')">
          {{ hasUserSession ? '返回首页' : '返回登录' }}
        </button>
        <button type="button" class="logout" @click="onLogout">退 出</button>
      </div>
    </header>

    <div class="body">
      <!-- 侧边栏 -->
      <aside class="side">
        <div class="side-title">内容管理 / CONTENT</div>
        <button
          v-for="t in tabs"
          :key="t.key"
          type="button"
          class="side-item"
          :class="{ active: tab === t.key }"
          @click="tab = t.key"
        >
          <span class="ico">{{ t.icon }}</span>
          <span class="txt">
            <b>{{ t.label }}</b>
            <em>{{ t.jp }}</em>
          </span>
        </button>
        <div class="side-foot">
          <div class="line"></div>
          <p>此处修改的数据<br />会即时反映到首页</p>
        </div>
      </aside>

      <!-- 主内容 -->
      <main class="main">
        <div class="msg" :class="[msgType, { show: !!msg }]">{{ msg }}</div>

        <div v-if="loading" class="loading">数据加载中…</div>

        <template v-else>
          <!-- ================= 概览 ================= -->
          <section v-if="tab === 'overview'" class="pane">
            <div class="pane-head">
              <h2>数据概览</h2>
              <p>当前首页展示的内容规模，修改任一模块后首页会自动同步。</p>
            </div>

            <div class="stat-grid">
              <div class="stat-card" @click="tab = 'quests'">
                <div class="stat-ico">★</div>
                <div class="stat-num">{{ stats.questCount }}</div>
                <div class="stat-label">奇妙任务</div>
                <div class="stat-hint">首页卡片列表 / 搜索数据源</div>
              </div>
              <div class="stat-card" @click="tab = 'keywords'">
                <div class="stat-ico">❖</div>
                <div class="stat-num">{{ stats.keywordCount }}</div>
                <div class="stat-label">热门关键词</div>
                <div class="stat-hint">搜索框下方「热门」标签</div>
              </div>
              <div class="stat-card" @click="tab = 'nav'">
                <div class="stat-ico">≡</div>
                <div class="stat-num">{{ stats.navCount }}</div>
                <div class="stat-label">导航菜单</div>
                <div class="stat-hint">首页顶部导航栏</div>
              </div>
              <div class="stat-card plain">
                <div class="stat-ico">◉</div>
                <div class="stat-num">{{ stats.userCount }}</div>
                <div class="stat-label">替身使者</div>
                <div class="stat-hint">已注册用户（只读）</div>
              </div>
            </div>

            <div class="tips">
              <h3>管理说明</h3>
              <ul>
                <li><b>奇妙任务</b>：新增 / 编辑 / 删除首页卡片，同时影响搜索与联想结果。</li>
                <li><b>热门关键词</b>：调整权重与顺序，权重越大越靠前，首页最多展示 8 个。</li>
                <li><b>导航菜单</b>：修改导航文案与顺序；标识（key）用于前端跳转，新增后请勿随意改动。</li>
                <li><b>排序</b>：数字越小越靠前，同值时按 ID 升序。</li>
              </ul>
            </div>
          </section>

          <!-- ================= 奇妙任务 ================= -->
          <section v-else-if="tab === 'quests'" class="pane">
            <div class="pane-head row">
              <div>
                <h2>奇妙任务管理</h2>
                <p>
                  共 {{ questList.length }} 条<template v-if="filteredQuests.length !== questList.length">
                    · 已筛选 {{ filteredQuests.length }} 条</template>
                  · 首页卡片、搜索结果、详情弹层均取此数据
                </p>
              </div>
              <button type="button" class="add-btn" @click="openCreate('quests')">+ 新增任务</button>
            </div>

            <!-- 工具栏：关键词搜索 / 等级筛选 / 刷新 -->
            <div class="toolbar">
              <div class="tb-search">
                <input
                  type="text"
                  v-model="filters.questKw"
                  placeholder="搜索标题或描述…"
                  @input="resetPage('quests')"
                />
                <button
                  v-if="filters.questKw"
                  type="button"
                  class="tb-clear"
                  title="清空"
                  @click="filters.questKw = ''; resetPage('quests')"
                >✕</button>
              </div>
              <div class="tb-ranks">
                <button
                  v-for="r in ['ALL', 'D', 'C', 'B', 'A']"
                  :key="r"
                  type="button"
                  class="rank-chip"
                  :class="{ active: filters.questRank === r }"
                  @click="filters.questRank = r; resetPage('quests')"
                >{{ r === 'ALL' ? '全部' : r }}</button>
              </div>
              <button type="button" class="tb-refresh" @click="loadAll">刷新</button>
            </div>

            <div class="table-wrap">
              <table class="grid">
                <thead>
                  <tr>
                    <th class="c-id">ID</th>
                    <th class="c-rank">等级</th>
                    <th class="c-title">标题</th>
                    <th class="c-desc">描述</th>
                    <th class="c-sort">排序</th>
                    <th class="c-ops">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="q in questPage.rows" :key="q.id">
                    <td class="c-id">{{ q.id }}</td>
                    <td><span :class="rankClass(q.rank)">{{ q.rank }}</span></td>
                    <td class="c-title">{{ q.title }}</td>
                    <td class="c-desc">{{ q.desc }}</td>
                    <td class="c-sort">{{ q.sort }}</td>
                    <td class="c-ops">
                      <button type="button" class="op edit" @click="openEdit('quests', q)">编辑</button>
                      <button type="button" class="op del" @click="removeRow('quests', q)">删除</button>
                    </td>
                  </tr>
                  <tr v-if="!questPage.rows.length">
                    <td colspan="6" class="empty">
                      {{ questList.length ? '没有匹配的任务，换个关键词或等级试试' : '暂无任务数据，点击右上角新增' }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="questPage.totalPages > 1" class="pager">
              <button
                type="button"
                class="pg-btn"
                :disabled="questPage.current === 1"
                @click="goPage('quests', questPage.current - 1)"
              >上一页</button>
              <template v-for="(p, i) in pageWindow(questPage.totalPages, questPage.current)" :key="i">
                <span v-if="p === '...'" class="pg-dots">…</span>
                <button
                  v-else
                  type="button"
                  class="pg-btn num"
                  :class="{ active: p === questPage.current }"
                  @click="goPage('quests', p)"
                >{{ p }}</button>
              </template>
              <button
                type="button"
                class="pg-btn"
                :disabled="questPage.current === questPage.totalPages"
                @click="goPage('quests', questPage.current + 1)"
              >下一页</button>
              <span class="pg-info">第 {{ questPage.current }} / {{ questPage.totalPages }} 页 · 每页 {{ PAGE_SIZE }} 条</span>
            </div>
          </section>

          <!-- ================= 热门关键词 ================= -->
          <section v-else-if="tab === 'keywords'" class="pane">
            <div class="pane-head row">
              <div>
                <h2>热门关键词管理</h2>
                <p>
                  共 {{ keywordList.length }} 条<template v-if="filteredKeywords.length !== keywordList.length">
                    · 已筛选 {{ filteredKeywords.length }} 条</template>
                  · 首页搜索框下方展示权重最高的 8 个
                </p>
              </div>
              <button type="button" class="add-btn" @click="openCreate('keywords')">+ 新增关键词</button>
            </div>

            <div class="toolbar">
              <div class="tb-search">
                <input
                  type="text"
                  v-model="filters.keywordKw"
                  placeholder="搜索关键词…"
                  @input="resetPage('keywords')"
                />
                <button
                  v-if="filters.keywordKw"
                  type="button"
                  class="tb-clear"
                  title="清空"
                  @click="filters.keywordKw = ''; resetPage('keywords')"
                >✕</button>
              </div>
              <button type="button" class="tb-refresh" @click="loadAll">刷新</button>
            </div>

            <div class="table-wrap">
              <table class="grid">
                <thead>
                  <tr>
                    <th class="c-id">ID</th>
                    <th>关键词</th>
                    <th class="c-weight">权重</th>
                    <th class="c-sort">排序</th>
                    <th class="c-ops">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="k in keywordPage.rows" :key="k.id">
                    <td class="c-id">{{ k.id }}</td>
                    <td class="c-title">{{ k.keyword }}</td>
                    <td class="c-weight">{{ k.weight }}</td>
                    <td class="c-sort">{{ k.sort }}</td>
                    <td class="c-ops">
                      <button type="button" class="op edit" @click="openEdit('keywords', k)">编辑</button>
                      <button type="button" class="op del" @click="removeRow('keywords', k)">删除</button>
                    </td>
                  </tr>
                  <tr v-if="!keywordPage.rows.length">
                    <td colspan="5" class="empty">
                      {{ keywordList.length ? '没有匹配的关键词' : '暂无关键词，首页将不展示「热门」标签' }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="keywordPage.totalPages > 1" class="pager">
              <button
                type="button"
                class="pg-btn"
                :disabled="keywordPage.current === 1"
                @click="goPage('keywords', keywordPage.current - 1)"
              >上一页</button>
              <template v-for="(p, i) in pageWindow(keywordPage.totalPages, keywordPage.current)" :key="i">
                <span v-if="p === '...'" class="pg-dots">…</span>
                <button
                  v-else
                  type="button"
                  class="pg-btn num"
                  :class="{ active: p === keywordPage.current }"
                  @click="goPage('keywords', p)"
                >{{ p }}</button>
              </template>
              <button
                type="button"
                class="pg-btn"
                :disabled="keywordPage.current === keywordPage.totalPages"
                @click="goPage('keywords', keywordPage.current + 1)"
              >下一页</button>
              <span class="pg-info">第 {{ keywordPage.current }} / {{ keywordPage.totalPages }} 页 · 每页 {{ PAGE_SIZE }} 条</span>
            </div>
          </section>

          <!-- ================= 导航菜单 ================= -->
          <section v-else class="pane">
            <div class="pane-head row">
              <div>
                <h2>首页导航栏管理</h2>
                <p>共 {{ navList.length }} 项 · 控制首页顶部导航的文案与顺序</p>
              </div>
              <button type="button" class="add-btn" @click="openCreate('nav')">+ 新增导航项</button>
            </div>

            <div class="toolbar">
              <p class="tb-note">导航项通常只有几项，修改后刷新首页即可看到</p>
              <button type="button" class="tb-refresh" @click="loadAll">刷新</button>
            </div>

            <div class="table-wrap">
              <table class="grid">
                <thead>
                  <tr>
                    <th class="c-id">ID</th>
                    <th class="c-key">标识 key</th>
                    <th>展示文案</th>
                    <th class="c-sort">排序</th>
                    <th class="c-ops">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="n in navPage.rows" :key="n.id">
                    <td class="c-id">{{ n.id }}</td>
                    <td class="c-key"><code>{{ n.itemKey }}</code></td>
                    <td class="c-title">{{ n.label }}</td>
                    <td class="c-sort">{{ n.sort }}</td>
                    <td class="c-ops">
                      <button type="button" class="op edit" @click="openEdit('nav', n)">编辑</button>
                      <button type="button" class="op del" @click="removeRow('nav', n)">删除</button>
                    </td>
                  </tr>
                  <tr v-if="!navPage.rows.length">
                    <td colspan="5" class="empty">导航为空时首页将使用内置菜单兜底</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="navPage.totalPages > 1" class="pager">
              <button
                type="button"
                class="pg-btn"
                :disabled="navPage.current === 1"
                @click="goPage('nav', navPage.current - 1)"
              >上一页</button>
              <template v-for="(p, i) in pageWindow(navPage.totalPages, navPage.current)" :key="i">
                <span v-if="p === '...'" class="pg-dots">…</span>
                <button
                  v-else
                  type="button"
                  class="pg-btn num"
                  :class="{ active: p === navPage.current }"
                  @click="goPage('nav', p)"
                >{{ p }}</button>
              </template>
              <button
                type="button"
                class="pg-btn"
                :disabled="navPage.current === navPage.totalPages"
                @click="goPage('nav', navPage.current + 1)"
              >下一页</button>
              <span class="pg-info">第 {{ navPage.current }} / {{ navPage.totalPages }} 页 · 每页 {{ PAGE_SIZE }} 条</span>
            </div>
          </section>
        </template>
      </main>
    </div>

    <!-- 编辑弹层 -->
    <transition name="fade">
      <div v-if="editor.open" class="mask" @click.self="closeEditor">
        <div class="editor">
          <span class="corner tl"></span><span class="corner br"></span>
          <div class="ed-head">
            <h3>{{ editorTitle }}</h3>
            <button type="button" class="close" @click="closeEditor">✕</button>
          </div>

          <!-- 任务 -->
          <template v-if="editor.kind === 'quests'">
            <div class="row">
              <label>等级</label>
              <select v-model="editor.data.rank">
                <option value="D">D</option>
                <option value="C">C</option>
                <option value="B">B</option>
                <option value="A">A</option>
              </select>
              <label>排序</label>
              <input type="number" v-model="editor.data.sort" />
            </div>
            <label>标题</label>
            <input type="text" v-model="editor.data.title" placeholder="例：杜王町的清晨" />
            <label>描述</label>
            <textarea v-model="editor.data.desc" rows="3" placeholder="例：在宁静的小镇中确认替身能力边界。"></textarea>
          </template>

          <!-- 关键词 -->
          <template v-else-if="editor.kind === 'keywords'">
            <label>关键词</label>
            <input type="text" v-model="editor.data.keyword" placeholder="例：杜王町" />
            <div class="row">
              <label>权重</label>
              <input type="number" v-model="editor.data.weight" />
              <label>排序</label>
              <input type="number" v-model="editor.data.sort" />
            </div>
            <p class="ed-hint">权重越大越靠前，首页最多展示 8 个</p>
          </template>

          <!-- 导航 -->
          <template v-else>
            <label>标识 key</label>
            <input type="text" v-model="editor.data.itemKey" placeholder="例：home / quests / stands" />
            <label>展示文案</label>
            <input type="text" v-model="editor.data.label" placeholder="例：奇妙任务" />
            <label>排序</label>
            <input type="number" v-model="editor.data.sort" />
            <p class="ed-hint">已有标识：home 首页、quests 奇妙任务、stands 替身图鉴、archive 冒险档案</p>
          </template>

          <div class="ed-ops">
            <button type="button" class="save" :disabled="saving" @click="saveEditor">
              {{ saving ? '保存中…' : '保 存' }}
            </button>
            <button type="button" class="cancel" @click="closeEditor">取 消</button>
          </div>
        </div>
      </div>
    </transition>

    <div class="credit">— 黄金精神 · GOLDEN SPIRIT —</div>
  </div>
</template>

<style scoped>
.console {
  min-height: 100vh;
  padding-bottom: 40px;
}

/* ===== 顶栏 ===== */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 26px;
  border-bottom: 1px solid var(--line);
  background: rgba(10, 10, 16, 0.82);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 20;
}
.brand { display: flex; align-items: center; gap: 14px; }
.brand .logo {
  font-family: "Cinzel", serif;
  font-weight: 900;
  font-size: 24px;
  letter-spacing: 0.05em;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.brand .divider { width: 1px; height: 18px; background: rgba(246, 197, 68, 0.3); }
.brand .sub { font-size: 12px; letter-spacing: 0.22em; color: var(--teal-glow); }

.top-right { display: flex; align-items: center; gap: 10px; }
.who {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  letter-spacing: 0.1em;
  color: #b7b0a2;
  margin-right: 4px;
}
.who i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #39d98a;
  box-shadow: 0 0 8px rgba(57, 217, 138, 0.8);
}
.ghost,
.logout {
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.12em;
  padding: 7px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: 0.22s;
  background: transparent;
}
.ghost { border: 1px solid rgba(63, 224, 242, 0.42); color: var(--teal-glow); }
.ghost:hover { background: rgba(63, 224, 242, 0.12); box-shadow: 0 0 14px rgba(63, 224, 242, 0.22); }
.logout { border: 1px solid rgba(226, 59, 84, 0.5); color: #ff9aa6; }
.logout:hover { background: rgba(226, 59, 84, 0.18); }

/* ===== 布局 ===== */
.body {
  display: flex;
  gap: 22px;
  width: min(1240px, calc(100% - 40px));
  margin: 22px auto 0;
  align-items: flex-start;
}

/* ===== 侧边栏 ===== */
.side {
  width: 208px;
  flex-shrink: 0;
  position: sticky;
  top: 82px;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 16px 12px;
  background: linear-gradient(180deg, rgba(20, 20, 28, 0.9), rgba(10, 10, 16, 0.95));
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.45);
}
.side-title {
  font-family: "Cinzel", serif;
  font-size: 10px;
  letter-spacing: 0.18em;
  color: #8a8475;
  padding: 0 8px 10px;
}
.side-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  text-align: left;
  border: 1px solid transparent;
  background: transparent;
  padding: 10px 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: 0.2s;
  margin-bottom: 4px;
  font-family: inherit;
}
.side-item .ico {
  width: 24px;
  text-align: center;
  color: rgba(246, 197, 68, 0.55);
  font-size: 14px;
}
.side-item .txt { display: flex; flex-direction: column; line-height: 1.35; }
.side-item .txt b { font-size: 13.5px; font-weight: 500; color: #cfc8b8; letter-spacing: 0.04em; }
.side-item .txt em { font-style: normal; font-size: 9px; letter-spacing: 0.16em; color: #6f6a5e; }
.side-item:hover { background: rgba(246, 197, 68, 0.06); }
.side-item.active {
  border-color: rgba(246, 197, 68, 0.4);
  background: linear-gradient(90deg, rgba(246, 197, 68, 0.16), rgba(246, 197, 68, 0.03));
}
.side-item.active .ico { color: var(--gold-bright); }
.side-item.active .txt b { color: var(--gold-bright); }
.side-item.active .txt em { color: rgba(246, 197, 68, 0.6); }

.side-foot { margin-top: 18px; padding: 0 8px; }
.side-foot .line { height: 1px; background: linear-gradient(90deg, rgba(246, 197, 68, 0.28), transparent); margin-bottom: 10px; }
.side-foot p { font-size: 11px; line-height: 1.7; color: #6f6a5e; letter-spacing: 0.04em; }

/* ===== 主内容 ===== */
.main { flex: 1; min-width: 0; }
.pane {
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 22px 22px 16px;
  background: linear-gradient(180deg, rgba(20, 20, 28, 0.92), rgba(10, 10, 16, 0.96));
  box-shadow: 0 0 0 1px rgba(246, 197, 68, 0.05), 0 24px 60px rgba(0, 0, 0, 0.45);
}
.pane-head { margin-bottom: 18px; }
.pane-head.row { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.pane-head h2 {
  font-family: "Cinzel", "Noto Serif JP", serif;
  font-size: 19px;
  letter-spacing: 0.12em;
  color: var(--paper);
}
.pane-head p { font-size: 12px; color: #8a8475; margin-top: 6px; letter-spacing: 0.04em; }

/* ===== 概览 ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.stat-card {
  position: relative;
  border: 1px solid rgba(246, 197, 68, 0.18);
  border-radius: 13px;
  padding: 18px 16px 15px;
  background: radial-gradient(circle at 100% 0%, rgba(246, 197, 68, 0.1), transparent 62%), rgba(255, 255, 255, 0.02);
  cursor: pointer;
  transition: 0.25s;
}
.stat-card.plain { cursor: default; }
.stat-card:not(.plain):hover {
  transform: translateY(-3px);
  border-color: rgba(246, 197, 68, 0.45);
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.4);
}
.stat-ico { font-size: 13px; color: rgba(246, 197, 68, 0.6); margin-bottom: 8px; }
.stat-num {
  font-family: "Cinzel", serif;
  font-weight: 900;
  font-size: 34px;
  line-height: 1;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.stat-label { font-size: 13px; color: #cfc8b8; letter-spacing: 0.08em; margin-top: 8px; }
.stat-hint { font-size: 11px; color: #6f6a5e; margin-top: 5px; line-height: 1.5; }

.tips {
  margin-top: 20px;
  padding: 16px 18px;
  border-radius: 12px;
  border: 1px dashed rgba(63, 224, 242, 0.25);
  background: rgba(63, 224, 242, 0.04);
}
.tips h3 {
  font-size: 12px;
  letter-spacing: 0.16em;
  color: var(--teal-glow);
  margin-bottom: 10px;
  font-weight: 500;
}
.tips ul { list-style: none; display: grid; gap: 7px; }
.tips li { font-size: 12.5px; color: #a8a294; line-height: 1.6; padding-left: 14px; position: relative; }
.tips li::before { content: "·"; position: absolute; left: 2px; color: var(--gold); }
.tips b { color: #d8d1c1; font-weight: 500; }

/* ===== 工具栏（搜索 / 筛选 / 刷新） ===== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.tb-search { position: relative; flex: 1; min-width: 170px; max-width: 320px; }
.tb-search input {
  width: 100%;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(246, 197, 68, 0.22);
  border-radius: 9px;
  padding: 9px 32px 9px 12px;
  color: var(--paper);
  font-family: inherit;
  font-size: 13px;
  outline: none;
  transition: 0.2s;
}
.tb-search input::placeholder { color: #6f6a5e; }
.tb-search input:focus {
  border-color: var(--gold);
  background: rgba(246, 197, 68, 0.05);
  box-shadow: 0 0 0 3px rgba(246, 197, 68, 0.1);
}
.tb-clear {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: #8a8475;
  font-size: 12px;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 6px;
  transition: 0.18s;
}
.tb-clear:hover { color: var(--gold-bright); background: rgba(246, 197, 68, 0.1); }

.tb-ranks { display: flex; gap: 5px; }
.rank-chip {
  border: 1px solid rgba(246, 197, 68, 0.2);
  background: transparent;
  color: #a8a294;
  font-family: "Cinzel", serif;
  font-size: 11.5px;
  letter-spacing: 0.06em;
  padding: 6px 13px;
  border-radius: 999px;
  cursor: pointer;
  transition: 0.2s;
}
.rank-chip:hover { border-color: rgba(246, 197, 68, 0.45); color: var(--gold-bright); }
.rank-chip.active {
  color: #1a1407;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(246, 197, 68, 0.28);
}

.tb-refresh {
  margin-left: auto;
  border: 1px solid rgba(63, 224, 242, 0.4);
  background: transparent;
  color: var(--teal-glow);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.1em;
  padding: 7px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: 0.2s;
}
.tb-refresh:hover { background: rgba(63, 224, 242, 0.12); box-shadow: 0 0 12px rgba(63, 224, 242, 0.2); }
.tb-note { font-size: 11.5px; color: #6f6a5e; }

/* ===== 分页 ===== */
.pager {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid rgba(246, 197, 68, 0.1);
}
.pg-btn {
  min-width: 32px;
  border: 1px solid rgba(246, 197, 68, 0.2);
  background: transparent;
  color: #a8a294;
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.04em;
  padding: 6px 11px;
  border-radius: 7px;
  cursor: pointer;
  transition: 0.18s;
}
.pg-btn.num { font-family: "Cinzel", serif; }
.pg-btn:hover:not(:disabled) {
  border-color: rgba(246, 197, 68, 0.5);
  color: var(--gold-bright);
  background: rgba(246, 197, 68, 0.07);
}
.pg-btn.active {
  color: #1a1407;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
  border-color: transparent;
}
.pg-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.pg-dots { color: #6f6a5e; font-size: 12px; padding: 0 2px; }
.pg-info { margin-left: auto; font-size: 11.5px; color: #6f6a5e; letter-spacing: 0.04em; }

/* ===== 表格 ===== */
.table-wrap { overflow-x: auto; }
.grid { width: 100%; border-collapse: collapse; font-size: 13.5px; }
.grid th {
  text-align: left;
  font-family: "Cinzel", serif;
  font-size: 11px;
  letter-spacing: 0.14em;
  color: var(--teal-glow);
  padding: 10px 12px;
  border-bottom: 1px solid var(--line);
  white-space: nowrap;
}
.grid td { padding: 12px; border-bottom: 1px solid rgba(246, 197, 68, 0.09); color: #cfc8b8; vertical-align: middle; }
.grid tbody tr:hover { background: rgba(246, 197, 68, 0.04); }
.c-id { width: 56px; color: #8a8475; }
.c-rank { width: 72px; }
.c-sort { width: 64px; }
.c-weight { width: 76px; }
.c-key { width: 140px; }
.c-ops { width: 140px; white-space: nowrap; }
.c-desc { color: #a8a294; font-size: 12.5px; line-height: 1.55; max-width: 380px; }
.c-key code {
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-size: 12px;
  color: var(--teal-glow);
  background: rgba(63, 224, 242, 0.08);
  border: 1px solid rgba(63, 224, 242, 0.2);
  padding: 2px 7px;
  border-radius: 5px;
}
.rk {
  display: inline-block;
  font-family: "Cinzel", serif;
  font-weight: 900;
  font-size: 11.5px;
  padding: 3px 11px;
  border-radius: 999px;
  color: #1a1407;
}
.rk-d { background: linear-gradient(180deg, #9fb2c9, #6f8198); }
.rk-c { background: linear-gradient(180deg, #8ee0c8, #3fb89a); }
.rk-b { background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep)); }
.rk-a { background: linear-gradient(180deg, #ff9aa6, var(--crimson)); color: #fff; }
.empty { text-align: center; padding: 34px; color: #8a8475; font-size: 13px; }
.loading { text-align: center; padding: 70px 0; color: #8a8475; letter-spacing: 0.2em; font-size: 13px; }

.op {
  border: 1px solid;
  background: transparent;
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.06em;
  padding: 5px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-right: 7px;
  transition: 0.2s;
}
.op.edit { border-color: rgba(63, 224, 242, 0.5); color: var(--teal-glow); }
.op.edit:hover { background: rgba(63, 224, 242, 0.14); }
.op.del { border-color: rgba(226, 59, 84, 0.5); color: #ff9aa6; }
.op.del:hover { background: rgba(226, 59, 84, 0.16); }

.add-btn {
  border: none;
  cursor: pointer;
  padding: 10px 20px;
  border-radius: 10px;
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 0.1em;
  color: #1a1407;
  white-space: nowrap;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold) 60%, var(--gold-deep));
  box-shadow: 0 8px 20px rgba(246, 197, 68, 0.28);
  transition: transform 0.15s, box-shadow 0.25s;
}
.add-btn:hover { transform: translateY(-2px); box-shadow: 0 12px 26px rgba(246, 197, 68, 0.42); }

/* ===== 提示条 ===== */
.msg {
  margin-bottom: 14px;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 13px;
  letter-spacing: 0.04em;
  opacity: 0;
  transform: translateY(-4px);
  transition: 0.28s;
  pointer-events: none;
  height: 0;
  padding-top: 0;
  padding-bottom: 0;
  overflow: hidden;
}
.msg.show { opacity: 1; transform: none; height: auto; padding: 10px 16px; }
.msg.ok { background: rgba(57, 217, 138, 0.1); border: 1px solid rgba(57, 217, 138, 0.45); color: #9af2c4; }
.msg.err { background: rgba(255, 93, 108, 0.1); border: 1px solid rgba(255, 93, 108, 0.45); color: #ffb3bb; }

/* ===== 编辑弹层 ===== */
.mask {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(4, 4, 8, 0.72);
  backdrop-filter: blur(4px);
}
.editor {
  position: relative;
  width: min(520px, 100%);
  max-height: 88vh;
  overflow-y: auto;
  padding: 22px 22px 18px;
  border-radius: 15px;
  background: linear-gradient(180deg, rgba(26, 26, 34, 0.99), rgba(12, 12, 18, 0.99));
  border: 1px solid rgba(246, 197, 68, 0.32);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.7);
}
.editor .corner { width: 16px; height: 16px; opacity: 0.55; }
.editor .corner.tl { top: 10px; left: 10px; }
.editor .corner.br { bottom: 10px; right: 10px; }
.ed-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.ed-head h3 {
  font-family: "Cinzel", serif;
  font-size: 16px;
  letter-spacing: 0.12em;
  color: var(--gold-bright);
}
.close {
  border: none;
  background: transparent;
  color: #8a8475;
  font-size: 15px;
  cursor: pointer;
  padding: 4px 8px;
  transition: 0.2s;
}
.close:hover { color: var(--gold-bright); }

.editor label { display: block; font-size: 12px; color: #9b9486; margin: 12px 0 6px; letter-spacing: 0.08em; }
.editor .row { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.editor .row label { margin: 12px 4px 0 0; }
.editor input[type="text"],
.editor input[type="number"],
.editor select,
.editor textarea {
  width: 100%;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(246, 197, 68, 0.25);
  border-radius: 9px;
  padding: 10px 12px;
  color: var(--paper);
  font-family: inherit;
  font-size: 13.5px;
  outline: none;
  transition: 0.2s;
}
.editor .row input[type="number"],
.editor .row select { width: 108px; }
.editor input:focus,
.editor select:focus,
.editor textarea:focus {
  border-color: var(--gold);
  background: rgba(246, 197, 68, 0.05);
  box-shadow: 0 0 0 3px rgba(246, 197, 68, 0.1);
}
.editor textarea { resize: vertical; }
.ed-hint { font-size: 11px; color: #6f6a5e; margin-top: 8px; line-height: 1.6; }
.ed-ops { display: flex; gap: 12px; margin-top: 20px; }
.save {
  border: none;
  cursor: pointer;
  padding: 11px 30px;
  border-radius: 10px;
  font-family: "Cinzel", serif;
  font-weight: 700;
  font-size: 13.5px;
  letter-spacing: 0.14em;
  color: #1a1407;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold) 60%, var(--gold-deep));
  box-shadow: 0 8px 20px rgba(246, 197, 68, 0.28);
  transition: transform 0.15s;
}
.save:hover { transform: translateY(-2px); }
.save:disabled { filter: grayscale(0.6) brightness(0.8); cursor: not-allowed; }
.cancel {
  border: 1px solid rgba(246, 197, 68, 0.3);
  background: transparent;
  color: #b7b0a2;
  font-family: inherit;
  font-size: 13.5px;
  padding: 11px 26px;
  border-radius: 10px;
  cursor: pointer;
  transition: 0.2s;
}
.cancel:hover { background: rgba(246, 197, 68, 0.08); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.22s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.credit {
  text-align: center;
  font-size: 11px;
  color: rgba(246, 197, 68, 0.32);
  letter-spacing: 0.1em;
  margin-top: 26px;
}

/* ===== 响应式 ===== */
@media (max-width: 1080px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 820px) {
  .body { flex-direction: column; width: calc(100% - 28px); margin-top: 16px; gap: 14px; }
  .side {
    width: 100%;
    position: static;
    display: flex;
    gap: 6px;
    overflow-x: auto;
    padding: 10px;
  }
  .side-title, .side-foot { display: none; }
  .side-item { width: auto; flex-shrink: 0; margin-bottom: 0; padding: 8px 12px; }
  .side-item .txt em { display: none; }
  .topbar { padding: 12px 16px; }
  .brand .sub { display: none; }
  .brand .divider { display: none; }
  .who { display: none; }
  .pane { padding: 16px 14px 12px; }
  .pane-head.row { flex-direction: column; }
  .grid td.c-desc, .grid th.c-desc { display: none; }
  .tb-search { max-width: none; }
  .pg-info { margin-left: 0; width: 100%; }
}
@media (max-width: 520px) {
  .stat-grid { grid-template-columns: 1fr; }
  .stat-num { font-size: 28px; }
}
</style>
