<script setup>
import { ref, onMounted } from 'vue'
import { adminQuests, adminCreateQuest, adminUpdateQuest, adminDeleteQuest } from './api.js'

const emit = defineEmits(['admin-logout'])

const token = localStorage.getItem('jojo_admin_token') || ''

const list = ref([])
const loading = ref(true)
const msg = ref('')
const msgType = ref('') // 'ok' | 'err'
let msgTimer = null

function flash(text, type) {
  msg.value = text
  msgType.value = type
  clearTimeout(msgTimer)
  msgTimer = setTimeout(() => (msg.value = ''), 3000)
}

// ===== 表单（新增 / 编辑） =====
const editingId = ref(null) // null 表示新增
const formOpen = ref(false)
const saving = ref(false)
const form = ref({ rank: 'D', title: '', desc: '', sort: 1 })

function openCreate() {
  editingId.value = null
  form.value = { rank: 'D', title: '', desc: '', sort: list.value.length + 1 }
  formOpen.value = true
}

function openEdit(q) {
  editingId.value = q.id
  form.value = { rank: q.rank, title: q.title, desc: q.desc, sort: q.sort }
  formOpen.value = true
}

function closeForm() {
  formOpen.value = false
  editingId.value = null
}

async function save() {
  if (!form.value.title.trim() || !form.value.desc.trim()) {
    flash('标题与描述不能为空', 'err')
    return
  }
  saving.value = true
  try {
    const payload = {
      rank: form.value.rank,
      title: form.value.title.trim(),
      desc: form.value.desc.trim(),
      sort: Number(form.value.sort) || 0
    }
    const res = editingId.value
      ? await adminUpdateQuest(editingId.value, payload, token)
      : await adminCreateQuest(payload, token)
    if (res && res.code === 0) {
      flash(editingId.value ? '任务已更新' : '任务已新增', 'ok')
      closeForm()
      loadQuests()
    } else {
      flash(res && res.msg ? res.msg : '保存失败', 'err')
    }
  } catch (e) {
    flash('网络异常，无法连接替身中枢', 'err')
  } finally {
    saving.value = false
  }
}

async function remove(q) {
  if (!confirm('确定删除任务「' + q.title + '」？此操作不可恢复')) return
  try {
    const res = await adminDeleteQuest(q.id, token)
    if (res && res.code === 0) {
      flash('任务已删除', 'ok')
      loadQuests()
    } else {
      flash(res && res.msg ? res.msg : '删除失败', 'err')
    }
  } catch (e) {
    flash('网络异常，无法连接替身中枢', 'err')
  }
}

function rankClass(r) {
  return 'rk rk-' + (r || 'D').toLowerCase()
}

async function loadQuests() {
  loading.value = true
  try {
    const res = await adminQuests(token)
    if (res && res.code === 0) {
      list.value = res.data || []
    } else {
      flash(res && res.msg ? res.msg : '加载失败', 'err')
      list.value = []
    }
  } catch (e) {
    flash('网络异常，无法连接替身中枢', 'err')
  } finally {
    loading.value = false
  }
}

function onLogout() {
  emit('admin-logout')
}

onMounted(loadQuests)
</script>

