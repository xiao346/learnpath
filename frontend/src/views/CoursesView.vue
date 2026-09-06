<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { api, type CourseSummary } from '../services/api'
import { defaultJourney, loadJourney, saveJourneyConfiguration, type JourneyConfig, type JourneyStageId } from '../services/journey'

type Choice = { id: string; name: string; note: string; badge?: string }
type RoadmapStage = { id: JourneyStageId; number: string; title: string; subtitle: string; output: string; time: string; skills: string[]; route: string | null; course: string | null }

const projectChoices: Choice[] = [
  { id: 'portfolio', name: '个人作品集', note: '展示你的介绍、技能与第一批作品', badge: '推荐' },
  { id: 'blog', name: '兴趣博客', note: '分享游戏、电影、摄影或校园生活' },
  { id: 'campus', name: '校园信息站', note: '整理社团、活动与实用校园信息' },
]
const frontendChoices: Choice[] = [
  { id: 'vue', name: 'JavaScript + Vue 3', note: '先学必要基础，再用组件搭建现代页面', badge: '主流推荐' },
  { id: 'vanilla', name: 'HTML + CSS + JavaScript', note: '不使用框架，完整理解网页原理', badge: '基础必学' },
]
const backendChoices: Choice[] = [
  { id: 'java', name: 'Java + Spring Boot', note: '和大学 Java 课程衔接，适合完整项目', badge: '课程最完整' },
  { id: 'python', name: 'Python + FastAPI', note: '语法直观，适合快速理解接口' },
  { id: 'later', name: '先做静态网站', note: '先完成能分享的网页，之后再学后端' },
]
const databaseChoices: Choice[] = [
  { id: 'mysql', name: 'MySQL', note: '通用且适合系统学习数据库', badge: '推荐' },
  { id: 'sqlite', name: 'SQLite', note: '不用配置服务，适合轻量项目' },
  { id: 'later', name: '暂时不需要', note: '静态网站阶段可以跳过数据库' },
]

const courses = ref<CourseSummary[]>([])
const completedStages = ref<JourneyStageId[]>([])
const configured = ref(false)
const editing = ref(true)
const loadingJourney = ref(true)
const savingJourney = ref(false)
const journeyError = ref('')
const config = ref<JourneyConfig>({
  project: defaultJourney.project,
  frontend: defaultJourney.frontend,
  backend: defaultJourney.backend,
  database: defaultJourney.database,
})
const savedConfig = ref<JourneyConfig | null>(null)

const choiceName = (choices: Choice[], id: string) => choices.find((item) => item.id === id)?.name ?? id
const projectName = computed(() => choiceName(projectChoices, config.value.project))
const stackSummary = computed(() => [
  choiceName(frontendChoices, config.value.frontend),
  choiceName(backendChoices, config.value.backend),
  config.value.backend === 'later' ? null : choiceName(databaseChoices, config.value.database),
].filter(Boolean).join(' · '))
const selectedTechnologyGuide = computed(() => [
  {
    name: config.value.frontend === 'vue' ? 'Vue 3 前端' : '原生前端',
    role: '负责用户看见和点击的页面',
    detail: config.value.frontend === 'vue'
      ? 'Vue 把页面拆成可复用组件，并让数据变化自动更新界面。它仍然建立在 HTML、CSS、JavaScript 之上。'
      : '直接使用 HTML 放内容、CSS 管样式、JavaScript 做交互，更容易理解网页底层如何工作。',
  },
  {
    name: config.value.backend === 'java' ? 'Spring Boot 后端' : config.value.backend === 'python' ? 'FastAPI 后端' : '暂不使用后端',
    role: config.value.backend === 'later' ? '先完成只在浏览器运行的网站' : '负责业务规则、登录和数据接口',
    detail: config.value.backend === 'java'
      ? 'Spring Boot 用 Java 编写服务端程序，适合与大学 Java 课程衔接，也常用于完整业务系统。'
      : config.value.backend === 'python'
        ? 'FastAPI 用 Python 快速编写接口，类型清楚并自动生成接口文档，适合第一次理解后端。'
        : '个人主页和作品集可以先不接后端，完成发布后再逐步升级。',
  },
  {
    name: config.value.backend === 'later' ? '暂不使用数据库' : config.value.database === 'sqlite' ? 'SQLite 数据库' : 'MySQL 数据库',
    role: config.value.backend === 'later' ? '当前阶段无需保存服务器数据' : '负责长期保存用户、文章和作品',
    detail: config.value.backend === 'later'
      ? '静态网站的文字直接写在项目里，适合先把第一份作品快速做出来。'
      : config.value.database === 'sqlite'
        ? 'SQLite 把数据库放在一个文件里，不用单独启动服务，适合小项目。'
        : 'MySQL 是常用的关系数据库，适合系统学习表、SQL、事务和索引。',
  },
])

