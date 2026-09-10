<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { api, type CourseSummary } from '../services/api'
import { defaultJourney, loadJourney, saveJourneyConfiguration, skipJourneyStage, type JourneyConfig, type JourneyStageId } from '../services/journey'
import { buildJourneyPlan } from '../content/journeyPlan'

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
  { id: 'later', name: '暂时不需要', note: '静态网站阶段可以跳过数据库' },
]

const routeProfiles: RouteProfile[] = [
  { id: 'first', title: '我第一次做网站', note: '先做出能分享的页面，再决定是否增加服务器功能', result: '原生网页 · 暂不使用后端和数据库', frontend: 'vanilla', backend: 'later', database: 'later' },
  { id: 'frontend', title: '我想学现代前端', note: '先补齐网页基础，再用 Vue 把页面拆成组件', result: 'Vue 3 · 先完成静态网站', frontend: 'vue', backend: 'later', database: 'later' },
  { id: 'fullstack', title: '我想做完整系统', note: '页面、接口和数据都会学习，路线更长也更完整', result: 'Vue 3 · Spring Boot · MySQL', frontend: 'vue', backend: 'java', database: 'mysql' },
]

const technologyFlow = [
  { name: '浏览器', role: '把网站展示给访客', example: '你打开网址、点击按钮，都发生在这里。' },
  { name: '前端', role: '决定页面和交互', example: 'HTML 放内容，CSS 管外观，JavaScript 和 Vue 处理变化。' },
  { name: '后端', role: '处理规则和请求', example: '登录是否成功、可以看哪些内容，都由后端判断。' },
  { name: '数据库', role: '长期保存数据', example: '文章、作品和账号保存在这里，刷新页面也不会消失。' },
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
const selectedProfile = ref('first')
const primerStep = ref(0)
let primerTimer: number | undefined
const config = ref<JourneyConfig>({
  project: defaultJourney.project,
  frontend: defaultJourney.frontend,
  backend: defaultJourney.backend,
  database: defaultJourney.database,
})
const savedConfig = ref<JourneyConfig | null>(null)

const choiceName = (choices: Choice[], id: string) => choices.find((item) => item.id === id)?.name ?? id
const projectName = computed(() => choiceName(projectChoices, config.value.project))
const activeTechnology = computed(() => technologyFlow[primerStep.value])
const stackSummary = computed(() => [
  choiceName(frontendChoices, config.value.frontend),
  choiceName(backendChoices, config.value.backend),
  config.value.backend === 'later' || config.value.database === 'later' ? null : choiceName(databaseChoices, config.value.database),
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
    name: config.value.backend === 'later' || config.value.database === 'later' ? '暂不使用数据库' : config.value.database === 'sqlite' ? 'SQLite 数据库' : 'MySQL 数据库',
    role: config.value.backend === 'later' || config.value.database === 'later' ? '当前阶段无需保存服务器数据' : '负责长期保存用户、文章和作品',
    detail: config.value.backend === 'later' || config.value.database === 'later'
      ? '静态网站的文字直接写在项目里，适合先把第一份作品快速做出来。'
      : config.value.database === 'sqlite'
        ? 'SQLite 把数据库放在一个文件里，不用单独启动服务，适合小项目。'
        : 'MySQL 是常用的关系数据库，适合系统学习表、SQL、事务和索引。',
  },
])

const selectedProfileInfo = computed(() => routeProfiles.find((item) => item.id === selectedProfile.value) ?? routeProfiles[0])

function showTechnology(index: number) {
  primerStep.value = index
  restartPrimerTimer()
}

function restartPrimerTimer() {
  if (primerTimer) window.clearInterval(primerTimer)
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  primerTimer = window.setInterval(() => {
    primerStep.value = (primerStep.value + 1) % technologyFlow.length
  }, 3200)
}

function useGuidedSelection() {
  selectionMode.value = 'guided'
  applyRecommendation(selectedProfileInfo.value)
}

function applyRecommendation(profile: RouteProfile) {
  selectedProfile.value = profile.id
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
    skippedStages.value = saved.skippedStages
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
  restartPrimerTimer()
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
    selectionMode.value = journey.configured ? 'manual' : 'guided'
    if (!journey.configured) applyRecommendation(routeProfiles[0])
  } catch (cause) {
    journeyError.value = cause instanceof Error ? cause.message : '建站路线加载失败'
  } finally {
    loadingJourney.value = false
  }
})

