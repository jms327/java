<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { me, quests, search, navItems, hotKeywords, suggest, questDetail } from './api.js'

const emit = defineEmits(['logout', 'to-admin'])

/* ============================================================================
 * 首页（纯前端）
 * ----------------------------------------------------------------------------
 * 本页只做前端展示与交互，数据全部走 ./api.js 中预留的接口：
 *   /api/me              当前用户
 *   /api/quests          任务列表
 *   /api/search?q=xxx    搜索
 *   /api/nav             导航菜单
 * 后端未就绪时自动回退本地演示数据，页面依然可正常浏览与演示。
 * ========================================================================== */

/* ===== 背景星点（乔斯达家纹） ===== */
const stars = Array.from({ length: 30 }, () => ({
  left: Math.random() * 100 + '%',
  top: Math.random() * 100 + '%',
  delay: Math.random() * 6 + 's',
  scale: (0.6 + Math.random() * 1.4).toFixed(2)
}))

const token = localStorage.getItem('jojo_token') || ''

/* ===== 用户信息 ===== */
const username = ref('替身使者')
const email = ref('')
const initials = computed(() => (username.value || 'J').trim().slice(0, 1).toUpperCase())

const userMenuOpen = ref(false)
const userMenuRef = ref(null)
const searchRef = ref(null)
function onDocClick(e) {
  if (userMenuRef.value && !userMenuRef.value.contains(e.target)) userMenuOpen.value = false
  if (searchRef.value && !searchRef.value.contains(e.target)) suggestOpen.value = false
}

/* ===== 轻提示 ===== */
const hint = ref('')
let hintTimer = null
function notify(text) {
  hint.value = text
  clearTimeout(hintTimer)
  hintTimer = setTimeout(() => (hint.value = ''), 2400)
}
function onPlaceholder(label) {
  userMenuOpen.value = false
  notify('「' + label.replace(/\s/g, '') + '」页面开发中，敬请期待')
}

/* ===== 导航栏 ===== */
const navs = ref([
  { key: 'home', label: '首 页' },
  { key: 'quests', label: '奇妙任务' },
  { key: 'stands', label: '替身图鉴' },
  { key: 'archive', label: '冒险档案' }
])
const activeNav = ref('home')

function onNavClick(item) {
  userMenuOpen.value = false
  if (item.key === 'home') {
    activeNav.value = 'home'
    keyword.value = ''
    list.value = allList.value
    searchMsg.value = ''
    suggestOpen.value = false
    return
  }
  // 其他页面暂不实现，仅做占位提示
  notify('「' + item.label.replace(/\s/g, '') + '」页面开发中，敬请期待')
}

/* ===== 搜索栏 ===== */
const keyword = ref('')
const searching = ref(false)
const searchMsg = ref('')
const suggestOpen = ref(false)
const quickTags = ref(['杜王町', '黄金精神', '命运', 'A'])

const HISTORY_KEY = 'jojo_search_history'
const history = ref([])
try {
  const raw = JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]')
  history.value = Array.isArray(raw) ? raw : []
} catch (e) {
  history.value = []
}
function addHistory(kw) {
  const q = (kw || '').trim()
  if (!q) return
  history.value = [q, ...history.value.filter((h) => h !== q)].slice(0, 8)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
}
function removeHistory(item) {
  history.value = history.value.filter((h) => h !== item)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
}
function clearHistory() {
  history.value = []
  localStorage.removeItem(HISTORY_KEY)
}

/* ===== 列表数据 ===== */
const RANK_TAG = { D: '日常', C: '追踪', B: '试炼', A: '命运' }

// 演示兜底数据：后端接口未就绪时用于展示前端效果
const DEMO_QUESTS = [
  { id: 1, rank: 'D', title: '杜王町的清晨', desc: '在宁静的小镇中确认替身能力的边界，顺便买一份炸鸡块。' },
  { id: 2, rank: 'C', title: '恶人的竞技场', desc: '击败三名虚有其表的替身使者，替杜王町清理隐患。' },
  { id: 3, rank: 'B', title: '黄金精神试炼', desc: '于绝境中守护同伴，证明人类的赞歌就是勇气的赞歌。' },
  { id: 4, rank: 'A', title: '命运之轮', desc: '改写一段既定的命运线，代价是你必须直面自己的过去。' },
  { id: 5, rank: 'C', title: '星尘十字军', desc: '横跨大陆的旅途，用六十天时间找到那条通往宿命的线。' },
  { id: 6, rank: 'B', title: '石之海的低语', desc: '在无法回头的洋流中，寻找自由与觉醒的答案。' }
]

const allList = ref([]) // 原始列表（用于本地筛选、搜索联想）
const list = ref([]) // 当前展示列表
const loading = ref(true)
const demoMode = ref(false)
const rankFilter = ref('ALL')

function normalize(q, i) {
  return {
    id: q && q.id !== undefined ? q.id : 'i' + i,
    rank: ((q && q.rank) || 'D').toUpperCase(),
    title: (q && q.title) || '未命名冒险',
    desc: (q && q.desc) || '暂无描述'
  }
}

const ranks = computed(() => {
  const set = []
  allList.value.forEach((q) => {
    if (!set.includes(q.rank)) set.push(q.rank)
  })
  return ['ALL', ...set.sort()]
})

const viewList = computed(() =>
  rankFilter.value === 'ALL' ? list.value : list.value.filter((q) => q.rank === rankFilter.value)
)

/* ===== 搜索联想：优先后端 /api/search/suggest，失败回退本地过滤 ===== */
const suggestList = ref([])
let suggestTimer = null

