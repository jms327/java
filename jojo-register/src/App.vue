<script setup>
import { ref, reactive } from 'vue'
import { register, login, me, quests } from './api.js'

// ===== 背景星点（乔斯达家纹） =====
const stars = Array.from({ length: 26 }, () => ({
  left: Math.random() * 100 + '%',
  top: Math.random() * 100 + '%',
  delay: Math.random() * 6 + 's',
  scale: (0.6 + Math.random() * 1.4).toFixed(2)
}))

// ===== 表单状态 =====
const mode = ref('register') // 'register' | 'login'

const user = ref('')
const email = ref('')
const pass = ref('')
const confirm = ref('')
const agree = ref(false)
const agreeColor = ref('')

const account = ref('')
const lpass = ref('')

// 密码强度条
const barWidth = ref('0%')
const barColor = ref('#ff5d6c')

// 字段校验状态
const fields = reactive({
  user: { valid: false, invalid: false, hint: '3–16 位，字母、数字或「_」' },
  email: { valid: false, invalid: false, hint: '用于接收替身觉醒通知' },
  pass: { valid: false, invalid: false, hint: '至少 8 位，含字母与数字' },
  confirm: { valid: false, invalid: false, hint: '两次输入需一致' },
  account: { valid: false, invalid: false, hint: '可使用替身使者名或电子邮箱' },
  lpass: { valid: false, invalid: false, hint: '至少 8 位' }
})

// 提示 / 遮罩 / 令牌
const alertMsg = ref('')
const alertShow = ref(false)
let alertTimer = null

const awakenShow = ref(false)
const awakenBig = ref('')
const awakenOra = ref('')
const awakenDesc = ref('')
const tokenCode = ref('')
const tokBoxShow = ref(false)

const meInfo = ref('')
const meInfoShow = ref(false)
const meSubmitting = ref(false)

// ===== 工具：设置字段校验态 =====
function setField(name, ok, msg) {
  fields[name].valid = ok
  fields[name].invalid = !ok
  if (msg !== undefined) fields[name].hint = msg
}

const reUser = /^[A-Za-z0-9_]{3,16}$/
const reEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// ===== 注册字段校验 =====
function onUser() {
  const v = user.value.trim()
  if (!v) return setField('user', false, '')
  const ok = reUser.test(v)
  setField('user', ok, ok ? '名称可用，替身已就位' : '3–16 位，仅限字母、数字或「_」')
}

function onEmail() {
  const v = email.value.trim()
  if (!v) return setField('email', false, '')
  const ok = reEmail.test(v)
  setField('email', ok, ok ? '邮箱格式正确' : '请输入有效的电子邮箱')
}

function onPass() {
  const v = pass.value
  let score = 0
  if (v.length >= 8) score++
  if (/[A-Za-z]/.test(v) && /\d/.test(v)) score++
  if (/[^A-Za-z0-9]/.test(v)) score++
  if (v.length >= 12) score++
  const colors = ['#ff5d6c', '#ff9f43', '#f6c544', '#39d98a']
  const pct = [0, 30, 65, 100][Math.min(score, 3)]
  barWidth.value = pct + '%'
  barColor.value = colors[Math.min(score, 3)]
  const ok = v.length >= 8 && /[A-Za-z]/.test(v) && /\d/.test(v)
  setField('pass', ok || !v, ok ? '波纹强度足够' : '至少 8 位，含字母与数字')
  if (confirm.value) onConfirm()
}

function onConfirm() {
  const v = confirm.value
  if (!v) return setField('confirm', false, '')
  const ok = v === pass.value && v.length > 0
  setField('confirm', ok, ok ? '两次输入一致' : '与波纹密码不一致')
}

function allValid() {
  return (
    reUser.test(user.value.trim()) &&
    reEmail.test(email.value.trim()) &&
    pass.value.length >= 8 &&
    /[A-Za-z]/.test(pass.value) &&
    /\d/.test(pass.value) &&
    confirm.value === pass.value &&
    confirm.value.length > 0 &&
    agree.value
  )
}

// ===== 错误提示 =====
function showError(msg) {
  alertMsg.value = '⚠ ' + (msg || '注册失败，请稍后再试')
  alertShow.value = true
  clearTimeout(alertTimer)
  alertTimer = setTimeout(() => (alertShow.value = false), 4500)
}

