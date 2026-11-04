<script setup>
import { ref, onMounted } from 'vue'
import { me, quests, search, navItems } from './api.js'

const emit = defineEmits(['logout', 'to-admin'])

// ===== 跳转后台管理系统 =====
function onAdmin() {
  emit('to-admin')
}

// ===== 背景星点（乔斯达家纹） =====
const stars = Array.from({ length: 26 }, () => ({
  left: Math.random() * 100 + '%',
  top: Math.random() * 100 + '%',
  delay: Math.random() * 6 + 's',
  scale: (0.6 + Math.random() * 1.4).toFixed(2)
}))

const token = localStorage.getItem('jojo_token') || ''

// ===== 用户信息 =====
const username = ref('替身使者')

// ===== 导航栏 =====
const navs = ref([
  { key: 'home', label: '首 页' },
  { key: 'quests', label: '奇妙任务' },
  { key: 'stands', label: '替身图鉴' },
  { key: 'archive', label: '冒险档案' }
])
const activeNav = ref('home')
// 其他页面暂未实现，先占位提示
const navToast = ref('')
let navToastTimer = null
function onNavClick(item) {
  activeNav.value = item.key
  if (item.key !== 'home') {
    navToast.value = '「' + item.label.replace(/\s/g, '') + '」页面开发中，敬请期待'
    clearTimeout(navToastTimer)
    navToastTimer = setTimeout(() => (navToast.value = ''), 2500)
  } else {
    navToast.value = ''
  }
}

// ===== 搜索栏 =====
const keyword = ref('')
const searching = ref(false)
const searchMsg = ref('')
const quickTags = ['杜王町', '黄金精神', '命运', 'A']

// ===== 内容列表（任务卡片，数据走预留接口） =====
const list = ref([])
const loading = ref(true)

