<script setup lang="ts">
import { computed, ref } from 'vue'
import { completeJourneyStage, readCompletedStages } from '../services/journey'

const props = defineProps<{ stage: 'style' | 'interaction' }>()
type FirstPage = { name: string; introduction: string; interest: string; theme: 'blue' | 'orange' | 'green' }
type StyleSettings = { accent: string; radius: number; spacing: number; shadow: boolean }

const firstPageFallback: FirstPage = {
  name: '小途',
  introduction: '一名正在探索 Web 世界的大一学生。',
  interest: '我喜欢摄影、音乐，也喜欢把新点子做出来。',
  theme: 'blue',
}
let firstPage = firstPageFallback
let storedStyle: StyleSettings | null = null
try { firstPage = JSON.parse(localStorage.getItem('learnpath_first_page') ?? 'null') as FirstPage ?? firstPageFallback }
catch { firstPage = firstPageFallback }
try { storedStyle = JSON.parse(localStorage.getItem('learnpath_page_style') ?? 'null') as StyleSettings | null }
catch { localStorage.removeItem('learnpath_page_style') }

const savedStages = readCompletedStages()
const styleCompleted = ref(savedStages.includes('style'))
const interactionCompleted = ref(savedStages.includes('interaction'))
const accent = ref(storedStyle?.accent ?? '#5b72f2')
const radius = ref(storedStyle?.radius ?? 18)
const spacing = ref(storedStyle?.spacing ?? 24)
const shadow = ref(storedStyle?.shadow ?? true)
const styleTouched = ref(Boolean(storedStyle))
const styleKnowledge = ref(false)
const styleReady = computed(() => styleTouched.value && styleKnowledge.value)
const palette = [
  { name: '深海蓝', value: '#5b72f2' },
  { name: '活力橙', value: '#ef744f' },
  { name: '薄荷绿', value: '#32ad83' },
  { name: '霓虹紫', value: '#9a64ee' },
]
const styleCode = computed(() => `.intro-card {
  padding: ${spacing.value}px;
  border-radius: ${radius.value}px;
  background: ${accent.value};
  box-shadow: ${shadow.value ? '0 18px 40px rgba(0, 0, 0, .18)' : 'none'};
}`)
const previewVars = computed(() => ({
  '--student-accent': accent.value,
  '--student-radius': `${radius.value}px`,
  '--student-spacing': `${spacing.value}px`,
}))

const darkMode = ref(false)
const liked = ref(false)
const storyOpen = ref(false)
const interactions = ref<string[]>([])
const interactionKnowledge = ref(false)
const interactionReady = computed(() => interactions.value.length === 3 && interactionKnowledge.value)
const interactionCode = `const button = document.querySelector('#theme-button')

button.addEventListener('click', () => {
  document.body.classList.toggle('dark')
})`

function pickAccent(value: string) {
  accent.value = value
  styleTouched.value = true
}

function markInteraction(name: string) {
  if (!interactions.value.includes(name)) interactions.value = [...interactions.value, name]
}

function toggleTheme() {
  darkMode.value = !darkMode.value
  markInteraction('theme')
}

function toggleLike() {
  liked.value = !liked.value
  markInteraction('like')
}

function toggleStory() {
  storyOpen.value = !storyOpen.value
  markInteraction('story')
}

function finishStyle() {
  if (!styleReady.value) return
  localStorage.setItem('learnpath_page_style', JSON.stringify({ accent: accent.value, radius: radius.value, spacing: spacing.value, shadow: shadow.value }))
  completeJourneyStage('style')
  styleCompleted.value = true
}

function finishInteraction() {
  if (!interactionReady.value) return
  completeJourneyStage('interaction')
  interactionCompleted.value = true
}
</script>

