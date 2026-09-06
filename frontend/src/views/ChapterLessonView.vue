<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, type ChapterLesson } from '../services/api'
import KnowledgePointDiagram from '../components/KnowledgePointDiagram.vue'
import { chapterChecks } from '../content/chapterChecks'
import ChapterTutorial from '../components/ChapterTutorial.vue'
import { chapterTutorials } from '../content/chapterTutorials'
import { networkFoundations } from '../content/networkFoundations'
import { databaseTutorials } from '../content/databaseTutorials'

const route = useRoute()
const router = useRouter()
const lesson = ref<ChapterLesson | null>(null)
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const check = computed(() => lesson.value ? chapterChecks[lesson.value.chapterTitle] : undefined)
const tutorial = computed(() => lesson.value ? chapterTutorials[lesson.value.chapterTitle] ?? networkFoundations[lesson.value.chapterTitle] ?? databaseTutorials[lesson.value.chapterTitle] : undefined)

async function loadLesson() {
  loading.value = true
  error.value = ''
  try {
    lesson.value = await api<ChapterLesson>(`/api/courses/${route.params.courseId}/chapters/${route.params.chapterId}`)
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '章节内容加载失败'
  } finally {
    loading.value = false
  }
}

async function completeLesson() {
  if (!lesson.value || lesson.value.completed || saving.value) return
  saving.value = true
  error.value = ''
  try {
    await api(`/api/courses/${lesson.value.courseId}/progress`, {
      method: 'POST',
      body: JSON.stringify({ completedLessons: lesson.value.orderIndex }),
    })
    if (lesson.value.nextChapterId) {
      await router.push(`/courses/${lesson.value.courseId}/chapters/${lesson.value.nextChapterId}`)
    } else {
      await loadLesson()
    }
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '学习进度保存失败'
  } finally {
    saving.value = false
  }
}

function goToChapter(chapterId: number | null) {
  if (chapterId && lesson.value) router.push(`/courses/${lesson.value.courseId}/chapters/${chapterId}`)
}

function printDocument() {
  window.print()
}

onMounted(loadLesson)
watch(() => route.params.chapterId, loadLesson)
</script>