const courseLink = (title: string) => {
  const course = courses.value.find((item) => item.title === title)
  return course ? `/courses/${course.id}` : null
}
const launchDescription = (backend: string) => backend === 'later'
  ? '检查内容、手机布局和链接，整理公开地址与作品说明'
  : '打通页面、接口和数据，修掉错误并完成最终部署'
const routeCourseTitles = computed(() => [
  'HTML 与 CSS 网页设计',
  'JavaScript 网页交互',
  config.value.frontend === 'vue' ? 'Vue 3 前端开发' : null,
  config.value.backend === 'java' ? 'Java Web 应用开发' : config.value.backend === 'python' ? 'FastAPI 后端开发' : null,
  config.value.backend === 'later' ? null : '数据库原理',
  '软件工程与 Git 协作',
].filter((item): item is string => Boolean(item)))

const stages = computed(() => {
  const siteLabel = projectName.value
  const frontendFramework = config.value.frontend === 'vue' ? 'Vue 组件与状态' : 'JavaScript 页面交互'
  const backendLabel = config.value.backend === 'python' ? 'Python 接口' : 'Spring Boot 接口'
  const databaseLabel = config.value.database === 'sqlite' ? 'SQLite' : 'MySQL'
  const items: RoadmapStage[] = []
  const addStage = (stage: Omit<RoadmapStage, 'number'>) => items.push({ ...stage, number: String(items.length + 1).padStart(2, '0') })
  addStage({ id: 'intro', title: '你好，这是我的网站', subtitle: `把名字和兴趣放进${siteLabel}`, output: '浏览器中出现属于你的首页', time: '25 分钟', skills: ['HTML 结构', '文字与图片'], route: '/courses/first-page', course: null })
  addStage({ id: 'style', title: '给网站换件衣服', subtitle: '用颜色、字体和留白做出自己的风格', output: '完成一套个人视觉主题', time: '45 分钟', skills: ['CSS', '盒模型', 'Flex 布局'], route: '/courses/style-workshop', course: null })
  addStage({ id: 'interaction', title: '让按钮真的有反应', subtitle: `用${frontendFramework}做导航、卡片和主题切换`, output: '页面可以响应点击与输入', time: '1.5 小时', skills: ['变量与函数', '事件', '状态'], route: '/courses/interaction-workshop', course: null })
  if (config.value.frontend === 'vue') {
    addStage({ id: 'framework', title: '把页面装进 Vue', subtitle: '用组件和响应式数据重新组织不断长大的页面', output: '一个结构清楚的 Vue 单页应用', time: '4 小时', skills: ['Vue 3', '组件', '路由与状态'], route: courseLink('Vue 3 前端开发'), course: 'Vue 3 前端开发' })
  }
  addStage({ id: 'publish', title: '先发给朋友看看', subtitle: '保存代码、完成构建，把成果发布成可访问的网站', output: '获得第一个可分享的网站地址', time: '45 分钟', skills: ['Git', '构建', '静态部署'], route: '/courses/publish-workshop', course: null })
  if (config.value.backend !== 'later') {
    addStage({ id: 'backend', title: '给网站接上大脑', subtitle: `用${backendLabel}接收页面请求`, output: '前端成功读取自己的接口', time: '4 小时', skills: ['HTTP', 'REST API', '调试'], route: config.value.backend === 'java' ? courseLink('Java Web 应用开发') : courseLink('FastAPI 后端开发'), course: config.value.backend === 'java' ? 'Java Web 应用开发' : 'FastAPI 后端开发' })
    addStage({ id: 'database', title: '让内容记得住', subtitle: `把文章和作品保存到 ${databaseLabel}`, output: '刷新页面后数据依然存在', time: '3 小时', skills: ['数据表', 'SQL', '数据持久化'], route: courseLink('数据库原理'), course: '数据库原理' })
  }
  addStage({ id: 'launch', title: '上线前的最后巡检', subtitle: launchDescription(config.value.backend), output: `完成可以展示的${siteLabel}`, time: '1.5 小时', skills: ['质量检查', '移动端', '交付说明'], route: '/courses/launch-workshop', course: null })
  return items
})
const isStageCompleted = (stage: { id: JourneyStageId; course?: string | null }) => {
  if (completedStages.value.includes(stage.id)) return true
  if (!stage.course) return false
  return courses.value.find((course) => course.title === stage.course)?.progressPercent === 100
}
const completedCount = computed(() => stages.value.filter(isStageCompleted).length)
const currentIndex = computed(() => {
  const index = stages.value.findIndex((stage) => !isStageCompleted(stage))
  return index < 0 ? stages.value.length - 1 : index
})
const progressPercent = computed(() => Math.round(completedCount.value / Math.max(stages.value.length, 1) * 100))
const nextStageTitle = computed(() => stages.value[currentIndex.value]?.title ?? '全部完成')

