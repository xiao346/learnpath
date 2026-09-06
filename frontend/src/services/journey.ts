export type JourneyStageId = 'intro' | 'style' | 'interaction' | 'framework' | 'publish' | 'backend' | 'database' | 'launch'

const progressKey = 'learnpath_web_journey_progress'

export function readCompletedStages(): JourneyStageId[] {
  try {
    const stored = JSON.parse(localStorage.getItem(progressKey) ?? '[]') as string[]
    return stored.filter((item): item is JourneyStageId =>
      ['intro', 'style', 'interaction', 'framework', 'publish', 'backend', 'database', 'launch'].includes(item))
  } catch {
    localStorage.removeItem(progressKey)
    return []
  }
}

export function completeJourneyStage(stage: JourneyStageId) {
  const completed = new Set(readCompletedStages())
  completed.add(stage)
  localStorage.setItem(progressKey, JSON.stringify([...completed]))
}