function localSuggest(kw) {
  const q = kw.toLowerCase()
  return allList.value
    .filter((x) => x.title.toLowerCase().includes(q) || x.rank.toLowerCase() === q)
    .slice(0, 8)
}

function refreshSuggest() {
  const kw = keyword.value.trim()
  clearTimeout(suggestTimer)
  if (!kw) {
    suggestList.value = []
    return
  }
  // 先用本地结果即时反馈，再异步请求后端覆盖
  suggestList.value = localSuggest(kw)
  suggestTimer = setTimeout(async () => {
    try {
      const res = await suggest(kw, token)
      if (res && res.code === 0 && Array.isArray(res.data) && keyword.value.trim() === kw) {
        suggestList.value = res.data.map(normalize)
      }
    } catch (e) {
      /* 保持本地联想结果 */
    }
  }, 220)
}

function onKeywordInput() {
  suggestOpen.value = true
  refreshSuggest()
}

/* ===== 登录状态失效提示 ===== */
const authExpired = ref(false)
function checkAuth(res) {
  if (res && res.code !== 0 && typeof res.msg === 'string' && res.msg.indexOf('令牌') === 0) {
    authExpired.value = true
  }
}

/* ===== 任务详情弹层 ===== */
const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const detailError = ref('')

async function openDetail(q) {
  detailOpen.value = true
  detailLoading.value = true
  detailError.value = ''
  detail.value = { ...q } // 先用列表数据占位，避免弹层空白
  try {
    const res = await questDetail(q.id, token)
    if (res && res.code === 0 && res.data) {
      detail.value = normalize(res.data)
    } else if (res && res.code === 1) {
      detailError.value = res.msg || '任务不存在或已被移除'
      checkAuth(res)
    }
  } catch (e) {
    /* 接口异常时保留列表中的任务信息，不阻断查看 */
  } finally {
    detailLoading.value = false
  }
}
function closeDetail() {
  detailOpen.value = false
}

/* ===== 拉取任务列表（/api/quests） ===== */
async function loadQuests() {
  loading.value = true
  try {
    const res = await quests(token)
    if (res && res.code === 0 && Array.isArray(res.data) && res.data.length) {
      allList.value = res.data.map(normalize)
      demoMode.value = false
      authExpired.value = false
    } else {
      checkAuth(res)
      allList.value = DEMO_QUESTS.map(normalize)
      demoMode.value = true
    }
  } catch (e) {
    allList.value = DEMO_QUESTS.map(normalize)
    demoMode.value = true
  } finally {
    list.value = allList.value
    loading.value = false
  }
}

/* ===== 搜索（预留 /api/search?q=） ===== */
function localSearch(kw) {
  const q = kw.toLowerCase()
  return allList.value.filter(
    (x) =>
      x.title.toLowerCase().includes(q) ||
      x.desc.toLowerCase().includes(q) ||
      x.rank.toLowerCase() === q
  )
}

function applyResult(rows, q, local) {
  list.value = rows
  rankFilter.value = 'ALL'
  searchMsg.value = rows.length
    ? '找到 ' + rows.length + ' 条与「' + q + '」相关的奇妙冒险' + (local ? '（本地筛选）' : '')
    : '未找到相关冒险，换个关键词试试'
}

async function doSearch(kw) {
  if (kw !== undefined) keyword.value = kw
  const q = keyword.value.trim()
  clearTimeout(suggestTimer)
  suggestOpen.value = false
  suggestList.value = []
  searchMsg.value = ''

  if (!q) {
    list.value = allList.value
    rankFilter.value = 'ALL'
    return
  }

  addHistory(q)
  searching.value = true
  try {
    const res = await search(q, token)
    if (res && res.code === 0 && Array.isArray(res.data)) {
      applyResult(res.data.map(normalize), q, false)
    } else {
      checkAuth(res)
      applyResult(localSearch(q), q, true)
    }
  } catch (e) {
    applyResult(localSearch(q), q, true)
  } finally {
    searching.value = false
  }
}

function clearKeyword() {
  keyword.value = ''
  list.value = allList.value
  rankFilter.value = 'ALL'
  searchMsg.value = ''
  suggestOpen.value = false
  clearTimeout(suggestTimer)
  suggestList.value = []
}

/* ===== 其他交互 ===== */
function onKeydown(e) {
  if (e.key === 'Escape') {
    if (detailOpen.value) detailOpen.value = false
    suggestOpen.value = false
  }
}

function onLogout() {
  userMenuOpen.value = false
  localStorage.removeItem('jojo_token')
  emit('logout')
}

onMounted(async () => {
  document.addEventListener('click', onDocClick)
  document.addEventListener('keydown', onKeydown)

  try {
    const res = await me(token)
    if (res && res.code === 0 && res.data) {
      username.value = res.data.username || username.value
      email.value = res.data.email || ''
    } else {
      checkAuth(res)
    }
  } catch (e) {
    /* 保持默认称呼 */
  }

  try {
    const res = await navItems(token)
    if (res && res.code === 0 && Array.isArray(res.data) && res.data.length) navs.value = res.data
  } catch (e) {
    /* 使用内置导航 */
  }

  // 热门关键词（后端 /api/hot-keywords，失败保留内置）
  try {
    const res = await hotKeywords()
    if (res && res.code === 0 && Array.isArray(res.data) && res.data.length) quickTags.value = res.data
  } catch (e) {
    /* 使用内置热门词 */
  }

  loadQuests()
})

