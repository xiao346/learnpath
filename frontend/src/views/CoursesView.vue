<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { api, type CourseSummary } from '../services/api'

const courses = ref<CourseSummary[]>([])
const keyword = ref('')
const category = ref('全部')
const loading = ref(true)
const error = ref('')
const categories = computed(() => ['全部', ...new Set(courses.value.map((course) => course.category))])
const filteredCourses = computed(() => courses.value.filter((course) => {
  const matchesCategory = category.value === '全部' || course.category === category.value
  const text = `${course.title}${course.subtitle}${course.teacherName}`.toLowerCase()
  return matchesCategory && text.includes(keyword.value.trim().toLowerCase())
}))

async function loadCourses() {
  loading.value = true; error.value = ''
  try { courses.value = await api<CourseSummary[]>('/api/courses') }
  catch (cause) { error.value = cause instanceof Error ? cause.message : '课程加载失败' }
  finally { loading.value = false }
}
onMounted(loadCourses)
</script>

<template>
  <section class="course-page">
    <div class="course-hero glass-card"><div><span class="eyebrow"><i></i> KNOWLEDGE GALAXY</span><h2>探索你的课程宇宙</h2><p>按自己的节奏前进，每完成一节，都让知识版图亮起一颗星。</p></div><div class="hero-orb"><span>{{ courses.length }}</span><small>门课程</small></div></div>
    <div class="course-toolbar"><label class="search-box"><span>⌕</span><input v-model="keyword" placeholder="搜索课程、教师或关键词" /></label><div class="category-tabs"><button v-for="item in categories" :key="item" :class="{ active: category === item }" @click="category = item">{{ item }}</button></div></div>
    <div v-if="loading" class="state-card glass-card"><span class="loader"></span><p>正在连接知识星图…</p></div>
    <div v-else-if="error" class="state-card glass-card"><strong>课程暂时迷路了</strong><p>{{ error }}</p><button @click="loadCourses">重新加载</button></div>
    <div v-else-if="!filteredCourses.length" class="state-card glass-card"><strong>没有找到匹配课程</strong><p>换个关键词或分类试试。</p></div>
    <div v-else class="course-grid">
      <RouterLink v-for="course in filteredCourses" :key="course.id" class="course-card glass-card" :to="`/courses/${course.id}`">
        <div class="course-cover" :style="{ '--course-accent': course.accent }"><span>{{ course.icon }}</span><small>{{ course.category }}</small><i></i></div>
        <div class="course-body"><div class="course-tags"><span>{{ course.difficulty }}</span><span>{{ course.totalLessons }} 课时</span><span class="resource-count">↗ {{ course.resourceCount }} 项资源</span></div><h3>{{ course.title }}</h3><p>{{ course.subtitle }}</p><div class="teacher-row"><span>{{ course.teacherName }}</span><em>{{ Math.floor(course.durationMinutes / 60) }}h {{ course.durationMinutes % 60 }}m</em></div><div class="course-progress"><div><i :style="{ width: `${course.progressPercent}%` }"></i></div><span>{{ course.completedLessons }}/{{ course.totalLessons }} · {{ course.progressPercent }}%</span></div></div>
      </RouterLink>
    </div>
  </section>
</template>
