<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useDashboardStore } from '../stores/dashboard'
import { loadJourney, type JourneyData } from '../services/journey'
import { buildJourneyPlan } from '../content/journeyPlan'
import { getStageGuidance, projectGoals } from '../content/beginnerGuidance'
import BeginnerHelp from '../components/BeginnerHelp.vue'
import NextDiscovery from '../components/NextDiscovery.vue'

const dashboard = useDashboardStore()
const journey = ref<JourneyData | null>(null)
const loading = ref(true)
const journeyError = ref('')
const goal = computed(() => projectGoals[journey.value?.project ?? 'portfolio'] ?? projectGoals.portfolio!)
const steps = computed(() => journey.value?.configured ? buildJourneyPlan(journey.value) : [])
const nextStep = computed(() => steps.value.find(step => !journey.value?.completedStages.includes(step.id) && !journey.value?.skippedStages.includes(step.id)))
const guide = computed(() => nextStep.value && journey.value ? getStageGuidance(journey.value, nextStep.value.id) : null)
const finished = computed(() => Boolean(journey.value?.configured && !nextStep.value))
const completedCount = computed(() => steps.value.filter(step => journey.value?.completedStages.includes(step.id)).length)
const skippedCount = computed(() => steps.value.filter(step => journey.value?.skippedStages.includes(step.id) && !journey.value?.completedStages.includes(step.id)).length)
const totalHours = computed(() => ((dashboard.data?.trend.totalMinutes ?? 0) / 60).toFixed(1))
async function readJourney() {
  loading.value = true
  journeyError.value = ''
  try { journey.value = await loadJourney() }
  catch (cause) { journeyError.value = cause instanceof Error ? cause.message : '暂时无法读取作品路线' }
  finally { loading.value = false }
}
onMounted(() => { void readJourney(); void dashboard.load(true) })
</script>

<template>
  <div class="goal-workbench">
    <div v-if="loading" class="state-card glass-card"><span class="loader"></span><p>正在打开你的作品路线…</p></div>
    <div v-else-if="journeyError" class="state-card glass-card"><strong>作品路线暂时无法加载</strong><p>{{ journeyError }}</p><button type="button" @click="readJourney">重新加载</button></div>
    <template v-else>
      <section class="build-focus-card glass-card">
        <div class="build-focus-copy">
          <span class="eyebrow"><i></i>{{ journey?.configured ? '我的作品 · 下一步从这里继续' : '还不知道从哪里开始？' }}</span>
          <h2>{{ !journey?.configured ? '先找到一个你想做出来的小网站' : finished ? '第一个网站完成了，接下来你想探索什么？' : nextStep?.title }}</h2>
          <p>{{ !journey?.configured ? '从个人作品集、兴趣博客和校园信息站里，选一个让你想动手的目标。先体验它，再跟着第一步做。' : finished ? '回头看看最让你有成就感的一步，再选一个小想法继续尝试。' : nextStep?.description }}</p>
          <div v-if="nextStep" class="build-next-meta"><span>{{ goal.name }}</span><span>{{ nextStep.time }} · 可以分几次完成</span></div>
          <RouterLink class="primary-link" :to="nextStep?.route ?? '/courses'">{{ !journey?.configured ? '看看我能做什么' : finished ? '回看我的建站之旅' : '继续做这一步' }} <span>→</span></RouterLink>
          <RouterLink v-if="nextStep" class="quiet-link" to="/courses">查看完整路线</RouterLink>
        </div>
        <aside class="work-goal"><small>{{ journey?.configured ? '我正在做什么' : '不用先决定学哪门语言' }}</small><h3>{{ journey?.configured ? goal.name : '一个能发给朋友的网址' }}</h3><p>{{ journey?.configured ? goal.purpose : '从写出第一张网页开始。每加一个功能，再学一点用得上的知识。' }}</p><template v-if="journey?.configured"><small>想帮助的人</small><p>{{ goal.audience }}</p><span>已亲手完成 {{ completedCount }} 站<template v-if="skippedCount"> · 已跳过 {{ skippedCount }} 站</template></span></template></aside>
      </section>

      <section v-if="guide && nextStep" class="next-action-card glass-card">
        <div class="next-action-heading"><span>今天只盯住这个小目标</span><h3>{{ guide.after }}</h3><p>{{ guide.why }}</p></div>
        <div class="next-action-boundaries"><div><h4>先用到这些</h4><p>{{ guide.need }}</p></div><div><h4>做到这里，可以先停下来</h4><p>{{ guide.enough }}</p></div><div><h4>暂时不用着急</h4><p>{{ guide.later }}</p></div></div>
        <BeginnerHelp :stage="nextStep.id" />
      </section>
      <section v-else-if="!journey?.configured" class="first-nudge glass-card"><h3>没有作品、没有基础，也有起点</h3><p>一段自我介绍、一篇游戏心得、一条校园活动，都可以成为第一张网页的内容。先选一件你愿意分享的事。</p><RouterLink to="/courses">从示例里找一点灵感 →</RouterLink></section>
      <NextDiscovery v-if="finished" />
    </template>

    <details class="workbench-extras glass-card">
      <summary>想换换脑子，或者查一点资料 <span>可选工具与学习记录</span></summary>
      <div class="extra-links"><RouterLink to="/knowledge"><strong>知识工具箱 →</strong><small>碰到不懂的概念时再来查</small></RouterLink><RouterLink to="/practice"><strong>在线练习 →</strong><small>用几道题试试刚学的知识</small></RouterLink><RouterLink to="/games"><strong>趣味闯关 →</strong><small>通过小挑战探索编程的乐趣</small></RouterLink><RouterLink to="/community"><strong>建站社区 →</strong><small>看看别人的作品，交流卡点</small></RouterLink></div>
      <p v-if="dashboard.loading && !dashboard.data">正在读取可选学习记录…</p>
      <div v-if="dashboard.data" class="optional-records"><details><summary>我的可选加练 · {{ dashboard.data.tasksCompleted }} / {{ dashboard.data.totalTasks }}</summary><p>有兴趣再做，不影响你从建站主线继续。</p><div class="task-list"><label v-for="task in dashboard.data.tasks" :key="task.id" class="task" :class="{ done: task.completed }"><input type="checkbox" :checked="task.completed" :disabled="dashboard.savingTaskIds.has(task.id)" @change="dashboard.toggleTask(task.id)" /><i></i><span><strong>{{ task.title }}</strong><small>{{ task.subject }} · {{ task.estimatedMinutes }} 分钟</small></span></label></div></details><details><summary>查看本周学习记录</summary><p>连续学习 {{ dashboard.data.streakDays }} 天 · 本周记录 {{ totalHours }} 小时。</p><p>记录帮助你回顾投入，可以按照自己的节奏继续做作品。</p></details></div>
      <p v-if="dashboard.error" class="practice-error">{{ dashboard.error }} <button type="button" @click="dashboard.load(true)">重试学习记录</button></p>
    </details>
  </div>