onUnmounted(() => {
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('keydown', onKeydown)
  clearTimeout(hintTimer)
})
</script>

<template>
  <div class="home">
    <!-- 背景星点 -->
    <div class="stars">
      <div
        v-for="(s, i) in stars"
        :key="i"
        class="star"
        :style="{ left: s.left, top: s.top, animationDelay: s.delay, transform: 'scale(' + s.scale + ')' }"
      ></div>
    </div>

    <!-- ======================= 导航栏 ======================= -->
    <header class="navbar">
      <div class="nav-brand" @click="onNavClick({ key: 'home', label: '首页' })">
        <span class="jp">ジョジョの奇妙な冒険</span>
        <span class="logo">JOJO</span>
      </div>

      <nav class="nav-links">
        <button
          v-for="item in navs"
          :key="item.key"
          type="button"
          class="nav-link"
          :class="{ active: activeNav === item.key }"
          @click="onNavClick(item)"
        >
          {{ item.label }}
        </button>
      </nav>

      <div class="nav-right">
        <button
          type="button"
          class="nav-admin"
          title="进入后台管理系统"
          aria-label="进入后台管理系统"
          @click="emit('to-admin')"
        >
          <svg class="na-ico" viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M12 15.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Z"
              fill="none"
              stroke="currentColor"
              stroke-width="1.6"
            />
            <path
              d="M19.4 13a7.6 7.6 0 0 0 0-2l1.7-1.3-1.7-3-2 .8a7.5 7.5 0 0 0-1.7-1L15.4 4h-3.5l-.3 2.5a7.5 7.5 0 0 0-1.7 1l-2-.8-1.7 3L6.6 11a7.6 7.6 0 0 0 0 2l-1.7 1.3 1.7 3 2-.8c.5.4 1.1.7 1.7 1l.3 2.5h3.5l.3-2.5c.6-.3 1.2-.6 1.7-1l2 .8 1.7-3L19.4 13Z"
              fill="none"
              stroke="currentColor"
              stroke-width="1.4"
              stroke-linejoin="round"
            />
          </svg>
          <span class="na-text">后台管理系统</span>
        </button>

        <div
          ref="userMenuRef"
          class="user-chip"
          :class="{ open: userMenuOpen }"
          @click.stop="userMenuOpen = !userMenuOpen"
        >
          <span class="avatar">{{ initials }}</span>
          <span class="name">{{ username }}</span>
          <span class="caret">▾</span>

          <div class="user-menu" v-show="userMenuOpen" @click.stop>
            <div class="um-head">
              <b>{{ username }}</b>
              <span>{{ email || '替身使者' }}</span>
            </div>
            <button type="button" @click="onPlaceholder('个人中心')">个人中心</button>
            <button type="button" @click="onPlaceholder('我的任务')">我的任务</button>
            <button type="button" @click="emit('to-admin')">后台管理</button>
            <button type="button" class="danger" @click="onLogout">退出登录</button>
          </div>
        </div>

        <button type="button" class="logout" @click="onLogout">退 出</button>
      </div>
    </header>

    <!-- 轻提示 -->
    <div class="hint-toast" :class="{ show: !!hint }">{{ hint }}</div>

    <!-- 登录状态失效提示 -->
    <div v-if="authExpired" class="auth-banner">
      <span class="ab-text">⚠ 登录状态已失效，当前展示的是演示数据</span>
      <button type="button" class="ab-btn" @click="onLogout">重新登录 →</button>
    </div>

    <!-- ======================= 搜索区 ======================= -->
    <section class="hero">
      <div class="hero-jp">スタープラチナ・ザ・ワールド</div>
      <h1 class="hero-title">奇妙冒险 · 由此开始</h1>
      <p class="hero-sub">搜索任务、替身与命运之线 —— 人类的赞歌就是勇气的赞歌</p>

      <form ref="searchRef" class="searchbar" @submit.prevent="doSearch()">
        <span class="corner tl"></span><span class="corner tr"></span>
        <span class="corner bl"></span><span class="corner br"></span>

        <span class="search-star">★</span>
        <input
          v-model="keyword"
          type="text"
          placeholder="输入关键词，寻找你的奇妙冒险…"
          autocomplete="off"
          @focus="suggestOpen = true"
          @input="onKeywordInput"
          @keydown.esc="suggestOpen = false"
        />
        <button v-if="keyword" type="button" class="clear-btn" @click="clearKeyword">✕</button>
        <button class="search-btn" type="submit" :disabled="searching">
          {{ searching ? '探索中…' : '搜 索' }}
        </button>

        <!-- 联想 / 搜索历史 -->
        <div class="suggest" v-show="suggestOpen">
          <template v-if="keyword.trim()">
            <div class="sg-head">联想结果</div>
            <button
              v-for="s in suggestList"
              :key="s.id"
              type="button"
              class="sg-item"
              @click="doSearch(s.title)"
            >
              <span class="sg-rank">{{ s.rank }}</span>
              <span class="sg-title">{{ s.title }}</span>
            </button>
            <div v-if="!suggestList.length" class="sg-empty">没有匹配的冒险，按 Enter 直接搜索</div>
          </template>
          <template v-else>
            <div class="sg-head">
              搜索历史
              <button v-if="history.length" type="button" class="sg-clear" @click="clearHistory">清空</button>
            </div>
            <div v-if="history.length" class="sg-tags">
              <span v-for="h in history" :key="h" class="sg-tag">
                <b @click="doSearch(h)">{{ h }}</b>
                <i @click="removeHistory(h)">✕</i>
              </span>
            </div>
            <div v-else class="sg-empty">还没有搜索记录，试试下面的热门关键词</div>
          </template>
        </div>
      </form>

      <div class="quick-tags">
        <span class="qt-label">热门：</span>
        <button v-for="t in quickTags" :key="t" type="button" class="qt" @click="doSearch(t)">{{ t }}</button>
      </div>

      <div class="search-msg" :class="{ show: !!searchMsg }">{{ searchMsg }}</div>

      <div class="stats">
        <div class="stat">
          <b>{{ allList.length }}</b>
          <span>冒险任务</span>
        </div>
        <i class="stat-sep"></i>
        <div class="stat">
          <b>{{ demoMode ? '—' : 'LIVE' }}</b>
          <span>数据来源</span>
        </div>
        <i class="stat-sep"></i>
        <div class="stat">
          <b>{{ ranks.length - 1 }}</b>
          <span>难度等级</span>
        </div>
      </div>
    </section>

    <!-- ======================= 内容区 ======================= -->
    <section class="content">
      <div class="section-head">
        <h2>奇妙冒险任务</h2>
        <span class="line"></span>
        <span class="rank-hint">RANK D → A</span>
      </div>

      <div class="filters">
        <button
          v-for="r in ranks"
          :key="r"
          type="button"
          class="filter"
          :class="{ active: rankFilter === r }"
          @click="rankFilter = r"
        >
          {{ r === 'ALL' ? '全部' : 'RANK ' + r }}
        </button>
        <span v-if="demoMode" class="demo-tip">演示数据 · 接口就绪后自动切换</span>
      </div>

      <div v-if="loading" class="grid">
        <div v-for="n in 4" :key="n" class="quest-card skeleton">
          <span class="sk sk-rank"></span>
          <span class="sk sk-title"></span>
          <span class="sk sk-line"></span>
          <span class="sk sk-line short"></span>
        </div>
      </div>

      <div v-else-if="viewList.length" class="grid">
        <article v-for="q in viewList" :key="q.id" class="quest-card">
          <span class="corner tl"></span><span class="corner br"></span>
          <div class="card-top">
            <span class="rank">{{ q.rank }}</span>
            <span class="tag">{{ RANK_TAG[q.rank] || '冒险' }}</span>
          </div>
          <h3>{{ q.title }}</h3>
          <p>{{ q.desc }}</p>
          <div class="card-foot">
            <span class="star-deco">★ ★ ★</span>
            <button type="button" class="accept" @click="openDetail(q)">查看详情 →</button>
          </div>
        </article>
      </div>

      <div v-else class="empty">
        <div class="empty-ora">オラオラ…</div>
        <p>这里空空如也，去搜索栏寻找你的命运吧</p>
        <button type="button" class="reset" @click="clearKeyword">重置筛选</button>
      </div>
    </section>

    <div class="credit">— 黄金精神 · GOLDEN SPIRIT —</div>

    <!-- ======================= 任务详情弹层 ======================= -->
    <div class="detail-mask" :class="{ show: detailOpen }" @click="closeDetail">
      <div class="detail-card" @click.stop>
        <span class="corner tl"></span><span class="corner tr"></span>
        <span class="corner bl"></span><span class="corner br"></span>
        <button type="button" class="detail-close" @click="closeDetail">✕</button>

        <div class="detail-jp">TASK DOSSIER</div>

        <div v-if="detail" class="detail-body">
          <div class="card-top">
            <span class="rank">{{ detail.rank }}</span>
            <span class="tag">{{ RANK_TAG[detail.rank] || '冒险' }}</span>
            <span v-if="detailLoading" class="detail-loading">同步替身中枢…</span>
          </div>
          <h3>{{ detail.title }}</h3>
          <p class="detail-desc">{{ detail.desc }}</p>
          <div v-if="detailError" class="detail-error">{{ detailError }}</div>
        </div>

        <div class="detail-foot">
          <span class="star-deco">★ ★ ★</span>
          <button type="button" class="detail-ok" @click="closeDetail">关 闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  position: relative;
  width: 100%;
  min-height: 100vh;
  z-index: 1;
  padding-bottom: 60px;
  animation: rise 0.7s cubic-bezier(0.2, 0.8, 0.2, 1) both;
}
@keyframes rise {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: none; }
}

