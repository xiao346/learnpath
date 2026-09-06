<script setup lang="ts">
import { computed, ref } from 'vue'
import { completeJourneyStage, readCompletedStages } from '../services/journey'

type FirstPage = { name: string; introduction: string; interest: string; theme: 'blue' | 'orange' | 'green' }
let firstPage: FirstPage | null = null
try { firstPage = JSON.parse(localStorage.getItem('learnpath_first_page') ?? 'null') as FirstPage | null }
catch { localStorage.removeItem('learnpath_first_page') }
const studentName = ref(firstPage?.name ?? '小途')
const introduction = ref(firstPage?.introduction ?? '一名正在探索 Web 世界的大一学生。')
const interest = ref(firstPage?.interest ?? '我喜欢摄影、音乐，也喜欢把新点子做出来。')
const theme = ref<'blue' | 'orange' | 'green'>(firstPage?.theme ?? 'blue')
const checks = ref([false, false, false])
const saved = ref(readCompletedStages().includes('intro'))
const allChecked = computed(() => checks.value.every(Boolean))
const themeLabel = computed(() => ({ blue: '夜空蓝', orange: '落日橙', green: '薄荷绿' })[theme.value])
const code = computed(() => `<main>
  <p>你好，我是</p>
  <h1>${studentName.value}</h1>
  <p>${introduction.value}</p>

  <section>
    <h2>最近的我</h2>
    <p>${interest.value}</p>
  </section>
</main>`)

function finishLesson() {
  if (!allChecked.value) return
  localStorage.setItem('learnpath_first_page', JSON.stringify({ name: studentName.value, introduction: introduction.value, interest: interest.value, theme: theme.value }))
  completeJourneyStage('intro')
  saved.value = true
}
</script>

<template>
  <section class="first-lesson-page">
    <RouterLink class="back-link" to="/courses">← 返回我的建站路线</RouterLink>
    <header class="first-lesson-header glass-card">
      <div><span class="lesson-kicker">第 01 站 · 预计 25 分钟</span><h2>你好，这是我的网站</h2><p>今天先不背概念。改三段文字，选一种颜色，让浏览器第一次替你介绍自己。</p></div>
      <div class="lesson-win"><small>这一站的成果</small><strong>一张属于你的首页</strong><span>HTML 结构 · 文字内容</span></div>
    </header>

    <div class="first-lesson-grid">
      <section class="lesson-workbench glass-card">
        <div class="workbench-heading"><span>动手区</span><h3>把示例换成你的故事</h3><p>右边的网页会立刻跟着变化。网页开发最迷人的地方，就是你的修改马上有回应。</p></div>
        <label class="lesson-field"><span>网页上怎么称呼你？</span><input v-model="studentName" maxlength="16" /><small>可以是真名、昵称，也可以是你的网名。</small></label>
        <label class="lesson-field"><span>用一句话介绍自己</span><input v-model="introduction" maxlength="42" /></label>
        <label class="lesson-field"><span>最近喜欢什么？</span><textarea v-model="interest" maxlength="72"></textarea></label>
        <fieldset class="theme-picker"><legend>给第一屏选个气氛</legend><label><input v-model="theme" type="radio" value="blue" /><span class="blue"></span>夜空蓝</label><label><input v-model="theme" type="radio" value="orange" /><span class="orange"></span>落日橙</label><label><input v-model="theme" type="radio" value="green" /><span class="green"></span>薄荷绿</label></fieldset>

        <div class="tiny-theory"><span>刚刚发生了什么？</span><h4>HTML 像一份“内容清单”</h4><p><code>&lt;h1&gt;</code> 告诉浏览器这里是最重要的标题，<code>&lt;p&gt;</code> 表示一段普通文字。标签负责说明内容是什么，稍后 CSS 会负责它长什么样。</p></div>
        <pre class="lesson-code"><code>{{ code }}</code></pre>
      </section>

      <aside class="preview-column">
        <section class="live-browser glass-card">
          <div class="browser-bar"><i></i><i></i><i></i><span>我的第一个网站 · {{ themeLabel }}</span></div>
          <div class="personal-preview" :class="`theme-${theme}`"><nav><strong>{{ studentName }}</strong><span>关于我&nbsp;&nbsp; 我的兴趣</span></nav><main><small>HELLO, WEB!</small><h1>你好，我是<br /><em>{{ studentName || '在这里写名字' }}</em></h1><p>{{ introduction || '用一句话介绍自己。' }}</p><div class="preview-interest"><span>最近的我</span><p>{{ interest || '写下一件你最近喜欢的事。' }}</p></div></main></div>
        </section>

        <section class="lesson-checklist glass-card">
          <span>完成检查</span><h3>网页已经有你的样子了吗？</h3>
          <label><input v-model="checks[0]" type="checkbox" /><i></i><span>页面上出现了我的名字</span></label>
          <label><input v-model="checks[1]" type="checkbox" /><i></i><span>介绍文字已经换成自己的内容</span></label>
          <label><input v-model="checks[2]" type="checkbox" /><i></i><span>我能说出 h1 和 p 分别表示什么</span></label>
          <button type="button" :disabled="!allChecked" @click="finishLesson">{{ saved ? '第一站已保存 ✓' : '完成第一站' }}</button>
          <p v-if="saved">干得漂亮。下一站，我们会用 CSS 给它认真换一身衣服。</p>
          <RouterLink v-if="saved" class="next-workshop-link" to="/courses/style-workshop">去第二站：学习 CSS →</RouterLink>
        </section>
      </aside>
    </div>
  </section>
</template>
