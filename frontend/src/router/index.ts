import { createRouter, createWebHashHistory } from 'vue-router'
import { getSavedToken } from '../services/api'
import LoginView from '../views/LoginView.vue'
import StudentLayout from '../layouts/StudentLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import CoursesView from '../views/CoursesView.vue'
import CourseDetailView from '../views/CourseDetailView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { guest: true } },
    { path: '/', component: StudentLayout, children: [
      { path: '', name: 'dashboard', component: DashboardView },
      { path: 'courses', name: 'courses', component: CoursesView },
      { path: 'courses/:id', name: 'course-detail', component: CourseDetailView },
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