/* ======================= 导航栏 ======================= */
.navbar {
  position: sticky;
  top: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  gap: 26px;
  padding: 13px 32px;
  background: linear-gradient(180deg, rgba(16, 16, 24, 0.95), rgba(10, 10, 16, 0.88));
  border-bottom: 1px solid rgba(246, 197, 68, 0.22);
  backdrop-filter: blur(14px);
  box-shadow: 0 10px 34px rgba(0, 0, 0, 0.5);
}
.navbar::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(246, 197, 68, 0.6), transparent);
}
.nav-brand {
  display: flex;
  flex-direction: column;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}
.nav-brand .jp {
  font-size: 9.5px;
  letter-spacing: 0.26em;
  color: var(--teal-glow);
  text-shadow: 0 0 10px rgba(63, 224, 242, 0.45);
}
.nav-brand .logo {
  font-family: "Cinzel", serif;
  font-weight: 900;
  font-size: 25px;
  line-height: 1.05;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold) 55%, var(--gold-deep));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  filter: drop-shadow(0 0 12px rgba(246, 197, 68, 0.3));
}
.nav-links {
  display: flex;
  gap: 4px;
  flex: 1;
  min-width: 0;
}
.nav-link {
  border: 1px solid transparent;
  cursor: pointer;
  background: transparent;
  color: #b7b0a2;
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 0.1em;
  padding: 9px 17px;
  border-radius: 999px;
  white-space: nowrap;
  transition: 0.25s;
}
.nav-link:hover {
  color: var(--gold-bright);
  border-color: rgba(246, 197, 68, 0.28);
  background: rgba(246, 197, 68, 0.06);
}
.nav-link.active {
  color: #1a1407;
  font-weight: 700;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold) 60%, var(--gold-deep));
  box-shadow: 0 6px 16px rgba(246, 197, 68, 0.28);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.nav-admin {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  border: 1px solid rgba(246, 197, 68, 0.5);
  background: linear-gradient(180deg, rgba(246, 197, 68, 0.18), rgba(246, 197, 68, 0.06));
  color: var(--gold-bright);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.06em;
  padding: 7px 14px;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
  transition: 0.25s;
}
.nav-admin .na-ico {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  opacity: 0.9;
}
.nav-admin:hover {
  background: linear-gradient(180deg, rgba(246, 197, 68, 0.32), rgba(246, 197, 68, 0.14));
  box-shadow: 0 0 16px rgba(246, 197, 68, 0.35);
  transform: translateY(-1px);
}

