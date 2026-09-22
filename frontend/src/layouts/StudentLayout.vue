<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useDashboardStore } from '../stores/dashboard'

const auth = useAuthStore()
const dashboard = useDashboardStore()
const router = useRouter()
const notificationOpen = ref(false)
const displayName = computed(() => auth.user?.displayName ?? '同学')
const avatarText = computed(() => displayName.value.slice(0, 1))
const greeting = computed(() => {
  const hour = new Date().getHours()
  return hour < 11 ? '早上好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'
})
async function logout() { await auth.logout(); await router.replace('/login') }
const remainingTasks = computed(() => Math.max(0, (dashboard.data?.totalTasks ?? 0) - (dashboard.data?.tasksCompleted ?? 0)))
onMounted(() => dashboard.load())
</script>

<template>
  <main class="dashboard-shell">
    <div class="dashboard-ambient"></div>
    <aside class="sidebar glass-card">
      <RouterLink class="brand dashboard-brand" to="/"><span class="brand-mark">知</span><span>知途 <b>LearnPath</b></span></RouterLink>
      <nav aria-label="主导航">
        <RouterLink class="nav-item" exact-active-class="active" to="/"><span>⌂</span>学习工作台</RouterLink>
        <RouterLink class="nav-item" active-class="active" to="/courses"><span>↗</span>建站之旅</RouterLink>
        <RouterLink class="nav-item" active-class="active" to="/knowledge"><span>▤</span>知识工具箱</RouterLink>
        <RouterLink class="nav-item" active-class="active" to="/practice"><span>◇</span>在线练习</RouterLink>
        <RouterLink class="nav-item" active-class="active" to="/games"><span>◎</span>趣味闯关</RouterLink>
        <RouterLink class="nav-item" active-class="active" to="/community"><span>♧</span>建站社区</RouterLink>
      </nav>
      <div class="sidebar-progress sidebar-start"><strong>还没找到想做的事？</strong><p>体验三个小作品，选一个愿意动手的目标。</p><RouterLink to="/courses">去建站之旅找灵感 →</RouterLink></div>
      <button class="logout-button" type="button" @click="logout">退出登录</button>
    </aside>
    <section class="dashboard-main">
      <header class="topbar"><div><p>{{ greeting }}，{{ displayName }} 👋</p><h1>今天，让你的网站多一个新本领</h1></div><div class="top-actions"><div class="notification-wrap"><button class="icon-button" aria-label="学习提醒" :aria-expanded="notificationOpen" @click="notificationOpen = !notificationOpen">♢<i v-if="!notificationOpen"></i></button><section v-if="notificationOpen" class="notification-panel glass-card"><header><div><span>学习提醒</span><strong>今天可以从这里继续</strong></div><button type="button" aria-label="关闭学习提醒" @click="notificationOpen = false">×</button></header><RouterLink to="/courses" @click="notificationOpen = false"><i>↗</i><div><strong>打开我的建站路线</strong><span>选择或继续当前一站，让网站多一个新本领。</span></div></RouterLink><RouterLink to="/" @click="notificationOpen = false"><i>✓</i><div><strong>{{ remainingTasks ? `还有 ${remainingTasks} 项可选加练` : '今天的加练已经完成' }}</strong><span>{{ remainingTasks ? '先完成首页标出的建站阶段，有余力再回来巩固。' : '可以去趣味闯关巩固刚学的知识。' }}</span></div></RouterLink><RouterLink to="/games" @click="notificationOpen = false"><i>◎</i><div><strong>四个趣味训练场已开放</strong><span>练习布局、CSS 排错、JavaScript 事件和各课程知识。</span></div></RouterLink></section></div><div class="avatar">{{ avatarText }}</div></div></header>
      <RouterView />
    </section>
    <nav class="mobile-navigation" aria-label="移动端主导航"><RouterLink exact-active-class="active" to="/"><span>⌂</span>工作台</RouterLink><RouterLink active-class="active" to="/courses"><span>↗</span>建站</RouterLink><RouterLink active-class="active" to="/knowledge"><span>▤</span>工具箱</RouterLink><RouterLink active-class="active" to="/practice"><span>◇</span>练习</RouterLink><RouterLink active-class="active" to="/games"><span>◎</span>闯关</RouterLink><RouterLink active-class="active" to="/community"><span>♧</span>社区</RouterLink></nav>
  </main>
</template>

<style scoped>
.sidebar-start > strong { font-size: 14px; color: #294b63; }.sidebar-start p { font-size: 12px; color: #5b7489; line-height: 1.8; }.sidebar-start a { color: #376c96; font-size: 12px; }
</style>
