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
          <section class="glass-card lesson-section">
            <div class="lesson-heading"><span>01</span><div><small>LEARNING GOALS</small><h3>本节学习目标</h3></div></div>
            <ul class="objective-list"><li v-for="objective in lesson.objectives" :key="objective"><i>✓</i><span>{{ objective }}</span></li></ul>
          </section>

          <section class="glass-card lesson-section knowledge-graph-section">
            <div class="lesson-heading"><span>02</span><div><small>KNOWLEDGE GRAPH</small><h3>章节知识图谱</h3></div></div>
            <p class="section-intro">从核心主题出发，沿着概念、原理、应用与实践关系建立完整知识网络，每个节点都附有详细说明。</p>
            <div class="knowledge-graph">
              <article class="graph-root"><small>{{ lesson.knowledgeNodes[0].category }}</small><strong>{{ lesson.knowledgeNodes[0].label }}</strong><p>{{ lesson.knowledgeNodes[0].description }}</p></article>
              <div class="graph-branches">
                <article v-for="(node, index) in lesson.knowledgeNodes.slice(1, -1)" :key="node.id" class="graph-node">
                  <span>{{ lesson.knowledgeEdges[index]?.relation }}</span><small>{{ node.category }}</small><strong>{{ node.label }}</strong><p>{{ node.description }}</p>
                </article>
              </div>
              <article class="graph-practice"><span>验证</span><small>{{ lesson.knowledgeNodes.at(-1)?.category }}</small><strong>{{ lesson.knowledgeNodes.at(-1)?.label }}</strong><p>{{ lesson.knowledgeNodes.at(-1)?.description }}</p></article>
            </div>
          </section>

          <section class="glass-card lesson-section study-document">
            <div class="document-toolbar"><div class="lesson-heading"><span>03</span><div><small>STUDY DOCUMENT</small><h3>章节精读文档</h3></div></div><button type="button" @click="printDocument">打印 / 保存 PDF</button></div>
            <p class="section-intro">按“概念—原理—应用—复盘”的顺序精读。每完成一部分，用自己的例子复述一次。</p>
            <article v-for="section in lesson.studySections" :key="section.title" class="document-section"><h4>{{ section.title }}</h4><p>{{ section.summary }}</p><ul><li v-for="point in section.points" :key="point">{{ point }}</li></ul></article>
            <div class="self-check"><span>章末自测</span><ol><li v-for="question in lesson.selfCheckQuestions" :key="question">{{ question }}</li></ol></div>
          </section>

          <section class="glass-card lesson-section practice-task">
            <div class="lesson-heading"><span>04</span><div><small>HANDS-ON TASK</small><h3>动手练习</h3></div></div>
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
