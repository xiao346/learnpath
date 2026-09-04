<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useDashboardStore } from '../stores/dashboard'

const auth = useAuthStore()
const dashboard = useDashboardStore()
const router = useRouter()
const displayName = computed(() => auth.user?.displayName ?? '同学')
const avatarText = computed(() => displayName.value.slice(0, 1))
const greeting = computed(() => {
  const hour = new Date().getHours()
  return hour < 11 ? '早上好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'
})
async function logout() { await auth.logout(); await router.replace('/login') }
const goalPercent = computed(() => dashboard.data?.weeklyGoalPercent ?? 0)
const goalRemaining = computed(() => {
  const minutes = dashboard.data?.weeklyRemainingMinutes ?? 0
  if (minutes <= 0) return '本周学习目标已完成'
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  return `再学习 ${hours ? `${hours} 小时 ` : ''}${rest ? `${rest} 分钟` : ''}即可完成`
})
onMounted(() => dashboard.load())
</script>

<template>
  <main class="dashboard-shell">
    <div class="dashboard-ambient"></div>
    <aside class="sidebar glass-card">
      <RouterLink class="brand dashboard-brand" to="/"><span class="brand-mark">知</span><span>知途 <b>LearnPath</b></span></RouterLink>
      <nav aria-label="主导航">
        <RouterLink class="nav-item" exact-active-class="active" to="/"><span>⌂</span>学习首页</RouterLink>
        <RouterLink class="nav-item" active-class="active" to="/courses"><span>▤</span>课程中心</RouterLink>
        <RouterLink class="nav-item" active-class="active" to="/practice"><span>◎</span>在线练习</RouterLink>
        <a class="nav-item disabled" href="#"><span>◇</span>学习计划</a>
        <a class="nav-item disabled" href="#"><span>✦</span>智能推荐</a>
        <a class="nav-item disabled" href="#"><span>◒</span>学习报告</a>
      </nav>
      <div class="sidebar-progress"><div><span>本周目标</span><strong>{{ goalPercent }}%</strong></div><div class="progress-track"><i :style="{ width: `${goalPercent}%` }"></i></div><small>{{ goalRemaining }}</small></div>
      <button class="logout-button" type="button" @click="logout">退出登录</button>
    </aside>
    <section class="dashboard-main">
      <header class="topbar"><div><p>{{ greeting }}，{{ displayName }} 👋</p><h1>今天也向目标靠近一点吧</h1></div><div class="top-actions"><button class="icon-button" aria-label="消息通知">♢<i></i></button><div class="avatar">{{ avatarText }}</div></div></header>
      <RouterView />
    </section>
  </main>
</template>
