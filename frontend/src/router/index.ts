import { createRouter, createWebHashHistory } from 'vue-router'
import { getSavedToken } from '../services/api'
import LoginView from '../views/LoginView.vue'
import StudentLayout from '../layouts/StudentLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import CoursesView from '../views/CoursesView.vue'
import CourseDetailView from '../views/CourseDetailView.vue'
import ChapterLessonView from '../views/ChapterLessonView.vue'
import PracticeView from '../views/PracticeView.vue'
import KnowledgeView from '../views/KnowledgeView.vue'
import GamesView from '../views/GamesView.vue'
import FirstWebsiteLessonView from '../views/FirstWebsiteLessonView.vue'
import JourneyWorkshopView from '../views/JourneyWorkshopView.vue'
import PublishWorkshopView from '../views/PublishWorkshopView.vue'
import LaunchWorkshopView from '../views/LaunchWorkshopView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { guest: true } },
    { path: '/', component: StudentLayout, children: [
      { path: '', name: 'dashboard', component: DashboardView },
      { path: 'courses', name: 'courses', component: CoursesView },
      { path: 'courses/first-page', name: 'first-website-lesson', component: FirstWebsiteLessonView },
      { path: 'courses/style-workshop', name: 'style-workshop', component: JourneyWorkshopView, props: { stage: 'style' } },
      { path: 'courses/interaction-workshop', name: 'interaction-workshop', component: JourneyWorkshopView, props: { stage: 'interaction' } },
      { path: 'courses/publish-workshop', name: 'publish-workshop', component: PublishWorkshopView },
      { path: 'courses/launch-workshop', name: 'launch-workshop', component: LaunchWorkshopView },
      { path: 'courses/:id', name: 'course-detail', component: CourseDetailView },
      { path: 'courses/:courseId/chapters/:chapterId', name: 'chapter-lesson', component: ChapterLessonView },
      { path: 'games', name: 'games', component: GamesView },
      { path: 'knowledge', name: 'knowledge', component: KnowledgeView },
      { path: 'practice', name: 'practice', component: PracticeView },
    ] },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to) => {
  const hasToken = Boolean(getSavedToken())
  if (!to.meta.guest && !hasToken) return { name: 'login', query: { redirect: to.fullPath } }
  if (to.meta.guest && hasToken) return { name: 'dashboard' }
})

export default router
