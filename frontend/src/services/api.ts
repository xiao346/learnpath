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
  resourceCount: number
  completedLessons: number
  progressPercent: number
  accent: string
  icon: string
}

export type Chapter = { id: number; title: string; orderIndex: number; durationMinutes: number; completed: boolean }
export type CourseResource = { id: number; title: string; provider: string; resourceType: string; description: string; url: string }
export type CourseDetail = CourseSummary & { description: string; lastStudiedAt: string | null; chapters: Chapter[]; resources: CourseResource[] }
export type ChapterLesson = {
  courseId: number
  courseTitle: string
  chapterId: number
  chapterTitle: string
  orderIndex: number
  durationMinutes: number
  completed: boolean
  overview: string
  beginnerIntro: string
  beginnerAnalogy: string
  beginnerWalkthrough: string[]
  objectives: string[]
  keyPoints: string[]
  workedExample: WorkedExample
  learningPath: LearningStep[]
  knowledgeAnalyses: KnowledgeAnalysis[]
  studySections: StudySection[]
  selfCheckQuestions: string[]
  practiceTask: string
  previousChapterId: number | null
  nextChapterId: number | null
}
export type WorkedExampleStep = { label: string; action: string; explanation: string }
export type WorkedExample = { title: string; scenario: string; steps: WorkedExampleStep[]; result: string; tryIt: string }
export type LearningStep = { id: string; stage: string; title: string; detail: string }
export type DiagramStep = { label: string; content: string }
export type KnowledgeAnalysis = {
  id: string
  title: string
  category: string
  plainExplanation: string
  whyItMatters: string
  diagram: DiagramStep[]
  example: string
  commonMistake: string
  quickCheck: string
}
export type StudySection = { title: string; summary: string; points: string[] }

export type DashboardFocus = { courseId: number; courseTitle: string; chapterTitle: string; estimatedMinutes: number; completedLessons: number; totalLessons: number; progressPercent: number }
export type DashboardTask = { id: number; title: string; subject: string; estimatedMinutes: number; xpReward: number; completed: boolean }
export type DashboardDay = { date: string; label: string; minutes: number; studied: boolean; today: boolean }
export type DashboardTrend = { totalMinutes: number; previousWeekMinutes: number; changePercent: number; days: DashboardDay[] }
export type DashboardRecommendation = { title: string; description: string; route: string }
export type DashboardData = {
  focus: DashboardFocus | null
  streakDays: number
  tasksCompleted: number
  totalTasks: number
  tasks: DashboardTask[]
  trend: DashboardTrend
  weeklyGoalPercent: number
  weeklyRemainingMinutes: number
  recommendation: DashboardRecommendation
}

export type PracticeOption = { key: string; text: string }
export type PracticeQuestion = {
  id: number
  subject: string
  prompt: string
  options: PracticeOption[]
  difficulty: string
  points: number
  answered: boolean
}
export type PracticeStats = { totalAnswered: number; correctAnswers: number; accuracyPercent: number; totalPoints: number }
export type PracticeAnswerResult = {
  questionId: number
  selectedOption: string
  correctOption: string
  correct: boolean
  explanation: string
  pointsEarned: number
  stats: PracticeStats
}

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
