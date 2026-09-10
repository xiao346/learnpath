<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api, type CourseSummary } from '../services/api'
import {
  completeJourneyStage,
  defaultJourney,
  loadJourney,
  saveJourneyStageEvidence,
  type JourneyData,
} from '../services/journey'
import { buildJourneyPlan, requiredJourneyStageIds } from '../content/journeyPlan'

type TechnicalStageId = 'framework' | 'backend' | 'database'
type ProjectProfile = {
  item: string
  collection: string
  feature: string
  endpoint: string
  table: string
  fields: string
  sqlFields: string[]
}

const route = useRoute()
const journey = ref<JourneyData>(defaultJourney)
const courses = ref<CourseSummary[]>([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const artifactChecked = ref(false)
const edgeChecked = ref(false)
const evidence = ref('')

const stageId = computed(() => String(route.params.stageId) as TechnicalStageId)
const validStage = computed(() => ['framework', 'backend', 'database'].includes(stageId.value))
const projectProfiles: Record<string, ProjectProfile> = {
  portfolio: { item: '作品', collection: '作品列表', feature: '按类别筛选作品', endpoint: 'works', table: 'portfolio_item', fields: 'title, summary, category, url, created_at', sqlFields: ['title VARCHAR(120) NOT NULL', 'summary VARCHAR(500) NOT NULL', 'category VARCHAR(40) NOT NULL', 'url VARCHAR(500)', 'created_at TIMESTAMP NOT NULL'] },
  blog: { item: '文章', collection: '文章列表', feature: '按主题筛选并展开文章', endpoint: 'posts', table: 'blog_post', fields: 'title, summary, content, topic, created_at', sqlFields: ['title VARCHAR(120) NOT NULL', 'summary VARCHAR(500) NOT NULL', 'content TEXT NOT NULL', 'topic VARCHAR(40) NOT NULL', 'created_at TIMESTAMP NOT NULL'] },
  campus: { item: '活动', collection: '活动列表', feature: '按日期筛选并查看活动地点', endpoint: 'events', table: 'campus_event', fields: 'title, event_date, location, description, created_at', sqlFields: ['title VARCHAR(120) NOT NULL', 'event_date TIMESTAMP NOT NULL', 'location VARCHAR(160) NOT NULL', 'description TEXT NOT NULL', 'created_at TIMESTAMP NOT NULL'] },
}
const profile = computed(() => projectProfiles[journey.value.project] ?? projectProfiles.portfolio)
const plan = computed(() => buildJourneyPlan(journey.value))
const planStep = computed(() => plan.value.find((step) => step.id === stageId.value))
const course = computed(() => courses.value.find((item) => item.title === planStep.value?.courseTitle))
const courseDone = computed(() => course.value?.progressPercent === 100)
const stageCompleted = computed(() => journey.value.completedStages.includes(stageId.value))
const stageIncluded = computed(() => validStage.value && requiredJourneyStageIds(journey.value).includes(stageId.value))
const stageUnlocked = computed(() => {
  const order = requiredJourneyStageIds(journey.value)
  const index = order.indexOf(stageId.value)
  if (index < 0) return false
  const resolved = new Set([...journey.value.completedStages, ...journey.value.skippedStages])
  return order.slice(0, index).every((id) => resolved.has(id))
})
const evidenceReady = computed(() => evidence.value.trim().length >= 20)
const canComplete = computed(() => stageIncluded.value && stageUnlocked.value && courseDone.value
  && artifactChecked.value && edgeChecked.value && evidenceReady.value)

const stageContent = computed(() => {
  const item = profile.value.item
  const collection = profile.value.collection
  if (stageId.value === 'framework') {
    return {
      kicker: 'Vue 项目站', title: '把现有页面迁移成可维护的 Vue 应用',
      outcome: `新增一条${item}数据，${collection}会自动出现新卡片`,
      concepts: ['组件边界', 'Props 数据输入', 'v-for 列表渲染', '响应式状态'],
      tasks: [
        { title: '画出组件边界', detail: `把页面拆成 AppHeader、${item}List、${item}Card 和 AppFooter，写清每个组件负责什么。` },
        { title: '迁移真实内容', detail: `把已有${item}整理成对象数组，通过 Props 传入卡片，禁止复制多份相同 HTML。` },
        { title: '恢复核心功能', detail: `把“${profile.value.feature}”迁移成响应式状态，确认重构前后的行为一致。` },
      ],
      code: `<script setup>\nconst items = ref([\n  { id: 1, title: '我的第一条${item}' },\n  { id: 2, title: '继续完善的${item}' }\n])\n<\/script>\n\n<template>\n  <${item}Card v-for="item in items" :key="item.id" :item="item" />\n</template>`,
      acceptance: [`至少拆出 3 个有明确职责的组件`, `${collection}由数组和 v-for 生成`, `新增数据后页面自动更新，${profile.value.feature}仍然可用`],
      evidencePrompt: `例如：我把${collection}拆成了哪些组件，数据放在哪里，新增一条${item}后如何验证。`,
    }
  }
  if (stageId.value === 'backend') {
    const isPython = journey.value.backend === 'python'
    return {
      kicker: `${isPython ? 'FastAPI' : 'Spring Boot'} 接口站`, title: `让${collection}从真实接口读取`,
      outcome: `GET /api/${profile.value.endpoint} 返回真实 JSON，页面能处理加载与失败`,
      concepts: ['请求与响应', 'REST 路径', 'JSON 契约', '错误状态'],
      tasks: [
        { title: '先写接口契约', detail: `确定 GET /api/${profile.value.endpoint} 的字段、成功状态码，以及数据不存在时怎样响应。` },
        { title: '实现两条真实数据', detail: `用${isPython ? 'FastAPI 路由与响应模型' : 'Controller、Service 和 DTO'}返回符合契约的${item}列表。` },
        { title: '接入前端三种状态', detail: `页面必须分别显示加载中、${collection}和请求失败提示，不能只处理成功情况。` },
      ],
      code: isPython
        ? `@app.get('/api/${profile.value.endpoint}', response_model=list[${item}View])\ndef list_${profile.value.endpoint}():\n    return service.find_all()`
        : `@GetMapping("/api/${profile.value.endpoint}")\npublic List<${item}View> list() {\n    return service.findAll();\n}`,
      acceptance: [`浏览器或接口工具能看到 200 与正确 JSON`, `前端不再使用写死的${item}数组`, `断开后端后页面显示可理解的错误提示`],
      evidencePrompt: `例如：我完成了哪个接口、返回哪些字段、前端如何处理成功和失败，以及我实际测试的地址。`,
    }
  }
  return {
    kicker: `${journey.value.database === 'sqlite' ? 'SQLite' : 'MySQL'} 数据站`, title: `让${item}刷新和重启后仍然存在`,
    outcome: `${profile.value.table} 表保存真实数据，接口重启后仍能读取`,
    concepts: ['主键与字段', '约束', 'CRUD', '持久化验证'],
    tasks: [
      { title: '从页面反推表结构', detail: `为 ${profile.value.table} 设计字段：${profile.value.fields}，标明主键、必填项和长度。` },
      { title: '打通新增与查询', detail: `通过接口写入一条真实${item}，再查询并显示到${collection}，不要只在数据库工具里手工插入。` },
      { title: '完成重启验证', detail: `记住新数据的 id，重启后端后重新请求并刷新页面，确认同一条${item}仍然存在。` },
    ],
    code: `CREATE TABLE ${profile.value.table} (\n  id BIGINT PRIMARY KEY,\n  ${profile.value.sqlFields.join(',\n  ')}\n);`,
    acceptance: [`表字段与页面真实需要对应`, `新增与查询共用同一条数据链路`, `刷新页面并重启服务后数据仍然存在`],
    evidencePrompt: `例如：我设计了哪些字段、插入了哪条${item}、重启服务后用什么步骤确认数据仍在。`,
  }
})

const courseRoute = computed(() => course.value ? {
  path: `/courses/${course.value.id}`,
  query: { from: 'journey', stage: stageId.value },
} : '/knowledge')

async function finishStage() {
  if (!canComplete.value || saving.value) return
  saving.value = true
  error.value = ''
  try {
    journey.value = await saveJourneyStageEvidence(stageId.value, evidence.value.trim())
    journey.value = await completeJourneyStage(stageId.value)
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '项目阶段保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const [journeyData, courseData] = await Promise.all([
      loadJourney(),
      api<CourseSummary[]>('/api/courses'),
    ])
    journey.value = journeyData
    courses.value = courseData
    evidence.value = journeyData.stageEvidence.find((item) => item.stageId === stageId.value)?.evidence ?? ''
    if (journeyData.completedStages.includes(stageId.value)) {
      artifactChecked.value = true
      edgeChecked.value = true
    }
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '项目阶段加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="technical-stage-page">
    <RouterLink class="back-link" to="/courses">← 返回我的建站路线</RouterLink>
    <div v-if="loading" class="state-card glass-card"><span class="loader"></span><p>正在连接课程与项目任务…</p></div>
    <div v-else-if="!stageIncluded" class="state-card glass-card"><strong>当前路线不需要这一站</strong><p>原生路线不需要 Vue 迁移；静态路线也不需要后端和数据库。路线会自动跳过不适用的技术。</p><RouterLink to="/courses">查看当前路线</RouterLink></div>
    <template v-else>
      <header class="first-lesson-header glass-card"><div><span class="lesson-kicker">{{ stageContent.kicker }} · 课程学习 + 项目实做</span><h2>{{ stageContent.title }}</h2><p>{{ planStep?.description }}完成课程只是准备，只有把知识应用到正在制作的网站，这一站才算完成。</p></div><div class="lesson-win"><small>这一站的成果</small><strong>{{ stageContent.outcome }}</strong><span>{{ stageContent.concepts.join(' · ') }}</span></div></header>

      <section class="lesson-agenda glass-card" aria-label="本阶段学习任务"><div><span>本阶段闭环</span><h3>学习、应用、验证、记录</h3></div><ol><li><i>1</i><span><b>完成课程</b><small>按章节补齐必要知识</small></span></li><li><i>2</i><span><b>修改项目</b><small>在同一个网站继续开发</small></span></li><li><i>3</i><span><b>边界验证</b><small>增加数据或制造失败情况</small></span></li><li><i>4</i><span><b>留下证据</b><small>记录改动与验证结果</small></span></li></ol></section>

      <div v-if="!stageUnlocked" class="stage-gate glass-card"><span>阶段尚未开放</span><h3>先完成或跳过路线中的上一站</h3><p>项目阶段必须按依赖顺序推进，否则这一站没有可以继续修改的成果。</p><RouterLink to="/courses">返回路线查看下一步 →</RouterLink></div>

      <div v-else class="technical-stage-grid">
        <main class="technical-project-work glass-card">
          <div class="workbench-heading"><span>应用到你的项目</span><h3>这次继续修改现有{{ profile.collection }}</h3><p>不要新建互不相关的课堂示例。下面三项任务都要落在前几站已经完成的网站里。</p></div>
          <div class="technical-task-list"><article v-for="(task, index) in stageContent.tasks" :key="task.title"><i>{{ index + 1 }}</i><div><strong>{{ task.title }}</strong><p>{{ task.detail }}</p></div></article></div>
          <section class="technical-contract"><span>参考骨架</span><h3>先看清数据与代码的连接点</h3><pre><code>{{ stageContent.code }}</code></pre><p>示例只说明结构。请把名称、字段和内容换成自己的项目，不要原样复制后就算完成。</p></section>
          <section class="technical-acceptance"><span>项目验收</span><h3>三个结果缺一不可</h3><ul><li v-for="item in stageContent.acceptance" :key="item">{{ item }}</li></ul></section>
        </main>

        <aside class="technical-stage-side">
          <section class="technical-course-card glass-card"><span>知识准备</span><h3>{{ planStep?.courseTitle }}</h3><div class="technical-course-progress"><i :style="{ width: `${course?.progressPercent ?? 0}%` }"></i></div><p>{{ course?.completedLessons ?? 0 }} / {{ course?.totalLessons ?? 0 }} 章 · {{ course?.progressPercent ?? 0 }}%</p><RouterLink :to="courseRoute">{{ courseDone ? '复习课程章节' : course?.progressPercent ? '继续完成课程' : '开始必要课程' }} →</RouterLink><small v-if="courseDone">✓ 课程已完成，现在把知识应用到项目</small></section>
          <section class="technical-proof-card glass-card"><span>项目档案</span><h3>{{ stageCompleted ? '这一站已经完成' : '记录真实改动后完成' }}</h3><label><input v-model="artifactChecked" type="checkbox" /><i></i><b>我在前几站的同一个项目中完成了上述功能</b></label><label><input v-model="edgeChecked" type="checkbox" /><i></i><b>我按验收标准测试了正常情况和一个边界情况</b></label><label class="evidence-field"><b>改动与验证记录</b><textarea v-model="evidence" maxlength="600" :placeholder="stageContent.evidencePrompt"></textarea><small :class="{ ready: evidenceReady }">{{ evidence.trim().length }} / 20 字起</small></label><p v-if="!courseDone">完成配套课程后才能提交项目结果。</p><button type="button" :disabled="!canComplete || saving || stageCompleted" @click="finishStage">{{ saving ? '正在写入项目档案…' : stageCompleted ? '这一站已完成 ✓' : '保存档案并完成这一站' }}</button><small v-if="error" class="practice-error">{{ error }}</small><RouterLink v-if="stageCompleted" class="next-workshop-link" to="/courses">回到路线，继续下一站 →</RouterLink></section>
        </aside>
      </div>
    </template>
  </section>
</template>
