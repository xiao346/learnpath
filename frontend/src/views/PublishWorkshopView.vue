<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { completeJourneyStage, defaultJourney, loadJourney, saveJourneyDeployment, type JourneyData } from '../services/journey'

const projectNames: Record<string, string> = { portfolio: 'my-portfolio', blog: 'my-blog', campus: 'campus-guide' }
const journey = ref<JourneyData>(defaultJourney)
const projectFolder = computed(() => projectNames[journey.value.project] ?? 'my-first-site')
const isVue = computed(() => journey.value.frontend === 'vue')
const completed = ref(false)
const saving = ref(false)
const error = ref('')
const finishedSteps = ref<number[]>([])
const consoleLines = ref<string[]>(['准备好了。按顺序完成左边四步，观察网站怎样从本地走向互联网。'])
const knowledgeChecked = ref(false)
const deploymentUrl = ref('')
const visitorChecked = ref(false)
const validDeploymentUrl = computed(() => /^https?:\/\/.+\..+/.test(deploymentUrl.value.trim()))

const steps = computed(() => [
  {
    title: '保存一个清楚的版本',
    command: 'git add .  &&  git commit -m "完成个人网站"',
    explanation: 'Git 会保存这一刻的代码快照。以后改坏了，可以找到之前能工作的版本。',
    output: `✓ 已保存提交：完成个人网站（${projectFolder.value}）`,
  },
  {
    title: isVue.value ? '生成上线文件' : '检查静态网页文件',
    command: isVue.value ? 'npm run build' : '检查 index.html、style.css 和 app.js',
    explanation: isVue.value ? '浏览器不直接读取开发工具里的项目结构。构建会生成更小、更适合上线的 dist 文件夹。' : '原生静态网站不一定需要构建，确认入口文件和资源路径正确即可发布。',
    output: isVue.value ? '✓ 构建完成：dist/ 已生成' : '✓ 静态文件检查完成：入口和资源路径正常',
  },
  {
    title: '把文件交给托管平台',
    command: isVue.value ? '发布 dist/ 文件夹' : '发布整个网站文件夹',
    explanation: '托管平台把文件放在一直联网的服务器上，并分配一个公开地址。',
    output: '✓ 文件上传完成：正在分配公开地址…',
  },
  {
    title: '从访客视角检查',
    command: '打开托管平台生成的真实网址',
    explanation: '上线成功不只看“发布完成”，还要重新打开地址，检查首页、手机布局、链接和控制台。',
    output: '✓ 发布流程演练完成，下一步请检查自己的真实网址',
  },
])
const nextStep = computed(() => finishedSteps.value.length)
const allStepsDone = computed(() => finishedSteps.value.length === steps.value.length)
const canFinish = computed(() => allStepsDone.value && knowledgeChecked.value && validDeploymentUrl.value && visitorChecked.value)

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
    journey.value = await saveJourneyDeployment(deploymentUrl.value.trim())
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
      <div><span class="lesson-kicker">发布站 · 从本地到互联网</span><h2>把网站发给朋友看看</h2><p>先通过模拟器看懂发布步骤，再把自己的文件真正发布，并用另一个设备检查网址。</p></div>
      <div class="lesson-win"><small>这一站的成果</small><strong>一个真实可以打开的网址</strong><span>Git · Build · Hosting</span></div>
    </header>

    <section class="publish-concepts glass-card">
      <article><i>Git</i><div><strong>版本相册</strong><p>记录每次可靠修改，方便协作和回退。</p></div></article>
      <b>→</b>
      <article><i>Build</i><div><strong>打包行李</strong><p>把开发代码整理成适合浏览器加载的文件。</p></div></article>
      <b>→</b>
      <article><i>Host</i><div><strong>找到住址</strong><p>让一台联网服务器持续提供网站文件。</p></div></article>
      <b>→</b>
      <article><i>URL</i><div><strong>公开门牌</strong><p>访客通过网址找到并打开你的网站。</p></div></article>
    </section>

    <div class="publish-grid">
      <section class="publish-steps glass-card">
        <div class="workbench-heading"><span>先演练，再实做</span><h3>按顺序完成四步</h3><p>模拟器帮你看懂流程；完成后还要把自己的文件交给托管平台，获得真实网址。</p></div>
        <article v-for="(step, index) in steps" :key="step.title" :class="{ done: finishedSteps.includes(index), active: index === nextStep }">
          <div class="publish-step-number">{{ finishedSteps.includes(index) ? '✓' : index + 1 }}</div>
          <div><h4>{{ step.title }}</h4><code>{{ step.command }}</code><p>{{ step.explanation }}</p></div>
          <button type="button" :disabled="index > nextStep || finishedSteps.includes(index)" @click="runStep(index)">{{ finishedSteps.includes(index) ? '已完成' : index === nextStep ? '执行这一步' : '等待上一步' }}</button>
        </article>
        <section class="real-publish-panel">
          <span>真正发布</span><h3>现在换成你自己的项目</h3>
          <ol><li><b>1</b><p>{{ isVue ? '在项目终端运行 npm run build，确认生成 dist 文件夹。' : '确认网站文件夹中有 index.html，并且图片和样式能够正常打开。' }}</p></li><li><b>2</b><p>{{ isVue ? '把 dist 文件夹上传到静态网站托管平台，或连接保存代码的 Git 仓库。' : '把整个网站文件夹上传到静态网站托管平台。' }}</p></li><li><b>3</b><p>复制平台生成的网址，用手机或无痕窗口打开，再回来填写下面的地址。</p></li></ol>
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
          <label><input v-model="knowledgeChecked" type="checkbox" /><i></i><span>我知道 Git 保存版本，Build 生成上线文件，Hosting 提供公开访问</span></label>
          <label class="deployment-field"><span>我的真实网站地址</span><input v-model.trim="deploymentUrl" type="url" placeholder="https://我的网站地址" /><small v-if="deploymentUrl && !validDeploymentUrl">请输入完整的 http:// 或 https:// 地址</small></label>
          <label><input v-model="visitorChecked" type="checkbox" /><i></i><span>我已经用另一个设备或无痕窗口打开并检查了网址</span></label>
          <button type="button" :disabled="!canFinish || saving" @click="finishWorkshop">{{ saving ? '正在保存到数据库…' : completed ? '真实网址已保存 ✓' : '保存网址并完成发布站' }}</button><small v-if="error" class="practice-error">{{ error }}</small>
          <RouterLink v-if="completed" class="next-workshop-link" to="/courses">回到路线，继续升级网站 →</RouterLink>
          <RouterLink v-if="completed" class="journey-share-link" :to="{ path: '/community', query: { compose: 'website', title: '我的第一个网站上线了', content: '我完成了从页面、样式、交互到发布的第一轮建站练习。这是现在可以访问的版本，欢迎给我建议。', url: deploymentUrl } }">带上网址去社区分享</RouterLink>
        </section>
      </aside>
    </div>
  </section>
</template>