// ===== 注册提交 =====
const regSubmitting = ref(false)
async function onRegSubmit() {
  if (!allValid()) {
    onUser()
    onEmail()
    onPass()
    onConfirm()
    if (!agree.value) agreeColor.value = '#ff5d6c'
    return
  }
  regSubmitting.value = true
  try {
    const res = await register({
      username: user.value.trim(),
      email: email.value.trim(),
      password: pass.value,
      confirmPassword: confirm.value
    })
    if (res && res.code === 0) {
      const token = res.data && res.data.token
      if (token) localStorage.setItem('jojo_token', token)
      awakenBig.value = 'STAND AWAKENED'
      awakenOra.value = 'オラオラオラ！'
      awakenDesc.value = '欢迎，' + user.value.trim() + '！你的替身已觉醒，奇妙冒险正式开始。'
      if (token) {
        tokenCode.value = token
        tokBoxShow.value = true
        meInfoShow.value = false
        meInfo.value = ''
      } else {
        tokBoxShow.value = false
      }
      awakenShow.value = true
    } else {
      showError(res && res.msg ? res.msg : '注册失败')
    }
  } catch (e) {
    showError('网络异常，无法连接替身中枢')
  } finally {
    regSubmitting.value = false
  }
}

// ===== 登录字段校验 =====
function onAccount() {
  const v = account.value.trim()
  if (!v) return setField('account', false, '')
  setField('account', true, '账号可用')
}

function onLpass() {
  const v = lpass.value
  const ok = v.length >= 8
  setField('lpass', ok || !v, ok ? '波纹强度足够' : '至少 8 位')
}

function loginValid() {
  return account.value.trim().length > 0 && lpass.value.length >= 8
}

// ===== 登录提交 =====
const loginSubmitting = ref(false)
async function onLoginSubmit() {
  if (!loginValid()) {
    onAccount()
    onLpass()
    return
  }
  loginSubmitting.value = true
  try {
    const res = await login({ account: account.value.trim(), password: lpass.value })
    if (res && res.code === 0) {
      const token = res.data.token
      localStorage.setItem('jojo_token', token)
      awakenBig.value = 'WELCOME BACK'
      awakenOra.value = 'トリック・オア・トリート？'
      awakenDesc.value = '欢迎回来，' + res.data.user.username + '！你的替身仍在等待。'
      tokenCode.value = token
      tokBoxShow.value = true
      meInfoShow.value = false
      meInfo.value = ''
      awakenShow.value = true
    } else {
      showError(res && res.msg ? res.msg : '登录失败')
    }
  } catch (e) {
    showError('网络异常，无法连接替身中枢')
  } finally {
    loginSubmitting.value = false
  }
}

// ===== 验证令牌：/api/me + 受保护 /api/quests =====
async function onMe() {
  const token = localStorage.getItem('jojo_token')
  if (!token) {
    meInfo.value = '<b style="color:var(--err)">未找到本地令牌</b>'
    meInfoShow.value = true
    return
  }
  meSubmitting.value = true
  meInfo.value = '验证中…'
  meInfoShow.value = true
  try {
    const res = await me(token)
    if (!res || res.code !== 0) {
      meInfo.value = '<b style="color:var(--err)">' + (res && res.msg ? res.msg : '验证失败') + '</b>'
      return
    }
    const u = res.data
    let html =
      '<b>令牌有效 ✔</b><br>ID: ' +
      u.id +
      '<br>用户名: ' +
      u.username +
      '<br>邮箱: ' +
      u.email +
      '<br>觉醒于: ' +
      u.createTime
    const qr = await quests(token)
    if (qr && qr.code === 0) {
      html += '<br><br><b style="color:var(--teal-glow)">奇妙冒险任务：</b>'
      ;(qr.data || []).forEach((q) => {
        html += '<br>· [' + q.rank + '] ' + q.title + ' — ' + q.desc
      })
    } else {
      html += '<br><span style="color:var(--err)">领取任务失败：' + (qr && qr.msg ? qr.msg : '') + '</span>'
    }
    meInfo.value = html
  } catch (e) {
    meInfo.value = '网络异常'
  } finally {
    meSubmitting.value = false
  }
}

// ===== 切换注册 / 登录 =====
function setMode(which) {
  mode.value = which
  alertShow.value = false
}

function onAgreeChange() {
  if (agree.value) agreeColor.value = ''
}
</script>

