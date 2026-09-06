<script setup lang="ts">
import { computed, ref } from 'vue'

type GameId = 'layout' | 'repair' | 'circuit'
type Alignment = 'flex-start' | 'center' | 'space-between' | 'flex-end'

const activeGame = ref<GameId>('layout')
const score = ref(0)
const awardedChallenges = new Set<string>()

function awardOnce(challenge: string, points: number) {
  if (awardedChallenges.has(challenge)) return
  awardedChallenges.add(challenge)
  score.value += points
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

const gameMeta = [
  { id: 'layout' as GameId, icon: '▦', title: '布局拼拼乐', description: '用 Flex 把元素送到正确位置' },
  { id: 'repair' as GameId, icon: '⌁', title: '样式修理铺', description: '找出让页面变形的 CSS' },
  { id: 'circuit' as GameId, icon: '⚡', title: '按钮机关屋', description: '补上代码，让交互重新工作' },
]

function selectLayout(value: Alignment) {
  layoutAnswer.value = value
  layoutFeedback.value = ''
  layoutSolved.value = false
}

function checkLayout() {
  if (layoutAnswer.value === layoutTarget.value.value) {
    layoutFeedback.value = '摆对了！justify-content 控制主轴上的排列方式。'
    awardOnce(`layout-${layoutLevel.value}`, 100)
    layoutSolved.value = true
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

function checkRepair() {
  if (!repairChoice.value) return
  if (repairChoice.value === repairTarget.value.correct) {
    repairFeedback.value = repairTarget.value.success
    awardOnce(`repair-${repairLevel.value}`, 120)
    repairSolved.value = true
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

function checkCircuit() {
  if (!circuitChoice.value) return
  if (circuitChoice.value === circuitTarget.value.correct) {
    circuitFeedback.value = circuitTarget.value.success
    awardOnce(`circuit-${circuitLevel.value}`, 150)
    circuitSolved.value = true
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
</script>

<template>
  <section class="games-page">
    <header class="journey-heading"><div><span class="eyebrow"><i></i> PLAY AND LEARN</span><h2>趣味闯关</h2><p>用几分钟的小挑战，把抽象知识变成手上的感觉。</p></div><div class="game-score"><small>本次得分</small><strong>{{ score }}</strong><span>XP</span></div></header>

    <div class="game-layout">
      <section v-if="activeGame === 'layout'" class="active-game glass-card">
        <div class="game-topline"><div><span>CSS 训练场 · 第 {{ layoutLevel + 1 }} / {{ layoutTargets.length }} 关</span><h3>布局拼拼乐</h3></div><em>约 3 分钟</em></div>
        <div class="mission-card"><span>本关任务</span><strong>{{ layoutTarget.label }}</strong><p>{{ layoutTarget.hint }}</p></div>
        <div class="layout-arenas"><div><span>目标队形</span><div class="layout-arena target" :style="{ justifyContent: layoutTarget.value }"><i>A</i><i>B</i><i>C</i></div></div><div><span>你的队形</span><div class="layout-arena player" :class="{ correct: layoutSolved }" :style="{ justifyContent: layoutAnswer }"><i>A</i><i>B</i><i>C</i></div></div></div>
        <div class="code-control"><code>justify-content: <b>{{ layoutAnswer }}</b>;</code><div><button v-for="item in layoutOptions" :key="item.value" type="button" :class="{ selected: layoutAnswer === item.value }" @click="selectLayout(item.value)">{{ item.label }}</button></div></div>
        <div class="game-actions"><p :class="{ success: layoutSolved }">{{ layoutFeedback || '选一个属性值，看看方块会怎么移动。' }}</p><button v-if="!layoutSolved" type="button" @click="checkLayout">检查队形</button><button v-else type="button" @click="nextLayout">下一关 →</button></div>
      </section>

      <section v-else-if="activeGame === 'repair'" class="active-game glass-card">
        <div class="game-topline"><div><span>CSS 急诊室 · 第 {{ repairLevel + 1 }} / {{ repairChallenges.length }} 关</span><h3>样式修理铺</h3></div><em>每题 +120 XP</em></div>
        <div class="repair-scene">
          <div class="broken-phone"><span>320px 手机屏幕</span><div :class="{ fixed: repairSolved, photo: repairLevel === 1, buttons: repairLevel === 2 }"><template v-if="repairLevel === 0"><strong>我的作品卡片</strong><p>修好后，我就不会冲出屏幕了。</p></template><template v-else-if="repairLevel === 1"><i>IMG</i><small>{{ repairSolved ? '比例恢复正常' : '照片变形了' }}</small></template><template v-else><button>首页</button><button>作品</button><button>关于</button><button>联系</button></template></div></div>
          <div class="repair-brief"><span>故障报告</span><h4>{{ repairTarget.title }}</h4><p>{{ repairTarget.clue }}</p><code>{{ repairTarget.broken }}</code></div>
        </div>
        <div class="repair-options"><span>选择一条修复代码</span><button v-for="option in repairTarget.options" :key="option" type="button" :class="{ selected: repairChoice === option, correct: repairSolved && option === repairTarget.correct }" @click="repairChoice = option; repairFeedback = ''; repairSolved = false"><code>{{ option }}</code></button></div>
        <div class="game-actions"><p :class="{ success: repairSolved }">{{ repairFeedback || '根据故障原因选择，不要只凭代码看起来熟悉。' }}</p><button v-if="!repairSolved" type="button" :disabled="!repairChoice" @click="checkRepair">运行修复</button><button v-else type="button" @click="nextRepair">下一张工单 →</button></div>
      </section>

      <section v-else class="active-game glass-card">
        <div class="game-topline"><div><span>JavaScript 电路台 · 第 {{ circuitLevel + 1 }} / {{ circuitChallenges.length }} 关</span><h3>按钮机关屋</h3></div><em>每题 +150 XP</em></div>
        <div class="mission-card"><span>故障现象</span><strong>{{ circuitTarget.title }}</strong><p>从三段代码中选一段，放进空缺线路。</p></div>
        <div class="circuit-board" :class="{ online: circuitSolved }"><div class="circuit-node"><span>用户</span><strong>CLICK</strong></div><i>→</i><div class="circuit-node missing"><span>缺少代码</span><strong>{{ circuitSolved ? 'CONNECTED' : '???' }}</strong></div><i>→</i><div class="circuit-node"><span>页面</span><strong>{{ circuitSolved ? 'UPDATED' : 'WAITING' }}</strong></div></div>
        <pre class="circuit-code"><code><span>{{ circuitTarget.before }}</span><b>{{ circuitChoice || '// 把正确代码接在这里' }}</b><span v-if="circuitTarget.after">  {{ circuitTarget.after }}</span><span v-if="circuitTarget.closing">{{ circuitTarget.closing }}</span></code></pre>
        <div class="repair-options circuit-options"><span>选择缺失线路</span><button v-for="option in circuitTarget.options" :key="option" type="button" :class="{ selected: circuitChoice === option, correct: circuitSolved && option === circuitTarget.correct }" @click="circuitChoice = option; circuitFeedback = ''; circuitSolved = false"><code>{{ option }}</code></button></div>
        <div class="game-actions"><p :class="{ success: circuitSolved }">{{ circuitFeedback || '想一想：浏览器用哪个方法监听事件或改变状态？' }}</p><button v-if="!circuitSolved" type="button" :disabled="!circuitChoice" @click="checkCircuit">接通机关</button><button v-else type="button" @click="nextCircuit">下一间机关屋 →</button></div>
      </section>

      <aside class="game-library">
        <button v-for="game in gameMeta" :key="game.id" type="button" class="game-select-card glass-card" :class="{ selected: activeGame === game.id }" @click="activeGame = game.id"><span class="game-icon">{{ game.icon }}</span><div><small>{{ activeGame === game.id ? '正在玩' : '点击进入' }}</small><strong>{{ game.title }}</strong><p>{{ game.description }}</p></div><i>→</i></button>
        <RouterLink class="classic-practice-link glass-card" to="/practice"><span>◎</span><div><small>经典模式</small><strong>知识选择题</strong><p>复习网页、Vue、后端与计算机基础</p></div><i>→</i></RouterLink>
      </aside>
    </div>
  </section>
</template>
