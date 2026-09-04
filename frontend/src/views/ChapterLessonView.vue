<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, type ChapterLesson } from '../services/api'

const route = useRoute()
const router = useRouter()
const lesson = ref<ChapterLesson | null>(null)
const loading = ref(true)
const saving = ref(false)
const error = ref('')

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
  <section class="lesson-page">
    <RouterLink class="back-link" :to="`/courses/${route.params.courseId}`">← 返回课程目录</RouterLink>
    <div v-if="loading" class="state-card glass-card"><span class="loader"></span><p>正在打开章节正文…</p></div>
    <div v-else-if="error && !lesson" class="state-card glass-card"><strong>章节暂时无法打开</strong><p>{{ error }}</p><button @click="loadLesson">重新加载</button></div>
    <template v-else-if="lesson">
      <header class="lesson-hero glass-card">
        <div><span>第 {{ String(lesson.orderIndex).padStart(2, '0') }} 章 · {{ lesson.courseTitle }}</span><h2>{{ lesson.chapterTitle }}</h2><p>{{ lesson.overview }}</p></div>
        <aside><strong>{{ lesson.durationMinutes }}</strong><span>预计分钟</span><em :class="{ done: lesson.completed }">{{ lesson.completed ? '✓ 已完成' : '学习中' }}</em></aside>
      </header>

      <div class="lesson-grid">
        <main class="lesson-content">
          <section class="glass-card lesson-section beginner-section">
            <div class="beginner-badge"><i></i><span>零基础学习模式</span><small>不需要提前懂术语，按顺序往下学</small></div>
            <div class="lesson-heading"><span>01</span><div><small>START HERE</small><h3>先用人话弄懂这节课</h3></div></div>
            <div class="beginner-intro"><span>一句话说明</span><p>{{ lesson.beginnerIntro }}</p></div>
            <div class="analogy-card"><span>💡</span><div><small>生活类比</small><p>{{ lesson.beginnerAnalogy }}</p></div></div>
          </section>

          <section class="glass-card lesson-section walkthrough-section">
            <div class="lesson-heading"><span>02</span><div><small>FOLLOW ME</small><h3>跟着示例一步一步做</h3></div></div>
            <p class="section-intro">先照着做，不要求第一次就背住。每完成一步，再读下面的“为什么”。</p>
            <ol class="walkthrough-list"><li v-for="(step, index) in lesson.beginnerWalkthrough" :key="step"><span>{{ index + 1 }}</span><div><small>第 {{ index + 1 }} 步</small><p>{{ step }}</p></div></li></ol>
          </section>

          <section class="glass-card lesson-section knowledge-graph-section">
            <div class="lesson-heading"><span>03</span><div><small>KNOWLEDGE GRAPH</small><h3>知识关系图</h3></div></div>
            <div class="knowledge-graph">
              <article class="graph-root"><small>{{ lesson.knowledgeNodes[0].category }}</small><strong>{{ lesson.knowledgeNodes[0].label }}</strong></article>
              <div class="graph-branches">
                <article v-for="(node, index) in lesson.knowledgeNodes.slice(1, -1)" :key="node.id" class="graph-node">
                  <span>{{ lesson.knowledgeEdges[index]?.relation }}</span><small>{{ node.category }}</small><strong>{{ node.label }}</strong>
                </article>
              </div>
              <article class="graph-practice"><span>验证</span><small>{{ lesson.knowledgeNodes.at(-1)?.category }}</small><strong>{{ lesson.knowledgeNodes.at(-1)?.label }}</strong></article>
            </div>
          </section>

          <section class="glass-card lesson-section analysis-section">
            <div class="document-toolbar"><div class="lesson-heading"><span>04</span><div><small>DETAILED EXPLANATION</small><h3>知识点逐条解析</h3></div></div><button type="button" @click="printDocument">打印 / 保存 PDF</button></div>
            <div class="analysis-list">
              <article v-for="(item, index) in lesson.knowledgeAnalyses" :key="item.id" class="analysis-card">
                <header><span>{{ String(index + 1).padStart(2, '0') }}</span><div><small>{{ item.category }}</small><h4>{{ item.title }}</h4></div></header>
                <p>{{ item.analysis }}</p>
                <div class="mini-diagram" :aria-label="`${item.title}图解`">
                  <template v-for="(node, nodeIndex) in item.diagramNodes" :key="`${item.id}-${nodeIndex}`"><span>{{ node }}</span><i v-if="nodeIndex < item.diagramNodes.length - 1">→</i></template>
                </div>
              </article>
            </div>
          </section>

          <section class="glass-card lesson-section practice-task">
            <div class="lesson-heading"><span>05</span><div><small>HANDS-ON TASK</small><h3>独立练习</h3></div></div>
            <p>{{ lesson.practiceTask }}</p>
            <div class="task-tip"><span>验收标准</span><p>能够独立完成任务，并用自己的话解释关键步骤与结果；遇到错误时记录原因和修正方法。</p></div>
          </section>
        </main>

        <aside class="lesson-side">
          <section class="glass-card lesson-checkpoint"><span class="eyebrow"><i></i> 学习检查点</span><h3>{{ lesson.completed ? '本节已掌握' : '完成阅读与练习' }}</h3><p>正文、知识要点和动手任务都完成后，再标记本节，形成真实学习闭环。</p><button :disabled="saving || lesson.completed" @click="completeLesson">{{ saving ? '正在保存…' : lesson.completed ? '本节已完成' : lesson.nextChapterId ? '完成并学习下一章 →' : '完成本课程' }}</button><small v-if="error">{{ error }}</small></section>
          <nav class="glass-card lesson-navigation" aria-label="章节导航"><button :disabled="!lesson.previousChapterId" @click="goToChapter(lesson.previousChapterId)">← 上一章</button><button :disabled="!lesson.nextChapterId" @click="goToChapter(lesson.nextChapterId)">下一章 →</button></nav>
        </aside>
      </div>
    </template>
  </section>
</template>
