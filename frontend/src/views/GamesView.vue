<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { completeGameChallenge, loadGameProgress } from '../services/games'

type GameId = 'layout' | 'repair' | 'circuit' | 'quiz'
type Alignment = 'flex-start' | 'center' | 'space-between' | 'flex-end'

const activeGame = ref<GameId>('layout')
const score = ref(0)
const completedChallengeCount = ref(0)
const totalChallenges = ref(21)
const awardedChallenges = new Set<string>()
const savingChallenge = ref(false)
const gameError = ref('')

async function awardOnce(challenge: string) {
  if (awardedChallenges.has(challenge)) return
  const progress = await completeGameChallenge(challenge)
  awardedChallenges.clear()
  progress.completedChallenges.forEach((item) => awardedChallenges.add(item))
  score.value = progress.totalScore
  completedChallengeCount.value = progress.completedCount
  totalChallenges.value = progress.totalChallenges
}

const layoutTargets: { value: Alignment; label: string; hint: string }[] = [
  { value: 'center', label: '挤到中间', hint: '让三个小伙伴在容器中央集合。' },
  { value: 'space-between', label: '分散站开', hint: '第一个靠左，最后一个靠右，中间均匀分布。' },
  { value: 'flex-end', label: '站到右边', hint: '让它们一起移动到容器右侧。' },
]
const layoutOptions: { value: Alignment; label: string }[] = [
  { value: 'flex-start', label: 'flex-start' },
  { value: 'center', label: 'center' },
  { value: 'space-between', label: 'space-between' },
  { value: 'flex-end', label: 'flex-end' },
]
const layoutLevel = ref(0)
const layoutAnswer = ref<Alignment>('flex-start')
const layoutFeedback = ref('')
const layoutSolved = ref(false)
const layoutTarget = computed(() => layoutTargets[layoutLevel.value])

const repairChallenges = [
  {
    title: '卡片冲出了手机屏幕',
    clue: '这张卡片写死了 480px 宽度，小屏幕只有 320px。',
    broken: 'width: 480px;',
    correct: 'max-width: 100%;',
    options: ['max-width: 100%;', 'font-size: 100px;', 'display: none;'],
    success: '卡片会在宽屏保持自然宽度，在小屏时缩到容器以内。',
  },
  {
    title: '照片被拉成了长脸',
    clue: '图片被塞进固定大小的封面，但没有规定如何裁切。',
    broken: 'width: 100%; height: 180px;',
    correct: 'object-fit: cover;',
    options: ['object-fit: cover;', 'text-align: center;', 'overflow: auto;'],
    success: 'cover 会保持图片比例，再裁掉超出封面的部分。',
  },
  {
    title: '按钮们在窄屏打架',
    clue: '一行放不下所有按钮，需要允许它们换到下一行。',
    broken: 'display: flex;',
    correct: 'flex-wrap: wrap;',
    options: ['flex-wrap: wrap;', 'opacity: 0;', 'position: fixed;'],
    success: '空间不足时，按钮会自动换行，不再互相挤压。',
  },
]
const repairLevel = ref(0)
const repairChoice = ref('')
const repairFeedback = ref('')
const repairSolved = ref(false)
const repairTarget = computed(() => repairChallenges[repairLevel.value])

const circuitChallenges = [
  {
    title: '按钮听不见点击',
    before: "const button = document.querySelector('#menu')",
    after: 'toggleMenu()',
    correct: "button.addEventListener('click', () => {",
    options: ["button.addEventListener('click', () => {", 'button = click {', 'button.style.click = true'],
    closing: '})',
    success: '监听器已经接通：点击发生时，toggleMenu 会被调用。',
  },
  {
    title: '菜单不会切换状态',
    before: "const menu = document.querySelector('.menu')",
    after: '',
    correct: "menu.classList.toggle('open')",
    options: ["menu.classList.toggle('open')", "menu.className = ''", "menu.remove('open')"],
    closing: '',
    success: 'toggle 会在有 open 时移除、没有时添加，正好适合开关状态。',
  },
  {
    title: '表单一提交就刷新',
    before: "form.addEventListener('submit', (event) => {",
    after: 'saveMessage()',
    correct: 'event.preventDefault()',
    options: ['event.preventDefault()', 'event.refresh()', 'form.stop = true'],
    closing: '})',
    success: '默认提交刷新被阻止，现在可以先用 JavaScript 处理表单。',
  },
]
const circuitLevel = ref(0)
const circuitChoice = ref('')
const circuitFeedback = ref('')
const circuitSolved = ref(false)
const circuitTarget = computed(() => circuitChallenges[circuitLevel.value])

