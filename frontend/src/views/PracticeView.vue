<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { api, type PracticeAnswerResult, type PracticeQuestion, type PracticeStats } from '../services/api'

const questions = ref<PracticeQuestion[]>([])
const stats = ref<PracticeStats>({ totalAnswered: 0, correctAnswers: 0, accuracyPercent: 0, totalPoints: 0 })
const currentIndex = ref(0)
const selectedOption = ref('')
const result = ref<PracticeAnswerResult | null>(null)
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const sessionCorrect = ref(0)
const finished = ref(false)

const currentQuestion = computed(() => questions.value[currentIndex.value])
const progressPercent = computed(() => questions.value.length
  ? Math.round(((currentIndex.value + (result.value ? 1 : 0)) / questions.value.length) * 100)
  : 0)

async function loadPractice() {
  loading.value = true
  error.value = ''
  try {
    const [questionData, statsData] = await Promise.all([
      api<PracticeQuestion[]>('/api/practice/questions'),
      api<PracticeStats>('/api/practice/stats'),
    ])
    questions.value = questionData
    stats.value = statsData
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '练习题加载失败'
  } finally {
    loading.value = false
  }
}

async function submitAnswer() {
  if (!currentQuestion.value || !selectedOption.value || submitting.value || result.value) return
  submitting.value = true
  error.value = ''
  try {
    result.value = await api<PracticeAnswerResult>(`/api/practice/questions/${currentQuestion.value.id}/submit`, {
      method: 'POST',
      body: JSON.stringify({ selectedOption: selectedOption.value }),
    })
    stats.value = result.value.stats
    if (result.value.correct) sessionCorrect.value += 1
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '答案提交失败'
  } finally {
    submitting.value = false
  }
}

function nextQuestion() {
  if (currentIndex.value >= questions.value.length - 1) {
    finished.value = true
    return
  }
  currentIndex.value += 1
  selectedOption.value = ''
  result.value = null
  error.value = ''
}

function restart() {
  currentIndex.value = 0
  selectedOption.value = ''
  result.value = null
  sessionCorrect.value = 0
  finished.value = false
  error.value = ''
}

function optionState(key: string) {
  if (!result.value) return { selected: selectedOption.value === key }
  return {
    selected: selectedOption.value === key,
    correct: result.value.correctOption === key,
    wrong: selectedOption.value === key && !result.value.correct,
  }
}

onMounted(loadPractice)
</script>

<template>
  <section class="practice-page">
    <div class="practice-hero glass-card">
      <div>
        <span class="eyebrow"><i></i> FOCUS TRAINING</span>
        <h2>在线练习舱</h2>
        <p>即时判分、逐题解析，让每一次作答都成为清晰可见的进步。</p>
      </div>
      <div class="practice-stats">
        <div><strong>{{ stats.totalAnswered }}</strong><span>累计答题</span></div>
        <div><strong>{{ stats.accuracyPercent }}%</strong><span>正确率</span></div>
        <div><strong>{{ stats.totalPoints }}</strong><span>获得积分</span></div>
      </div>
    </div>

    <div v-if="loading" class="state-card glass-card"><span class="loader"></span><p>正在准备今日练习…</p></div>
    <div v-else-if="error && !questions.length" class="state-card glass-card"><strong>练习舱暂时无法启动</strong><p>{{ error }}</p><button @click="loadPractice">重新加载</button></div>
    <div v-else-if="!questions.length" class="state-card glass-card"><strong>题库正在补充中</strong><p>稍后再来看看吧。</p></div>

    <section v-else-if="finished" class="practice-summary glass-card">
      <div class="summary-orbit"><strong>{{ sessionCorrect }}/{{ questions.length }}</strong><span>本轮答对</span></div>
      <span class="eyebrow"><i></i> TRAINING COMPLETE</span>
      <h3>本轮练习完成</h3>
      <p>{{ sessionCorrect === questions.length ? '全对！你的知识网络非常稳固。' : '解析已经记录，趁热再来一轮巩固薄弱点吧。' }}</p>
      <button @click="restart">重新练习</button>
    </section>

    <div v-else class="practice-layout">
      <section class="question-card glass-card">
        <div class="question-progress">
          <div><span>练习进度</span><strong>{{ currentIndex + 1 }} / {{ questions.length }}</strong></div>
          <div class="progress-track"><i :style="{ width: `${progressPercent}%` }"></i></div>
        </div>
        <div class="question-meta">
          <span>{{ currentQuestion.subject }}</span>
          <span>{{ currentQuestion.difficulty }}</span>
          <em>+{{ currentQuestion.points }} XP</em>
        </div>
        <h3>{{ currentQuestion.prompt }}</h3>
        <div class="option-list">
          <button
            v-for="option in currentQuestion.options"
            :key="option.key"
            type="button"
            :disabled="Boolean(result)"
            :class="optionState(option.key)"
            @click="selectedOption = option.key"
          >
            <span>{{ option.key }}</span><strong>{{ option.text }}</strong><i>{{ result?.correctOption === option.key ? '✓' : selectedOption === option.key ? '●' : '' }}</i>
          </button>
        </div>

        <div v-if="result" class="answer-feedback" :class="result.correct ? 'success' : 'error'">
          <div><span>{{ result.correct ? '✓' : '!' }}</span><strong>{{ result.correct ? `回答正确，获得 ${result.pointsEarned} XP` : `回答错误，正确答案是 ${result.correctOption}` }}</strong></div>
          <p>{{ result.explanation }}</p>
        </div>
        <p v-else-if="error" class="practice-error">{{ error }}</p>

        <div class="question-actions">
          <small>{{ result ? '理解解析后继续下一题' : '选择你认为最准确的答案' }}</small>
          <button v-if="!result" :disabled="!selectedOption || submitting" @click="submitAnswer">{{ submitting ? '正在判分…' : '提交答案' }}</button>
          <button v-else @click="nextQuestion">{{ currentIndex === questions.length - 1 ? '查看结果' : '下一题' }} →</button>
        </div>
      </section>

      <aside class="practice-side">
        <section class="glass-card"><span class="side-icon">◎</span><div><small>本轮表现</small><strong>{{ sessionCorrect }} 题正确</strong><p>已完成 {{ currentIndex + (result ? 1 : 0) }} 题</p></div></section>
        <section class="glass-card practice-tip"><span>答题提示</span><p>先排除明显错误选项，再比较剩余选项和题干中的关键词。</p></section>
        <div class="question-map">
          <button v-for="(_, index) in questions" :key="index" :class="{ current: index === currentIndex, passed: index < currentIndex || (index === currentIndex && result) }" disabled>{{ index + 1 }}</button>
        </div>
      </aside>
    </div>
  </section>
</template>
