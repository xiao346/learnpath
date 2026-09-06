import { api } from './api'

export type GameProgress = {
  totalScore: number
  completedCount: number
  totalChallenges: number
  completedChallenges: string[]
  updatedAt: string | null
}

export const loadGameProgress = () => api<GameProgress>('/api/games/progress')

export const completeGameChallenge = (challengeId: string) => api<GameProgress>(`/api/games/challenges/${challengeId}/complete`, {
  method: 'POST',
})
