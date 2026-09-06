<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { api, type CourseDetail } from '../services/api'

const route = useRoute()
const course = ref<CourseDetail | null>(null)
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const resourceType = ref('全部')
const nextChapter = computed(() => course.value?.chapters.find((chapter) => !chapter.completed))
const resourceTypes = computed(() => ['全部', ...new Set(course.value?.resources.map((resource) => resource.resourceType) ?? [])])
const filteredResources = computed(() => resourceType.value === '全部'
  ? course.value?.resources ?? []
  : course.value?.resources.filter((resource) => resource.resourceType === resourceType.value) ?? [])

async function loadCourse() {
  loading.value = true; error.value = ''
  try { course.value = await api<CourseDetail>(`/api/courses/${route.params.id}`); resourceType.value = '全部' }
  catch (cause) { error.value = cause instanceof Error ? cause.message : '课程加载失败' }
  finally { loading.value = false }
}
async function completeNext() {
  if (!course.value || saving.value || course.value.completedLessons >= course.value.totalLessons) return
  saving.value = true; error.value = ''
  try {
    await api(`/api/courses/${course.value.id}/progress`, { method: 'POST', body: JSON.stringify({ completedLessons: course.value.completedLessons + 1 }) })
    await loadCourse()
  } catch (cause) { error.value = cause instanceof Error ? cause.message : '进度保存失败' }
  finally { saving.value = false }
}
onMounted(loadCourse)
watch(() => route.params.id, loadCourse)
</script>

<template>
  <section class="detail-page">
    <RouterLink class="back-link" to="/knowledge">← 返回知识工具箱</RouterLink>
    <div v-if="loading" class="state-card glass-card"><span class="loader"></span><p>正在展开课程地图…</p></div>
    <div v-else-if="error && !course" class="state-card glass-card"><strong>课程暂时无法打开</strong><p>{{ error }}</p><button @click="loadCourse">重新加载</button></div>
    <template v-else-if="course">
      <section class="detail-hero glass-card" :style="{ '--course-accent': course.accent }"><div class="detail-icon">{{ course.icon }}</div><div class="detail-copy"><span>{{ course.category }} · {{ course.difficulty }}</span><h2>{{ course.title }}</h2><p>{{ course.description }}</p><div><span>讲师 {{ course.teacherName }}</span><span>◷ {{ Math.floor(course.durationMinutes / 60) }} 小时 {{ course.durationMinutes % 60 }} 分</span><span>▤ {{ course.totalLessons }} 课时</span></div></div><div class="detail-progress" :style="{ '--progress': `${course.progressPercent * 3.6}deg` }"><div><strong>{{ course.progressPercent }}%</strong><small>已完成</small></div></div></section>
      <div class="detail-columns">
        <div class="detail-content-stack">
          <section class="chapter-panel glass-card"><div class="panel-title"><div><span class="mini-icon blue">▤</span><h3>课程章节</h3></div><span>{{ course.completedLessons }}/{{ course.totalLessons }} 已完成</span></div><div class="chapter-list"><RouterLink v-for="chapter in course.chapters" :key="chapter.id" :to="`/courses/${course.id}/chapters/${chapter.id}`" :class="{ completed: chapter.completed, current: nextChapter?.id === chapter.id }"><span class="chapter-index">{{ chapter.completed ? '✓' : String(chapter.orderIndex).padStart(2, '0') }}</span><div><strong>{{ chapter.title }}</strong><small>{{ nextChapter?.id === chapter.id ? '继续学习 · 点击查看正文' : `约 ${chapter.durationMinutes} 分钟 · 点击查看正文` }}</small></div><span class="chapter-state">{{ chapter.completed ? '已完成' : nextChapter?.id === chapter.id ? '进行中' : '学习 →' }}</span></RouterLink></div></section>
          <section class="resource-panel glass-card">
            <div class="panel-title"><div><span class="mini-icon cyan">↗</span><h3>课程学习资源库</h3></div><span>{{ course.resources.length }} 项权威资料</span></div>
            <div class="learning-path"><span>推荐学习法</span><p><b>01</b> 基础阅读 <i>→</i><b>02</b> 跟随教程 <i>→</i><b>03</b> 动手练习 <i>→</i><b>04</b> 项目复盘</p><small>不要只收藏链接：每学完一项，至少完成一次笔记、练习或可运行作品。</small></div>
            <div class="resource-tabs"><button v-for="type in resourceTypes" :key="type" type="button" :class="{ active: resourceType === type }" @click="resourceType = type">{{ type }}</button></div>
            <div class="resource-list"><a v-for="(resource, index) in filteredResources" :key="resource.id" :href="resource.url" target="_blank" rel="noopener noreferrer"><span class="resource-step">{{ String(index + 1).padStart(2, '0') }}</span><div><span class="resource-meta"><em>{{ resource.resourceType }}</em>{{ resource.provider }}</span><strong>{{ resource.title }}</strong><p>{{ resource.description }}</p></div><i>↗</i></a></div>
          </section>
        </div>
        <aside class="learning-card glass-card"><span class="eyebrow"><i></i> 下一步</span><h3>{{ nextChapter?.title ?? '课程已全部完成' }}</h3><p>{{ nextChapter ? '准备好后完成下一节，学习进度会实时保存。' : '太棒了，你已经点亮这门课程的全部章节。' }}</p><button :disabled="saving || !nextChapter" @click="completeNext">{{ saving ? '正在保存…' : nextChapter ? '完成下一节 →' : '已完成全部课程' }}</button><small v-if="error">{{ error }}</small></aside>
      </div>
    </template>
  </section>
</template>