const courseQuizChallenges = [
  { course: 'HTML 与 CSS', icon: '</>', question: '想让“查看作品”文字可以点击并跳转，应该使用哪个 HTML 标签？', options: ['<a>', '<p>', '<strong>'], correct: '<a>', explanation: '<a> 是超链接标签，href 属性负责写目标地址。' },
  { course: 'JavaScript', icon: 'JS', question: '按钮点击后要执行一个函数，浏览器最常用哪个方法监听点击？', options: ['addEventListener', 'queryDatabase', 'setColorOnly'], correct: 'addEventListener', explanation: 'addEventListener 可以把 click 等事件和要执行的函数连接起来。' },
  { course: 'Vue 3', icon: 'V', question: '在组合式 API 中，哪个工具可以创建会触发页面更新的简单响应式值？', options: ['ref', 'fetch', 'style'], correct: 'ref', explanation: 'ref 包装数据后，修改它的 value 会让使用该数据的界面自动更新。' },
  { course: 'Java Web', icon: 'J', question: 'Spring Boot 中要让方法处理 GET 请求，最合适的注解是？', options: ['@GetMapping', '@Entity', '@BeanOnly'], correct: '@GetMapping', explanation: '@GetMapping 把一个 GET 地址映射到控制器方法。' },
  { course: 'FastAPI', icon: 'Py', question: 'FastAPI 中要声明 GET /posts 接口，应该使用哪段装饰器？', options: ["@app.get('/posts')", "@app.table('posts')", "@app.css('/posts')"], correct: "@app.get('/posts')", explanation: '@app.get 会把路径和 Python 函数连接成 GET 接口。' },
  { course: '数据库原理', icon: 'DB', question: '用户表中需要一个永不重复的编号来识别每位用户，它最适合做什么？', options: ['主键', '备注', '临时变量'], correct: '主键', explanation: '主键必须唯一且不能为空，适合稳定识别一行数据。' },
  { course: '计算机网络', icon: '⌁', question: '在浏览器输入域名后，先把域名转换为 IP 地址的服务是什么？', options: ['DNS', 'CSS', 'Git'], correct: 'DNS', explanation: 'DNS 像互联网通讯录，把容易记的域名解析为 IP 地址。' },
  { course: '数据结构与算法', icon: '⌘', question: '网站消息按照“先到先处理”的顺序排队，最贴近哪种数据结构？', options: ['队列', '栈', '二叉树'], correct: '队列', explanation: '队列遵循先进先出，先进入的消息会先被处理。' },
  { course: '软件工程与 Git', icon: 'Git', question: '准备保存一次清楚的代码版本时，先把修改加入暂存区的命令是？', options: ['git add', 'git clone', 'git delete'], correct: 'git add', explanation: 'git add 选择下一次提交要包含的修改，然后再用 git commit 保存版本。' },
  { course: 'Python 数据分析', icon: 'Pd', question: '要在 Python 中处理带行和列的二维表格，Pandas 最常用哪个结构？', options: ['DataFrame', 'Set', 'String'], correct: 'DataFrame', explanation: 'DataFrame 用行列组织数据，适合筛选、统计和清洗表格。' },
  { course: '人工智能导论', icon: 'AI', question: '用已经标注“猫/狗”的图片训练分类器，这属于哪种学习方式？', options: ['监督学习', '随机排序', '网页布局'], correct: '监督学习', explanation: '监督学习从带有正确答案的样本中学习输入与标签的关系。' },
  { course: '大学英语', icon: 'A+', question: '向访客介绍自己的网站目标，哪一句表达最清楚？', options: ['This website helps students share campus stories.', 'Website very good.', 'I am website.'], correct: 'This website helps students share campus stories.', explanation: '完整句子说明了网站做什么以及帮助谁，适合作品介绍。' },
]
const quizLevel = ref(0)
const quizChoice = ref('')
const quizFeedback = ref('')
const quizSolved = ref(false)
const quizTarget = computed(() => courseQuizChallenges[quizLevel.value])