.user-chip {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px 5px 5px;
  border-radius: 999px;
  border: 1px solid rgba(246, 197, 68, 0.2);
  background: rgba(255, 255, 255, 0.03);
  cursor: pointer;
  user-select: none;
  transition: 0.25s;
}
.user-chip:hover,
.user-chip.open {
  border-color: rgba(246, 197, 68, 0.5);
  background: rgba(246, 197, 68, 0.08);
}
.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Cinzel", serif;
  font-weight: 900;
  font-size: 14px;
  color: #1a1407;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
  box-shadow: 0 0 12px rgba(246, 197, 68, 0.35);
}
.user-chip .name {
  font-size: 13px;
  color: var(--paper);
  letter-spacing: 0.06em;
  max-width: 108px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-chip .caret {
  font-size: 10px;
  color: #8a8475;
  transition: transform 0.25s;
}
.user-chip.open .caret { transform: rotate(180deg); }

.user-menu {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 208px;
  padding: 8px;
  border-radius: 12px;
  background: linear-gradient(180deg, rgba(22, 22, 32, 0.99), rgba(10, 10, 16, 0.99));
  border: 1px solid rgba(246, 197, 68, 0.3);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.6);
  animation: rise 0.22s both;
  z-index: 50;
}
.um-head {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 10px 10px;
  border-bottom: 1px dashed rgba(246, 197, 68, 0.2);
  margin-bottom: 6px;
}
.um-head b {
  font-size: 13.5px;
  color: var(--gold-bright);
  letter-spacing: 0.06em;
}
.um-head span {
  font-size: 11.5px;
  color: #8a8475;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-menu button {
  display: block;
  width: 100%;
  text-align: left;
  border: none;
  background: transparent;
  color: #c9c2b2;
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 0.06em;
  padding: 9px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.2s;
}
.user-menu button:hover {
  background: rgba(246, 197, 68, 0.1);
  color: var(--gold-bright);
}
.user-menu button.danger { color: #ff9aa6; }
.user-menu button.danger:hover {
  background: rgba(226, 59, 84, 0.16);
  color: #ffb3bb;
}

.logout {
  border: 1px solid rgba(226, 59, 84, 0.45);
  background: rgba(226, 59, 84, 0.1);
  color: #ff9aa6;
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.12em;
  padding: 7px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: 0.25s;
}
.logout:hover {
  background: rgba(226, 59, 84, 0.22);
  box-shadow: 0 0 14px rgba(226, 59, 84, 0.28);
}

.hint-toast {
  position: fixed;
  top: 76px;
  left: 50%;
  transform: translateX(-50%) translateY(-10px);
  z-index: 45;
  padding: 9px 20px;
  border-radius: 999px;
  font-size: 12.5px;
  letter-spacing: 0.06em;
  color: var(--gold-bright);
  background: rgba(20, 20, 28, 0.96);
  border: 1px solid var(--line);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.55);
  opacity: 0;
  pointer-events: none;
  transition: 0.3s;
}
.hint-toast.show {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

/* ======================= 搜索区 ======================= */
.hero {
  text-align: center;
  padding: 66px 20px 14px;
}
.hero-jp {
  font-size: 12px;
  letter-spacing: 0.4em;
  color: var(--teal-glow);
  text-shadow: 0 0 12px rgba(63, 224, 242, 0.45);
}
.hero-title {
  font-family: "Cinzel", "Noto Serif JP", serif;
  font-weight: 900;
  font-size: 40px;
  margin: 12px 0 10px;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold) 55%, var(--gold-deep));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  letter-spacing: 0.06em;
  filter: drop-shadow(0 3px 0 rgba(0, 0, 0, 0.35)) drop-shadow(0 0 24px rgba(246, 197, 68, 0.28));
}
.hero-sub {
  font-size: 13.5px;
  color: #9b9486;
  letter-spacing: 0.14em;
  margin-bottom: 32px;
}

