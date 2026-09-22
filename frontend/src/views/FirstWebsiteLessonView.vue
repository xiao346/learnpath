<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import StageCompass from '../components/StageCompass.vue'
import { buildFirstPageDocument } from '../content/firstPageDocument'
import { completeJourneyStage, loadJourney, saveJourneyFirstPage, type FirstPageData } from '../services/journey'

const studentName = ref('小途')
const introduction = ref('一名正在探索 Web 世界的大一学生。')
const interest = ref('我喜欢摄影、音乐，也喜欢把新点子做出来。')
const theme = ref<FirstPageData['theme']>('blue')
const project = ref('portfolio')
const checks = ref([false, false, false, false, false])
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
const code = computed(() => buildFirstPageDocument({ name: studentName.value, introduction: introduction.value, interest: interest.value, theme: theme.value }, brief.value.section))
const fileMessage = ref('')
watch([studentName, introduction, interest, theme], () => { saved.value = false; fileMessage.value = ''; checks.value = checks.value.map(() => false) }, { flush: 'sync' })

function downloadPage() {
  const url = URL.createObjectURL(new Blob([code.value], { type: 'text/html;charset=utf-8' }))
  const link = document.createElement('a')
  link.href = url
  link.download = 'index.html'
  link.click()
  window.setTimeout(() => URL.revokeObjectURL(url), 1000)
  fileMessage.value = '已发起下载。请在浏览器下载列表找到 index.html，移入你的项目文件夹后打开。'
}