</template>

<style scoped>
.goal-workbench { display: grid; gap: 22px; }.work-goal { padding: 23px; border: 1px solid var(--border); border-radius: 16px; background: rgba(255,255,255,.58); }.work-goal small, .next-action-heading > span { color: #477c9f; font-size: 12px; font-weight: 600; }.work-goal h3 { font-size: 22px; margin: 10px 0; }.work-goal p { color: #5b7489; font-size: 14px; line-height: 1.8; }.work-goal > span { display: block; border-top: 1px solid var(--border); padding-top: 13px; font-size: 12px; color: #647d90; }.quiet-link { display: inline-block; margin: 16px; color: #426f91; font-size: 14px; }.next-action-card, .first-nudge { padding: 27px; }.next-action-heading h3 { font-size: 23px; margin: 12px 0; }.next-action-heading p, .first-nudge p, .workbench-extras p { color: #5b7489; line-height: 1.8; font-size: 14px; }.next-action-boundaries { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 24px; margin-top: 24px; }.next-action-boundaries h4 { font-size: 14px; margin: 0 0 8px; }.next-action-boundaries p { color: #5b7489; font-size: 14px; line-height: 1.8; margin: 0; }.workbench-extras { padding: 23px; }.workbench-extras summary { cursor: pointer; font-size: 14px; line-height: 1.8; }.workbench-extras > summary { font-weight: 600; }.workbench-extras summary span { display: inline-block; margin-left: 15px; color: #647d90; font-size: 12px; font-weight: 400; }.extra-links { display: grid; grid-template-columns: repeat(4,minmax(0,1fr)); gap: 14px; margin: 22px 0; }.extra-links a { padding: 16px; border: 1px solid var(--border); border-radius: 12px; color: #426f91; }.extra-links strong { font-size: 14px; }.extra-links small { display: block; font-size: 12px; line-height: 1.7; margin-top: 8px; color: #647d90; }.optional-records { display: grid; gap: 15px; }.first-nudge a { color: #426f91; }
@media(max-width: 800px) { .next-action-boundaries { grid-template-columns: 1fr; gap: 18px; }.extra-links { grid-template-columns: repeat(2,minmax(0,1fr)); }.next-action-card { padding: 20px; } }
</style>