<template>
  <section class="lesson-page" :class="{ 'has-tutorial': tutorial }">
    <RouterLink class="back-link" :to="`/courses/${route.params.courseId}`">← 返回知识目录</RouterLink>
    <div v-if="loading" class="state-card glass-card"><span class="loader"></span><p>正在打开章节正文…</p></div>
    <div v-else-if="error && !lesson" class="state-card glass-card"><strong>章节暂时无法打开</strong><p>{{ error }}</p><button @click="loadLesson">重新加载</button></div>
    <template v-else-if="lesson">
      <header class="lesson-hero glass-card">
        <div><span>第 {{ String(lesson.orderIndex).padStart(2, '0') }} 章 · {{ lesson.courseTitle }}</span><h2>{{ lesson.chapterTitle }}</h2><p>{{ lesson.beginnerIntro }}</p></div>
        <aside><strong>{{ lesson.durationMinutes }}</strong><span>预计分钟</span><em :class="{ done: lesson.completed }">{{ lesson.completed ? '✓ 已完成' : '学习中' }}</em></aside>
      </header>

      <div class="lesson-grid">
        <main class="lesson-content">
          <ChapterTutorial v-if="tutorial" :key="lesson.chapterTitle" :tutorial="tutorial" :chapter-title="lesson.chapterTitle" />
          <template v-else>
          <section class="glass-card lesson-section beginner-section">
            <div class="beginner-badge"><i></i><span>零基础学习模式</span><small>不需要提前懂术语，按顺序往下学</small></div>
            <div class="lesson-heading"><span>01</span><div><small>START HERE</small><h3>从熟悉的场景开始</h3></div></div>
            <div class="beginner-intro"><span>一句话说明</span><p>{{ lesson.beginnerIntro }}</p></div>
            <div class="analogy-card"><span>💡</span><div><small>生活类比</small><p>{{ lesson.beginnerAnalogy }}</p></div></div>
          </section>

          <section class="glass-card lesson-section analysis-section">
            <div class="document-toolbar"><div class="lesson-heading"><span>02</span><div><small>LEARN ONE BY ONE</small><h3>对照图片，逐个理解知识点</h3></div></div><button type="button" @click="printDocument">打印 / 保存 PDF</button></div>
            <p class="section-intro">先认清图中物体对应什么，再跟着具体数据或操作推演。图下注释解释场景，例子讲清过程与原因，最后用准确结论核对理解。图片可点击放大。</p>
            <div class="analysis-list">
              <article v-for="(item, index) in lesson.knowledgeAnalyses" :key="item.id" class="analysis-card">
                <header><span>{{ String(index + 1).padStart(2, '0') }}</span><div><small>{{ item.category }}</small><h4>{{ item.title }}</h4></div></header>
                <KnowledgePointDiagram :chapter-title="lesson.chapterTitle" :course-title="lesson.courseTitle" :point-index="index" :title="item.title" />
                <div class="knowledge-definition"><small>回到知识点：核对准确结论</small><p>{{ item.conclusion }}</p></div>
              </article>
            </div>
          </section>

          </template>
          <section v-if="check" class="glass-card lesson-section review-section">
            <div class="lesson-heading"><span>03</span><div><small>CHECK YOURSELF</small><h3>换个例子，检查是否真的理解</h3></div></div>
            <div class="chapter-check"><p>{{ check[0] }}</p><small>先自己作答，再展开解析核对结果与原因。</small><details :key="lesson.chapterTitle"><summary>展开答案与解析</summary><p>{{ check[1] }}</p></details></div>
          </section>

          <section class="glass-card lesson-section practice-task">
            <div class="lesson-heading"><span>04</span><div><small>HANDS-ON TASK</small><h3>独立练习：做出一个可检查的结果</h3></div></div>
            <p>{{ lesson.practiceTask }}</p>
            <div class="task-acceptance"><div><b>1</b><span>先写输入与目标</span></div><div><b>2</b><span>记录每一步变化</span></div><div><b>3</b><span>展示最终结果</span></div><div><b>4</b><span>补一个错误或边界案例</span></div></div>
            <div class="task-tip"><span>验收标准</span><p>能够独立完成任务，并用自己的话解释关键步骤与结果；遇到错误时记录原因和修正方法。{{ tutorial ? '完成练习并核对解析后再标记本章。' : '四项都完成后再标记本章。' }}</p></div>
          </section>
        </main>
      </div>

      <footer class="glass-card lesson-finish">
        <div class="lesson-finish-copy">
          <span class="eyebrow"><i></i> 学习检查点</span>
          <h3>{{ lesson.completed ? '本节已掌握' : '完成阅读与练习' }}</h3>
          <p>正文、知识要点和动手任务都完成后，再标记本节，形成真实学习闭环。</p>
        </div>
        <div class="lesson-finish-actions">
          <button class="lesson-complete-button" :disabled="saving || lesson.completed" @click="completeLesson">{{ saving ? '正在保存…' : lesson.completed ? '本节已完成' : lesson.nextChapterId ? '完成并学习下一章 →' : '完成本课程' }}</button>
          <nav class="lesson-navigation" aria-label="章节导航"><button :disabled="!lesson.previousChapterId" @click="goToChapter(lesson.previousChapterId)">← 上一章</button><button :disabled="!lesson.nextChapterId" @click="goToChapter(lesson.nextChapterId)">下一章 →</button></nav>
          <small v-if="error">{{ error }}</small>
        </div>
      </footer>
    </template>
  </section>
</template>

<style scoped>
.has-tutorial .task-acceptance { display: none; }
@media print { .has-tutorial .lesson-grid { display: block; } }
.chapter-check > p { font-size: 17px; line-height: 1.9; color: #e3eafa; }
.chapter-check > small { color: #a6b7d8; }
.chapter-check details { margin-top: 22px; border-top: 1px solid rgba(136,226,204,.2); padding-top: 16px; }
.chapter-check summary { color: #92e1ce; cursor: pointer; width: fit-content; padding: 6px 0; }
.chapter-check summary:focus-visible { outline: 2px solid #92e1ce; outline-offset: 5px; }
.chapter-check details p { margin: 15px 0 0; color: #dce6fa; font-size: 16px; line-height: 2; }
@media print { .chapter-check > p, .chapter-check > small, .chapter-check details p { color: #222; } }
</style>