<template>
  <section class="first-lesson-page workshop-page">
    <RouterLink class="back-link" to="/courses">← 返回我的建站路线</RouterLink>

    <template v-if="stage === 'style'">
      <header class="first-lesson-header glass-card">
        <div><span class="lesson-kicker">第 02 站 · CSS 造型室</span><h2>给网站换件衣服</h2><p>HTML 已经准备好内容，现在轮到 CSS 决定颜色、空间和形状。拖一拖、点一点，观察每条样式怎样改变页面。</p></div>
        <div class="lesson-win"><small>这一站的成果</small><strong>一套自己的视觉主题</strong><span>选择器 · 属性 · 盒模型</span></div>
      </header>

      <div class="first-lesson-grid">
        <section class="lesson-workbench glass-card">
          <div class="workbench-heading"><span>CSS 调色台</span><h3>先从最容易看见的变化开始</h3><p>CSS 的基本写法是“选中谁，然后修改它的某个属性”。这里选中的是介绍卡片。</p></div>

          <div class="style-control">
            <div><strong>主色</strong><small>决定按钮和卡片的重点颜色</small></div>
            <div class="palette-options"><button v-for="color in palette" :key="color.value" type="button" :class="{ selected: accent === color.value }" :title="color.name" :style="{ background: color.value }" @click="pickAccent(color.value)"><span>{{ color.name }}</span></button></div>
          </div>
          <label class="range-control"><span><strong>圆角</strong><small>{{ radius }}px</small></span><input v-model="radius" type="range" min="0" max="34" @input="styleTouched = true" /></label>
          <label class="range-control"><span><strong>内部留白</strong><small>{{ spacing }}px</small></span><input v-model="spacing" type="range" min="12" max="42" @input="styleTouched = true" /></label>
          <label class="switch-control"><span><strong>卡片阴影</strong><small>让卡片像浮在页面上</small></span><input v-model="shadow" type="checkbox" @change="styleTouched = true" /><i></i></label>

          <div class="tiny-theory"><span>拆开一行 CSS</span><h4><code>padding: {{ spacing }}px;</code></h4><p><b>padding</b> 是属性，表示内容与边框之间的空间；<b>{{ spacing }}px</b> 是它的值。冒号把属性和值连起来，分号表示这条设置结束。</p></div>
          <pre class="lesson-code"><code>{{ styleCode }}</code></pre>
        </section>

        <aside class="preview-column">
          <section class="live-browser glass-card">
            <div class="browser-bar"><i></i><i></i><i></i><span>CSS 实时预览</span></div>
            <div class="css-preview" :style="previewVars"><nav><strong>{{ firstPage.name }}</strong><span>首页&nbsp;&nbsp; 关于我</span></nav><main><small>HELLO, WEB!</small><h1>把喜欢的样子<br />写进 <em>CSS</em></h1><p>{{ firstPage.introduction }}</p><section :class="{ flat: !shadow }"><span>最近的我</span><p>{{ firstPage.interest }}</p></section></main></div>
          </section>

          <section class="lesson-checklist glass-card">
            <span>完成检查</span><h3>你已经在指挥页面变装了吗？</h3>
            <div class="workshop-status" :class="{ done: styleTouched }"><i>{{ styleTouched ? '✓' : '1' }}</i><span>调整一次颜色、圆角或留白</span></div>
            <label><input v-model="styleKnowledge" type="checkbox" /><i></i><span>我知道属性是“改什么”，值是“改成什么”</span></label>
            <button type="button" :disabled="!styleReady" @click="finishStyle">{{ styleCompleted ? '第二站已保存 ✓' : '完成第二站' }}</button>
            <RouterLink v-if="styleCompleted" class="next-workshop-link" to="/courses/interaction-workshop">去第三站：让按钮工作 →</RouterLink>
          </section>
        </aside>
      </div>
    </template>

    <template v-else>
      <header class="first-lesson-header glass-card">
        <div><span class="lesson-kicker">第 03 站 · JavaScript 机关室</span><h2>让按钮真的有反应</h2><p>一个按钮被点击时，JavaScript 会收到消息、改变状态，再让页面呈现新的结果。先亲手触发三个机关。</p></div>
        <div class="lesson-win"><small>这一站的成果</small><strong>会回应用户的页面</strong><span>事件 · 状态 · DOM</span></div>
      </header>

      <div class="first-lesson-grid">
        <section class="lesson-workbench glass-card">
          <div class="workbench-heading"><span>三个小机关</span><h3>点击、变化、记住结果</h3><p>每次点击都是一个“事件”。页面记住的深色模式、点赞状态和展开状态，都叫“状态”。</p></div>

          <div class="interaction-missions">
            <button type="button" :class="{ done: interactions.includes('theme') }" @click="toggleTheme"><i>{{ interactions.includes('theme') ? '✓' : '1' }}</i><span><strong>切换白天与夜晚</strong><small>观察整张页面的颜色怎样一起变化</small></span><em>点击试试</em></button>
            <button type="button" :class="{ done: interactions.includes('like') }" @click="toggleLike"><i>{{ interactions.includes('like') ? '✓' : '2' }}</i><span><strong>给兴趣卡片点赞</strong><small>同一个按钮可以在两种状态间切换</small></span><em>{{ liked ? '已点赞' : '点击试试' }}</em></button>
            <button type="button" :class="{ done: interactions.includes('story') }" @click="toggleStory"><i>{{ interactions.includes('story') ? '✓' : '3' }}</i><span><strong>展开隐藏内容</strong><small>让页面根据状态决定显示什么</small></span><em>{{ storyOpen ? '已展开' : '点击试试' }}</em></button>
          </div>

          <div class="event-flow"><span>用户点击</span><i>→</i><span>触发 click 事件</span><i>→</i><span>修改状态</span><i>→</i><span>页面更新</span></div>
          <div class="tiny-theory"><span>关键不是背代码</span><h4>先读懂动作发生的顺序</h4><p><code>addEventListener</code> 像给按钮安排一个值班同学：一听见 click，就执行大括号里的任务。</p></div>
          <pre class="lesson-code"><code>{{ interactionCode }}</code></pre>
        </section>

        <aside class="preview-column">
          <section class="live-browser glass-card">
            <div class="browser-bar"><i></i><i></i><i></i><span>JavaScript 实时预览</span></div>
            <div class="interaction-preview" :class="{ dark: darkMode }"><nav><strong>{{ firstPage.name }}</strong><button type="button" @click="toggleTheme">{{ darkMode ? '☀ 切到白天' : '☾ 切到夜晚' }}</button></nav><main><small>MY LITTLE WEBSITE</small><h1>页面现在会<br /><em>回应你</em></h1><p>{{ firstPage.introduction }}</p><section><div><span>最近的我</span><button type="button" :class="{ liked }" @click="toggleLike">{{ liked ? '♥ 已喜欢' : '♡ 喜欢' }}</button></div><p>{{ firstPage.interest }}</p><button class="story-button" type="button" @click="toggleStory">{{ storyOpen ? '收起故事' : '看看背后的故事 →' }}</button><p v-if="storyOpen" class="hidden-story">这是 JavaScript 帮我们显示出来的新内容。按钮改变了状态，页面根据状态决定是否展示这段话。</p></section></main></div>
          </section>

          <section class="lesson-checklist glass-card">
            <span>完成检查</span><h3>三个机关都接通了吗？</h3>
            <div class="interaction-progress"><i :style="{ width: `${interactions.length / 3 * 100}%` }"></i></div>
            <p class="progress-copy">{{ interactions.length }} / 3 个交互已触发</p>
            <label><input v-model="interactionKnowledge" type="checkbox" /><i></i><span>我知道事件是发生的动作，状态是页面记住的结果</span></label>
            <button type="button" :disabled="!interactionReady" @click="finishInteraction">{{ interactionCompleted ? '第三站已保存 ✓' : '完成第三站' }}</button>
            <RouterLink v-if="interactionCompleted" class="next-workshop-link" to="/courses">回到路线，看看下一站 →</RouterLink>
          </section>
        </aside>
      </div>
    </template>
  </section>
</template>
