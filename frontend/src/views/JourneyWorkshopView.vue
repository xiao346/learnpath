<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { completeJourneyStage, loadJourney, saveJourneyStyle, type FirstPageData, type StyleData } from '../services/journey'

const props = defineProps<{ stage: 'style' | 'interaction' }>()
const firstPageFallback: FirstPageData = {
  name: '小途',
  introduction: '一名正在探索 Web 世界的大一学生。',
  interest: '我喜欢摄影、音乐，也喜欢把新点子做出来。',
  theme: 'blue',
}
const firstPage = ref<FirstPageData>(firstPageFallback)
const project = ref('portfolio')
const styleCompleted = ref(false)
const interactionCompleted = ref(false)
const accent = ref('#5b72f2')
const radius = ref(18)
const spacing = ref(24)
const shadow = ref(true)
const headingSize = ref(31)
const bodyLineHeight = ref(1.6)
const previewMode = ref<'desktop' | 'mobile'>('desktop')
const styleExperiments = ref<string[]>([])
const saving = ref(false)
const error = ref('')
const styleKnowledge = ref(false)
const stylePracticeChecked = ref(false)
const styleReady = computed(() => styleExperiments.value.length === 3 && styleKnowledge.value && stylePracticeChecked.value)
const palette = [
  { name: '深海蓝', value: '#5b72f2' },
  { name: '活力橙', value: '#ef744f' },
  { name: '薄荷绿', value: '#32ad83' },
  { name: '霓虹紫', value: '#9a64ee' },
]
const styleCode = computed(() => `h1 {
  font-size: ${headingSize.value}px;
}

.intro-card {
  padding: ${spacing.value}px;
  border-radius: ${radius.value}px;
  background: ${accent.value};
  box-shadow: ${shadow.value ? '0 18px 40px rgba(0, 0, 0, .18)' : 'none'};
  line-height: ${bodyLineHeight.value};
}

@media (max-width: 480px) {
  .intro-card { padding: ${Math.max(12, spacing.value - 6)}px; }
}`)
const previewVars = computed(() => ({
  '--student-accent': accent.value,
  '--student-radius': `${radius.value}px`,
  '--student-spacing': `${spacing.value}px`,
  '--student-heading-size': `${headingSize.value}px`,
  '--student-line-height': String(bodyLineHeight.value),
}))

const filterActive = ref(false)
const liked = ref(false)
const storyOpen = ref(false)
const interactions = ref<string[]>([])
const interactionKnowledge = ref(false)
const interactionPracticeChecked = ref(false)
const interactionDebugChecked = ref(false)
const interactionReady = computed(() => interactions.value.length === 3 && interactionKnowledge.value && interactionPracticeChecked.value && interactionDebugChecked.value)
const interactionCode = computed(() => `const button = document.querySelector('#filter-button')
const cards = document.querySelectorAll('[data-category]')
let filtering = false

button.addEventListener('click', () => {
  filtering = !filtering
  cards.forEach((card) => {
    card.hidden = filtering && card.dataset.category !== '重点'
  })
  button.textContent = filtering ? '显示全部' : '${projectBrief.value.filterLabel}'
})`)
const projectBriefs: Record<string, { section: string; styleTask: string; interactions: string[]; interactionResult: string; filterLabel: string; filteredCopy: string }> = {
  portfolio: { section: '作品卡片', styleTask: '让作品标题、介绍和链接在一张卡片里层级清楚', interactions: ['只看前端作品', '收藏喜欢的作品', '展开作品详情'], interactionResult: '访客能筛选并查看你的作品', filterLabel: '只看前端作品', filteredCopy: '当前显示：前端作品 · 个人网站重构' },
  blog: { section: '文章卡片', styleTask: '让文章标题、摘要和发布时间读起来更舒服', interactions: ['只看学习笔记', '收藏想读的文章', '展开文章摘要'], interactionResult: '读者能筛选并阅读你的文章', filterLabel: '只看学习笔记', filteredCopy: '当前显示：学习笔记 · 我的第一周复盘' },
  campus: { section: '活动卡片', styleTask: '让活动名称、时间和地点一眼就能看清', interactions: ['只看本周活动', '收藏想参加的活动', '展开活动详情'], interactionResult: '同学能筛选并查看活动信息', filterLabel: '只看本周活动', filteredCopy: '当前显示：本周活动 · 周五社团开放日' },
}
const projectBrief = computed(() => projectBriefs[project.value] ?? projectBriefs.portfolio)

