export type ApiResponse<T> = { success: boolean; message: string; data: T; timestamp: string }

export type User = {
  id: number
  account: string
  displayName: string
  role: 'STUDENT' | 'TEACHER' | 'ADMIN'
}

export type LoginData = { accessToken: string; expiresInSeconds: number; user: User }

export type CourseSummary = {
  id: number
  title: string
  subtitle: string
  category: string
  teacherName: string
  difficulty: string
  durationMinutes: number
  totalLessons: number
  completedLessons: number
  progressPercent: number
  accent: string
  icon: string
}

export type Chapter = { id: number; title: string; orderIndex: number; durationMinutes: number; completed: boolean }
export type CourseDetail = CourseSummary & { description: string; lastStudiedAt: string | null; chapters: Chapter[] }

const tokenKey = 'learnpath_token'

export const getSavedToken = () => localStorage.getItem(tokenKey) ?? sessionStorage.getItem(tokenKey)

export const saveToken = (token: string, remember: boolean) => {
  localStorage.removeItem(tokenKey)
  sessionStorage.removeItem(tokenKey)
  ;(remember ? localStorage : sessionStorage).setItem(tokenKey, token)
}

export const clearToken = () => {
  localStorage.removeItem(tokenKey)
  sessionStorage.removeItem(tokenKey)
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  const token = getSavedToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  const response = await fetch(path, { ...options, headers })
  const result = await response.json() as ApiResponse<T>
  if (!response.ok || !result.success) throw new Error(result.message || '请求失败，请稍后重试')
  return result.data
}