const isStageUnlocked = (index: number) => index <= currentIndex.value
const stageActionLabel = (stage: { id: JourneyStageId; course: string | null }, index: number) => {
  if (isStageCompleted(stage)) return stage.course ? `复习 ${stage.course}` : '再次练习'
  if (index === currentIndex.value) return stage.course ? `学习 ${stage.course}` : index === 0 ? '开始第一站' : '进入这一站'
  return stage.course ? `学习 ${stage.course}` : '进入这一站'
}

function choose<K extends keyof JourneyConfig>(key: K, value: JourneyConfig[K]) {
  config.value[key] = value
  if (key === 'backend' && value === 'later') config.value.database = 'later'
  if (key === 'backend' && value !== 'later' && config.value.database === 'later') config.value.database = 'mysql'
}

async function createJourney() {
  if (savingJourney.value) return
  savingJourney.value = true
  journeyError.value = ''
  try {
    const saved = await saveJourneyConfiguration(config.value)
    savedConfig.value = { project: saved.project, frontend: saved.frontend, backend: saved.backend, database: saved.database }
    completedStages.value = saved.completedStages
    configured.value = true
    editing.value = false
  } catch (cause) {
    journeyError.value = cause instanceof Error ? cause.message : '建站路线保存失败'
  } finally {
    savingJourney.value = false
  }
}

function editJourney() { editing.value = true }
function cancelEditing() {
  if (savedConfig.value) config.value = { ...savedConfig.value }
  editing.value = false
}

onMounted(async () => {
  try { courses.value = await api<CourseSummary[]>('/api/courses') }
  catch { courses.value = [] }
  try {
    const journey = await loadJourney()
    config.value = { project: journey.project, frontend: journey.frontend, backend: journey.backend, database: journey.database }
    savedConfig.value = journey.configured ? { ...config.value } : null
    completedStages.value = journey.completedStages
    configured.value = journey.configured
    editing.value = !journey.configured
  } catch (cause) {
    journeyError.value = cause instanceof Error ? cause.message : '建站路线加载失败'
  } finally {
    loadingJourney.value = false
  }
})
</script>

