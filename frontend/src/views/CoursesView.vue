<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { api, type CourseSummary } from '../services/api'
import { defaultJourney, loadJourney, saveJourneyConfiguration, skipJourneyStage, type JourneyConfig, type JourneyStageId } from '../services/journey'
import { buildJourneyPlan } from '../content/journeyPlan'
import ProjectInspiration from '../components/ProjectInspiration.vue'
import StageCompass from '../components/StageCompass.vue'
import NextDiscovery from '../components/NextDiscovery.vue'

type Choice = { id: string; name: string; note: string; badge?: string }
type StageTask = { title: string; detail: string }
type RoadmapStage = {
  id: JourneyStageId
  number: string
  title: string
  subtitle: string
  output: string
  mission: string
  proof: string
  skipHint: string
  time: string
  skills: string[]
  tasks: StageTask[]
  route: string | null
  course: string | null
}
type RouteProfile = {
  id: string
  title: string
  note: string
  result: string
  frontend: JourneyConfig['frontend']
  backend: JourneyConfig['backend']
  database: JourneyConfig['database']
}

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
  { id: 'later', name: '暂时不需要', note: '先用程序里的示例数据，之后再接数据库' },
]

const routeProfiles: RouteProfile[] = [
  { id: 'first', title: '我第一次做网站', note: '先做出能分享的页面，再决定是否增加服务器功能', result: '原生网页 · 暂不使用后端和数据库', frontend: 'vanilla', backend: 'later', database: 'later' },
  { id: 'frontend', title: '我想学现代前端', note: '先补齐网页基础，再用 Vue 把页面拆成组件', result: 'Vue 3 · 先完成静态网站', frontend: 'vue', backend: 'later', database: 'later' },
  { id: 'fullstack', title: '我想做完整系统', note: '页面、接口和数据都会学习，路线更长也更完整', result: 'Vue 3 · Spring Boot · MySQL', frontend: 'vue', backend: 'java', database: 'mysql' },
]

const projectStageCopy: Record<string, Partial<Record<JourneyStageId, { mission: string; proof: string }>>> = {
  portfolio: {
    intro: { mission: '写好个人介绍，并放上第一项想展示的作品', proof: '首页能清楚介绍你，并出现一个真实作品区' },
    style: { mission: '为作品卡片设计颜色、间距和布局', proof: '作品卡片在电脑和手机上都清楚易读' },
    interaction: { mission: '为作品增加分类筛选或详情展开', proof: '访客点击后，能看到正确的作品内容' },
    framework: { mission: '把作品卡片拆成 Vue 组件，用数组生成多张卡片', proof: '新增一条作品数据，页面会自动多出一张卡片' },
    publish: { mission: '构建并发布第一版个人作品集', proof: '获得一个其他设备也能打开的网址' },
    backend: { mission: '编写作品列表接口，让前端读取真实数据', proof: '页面能从接口成功显示作品列表' },
    database: { mission: '设计作品表，并保存标题、介绍和链接', proof: '重启服务后，作品数据仍然存在' },
    launch: { mission: '检查内容、手机布局、链接和访问速度', proof: '陌生访客能顺利看懂并浏览你的作品' },
  },
  blog: {
    intro: { mission: '写好博客介绍，并完成第一篇短文章', proof: '首页能说明博客主题，并出现一篇真实文章' },
    style: { mission: '为文章列表设计舒服的阅读排版', proof: '标题、摘要和正文层级清楚，手机上也能阅读' },
    interaction: { mission: '增加文章展开或主题筛选', proof: '访客可以找到并打开想看的文章' },
    framework: { mission: '把文章卡片拆成 Vue 组件，用数组生成文章列表', proof: '新增一条文章数据，列表会自动更新' },
    publish: { mission: '构建并发布第一版兴趣博客', proof: '获得一个可以分享给朋友的网址' },
    backend: { mission: '编写文章接口，让前端读取文章列表', proof: '页面能从接口成功显示文章内容' },
    database: { mission: '设计文章表，并保存标题、摘要和正文', proof: '重启服务后，文章仍然能够打开' },
    launch: { mission: '检查阅读体验、文章链接和手机布局', proof: '访客可以顺利找到并阅读第一篇文章' },
  },
  campus: {
    intro: { mission: '写好站点说明，并发布第一条校园活动', proof: '首页能说明服务对象，并出现一条真实活动' },
    style: { mission: '为活动列表设计清楚的时间与地点层级', proof: '访客一眼能看清活动名称、时间和地点' },
    interaction: { mission: '增加活动详情展开或日期筛选', proof: '访客可以快速找到自己能参加的活动' },
    framework: { mission: '把活动卡片拆成 Vue 组件，用数组生成活动列表', proof: '新增一条活动数据，页面会自动更新' },
    publish: { mission: '构建并发布第一版校园信息站', proof: '获得一个可以发给同学的网址' },
    backend: { mission: '编写活动接口，让前端读取活动列表', proof: '页面能从接口成功显示校园活动' },
    database: { mission: '设计活动表，并保存时间、地点和介绍', proof: '重启服务后，活动信息仍然存在' },
    launch: { mission: '检查信息准确性、手机布局和所有链接', proof: '同学可以顺利查到一条有用的校园信息' },
  },
}

