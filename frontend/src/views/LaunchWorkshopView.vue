<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { completeJourneyStage, defaultJourney, loadJourney, saveJourneyStageEvidence, type JourneyData } from '../services/journey'

type CheckResult = { ok: boolean; message: string }

const projectNames: Record<string, string> = {
  portfolio: '个人作品集',
  blog: '兴趣博客',
  campus: '校园信息站',
}
const technologyNames: Record<string, string> = {
  vue: 'Vue 3',
  vanilla: 'HTML + CSS + JavaScript',
  java: 'Spring Boot',
  python: 'FastAPI',
  mysql: 'MySQL',
  sqlite: 'SQLite',
  later: '暂不使用',
}

const journey = ref<JourneyData>(defaultJourney)
const projectName = computed(() => projectNames[journey.value.project] ?? '我的第一个网站')
const hasBackend = computed(() => journey.value.backend !== 'later')
const hasDatabase = computed(() => hasBackend.value && journey.value.database !== 'later')
const hasDeployment = computed(() => Boolean(journey.value.deploymentUrl) && (!hasBackend.value || Boolean(journey.value.apiUrl)))
const projectCore = computed(() => journey.value.project === 'blog' ? '筛选并打开一篇文章'
  : journey.value.project === 'campus' ? '筛选活动并查看时间和地点'
    : '筛选并打开一项作品')
const stack = computed(() => [
  technologyNames[journey.value.frontend] ?? journey.value.frontend,
  hasBackend.value ? technologyNames[journey.value.backend] ?? journey.value.backend : null,
  hasDatabase.value ? technologyNames[journey.value.database] ?? journey.value.database : null,
].filter(Boolean).join(' · '))

const checks = computed(() => [
  { id: 'build', area: '构建', title: '生产构建可以完成', detail: journey.value.frontend === 'vue' ? '运行 npm run build，没有类型和构建错误。' : '首页文件、样式和脚本都能被浏览器正确读取。', command: journey.value.frontend === 'vue' ? 'npm run build' : 'open index.html' },
  { id: 'content', area: '内容', title: '访客一眼知道你是谁', detail: `首页清楚说明${projectName.value}的主题，并提供至少一个真实内容区。`, command: 'check homepage' },
  { id: 'mobile', area: '体验', title: '手机上没有横向滚动', detail: '在 375px 宽度下检查导航、卡片、文字和按钮。', command: 'viewport: 375px' },
  { id: 'accessibility', area: '可用性', title: '键盘和文字提示可用', detail: '图片有替代文字，输入框有标签，按钮可用 Tab 键访问。', command: 'keyboard + labels' },
  { id: 'core', area: '核心功能', title: `访客能完成“${projectCore.value}”`, detail: '从首页开始完整操作一次，确认结果正确并且每一步都有反馈。', command: 'complete core user flow' },
  hasBackend.value
    ? { id: 'api', area: '接口', title: '公开 API 健康检查通过', detail: `请求发布站保存的 ${technologyNames[journey.value.backend]} 健康地址，确认线上服务真实响应。`, command: journey.value.apiUrl ? `GET ${journey.value.apiUrl}` : 'GET public API health URL' }
    : { id: 'links', area: '链接', title: '站内链接都能打开', detail: '逐个检查导航、作品卡片和联系方式，不留下空链接。', command: 'check all links' },
  hasDatabase.value
    ? { id: 'data', area: '数据', title: '刷新后数据仍然存在', detail: `新增一条内容后刷新页面，确认数据已保存到 ${technologyNames[journey.value.database]}。`, command: 'create → refresh → read' }
    : { id: 'fallback', area: '容错', title: '资源加载失败也有提示', detail: '图片、字体或脚本失败时，页面仍保留可读内容。', command: 'offline check' },
  { id: 'publish', area: '发布', title: '公开地址可以重新访问', detail: `使用无痕窗口打开${journey.value.deploymentUrl ? ` ${journey.value.deploymentUrl}` : '发布站保存的网址'}，再检查首页、移动端和控制台。`, command: journey.value.deploymentUrl ? `open ${journey.value.deploymentUrl}` : 'open public URL' },
  { id: 'revision', area: '迭代', title: '根据一条访客反馈完成修订', detail: '请一位同学尝试核心功能，记录他卡住的位置，修改后再请他复查。', command: 'feedback → fix → retest' },
])