.searchbar {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  width: min(660px, 100%);
  margin: 0 auto;
  padding: 8px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(20, 20, 28, 0.94), rgba(10, 10, 16, 0.97));
  border: 1px solid var(--line);
  box-shadow: 0 0 0 1px rgba(246, 197, 68, 0.06), 0 22px 54px rgba(0, 0, 0, 0.5),
    0 0 44px rgba(246, 197, 68, 0.06);
  transition: border-color 0.25s, box-shadow 0.25s;
}
.searchbar:focus-within {
  border-color: rgba(246, 197, 68, 0.7);
  box-shadow: 0 0 0 3px rgba(246, 197, 68, 0.1), 0 22px 54px rgba(0, 0, 0, 0.5);
}
.search-star {
  padding-left: 12px;
  color: var(--teal-glow);
  font-size: 15px;
  text-shadow: 0 0 10px rgba(63, 224, 242, 0.55);
}
.searchbar input {
  flex: 1;
  min-width: 0;
  background: transparent;
  border: none;
  outline: none;
  color: var(--paper);
  font-family: inherit;
  font-size: 15px;
  letter-spacing: 0.04em;
  padding: 12px 4px;
}
.searchbar input::placeholder { color: #6f6a5d; }

.clear-btn {
  border: none;
  background: transparent;
  color: #7d776a;
  font-size: 12px;
  cursor: pointer;
  padding: 6px;
  border-radius: 50%;
  transition: 0.2s;
}
.clear-btn:hover { color: var(--paper); background: rgba(255, 255, 255, 0.06); }

.search-btn {
  border: none;
  cursor: pointer;
  padding: 12px 30px;
  border-radius: 11px;
  font-family: "Cinzel", serif;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.2em;
  color: #1a1407;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold) 60%, var(--gold-deep));
  box-shadow: 0 8px 20px rgba(246, 197, 68, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.5);
  transition: transform 0.15s, box-shadow 0.25s;
}
.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px rgba(246, 197, 68, 0.42), inset 0 1px 0 rgba(255, 255, 255, 0.6);
}
.search-btn:disabled {
  filter: grayscale(0.6) brightness(0.8);
  cursor: not-allowed;
  transform: none;
}

/* 联想面板 */
.suggest {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  z-index: 35;
  text-align: left;
  padding: 10px;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(22, 22, 32, 0.99), rgba(10, 10, 16, 0.99));
  border: 1px solid rgba(246, 197, 68, 0.3);
  box-shadow: 0 22px 50px rgba(0, 0, 0, 0.62);
  animation: rise 0.22s both;
}
.sg-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  letter-spacing: 0.18em;
  color: #8a8475;
  padding: 4px 8px 8px;
}
.sg-clear {
  border: none;
  background: transparent;
  color: var(--teal-glow);
  font-family: inherit;
  font-size: 11px;
  cursor: pointer;
  letter-spacing: 0.06em;
}
.sg-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  border: none;
  background: transparent;
  color: #cfc8b8;
  font-family: inherit;
  font-size: 13px;
  text-align: left;
  padding: 9px 10px;
  border-radius: 9px;
  cursor: pointer;
  transition: 0.2s;
}
.sg-item:hover {
  background: rgba(246, 197, 68, 0.1);
  color: var(--gold-bright);
}
.sg-rank {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-family: "Cinzel", serif;
  font-weight: 900;
  font-size: 11px;
  color: #1a1407;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
}
.sg-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sg-empty {
  padding: 12px 10px 14px;
  font-size: 12.5px;
  color: #7d776a;
  letter-spacing: 0.04em;
}
.sg-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 2px 8px 8px;
}
.sg-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 6px 5px 12px;
  border-radius: 999px;
  border: 1px solid rgba(246, 197, 68, 0.25);
  background: rgba(246, 197, 68, 0.06);
}
.sg-tag b {
  font-weight: 500;
  font-size: 12.5px;
  color: #cfc8b8;
  cursor: pointer;
}
.sg-tag b:hover { color: var(--gold-bright); }
.sg-tag i {
  font-style: normal;
  font-size: 10px;
  color: #7d776a;
  cursor: pointer;
  padding: 0 3px;
}
.sg-tag i:hover { color: var(--err); }

.corner {
  position: absolute;
  width: 18px;
  height: 18px;
  border: 2px solid var(--gold);
  opacity: 0.55;
  pointer-events: none;
}
.corner.tl { top: -6px; left: -6px; border-right: 0; border-bottom: 0; }
.corner.tr { top: -6px; right: -6px; border-left: 0; border-bottom: 0; }
.corner.bl { bottom: -6px; left: -6px; border-right: 0; border-top: 0; }
.corner.br { bottom: -6px; right: -6px; border-left: 0; border-top: 0; }

.quick-tags {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.qt-label {
  font-size: 12px;
  color: #8a8475;
  letter-spacing: 0.1em;
}
.qt {
  cursor: pointer;
  font-family: inherit;
  font-size: 12.5px;
  letter-spacing: 0.08em;
  color: var(--teal-glow);
  padding: 5px 14px;
  border-radius: 999px;
  border: 1px solid rgba(63, 224, 242, 0.35);
  background: rgba(31, 182, 201, 0.08);
  transition: 0.25s;
}
.qt:hover {
  background: rgba(31, 182, 201, 0.18);
  box-shadow: 0 0 14px rgba(63, 224, 242, 0.22);
}

.search-msg {
  margin-top: 14px;
  font-size: 12.5px;
  color: var(--gold-bright);
  letter-spacing: 0.06em;
  opacity: 0;
  transition: opacity 0.3s;
}
.search-msg.show { opacity: 1; }

.stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 26px;
  margin: 30px auto 0;
  padding: 16px 26px;
  width: fit-content;
  border-radius: 14px;
  border: 1px solid rgba(246, 197, 68, 0.16);
  background: rgba(255, 255, 255, 0.02);
}
.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
}
.stat b {
  font-family: "Cinzel", serif;
  font-size: 20px;
  color: var(--gold-bright);
  letter-spacing: 0.06em;
  text-shadow: 0 0 14px rgba(246, 197, 68, 0.3);
}
.stat span {
  font-size: 11px;
  letter-spacing: 0.16em;
  color: #8a8475;
}
.stat-sep {
  width: 1px;
  height: 26px;
  background: linear-gradient(180deg, transparent, rgba(246, 197, 68, 0.35), transparent);
}

