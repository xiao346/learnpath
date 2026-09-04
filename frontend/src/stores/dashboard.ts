import { ref } from 'vue'
import { defineStore } from 'pinia'
import { api, type DashboardData } from '../services/api'

export const useDashboardStore = defineStore('dashboard', () => {
  const data = ref<DashboardData | null>(null)
  const loading = ref(false)
  const error = ref('')
  const savingTaskIds = ref(new Set<number>())

  async function load(force = false) {
    if (loading.value) return
    if (data.value && !force) return
    loading.value = true
    error.value = ''
    try { data.value = await api<DashboardData>('/api/dashboard') }
    catch (cause) { error.value = cause instanceof Error ? cause.message : '首页数据加载失败' }
    finally { loading.value = false }
  }

  async function toggleTask(taskId: number) {
    if (savingTaskIds.value.has(taskId)) return
    savingTaskIds.value.add(taskId)
    error.value = ''
    try { data.value = await api<DashboardData>(`/api/dashboard/tasks/${taskId}/toggle`, { method: 'POST' }) }
    catch (cause) { error.value = cause instanceof Error ? cause.message : '任务状态保存失败' }
    finally {
      savingTaskIds.value.delete(taskId)
      savingTaskIds.value = new Set(savingTaskIds.value)
    }
  }

  return { data, loading, error, savingTaskIds, load, toggleTask }
})
