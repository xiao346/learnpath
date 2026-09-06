<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { completeJourneyStage, defaultJourney, loadJourney, type JourneyData } from '../services/journey'

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
const stack = computed(() => [
  technologyNames[journey.value.frontend] ?? journey.value.frontend,
  hasBackend.value ? technologyNames[journey.value.backend] ?? journey.value.backend : null,
  hasBackend.value ? technologyNames[journey.value.database] ?? journey.value.database : null,
].filter(Boolean).join(' · '))

const checks = computed(() => [
  { id: 'build', area: '构建', title: '生产构建可以完成', detail: journey.value.frontend === 'vue' ? '运行 npm run build，没有类型和构建错误。' : '首页文件、样式和脚本都能被浏览器正确读取。', command: journey.value.frontend === 'vue' ? 'npm run build' : 'open index.html' },
  { id: 'content', area: '内容', title: '访客一眼知道你是谁', detail: `首页清楚说明${projectName.value}的主题，并提供至少一个真实内容区。`, command: 'check homepage' },
  { id: 'mobile', area: '体验', title: '手机上没有横向滚动', detail: '在 375px 宽度下检查导航、卡片、文字和按钮。', command: 'viewport: 375px' },
  { id: 'accessibility', area: '可用性', title: '键盘和文字提示可用', detail: '图片有替代文字，输入框有标签，按钮可用 Tab 键访问。', command: 'keyboard + labels' },
  hasBackend.value
    ? { id: 'api', area: '接口', title: '后端健康检查通过', detail: `确认 ${technologyNames[journey.value.backend]} 服务能接收请求并返回成功响应。`, command: 'GET /api/public/status' }
    : { id: 'links', area: '链接', title: '站内链接都能打开', detail: '逐个检查导航、作品卡片和联系方式，不留下空链接。', command: 'check all links' },
  hasBackend.value
    ? { id: 'data', area: '数据', title: '刷新后数据仍然存在', detail: `新增一条内容后刷新页面，确认数据已保存到 ${technologyNames[journey.value.database]}。`, command: 'create → refresh → read' }
    : { id: 'fallback', area: '容错', title: '资源加载失败也有提示', detail: '图片、字体或脚本失败时，页面仍保留可读内容。', command: 'offline check' },
  { id: 'publish', area: '发布', title: '公开地址可以重新访问', detail: '使用无痕窗口打开网址，再检查首页、移动端和控制台。', command: 'open public URL' },
])

const results = ref<Record<string, CheckResult>>({})
const runningId = ref('')
const promiseChecked = ref(false)
const completed = ref(false)
const saving = ref(false)
const error = ref('')
const allPassed = computed(() => checks.value.every((item) => results.value[item.id]?.ok))
const passedCount = computed(() => checks.value.filter((item) => results.value[item.id]?.ok).length)
const healthPercent = computed(() => completed.value ? 100 : Math.round(passedCount.value / checks.value.length * 100))

async function runCheck(id: string) {
  if (runningId.value || results.value[id]?.ok) return
  runningId.value = id
  let result: CheckResult = { ok: true, message: '检查通过，已经记入交付报告。' }
  try {
    if (id === 'api') {
      const response = await fetch('/api/public/status')
      const payload = await response.json() as { success?: boolean; data?: { status?: string } }
      if (!response.ok || !payload.success || payload.data?.status !== 'ready') throw new Error('服务尚未就绪')
      result = { ok: true, message: '后端返回 ready，前后端连接正常。' }
    } else {
      await new Promise((resolve) => window.setTimeout(resolve, 320))
    }
  } catch (cause) {
    result = { ok: false, message: cause instanceof Error ? `${cause.message}，请启动服务后重试。` : '检查失败，请重试。' }
  }
  results.value = { ...results.value, [id]: result }
  runningId.value = ''
}

async function runRemainingChecks() {
  for (const item of checks.value) {
    if (!results.value[item.id]?.ok) await runCheck(item.id)
  }
}

async function finishJourney() {
  if (!allPassed.value || !promiseChecked.value || saving.value) return
  saving.value = true
  error.value = ''
  try {
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
    `检查日期：${new Date().toLocaleDateString('zh-CN')}`,
    '',
    ...checks.value.flatMap((item) => [`- [x] ${item.title}`, `  ${item.detail}`]),
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
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '建站路线加载失败'
  }
})
</script>

