<script setup>
import { ref } from 'vue'
import { adminLogin } from './api.js'

const emit = defineEmits(['admin-ok', 'back'])

// 背景星点（乔斯达家纹）
const stars = Array.from({ length: 22 }, () => ({
  left: Math.random() * 100 + '%',
  top: Math.random() * 100 + '%',
  delay: Math.random() * 6 + 's',
  scale: (0.6 + Math.random() * 1.4).toFixed(2)
}))

const account = ref('')
const password = ref('')
const alertMsg = ref('')
const alertShow = ref(false)
const submitting = ref(false)

function showError(msg) {
  alertMsg.value = '⚠ ' + (msg || '登录失败，请稍后再试')
  alertShow.value = true
}

async function onSubmit() {
  if (!account.value.trim() || !password.value) {
    showError('请输入账号与波纹密码')
    return
  }
  submitting.value = true
  alertShow.value = false
  try {
    const res = await adminLogin({ account: account.value.trim(), password: password.value })
    if (res && res.code === 0) {
      const token = res.data.token
      localStorage.setItem('jojo_admin_token', token)
      const who = res.data.user && res.data.user.username ? res.data.user.username : account.value.trim()
      localStorage.setItem('jojo_admin_name', who)
      emit('admin-ok')
    } else {
      showError(res && res.msg ? res.msg : '登录失败')
    }
  } catch (e) {
    showError('网络异常，无法连接替身中枢')
  } finally {
    submitting.value = false
  }
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
      <div class="jp">ジョジョの奇妙な冒険 · 管理</div>
      <div class="logo">JOJO</div>
      <div class="tag">后 台 管 理 系 统 · 登 录</div>
    </div>

    <div class="alert" :class="{ show: alertShow }">{{ alertMsg }}</div>

    <form novalidate @submit.prevent="onSubmit">
      <div class="field">
        <input
          type="text"
          id="admin-account"
          v-model="account"
          placeholder=" "
          autocomplete="off"
          required
        />
        <label for="admin-account">管理员账号 / Account</label>
        <div class="hint">可使用管理员名或电子邮箱</div>
      </div>

      <div class="field">
        <input
          type="password"
          id="admin-pass"
          v-model="password"
          placeholder=" "
          required
        />
        <label for="admin-pass">波纹密码 / Password</label>
        <div class="hint">仅管理员可进入后台</div>
      </div>

      <button class="submit" type="submit" :disabled="submitting">
        {{ submitting ? '验证中…' : '进 入 后 台' }}
      </button>
    </form>

    <div class="admin-tip">
      <span>DEMO</span> 默认管理员 <b>admin</b> / <b>admin123</b>（无注册入口，仅管理员可进入）
    </div>

    <div class="foot">
      <a href="#" @click.prevent="emit('back')">← 返回首页</a>
    </div>
  </div>

  <div class="credit">— 黄金精神 · GOLDEN SPIRIT —</div>
</template>

<style scoped>
.admin-tip {
  margin-top: 16px;
  padding: 9px 12px;
  border-radius: 9px;
  border: 1px dashed rgba(246, 197, 68, 0.28);
  background: rgba(246, 197, 68, 0.04);
  font-size: 11.5px;
  line-height: 1.6;
  color: #8a8475;
  letter-spacing: 0.02em;
}
.admin-tip span {
  display: inline-block;
  font-family: "Cinzel", serif;
  font-size: 9px;
  letter-spacing: 0.12em;
  color: #1a1407;
  background: linear-gradient(180deg, var(--gold-bright), var(--gold-deep));
  padding: 1px 6px;
  border-radius: 4px;
  margin-right: 6px;
  vertical-align: 1px;
}
.admin-tip b { color: #d8d1c1; font-weight: 600; }
</style>