<template>
  <div class="admin">
    <!-- 顶栏 -->
    <header class="topbar">
      <div class="brand">
        <span class="logo">JOJO</span>
        <span class="sub">后 台 管 理 系 统 · 奇妙任务</span>
      </div>
      <button type="button" class="logout" @click="onLogout">退 出</button>
    </header>

    <!-- 提示条 -->
    <div class="msg" :class="[msgType, { show: !!msg }]">{{ msg }}</div>

    <main class="content">
      <div class="panel">
        <div class="panel-head">
          <h2>奇妙冒险任务管理</h2>
          <button type="button" class="add-btn" @click="openCreate">+ 新增任务</button>
        </div>

        <!-- 数据表格 -->
        <div v-if="loading" class="loading">数据加载中…</div>
        <table v-else class="grid">
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
            <tr v-for="q in list" :key="q.id">
              <td class="c-id">{{ q.id }}</td>
              <td class="c-rank"><span :class="rankClass(q.rank)">{{ q.rank }}</span></td>
              <td class="c-title">{{ q.title }}</td>
              <td class="c-desc">{{ q.desc }}</td>
              <td class="c-sort">{{ q.sort }}</td>
              <td class="c-ops">
                <button type="button" class="op edit" @click="openEdit(q)">编辑</button>
                <button type="button" class="op del" @click="remove(q)">删除</button>
              </td>
            </tr>
            <tr v-if="!list.length">
              <td colspan="6" class="empty">暂无任务数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 新增 / 编辑 表单 -->
      <transition name="slide">
        <div v-if="formOpen" class="form-card">
          <span class="corner tl"></span><span class="corner br"></span>
          <h3>{{ editingId ? '编辑任务' : '新增任务' }}</h3>
          <div class="row">
            <label>等级</label>
            <select v-model="form.rank">
              <option value="D">D</option>
              <option value="C">C</option>
              <option value="B">B</option>
              <option value="A">A</option>
            </select>
            <label>排序</label>
            <input type="number" v-model="form.sort" />
          </div>
          <label>标题</label>
          <input type="text" v-model="form.title" placeholder="任务标题" />
          <label>描述</label>
          <textarea v-model="form.desc" rows="3" placeholder="任务描述"></textarea>
          <div class="form-ops">
            <button type="button" class="save" :disabled="saving" @click="save">
              {{ saving ? '保存中…' : '保 存' }}
            </button>
            <button type="button" class="cancel" @click="closeForm">取 消</button>
          </div>
        </div>
      </transition>
    </main>

    <div class="credit">— 黄金精神 · GOLDEN SPIRIT —</div>
  </div>
</template>

