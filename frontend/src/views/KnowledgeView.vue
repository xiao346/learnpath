<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api, type CourseSummary } from '../services/api'

const route = useRoute()
const featuredTitles = ['HTML 与 CSS 网页设计', 'JavaScript 网页交互', 'Vue 3 前端开发', 'FastAPI 后端开发', 'Java Web 应用开发', '数据库原理']
const courses = ref<CourseSummary[]>([])
const keyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const category = ref('全部')
const loading = ref(true)
const error = ref('')
const categories = computed(() => ['全部', ...new Set(courses.value.map((course) => course.category))])
const filteredCourses = computed(() => courses.value.filter((course) => {
  const matchesCategory = category.value === '全部' || course.category === category.value
  const text = `${course.title}${course.subtitle}${course.teacherName}`.toLowerCase()
  return matchesCategory && text.includes(keyword.value.trim().toLowerCase())
}).sort((a, b) => Number(featuredTitles.includes(b.title)) - Number(featuredTitles.includes(a.title))))

async function loadCourses() {
  loading.value = true
  error.value = ''
  try { courses.value = await api<CourseSummary[]>('/api/courses') }
  catch (cause) { error.value = cause instanceof Error ? cause.message : '知识内容加载失败' }
  finally { loading.value = false }
}
onMounted(loadCourses)
</script>

<template>
  <section class="course-page knowledge-page">
    <header class="journey-heading">
      <div><span class="eyebrow"><i></i> KNOWLEDGE TOOLBOX</span><h2>知识工具箱</h2><p>建站时遇到陌生概念，就来这里查一查、补一补。</p></div>
      <div class="toolbox-tip"><span>使用方法</span><p>先沿建站路线动手，卡住时再回来学习相关知识。</p></div>
    </header>
    <div class="course-toolbar"><label class="search-box"><span>⌕</span><input v-model="keyword" placeholder="搜索 SQL、HTTP、Java 或课程" /></label><div class="category-tabs"><button v-for="item in categories" :key="item" :class="{ active: category === item }" @click="category = item">{{ item }}</button></div></div>
    <div v-if="loading" class="state-card glass-card"><span class="loader"></span><p>正在整理工具箱…</p></div>
    <div v-else-if="error" class="state-card glass-card"><strong>知识工具箱暂时打不开</strong><p>{{ error }}</p><button @click="loadCourses">重新加载</button></div>
    <div v-else-if="!filteredCourses.length" class="state-card glass-card"><strong>没有找到相关内容</strong><p>换一个知识点名称试试。</p></div>
    <div v-else class="course-grid">
      <RouterLink v-for="course in filteredCourses" :key="course.id" class="course-card glass-card" :class="{ featured: featuredTitles.includes(course.title) }" :to="`/courses/${course.id}`">
        <div class="course-cover" :style="{ '--course-accent': course.accent }"><span>{{ course.icon }}</span><small>{{ course.category }}</small><b v-if="featuredTitles.includes(course.title)" class="new-course-badge">建站路线</b><i></i></div>
        <div class="course-body"><div class="course-tags"><span>{{ course.difficulty }}</span><span>{{ course.totalLessons }} 个知识单元</span></div><h3>{{ course.title }}</h3><p>{{ course.subtitle }}</p><div class="teacher-row"><span>{{ course.teacherName }}</span><em>约 {{ Math.floor(course.durationMinutes / 60) }} 小时</em></div><div class="course-progress"><div><i :style="{ width: `${course.progressPercent}%` }"></i></div><span>{{ course.completedLessons }}/{{ course.totalLessons }} · {{ course.progressPercent }}%</span></div></div>
      </RouterLink>
    </div>
  </section>
</template>
