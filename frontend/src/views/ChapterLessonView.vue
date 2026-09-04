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

          <section class="glass-card lesson-section">
            <div class="lesson-heading"><span>02</span><div><small>CORE NOTES</small><h3>核心知识讲解</h3></div></div>
            <div class="knowledge-list"><article v-for="(point, index) in lesson.keyPoints" :key="point"><b>{{ String(index + 1).padStart(2, '0') }}</b><p>{{ point }}</p></article></div>
          </section>

          <section class="glass-card lesson-section practice-task">
            <div class="lesson-heading"><span>03</span><div><small>HANDS-ON TASK</small><h3>动手练习</h3></div></div>
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