const stageSkipHints: Record<JourneyStageId, string> = {
  intro: '能独立创建 HTML 文件，并用标题、段落和图片组织首页',
  style: '能使用盒模型和 Flex 布局完成响应式卡片',
  interaction: '能处理点击事件，并根据状态更新页面',
  framework: '能创建 Vue 项目、编写组件并渲染列表',
  publish: '能使用 Git、完成构建并发布静态网站',
  backend: '能编写 REST 接口并让前端成功请求',
  database: '能设计数据表，并完成基本增删改查',
  launch: '完成最终检查后才能结束路线',
}

const courses = ref<CourseSummary[]>([])
const completedStages = ref<JourneyStageId[]>([])
const skippedStages = ref<JourneyStageId[]>([])
const configured = ref(false)
const editing = ref(true)
const loadingJourney = ref(true)
const savingJourney = ref(false)
const skippingStage = ref<JourneyStageId | null>(null)
const journeyError = ref('')
const selectionMode = ref<'guided' | 'manual'>('guided')
const wizardStep = ref(1)
const wizardRoot = ref<HTMLElement | null>(null)
const roadmapRoot = ref<HTMLElement | null>(null)
const wizardHeading = ref<HTMLElement | null>(null)
const loadError = ref('')
const stepLabels = ['选作品', '选起步方式', '确认路线']
async function goToStep(step: number) {
  wizardStep.value = step
  await nextTick()
  wizardRoot.value?.scrollIntoView({ block: 'start' })
  wizardHeading.value?.focus({ preventScroll: true })
}
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
  config.value.backend === 'later' || config.value.database === 'later' ? null : choiceName(databaseChoices, config.value.database),
].filter(Boolean).join(' · '))
const selectedProfile = computed(() => routeProfiles.find(profile => profile.frontend === config.value.frontend && profile.backend === config.value.backend && profile.database === config.value.database)?.id)
const routeChanged = computed(() => savedConfig.value && (Object.keys(config.value) as (keyof JourneyConfig)[]).some(key => config.value[key] !== savedConfig.value?.[key]))

function applyRecommendation(profile: RouteProfile) {
  config.value.frontend = profile.frontend
  config.value.backend = profile.backend
  config.value.database = profile.database
}

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
  config.value.backend === 'later' || config.value.database === 'later' ? null : '数据库原理',
  '软件工程与 Git 协作',
].filter((item): item is string => Boolean(item)))

