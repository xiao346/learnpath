<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useDashboardStore } from '../stores/dashboard'
import { readCompletedStages } from '../services/journey'

const dashboard = useDashboardStore()
const savedJourney = localStorage.getItem('learnpath_web_journey')
let journey: { project: string; frontend: string; backend: string } | null = null
try { journey = savedJourney ? JSON.parse(savedJourney) as { project: string; frontend: string; backend: string } : null }
catch { localStorage.removeItem('learnpath_web_journey') }
const projectNames: Record<string, string> = { portfolio: '个人作品集', blog: '兴趣博客', campus: '校园信息站' }
const projectName = journey ? (projectNames[journey.project] ?? '我的第一个网站') : ''
const completedJourneyStages = readCompletedStages()
const journeyHeadline = computed(() => {
  if (!journey) return '选择技术路线，开始做第一个网站'
  if (completedJourneyStages.includes('launch')) return `${projectName}已经完成，可以展示了`
  if (completedJourneyStages.length) return `${projectName}已经完成 ${completedJourneyStages.length} 个建站阶段`
  return `${projectName}正在等待第一行代码`
})
const journeyDescription = computed(() => completedJourneyStages.includes('launch')
  ? '你的第一份网站作品已经通过上线检查，可以继续补充内容，或开始构思下一个项目。'
  : '先完成一张属于自己的首页，再逐步加入样式、交互、后端和数据库。')
const maxMinutes = computed(() => Math.max(1, ...(dashboard.data?.trend.days.map((day) => day.minutes) ?? [1])))
const totalHours = computed(() => ((dashboard.data?.trend.totalMinutes ?? 0) / 60).toFixed(1))
const differenceHours = computed(() => Math.abs(((dashboard.data?.trend.totalMinutes ?? 0) - (dashboard.data?.trend.previousWeekMinutes ?? 0)) / 60).toFixed(1))
const weekDifference = computed(() => {
  const difference = (dashboard.data?.trend.totalMinutes ?? 0) - (dashboard.data?.trend.previousWeekMinutes ?? 0)
  if (difference === 0) return '与上周持平'
  return `比上周${difference > 0 ? '多' : '少'} ${differenceHours.value} 小时`
})
onMounted(() => dashboard.load(true))
</script>

<template>
  <div v-if="dashboard.loading && !dashboard.data" class="state-card glass-card"><span class="loader"></span><p>正在汇总你的学习数据…</p></div>
  <div v-else-if="dashboard.error && !dashboard.data" class="state-card glass-card"><strong>首页数据暂时无法加载</strong><p>{{ dashboard.error }}</p><button @click="dashboard.load(true)">重新加载</button></div>
  <div v-else-if="dashboard.data" class="dashboard-grid">
    <section class="build-focus-card glass-card"><div class="build-focus-copy"><span class="eyebrow"><i></i> {{ journey ? '我的建站进度' : '从这里开始' }}</span><h2>{{ journeyHeadline }}</h2><p>{{ journey ? journeyDescription : '告诉我们你想做什么、想用什么技术，知途会为你排好每一步。' }}</p><RouterLink class="primary-link" to="/courses">{{ journey ? completedJourneyStages.includes('launch') ? '查看完整路线' : '继续建站' : '生成我的路线' }} <span>→</span></RouterLink></div><div class="build-window" aria-hidden="true"><div><i></i><i></i><i></i></div><code><b>&lt;h1&gt;</b>你好，Web！<b>&lt;/h1&gt;</b><span>/* 你的作品从这里开始 */</span></code></div></section>
    <section v-if="dashboard.data.focus" class="focus-card glass-card"><div class="focus-content"><span class="eyebrow"><i></i> 今日学习焦点</span><h2>{{ dashboard.data.focus.courseTitle }} · 第 {{ dashboard.data.focus.completedLessons + 1 }} 章</h2><p>{{ dashboard.data.focus.chapterTitle }}</p><div class="focus-meta"><span>◷ 预计 {{ dashboard.data.focus.estimatedMinutes }} 分钟</span><span>共 {{ dashboard.data.focus.totalLessons }} 章</span></div><RouterLink class="primary-link" :to="`/courses/${dashboard.data.focus.courseId}`">继续学习 <span>→</span></RouterLink></div><div class="focus-ring" :style="{ '--progress': `${dashboard.data.focus.progressPercent * 3.6}deg` }"><div><strong>{{ dashboard.data.focus.progressPercent }}%</strong><span>课程进度</span></div></div></section>
    <section class="streak-card glass-card"><div class="card-heading"><div><span class="mini-icon purple">↗</span><h2>连续学习</h2></div><span class="live-badge">本周实时</span></div><div class="streak-number"><strong>{{ dashboard.data.streakDays }}</strong><span>天</span><em>{{ dashboard.data.streakDays >= 7 ? '保持得很棒！' : '继续积累专注力' }}</em></div><div class="week-row"><div v-for="day in dashboard.data.trend.days" :key="day.date" :class="{ today: day.today, future: !day.studied && !day.today }"><i>{{ day.studied ? '✓' : '' }}</i><span>{{ day.label }}</span></div></div></section>
    <section class="tasks-card glass-card"><div class="card-heading"><div><span class="mini-icon blue">✓</span><h2>今日任务</h2></div><span class="counter">{{ dashboard.data.tasksCompleted }} / {{ dashboard.data.totalTasks }}</span></div><div class="task-list"><label v-for="task in dashboard.data.tasks" :key="task.id" class="task" :class="{ done: task.completed }"><input type="checkbox" :checked="task.completed" :disabled="dashboard.savingTaskIds.has(task.id)" @change="dashboard.toggleTask(task.id)" /><i></i><span><strong>{{ task.title }}</strong><small>{{ task.subject }} · {{ task.estimatedMinutes }} 分钟</small></span><em>+{{ task.xpReward }} XP</em></label></div><p v-if="dashboard.error" class="practice-error">{{ dashboard.error }}</p></section>
    <section class="chart-card glass-card"><div class="card-heading"><div><span class="mini-icon cyan">◒</span><h2>本周学习趋势</h2></div><span class="trend-up" :class="{ down: dashboard.data.trend.changePercent < 0 }">{{ dashboard.data.trend.changePercent >= 0 ? '↑' : '↓' }} {{ Math.abs(dashboard.data.trend.changePercent) }}%</span></div><div class="chart-summary"><strong>{{ totalHours }}</strong><span>小时</span><small>{{ weekDifference }}</small></div><div class="bar-chart"><div v-for="day in dashboard.data.trend.days" :key="day.date"><i :style="{ height: `${Math.max(day.minutes ? 10 : 3, Math.round(day.minutes / maxMinutes * 100))}%` }" :class="{ highlight: day.today }" :title="`${day.minutes} 分钟`"></i><span>{{ day.label }}</span></div></div></section>
    <section class="recommend-card glass-card"><div class="recommend-icon">✦</div><div><span>智能推荐</span><h2>{{ dashboard.data.recommendation.title }}</h2><p>{{ dashboard.data.recommendation.description }}</p></div><RouterLink class="recommend-link" :to="dashboard.data.recommendation.route">查看推荐 →</RouterLink></section>
  </div>
</template>
