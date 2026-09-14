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
const journeyPercent = computed(() => journeySteps.value.length
  ? Math.round(resolvedJourneySteps.value / journeySteps.value.length * 100)
  : 0)
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
  <div v-else-if="dashboard.data" class="dashboard-grid dashboard-workbench">
    <section class="build-focus-card glass-card">
      <div class="build-focus-copy">
        <span class="eyebrow"><i></i> {{ journey?.configured ? '今日主线 · 先完成这一项' : '从这里开始' }}</span>
        <h2>{{ journeyHeadline }}</h2>
        <p>{{ journey?.configured ? journeyDescription : '回答几个简单问题，知途会按你的项目目标和技术选择排好全部建站步骤。' }}</p>
        <div v-if="journey?.configured && nextJourneyStep" class="build-next-meta"><span>预计 {{ nextJourneyStep.time }}</span><span>{{ projectName }}</span></div>
        <RouterLink class="primary-link" :to="journeyRoute">{{ journeyAction }} <span>→</span></RouterLink>
      </div>
      <div class="journey-progress-panel">
        <header><span>建站路线</span><strong>{{ resolvedJourneySteps }} / {{ journeySteps.length || 1 }} 站</strong></header>
        <div class="journey-progress-track"><i :style="{ width: `${journeyPercent}%` }"></i></div>
        <div class="journey-progress-copy"><small>{{ journey?.configured ? '当前项目' : '等待生成路线' }}</small><strong>{{ projectName || '我的第一个网站' }}</strong><span>{{ journey?.configured ? `${journeyPercent}% 已推进` : '选方向、前端与后端后即可开始' }}</span></div>
      </div>
    </section>

    <section class="today-plan-card glass-card">
      <div class="card-heading"><div><span class="mini-icon blue">→</span><h2>主线后的学习安排</h2></div><span class="counter">按需完成</span></div>
      <RouterLink v-if="dashboard.data.focus?.courseId" class="dashboard-course-next" :to="`/courses/${dashboard.data.focus.courseId}`">
        <i>01</i><div><small>配套课程 · 约 {{ dashboard.data.focus.estimatedMinutes }} 分钟</small><strong>{{ dashboard.data.focus.courseTitle }}</strong><span>下一节：{{ dashboard.data.focus.chapterTitle }}</span></div><em>{{ dashboard.data.focus.progressPercent }}% →</em>
      </RouterLink>
      <div class="optional-task-heading"><span>02</span><div><strong>可选加练</strong><small>主线卡住时，选一项补知识；无需全部完成</small></div><em>{{ dashboard.data.tasksCompleted }} / {{ dashboard.data.totalTasks }}</em></div>
      <div class="task-list"><label v-for="task in dashboard.data.tasks" :key="task.id" class="task" :class="{ done: task.completed }"><input type="checkbox" :checked="task.completed" :disabled="dashboard.savingTaskIds.has(task.id)" @change="dashboard.toggleTask(task.id)" /><i></i><span><strong>{{ task.title }}</strong><small>{{ task.subject }} · {{ task.estimatedMinutes }} 分钟</small></span><em>+{{ task.xpReward }} XP</em></label></div>
      <p v-if="dashboard.error" class="practice-error">{{ dashboard.error }}</p>
    </section>

    <section class="week-overview-card glass-card">
      <div class="card-heading"><div><span class="mini-icon cyan">◒</span><h2>本周进度</h2></div><span class="live-badge">实时更新</span></div>
      <div class="week-metrics"><div><small>连续学习</small><strong>{{ dashboard.data.streakDays }}<em> 天</em></strong></div><div><small>本周投入</small><strong>{{ totalHours }}<em> 小时</em></strong></div><div><small>周目标</small><strong>{{ dashboard.data.weeklyGoalPercent }}<em>%</em></strong></div></div>
      <div class="weekly-goal-track"><i :style="{ width: `${dashboard.data.weeklyGoalPercent}%` }"></i></div>
      <p class="weekly-goal-copy">{{ dashboard.data.weeklyRemainingMinutes ? `距离 10 小时目标还差 ${dashboard.data.weeklyRemainingMinutes} 分钟` : '本周目标已完成，可以安心复盘作品' }}</p>
      <div class="compact-chart"><div v-for="day in dashboard.data.trend.days" :key="day.date" :class="{ today: day.today }"><span><i :style="{ height: `${Math.max(day.minutes ? 10 : 3, Math.round(day.minutes / maxMinutes * 100))}%` }"></i></span><small>{{ day.label }}</small></div></div>
      <footer><span>{{ weekDifference }}</span><em :class="{ down: dashboard.data.trend.changePercent < 0 }">{{ dashboard.data.trend.changePercent >= 0 ? '↑' : '↓' }} {{ Math.abs(dashboard.data.trend.changePercent) }}%</em></footer>
    </section>

    <section class="recommend-card glass-card"><div class="recommend-icon">✦</div><div><span>下一项推荐</span><h2>{{ dashboard.data.recommendation.title }}</h2><p>{{ dashboard.data.recommendation.description }}</p></div><RouterLink class="recommend-link" :to="dashboard.data.recommendation.route">查看建议 →</RouterLink></section>
  </div>
</template>