const stages = computed(() => {
  const siteLabel = projectName.value
  const contentLabel = config.value.project === 'blog' ? '文章' : config.value.project === 'campus' ? '活动' : '作品'
  const frontendFramework = config.value.frontend === 'vue' ? 'Vue 组件与状态' : 'JavaScript 页面交互'
  const backendLabel = config.value.backend === 'python' ? 'Python 接口' : 'Spring Boot 接口'
  const databaseLabel = config.value.database === 'sqlite' ? 'SQLite' : 'MySQL'
  const items: RoadmapStage[] = []
  const addStage = (stage: Omit<RoadmapStage, 'number' | 'mission' | 'proof' | 'skipHint'>) => {
    const copy = projectStageCopy[config.value.project]?.[stage.id]
    items.push({
      ...stage,
      mission: copy?.mission ?? stage.subtitle,
      proof: copy?.proof ?? stage.output,
      skipHint: stageSkipHints[stage.id],
      number: String(items.length + 1).padStart(2, '0'),
    })
  }
  addStage({ id: 'intro', title: '你好，这是我的网站', subtitle: `把名字和兴趣放进${siteLabel}`, output: '浏览器中出现属于你的首页', time: '约 60 分钟', skills: ['HTML 结构', '语义标签', '文字与链接'], tasks: [
    { title: '规划首页内容', detail: `确定站点名称、一句话介绍和第一条真实${contentLabel}。` },
    { title: '搭出 HTML 结构', detail: `用 h1、p、section 和 article 组织${siteLabel}首页。` },
    { title: '在浏览器验证', detail: '保存 index.html，刷新页面，并确认修改后的内容真实出现。' },
  ], route: '/courses/first-page', course: null })
  addStage({ id: 'style', title: '给网站换件衣服', subtitle: '用颜色、字体和留白做出自己的风格', output: '完成一套个人视觉主题', time: '约 90 分钟', skills: ['CSS 层叠', '盒模型', 'Flex 布局', '响应式'], tasks: [
    { title: '拉开阅读层级', detail: `分别设置${contentLabel}标题、摘要和正文的字号、颜色与行高。` },
    { title: '设计卡片布局', detail: '用 padding、gap、圆角和 Flex 排好内容与操作按钮。' },
    { title: '检查手机宽度', detail: '切到 375px 预览，修正文字溢出和横向滚动。' },
  ], route: '/courses/style-workshop', course: null })
  addStage({ id: 'interaction', title: '让按钮真的有反应', subtitle: `用${frontendFramework}完成筛选、收藏和详情展开`, output: '页面可以响应点击与输入', time: '约 2 小时', skills: ['DOM', '事件', '状态', '交互反馈'], tasks: [
    { title: '定义交互规则', detail: `写清访客点击前后，${contentLabel}列表或详情应该发生什么变化。` },
    { title: '连接事件与状态', detail: '选择按钮、监听 click，并根据状态更新文字、样式或内容。' },
    { title: '测试重复操作', detail: '连续点击、切换和收起，确认页面状态始终正确且有反馈。' },
  ], route: '/courses/interaction-workshop', course: null })
  if (config.value.frontend === 'vue') {
    addStage({ id: 'framework', title: '把页面装进 Vue', subtitle: '用组件和响应式数据重新组织不断长大的页面', output: '一个结构清楚的 Vue 单页应用', time: '5 个章节', skills: ['Vue 3', '组件', 'Props', '列表渲染'], tasks: [
      { title: '划分页面组件', detail: `把导航、${contentLabel}卡片和页脚拆成职责单一的组件。` },
      { title: '用数据生成内容', detail: `把多条${contentLabel}放进数组，通过 v-for 渲染并传入 Props。` },
      { title: '整理状态与结构', detail: `新增一条${contentLabel}数据，确认列表自动更新且组件无需复制。` },
    ], route: '/courses/project-stage/framework', course: 'Vue 3 前端开发' })
  }
  addStage({ id: 'publish', title: '把完整版本发给朋友', subtitle: config.value.backend === 'later' ? '保存代码、完成构建，把页面发布成可访问的网站' : `发布页面与${backendLabel}，在公开环境完成前后端联调`, output: config.value.backend === 'later' ? '获得第一个可分享的网站地址' : '获得可访问的网站与 API 地址', time: '约 60–90 分钟', skills: config.value.backend === 'later' ? ['Git', '构建', '静态部署', '线上验证'] : ['Git', '生产构建', '服务部署', '环境变量', '线上联调'], tasks: [
    { title: '保存可靠版本', detail: '检查文件并创建一次 Git 提交，给当前可运行版本留下记录。' },
    { title: config.value.backend === 'later' ? '生成上线文件' : '部署服务与数据', detail: config.value.backend === 'later' ? (config.value.frontend === 'vue' ? '执行生产构建，读懂输出并确认 dist 文件夹生成。' : '检查 index.html 入口和图片、样式、脚本的相对路径。') : `部署${backendLabel}${config.value.database === 'later' ? '' : `与 ${databaseLabel}`}，确认公开健康接口可以访问。` },
    { title: config.value.backend === 'later' ? '发布并异地验证' : '发布页面并联调', detail: config.value.backend === 'later' ? '上传网站，用手机或无痕窗口打开公开网址并检查主要页面。' : '配置生产 API 地址，重新构建前端，在无痕窗口走通一次页面—接口—数据流程。' },
  ], route: '/courses/publish-workshop', course: null })
  if (config.value.backend !== 'later') {
    addStage({ id: 'backend', title: '给网站接上大脑', subtitle: `用${backendLabel}接收页面请求`, output: '前端成功读取自己的接口', time: '5 个章节', skills: ['HTTP', 'REST API', 'JSON', '异常处理'], tasks: [
      { title: '先约定接口', detail: `确定 GET /api/${contentLabel} 要接收什么、返回哪些 JSON 字段。` },
      { title: '实现并测试接口', detail: `用${backendLabel}返回两条真实数据，再检查状态码和响应内容。` },
      { title: '连接前端页面', detail: '用 fetch 请求接口，并补上加载中、成功和失败三种界面状态。' },
    ], route: '/courses/project-stage/backend', course: config.value.backend === 'java' ? 'Java Web 应用开发' : 'FastAPI 后端开发' })
    if (config.value.database !== 'later') {
      addStage({ id: 'database', title: '让内容记得住', subtitle: `把文章和作品保存到 ${databaseLabel}`, output: '刷新页面后数据依然存在', time: '5 个章节', skills: ['表设计', 'SQL', 'CRUD', '数据持久化'], tasks: [
        { title: '设计内容表', detail: `为${contentLabel}确定主键、标题、正文和创建时间等字段。` },
        { title: '完成读写闭环', detail: `插入一条${contentLabel}，再通过查询接口把它显示到页面。` },
        { title: '验证真的保存', detail: '刷新页面并重启后端，确认刚才的数据仍能读取。' },
      ], route: '/courses/project-stage/database', course: '数据库原理' })
    }
  }
  addStage({ id: 'launch', title: '上线前的最后巡检', subtitle: launchDescription(config.value.backend), output: `完成可以展示的${siteLabel}`, time: '约 1.5 小时', skills: ['质量检查', '移动端', '可访问性', '交付说明'], tasks: [
    { title: '跑完项目体检', detail: '逐项检查构建、内容、手机布局、键盘操作和全部链接。' },
    { title: '验证真实环境', detail: config.value.backend !== 'later' ? '从公开页面走通接口与数据流程，记录并修复失败项。' : '用无痕窗口重新访问，确认资源和交互都能正常加载。' },
    { title: '完成作品说明', detail: '用三句话介绍网站主题、技术路线和一个自己解决的问题。' },
  ], route: '/courses/launch-workshop', course: null })
  const centralPlan = buildJourneyPlan(config.value)
  return centralPlan.map((planStep, index) => ({
    ...items.find((stage) => stage.id === planStep.id)!,
    title: planStep.title,
    time: planStep.time,
    route: planStep.route,
    course: planStep.courseTitle ?? null,
    number: String(index + 1).padStart(2, '0'),
  }))
})
const isStageCompleted = (stage: { id: JourneyStageId }) => completedStages.value.includes(stage.id)
const isStageSkipped = (stage: { id: JourneyStageId }) => skippedStages.value.includes(stage.id) && !isStageCompleted(stage)
const isStageResolved = (stage: { id: JourneyStageId; course?: string | null }) => isStageCompleted(stage) || isStageSkipped(stage)
const skippedCount = computed(() => stages.value.filter(isStageSkipped).length)
const resolvedCount = computed(() => stages.value.filter(isStageResolved).length)
const currentIndex = computed(() => {
  const index = stages.value.findIndex((stage) => !isStageResolved(stage))
  return index < 0 ? stages.value.length - 1 : index
})
const progressPercent = computed(() => Math.round(resolvedCount.value / Math.max(stages.value.length, 1) * 100))
const nextStageTitle = computed(() => stages.value[currentIndex.value]?.title ?? '全部完成')