/* ======================= 内容区 ======================= */
.content {
  width: min(1080px, calc(100% - 40px));
  margin: 40px auto 40px;
}
.section-head {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 16px;
}
.section-head h2 {
  font-family: "Cinzel", "Noto Serif JP", serif;
  font-size: 20px;
  letter-spacing: 0.16em;
  color: var(--paper);
}
.section-head .line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, rgba(246, 197, 68, 0.4), transparent);
}
.rank-hint {
  font-family: "Cinzel", serif;
  font-size: 11px;
  letter-spacing: 0.2em;
  color: var(--teal-glow);
}

.filters {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.filter {
  border: 1px solid rgba(246, 197, 68, 0.22);
  background: transparent;
  color: #9b9486;
  font-family: "Cinzel", serif;
  font-size: 11.5px;
  letter-spacing: 0.14em;
  padding: 6px 15px;
  border-radius: 999px;
  cursor: pointer;
  transition: 0.22s;
}
.filter:hover {
  color: var(--gold-bright);
  border-color: rgba(246, 197, 68, 0.5);
}
.filter.active {
  color: #1a1407;
  font-weight: 700;
  border-color: transparent;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
  box-shadow: 0 5px 14px rgba(246, 197, 68, 0.25);
}
.demo-tip {
  margin-left: auto;
  font-size: 11px;
  letter-spacing: 0.08em;
  color: rgba(63, 224, 242, 0.7);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(246px, 1fr));
  gap: 18px;
}
.quest-card {
  position: relative;
  padding: 20px 20px 15px;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(20, 20, 28, 0.94), rgba(10, 10, 16, 0.97));
  border: 1px solid rgba(246, 197, 68, 0.2);
  transition: transform 0.25s, box-shadow 0.25s, border-color 0.25s;
  overflow: hidden;
}
.quest-card::after {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(420px 200px at 100% 0%, rgba(246, 197, 68, 0.08), transparent 65%);
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}
.quest-card:hover {
  transform: translateY(-4px);
  border-color: var(--gold);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.5), 0 0 26px rgba(246, 197, 68, 0.12);
}
.quest-card:hover::after { opacity: 1; }
.quest-card .corner {
  width: 14px;
  height: 14px;
  opacity: 0.45;
  transition: opacity 0.25s;
}
.quest-card .corner.tl { top: 8px; left: 8px; }
.quest-card .corner.br { bottom: 8px; right: 8px; }
.quest-card:hover .corner { opacity: 0.9; }

.card-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 11px;
}
.rank {
  display: inline-block;
  font-family: "Cinzel", serif;
  font-weight: 900;
  font-size: 12.5px;
  color: #1a1407;
  padding: 3px 11px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
  box-shadow: 0 4px 12px rgba(246, 197, 68, 0.26);
}
.tag {
  font-size: 11px;
  letter-spacing: 0.1em;
  color: var(--teal-glow);
  padding: 2px 9px;
  border-radius: 999px;
  border: 1px solid rgba(63, 224, 242, 0.28);
  background: rgba(31, 182, 201, 0.07);
}
.quest-card h3 {
  font-size: 16.5px;
  color: var(--gold-bright);
  letter-spacing: 0.08em;
  margin-bottom: 8px;
}
.quest-card p {
  font-size: 12.5px;
  line-height: 1.85;
  color: #b7b0a2;
  min-height: 46px;
}
.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed rgba(246, 197, 68, 0.18);
}
.star-deco {
  font-size: 10px;
  letter-spacing: 0.3em;
  color: rgba(246, 197, 68, 0.45);
}
.accept {
  border: none;
  cursor: pointer;
  background: transparent;
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.1em;
  color: var(--teal-glow);
  transition: 0.2s;
}
.accept:hover { text-shadow: 0 0 10px rgba(63, 224, 242, 0.7); }

/* 骨架屏 */
.skeleton { display: flex; flex-direction: column; gap: 10px; }
.sk {
  display: block;
  border-radius: 6px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.04), rgba(246, 197, 68, 0.1), rgba(255, 255, 255, 0.04));
  background-size: 200% 100%;
  animation: shimmer 1.4s linear infinite;
}
@keyframes shimmer {
  from { background-position: 200% 0; }
  to { background-position: -200% 0; }
}
.sk-rank { width: 52px; height: 20px; border-radius: 999px; }
.sk-title { width: 70%; height: 18px; }
.sk-line { width: 100%; height: 11px; }
.sk-line.short { width: 60%; }

.empty {
  text-align: center;
  padding: 62px 0 70px;
  color: #8a8475;
}
.empty-ora {
  font-size: 26px;
  font-weight: 900;
  color: var(--crimson);
  text-shadow: 0 0 14px rgba(226, 59, 84, 0.45);
  margin-bottom: 12px;
  letter-spacing: 0.1em;
}
.empty p { font-size: 13px; letter-spacing: 0.08em; }
.reset {
  margin-top: 18px;
  border: 1px solid var(--line);
  background: rgba(246, 197, 68, 0.08);
  color: var(--gold-bright);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.14em;
  padding: 8px 20px;
  border-radius: 999px;
  cursor: pointer;
  transition: 0.25s;
}
.reset:hover { background: rgba(246, 197, 68, 0.18); }

