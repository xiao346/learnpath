<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { completeJourneyStage, loadJourney, saveJourneyFirstPage, type FirstPageData } from '../services/journey'

const studentName = ref('小途')
const introduction = ref('一名正在探索 Web 世界的大一学生。')
const interest = ref('我喜欢摄影、音乐，也喜欢把新点子做出来。')
const theme = ref<FirstPageData['theme']>('blue')
const project = ref('portfolio')
const checks = ref([false, false, false, false])
const saved = ref(false)
const saving = ref(false)
const error = ref('')
const allChecked = computed(() => checks.value.every(Boolean))
const themeLabel = computed(() => ({ blue: '夜空蓝', orange: '落日橙', green: '薄荷绿' })[theme.value])
const projectBriefs: Record<string, { title: string; intro: string; section: string; field: string; result: string }> = {
  portfolio: { title: '做出你的个人作品集首页', intro: '先让访客认识你，再放上一件你想展示的作品。', section: '我的第一件作品', field: '第一件想展示的作品是什么？', result: '一张能介绍你和第一件作品的首页' },
  blog: { title: '写出你的兴趣博客首页', intro: '先告诉读者你会分享什么，再写下第一篇短文章。', section: '第一篇文章', field: '第一篇文章想写什么？', result: '一张有主题和第一篇文章的博客首页' },
  campus: { title: '搭好你的校园信息站首页', intro: '先说明这个站点帮助谁，再发布第一条校园信息。', section: '第一条校园信息', field: '第一条想分享的校园信息是什么？', result: '一张有说明和真实信息的校园站首页' },
}
const brief = computed(() => projectBriefs[project.value] ?? projectBriefs.portfolio)
const code = computed(() => `<main>
  <p>你好，我是</p>
  <h1>${studentName.value}</h1>
  <p>${introduction.value}</p>

  <section>
    <h2>最近的我</h2>
    <p>${interest.value}</p>
  </section>
</main>`)

async function finishLesson() {
  if (!allChecked.value || saving.value) return
  saving.value = true
  error.value = ''
  try {
    await saveJourneyFirstPage({ name: studentName.value, introduction: introduction.value, interest: interest.value, theme: theme.value })
    await completeJourneyStage('intro')
    saved.value = true
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '首页内容保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const journey = await loadJourney()
    project.value = journey.project
    studentName.value = journey.firstPage.name
    introduction.value = journey.firstPage.introduction
    interest.value = journey.firstPage.interest
    theme.value = journey.firstPage.theme
    saved.value = journey.completedStages.includes('intro')
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '首页内容加载失败'
  }
})
</script>