const isStageUnlocked = (index: number) => index <= currentIndex.value || Boolean(stages.value[index] && isStageResolved(stages.value[index]))
const stageActionLabel = (stage: { id: JourneyStageId; course: string | null }, index: number) => {
  if (isStageSkipped(stage)) return stage.course ? `补学 ${stage.course}` : '重新学习这一站'
  if (isStageCompleted(stage)) return stage.course ? `复习 ${stage.course}` : '再次练习'
  if (index === currentIndex.value) return stage.course ? '学习课程并应用到项目' : index === 0 ? '开始第一站' : '进入这一站'
  return stage.course ? '进入项目实做' : '进入这一站'
}
const canSkipStage = (stage: { id: JourneyStageId }, index: number) => index === currentIndex.value
  && stage.id !== 'launch'
  && !isStageResolved(stage)

async function skipStage(stageId: JourneyStageId) {
  if (skippingStage.value) return
  skippingStage.value = stageId
  journeyError.value = ''
  try {
    const journey = await skipJourneyStage(stageId)
    completedStages.value = journey.completedStages
    skippedStages.value = journey.skippedStages
  } catch (cause) {
    journeyError.value = cause instanceof Error ? cause.message : '暂时无法跳过这一站'
  } finally {
    skippingStage.value = null
  }
}

function choose<K extends keyof JourneyConfig>(key: K, value: JourneyConfig[K]) {
  if (config.value[key] === value) return
  const previousBackend = config.value.backend
  config.value[key] = value
  if (key === 'backend' && value === 'later') config.value.database = 'later'
  if (key === 'backend' && value !== 'later' && previousBackend === 'later') config.value.database = 'mysql'
}

async function createJourney() {
  if (savingJourney.value) return
  savingJourney.value = true
  journeyError.value = ''
  try {
    const saved = await saveJourneyConfiguration(config.value)
    savedConfig.value = { project: saved.project, frontend: saved.frontend, backend: saved.backend, database: saved.database }
    completedStages.value = saved.completedStages
    skippedStages.value = saved.skippedStages
    configured.value = true
    editing.value = false
    await nextTick()
    const currentStage = roadmapRoot.value?.querySelector<HTMLElement>('.current')
    currentStage?.scrollIntoView({ block: 'start' })
    currentStage?.focus({ preventScroll: true })
  } catch (cause) {
    journeyError.value = cause instanceof Error ? cause.message : '建站路线保存失败'
  } finally {
    savingJourney.value = false
  }
}

function editJourney() { journeyError.value = ''; editing.value = true; void goToStep(1) }
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
    skippedStages.value = journey.skippedStages
    configured.value = journey.configured
    editing.value = !journey.configured
    selectionMode.value = routeProfiles.some(profile => profile.frontend === journey.frontend && profile.backend === journey.backend && profile.database === journey.database) || !journey.configured ? 'guided' : 'manual'
    if (!journey.configured) applyRecommendation(routeProfiles[0])
  } catch (cause) {
    loadError.value = cause instanceof Error ? cause.message : '建站路线加载失败'
  } finally {
    loadingJourney.value = false
  }
})

</script>

