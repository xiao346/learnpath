<script setup lang="ts">
import StageCompass from '../components/StageCompass.vue'
import { computed, onMounted, ref } from 'vue'
import { completeJourneyStage, defaultJourney, loadJourney, saveJourneyDeployment, type JourneyData } from '../services/journey'

const projectNames: Record<string, string> = { portfolio: 'my-portfolio', blog: 'my-blog', campus: 'campus-guide' }
const journey = ref<JourneyData>(defaultJourney)
const projectFolder = computed(() => projectNames[journey.value.project] ?? 'my-first-site')
const isVue = computed(() => journey.value.frontend === 'vue')
const hasBackend = computed(() => journey.value.backend !== 'later')
const hasDatabase = computed(() => hasBackend.value && journey.value.database !== 'later')
const backendName = computed(() => journey.value.backend === 'python' ? 'FastAPI' : 'Spring Boot')
const completed = ref(false)
const saving = ref(false)
const error = ref('')
const finishedSteps = ref<number[]>([])
const consoleLines = ref<string[]>(['准备好了。按顺序完成左侧发布步骤，观察网站怎样从本地走向互联网。'])
const knowledgeChecked = ref(false)
const deploymentUrl = ref('')
const apiUrl = ref('')
const visitorChecked = ref(false)
const validDeploymentUrl = computed(() => /^https?:\/\/.+\..+/.test(deploymentUrl.value.trim()))
const validApiUrl = computed(() => !hasBackend.value || /^https?:\/\/.+\..+/.test(apiUrl.value.trim()))

const steps = computed(() => {
  const result = [{
    title: '保存一个清楚的版本',
    command: 'git add .  &&  git commit -m "完成个人网站"',
    explanation: 'Git 会保存这一刻的代码快照。以后改坏了，可以找到之前能工作的版本。',
    output: `✓ 已保存提交：完成个人网站（${projectFolder.value}）`,
  }, {
    title: isVue.value ? '生成上线文件' : '检查静态网页文件',
    command: isVue.value ? 'npm run build' : '检查 index.html、style.css 和 app.js',
    explanation: isVue.value ? '浏览器不直接读取开发工具里的项目结构。构建会生成更小、更适合上线的 dist 文件夹。' : '原生静态网站不一定需要构建，确认入口文件和资源路径正确即可发布。',
    output: isVue.value ? '✓ 构建完成：dist/ 已生成' : '✓ 静态文件检查完成：入口和资源路径正常',
  }]
  if (hasBackend.value) result.push({
    title: `部署 ${backendName.value} 服务${hasDatabase.value ? '并连接数据库' : ''}`,
    command: hasDatabase.value ? `配置生产环境变量  →  启动 ${backendName.value}  →  检查数据库连接` : `启动 ${backendName.value}  →  检查健康接口`,
    explanation: hasDatabase.value ? '生产环境的服务地址、数据库凭证和跨域来源要通过环境变量配置，不能写死在前端代码里。' : '后端需要独立的公开地址，并允许已经发布的前端域名访问接口。',
    output: `✓ ${backendName.value} 已上线：健康接口可以访问`,
  })
  result.push({
    title: hasBackend.value ? '发布前端并连接线上接口' : '把文件交给托管平台',
    command: hasBackend.value ? '设置生产 API 地址  →  重新构建  →  发布前端' : isVue.value ? '发布 dist/ 文件夹' : '发布整个网站文件夹',
    explanation: hasBackend.value ? '前端必须请求刚刚部署的公开接口，而不是继续请求 localhost。' : '托管平台把文件放在一直联网的服务器上，并分配一个公开地址。',
    output: '✓ 前端文件上传完成：已获得公开地址',
  }, {
    title: '从访客视角完成端到端检查',
    command: hasBackend.value ? '打开公开网站  →  请求线上 API  →  刷新验证数据' : '打开托管平台生成的真实网址',
    explanation: hasBackend.value ? '用无痕窗口完成一次真实功能，确认页面、接口和数据都来自线上环境。' : '重新打开地址，检查首页、手机布局、链接和控制台。',
    output: '✓ 发布流程演练完成，下一步请检查自己的真实网址',
  })
  return result
})
const nextStep = computed(() => finishedSteps.value.length)
const allStepsDone = computed(() => finishedSteps.value.length === steps.value.length)
const canFinish = computed(() => allStepsDone.value && knowledgeChecked.value && validDeploymentUrl.value && validApiUrl.value && visitorChecked.value)