<template>
  <div class="stars">
    <div
      v-for="(s, i) in stars"
      :key="i"
      class="star"
      :style="{ left: s.left, top: s.top, animationDelay: s.delay, transform: 'scale(' + s.scale + ')' }"
    ></div>
  </div>

  <div class="card">
    <span class="corner tl"></span><span class="corner tr"></span>
    <span class="corner bl"></span><span class="corner br"></span>

    <div class="brand">
      <div class="jp">ジョジョの奇妙な冒険</div>
      <div class="logo">JOJO</div>
      <div class="tag">替 身 使 者 觉 醒 登 录</div>
    </div>

    <div class="tabs">
      <button type="button" class="tab" :class="{ active: mode === 'register' }" @click="setMode('register')">
        觉 醒 注 册
      </button>
      <button type="button" class="tab" :class="{ active: mode === 'login' }" @click="setMode('login')">
        替 身 登 录
      </button>
    </div>

    <div class="alert" :class="{ show: alertShow }">{{ alertMsg }}</div>

    <!-- 注册表单 -->
    <form v-show="mode === 'register'" novalidate @submit.prevent="onRegSubmit">
      <div class="field" :class="{ valid: fields.user.valid, invalid: fields.user.invalid }">
        <input type="text" id="user" v-model="user" @input="onUser" placeholder="替身使者名" autocomplete="off" required />
        <label for="user">替身使者名 / Stand User</label>
        <span class="star-mark">★</span>
        <div class="hint">{{ fields.user.hint }}</div>
      </div>

      <div class="field" :class="{ valid: fields.email.valid, invalid: fields.email.invalid }">
        <input type="email" id="email" v-model="email" @input="onEmail" placeholder="电子邮箱" autocomplete="off" required />
        <label for="email">电子邮箱 / Email</label>
        <span class="star-mark">★</span>
        <div class="hint">{{ fields.email.hint }}</div>
      </div>

      <div class="field" :class="{ valid: fields.pass.valid, invalid: fields.pass.invalid }">
        <input type="password" id="pass" v-model="pass" @input="onPass" placeholder="波纹密码" required />
        <label for="pass">波纹密码 / Password</label>
        <span class="star-mark">★</span>
        <div class="hint">{{ fields.pass.hint }}</div>
        <div class="strength"><i :style="{ width: barWidth, background: barColor }"></i></div>
      </div>

      <div class="field" :class="{ valid: fields.confirm.valid, invalid: fields.confirm.invalid }">
        <input type="password" id="confirm" v-model="confirm" @input="onConfirm" placeholder="确认波纹密码" required />
        <label for="confirm">确认波纹密码 / Confirm</label>
        <span class="star-mark">★</span>
        <div class="hint">{{ fields.confirm.hint }}</div>
      </div>

      <div class="terms" :style="{ color: agreeColor }">
        <input type="checkbox" id="agree" v-model="agree" @change="onAgreeChange" required />
        <label for="agree">
          我已理解「人类的赞歌就是勇气的赞歌」，并同意
          <a href="#" @click.prevent>替身使者契约</a>。
        </label>
      </div>

      <button class="submit" type="submit" :disabled="regSubmitting">
        {{ regSubmitting ? '觉醒中…' : '觉 醒 我 的 替 身' }}
      </button>
    </form>

    <!-- 登录表单 -->
    <form v-show="mode === 'login'" novalidate @submit.prevent="onLoginSubmit">
      <div class="field" :class="{ valid: fields.account.valid, invalid: fields.account.invalid }">
        <input type="text" id="account" v-model="account" @input="onAccount" placeholder="用户名 / 邮箱" autocomplete="off" required />
        <label for="account">账号 / Account</label>
        <span class="star-mark">★</span>
        <div class="hint">{{ fields.account.hint }}</div>
      </div>

      <div class="field" :class="{ valid: fields.lpass.valid, invalid: fields.lpass.invalid }">
        <input type="password" id="lpass" v-model="lpass" @input="onLpass" placeholder="波纹密码" required />
        <label for="lpass">波纹密码 / Password</label>
        <span class="star-mark">★</span>
        <div class="hint">{{ fields.lpass.hint }}</div>
      </div>

      <button class="submit" type="submit" :disabled="loginSubmitting">
        {{ loginSubmitting ? '验证中…' : '进 入 奇 妙 冒 险' }}
      </button>
    </form>

    <div class="foot" v-show="mode === 'register'">
      已有替身？ <a href="#" @click.prevent="setMode('login')">直接登录 →</a>
    </div>
    <div class="foot" v-show="mode === 'login'">
      新觉醒的替身？ <a href="#" @click.prevent="setMode('register')">去注册 →</a>
    </div>
  </div>

  <!-- 觉醒成功遮罩 -->
  <div class="awaken" :class="{ show: awakenShow }" @click="awakenShow = false">
    <div class="inner" @click.stop>
      <div class="big">{{ awakenBig }}</div>
      <div class="ora">{{ awakenOra }}</div>
      <div class="desc">{{ awakenDesc }}</div>
      <div class="tok" :class="{ show: tokBoxShow }">
        <div class="tok-label">你的替身令牌 (JWT)</div>
        <code>{{ tokenCode }}</code>
        <button class="mini" type="button" @click="onMe" :disabled="meSubmitting">验证令牌 · 领取任务</button>
        <div class="me" :class="{ show: meInfoShow }" v-html="meInfo"></div>
      </div>
    </div>
  </div>

  <div class="credit">— 黄金精神 · GOLDEN SPIRIT —</div>
</template>
