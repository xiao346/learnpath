<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const account = ref('20240001')
const password = ref('123456')
const role = ref<'student' | 'teacher' | 'admin'>('student')
const rememberMe = ref(true)
const showPassword = ref(false)
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
        <div class="brand-copy"><span class="eyebrow"><i></i> 智能学习空间</span><h1>让每一次学习，<br /><em>都有迹可循。</em></h1><p>洞察学习状态，找到适合你的节奏。今天的一小步，会成为明天清晰的成长轨迹。</p></div>
        <div class="orbit-scene" aria-hidden="true"><div class="orbit orbit-outer"></div><div class="orbit orbit-inner"></div><div class="planet-core"><span>78%</span><small>本周目标</small></div><div class="floating-chip chip-one"><span>✦</span> 专注力 +12%</div><div class="floating-chip chip-two"><span>↗</span> 连续学习 12 天</div><div class="floating-chip chip-three"><span>✓</span> 今日任务 3/5</div></div>
        <div class="quote-card"><span class="quote-icon">“</span><p>学习从来不是孤独的远行，而是一场不断遇见更好自己的探索。</p><span>今日学习寄语</span></div>
      </div>
      <div class="login-panel">
        <div class="login-card glass-card">
          <div class="mobile-brand"><span class="brand-mark">知</span><span>知途 LearnPath</span></div>
          <div class="welcome-copy"><p><span class="status-dot"></span>{{ greeting }}，欢迎回来</p><h2 id="login-title">继续你的学习旅程</h2></div>
          <div class="role-switch" aria-label="选择登录身份"><button :class="{ active: role === 'student' }" type="button" @click="role = 'student'">学生</button><button :class="{ active: role === 'teacher' }" type="button" @click="role = 'teacher'">教师</button><button :class="{ active: role === 'admin' }" type="button" @click="role = 'admin'">管理员</button></div>
          <form @submit.prevent="login">
            <label class="field"><span>账号</span><span class="input-wrap"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20 21a8 8 0 0 0-16 0M12 13a5 5 0 1 0 0-10 5 5 0 0 0 0 10Z" /></svg><input v-model="account" autocomplete="username" placeholder="请输入学号或工号" /></span></label>
            <label class="field"><span>密码</span><span class="input-wrap"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 10V8a6 6 0 0 1 12 0v2M5 10h14v11H5V10Z" /></svg><input v-model="password" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" placeholder="请输入登录密码" /><button class="password-toggle" type="button" @click="showPassword = !showPassword">{{ showPassword ? '隐藏' : '显示' }}</button></span></label>
            <div class="form-options"><label class="remember"><input v-model="rememberMe" type="checkbox" /><span></span>记住我</label><button class="text-button" type="button">忘记密码？</button></div>
            <p v-if="errorMessage" class="form-error" role="alert">{{ errorMessage }}</p>
            <button class="login-button" type="submit" :disabled="isLoading"><span>{{ isLoading ? '正在进入学习空间…' : '进入学习空间' }}</span><span v-if="!isLoading" class="arrow">→</span></button>
          </form>
          <div class="login-tip"><span>✦</span> 演示账号已填充，点击即可体验学生工作台</div>
        </div>
        <p class="copyright">© 2026 知途智能学习平台 · 保持好奇，持续生长</p>
      </div>
    </section>
  </main>
</template>