<style scoped>
.admin {
  min-height: 100vh;
  padding: 0 0 60px;
}
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 28px;
  border-bottom: 1px solid var(--line);
  background: rgba(10, 10, 16, 0.7);
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  z-index: 5;
}
.brand { display: flex; align-items: baseline; gap: 14px; }
.brand .logo {
  font-family: "Cinzel", serif;
  font-weight: 900;
  font-size: 26px;
  letter-spacing: 0.05em;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.brand .sub { font-size: 13px; letter-spacing: 0.16em; color: var(--teal-glow); }
.logout {
  border: 1px solid rgba(226, 59, 84, 0.5);
  background: rgba(226, 59, 84, 0.1);
  color: #ff9aa6;
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.12em;
  padding: 8px 18px;
  border-radius: 999px;
  cursor: pointer;
  transition: 0.25s;
}
.logout:hover { background: rgba(226, 59, 84, 0.22); box-shadow: 0 0 14px rgba(226, 59, 84, 0.3); }

.msg {
  max-width: 1080px;
  margin: 14px auto 0;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 13px;
  letter-spacing: 0.05em;
  opacity: 0;
  transition: opacity 0.3s;
}
.msg.show { opacity: 1; }
.msg.ok { background: rgba(57, 217, 138, 0.12); border: 1px solid rgba(57, 217, 138, 0.5); color: #9af2c4; }
.msg.err { background: rgba(255, 93, 108, 0.12); border: 1px solid rgba(255, 93, 108, 0.5); color: #ffb3bb; }

.content { width: min(1080px, calc(100% - 40px)); margin: 18px auto 0; }
.panel {
  background: linear-gradient(180deg, rgba(20, 20, 28, 0.92), rgba(10, 10, 16, 0.96));
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 22px 22px 14px;
  box-shadow: 0 0 0 1px rgba(246, 197, 68, 0.06), 0 24px 60px rgba(0, 0, 0, 0.5);
}
.panel-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.panel-head h2 {
  font-family: "Cinzel", "Noto Serif JP", serif;
  font-size: 19px;
  letter-spacing: 0.14em;
  color: var(--paper);
}
.add-btn {
  border: none;
  cursor: pointer;
  padding: 10px 20px;
  border-radius: 10px;
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 0.1em;
  color: #1a1407;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold) 60%, var(--gold-deep));
  box-shadow: 0 8px 20px rgba(246, 197, 68, 0.3);
  transition: transform 0.15s, box-shadow 0.25s;
}
.add-btn:hover { transform: translateY(-2px); box-shadow: 0 12px 26px rgba(246, 197, 68, 0.45); }

.loading { text-align: center; padding: 50px 0; color: #8a8475; letter-spacing: 0.2em; font-size: 13px; }

.grid { width: 100%; border-collapse: collapse; font-size: 13.5px; }
.grid th {
  text-align: left;
  font-family: "Cinzel", serif;
  font-size: 11px;
  letter-spacing: 0.14em;
  color: var(--teal-glow);
  padding: 10px 12px;
  border-bottom: 1px solid var(--line);
}
.grid td { padding: 12px; border-bottom: 1px solid rgba(246, 197, 68, 0.1); color: #cfc8b8; vertical-align: top; }
.grid tbody tr:hover { background: rgba(246, 197, 68, 0.04); }
.c-id { width: 56px; color: #8a8475; }
.c-rank { width: 70px; }
.c-sort { width: 64px; }
.c-ops { width: 130px; }
.c-desc { color: #b7b0a2; }
.rk {
  display: inline-block;
  font-family: "Cinzel", serif;
  font-weight: 900;
  font-size: 12px;
  padding: 3px 11px;
  border-radius: 999px;
  color: #1a1407;
}
.rk-d { background: linear-gradient(180deg, #9fb2c9, #6f8198); }
.rk-c { background: linear-gradient(180deg, #8ee0c8, #3fb89a); }
.rk-b { background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep)); }
.rk-a { background: linear-gradient(180deg, #ff9aa6, var(--crimson)); color: #fff; }
.op {
  border: 1px solid;
  background: transparent;
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.06em;
  padding: 5px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-right: 8px;
  transition: 0.2s;
}
.op.edit { border-color: rgba(63, 224, 242, 0.5); color: var(--teal-glow); }
.op.edit:hover { background: rgba(63, 224, 242, 0.14); }
.op.del { border-color: rgba(226, 59, 84, 0.5); color: #ff9aa6; }
.op.del:hover { background: rgba(226, 59, 84, 0.16); }
.empty { text-align: center; padding: 30px; color: #8a8475; }

/* 表单卡片 */
.form-card {
  position: relative;
  margin-top: 18px;
  padding: 22px 22px 18px;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(24, 24, 32, 0.95), rgba(12, 12, 18, 0.98));
  border: 1px solid rgba(246, 197, 68, 0.25);
}
.form-card .corner { width: 16px; height: 16px; opacity: 0.5; }
.form-card .corner.tl { top: 10px; left: 10px; }
.form-card .corner.br { bottom: 10px; right: 10px; }
.form-card h3 { font-family: "Cinzel", serif; font-size: 16px; letter-spacing: 0.12em; color: var(--gold-bright); margin-bottom: 14px; }
.form-card label { display: block; font-size: 12px; color: #9b9486; margin: 12px 0 6px; letter-spacing: 0.08em; }
.form-card .row { display: flex; gap: 14px; align-items: center; flex-wrap: wrap; }
.form-card .row label { margin: 0 4px 0 14px; }
.form-card input[type="text"],
.form-card input[type="number"],
.form-card select,
.form-card textarea {
  width: 100%;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(246, 197, 68, 0.25);
  border-radius: 9px;
  padding: 11px 13px;
  color: var(--paper);
  font-family: inherit;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.form-card .row input[type="number"],
.form-card .row select { width: 120px; }
.form-card input:focus,
.form-card select:focus,
.form-card textarea:focus {
  border-color: var(--gold);
  background: rgba(246, 197, 68, 0.05);
  box-shadow: 0 0 0 3px rgba(246, 197, 68, 0.1);
}
.form-card textarea { resize: vertical; }
.form-ops { display: flex; gap: 12px; margin-top: 18px; }
.save {
  border: none;
  cursor: pointer;
  padding: 11px 30px;
  border-radius: 10px;
  font-family: "Cinzel", serif;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.14em;
  color: #1a1407;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold) 60%, var(--gold-deep));
  box-shadow: 0 8px 20px rgba(246, 197, 68, 0.3);
  transition: transform 0.15s;
}
.save:hover { transform: translateY(-2px); }
.save:disabled { filter: grayscale(0.6) brightness(0.8); cursor: not-allowed; }
.cancel {
  border: 1px solid rgba(246, 197, 68, 0.3);
  background: transparent;
  color: #b7b0a2;
  font-family: inherit;
  font-size: 14px;
  padding: 11px 26px;
  border-radius: 10px;
  cursor: pointer;
  transition: 0.2s;
}
.cancel:hover { background: rgba(246, 197, 68, 0.08); }

.slide-enter-active, .slide-leave-active { transition: all 0.3s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(-10px); }

.credit {
  text-align: center;
  font-size: 11px;
  color: rgba(246, 197, 68, 0.35);
  letter-spacing: 0.1em;
  margin-top: 30px;
}

@media (max-width: 720px) {
  .grid th.c-desc, .grid td.c-desc { display: none; }
  .topbar { padding: 14px 16px; }
}
</style>