async function loadQuests() {
  loading.value = true
  try {
    const res = await quests(token)
    if (res && res.code === 0) list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function onSearch(kw) {
  if (kw !== undefined) keyword.value = kw
  searching.value = true
  searchMsg.value = ''
  try {
    const res = await search(keyword.value, token)
    if (res && res.code === 0) {
      list.value = res.data || []
      searchMsg.value = keyword.value.trim()
        ? list.value.length
          ? '找到 ' + list.value.length + ' 条与「' + keyword.value.trim() + '」相关的奇妙冒险'
          : '未找到相关冒险，换个关键词试试'
        : ''
    } else {
      searchMsg.value = res && res.msg ? res.msg : '搜索失败'
    }
  } catch (e) {
    searchMsg.value = '网络异常，无法连接替身中枢'
  } finally {
    searching.value = false
  }
}

// ===== 退出登录 =====
function onLogout() {
  localStorage.removeItem('jojo_token')
  emit('logout')
}

onMounted(async () => {
  // 拉取当前用户（预留 /api/me）
  try {
    const res = await me(token)
    if (res && res.code === 0 && res.data) username.value = res.data.username || username.value
  } catch (e) {
    /* 忽略，保持默认称呼 */
  }
  // 拉取导航项（预留 /api/nav，失败时用内置项）
  try {
    const res = await navItems(token)
    if (res && res.code === 0 && res.data && res.data.length) navs.value = res.data
  } catch (e) {
    /* 保持内置导航 */
  }
  loadQuests()
})
</script>

<template>
  <div class="home">
    <div class="stars">
      <div
        v-for="(s, i) in stars"
        :key="i"
        class="star"
        :style="{ left: s.left, top: s.top, animationDelay: s.delay, transform: 'scale(' + s.scale + ')' }"
      ></div>
    </div>

    <!-- ===== 导航栏 ===== -->
    <header class="navbar">
      <div class="nav-brand" @click="onNavClick({ key: 'home' })">
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

      <button type="button" class="nav-admin" @click="onAdmin">后台管理</button>

      <div class="nav-user">
        <span class="avatar">{{ username.slice(0, 1).toUpperCase() }}</span>
        <span class="name">{{ username }}</span>
        <button type="button" class="logout" @click="onLogout">退 出</button>
      </div>
    </header>

    <div class="nav-toast" :class="{ show: !!navToast }">{{ navToast }}</div>

    <!-- ===== 搜索区 ===== -->
    <section class="hero">
      <div class="hero-jp">スタープラチナ・ザ・ワールド</div>
      <h1 class="hero-title">奇妙冒险 · 由此开始</h1>
      <p class="hero-sub">搜索任务、替身与命运之线 —— 人类的赞歌就是勇气的赞歌</p>

      <form class="searchbar" @submit.prevent="onSearch()">
        <span class="corner tl"></span><span class="corner tr"></span>
        <span class="corner bl"></span><span class="corner br"></span>
        <span class="search-star">★</span>
        <input
          v-model="keyword"
          type="text"
          placeholder="输入关键词，寻找你的奇妙冒险…"
          autocomplete="off"
        />
        <button class="search-btn" type="submit" :disabled="searching">
          {{ searching ? '探索中…' : '搜 索' }}
        </button>
      </form>

      <div class="quick-tags">
        <span class="qt-label">热门：</span>
        <button v-for="t in quickTags" :key="t" type="button" class="qt" @click="onSearch(t)">{{ t }}</button>
      </div>

      <div class="search-msg" :class="{ show: !!searchMsg }">{{ searchMsg }}</div>
    </section>

    <!-- ===== 内容卡片 ===== -->
    <section class="content">
      <div class="section-head">
        <h2>奇妙冒险任务</h2>
        <span class="line"></span>
        <span class="rank-hint">RANK D → A</span>
      </div>

      <div v-if="loading" class="loading">替身能力加载中…</div>

      <div v-else class="grid">
        <article v-for="(q, i) in list" :key="i" class="quest-card">
          <span class="corner tl"></span><span class="corner br"></span>
          <div class="rank">{{ q.rank }}</div>
          <h3>{{ q.title }}</h3>
          <p>{{ q.desc }}</p>
          <div class="card-foot">
            <span class="star-deco">★ ★ ★</span>
            <button type="button" class="accept">接受任务 →</button>
          </div>
        </article>
      </div>

      <div v-if="!loading && !list.length" class="empty">
        <div class="empty-ora">オラオラ…</div>
        <p>这里空空如也，去搜索栏寻找你的命运吧</p>
      </div>
    </section>

    <div class="credit">— 黄金精神 · GOLDEN SPIRIT —</div>
  </div>
</template>

<style scoped>
.home {
  position: relative;
  width: 100%;
  min-height: 100vh;
  z-index: 1;
  animation: rise 0.7s cubic-bezier(0.2, 0.8, 0.2, 1) both;
}
@keyframes rise {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: none; }
}

/* ===== 导航栏 ===== */
.navbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 14px 32px;
  background: linear-gradient(180deg, rgba(16, 16, 24, 0.95), rgba(10, 10, 16, 0.9));
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(12px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.5);
}
.nav-brand {
  display: flex;
  flex-direction: column;
  cursor: pointer;
  user-select: none;
}
.nav-brand .jp {
  font-size: 10px;
  letter-spacing: 0.28em;
  color: var(--teal-glow);
  text-shadow: 0 0 10px rgba(63, 224, 242, 0.5);
}
.nav-brand .logo {
  font-family: "Cinzel", serif;
  font-weight: 900;
  font-size: 26px;
  line-height: 1.05;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold) 55%, var(--gold-deep));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  filter: drop-shadow(0 0 12px rgba(246, 197, 68, 0.35));
}
.nav-links {
  display: flex;
  gap: 6px;
  flex: 1;
}
.nav-link {
  border: 1px solid transparent;
  cursor: pointer;
  background: transparent;
  color: #b7b0a2;
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 0.1em;
  padding: 9px 18px;
  border-radius: 999px;
  transition: 0.25s;
}
.nav-link:hover {
  color: var(--gold-bright);
  border-color: var(--line);
  background: rgba(246, 197, 68, 0.06);
}
.nav-link.active {
  color: #1a1407;
  font-weight: 700;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold) 60%, var(--gold-deep));
  box-shadow: 0 6px 16px rgba(246, 197, 68, 0.3);
}
.nav-user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Cinzel", serif;
  font-weight: 900;
  color: #1a1407;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
  box-shadow: 0 0 12px rgba(246, 197, 68, 0.4);
}
.nav-user .name {
  font-size: 13.5px;
  color: var(--paper);
  letter-spacing: 0.06em;
}
.logout {
  border: 1px solid rgba(226, 59, 84, 0.5);
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
  box-shadow: 0 0 14px rgba(226, 59, 84, 0.3);
}

/* 后台管理按钮：金色描边，与退出按钮区分 */
.nav-admin {
  border: 1px solid var(--line);
  background: rgba(246, 197, 68, 0.08);
  color: var(--gold-bright);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.14em;
  padding: 7px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: 0.25s;
}
.nav-admin:hover {
  background: rgba(246, 197, 68, 0.2);
  box-shadow: 0 0 14px rgba(246, 197, 68, 0.3);
}