<template>
  <section class="journey-page">
    <header class="journey-heading">
      <div><span class="eyebrow"><i></i> BUILD YOUR FIRST WEBSITE</span><h2>建站之旅</h2><p>从一个你想做的小作品开始，一步步找到学习的方向。</p></div>
      <button v-if="configured && !editing" class="ghost-button" type="button" @click="editJourney">调整作品与路线</button>
    </header>

    <div v-if="loadingJourney" class="state-card glass-card"><span class="loader"></span><p>正在读取你的建站路线…</p></div>
    <div v-else-if="loadError" class="state-card glass-card"><strong>建站路线暂时无法读取</strong><p>{{ loadError }}</p><button type="button" @click="$router.go(0)">重新加载</button></div>
    <section v-else-if="editing" ref="wizardRoot" class="journey-builder route-wizard glass-card" :aria-busy="savingJourney">
      <nav class="wizard-steps" aria-label="路线选择步骤">
        <button v-for="(label, index) in stepLabels" :key="label" type="button" :disabled="savingJourney" :aria-current="wizardStep === index + 1 ? 'step' : undefined" :class="{ active: wizardStep === index + 1, passed: wizardStep > index + 1 }" @click="goToStep(index + 1)"><b>{{ index + 1 }}</b><span>{{ label }}</span></button>
      </nav>
      <div class="wizard-content">
        <header class="wizard-heading"><button v-if="configured" class="wizard-cancel" type="button" :disabled="savingJourney" @click="cancelEditing">取消调整</button><span>第 {{ wizardStep }} 步 / 共 3 步</span><h3 ref="wizardHeading" tabindex="-1">{{ wizardStep === 1 ? '你想做一个什么网站？' : wizardStep === 2 ? '你准备从哪里开始？' : '这就是你的建站路线' }}</h3><p>{{ wizardStep === 1 ? '点击选择一个作品方向。不确定的话，先选个人作品集。' : wizardStep === 2 ? '第一次做网站，选第一项就可以。这里决定路线长短，不要求你已经学会这些技术。' : '核对作品和步骤，确认后就可以动手。你可以返回前面修改选择。' }}</p></header>
        <fieldset class="wizard-fields" :disabled="savingJourney">
          <template v-if="wizardStep === 1">
            <ProjectInspiration :model-value="config.project" @update:model-value="choose('project', $event)" />
          </template>
          <template v-else-if="wizardStep === 2">
            <div class="start-mode" role="group" aria-label="起步方式选择模式"><button type="button" :aria-pressed="selectionMode === 'guided'" @click="selectionMode = 'guided'">按目标选择</button><button type="button" :aria-pressed="selectionMode === 'manual'" @click="selectionMode = 'manual'">自行搭配技术</button></div>
            <div v-if="selectionMode === 'guided'" class="start-profiles" role="group" aria-label="选择起步方式">
              <button v-for="(profile, index) in routeProfiles" :key="profile.id" type="button" class="start-profile" :aria-pressed="selectedProfile === profile.id" @click="applyRecommendation(profile)"><span class="selection-dot">{{ selectedProfile === profile.id ? '✓' : '' }}</span><span class="profile-copy"><strong>{{ profile.title }} <small v-if="index === 0">新手推荐</small></strong><span>{{ profile.note }}</span><em>{{ profile.result }}</em></span><b>{{ buildJourneyPlan({ ...config, ...profile }).length }} 站</b></button>
              <p v-if="!selectedProfile" class="custom-route-note">当前保留你的自定义技术组合。点击上方方案才会替换，也可以直接进入下一步。</p>
            </div>
            <div v-else class="custom-stack">
              <p class="custom-route-note">按顺序选择页面、后端和数据存储。不了解这些名称，可以切回“按目标选择”。</p>
              <section><h4>① 页面怎么做</h4><div class="stack-options"><button v-for="item in frontendChoices" :key="item.id" type="button" :aria-pressed="config.frontend === item.id" @click="choose('frontend', item.id)"><strong>{{ item.name }}</strong><small>{{ item.note }}</small><i>{{ config.frontend === item.id ? '✓ 已选' : '选择' }}</i></button></div></section>
              <section><h4>② 是否需要后端</h4><p>第一版只展示内容，可以先不加后端。</p><div class="stack-options three"><button v-for="item in backendChoices" :key="item.id" type="button" :aria-pressed="config.backend === item.id" @click="choose('backend', item.id)"><strong>{{ item.name }}</strong><small>{{ item.note }}</small><i>{{ config.backend === item.id ? '✓ 已选' : '选择' }}</i></button></div></section>
              <section><h4>③ 数据放在哪里</h4><p v-if="config.backend === 'later'" class="database-note">当前是静态网站：内容先写在项目文件里，无需配置数据库。选择后端后，这里会出现数据库选项。</p><div v-else class="stack-options three"><button v-for="item in databaseChoices" :key="item.id" type="button" :aria-pressed="config.database === item.id" @click="choose('database', item.id)"><strong>{{ item.name }}</strong><small>{{ item.note }}</small><i>{{ config.database === item.id ? '✓ 已选' : '选择' }}</i></button></div></section>
            </div>
            <details class="tool-explainer"><summary>前端、后端、数据库分别做什么？</summary><p><b>前端</b>：访客看到的页面和点击时的变化。HTML 放内容、CSS 管外观、JavaScript 做交互；Vue 帮你组织较复杂的页面。</p><p><b>后端</b>：接收网页请求，处理规则并返回数据。Java 和 Python 是这里可以选择的两种语言。</p><p><b>数据库</b>：长期保存文章、作品等数据。简单展示网站可以暂时不用后端和数据库。</p></details>
          </template>
          <template v-else>
            <div class="route-review"><div><small>我要做的作品</small><strong>{{ projectName }}</strong><button type="button" @click="goToStep(1)">修改作品</button></div><div><small>我的起步方式</small><strong>{{ routeProfiles.find(profile => profile.id === selectedProfile)?.title ?? '自定义技术路线' }}</strong><span>{{ stackSummary }}</span><button type="button" @click="goToStep(2)">修改方式</button></div></div>
            <h4 class="preview-plan-title">从第一张网页到分享作品，共 {{ stages.length }} 站</h4>
            <ol class="preview-plan"><li v-for="stage in stages" :key="stage.id"><b>{{ stage.number }}</b><div><strong>{{ stage.title }}</strong><span>{{ stage.mission }}</span></div></li></ol>
            <p class="first-step-note">{{ configured ? '保存后，路线会显示你接下来可以继续的一站。' : '确认后从第一站开始：写下你的介绍，在浏览器里打开自己的网页。' }}</p>
            <p v-if="configured && routeChanged" class="route-impact" role="note">保存会更新已有路线：更换作品方向会重置阶段进度；更换技术会重置受影响阶段和后续发布记录。现在返回修改或取消，不会改变已保存的路线。</p>
          </template>
        </fieldset>
      </div>
      <footer class="wizard-footer"><div class="wizard-selection"><small>{{ wizardStep === 1 ? '已选作品' : '当前选择' }}</small><strong>{{ projectName }}</strong><span v-if="wizardStep > 1">{{ routeProfiles.find(profile => profile.id === selectedProfile)?.title ?? '自定义技术路线' }}</span></div><div class="wizard-actions"><button v-if="wizardStep > 1" class="wizard-back" type="button" :disabled="savingJourney" @click="goToStep(wizardStep - 1)">上一步</button><button v-if="wizardStep < 3" class="wizard-primary" type="button" @click="goToStep(wizardStep + 1)">{{ wizardStep === 1 ? '下一步：选起步方式 →' : '下一步：确认路线 →' }}</button><button v-else class="wizard-primary" type="button" :disabled="savingJourney" @click="createJourney">{{ savingJourney ? '正在保存…' : configured ? '保存这条路线' : '确认路线，开始建站 →' }}</button></div><p v-if="journeyError" class="wizard-error" role="alert">{{ journeyError }}，你的选择已保留，请重试。</p></footer>
    </section>

    <template v-else>
      <section class="journey-summary glass-card"><div class="journey-project-mark">{{ projectName.slice(0, 1) }}</div><div><span>我的第一个网站</span><h3>{{ projectName }}</h3><p>{{ stackSummary }}</p></div><div class="journey-progress"><span><b>{{ resolvedCount }}</b> / {{ stages.length }} 站</span><div><i :style="{ width: `${progressPercent}%` }"></i></div><small>{{ resolvedCount === stages.length ? '路线已全部完成' : `下一站：${nextStageTitle}` }} · 已完成 {{ resolvedCount - skippedCount }}<template v-if="skippedCount"> · 跳过 {{ skippedCount }}</template></small></div></section>

      <details class="route-resources"><summary>需要时再查：这条路线的配套课程</summary><section class="route-course-strip"><div><span>这条路线会用到</span><strong>{{ routeCourseTitles.length }} 门配套技术课</strong></div><div><template v-for="title in routeCourseTitles" :key="title"><RouterLink v-if="courseLink(title)" :to="courseLink(title) || '/knowledge'">{{ title }} <span>↗</span></RouterLink><span v-else>{{ title }}</span></template></div></section></details>

      <div class="roadmap-header"><div><span>你的专属路线</span><h3>每到一站，网站就多一个新本领</h3></div><p>做到哪一步，就学习哪一步需要的知识；已经掌握的阶段可以直接跳过，之后仍能回来补学。</p></div>

      <div ref="roadmapRoot" class="journey-roadmap">
        <article v-for="(stage, index) in stages" :key="stage.number" class="roadmap-stage glass-card" tabindex="-1" :class="{ current: index === currentIndex, completed: isStageCompleted(stage), skipped: isStageSkipped(stage), locked: !isStageUnlocked(index) }">
          <div class="stage-number">{{ isStageCompleted(stage) ? '✓' : isStageSkipped(stage) ? '跳' : stage.number }}<i></i></div>
          <div class="stage-main"><div class="stage-label"><span>{{ isStageCompleted(stage) ? '这一站已完成' : isStageSkipped(stage) ? '已跳过，随时可以回来补学' : index === currentIndex ? '现在从这里开始' : index === stages.length - 1 ? '最终作品' : '建站阶段' }}</span><em>{{ stage.time }}</em></div><h3>{{ stage.title }}</h3><p>{{ stage.mission }}</p><StageCompass v-if="index === currentIndex && !isStageResolved(stage)" :config="config" :stage="stage.id" compact /><div class="stage-skills"><span v-for="skill in stage.skills" :key="skill">{{ skill }}</span></div><details class="stage-learning-plan" :open="index === currentIndex"><summary><span>本阶段的 3 个动手任务</span><small>{{ isStageResolved(stage) ? '可随时回来复习' : '按顺序完成，最后用作品验收' }}</small></summary><ol><li v-for="(task, taskIndex) in stage.tasks" :key="task.title"><i>{{ taskIndex + 1 }}</i><span><b>{{ task.title }}</b><small>{{ task.detail }}</small></span></li></ol><div class="stage-plan-proof"><span>交付结果</span><strong>{{ stage.proof }}</strong></div></details></div>
          <div class="stage-actions"><RouterLink v-if="stage.route && isStageUnlocked(index)" class="stage-action" :to="stage.route">{{ stageActionLabel(stage, index) }} <span>→</span></RouterLink><button v-else class="stage-action locked" type="button" disabled>{{ isStageUnlocked(index) ? '正在匹配课程' : '完成上一站后开启' }}</button><template v-if="canSkipStage(stage, index)"><p class="stage-skip-hint">满足下面能力就能跳过：<br />{{ stage.skipHint }}</p><button class="stage-skip" type="button" :disabled="Boolean(skippingStage)" @click="skipStage(stage.id)">{{ skippingStage === stage.id ? '正在跳过…' : '我已经会了，跳过' }}</button></template></div>
        </article>
      </div>
      <NextDiscovery v-if="resolvedCount === stages.length" />
    </template>
  </section>