const gameMeta = [
  { id: 'layout' as GameId, icon: '▦', title: '布局拼拼乐', description: '用 Flex 把元素送到正确位置' },
  { id: 'repair' as GameId, icon: '⌁', title: '样式修理铺', description: '找出让页面变形的 CSS' },
  { id: 'circuit' as GameId, icon: '⚡', title: '按钮机关屋', description: '补上代码，让交互重新工作' },
  { id: 'quiz' as GameId, icon: '✦', title: '课程闪答', description: '12 门课程各来一道小挑战' },
]

function selectLayout(value: Alignment) {
  layoutAnswer.value = value
  layoutFeedback.value = ''
  layoutSolved.value = false
}

async function checkLayout() {
  if (layoutAnswer.value === layoutTarget.value.value) {
    savingChallenge.value = true
    gameError.value = ''
    try {
      await awardOnce(`layout-${layoutLevel.value}`)
      layoutFeedback.value = '摆对了！justify-content 控制主轴上的排列方式，成绩已保存到数据库。'
      layoutSolved.value = true
    } catch (cause) {
      gameError.value = cause instanceof Error ? cause.message : '游戏成绩保存失败'
    } finally {
      savingChallenge.value = false
    }
  } else {
    layoutFeedback.value = layoutAnswer.value === 'flex-start' ? '现在它们还挤在起点，再观察目标位置。' : '已经移动了，但和目标还有一点差别。'
  }
}

function nextLayout() {
  layoutLevel.value = (layoutLevel.value + 1) % layoutTargets.length
  layoutAnswer.value = 'flex-start'
  layoutFeedback.value = ''
  layoutSolved.value = false
}

async function checkRepair() {
  if (!repairChoice.value) return
  if (repairChoice.value === repairTarget.value.correct) {
    savingChallenge.value = true
    gameError.value = ''
    try {
      await awardOnce(`repair-${repairLevel.value}`)
      repairFeedback.value = `${repairTarget.value.success} 成绩已保存到数据库。`
      repairSolved.value = true
    } catch (cause) {
      gameError.value = cause instanceof Error ? cause.message : '游戏成绩保存失败'
    } finally {
      savingChallenge.value = false
    }
  } else {
    repairFeedback.value = '这条代码没有解决题目里的布局原因，再看一次线索。'
  }
}

function nextRepair() {
  repairLevel.value = (repairLevel.value + 1) % repairChallenges.length
  repairChoice.value = ''
  repairFeedback.value = ''
  repairSolved.value = false
}

async function checkCircuit() {
  if (!circuitChoice.value) return
  if (circuitChoice.value === circuitTarget.value.correct) {
    savingChallenge.value = true
    gameError.value = ''
    try {
      await awardOnce(`circuit-${circuitLevel.value}`)
      circuitFeedback.value = `${circuitTarget.value.success} 成绩已保存到数据库。`
      circuitSolved.value = true
    } catch (cause) {
      gameError.value = cause instanceof Error ? cause.message : '游戏成绩保存失败'
    } finally {
      savingChallenge.value = false
    }
  } else {
    circuitFeedback.value = '线路还没有接通。注意代码要使用浏览器真正认识的方法。'
  }
}

function nextCircuit() {
  circuitLevel.value = (circuitLevel.value + 1) % circuitChallenges.length
  circuitChoice.value = ''
  circuitFeedback.value = ''
  circuitSolved.value = false
}

async function checkQuiz() {
  if (!quizChoice.value) return
  if (quizChoice.value === quizTarget.value.correct) {
    savingChallenge.value = true
    gameError.value = ''
    try {
      await awardOnce(`quiz-${quizLevel.value}`)
      quizFeedback.value = `${quizTarget.value.explanation} 成绩已保存到数据库。`
      quizSolved.value = true
    } catch (cause) {
      gameError.value = cause instanceof Error ? cause.message : '游戏成绩保存失败'
    } finally {
      savingChallenge.value = false
    }
  } else {
    quizFeedback.value = '还差一点。先想想这个工具在真实项目里负责哪一件事。'
  }
}

function nextQuiz() {
  quizLevel.value = (quizLevel.value + 1) % courseQuizChallenges.length
  quizChoice.value = ''
  quizFeedback.value = ''
  quizSolved.value = false
}