function pickAccent(value: string) {
  accent.value = value
  markStyleExperiment('hierarchy')
}

function markStyleExperiment(name: string) {
  if (!styleExperiments.value.includes(name)) styleExperiments.value = [...styleExperiments.value, name]
}

function setPreviewMode(mode: 'desktop' | 'mobile') {
  previewMode.value = mode
  if (mode === 'mobile') markStyleExperiment('mobile')
}

function markInteraction(name: string) {
  if (!interactions.value.includes(name)) interactions.value = [...interactions.value, name]
}

function toggleFilter() {
  filterActive.value = !filterActive.value
  markInteraction('filter')
}

function toggleLike() {
  liked.value = !liked.value
  markInteraction('like')
}

function toggleStory() {
  storyOpen.value = !storyOpen.value
  markInteraction('story')
}

async function finishStyle() {
  if (!styleReady.value || saving.value) return
  saving.value = true
  error.value = ''
  try {
    const style: StyleData = { accent: accent.value, radius: radius.value, spacing: spacing.value, shadow: shadow.value }
    await saveJourneyStyle(style)
    await completeJourneyStage('style')
    styleCompleted.value = true
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '页面样式保存失败'
  } finally {
    saving.value = false
  }
}

async function finishInteraction() {
  if (!interactionReady.value || saving.value) return
  saving.value = true
  error.value = ''
  try {
    await completeJourneyStage('interaction')
    interactionCompleted.value = true
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '互动阶段保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const journey = await loadJourney()
    project.value = journey.project
    firstPage.value = journey.firstPage
    accent.value = journey.style.accent
    radius.value = journey.style.radius
    spacing.value = journey.style.spacing
    shadow.value = journey.style.shadow
    styleCompleted.value = journey.completedStages.includes('style')
    interactionCompleted.value = journey.completedStages.includes('interaction')
    if (styleCompleted.value) {
      styleExperiments.value = ['hierarchy', 'box', 'mobile']
      styleKnowledge.value = true
      stylePracticeChecked.value = true
    }
    if (interactionCompleted.value) {
      interactions.value = ['filter', 'like', 'story']
      interactionKnowledge.value = true
      interactionPracticeChecked.value = true
      interactionDebugChecked.value = true
    }
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '建站作品加载失败'
  }
})
</script>

