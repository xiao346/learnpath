<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useDashboardStore } from '../stores/dashboard'
import { loadJourney, type JourneyData } from '../services/journey'
import { buildJourneyPlan, type JourneyPlanStep } from '../content/journeyPlan'

const dashboard = useDashboardStore()
const journey = ref<JourneyData | null>(null)
const projectNames: Record<string, string> = { portfolio: '个人作品集', blog: '兴趣博客', campus: '校园信息站' }
const projectName = computed(() => journey.value ? (projectNames[journey.value.project] ?? '我的第一个网站') : '')
const journeySteps = computed(() => journey.value ? buildJourneyPlan(journey.value) : [])
const isStepResolved = (step: JourneyPlanStep) => Boolean(journey.value && (
  journey.value.completedStages.includes(step.id as JourneyData['completedStages'][number])
  || journey.value.skippedStages.includes(step.id as JourneyData['skippedStages'][number])
))
const nextJourneyStep = computed(() => journeySteps.value.find(step => !isStepResolved(step)) ?? null)
const resolvedJourneySteps = computed(() => journeySteps.value.filter(isStepResolved).length)
const journeyHeadline = computed(() => {
  if (!journey.value?.configured) return '选择技术路线，开始做第一个网站'
  if (!nextJourneyStep.value) return `${projectName.value}已经完成，可以展示了`
  return `下一步：${nextJourneyStep.value.title}`
})
const journeyDescription = computed(() => nextJourneyStep.value?.description
  ?? '你的第一份网站作品已经通过上线检查，可以继续补充内容，或开始构思下一个项目。')
const journeyRoute = computed(() => journey.value?.configured ? nextJourneyStep.value?.route ?? '/courses' : '/courses')
const journeyAction = computed(() => journey.value?.configured ? nextJourneyStep.value ? '进入这一站' : '查看完整路线' : '生成我的路线')
const maxMinutes = computed(() => Math.max(1, ...(dashboard.data?.trend.days.map((day) => day.minutes) ?? [1])))
const totalHours = computed(() => ((dashboard.data?.trend.totalMinutes ?? 0) / 60).toFixed(1))
const differenceHours = computed(() => Math.abs(((dashboard.data?.trend.totalMinutes ?? 0) - (dashboard.data?.trend.previousWeekMinutes ?? 0)) / 60).toFixed(1))
const weekDifference = computed(() => {
  const difference = (dashboard.data?.trend.totalMinutes ?? 0) - (dashboard.data?.trend.previousWeekMinutes ?? 0)
  if (difference === 0) return '与上周持平'
  return `比上周${difference > 0 ? '多' : '少'} ${differenceHours.value} 小时`
})
onMounted(async () => {
  await Promise.all([
    dashboard.load(true),
    loadJourney().then((data) => { journey.value = data }).catch(() => { journey.value = null }),
  ])
})
</script>

<template>
  <div v-if="dashboard.loading && !dashboard.data" class="state-card glass-card"><span class="loader"></span><p>正在汇总你的学习数据…</p></div>
  <div v-else-if="dashboard.error && !dashboard.data" class="state-card glass-card"><strong>首页数据暂时无法加载</strong><p>{{ dashboard.error }}</p><button @click="dashboard.load(true)">重新加载</button></div>
  <div v-else-if="dashboard.data" class="dashboard-grid">
    <section class="build-focus-card glass-card"><div class="build-focus-copy"><span class="eyebrow"><i></i> {{ journey?.configured ? '今天只推进这一件事' : '从这里开始' }}</span><h2>{{ journeyHeadline }}</h2><p>{{ journey?.configured ? journeyDescription : '告诉我们你想做什么、想用什么技术，知途会为你排好每一步。' }}</p><div v-if="journey?.configured && nextJourneyStep" class="build-next-meta"><span>◷ {{ nextJourneyStep.time }}</span><span>{{ resolvedJourneySteps }} / {{ journeySteps.length }} 站已推进</span></div><RouterLink class="primary-link" :to="journeyRoute">{{ journeyAction }} <span>→</span></RouterLink></div><div class="build-window" aria-hidden="true"><div><i></i><i></i><i></i></div><code><b>&lt;h1&gt;</b>你好，Web！<b>&lt;/h1&gt;</b><span>/* 你的作品从这里开始 */</span></code></div></section>
    <section class="streak-card glass-card"><div class="card-heading"><div><span class="mini-icon purple">↗</span><h2>连续学习</h2></div><span class="live-badge">本周实时</span></div><div class="streak-number"><strong>{{ dashboard.data.streakDays }}</strong><span>天</span><em>{{ dashboard.data.streakDays >= 7 ? '保持得很棒！' : '继续积累专注力' }}</em></div><div class="week-row"><div v-for="day in dashboard.data.trend.days" :key="day.date" :class="{ today: day.today, future: !day.studied && !day.today }"><i>{{ day.studied ? '✓' : '' }}</i><span>{{ day.label }}</span></div></div></section>
    <section class="tasks-card glass-card"><div class="card-heading"><div><span class="mini-icon blue">✓</span><h2>可选加练</h2></div><span class="counter">{{ dashboard.data.tasksCompleted }} / {{ dashboard.data.totalTasks }}</span></div><div class="task-list"><label v-for="task in dashboard.data.tasks" :key="task.id" class="task" :class="{ done: task.completed }"><input type="checkbox" :checked="task.completed" :disabled="dashboard.savingTaskIds.has(task.id)" @change="dashboard.toggleTask(task.id)" /><i></i><span><strong>{{ task.title }}</strong><small>{{ task.subject }} · {{ task.estimatedMinutes }} 分钟</small></span><em>+{{ task.xpReward }} XP</em></label></div><p v-if="dashboard.error" class="practice-error">{{ dashboard.error }}</p></section>
    <section class="chart-card glass-card"><div class="card-heading"><div><span class="mini-icon cyan">◒</span><h2>本周学习趋势</h2></div><span class="trend-up" :class="{ down: dashboard.data.trend.changePercent < 0 }">{{ dashboard.data.trend.changePercent >= 0 ? '↑' : '↓' }} {{ Math.abs(dashboard.data.trend.changePercent) }}%</span></div><div class="chart-summary"><strong>{{ totalHours }}</strong><span>小时</span><small>{{ weekDifference }}</small></div><div class="bar-chart"><div v-for="day in dashboard.data.trend.days" :key="day.date"><i :style="{ height: `${Math.max(day.minutes ? 10 : 3, Math.round(day.minutes / maxMinutes * 100))}%` }" :class="{ highlight: day.today }" :title="`${day.minutes} 分钟`"></i><span>{{ day.label }}</span></div></div></section>
    <section class="recommend-card glass-card"><div class="recommend-icon">✦</div><div><span>拓展推荐</span><h2>{{ dashboard.data.recommendation.title }}</h2><p>{{ dashboard.data.recommendation.description }}</p></div><RouterLink class="recommend-link" :to="dashboard.data.recommendation.route">有空再看 →</RouterLink></section>
  </div>
</template>
