<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const account = ref('20240001')
const password = ref('123456')
const role = ref<'student' | 'teacher' | 'admin'>('student')
const rememberMe = ref(true)
const showPassword = ref(false)
const showLoginHelp = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const greeting = computed(() => {
  const hour = new Date().getHours()
  return hour < 11 ? '早上好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'
})

async function login() {
  errorMessage.value = ''
  if (!account.value.trim() || !password.value.trim()) { errorMessage.value = '请填写账号和密码'; return }
  isLoading.value = true
  try {
    await auth.login(account.value.trim(), password.value, role.value, rememberMe.value)
    await router.replace(typeof route.query.redirect === 'string' ? route.query.redirect : '/')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '学习服务暂时无法连接'
  } finally { isLoading.value = false }
}
</script>

<template>
  <main class="auth-shell">
    <div class="ambient ambient-one"></div><div class="ambient ambient-two"></div><div class="grid-glow"></div>
    <section class="auth-layout" aria-labelledby="login-title">
      <div class="brand-panel">
        <RouterLink class="brand" to="/" aria-label="知途首页"><span class="brand-mark">知</span><span>知途 <b>LearnPath</b></span></RouterLink>
        <div class="brand-copy"><span class="eyebrow"><i></i> 从想法到上线</span><h1>做出属于你的，<br /><em>第一个网站。</em></h1><p>选一条适合自己的技术路线，每学会一个知识，就让网站多一个看得见的新功能。</p></div>
        <div class="orbit-scene" aria-hidden="true"><div class="orbit orbit-outer"></div><div class="orbit orbit-inner"></div><div class="planet-core"><span>&lt;/&gt;</span><small>正在创造</small></div><div class="floating-chip chip-one"><span>01</span> 写下第一行 HTML</div><div class="floating-chip chip-two"><span>02</span> 让按钮有反应</div><div class="floating-chip chip-three"><span>03</span> 上线分享作品</div></div>
        <div class="quote-card"><span class="quote-icon">“</span><p>最好的入门作品，不必复杂，但一定要有你自己的想法。</p><span>知途建站之旅</span></div>
      </div>
      <div class="login-panel">
        <div class="login-card glass-card">
          <div class="mobile-brand"><span class="brand-mark">知</span><span>知途 LearnPath</span></div>
          <div class="welcome-copy"><p><span class="status-dot"></span>{{ greeting }}，欢迎回来</p><h2 id="login-title">继续制作你的网站</h2></div>
          <div class="role-switch" aria-label="选择登录身份"><button :class="{ active: role === 'student' }" type="button" @click="role = 'student'">学生</button><button :class="{ active: role === 'teacher' }" type="button" @click="role = 'teacher'">教师</button><button :class="{ active: role === 'admin' }" type="button" @click="role = 'admin'">管理员</button></div>
          <form @submit.prevent="login">
            <label class="field"><span>账号</span><span class="input-wrap"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20 21a8 8 0 0 0-16 0M12 13a5 5 0 1 0 0-10 5 5 0 0 0 0 10Z" /></svg><input v-model="account" autocomplete="username" placeholder="请输入学号或工号" /></span></label>
            <label class="field"><span>密码</span><span class="input-wrap"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 10V8a6 6 0 0 1 12 0v2M5 10h14v11H5V10Z" /></svg><input v-model="password" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" placeholder="请输入登录密码" /><button class="password-toggle" type="button" @click="showPassword = !showPassword">{{ showPassword ? '隐藏' : '显示' }}</button></span></label>
            <div class="form-options"><label class="remember"><input v-model="rememberMe" type="checkbox" /><span></span>记住我</label><button class="text-button" type="button" :aria-expanded="showLoginHelp" @click="showLoginHelp = !showLoginHelp">忘记密码？</button></div>
            <p v-if="errorMessage" class="form-error" role="alert">{{ errorMessage }}</p>
            <button class="login-button" type="submit" :disabled="isLoading"><span>{{ isLoading ? '正在进入学习空间…' : '进入学习空间' }}</span><span v-if="!isLoading" class="arrow">→</span></button>
          </form>
          <div v-if="showLoginHelp" class="login-help" role="status"><strong>本地演示账号</strong><p>学生 <code>20240001</code>、教师 <code>T10001</code>、管理员 <code>admin</code>，演示密码均为 <code>123456</code>。</p><small>正式环境的密码需要由系统管理员重置。</small></div>
          <div class="login-tip"><span>✦</span> 演示账号已填充，登录即可生成你的建站路线</div>
        </div>
        <p class="copyright">© 2026 知途智能学习平台 · 保持好奇，持续生长</p>
      </div>
    </section>
  </main>
</template>