.nav-toast {
  position: fixed;
  top: 74px;
  left: 50%;
  transform: translateX(-50%) translateY(-8px);
  z-index: 30;
  padding: 9px 20px;
  border-radius: 999px;
  font-size: 12.5px;
  letter-spacing: 0.06em;
  color: var(--gold-bright);
  background: rgba(20, 20, 28, 0.95);
  border: 1px solid var(--line);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  opacity: 0;
  pointer-events: none;
  transition: 0.3s;
}
.nav-toast.show {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

/* ===== 搜索区 ===== */
.hero {
  text-align: center;
  padding: 72px 20px 30px;
}
.hero-jp {
  font-size: 12px;
  letter-spacing: 0.4em;
  color: var(--teal-glow);
  text-shadow: 0 0 12px rgba(63, 224, 242, 0.5);
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
  filter: drop-shadow(0 3px 0 rgba(0, 0, 0, 0.4)) drop-shadow(0 0 22px rgba(246, 197, 68, 0.3));
}
.hero-sub {
  font-size: 13.5px;
  color: #9b9486;
  letter-spacing: 0.14em;
  margin-bottom: 34px;
}

.searchbar {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  width: min(640px, 100%);
  margin: 0 auto;
  padding: 8px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(20, 20, 28, 0.92), rgba(10, 10, 16, 0.96));
  border: 1px solid var(--line);
  box-shadow: 0 0 0 1px rgba(246, 197, 68, 0.08), 0 20px 50px rgba(0, 0, 0, 0.5),
    0 0 40px rgba(246, 197, 68, 0.06);
}
.search-star {
  padding-left: 12px;
  color: var(--teal-glow);
  font-size: 16px;
  text-shadow: 0 0 10px rgba(63, 224, 242, 0.6);
}
.searchbar input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: var(--paper);
  font-family: inherit;
  font-size: 15px;
  letter-spacing: 0.04em;
  padding: 12px 4px;
}
.searchbar input::placeholder {
  color: #6f6a5d;
}
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
  box-shadow: 0 8px 20px rgba(246, 197, 68, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.5);
  transition: transform 0.15s, box-shadow 0.25s;
}
.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px rgba(246, 197, 68, 0.45), inset 0 1px 0 rgba(255, 255, 255, 0.6);
}
.search-btn:disabled {
  filter: grayscale(0.6) brightness(0.8);
  cursor: not-allowed;
}

.corner {
  position: absolute;
  width: 18px;
  height: 18px;
  border: 2px solid var(--gold);
  opacity: 0.6;
  pointer-events: none;
}
.corner.tl { top: -6px; left: -6px; border-right: 0; border-bottom: 0; }
.corner.tr { top: -6px; right: -6px; border-left: 0; border-bottom: 0; }
.corner.bl { bottom: -6px; left: -6px; border-right: 0; border-top: 0; }
.corner.br { bottom: -6px; right: -6px; border-left: 0; border-top: 0; }

.quick-tags {
  margin-top: 18px;
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
  box-shadow: 0 0 14px rgba(63, 224, 242, 0.25);
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

/* ===== 内容区 ===== */
.content {
  width: min(1080px, calc(100% - 40px));
  margin: 26px auto 90px;
}
.section-head {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 22px;
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
  background: linear-gradient(90deg, var(--line), transparent);
}
.rank-hint {
  font-family: "Cinzel", serif;
  font-size: 11px;
  letter-spacing: 0.2em;
  color: var(--teal-glow);
}
.loading {
  text-align: center;
  padding: 60px 0;
  color: #8a8475;
  letter-spacing: 0.2em;
  font-size: 13px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 18px;
}
.quest-card {
  position: relative;
  padding: 22px 20px 16px;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(20, 20, 28, 0.92), rgba(10, 10, 16, 0.96));
  border: 1px solid rgba(246, 197, 68, 0.2);
  transition: transform 0.25s, box-shadow 0.25s, border-color 0.25s;
  overflow: hidden;
}
.quest-card:hover {
  transform: translateY(-4px);
  border-color: var(--gold);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.5), 0 0 24px rgba(246, 197, 68, 0.12);
}
.quest-card .corner { width: 14px; height: 14px; opacity: 0.5; }
.quest-card .corner.tl { top: 8px; left: 8px; }
.quest-card .corner.br { bottom: 8px; right: 8px; }
.rank {
  display: inline-block;
  font-family: "Cinzel", serif;
  font-weight: 900;
  font-size: 13px;
  color: #1a1407;
  padding: 3px 12px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
  box-shadow: 0 4px 12px rgba(246, 197, 68, 0.3);
  margin-bottom: 12px;
}
.quest-card h3 {
  font-size: 16.5px;
  color: var(--gold-bright);
  letter-spacing: 0.08em;
  margin-bottom: 8px;
}
.quest-card p {
  font-size: 12.5px;
  line-height: 1.8;
  color: #b7b0a2;
  min-height: 44px;
}
.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed rgba(246, 197, 68, 0.2);
}
.star-deco {
  font-size: 10px;
  letter-spacing: 0.3em;
  color: rgba(246, 197, 68, 0.5);
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
.accept:hover {
  text-shadow: 0 0 10px rgba(63, 224, 242, 0.7);
}

.empty {
  text-align: center;
  padding: 70px 0;
  color: #8a8475;
}
.empty-ora {
  font-size: 26px;
  font-weight: 900;
  color: var(--crimson);
  text-shadow: 0 0 14px rgba(226, 59, 84, 0.5);
  margin-bottom: 12px;
  letter-spacing: 0.1em;
}

/* ===== 响应式 ===== */
@media (max-width: 720px) {
  .navbar { flex-wrap: wrap; gap: 12px; padding: 12px 16px; }
  .nav-links { order: 3; width: 100%; overflow-x: auto; }
  .hero-title { font-size: 28px; }
  .searchbar { flex-direction: column; align-items: stretch; }
  .search-star { display: none; }
}
</style>