<template>
  <section class="first-lesson-page">
    <RouterLink class="back-link" to="/courses">← 返回我的建站路线</RouterLink>
    <header class="first-lesson-header glass-card">
      <div><span class="lesson-kicker">第 01 站 · 预计 25 分钟</span><h2>{{ brief.title }}</h2><p>{{ brief.intro }}先在这里看到效果，再把代码保存到自己的文件里。</p></div>
      <div class="lesson-win"><small>这一站的成果</small><strong>{{ brief.result }}</strong><span>HTML 结构 · 文字内容</span></div>
    </header>

    <div class="first-lesson-grid">
      <section class="lesson-workbench glass-card">
        <div class="workbench-heading"><span>先看效果</span><h3>把示例换成你的真实内容</h3><p>右边的网页会立刻跟着变化。先看懂内容和页面的关系，再去自己的文件里做一次。</p></div>
        <label class="lesson-field"><span>网页上怎么称呼你？</span><input v-model="studentName" maxlength="16" /><small>可以是真名、昵称，也可以是你的网名。</small></label>
        <label class="lesson-field"><span>用一句话介绍自己</span><input v-model="introduction" maxlength="42" /></label>
        <label class="lesson-field"><span>{{ brief.field }}</span><textarea v-model="interest" maxlength="72"></textarea></label>
        <fieldset class="theme-picker"><legend>给第一屏选个气氛</legend><label><input v-model="theme" type="radio" value="blue" /><span class="blue"></span>夜空蓝</label><label><input v-model="theme" type="radio" value="orange" /><span class="orange"></span>落日橙</label><label><input v-model="theme" type="radio" value="green" /><span class="green"></span>薄荷绿</label></fieldset>

        <div class="tiny-theory"><span>刚刚发生了什么？</span><h4>HTML 像一份“内容清单”</h4><p><code>&lt;h1&gt;</code> 告诉浏览器这里是最重要的标题，<code>&lt;p&gt;</code> 表示一段普通文字。标签负责说明内容是什么，稍后 CSS 会负责它长什么样。</p></div>
        <pre class="lesson-code"><code>{{ code }}</code></pre>
        <section class="local-practice">
          <span>轮到你亲手做</span><h4>在自己的电脑上打开这张首页</h4>
          <ol><li><b>1</b><p>新建一个网站文件夹，再创建 <code>index.html</code>。</p></li><li><b>2</b><p>把上面的代码输入或复制进去，换成自己的内容并保存。</p></li><li><b>3</b><p>双击 <code>index.html</code>，确认浏览器里出现你的标题和内容。</p></li></ol>
          <details><summary>保存后没有看到页面？</summary><p>先确认文件名是 <code>index.html</code>，不是 <code>index.html.txt</code>；再按 Ctrl + S 保存，并刷新浏览器。如果只看到代码文字，通常是文件扩展名不正确。</p></details>
        </section>
      </section>

      <aside class="preview-column">
        <section class="live-browser glass-card">
          <div class="browser-bar"><i></i><i></i><i></i><span>我的第一个网站 · {{ themeLabel }}</span></div>
          <div class="personal-preview" :class="`theme-${theme}`"><nav><strong>{{ studentName }}</strong><span>首页&nbsp;&nbsp; {{ brief.section }}</span></nav><main><small>HELLO, WEB!</small><h1>你好，我是<br /><em>{{ studentName || '在这里写名字' }}</em></h1><p>{{ introduction || '用一句话介绍自己。' }}</p><div class="preview-interest"><span>{{ brief.section }}</span><p>{{ interest || brief.field }}</p></div></main></div>
        </section>

        <section class="lesson-checklist glass-card">
          <span>完成检查</span><h3>网页已经有你的样子了吗？</h3>
          <label><input v-model="checks[0]" type="checkbox" /><i></i><span>页面上出现了我的名字</span></label>
          <label><input v-model="checks[1]" type="checkbox" /><i></i><span>介绍文字已经换成自己的内容</span></label>
          <label><input v-model="checks[2]" type="checkbox" /><i></i><span>我能说出 h1 和 p 分别表示什么</span></label>
          <label><input v-model="checks[3]" type="checkbox" /><i></i><span>我已经在自己的 index.html 中打开了页面</span></label>
          <button type="button" :disabled="!allChecked || saving" @click="finishLesson">{{ saving ? '正在保存到数据库…' : saved ? '第一站已保存 ✓' : '完成第一站' }}</button>
          <small v-if="error" class="practice-error">{{ error }}</small>
          <p v-if="saved">干得漂亮。下一站，我们会用 CSS 给它认真换一身衣服。</p>
          <RouterLink v-if="saved" class="next-workshop-link" to="/courses/style-workshop">去第二站：学习 CSS →</RouterLink>
          <RouterLink v-if="saved" class="journey-share-link" :to="{ path: '/community', query: { compose: 'journey', title: '我完成了第一张网页', content: `我刚完成了${brief.title}，第一次在浏览器里打开了自己写的 HTML。下一步准备学习 CSS。` } }">分享这次进展</RouterLink>
        </section>
      </aside>
    </div>
  </section>
</template>