<template>
  <section class="first-lesson-page workshop-page">
    <RouterLink class="back-link" to="/courses">← 返回我的建站路线</RouterLink>

    <template v-if="stage === 'style'">
      <header class="first-lesson-header glass-card">
        <div><span class="lesson-kicker">第 02 站 · CSS 造型室 · 预计 90 分钟</span><h2>给网站换件衣服</h2><p>从文字层级、盒模型到手机适配，完成三组实验，再把生成的 CSS 写回自己的项目。</p></div>
        <div class="lesson-win"><small>这一站的成果</small><strong>一套自己的{{ projectBrief.section }}样式</strong><span>选择器 · 属性 · 盒模型</span></div>
      </header>

      <section class="lesson-agenda glass-card" aria-label="本阶段学习任务">
        <div><span>本阶段路线</span><h3>3 组样式实验 + 1 次本地实做</h3></div>
        <ol><li><i>1</i><span><b>文字层级</b><small>字号、颜色和行高</small></span></li><li><i>2</i><span><b>卡片盒模型</b><small>留白、圆角和阴影</small></span></li><li><i>3</i><span><b>手机适配</b><small>在 375px 下检查页面</small></span></li><li><i>4</i><span><b>写回项目</b><small>保存 CSS 并刷新验证</small></span></li></ol>
      </section>

      <div class="first-lesson-grid">
        <section class="lesson-workbench glass-card">
          <div class="workbench-heading"><span>CSS 实验台</span><h3>让每一次调整都有明确目的</h3><p>这一站要{{ projectBrief.styleTask }}。按三个任务调整右侧预览，同时观察代码中的属性和值怎样变化。</p></div>

          <div class="workshop-task-heading"><i>1</i><div><strong>拉开文字层级</strong><small>标题要先被看见，正文要能持续阅读。</small></div><span :class="{ done: styleExperiments.includes('hierarchy') }">{{ styleExperiments.includes('hierarchy') ? '已实验' : '待完成' }}</span></div>
          <div class="style-control">
            <div><strong>主色</strong><small>决定按钮和卡片的重点颜色</small></div>
            <div class="palette-options"><button v-for="color in palette" :key="color.value" type="button" :class="{ selected: accent === color.value }" :title="color.name" :style="{ background: color.value }" @click="pickAccent(color.value)"><span>{{ color.name }}</span></button></div>
          </div>
          <label class="range-control"><span><strong>主标题字号</strong><small>{{ headingSize }}px · 建立视觉重点</small></span><input v-model.number="headingSize" type="range" min="24" max="46" @input="markStyleExperiment('hierarchy')" /></label>
          <label class="range-control"><span><strong>正文行高</strong><small>{{ bodyLineHeight }} · 避免文字挤在一起</small></span><input v-model.number="bodyLineHeight" type="range" min="1.2" max="2" step="0.1" @input="markStyleExperiment('hierarchy')" /></label>

          <div class="workshop-task-heading"><i>2</i><div><strong>看懂卡片的盒模型</strong><small>内容、内边距、边框和外部空间共同决定卡片尺寸。</small></div><span :class="{ done: styleExperiments.includes('box') }">{{ styleExperiments.includes('box') ? '已实验' : '待完成' }}</span></div>
          <label class="range-control"><span><strong>圆角</strong><small>{{ radius }}px · 改变边框转角</small></span><input v-model.number="radius" type="range" min="0" max="34" @input="markStyleExperiment('box')" /></label>
          <label class="range-control"><span><strong>内部留白</strong><small>{{ spacing }}px · 内容到边框的距离</small></span><input v-model.number="spacing" type="range" min="12" max="42" @input="markStyleExperiment('box')" /></label>
          <label class="switch-control"><span><strong>卡片阴影</strong><small>比较有阴影和无阴影的层次差异</small></span><input v-model="shadow" type="checkbox" @change="markStyleExperiment('box')" /><i></i></label>

          <div class="workshop-task-heading"><i>3</i><div><strong>切到手机宽度检查</strong><small>窄屏不应出现横向滚动，文字与按钮仍要清楚。</small></div><span :class="{ done: styleExperiments.includes('mobile') }">{{ styleExperiments.includes('mobile') ? '已检查' : '待完成' }}</span></div>
          <div class="preview-mode-picker"><button type="button" :class="{ active: previewMode === 'desktop' }" @click="setPreviewMode('desktop')">电脑 · 960px</button><button type="button" :class="{ active: previewMode === 'mobile' }" @click="setPreviewMode('mobile')">手机 · 375px</button></div>

          <section class="concept-lab compact">
            <header><span>四个必须会区分的概念</span><h3>CSS 不是“调好看”，而是在建立稳定规则</h3></header>
            <div class="concept-card-grid"><article><code>selector</code><strong>选择器</strong><p>决定这条规则要修改页面里的谁。</p></article><article><code>padding</code><strong>内边距</strong><p>控制内容与卡片边框之间的空间。</p></article><article><code>gap / flex</code><strong>布局</strong><p>安排多个元素的方向、间隔和对齐。</p></article><article><code>@media</code><strong>响应式</strong><p>屏幕变窄时使用另一组更合适的规则。</p></article></div>
          </section>
          <div class="tiny-theory"><span>拆开一行 CSS</span><h4><code>padding: {{ spacing }}px;</code></h4><p><b>padding</b> 是属性，表示内容与边框之间的空间；<b>{{ spacing }}px</b> 是它的值。冒号连接属性和值，分号表示这条声明结束。</p></div>
          <pre class="lesson-code"><code>{{ styleCode }}</code></pre>
          <section class="local-practice"><span>任务 4 · 写回自己的项目</span><h4>把实验结果变成真正的 CSS</h4><ol><li><b>1</b><p>在 <code>index.html</code> 同级创建 <code>style.css</code>，并在 HTML 的 <code>&lt;head&gt;</code> 中用 <code>&lt;link rel="stylesheet" href="style.css"&gt;</code> 引入。</p></li><li><b>2</b><p>复制上面的规则，根据自己的页面类名修改 <code>.intro-card</code>，不要为了生效就给所有元素都加 <code>!important</code>。</p></li><li><b>3</b><p>把浏览器窗口拖窄到约 375px，检查标题换行、卡片宽度和正文阅读；发现溢出时优先检查固定宽度。</p></li><li><b>4</b><p>至少修改一处数值，保存并刷新。用“改了哪条规则、页面哪里变化”描述结果。</p></li></ol><details><summary>样式没有生效时怎么查？</summary><p>先检查 <code>style.css</code> 路径和类名是否一致，再用浏览器开发者工具查看规则是否被划掉。被划掉通常表示有另一条优先级更高或位置更靠后的规则覆盖了它。</p></details></section>
        </section>

        <aside class="preview-column">
          <section class="live-browser glass-card style-preview-frame" :class="{ mobile: previewMode === 'mobile' }">
            <div class="browser-bar"><i></i><i></i><i></i><span>CSS 实时预览</span></div>
            <div class="css-preview" :style="previewVars"><nav><strong>{{ firstPage.name }}</strong><span>首页&nbsp;&nbsp; 关于我</span></nav><main><small>HELLO, WEB!</small><h1>把喜欢的样子<br />写进 <em>CSS</em></h1><p>{{ firstPage.introduction }}</p><section :class="{ flat: !shadow }"><span>最近的我</span><p>{{ firstPage.interest }}</p></section></main></div>
          </section>

          <section class="lesson-checklist glass-card">
            <span>完成检查</span><h3>你已经在指挥页面变装了吗？</h3>
            <div class="workshop-status" :class="{ done: styleExperiments.includes('hierarchy') }"><i>{{ styleExperiments.includes('hierarchy') ? '✓' : '1' }}</i><span>调整过标题字号、正文行高或主色</span></div>
            <div class="workshop-status" :class="{ done: styleExperiments.includes('box') }"><i>{{ styleExperiments.includes('box') ? '✓' : '2' }}</i><span>比较过留白、圆角或阴影</span></div>
            <div class="workshop-status" :class="{ done: styleExperiments.includes('mobile') }"><i>{{ styleExperiments.includes('mobile') ? '✓' : '3' }}</i><span>切到 375px 手机宽度检查过页面</span></div>
            <label><input v-model="styleKnowledge" type="checkbox" /><i></i><span>我知道属性是“改什么”，值是“改成什么”</span></label>
            <label><input v-model="stylePracticeChecked" type="checkbox" /><i></i><span>我已经把 CSS 写回自己的项目并刷新验证</span></label>
            <button type="button" :disabled="!styleReady || saving" @click="finishStyle">{{ saving ? '正在保存到数据库…' : styleCompleted ? '第二站已保存 ✓' : '完成第二站' }}</button><small v-if="error" class="practice-error">{{ error }}</small>
            <RouterLink v-if="styleCompleted" class="next-workshop-link" to="/courses/interaction-workshop">去第三站：让按钮工作 →</RouterLink>
          </section>
        </aside>
      </div>
    </template>

    <template v-else>
      <header class="first-lesson-header glass-card">
        <div><span class="lesson-kicker">第 03 站 · JavaScript 机关室 · 预计 2 小时</span><h2>让按钮真的有反应</h2><p>先拆开“点击—状态—更新”的完整过程，再亲手触发、编写和调试三个交互，最后写回自己的页面。</p></div>
        <div class="lesson-win"><small>这一站的成果</small><strong>{{ projectBrief.interactionResult }}</strong><span>事件 · 状态 · DOM</span></div>
      </header>

      <section class="lesson-agenda glass-card" aria-label="本阶段学习任务">
        <div><span>本阶段路线</span><h3>3 个交互实验 + 1 次本地实做</h3></div>
        <ol><li><i>1</i><span><b>找到元素</b><small>用选择器拿到页面按钮</small></span></li><li><i>2</i><span><b>监听事件</b><small>点击后执行指定函数</small></span></li><li><i>3</i><span><b>更新状态</b><small>让文字、样式和内容同步变化</small></span></li><li><i>4</i><span><b>调试验证</b><small>重复操作并检查异常情况</small></span></li></ol>
      </section>

      <div class="first-lesson-grid">
        <section class="lesson-workbench glass-card">
          <div class="workbench-heading"><span>三个项目功能</span><h3>筛选、收藏、展开详情</h3><p>每次点击都是一个“事件”。页面记住的筛选条件、收藏状态和展开状态，都叫“状态”；三个功能都直接服务于你的{{ projectBrief.section }}。</p></div>

          <section class="concept-lab compact">
            <header><span>先拆开一次点击</span><h3>交互由四个角色合作完成</h3><p>只看最终效果容易误以为按钮“自己会动”。真正的过程是先找到元素，再监听事件、修改状态，最后更新界面。</p></header>
            <div class="concept-card-grid"><article><code>querySelector</code><strong>找到元素</strong><p>根据 id 或 class 取得要控制的按钮。</p></article><article><code>click</code><strong>监听事件</strong><p>用户点击时，浏览器才调用对应函数。</p></article><article><code>filtering</code><strong>保存状态</strong><p>用变量记住当前是否正在筛选、收藏或展开。</p></article><article><code>hidden</code><strong>更新界面</strong><p>根据新状态更新卡片、按钮文字或详情内容。</p></article></div>
          </section>

          <div class="interaction-missions">
            <button type="button" :class="{ done: interactions.includes('filter') }" @click="toggleFilter"><i>{{ interactions.includes('filter') ? '✓' : '1' }}</i><span><strong>{{ projectBrief.interactions[0] }}</strong><small>根据项目内容筛选出访客真正想看的卡片</small></span><em>{{ filterActive ? '显示全部' : '点击试试' }}</em></button>
            <button type="button" :class="{ done: interactions.includes('like') }" @click="toggleLike"><i>{{ interactions.includes('like') ? '✓' : '2' }}</i><span><strong>{{ projectBrief.interactions[1] }}</strong><small>同一个按钮可以在两种状态间切换</small></span><em>{{ liked ? '已收藏' : '点击试试' }}</em></button>
            <button type="button" :class="{ done: interactions.includes('story') }" @click="toggleStory"><i>{{ interactions.includes('story') ? '✓' : '3' }}</i><span><strong>{{ projectBrief.interactions[2] }}</strong><small>让页面根据状态决定显示什么</small></span><em>{{ storyOpen ? '已展开' : '点击试试' }}</em></button>
          </div>

          <div class="event-flow"><span>用户点击</span><i>→</i><span>触发 click 事件</span><i>→</i><span>修改状态</span><i>→</i><span>页面更新</span></div>
          <div class="tiny-theory"><span>关键不是背代码</span><h4>先读懂动作发生的顺序</h4><p><code>addEventListener</code> 像给按钮安排一个值班同学：一听见 click，就执行大括号里的任务。</p></div>
          <pre class="lesson-code"><code>{{ interactionCode }}</code></pre>
          <section class="local-practice"><span>任务 4 · 写回自己的项目</span><h4>给{{ projectBrief.section }}接上真实筛选</h4><ol><li><b>1</b><p>为每张卡片添加 <code>data-category</code>，再添加 <code>&lt;button id="filter-button"&gt;{{ projectBrief.filterLabel }}&lt;/button&gt;</code>。</p></li><li><b>2</b><p>在页面底部引入 <code>app.js</code>，复制上面的事件代码，并确认按钮 id 与卡片属性完全一致。</p></li><li><b>3</b><p>点击筛选按钮，确认不符合条件的卡片隐藏，按钮文字变为“显示全部”。</p></li><li><b>4</b><p>连续点击两次，确认列表能筛选再恢复；再测试没有匹配项时是否给访客明确提示。</p></li></ol><details><summary>点击没有反应时按什么顺序检查？</summary><p>先看控制台是否报错，再检查脚本是否加载、选择器是否找到元素、事件名是否拼对，最后在回调函数第一行加入 <code>console.log('clicked')</code>，确认点击是否真的进入函数。</p></details></section>
        </section>

        <aside class="preview-column">
          <section class="live-browser glass-card">
            <div class="browser-bar"><i></i><i></i><i></i><span>JavaScript 实时预览</span></div>
            <div class="interaction-preview"><nav><strong>{{ firstPage.name }}</strong><button type="button" :class="{ active: filterActive }" @click="toggleFilter">{{ filterActive ? '显示全部' : projectBrief.filterLabel }}</button></nav><main><small>MY LITTLE WEBSITE</small><h1>页面现在会<br /><em>回应你</em></h1><p>{{ firstPage.introduction }}</p><p v-if="filterActive" class="filter-status">{{ projectBrief.filteredCopy }}</p><section><div><span>{{ projectBrief.section }}</span><button type="button" :class="{ liked }" @click="toggleLike">{{ liked ? '♥ 已收藏' : '♡ 收藏' }}</button></div><p>{{ filterActive ? projectBrief.filteredCopy : firstPage.interest }}</p><button class="story-button" type="button" @click="toggleStory">{{ storyOpen ? '收起详情' : '查看详情 →' }}</button><p v-if="storyOpen" class="hidden-story">这是 JavaScript 根据当前状态显示的详情。收起后内容仍在数据中，只是不再显示在界面上。</p></section></main></div>
          </section>

          <section class="lesson-checklist glass-card">
            <span>完成检查</span><h3>三个机关都接通了吗？</h3>
            <div class="interaction-progress"><i :style="{ width: `${interactions.length / 3 * 100}%` }"></i></div>
            <p class="progress-copy">{{ interactions.length }} / 3 个交互已触发</p>
            <label><input v-model="interactionKnowledge" type="checkbox" /><i></i><span>我知道事件是发生的动作，状态是页面记住的结果</span></label>
            <label><input v-model="interactionPracticeChecked" type="checkbox" /><i></i><span>我已经在自己的页面里接入至少一个按钮事件</span></label>
            <label><input v-model="interactionDebugChecked" type="checkbox" /><i></i><span>我重复点击并用控制台检查过交互结果</span></label>
            <button type="button" :disabled="!interactionReady || saving" @click="finishInteraction">{{ saving ? '正在保存到数据库…' : interactionCompleted ? '第三站已保存 ✓' : '完成第三站' }}</button><small v-if="error" class="practice-error">{{ error }}</small>
            <RouterLink v-if="interactionCompleted" class="next-workshop-link" to="/courses">回到路线，看看下一站 →</RouterLink>
          </section>
        </aside>
      </div>
    </template>
  </section>
</template>
