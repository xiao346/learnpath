import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const auth = useAuthStore(pinia)

async function bootstrap() {
  await auth.restore()
  await router.isReady()
  if (!auth.isLoggedIn && router.currentRoute.value.name !== 'login') {
    await router.replace({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
  }
  app.mount('#app')
}

bootstrap()