/* ======================= 登录失效提示 ======================= */
.auth-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
  margin: 0 auto;
  padding: 10px 20px;
  width: fit-content;
  max-width: calc(100% - 32px);
  border-radius: 999px;
  border: 1px solid rgba(226, 59, 84, 0.45);
  background: rgba(226, 59, 84, 0.1);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
}
.ab-text {
  font-size: 12.5px;
  letter-spacing: 0.06em;
  color: #ffb3bb;
}
.ab-btn {
  border: none;
  cursor: pointer;
  background: transparent;
  font-family: inherit;
  font-size: 12.5px;
  letter-spacing: 0.1em;
  color: var(--gold-bright);
  text-decoration: underline;
  text-underline-offset: 3px;
}

/* ======================= 任务详情弹层 ======================= */
.detail-mask {
  position: fixed;
  inset: 0;
  z-index: 60;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: radial-gradient(circle at center, rgba(20, 20, 28, 0.82), rgba(5, 5, 10, 0.96));
  backdrop-filter: blur(6px);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s;
}
.detail-mask.show {
  opacity: 1;
  pointer-events: auto;
}
.detail-card {
  position: relative;
  width: min(520px, 100%);
  padding: 30px 30px 20px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(20, 20, 28, 0.98), rgba(10, 10, 16, 0.99));
  border: 1px solid var(--line);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.6), 0 0 60px rgba(246, 197, 68, 0.08);
  transform: translateY(14px) scale(0.98);
  transition: transform 0.32s cubic-bezier(0.2, 0.9, 0.2, 1);
}
.detail-mask.show .detail-card {
  transform: none;
}
.detail-card .corner { width: 18px; height: 18px; opacity: 0.6; }
.detail-card .corner.tl { top: 10px; left: 10px; }
.detail-card .corner.tr { top: 10px; right: 10px; }
.detail-card .corner.bl { bottom: 10px; left: 10px; }
.detail-card .corner.br { bottom: 10px; right: 10px; }
.detail-close {
  position: absolute;
  top: 12px;
  right: 40px;
  border: none;
  background: transparent;
  color: #7d776a;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 6px;
  transition: 0.2s;
}
.detail-close:hover { color: var(--paper); background: rgba(255, 255, 255, 0.06); }
.detail-jp {
  font-family: "Cinzel", serif;
  font-size: 10.5px;
  letter-spacing: 0.34em;
  color: var(--teal-glow);
  text-shadow: 0 0 10px rgba(63, 224, 242, 0.4);
  margin-bottom: 16px;
}
.detail-body .card-top { margin-bottom: 14px; }
.detail-body h3 {
  font-family: "Cinzel", "Noto Serif JP", serif;
  font-size: 22px;
  letter-spacing: 0.08em;
  color: var(--gold-bright);
  margin-bottom: 12px;
}
.detail-desc {
  font-size: 13.5px;
  line-height: 2;
  color: #c2bbab;
  letter-spacing: 0.04em;
}
.detail-loading {
  margin-left: auto;
  font-size: 11px;
  letter-spacing: 0.12em;
  color: rgba(63, 224, 242, 0.75);
  animation: blink 1.2s ease-in-out infinite;
}
@keyframes blink {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}
.detail-error {
  margin-top: 14px;
  padding: 9px 12px;
  border-radius: 9px;
  font-size: 12px;
  letter-spacing: 0.04em;
  color: #ffb3bb;
  background: rgba(255, 93, 108, 0.1);
  border: 1px solid rgba(255, 93, 108, 0.35);
}
.detail-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 22px;
  padding-top: 14px;
  border-top: 1px dashed rgba(246, 197, 68, 0.2);
}
.detail-ok {
  border: none;
  cursor: pointer;
  padding: 10px 30px;
  border-radius: 10px;
  font-family: "Cinzel", serif;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.18em;
  color: #1a1407;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold) 60%, var(--gold-deep));
  box-shadow: 0 8px 20px rgba(246, 197, 68, 0.26);
  transition: transform 0.15s, box-shadow 0.25s;
}
.detail-ok:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px rgba(246, 197, 68, 0.4);
}

/* ======================= 响应式 ======================= */
@media (max-width: 900px) {
  .navbar { gap: 14px; padding: 12px 18px; }
  .user-chip .name { display: none; }
  .nav-admin { padding: 7px 10px; }
  .nav-admin .na-text { display: none; }
}
@media (max-width: 720px) {
  .navbar { flex-wrap: wrap; gap: 10px; padding: 11px 14px; }
  .nav-links { order: 3; width: 100%; overflow-x: auto; padding-bottom: 2px; }
  .nav-links::-webkit-scrollbar { height: 0; }
  .nav-link { padding: 8px 14px; font-size: 13px; }
  .nav-right { margin-left: auto; }
  .hero { padding: 44px 16px 10px; }
  .hero-title { font-size: 27px; }
  .hero-sub { font-size: 12px; letter-spacing: 0.08em; }
  .searchbar { flex-wrap: wrap; }
  .search-star { display: none; }
  .searchbar input { padding: 12px 10px; }
  .search-btn { width: 100%; }
  .stats { gap: 18px; padding: 14px 18px; width: 100%; }
  .content { width: calc(100% - 28px); margin-top: 28px; }
  .demo-tip { margin-left: 0; }
}
</style>