</template>

<style scoped>
.journey-page { --muted: #455b70; color: #18324a; }
.journey-heading p { color: #455b70; }.journey-page .ghost-button:hover { color: #18324a; background: #e5eef6; }
.route-wizard { background: #fff; border: 1px solid #cedbe6; scroll-margin-top: 20px; overflow: visible; }
.wizard-steps { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); padding: 22px 28px; gap: 14px; border-bottom: 1px solid #dce5ed; background: #f6f9fc; border-radius: 24px 24px 0 0; }
.wizard-steps button { display: flex; align-items: center; gap: 10px; padding: 10px 14px; color: #455b70; border: 1px solid transparent; background: transparent; border-radius: 10px; font-size: 15px; text-align: left; }.wizard-steps b { display: grid; place-items: center; width: 29px; height: 29px; border-radius: 50%; background: #e3eaf0; font-size: 13px; }.wizard-steps button.active { color: #17466b; border-color: #90b2cc; background: #e4eff8; font-weight: 700; }.wizard-steps .active b { background: #285e87; color: white; }.wizard-steps .passed b { color: #245c48; background: #dfede6; }
.wizard-content { padding: 28px; }.wizard-heading > span { color: #46647b; font-size: 13px; font-weight: 600; }.wizard-heading h3 { font-size: 26px; margin: 10px 0; outline: none; }.wizard-heading p { font-size: 15px; color: #455b70; line-height: 1.8; margin: 0 0 22px; }.wizard-fields { border: 0; margin: 0; padding: 0; min-width: 0; }
.start-mode { display: flex; gap: 4px; width: fit-content; padding: 4px; border: 1px solid #cedbe6; border-radius: 10px; margin-bottom: 20px; }.start-mode button { color: #455b70; padding: 10px 16px; border: 0; border-radius: 7px; background: transparent; font-size: 14px; }.start-mode button[aria-pressed=true] { color: white; background: #285e87; }
.start-profiles { display: grid; gap: 12px; }.start-profile { display: flex; align-items: center; gap: 18px; text-align: left; background: #fff; border: 1px solid #becdd9; border-radius: 13px; padding: 21px; color: #18324a; }.start-profile[aria-pressed=true], .stack-options button[aria-pressed=true] { background: #eaf3fa; border-color: #326b96; box-shadow: inset 0 0 0 1px #326b96; }.start-profile:hover, .stack-options button:hover { border-color: #326b96; }.selection-dot { display: grid; place-items: center; flex: 0 0 24px; height: 24px; border: 1px solid #69849a; border-radius: 50%; color: #fff; font-size: 13px; }.start-profile[aria-pressed=true] .selection-dot { background: #285e87; border-color: #285e87; }.profile-copy { flex: 1; display: grid; gap: 8px; }.profile-copy strong { color: #18324a; font-size: 17px; line-height: 1.7; }.profile-copy strong small { display: inline-block; color: #245940; background: #dceddf; font-size: 11px; padding: 1px 7px; border-radius: 5px; margin-left: 6px; }.profile-copy > span { color: #455b70; font-size: 14px; line-height: 1.7; }.profile-copy em { color: #3d5c73; font-size: 12px; font-style: normal; }.start-profile > b { white-space: nowrap; color: #285e87; font-size: 14px; }
.custom-stack section + section { margin-top: 24px; }.custom-stack h4 { font-size: 16px; margin: 0 0 12px; }.custom-stack p, .custom-route-note { font-size: 14px; color: #455b70; line-height: 1.8; }.stack-options { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 12px; }.stack-options.three { grid-template-columns: repeat(3,minmax(0,1fr)); }.stack-options button { display: flex; flex-direction: column; align-items: start; gap: 10px; text-align: left; border: 1px solid #becdd9; border-radius: 12px; background: #fff; padding: 18px; color: #18324a; }.stack-options strong { font-size: 15px; line-height: 1.6; }.stack-options small { color: #455b70; font-size: 13px; line-height: 1.75; }.stack-options i { font-size: 12px; color: #285e87; font-style: normal; margin-top: auto; }.database-note { border: 1px dashed #a4b8c8; border-radius: 10px; background: #f4f7fa; padding: 16px; }
.tool-explainer { margin-top: 22px; border-top: 1px solid #dce5ed; padding-top: 18px; }.tool-explainer summary { color: #285e87; font-size: 14px; cursor: pointer; }.tool-explainer p { color: #455b70; font-size: 14px; line-height: 1.85; }
.route-review { display: grid; grid-template-columns: 1fr 1.5fr; gap: 20px; padding: 20px; border-radius: 13px; background: #f1f6fa; }.route-review > div { display: grid; gap: 8px; justify-items: start; }.route-review small, .route-review span { color: #455b70; font-size: 13px; line-height: 1.7; }.route-review strong { color: #18324a; font-size: 18px; }.route-review button { padding: 4px 0; background: transparent; border: 0; color: #285e87; font-size: 13px; text-decoration: underline; text-underline-offset: 3px; }.preview-plan-title { margin: 24px 0 14px; font-size: 16px; }.preview-plan { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 12px; margin: 0; padding: 0; list-style: none; }.preview-plan li { display: flex; align-items: start; gap: 12px; padding: 15px; border: 1px solid #dce5ed; border-radius: 10px; }.preview-plan li > b { color: #285e87; font-size: 14px; }.preview-plan li div { display: grid; gap: 6px; }.preview-plan strong { font-size: 14px; }.preview-plan span { font-size: 13px; line-height: 1.7; color: #455b70; }.first-step-note { color: #245940; background: #ecf5ee; padding: 16px; border-radius: 10px; font-size: 14px; line-height: 1.8; }.route-impact { color: #704910; background: #fff7e8; padding: 16px; border: 1px solid #e4c993; border-radius: 10px; font-size: 14px; line-height: 1.8; }
.wizard-footer { position: sticky; bottom: 16px; z-index: 5; display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 16px; padding: 20px 28px; border-top: 1px solid #cedbe6; background: #fff; border-radius: 0 0 24px 24px; box-shadow: 0 -6px 20px rgba(24,50,74,.06); }.wizard-selection { display: grid; gap: 3px; }.wizard-selection small, .wizard-selection > span { color: #455b70; font-size: 12px; }.wizard-selection strong { font-size: 17px; color: #18324a; }.wizard-actions { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; }.wizard-actions button { padding: 12px 18px; border-radius: 9px; font-size: 14px; font-weight: 600; }.wizard-primary { color: #fff; background: #285e87; border: 1px solid #285e87; }.wizard-primary:hover:not(:disabled) { background: #1d496b; }.wizard-back { color: #285e87; background: #fff; border: 1px solid #a4b8c8; }.wizard-cancel { float: right; padding: 2px 0 8px 12px; color: #455b70; background: transparent; border: 1px solid transparent; font-size: 13px; text-decoration: underline; text-underline-offset: 3px; }.wizard-actions button:disabled { cursor: wait; opacity: .7; }.wizard-error { flex-basis: 100%; margin: 0; color: #9e3030; font-size: 14px; line-height: 1.7; }
.route-wizard button:focus-visible, .route-wizard summary:focus-visible { outline: 3px solid #285e87; outline-offset: 3px; }
.route-resources { margin: 20px 0; }.route-resources > summary { cursor: pointer; font-weight: 600; font-size: 14px; color: #3d5c73; }
.journey-page .roadmap-stage { scroll-margin-top: 20px; }.journey-page .roadmap-stage:focus-visible { outline: 2px solid #285e87; outline-offset: 3px; }
.journey-page .roadmap-stage.locked { opacity: 1; background: #f3f6f9; }.journey-page .stage-main > p, .journey-page .stage-skills span, .journey-page .stage-learning-plan li small, .journey-page .stage-skip-hint, .journey-page .roadmap-header p { color: #455b70; }.journey-page .stage-learning-plan li small { font-size: 13px; line-height: 1.8; }.journey-page .stage-learning-plan li b, .journey-page .stage-plan-proof strong { font-size: 14px; color: #203d55; }.journey-page .stage-learning-plan summary small, .journey-page .stage-skip-hint { font-size: 12px; }.journey-page .stage-action:not(.locked) { background: #285e87; color: #fff; }.journey-page .stage-action.locked { color: #455b70; background: #e6edf3; }
@media(max-width: 900px) { .stack-options.three { grid-template-columns: 1fr; }.preview-plan { grid-template-columns: 1fr; } }
@media(max-width: 600px) { .wizard-steps { padding: 14px 10px; gap: 4px; }.wizard-steps button { padding: 8px 3px; gap: 6px; font-size: 12px; }.wizard-steps b { width: 24px; height: 24px; }.wizard-content { padding: 20px 16px; }.wizard-heading h3 { font-size: 23px; }.start-profile { padding: 16px 12px; gap: 10px; }.profile-copy strong { font-size: 15px; }.start-profile > b { font-size: 12px; }.stack-options, .route-review { grid-template-columns: 1fr; }.wizard-footer { bottom: 76px; padding: 15px; gap: 10px; }.wizard-selection { grid-template-columns: auto auto; align-items: center; gap: 6px; }.wizard-selection > span { grid-column: 1 / -1; }.wizard-actions { width: 100%; }.wizard-actions button { padding: 10px 12px; }.wizard-primary { margin-left: auto; }.wizard-cancel { padding-left: 0 !important; } }
</style>