<template>
  <section class="launch-workshop-page">
    <RouterLink class="back-link" to="/courses">← 返回我的建站路线</RouterLink>

    <header class="launch-hero glass-card">
      <div><span class="eyebrow"><i></i> FINAL CHECK</span><h2>让第一个网站正式毕业</h2><p>最后一站不再增加新功能。我们从访客视角检查构建、内容、手机体验、接口和公开地址，确保作品可以放心展示。</p><div class="launch-stack"><span>{{ projectName }}</span><b>{{ stack }}</b></div></div>
      <div class="launch-score" :class="{ ready: allPassed || completed }"><small>项目健康度</small><strong>{{ healthPercent }}</strong><span>/ 100</span><i></i></div>
    </header>

    <div v-if="!completed" class="launch-grid">
      <section class="launch-checks glass-card">
        <div class="workbench-heading"><span>交付清单</span><h3>逐项完成上线体检</h3><p>每一项都对应真实项目最常见的交付问题。你可以逐项检查，也可以运行全部检查。</p></div>
        <div class="launch-progress"><i :style="{ width: `${passedCount / checks.length * 100}%` }"></i></div>
        <div class="launch-progress-copy"><span>{{ passedCount }} / {{ checks.length }} 项通过</span><button type="button" :disabled="Boolean(runningId) || allPassed" @click="runRemainingChecks">{{ runningId ? '正在检查…' : allPassed ? '全部通过 ✓' : '一键运行剩余检查' }}</button></div>

        <article v-for="item in checks" :key="item.id" :class="{ passed: results[item.id]?.ok, failed: results[item.id] && !results[item.id].ok, running: runningId === item.id }">
          <div class="launch-check-state">{{ results[item.id]?.ok ? '✓' : runningId === item.id ? '…' : '○' }}</div>
          <div><span>{{ item.area }}</span><h4>{{ item.title }}</h4><p>{{ item.detail }}</p><code>{{ item.command }}</code><small v-if="results[item.id]" :class="{ error: !results[item.id].ok }">{{ results[item.id].message }}</small></div>
          <button type="button" :disabled="Boolean(runningId) || results[item.id]?.ok" @click="runCheck(item.id)">{{ results[item.id]?.ok ? '已通过' : results[item.id] ? '重新检查' : '运行检查' }}</button>
        </article>
      </section>

      <aside class="launch-side">
        <section class="launch-rule glass-card"><span>作品完成标准</span><h3>小而完整，就值得展示</h3><p>第一个网站不需要塞满高级技术。访客能打开、看懂、操作，你能说明自己做了什么，这就是一份真正的项目。</p><ul><li>至少有一个你亲手完成的页面</li><li>至少有一次可见的交互</li><li>同时适配电脑和手机</li><li>有公开地址和运行说明</li></ul></section>
        <section class="lesson-checklist glass-card"><span>毕业确认</span><h3>{{ allPassed ? '所有检查都通过了' : '完成左侧项目体检' }}</h3><label><input v-model="promiseChecked" type="checkbox" :disabled="!allPassed" /><i></i><span>我可以向别人介绍这个网站的主题、技术路线和一个自己解决的问题</span></label><button type="button" :disabled="!allPassed || !promiseChecked || saving" @click="finishJourney">{{ saving ? '正在保存到数据库…' : '完成我的建站之旅' }}</button><small v-if="error" class="practice-error">{{ error }}</small></section>
      </aside>
    </div>

    <section v-else class="project-graduation glass-card">
      <div class="graduation-mark">✓</div><span>FIRST WEBSITE COMPLETED</span><h2>{{ projectName }} 已完成</h2><p>你已经走完从第一行 HTML 到项目上线检查的完整路线。下一次做项目时，可以继续沿用这份检查方法。</p><div><span>技术路线</span><strong>{{ stack }}</strong></div><div class="graduation-actions"><button type="button" @click="downloadReport">下载上线检查报告</button><RouterLink to="/">返回学习工作台 →</RouterLink></div>
    </section>
  </section>
</template>
