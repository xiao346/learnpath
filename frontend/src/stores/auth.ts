import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { api, clearToken, getSavedToken, saveToken, type LoginData, type User } from '../services/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const ready = ref(false)
  const isLoggedIn = computed(() => Boolean(user.value && getSavedToken()))

  async function login(account: string, password: string, role: string, rememberMe: boolean) {
    const result = await api<LoginData>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ account, password, role: role.toUpperCase(), rememberMe }),
    })
    saveToken(result.accessToken, rememberMe)
    user.value = result.user
  }

  async function restore() {
    const token = getSavedToken()
    if (!token) { ready.value = true; return }
    try { user.value = await api<User>('/api/auth/me') }
    catch { clearToken(); user.value = null }
    finally { ready.value = true }
  }

  async function logout() {
    try { if (getSavedToken()) await api<void>('/api/auth/logout', { method: 'POST' }) }
    finally { clearToken(); user.value = null }
  }

  return { user, ready, isLoggedIn, login, restore, logout }
})