onMounted(async () => {
  try {
    const progress = await loadGameProgress()
    score.value = progress.totalScore
    completedChallengeCount.value = progress.completedCount
    totalChallenges.value = progress.totalChallenges
    progress.completedChallenges.forEach((item) => awardedChallenges.add(item))
  } catch (cause) {
    gameError.value = cause instanceof Error ? cause.message : '游戏进度加载失败'
  }
})
</script>

<template>
  <section class="games-page">
    <header class="journey-heading"><div><span class="eyebrow"><i></i> PLAY AND LEARN</span><h2>趣味闯关</h2><p>用几分钟的小挑战，把抽象知识变成手上的感觉。</p><small v-if="gameError" class="practice-error">{{ gameError }}</small></div><div class="game-score"><small>完成 {{ completedChallengeCount }}/{{ totalChallenges }} 关</small><strong>{{ score }}</strong><span>XP</span></div></header>

    <div class="game-layout">
      <section v-if="activeGame === 'layout'" class="active-game glass-card">
        <div class="game-topline"><div><span>CSS 训练场 · 第 {{ layoutLevel + 1 }} / {{ layoutTargets.length }} 关</span><h3>布局拼拼乐</h3></div><em>约 3 分钟</em></div>
        <div class="mission-card"><span>本关任务</span><strong>{{ layoutTarget.label }}</strong><p>{{ layoutTarget.hint }}</p></div>
        <div class="layout-arenas"><div><span>目标队形</span><div class="layout-arena target" :style="{ justifyContent: layoutTarget.value }"><i>A</i><i>B</i><i>C</i></div></div><div><span>你的队形</span><div class="layout-arena player" :class="{ correct: layoutSolved }" :style="{ justifyContent: layoutAnswer }"><i>A</i><i>B</i><i>C</i></div></div></div>
        <div class="code-control"><code>justify-content: <b>{{ layoutAnswer }}</b>;</code><div><button v-for="item in layoutOptions" :key="item.value" type="button" :class="{ selected: layoutAnswer === item.value }" @click="selectLayout(item.value)">{{ item.label }}</button></div></div>
        <div class="game-actions"><p :class="{ success: layoutSolved }">{{ layoutFeedback || '选一个属性值，看看方块会怎么移动。' }}</p><button v-if="!layoutSolved" type="button" :disabled="savingChallenge" @click="checkLayout">{{ savingChallenge ? '正在保存…' : '检查队形' }}</button><button v-else type="button" @click="nextLayout">下一关 →</button></div>
      </section>

      <section v-else-if="activeGame === 'repair'" class="active-game glass-card">
        <div class="game-topline"><div><span>CSS 急诊室 · 第 {{ repairLevel + 1 }} / {{ repairChallenges.length }} 关</span><h3>样式修理铺</h3></div><em>每题 +120 XP</em></div>
        <div class="repair-scene">
          <div class="broken-phone"><span>320px 手机屏幕</span><div :class="{ fixed: repairSolved, photo: repairLevel === 1, buttons: repairLevel === 2 }"><template v-if="repairLevel === 0"><strong>我的作品卡片</strong><p>修好后，我就不会冲出屏幕了。</p></template><template v-else-if="repairLevel === 1"><i>IMG</i><small>{{ repairSolved ? '比例恢复正常' : '照片变形了' }}</small></template><template v-else><button>首页</button><button>作品</button><button>关于</button><button>联系</button></template></div></div>
          <div class="repair-brief"><span>故障报告</span><h4>{{ repairTarget.title }}</h4><p>{{ repairTarget.clue }}</p><code>{{ repairTarget.broken }}</code></div>
        </div>
        <div class="repair-options"><span>选择一条修复代码</span><button v-for="option in repairTarget.options" :key="option" type="button" :class="{ selected: repairChoice === option, correct: repairSolved && option === repairTarget.correct }" @click="repairChoice = option; repairFeedback = ''; repairSolved = false"><code>{{ option }}</code></button></div>
        <div class="game-actions"><p :class="{ success: repairSolved }">{{ repairFeedback || '根据故障原因选择，不要只凭代码看起来熟悉。' }}</p><button v-if="!repairSolved" type="button" :disabled="!repairChoice || savingChallenge" @click="checkRepair">{{ savingChallenge ? '正在保存…' : '运行修复' }}</button><button v-else type="button" @click="nextRepair">下一张工单 →</button></div>
      </section>

      <section v-else-if="activeGame === 'circuit'" class="active-game glass-card">
        <div class="game-topline"><div><span>JavaScript 电路台 · 第 {{ circuitLevel + 1 }} / {{ circuitChallenges.length }} 关</span><h3>按钮机关屋</h3></div><em>每题 +150 XP</em></div>
        <div class="mission-card"><span>故障现象</span><strong>{{ circuitTarget.title }}</strong><p>从三段代码中选一段，放进空缺线路。</p></div>
        <div class="circuit-board" :class="{ online: circuitSolved }"><div class="circuit-node"><span>用户</span><strong>CLICK</strong></div><i>→</i><div class="circuit-node missing"><span>缺少代码</span><strong>{{ circuitSolved ? 'CONNECTED' : '???' }}</strong></div><i>→</i><div class="circuit-node"><span>页面</span><strong>{{ circuitSolved ? 'UPDATED' : 'WAITING' }}</strong></div></div>
        <pre class="circuit-code"><code><span>{{ circuitTarget.before }}</span><b>{{ circuitChoice || '// 把正确代码接在这里' }}</b><span v-if="circuitTarget.after">  {{ circuitTarget.after }}</span><span v-if="circuitTarget.closing">{{ circuitTarget.closing }}</span></code></pre>
        <div class="repair-options circuit-options"><span>选择缺失线路</span><button v-for="option in circuitTarget.options" :key="option" type="button" :class="{ selected: circuitChoice === option, correct: circuitSolved && option === circuitTarget.correct }" @click="circuitChoice = option; circuitFeedback = ''; circuitSolved = false"><code>{{ option }}</code></button></div>
        <div class="game-actions"><p :class="{ success: circuitSolved }">{{ circuitFeedback || '想一想：浏览器用哪个方法监听事件或改变状态？' }}</p><button v-if="!circuitSolved" type="button" :disabled="!circuitChoice || savingChallenge" @click="checkCircuit">{{ savingChallenge ? '正在保存…' : '接通机关' }}</button><button v-else type="button" @click="nextCircuit">下一间机关屋 →</button></div>
      </section>

      <section v-else class="active-game quiz-game glass-card">
        <div class="game-topline"><div><span>课程知识站 · 第 {{ quizLevel + 1 }} / {{ courseQuizChallenges.length }} 关</span><h3>课程闪答</h3></div><em>每题 +100 XP</em></div>
        <div class="quiz-course-banner"><span>{{ quizTarget.icon }}</span><div><small>本题来自</small><strong>{{ quizTarget.course }}</strong></div><i>{{ String(quizLevel + 1).padStart(2, '0') }}</i></div>
        <div class="quiz-question"><span>遇到这个项目情境，你会怎么选？</span><h4>{{ quizTarget.question }}</h4></div>
        <div class="quiz-options"><button v-for="(option, index) in quizTarget.options" :key="option" type="button" :class="{ selected: quizChoice === option, correct: quizSolved && option === quizTarget.correct }" @click="quizChoice = option; quizFeedback = ''; quizSolved = false"><span>{{ String.fromCharCode(65 + index) }}</span><strong>{{ option }}</strong></button></div>
        <div class="game-actions quiz-actions"><p :class="{ success: quizSolved }">{{ quizFeedback || '先联系课程中的真实用途，再选择答案。' }}</p><button v-if="!quizSolved" type="button" :disabled="!quizChoice || savingChallenge" @click="checkQuiz">{{ savingChallenge ? '正在保存…' : '提交答案' }}</button><button v-else type="button" @click="nextQuiz">下一门课程 →</button></div>
      </section>

      <aside class="game-library">
        <button v-for="game in gameMeta" :key="game.id" type="button" class="game-select-card glass-card" :class="{ selected: activeGame === game.id }" @click="activeGame = game.id"><span class="game-icon">{{ game.icon }}</span><div><small>{{ activeGame === game.id ? '正在玩' : '点击进入' }}</small><strong>{{ game.title }}</strong><p>{{ game.description }}</p></div><i>→</i></button>
        <RouterLink class="classic-practice-link glass-card" to="/practice"><span>◎</span><div><small>经典模式</small><strong>知识选择题</strong><p>复习网页、Vue、后端与计算机基础</p></div><i>→</i></RouterLink>
      </aside>
    </div>
  </section>
</template>
