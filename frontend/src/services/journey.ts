import { api } from './api'

export type JourneyStageId = 'intro' | 'style' | 'interaction' | 'framework' | 'publish' | 'backend' | 'database' | 'launch'
export type JourneyConfig = { project: string; frontend: string; backend: string; database: string }
export type FirstPageData = { name: string; introduction: string; interest: string; theme: 'blue' | 'orange' | 'green' }
export type StyleData = { accent: string; radius: number; spacing: number; shadow: boolean }

export type JourneyData = JourneyConfig & {
  configured: boolean
  firstPage: FirstPageData
  style: StyleData
  completedStages: JourneyStageId[]
  graduatedAt: string | null
  updatedAt: string | null
}

export const defaultJourney: JourneyData = {
  configured: false,
  project: 'portfolio',
  frontend: 'vue',
  backend: 'java',
  database: 'mysql',
  firstPage: {
    name: '小途',
    introduction: '一名正在探索 Web 世界的大一学生。',
    interest: '我喜欢摄影、音乐，也喜欢把新点子做出来。',
    theme: 'blue',
  },
  style: { accent: '#5b72f2', radius: 18, spacing: 24, shadow: true },
  completedStages: [],
  graduatedAt: null,
  updatedAt: null,
}

const legacyKeys = ['learnpath_web_journey', 'learnpath_first_page', 'learnpath_page_style', 'learnpath_web_journey_progress']

export async function loadJourney() {
  let journey = await api<JourneyData>('/api/journey')
  const hasLegacyData = legacyKeys.some((key) => localStorage.getItem(key) !== null)
  if (!hasLegacyData) return journey
  if (journey.configured) {
    legacyKeys.forEach((key) => localStorage.removeItem(key))
    return journey
  }

  try {
    const configuration = parseLegacy<JourneyConfig>('learnpath_web_journey')
    const firstPage = parseLegacy<FirstPageData>('learnpath_first_page')
    const style = parseLegacy<StyleData>('learnpath_page_style')
    const stages = parseLegacy<JourneyStageId[]>('learnpath_web_journey_progress') ?? []
    if (configuration) journey = await saveJourneyConfiguration(configuration)
    if (firstPage) journey = await saveJourneyFirstPage(firstPage)
    if (style) journey = await saveJourneyStyle(style)
    for (const stage of stages) journey = await completeJourneyStage(stage)
    legacyKeys.forEach((key) => localStorage.removeItem(key))
  } catch {
    // Keep legacy values for the next retry if MySQL or Redis is temporarily unavailable.
  }
  return journey
}

function parseLegacy<T>(key: string): T | null {
  const value = localStorage.getItem(key)
  if (!value) return null
  try { return JSON.parse(value) as T }
  catch { return null }
}

export const saveJourneyConfiguration = (config: JourneyConfig) => api<JourneyData>('/api/journey', {
  method: 'PUT',
  body: JSON.stringify(config),
})

export const saveJourneyFirstPage = (firstPage: FirstPageData) => api<JourneyData>('/api/journey/first-page', {
  method: 'PUT',
  body: JSON.stringify(firstPage),
})

export const saveJourneyStyle = (style: StyleData) => api<JourneyData>('/api/journey/style', {
  method: 'PUT',
  body: JSON.stringify(style),
})

export const completeJourneyStage = (stage: JourneyStageId) => api<JourneyData>(`/api/journey/stages/${stage}/complete`, {
  method: 'POST',
})
