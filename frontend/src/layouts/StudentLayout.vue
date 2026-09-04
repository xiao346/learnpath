<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const displayName = computed(() => auth.user?.displayName ?? '同学')
const avatarText = computed(() => displayName.value.slice(0, 1))
const greeting = computed(() => {
  const hour = new Date().getHours()
  return hour < 11 ? '早上好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'
})
async function logout() { await auth.logout(); await router.replace('/login') }
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
      <div class="sidebar-progress"><div><span>本周目标</span><strong>78%</strong></div><div class="progress-track"><i></i></div><small>再学习 2.2 小时即可完成</small></div>
      <button class="logout-button" type="button" @click="logout">退出登录</button>
    </aside>
    <section class="dashboard-main">
      <header class="topbar"><div><p>{{ greeting }}，{{ displayName }} 👋</p><h1>今天也向目标靠近一点吧</h1></div><div class="top-actions"><button class="icon-button" aria-label="消息通知">♢<i></i></button><div class="avatar">{{ avatarText }}</div></div></header>
      <RouterView />
    </section>
  </main>
</template>