const results = ref<Record<string, CheckResult>>({})
const runningId = ref('')
const promiseChecked = ref(false)
const completed = ref(false)
const loaded = ref(false)
const saving = ref(false)
const error = ref('')
const revisionNote = ref('')
const allPassed = computed(() => checks.value.every((item) => results.value[item.id]?.ok))
const passedCount = computed(() => checks.value.filter((item) => results.value[item.id]?.ok).length)
const healthPercent = computed(() => completed.value ? 100 : Math.round(passedCount.value / checks.value.length * 100))
const revisionReady = computed(() => revisionNote.value.trim().length >= 20)

async function runCheck(id: string) {
  if (runningId.value || results.value[id]?.ok) return
  runningId.value = id
  let result: CheckResult = { ok: true, message: '检查通过，已经记入交付报告。' }
  try {
    if (id === 'api') {
      if (!journey.value.apiUrl) throw new Error('尚未保存公开 API 健康地址')
      const response = await fetch(journey.value.apiUrl)
      if (!response.ok) throw new Error(`接口返回 ${response.status}`)
      result = { ok: true, message: '公开 API 已返回成功状态，线上服务可以访问。' }
    } else {
      await new Promise((resolve) => window.setTimeout(resolve, 320))
      result = { ok: true, message: '已由你完成实际检查并确认结果。' }
    }
  } catch (cause) {
    result = { ok: false, message: cause instanceof Error ? `${cause.message}，请启动服务后重试。` : '检查失败，请重试。' }
  }
  results.value = { ...results.value, [id]: result }
  runningId.value = ''
}

async function finishJourney() {
  if (!allPassed.value || !promiseChecked.value || !hasDeployment.value || !revisionReady.value || saving.value) return
  saving.value = true
  error.value = ''
  try {
    journey.value = await saveJourneyStageEvidence('launch', revisionNote.value.trim())
    journey.value = await completeJourneyStage('launch')
    completed.value = true
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '毕业状态保存失败'
  } finally {
    saving.value = false
  }
}

function downloadReport() {
  const lines = [
    `# ${projectName.value} · 上线检查报告`,
    '',
    `技术路线：${stack.value}`,
    `网站地址：${journey.value.deploymentUrl ?? '未填写'}`,
    ...(journey.value.apiUrl ? [`API 地址：${journey.value.apiUrl}`] : []),
    `检查日期：${new Date().toLocaleDateString('zh-CN')}`,
    '',
    ...checks.value.flatMap((item) => [`- [x] ${item.title}`, `  ${item.detail}`]),
    '',
    `访客反馈与修订：${revisionNote.value}`,
    '',
    '结果：全部检查通过，可以作为第一个网站作品进行展示。',
  ]
  const url = URL.createObjectURL(new Blob([lines.join('\n')], { type: 'text/markdown;charset=utf-8' }))
  const link = document.createElement('a')
  link.href = url
  link.download = `${projectName.value}-上线检查报告.md`
  link.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  try {
    journey.value = await loadJourney()
    completed.value = journey.value.completedStages.includes('launch')
    revisionNote.value = journey.value.stageEvidence.find((item) => item.stageId === 'launch')?.evidence ?? ''
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '建站路线加载失败'
  } finally {
    loaded.value = true
  }
})
</script>