function runStep(index: number) {
  if (index > nextStep.value || finishedSteps.value.includes(index)) return
  finishedSteps.value = [...finishedSteps.value, index]
  consoleLines.value = [...consoleLines.value, `> ${steps.value[index].command}`, steps.value[index].output]
}

async function finishWorkshop() {
  if (!canFinish.value || saving.value) return
  saving.value = true
  error.value = ''
  try {
    journey.value = await saveJourneyDeployment(deploymentUrl.value.trim(), hasBackend.value ? apiUrl.value.trim() : null)
    await completeJourneyStage('publish')
    completed.value = true
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '发布阶段保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    journey.value = await loadJourney()
    deploymentUrl.value = journey.value.deploymentUrl ?? ''
    apiUrl.value = journey.value.apiUrl ?? ''
    completed.value = journey.value.completedStages.includes('publish')
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '建站路线加载失败'
  }
})
</script>

<template>
  <section class="first-lesson-page publish-workshop-page">
    <RouterLink class="back-link" to="/courses">← 返回我的建站路线</RouterLink>
    <header class="first-lesson-header glass-card">
        <div><span class="lesson-kicker">发布站 · 从本地到互联网 · 预计 60–90 分钟</span><h2>把完整版本发给朋友看看</h2><p>{{ hasBackend ? '这一站会发布前端、后端与数据连接，并验证线上完整功能。' : '先通过模拟器看懂发布步骤，再把自己的静态网站真正发布。' }}最后用另一个设备检查网址。</p></div>
      <div class="lesson-win"><small>这一站的成果</small><strong>{{ hasBackend ? '一套真正连通的线上网站与接口' : '一个真实可以打开的网址' }}</strong><span>Git · Build · Hosting{{ hasBackend ? ' · API' : '' }}</span></div>
    </header>
    <StageCompass :config="journey" stage="publish" />

    <section class="lesson-agenda glass-card" aria-label="本阶段学习任务">
      <div><span>本阶段路线</span><h3>从可靠版本到真实网址</h3></div>
      <ol><li><i>1</i><span><b>保存版本</b><small>用 Git 留下可恢复的快照</small></span></li><li><i>2</i><span><b>准备构建</b><small>生成生产环境文件</small></span></li><li><i>3</i><span><b>{{ hasBackend ? '部署前后端' : '发布托管' }}</b><small>{{ hasBackend ? '连接线上接口与数据' : '让服务器提供页面' }}</small></span></li><li><i>4</i><span><b>异地验收</b><small>用手机或无痕窗口复查</small></span></li></ol>
    </section>

    <section class="publish-concepts glass-card" :class="{ 'has-api': hasBackend }">
      <article><i>Git</i><div><strong>版本相册</strong><p>记录每次可靠修改，方便协作和回退。</p></div></article>
      <b>→</b>
      <article><i>Build</i><div><strong>打包行李</strong><p>把开发代码整理成适合浏览器加载的文件。</p></div></article>
      <b>→</b>
      <article><i>Host</i><div><strong>找到住址</strong><p>{{ hasBackend ? '让前端页面和后端服务分别获得稳定运行环境。' : '让一台联网服务器持续提供网站文件。' }}</p></div></article>
      <template v-if="hasBackend"><b>→</b><article><i>API</i><div><strong>接通线上数据</strong><p>页面请求公开接口，并处理跨域、加载和失败状态。</p></div></article></template>
      <b>→</b>
      <article><i>URL</i><div><strong>公开门牌</strong><p>访客通过网址找到并打开你的网站。</p></div></article>
    </section>

    <div class="publish-grid">
      <section class="publish-steps glass-card">
        <div class="workbench-heading"><span>先演练，再实做</span><h3>按顺序完成 {{ steps.length }} 步</h3><p>模拟器帮你看懂流程；完成后还要把自己的完整项目交给托管平台，获得真实网址。</p></div>
        <article v-for="(step, index) in steps" :key="step.title" :class="{ done: finishedSteps.includes(index), active: index === nextStep }">
          <div class="publish-step-number">{{ finishedSteps.includes(index) ? '✓' : index + 1 }}</div>
          <div><h4>{{ step.title }}</h4><code>{{ step.command }}</code><p>{{ step.explanation }}</p></div>
          <button type="button" :disabled="index > nextStep || finishedSteps.includes(index)" @click="runStep(index)">{{ finishedSteps.includes(index) ? '已完成' : index === nextStep ? '执行这一步' : '等待上一步' }}</button>
        </article>
        <section class="real-publish-panel">
          <span>真正发布</span><h3>现在换成你自己的项目</h3>
          <ol><li><b>1</b><p>{{ isVue ? '在项目终端运行 npm run build，确认生成 dist 文件夹且没有构建错误。' : '确认网站文件夹中有 index.html，并且图片、样式和脚本能够正常打开。' }}</p></li><li v-if="hasBackend"><b>2</b><p>部署 {{ backendName }}{{ hasDatabase ? ' 并配置线上数据库' : '' }}，保存公开 API 地址，确认健康接口能够访问。</p></li><li><b>{{ hasBackend ? 3 : 2 }}</b><p>{{ hasBackend ? '把生产 API 地址写进环境变量，重新构建并发布前端，禁止在线上继续使用 localhost。' : isVue ? '把 dist 文件夹上传到静态网站托管平台，或连接保存代码的 Git 仓库。' : '把整个网站文件夹上传到静态网站托管平台。' }}</p></li><li><b>{{ hasBackend ? 4 : 3 }}</b><p>复制平台生成的网址，用手机或无痕窗口打开{{ hasBackend ? '，完成一次会请求接口的真实功能' : '' }}，再回来填写下面的地址。</p></li></ol>
          <details><summary>发布失败时先检查什么？</summary><p>Vue 项目先确认上传的是构建后的 <code>dist</code>；原生网页先确认首页文件名为 <code>index.html</code>。如果页面能开但样式丢失，通常是资源路径写错。</p></details>
        </section>
        <RouterLink class="git-course-link" to="/knowledge?keyword=Git"><span>想知道 Git 为什么能回到旧版本？</span><strong>去学习 Git 配套课程 →</strong></RouterLink>
      </section>

      <aside class="publish-side">
        <section class="deploy-console glass-card"><div><i></i><i></i><i></i><span>publish-simulator</span></div><pre><code><span v-for="line in consoleLines" :key="line">{{ line }}</span><b v-if="!allStepsDone">_</b></code></pre></section>
        <section class="lesson-checklist glass-card">
          <span>完成检查</span><h3>{{ allStepsDone ? '网站已经“模拟上线”' : `还差 ${steps.length - finishedSteps.length} 步` }}</h3>
          <div class="interaction-progress"><i :style="{ width: `${finishedSteps.length / steps.length * 100}%` }"></i></div>
          <p class="progress-copy">{{ finishedSteps.length }} / {{ steps.length }} 个发布步骤</p>
          <label><input v-model="knowledgeChecked" type="checkbox" /><i></i><span>我知道 Git 保存版本，Build 生成上线文件，Hosting 提供公开访问{{ hasBackend ? '，线上前端必须连接公开 API' : '' }}</span></label>
          <label class="deployment-field"><span>我的真实网站地址</span><input v-model.trim="deploymentUrl" type="url" placeholder="https://我的网站地址" /><small v-if="deploymentUrl && !validDeploymentUrl">请输入完整的 http:// 或 https:// 地址</small></label>
          <label v-if="hasBackend" class="deployment-field"><span>我的公开 API 健康检查地址</span><input v-model.trim="apiUrl" type="url" placeholder="https://api.example.com/health" /><small v-if="apiUrl && !validApiUrl">请输入可直接返回成功状态的完整 http:// 或 https:// 地址</small></label>
          <label><input v-model="visitorChecked" type="checkbox" /><i></i><span>我已经用另一个设备或无痕窗口打开网址{{ hasBackend ? '，并走通一次页面—接口—数据流程' : '' }}</span></label>
          <button type="button" :disabled="!canFinish || saving" @click="finishWorkshop">{{ saving ? '正在保存到数据库…' : completed ? '真实网址已保存 ✓' : '保存网址并完成发布站' }}</button><small v-if="error" class="practice-error">{{ error }}</small>
          <RouterLink v-if="completed" class="next-workshop-link" to="/courses">回到路线，继续升级网站 →</RouterLink>
          <RouterLink v-if="completed" class="journey-share-link" :to="{ path: '/community', query: { compose: 'website', title: '我的第一个网站上线了', content: '我完成了从页面、样式、交互到发布的第一轮建站练习。这是现在可以访问的版本，欢迎给我建议。', url: deploymentUrl } }">带上网址去社区分享</RouterLink>
        </section>
      </aside>
    </div>
  </section>
</template>