onBeforeUnmount(() => {
  if (primerTimer) window.clearInterval(primerTimer)
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
      <div class="builder-intro"><span>路线定制</span><h3>{{ configured ? '重新安排你的建站路线' : '先看懂网站，再决定怎么学' }}</h3><p>不用先认识 Vue、Spring Boot 或 MySQL。看完下面这段演示，再按自己想做的成果选择。</p></div>

      <section class="technology-primer">
        <div class="primer-heading"><span>60 秒看懂</span><h3>你点开一个网站时，谁在工作？</h3><p>动画会沿着一次真实请求前进。也可以点击任意角色，单独看它负责什么。</p></div>
        <div class="technology-story">
          <div class="website-flow" aria-label="网站技术工作流程">
            <template v-for="(item, index) in technologyFlow" :key="item.name">
              <button type="button" :class="{ active: primerStep === index, visited: primerStep > index }" @click="showTechnology(index)">
                <i>{{ index + 1 }}</i><strong>{{ item.name }}</strong><span>{{ item.role }}</span>
              </button>
              <b v-if="index < technologyFlow.length - 1" :class="{ active: primerStep > index }">→</b>
            </template>
          </div>
          <article class="technology-scene" aria-live="polite">
            <span>现在看到第 {{ primerStep + 1 }} 步</span>
            <h4>{{ activeTechnology.name }}：{{ activeTechnology.role }}</h4>
            <p>{{ activeTechnology.example }}</p>
            <div><i :style="{ width: `${(primerStep + 1) / technologyFlow.length * 100}%` }"></i></div>
          </article>
        </div>
        <div class="plain-tech-notes"><p><b>HTML</b> 放内容，<b>CSS</b> 管外观，<b>JavaScript</b> 让页面有反应；<b>Vue</b> 帮你把这些代码整理成组件。</p><p>只有登录、评论或长期保存内容时，网站才需要后端和数据库。第一次建站可以先把页面做出来。</p></div>
      </section>

      <div class="choice-section"><div class="choice-title"><b>1</b><div><h4>先选一个作品方向</h4><p>课程中的例子会跟着你的主题变化。</p></div></div><div class="choice-grid project-choice-grid"><button v-for="item in projectChoices" :key="item.id" type="button" :class="{ selected: config.project === item.id }" @click="choose('project', item.id)"><span v-if="item.badge">{{ item.badge }}</span><strong>{{ item.name }}</strong><small>{{ item.note }}</small><i>{{ config.project === item.id ? '✓' : '○' }}</i></button></div></div>

      <section class="choice-section route-choice-section">
        <div class="choice-title"><b>2</b><div><h4>你希望怎样选择技术？</h4><p>不懂技术名称时按目标选择；已经了解技术时可以自己搭配。</p></div></div>
        <div class="route-choice-tabs" role="tablist" aria-label="技术路线选择方式">
          <button type="button" role="tab" :aria-selected="selectionMode === 'guided'" :class="{ active: selectionMode === 'guided' }" @click="useGuidedSelection">帮我推荐</button>
          <button type="button" role="tab" :aria-selected="selectionMode === 'manual'" :class="{ active: selectionMode === 'manual' }" @click="selectionMode = 'manual'">我想自己选</button>
        </div>

        <div v-if="selectionMode === 'guided'" class="route-profile-grid">
          <button v-for="profile in routeProfiles" :key="profile.id" type="button" :class="{ selected: selectedProfile === profile.id }" @click="applyRecommendation(profile)">
            <i>{{ selectedProfile === profile.id ? '✓' : '○' }}</i><strong>{{ profile.title }}</strong><span>{{ profile.note }}</span><small>{{ profile.result }}</small>
          </button>
          <div class="recommendation-result"><span>为什么这样推荐</span><strong>{{ selectedProfileInfo.title }}</strong><p>{{ selectedProfileInfo.note }}。路线会先补齐 HTML、CSS 和 JavaScript，需要时再加入其他工具。</p></div>
        </div>

        <div v-else class="manual-stack-picker">
          <div><label>页面用什么做？</label><div class="choice-grid"><button v-for="item in frontendChoices" :key="item.id" type="button" :class="{ selected: config.frontend === item.id }" @click="choose('frontend', item.id)"><span v-if="item.badge">{{ item.badge }}</span><strong>{{ item.name }}</strong><small>{{ item.note }}</small><i>{{ config.frontend === item.id ? '✓' : '○' }}</i></button></div></div>
          <div class="stack-choice-columns"><div><label>后端语言与框架</label><div class="choice-grid compact"><button v-for="item in backendChoices" :key="item.id" type="button" :class="{ selected: config.backend === item.id }" @click="choose('backend', item.id)"><span v-if="item.badge">{{ item.badge }}</span><strong>{{ item.name }}</strong><small>{{ item.note }}</small><i>{{ config.backend === item.id ? '✓' : '○' }}</i></button></div></div><div :class="{ muted: config.backend === 'later' }"><label>数据存在哪里</label><div class="choice-grid compact"><button v-for="item in databaseChoices" :key="item.id" type="button" :disabled="config.backend === 'later'" :class="{ selected: config.database === item.id }" @click="choose('database', item.id)"><span v-if="item.badge">{{ item.badge }}</span><strong>{{ item.name }}</strong><small>{{ item.note }}</small><i>{{ config.database === item.id ? '✓' : '○' }}</i></button></div></div></div>
        </div>
      </section>

      <section class="selected-tech-guide"><div class="choice-title"><b>3</b><div><h4>确认每件工具的工作</h4><p>生成路线前，只需要确认它们是否符合你想做的成果。</p></div></div><div><article v-for="item in selectedTechnologyGuide" :key="item.name"><span>{{ item.role }}</span><h4>{{ item.name }}</h4><p>{{ item.detail }}</p></article></div></section>

      <p v-if="configured" class="route-change-impact">路线调整说明：更换项目方向后，全部阶段需要重新验收；更换前端、后端或数据库时，只重新验收受影响的技术阶段及后续发布。</p><p v-if="journeyError" class="practice-error">{{ journeyError }}</p><footer class="builder-footer"><div><small>你的路线</small><strong>{{ projectName }}</strong><span>{{ stackSummary }}</span></div><div><button v-if="configured" class="ghost-button" type="button" @click="cancelEditing">取消</button><button class="primary-journey-button" type="button" :disabled="savingJourney" @click="createJourney">{{ savingJourney ? '正在保存到数据库…' : '生成我的建站之旅 →' }}</button></div></footer>
    </section>

    <template v-else>
      <section class="journey-summary glass-card"><div class="journey-project-mark">{{ projectName.slice(0, 1) }}</div><div><span>我的第一个网站</span><h3>{{ projectName }}</h3><p>{{ stackSummary }}</p></div><div class="journey-progress"><span><b>{{ resolvedCount }}</b> / {{ stages.length }} 站</span><div><i :style="{ width: `${progressPercent}%` }"></i></div><small>{{ resolvedCount === stages.length ? '路线已全部完成' : `下一站：${nextStageTitle}` }} · 已完成 {{ resolvedCount - skippedCount }}<template v-if="skippedCount"> · 跳过 {{ skippedCount }}</template></small></div></section>

      <section class="route-course-strip"><div><span>这条路线会用到</span><strong>{{ routeCourseTitles.length }} 门配套技术课</strong></div><div><template v-for="title in routeCourseTitles" :key="title"><RouterLink v-if="courseLink(title)" :to="courseLink(title) || '/knowledge'">{{ title }} <span>↗</span></RouterLink><span v-else>{{ title }}</span></template></div></section>

      <div class="roadmap-header"><div><span>你的专属路线</span><h3>每到一站，网站就多一个新本领</h3></div><p>做到哪一步，就学习哪一步需要的知识；已经掌握的阶段可以直接跳过，之后仍能回来补学。</p></div>

      <div class="journey-roadmap">
        <article v-for="(stage, index) in stages" :key="stage.number" class="roadmap-stage glass-card" :class="{ current: index === currentIndex, completed: isStageCompleted(stage), skipped: isStageSkipped(stage), locked: !isStageUnlocked(index) }">
          <div class="stage-number">{{ isStageCompleted(stage) ? '✓' : isStageSkipped(stage) ? '跳' : stage.number }}<i></i></div>
          <div class="stage-main"><div class="stage-label"><span>{{ isStageCompleted(stage) ? '这一站已完成' : isStageSkipped(stage) ? '已跳过，随时可以回来补学' : index === currentIndex ? '现在从这里开始' : index === stages.length - 1 ? '最终作品' : '建站阶段' }}</span><em>{{ stage.time }}</em></div><h3>{{ stage.title }}</h3><p>{{ stage.mission }}</p><div class="stage-skills"><span v-for="skill in stage.skills" :key="skill">{{ skill }}</span></div><details class="stage-learning-plan" :open="index === currentIndex"><summary><span>本阶段的 3 个动手任务</span><small>{{ isStageResolved(stage) ? '可随时回来复习' : '按顺序完成，最后用作品验收' }}</small></summary><ol><li v-for="(task, taskIndex) in stage.tasks" :key="task.title"><i>{{ taskIndex + 1 }}</i><span><b>{{ task.title }}</b><small>{{ task.detail }}</small></span></li></ol><div class="stage-plan-proof"><span>交付结果</span><strong>{{ stage.proof }}</strong></div></details></div>
          <div class="stage-actions"><RouterLink v-if="stage.route && isStageUnlocked(index)" class="stage-action" :to="stage.route">{{ stageActionLabel(stage, index) }} <span>→</span></RouterLink><button v-else class="stage-action locked" type="button" disabled>{{ isStageUnlocked(index) ? '正在匹配课程' : '完成上一站后开启' }}</button><template v-if="canSkipStage(stage, index)"><p class="stage-skip-hint">满足下面能力就能跳过：<br />{{ stage.skipHint }}</p><button class="stage-skip" type="button" :disabled="Boolean(skippingStage)" @click="skipStage(stage.id)">{{ skippingStage === stage.id ? '正在跳过…' : '我已经会了，跳过' }}</button></template></div>
        </article>
      </div>
    </template>
  </section>
</template>