<template>
  <section class="launch-workshop-page">
    <RouterLink class="back-link" to="/courses">← 返回我的建站路线</RouterLink>

    <header class="launch-hero glass-card">
      <div><span class="eyebrow"><i></i> FINAL CHECK · 预计 90 分钟</span><h2>让第一个网站正式毕业</h2><p>从访客视角检查构建、内容、手机体验、{{ hasBackend ? '接口、数据与公开地址' : '链接与公开地址' }}，记录失败项并完成修复，最后整理一份能向别人介绍的作品说明。</p><div class="launch-stack"><span>{{ projectName }}</span><b>{{ stack }}</b></div></div>
      <div class="launch-score" :class="{ ready: allPassed || completed }"><small>项目健康度</small><strong>{{ healthPercent }}</strong><span>/ 100</span><i></i></div>
    </header>

    <section v-if="!completed" class="lesson-agenda glass-card" aria-label="本阶段学习任务">
      <div><span>本阶段路线</span><h3>检查、修复、复查、交付</h3></div>
      <ol><li><i>1</i><span><b>逐项体检</b><small>定位构建、内容与体验问题</small></span></li><li><i>2</i><span><b>修复失败项</b><small>按提示回到代码解决问题</small></span></li><li><i>3</i><span><b>真实环境复查</b><small>从公开网址再走一遍流程</small></span></li><li><i>4</i><span><b>整理作品说明</b><small>讲清成果、技术与解决的问题</small></span></li></ol>
    </section>

    <section v-if="loaded && !hasDeployment && !completed" class="launch-url-warning glass-card"><div><span>还缺一项真实成果</span><h3>先保存可以访问的{{ hasBackend ? '网站与 API 地址' : '网站地址' }}</h3><p>最终检查会使用发布站保存的真实地址。{{ hasBackend ? '全栈路线需要同时发布页面和后端健康接口。' : '即使已经掌握发布知识，也需要留下实际网址。' }}</p></div><RouterLink to="/courses/publish-workshop">返回发布站填写地址 →</RouterLink></section>

    <div v-if="!completed" class="launch-grid">
      <section class="launch-checks glass-card">
        <div class="workbench-heading"><span>交付清单</span><h3>逐项完成上线体检</h3><p>每一项都对应真实项目最常见的交付问题。API 项会请求真实地址，其余项目需要你实际操作后逐项确认。</p></div>
        <div class="launch-progress"><i :style="{ width: `${passedCount / checks.length * 100}%` }"></i></div>
        <div class="launch-progress-copy"><span>{{ passedCount }} / {{ checks.length }} 项通过</span><b>{{ allPassed ? '全部完成 ✓' : '请按真实结果逐项确认' }}</b></div>

        <article v-for="item in checks" :key="item.id" :class="{ passed: results[item.id]?.ok, failed: results[item.id] && !results[item.id].ok, running: runningId === item.id }">
          <div class="launch-check-state">{{ results[item.id]?.ok ? '✓' : runningId === item.id ? '…' : '○' }}</div>
          <div><span>{{ item.area }}</span><h4>{{ item.title }}</h4><p>{{ item.detail }}</p><code>{{ item.command }}</code><small v-if="results[item.id]" :class="{ error: !results[item.id].ok }">{{ results[item.id].message }}</small></div>
          <button type="button" :disabled="Boolean(runningId) || results[item.id]?.ok" @click="runCheck(item.id)">{{ results[item.id]?.ok ? '已通过' : runningId === item.id ? '检查中…' : item.id === 'api' ? '检测真实接口' : '确认已检查' }}</button>
        </article>
      </section>

      <aside class="launch-side">
        <section class="launch-rule glass-card"><span>作品完成标准</span><h3>小而完整，就值得展示</h3><p>第一个网站不需要塞满高级技术。访客能打开、看懂、操作，你能说明自己做了什么，这就是一份真正的项目。</p><ul><li>至少有一个你亲手完成的页面</li><li>至少有一次可见的交互</li><li>同时适配电脑和手机</li><li>有公开地址和运行说明</li></ul></section>
        <section class="lesson-checklist glass-card"><span>毕业确认</span><h3>{{ !hasDeployment ? `先保存真实${hasBackend ? '网站与 API' : '网站'}地址` : allPassed ? '记录最后一次修订' : '完成左侧项目体检' }}</h3><label class="launch-reflection"><b>访客反馈与我的修改</b><textarea v-model="revisionNote" maxlength="600" placeholder="例如：同学第一次没有找到文章筛选按钮，我提高了按钮对比度并修改文案；复查后他能独立完成筛选。"></textarea><small :class="{ ready: revisionReady }">{{ revisionNote.trim().length }} / 20 字起</small></label><label><input v-model="promiseChecked" type="checkbox" :disabled="!allPassed || !hasDeployment || !revisionReady" /><i></i><span>我可以向别人介绍这个网站的主题、技术路线和一个自己解决的问题</span></label><button type="button" :disabled="!allPassed || !promiseChecked || !hasDeployment || !revisionReady || saving" @click="finishJourney">{{ saving ? '正在写入项目档案…' : '完成我的建站之旅' }}</button><small v-if="error" class="practice-error">{{ error }}</small></section>
      </aside>
    </div>

    <section v-else class="project-graduation glass-card">
      <div class="graduation-mark">✓</div><span>FIRST WEBSITE COMPLETED</span><h2>{{ projectName }} 已完成</h2><p>你已经走完从第一行 HTML 到项目上线检查的完整路线。下一次做项目时，可以继续沿用这份检查方法。</p><div><span>技术路线</span><strong>{{ stack }}</strong></div><div class="graduation-actions"><button type="button" @click="downloadReport">下载上线检查报告</button><RouterLink :to="{ path: '/community', query: { compose: 'website', title: `${projectName}完成毕业检查`, content: '我完成了第一次完整的建站路线，也通过了上线前的项目检查。这是我的最终版本，欢迎提出建议。', url: journey.deploymentUrl ?? '' } }">把毕业作品分享到社区</RouterLink><RouterLink to="/">返回学习工作台 →</RouterLink></div>
    </section>
  </section>
</template>
