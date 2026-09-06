<script setup lang="ts">
import { computed, ref } from 'vue'
import { completeJourneyStage, readCompletedStages } from '../services/journey'

type JourneyConfig = { project: string; frontend: string }
let journey: JourneyConfig = { project: 'portfolio', frontend: 'vue' }
try { journey = JSON.parse(localStorage.getItem('learnpath_web_journey') ?? 'null') as JourneyConfig ?? journey }
catch { localStorage.removeItem('learnpath_web_journey') }

const projectNames: Record<string, string> = { portfolio: 'my-portfolio', blog: 'my-blog', campus: 'campus-guide' }
const projectFolder = projectNames[journey.project] ?? 'my-first-site'
const isVue = journey.frontend === 'vue'
const completed = ref(readCompletedStages().includes('publish'))
const finishedSteps = ref<number[]>([])
const consoleLines = ref<string[]>(['准备好了。按顺序完成左边四步，观察网站怎样从本地走向互联网。'])
const knowledgeChecked = ref(false)

const steps = computed(() => [
  {
    title: '保存一个清楚的版本',
    command: 'git add .  &&  git commit -m "完成个人网站"',
    explanation: 'Git 会保存这一刻的代码快照。以后改坏了，可以找到之前能工作的版本。',
    output: `✓ 已保存提交：完成个人网站（${projectFolder}）`,
  },
  {
    title: isVue ? '生成上线文件' : '检查静态网页文件',
    command: isVue ? 'npm run build' : '检查 index.html、style.css 和 app.js',
    explanation: isVue ? '浏览器不直接读取开发工具里的项目结构。构建会生成更小、更适合上线的 dist 文件夹。' : '原生静态网站不一定需要构建，确认入口文件和资源路径正确即可发布。',
    output: isVue ? '✓ 构建完成：dist/ 已生成' : '✓ 静态文件检查完成：入口和资源路径正常',
  },
  {
    title: '把文件交给托管平台',
    command: isVue ? '发布 dist/ 文件夹' : '发布整个网站文件夹',
    explanation: '托管平台把文件放在一直联网的服务器上，并分配一个公开地址。',
    output: '✓ 文件上传完成：正在分配公开地址…',
  },
  {
    title: '从访客视角检查',
    command: `打开 https://${projectFolder}.example.site`,
    explanation: '上线成功不只看“发布完成”，还要重新打开地址，检查首页、手机布局、链接和控制台。',
    output: `✓ 网站可访问：https://${projectFolder}.example.site`,
  },
])
const nextStep = computed(() => finishedSteps.value.length)
const allStepsDone = computed(() => finishedSteps.value.length === steps.value.length)
const canFinish = computed(() => allStepsDone.value && knowledgeChecked.value)

function runStep(index: number) {
  if (index > nextStep.value || finishedSteps.value.includes(index)) return
  finishedSteps.value = [...finishedSteps.value, index]
  consoleLines.value = [...consoleLines.value, `> ${steps.value[index].command}`, steps.value[index].output]
}

function finishWorkshop() {
  if (!canFinish.value) return
  completeJourneyStage('publish')
  completed.value = true
}
</script>

<template>
  <section class="first-lesson-page publish-workshop-page">
    <RouterLink class="back-link" to="/courses">← 返回我的建站路线</RouterLink>
    <header class="first-lesson-header glass-card">
      <div><span class="lesson-kicker">发布站 · 从本地到互联网</span><h2>把网站发给朋友看看</h2><p>“在我的电脑上能打开”只是第一步。这一站会把保存版本、构建和发布连成一条清楚的路线。</p></div>
      <div class="lesson-win"><small>这一站的成果</small><strong>理解网站怎样上线</strong><span>Git · Build · Hosting</span></div>
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
        <div class="workbench-heading"><span>上线模拟器</span><h3>按顺序完成四步</h3><p>这里不会真的发布文件；你会先看懂每一步在真实项目中解决什么问题。</p></div>
        <article v-for="(step, index) in steps" :key="step.title" :class="{ done: finishedSteps.includes(index), active: index === nextStep }">
          <div class="publish-step-number">{{ finishedSteps.includes(index) ? '✓' : index + 1 }}</div>
          <div><h4>{{ step.title }}</h4><code>{{ step.command }}</code><p>{{ step.explanation }}</p></div>
          <button type="button" :disabled="index > nextStep || finishedSteps.includes(index)" @click="runStep(index)">{{ finishedSteps.includes(index) ? '已完成' : index === nextStep ? '执行这一步' : '等待上一步' }}</button>
        </article>
        <RouterLink class="git-course-link" to="/knowledge?keyword=Git"><span>想知道 Git 为什么能回到旧版本？</span><strong>去学习 Git 配套课程 →</strong></RouterLink>
      </section>

      <aside class="publish-side">
        <section class="deploy-console glass-card"><div><i></i><i></i><i></i><span>publish-simulator</span></div><pre><code><span v-for="line in consoleLines" :key="line">{{ line }}</span><b v-if="!allStepsDone">_</b></code></pre></section>
        <section class="lesson-checklist glass-card">
          <span>完成检查</span><h3>{{ allStepsDone ? '网站已经“模拟上线”' : `还差 ${steps.length - finishedSteps.length} 步` }}</h3>
          <div class="interaction-progress"><i :style="{ width: `${finishedSteps.length / steps.length * 100}%` }"></i></div>
          <p class="progress-copy">{{ finishedSteps.length }} / {{ steps.length }} 个发布步骤</p>
          <label><input v-model="knowledgeChecked" type="checkbox" /><i></i><span>我知道 Git 保存版本，Build 生成上线文件，Hosting 提供公开访问</span></label>
          <button type="button" :disabled="!canFinish" @click="finishWorkshop">{{ completed ? '发布站已保存 ✓' : '完成发布站' }}</button>
          <RouterLink v-if="completed" class="next-workshop-link" to="/courses">回到路线，继续升级网站 →</RouterLink>
        </section>
      </aside>
    </div>
  </section>
</template>
