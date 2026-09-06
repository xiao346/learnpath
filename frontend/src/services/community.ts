import { api } from './api'

export type CommunityPostType = 'JOURNEY' | 'WEBSITE'
export type CommunityFilter = 'ALL' | CommunityPostType

export type CommunityPost = {
  id: number
  authorId: number
  authorName: string
  authorRole: 'STUDENT' | 'TEACHER' | 'ADMIN'
  type: CommunityPostType
  title: string
  content: string
  websiteUrl: string | null
  stackSummary: string
  imageUrls: string[]
  createdAt: string
}

export type CommunityFeed = {
  posts: CommunityPost[]
  total: number
}

export type CommunityPostDraft = {
  type: CommunityPostType
  title: string
  content: string
  websiteUrl: string | null
}

export const loadCommunityPosts = (filter: CommunityFilter) =>
  api<CommunityFeed>(`/api/community/posts?type=${filter}`)

export const publishCommunityPost = (draft: CommunityPostDraft, images: File[]) => {
  const formData = new FormData()
  formData.append('metadata', new Blob([JSON.stringify(draft)], { type: 'application/json' }))
  images.forEach(image => formData.append('images', image))
  return api<CommunityPost>('/api/community/posts', {
    method: 'POST',
    body: formData,
  })
}