async function copyCode() {
  try {
    await navigator.clipboard.writeText(code.value)
    fileMessage.value = '代码已复制。在编辑器里粘贴，保存为 index.html，再用浏览器打开。'
  } catch {
    fileMessage.value = '浏览器未允许复制。可以下载 HTML 文件，或手动选中下方代码复制。'
  }
}

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
      <div><span class="lesson-kicker">第 01 站 · 预计 60 分钟</span><h2>{{ brief.title }}</h2><p>{{ brief.intro }}先用下面的起步文件打开自己的页面，再读懂它、亲手改一次。完整学习约 60 分钟，第一次看到页面可以更早。</p></div>
      <div class="lesson-win"><small>这一站的成果</small><strong>{{ brief.result }}</strong><span>HTML 结构 · 文字内容</span></div>
    </header>
    <StageCompass :config="{ project }" stage="intro" />

    <section class="lesson-agenda glass-card" aria-label="本阶段学习任务">
      <div><span>本阶段路线</span><h3>完成 4 个任务，交付一张真实首页</h3></div>
      <ol><li><i>1</i><span><b>规划内容</b><small>先决定访客要看到什么</small></span></li><li><i>2</i><span><b>读懂标签</b><small>认识标题、段落和内容区</small></span></li><li><i>3</i><span><b>写进文件</b><small>创建并保存 index.html</small></span></li><li><i>4</i><span><b>刷新验证</b><small>确认修改真的出现在浏览器</small></span></li></ol>
    </section>

    <div class="first-lesson-grid">
      <section class="lesson-workbench glass-card">
        <div class="workbench-heading"><span>先看效果</span><h3>把示例换成你的真实内容</h3><p>右侧是这份 HTML 的真实浏览器预览。先填内容，下载文件后，你就能在自己的电脑里打开它。</p></div>
        <label class="lesson-field"><span>网页上怎么称呼你？</span><input v-model="studentName" maxlength="16" /><small>可以是真名、昵称，也可以是你的网名。</small></label>
        <label class="lesson-field"><span>用一句话介绍自己</span><input v-model="introduction" maxlength="42" /></label>
        <label class="lesson-field"><span>{{ brief.field }}</span><textarea v-model="interest" maxlength="72"></textarea></label>
        <details class="future-theme"><summary>可选：记下下一站想试的配色</summary><fieldset class="theme-picker"><legend>给第一屏选个气氛</legend><label><input v-model="theme" type="radio" value="blue" /><span class="blue"></span>夜空蓝</label><label><input v-model="theme" type="radio" value="orange" /><span class="orange"></span>落日橙</label><label><input v-model="theme" type="radio" value="green" /><span class="green"></span>薄荷绿</label></fieldset></details>

        <section class="local-practice first-file-steps">
          <span>先拿到第一个成果 · 建议先做这一步</span><h4>让电脑打开你写的网页</h4>
          <div class="first-file-actions"><button type="button" @click="downloadPage">下载我的 index.html</button><button type="button" @click="copyCode">复制 HTML 代码</button></div>
          <p class="file-message" role="status">{{ fileMessage }}</p>
          <ol>
            <li><b>1</b><p><strong>给作品找个家。</strong>在桌面空白处右键 → 新建 → 文件夹，命名为 <code>my-first-site</code>。今后的网页、图片和样式都放在这里。</p></li>
            <li><b>2</b><p><strong>放入起步文件。</strong>点击上方下载，在浏览器的下载列表选择“在文件夹中显示”，把下载的 <code>index.html</code> 移入刚建的文件夹。如果多次下载产生 <code>index (1).html</code>，请选定一份作为项目文件，后续始终编辑这一份。</p></li>
            <li><b>3</b><p><strong>在浏览器看效果。</strong>右键这个文件 → 打开方式 → 选择 Edge、Chrome 或其他浏览器。看到自己的标题和内容，就已经有第一张网页了。此时没有颜色和卡片排版是正常的。</p></li>
            <li><b>4</b><p><strong>亲手改一次。</strong>同一个文件右键 → 打开方式 → 记事本（或代码编辑器）。找到 <code>&lt;h1&gt;</code> 后面的标题，在文字后加上“出发！”，按 <kbd>Ctrl + S</kbd> 保存，再回浏览器刷新。标题出现新文字，就是你的修改在起作用。</p></li>
          </ol>
          <details><summary>我想自己创建文件，应该怎么保存？</summary><p>打开记事本，粘贴上面的代码，选择“文件 → 另存为”，保存位置选 my-first-site，文件名填 index.html，保存类型选“所有文件”，编码选 UTF-8。在文件资源管理器开启“文件扩展名”，确认结尾不是 .txt。</p><p>Mac 用户可用文本编辑：先选择“格式 → 制作纯文本”，再保存为 index.html，确认没有自动添加 .txt。保存快捷键为 Command + S。</p></details>
          <details><summary>查看这份 HTML 代码，试着找到你的标题</summary><pre class="lesson-code"><code>{{ code }}</code></pre></details>
          <p>这里提供的是起步文件。之后请在自己的文件中继续创作；本地修改不会自动同步到本页表单。重新下载会生成表单当前内容，请注意保留自己的修改。</p>
        </section>

        <section class="concept-lab">
          <header><span>任务 2 · 读懂结构</span><h3>浏览器不是在“猜”，标签说清了每段内容的身份</h3><p>HTML 把内容组织成一棵结构树。选对标签后，浏览器、搜索引擎和读屏软件都更容易理解页面。</p></header>
          <div class="concept-card-grid"><article><code>&lt;h1&gt;</code><strong>页面主标题</strong><p>一页通常只有一个，先告诉访客这里是什么。</p></article><article><code>&lt;p&gt;</code><strong>普通段落</strong><p>承载介绍和正文，不要用多个换行代替段落。</p></article><article><code>&lt;section&gt;</code><strong>一组相关内容</strong><p>把“最近的我”这样的独立主题组织在一起。</p></article><article><code>&lt;a&gt;</code><strong>可以访问的链接</strong><p>用 href 指向作品、文章或联系方式。</p></article></div>
          <div class="browser-flow"><span>读取 index.html</span><i>→</i><span>识别标签关系</span><i>→</i><span>生成页面结构</span><i>→</i><span>绘制到屏幕</span></div>
        </section>

        <div class="tiny-theory"><span>刚刚发生了什么？</span><h4>HTML 描述“是什么”，不是“长什么样”</h4><p><code>&lt;h1&gt;</code> 告诉浏览器这是最重要的标题，<code>&lt;p&gt;</code> 表示普通文字。标签负责内容结构；下一站再用 CSS 决定颜色、间距和布局。</p></div>

        <section class="practice-challenge"><span>加深挑战 · 10 分钟</span><h4>再增加一个真正有用的内容区</h4><p>选择“关于我、作品列表、文章目录、活动须知”中的一个，用新的 <code>&lt;section&gt;</code> 写出标题和两段内容。完成后检查：即使没有颜色和排版，访客是否仍能看懂页面顺序？</p></section>
      </section>

      <aside class="preview-column">
        <section class="live-browser glass-card"><div class="browser-bar"><i></i><i></i><i></i><span>当前 HTML 的真实效果</span></div><iframe class="raw-html-preview" title="第一张网页的 HTML 预览" sandbox="" :srcdoc="code"></iframe></section>
        <details class="future-preview"><summary>想看看加上 CSS 后可以变成什么样？</summary><p>这是下一站的设计参考，当前下载的文件只包含 HTML 内容。</p>
        <section class="live-browser glass-card">
          <div class="browser-bar"><i></i><i></i><i></i><span>下一站的外观参考 · {{ themeLabel }}</span></div>
          <div class="personal-preview" :class="`theme-${theme}`"><nav><strong>{{ studentName }}</strong><span>首页&nbsp;&nbsp; {{ brief.section }}</span></nav><main><small>HELLO, WEB!</small><h1>你好，我是<br /><em>{{ studentName || '在这里写名字' }}</em></h1><p>{{ introduction || '用一句话介绍自己。' }}</p><div class="preview-interest"><span>{{ brief.section }}</span><p>{{ interest || brief.field }}</p></div></main></div>
        </section>

        </details>

        <section class="lesson-checklist glass-card">
          <span>完成检查</span><h3>网页已经有你的样子了吗？</h3>
          <label><input v-model="checks[0]" type="checkbox" /><i></i><span>页面上出现了我的名字</span></label>
          <label><input v-model="checks[1]" type="checkbox" /><i></i><span>介绍文字已经换成自己的内容</span></label>
          <label><input v-model="checks[2]" type="checkbox" /><i></i><span>我能说出 h1 和 p 分别表示什么</span></label>
          <label><input v-model="checks[3]" type="checkbox" /><i></i><span>我已经在自己的 index.html 中打开了页面</span></label>
          <label><input v-model="checks[4]" type="checkbox" /><i></i><span>我修改并刷新过一次，确认浏览器显示的是最新内容</span></label>
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

<style scoped>
.first-file-actions { display: flex; flex-wrap: wrap; gap: 10px; margin: 18px 0 8px; }.first-file-actions button { border: 1px solid #628bab; border-radius: 9px; padding: 11px 16px; color: #fff; background: #426f91; }.first-file-actions button + button { color: #426f91; background: transparent; }.first-file-steps p, .future-preview p { font-size: 14px; line-height: 1.85; color: #58748a; }.first-file-steps strong { color: #294b63; }.file-message:empty { display: none; }.raw-html-preview { display: block; width: 100%; height: 340px; border: 0; background: white; }.future-preview, .future-theme { margin: 18px 0; }.future-preview > summary, .future-theme > summary { color: #426f91; cursor: pointer; font-size: 14px; line-height: 1.7; }.first-file-steps details { margin: 14px 0; }.first-file-steps pre { max-height: 480px; overflow: auto; }
</style>