<template>
  <section class="journey-page">
    <header class="journey-heading">
      <div><span class="eyebrow"><i></i> BUILD YOUR FIRST WEBSITE</span><h2>建站之旅</h2><p>先选好工具，再把一个想法一步步变成可以分享的网站。</p></div>
      <button v-if="configured && !editing" class="ghost-button" type="button" @click="editJourney">调整技术路线</button>
    </header>

    <div v-if="loadingJourney" class="state-card glass-card"><span class="loader"></span><p>正在读取你的建站路线…</p></div>
    <div v-else-if="journeyError && !configured" class="state-card glass-card"><strong>建站路线暂时无法读取</strong><p>{{ journeyError }}</p><button type="button" @click="$router.go(0)">重新加载</button></div>
    <section v-else-if="editing" class="journey-builder glass-card">
      <div class="builder-intro"><span>路线定制</span><h3>{{ configured ? '重新选择你的建站工具' : '你想做一个什么样的网站？' }}</h3><p>不了解这些名字也没关系，我们已经标出了更适合第一次建站的选择。</p></div>

      <section class="technology-primer">
        <div class="primer-heading"><span>先别急着选</span><h3>一个网站，其实是几位搭档在合作</h3><p>你现在不需要记住所有名词，只要先看懂数据从哪里来、页面由谁负责。</p></div>
        <div class="website-flow"><div><i>1</i><strong>浏览器</strong><span>展示页面、接收点击</span></div><b>→</b><div><i>2</i><strong>前端</strong><span>决定内容、样式与交互</span></div><b>⇄</b><div><i>3</i><strong>后端</strong><span>处理规则并提供接口</span></div><b>⇄</b><div><i>4</i><strong>数据库</strong><span>把数据长期保存</span></div></div>
        <div class="plain-tech-notes"><p><b>HTML</b> 放内容，<b>CSS</b> 管外观，<b>JavaScript</b> 让页面有反应；<b>Vue</b> 则把这三者组织成更容易维护的组件。</p><p>框架是一套帮你组织代码的工具，不是另一种编程语言。选 Vue 之后，路线仍会先带你补齐网页基础。</p></div>
      </section>

      <div class="choice-section"><div class="choice-title"><b>1</b><div><h4>先选一个作品方向</h4><p>课程中的例子会跟着你的主题变化。</p></div></div><div class="choice-grid project-choice-grid"><button v-for="item in projectChoices" :key="item.id" type="button" :class="{ selected: config.project === item.id }" @click="choose('project', item.id)"><span v-if="item.badge">{{ item.badge }}</span><strong>{{ item.name }}</strong><small>{{ item.note }}</small><i>{{ config.project === item.id ? '✓' : '○' }}</i></button></div></div>

      <div class="choice-section"><div class="choice-title"><b>2</b><div><h4>页面准备用什么做？</h4><p>HTML、CSS 和 JavaScript 会从零讲起。</p></div></div><div class="choice-grid"><button v-for="item in frontendChoices" :key="item.id" type="button" :class="{ selected: config.frontend === item.id }" @click="choose('frontend', item.id)"><span v-if="item.badge">{{ item.badge }}</span><strong>{{ item.name }}</strong><small>{{ item.note }}</small><i>{{ config.frontend === item.id ? '✓' : '○' }}</i></button></div></div>

      <div class="choice-section"><div class="choice-title"><b>3</b><div><h4>需要后端和数据库吗？</h4><p>想尽快看到成果，可以先做静态网站。</p></div></div><div class="stack-choice-columns"><div><label>后端语言与框架</label><div class="choice-grid compact"><button v-for="item in backendChoices" :key="item.id" type="button" :class="{ selected: config.backend === item.id }" @click="choose('backend', item.id)"><span v-if="item.badge">{{ item.badge }}</span><strong>{{ item.name }}</strong><small>{{ item.note }}</small><i>{{ config.backend === item.id ? '✓' : '○' }}</i></button></div></div><div :class="{ muted: config.backend === 'later' }"><label>数据存在哪里</label><div class="choice-grid compact"><button v-for="item in databaseChoices" :key="item.id" type="button" :disabled="config.backend === 'later'" :class="{ selected: config.database === item.id }" @click="choose('database', item.id)"><span v-if="item.badge">{{ item.badge }}</span><strong>{{ item.name }}</strong><small>{{ item.note }}</small><i>{{ config.database === item.id ? '✓' : '○' }}</i></button></div></div></div></div>

      <section class="selected-tech-guide"><div class="choice-title"><b>4</b><div><h4>你选的工具分别做什么？</h4><p>改变上面的选择，这里的解释也会跟着变化。</p></div></div><div><article v-for="item in selectedTechnologyGuide" :key="item.name"><span>{{ item.role }}</span><h4>{{ item.name }}</h4><p>{{ item.detail }}</p></article></div></section>

      <p v-if="journeyError" class="practice-error">{{ journeyError }}</p><footer class="builder-footer"><div><small>你的路线</small><strong>{{ projectName }}</strong><span>{{ stackSummary }}</span></div><div><button v-if="configured" class="ghost-button" type="button" @click="cancelEditing">取消</button><button class="primary-journey-button" type="button" :disabled="savingJourney" @click="createJourney">{{ savingJourney ? '正在保存到数据库…' : '生成我的建站之旅 →' }}</button></div></footer>
    </section>

    <template v-else>
      <section class="journey-summary glass-card"><div class="journey-project-mark">{{ projectName.slice(0, 1) }}</div><div><span>我的第一个网站</span><h3>{{ projectName }}</h3><p>{{ stackSummary }}</p></div><div class="journey-progress"><span><b>{{ completedCount }}</b> / {{ stages.length }} 站</span><div><i :style="{ width: `${progressPercent}%` }"></i></div><small>{{ completedCount === stages.length ? '路线已全部完成' : `下一站：${nextStageTitle}` }}</small></div></section>

      <section class="route-course-strip"><div><span>这条路线会用到</span><strong>{{ routeCourseTitles.length }} 门配套技术课</strong></div><div><template v-for="title in routeCourseTitles" :key="title"><RouterLink v-if="courseLink(title)" :to="courseLink(title) || '/knowledge'">{{ title }} <span>↗</span></RouterLink><span v-else>{{ title }}</span></template></div></section>

      <div class="roadmap-header"><div><span>你的专属路线</span><h3>每到一站，网站就多一个新本领</h3></div><p>不需要先学完整本教材。做到哪一步，就学习哪一步需要的知识。</p></div>

      <div class="journey-roadmap">
        <article v-for="(stage, index) in stages" :key="stage.number" class="roadmap-stage glass-card" :class="{ current: index === currentIndex, completed: isStageCompleted(stage), locked: !isStageUnlocked(index) }">
          <div class="stage-number">{{ isStageCompleted(stage) ? '✓' : stage.number }}<i></i></div>
          <div class="stage-main"><div class="stage-label"><span>{{ isStageCompleted(stage) ? '这一站已完成' : index === currentIndex ? '现在从这里开始' : index === stages.length - 1 ? '最终作品' : '建站阶段' }}</span><em>{{ stage.time }}</em></div><h3>{{ stage.title }}</h3><p>{{ stage.subtitle }}</p><div class="stage-skills"><span v-for="skill in stage.skills" :key="skill">{{ skill }}</span></div><div class="stage-output"><small>完成后你将得到</small><strong>{{ stage.output }}</strong></div></div>
          <RouterLink v-if="stage.route && isStageUnlocked(index)" class="stage-action" :to="stage.route">{{ stageActionLabel(stage, index) }} <span>→</span></RouterLink><button v-else class="stage-action locked" type="button" disabled>{{ isStageUnlocked(index) ? '正在匹配课程' : '完成上一站后开启' }}</button>
        </article>
      </div>
    </template>
  </section>
</template>
