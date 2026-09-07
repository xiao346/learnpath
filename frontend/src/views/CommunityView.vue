<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  loadCommunityPosts,
  publishCommunityComment,
  publishCommunityPost,
  toggleCommunityPostLike,
  type CommunityFilter,
  type CommunityPost,
  type CommunityPostType,
} from '../services/community'

const filters: { id: CommunityFilter; label: string }[] = [
  { id: 'ALL', label: '全部分享' },
  { id: 'JOURNEY', label: '建站历程' },
  { id: 'WEBSITE', label: '小网站' },
]
const route = useRoute()

const activeFilter = ref<CommunityFilter>('ALL')
const posts = ref<CommunityPost[]>([])
const loading = ref(true)
const feedError = ref('')
const postType = ref<CommunityPostType>('JOURNEY')
const title = ref('')
const content = ref('')
const websiteUrl = ref('')
const publishing = ref(false)
const publishError = ref('')
const publishedMessage = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const composerDialog = ref<HTMLDialogElement | null>(null)
const selectedImages = ref<{ file: File; previewUrl: string }[]>([])
const openCommentPosts = ref(new Set<number>())
const likingPostIds = ref(new Set<number>())
const commentingPostIds = ref(new Set<number>())
const commentDrafts = ref<Record<number, string>>({})
const interactionErrors = ref<Record<number, string>>({})
const allowedImageTypes = new Set(['image/png', 'image/jpeg', 'image/webp', 'image/gif'])
const maxImageSize = 5 * 1024 * 1024

const canPublish = computed(() => title.value.trim().length >= 4
  && content.value.trim().length >= 10
  && (postType.value === 'JOURNEY' || websiteUrl.value.trim().length > 0))

function openComposer() {
  publishError.value = ''
  publishedMessage.value = ''
  composerDialog.value?.showModal()
}

function closeComposer(force = false) {
  if (publishing.value && !force) return
  composerDialog.value?.close()
}

async function loadPosts() {
  loading.value = true
  feedError.value = ''
  try {
    const feed = await loadCommunityPosts(activeFilter.value)
    posts.value = feed.posts
  } catch (cause) {
    feedError.value = cause instanceof Error ? cause.message : '社区内容加载失败'
  } finally {
    loading.value = false
  }
}

async function changeFilter(filter: CommunityFilter) {
  if (activeFilter.value === filter) return
  activeFilter.value = filter
  await loadPosts()
}

async function publish() {
  if (!canPublish.value || publishing.value) return
  publishing.value = true
  publishError.value = ''
  publishedMessage.value = ''
  try {
    await publishCommunityPost({
      type: postType.value,
      title: title.value.trim(),
      content: content.value.trim(),
      websiteUrl: websiteUrl.value.trim() || null,
    }, selectedImages.value.map(image => image.file))
    title.value = ''
    content.value = ''
    websiteUrl.value = ''
    clearSelectedImages()
    publishedMessage.value = '分享成功，大家现在可以看到你的记录了。'
    activeFilter.value = 'ALL'
    closeComposer(true)
    await loadPosts()
  } catch (cause) {
    publishError.value = cause instanceof Error ? cause.message : '分享发布失败'
  } finally {
    publishing.value = false
  }
}

function openFilePicker() {
  fileInput.value?.click()
}

function chooseImages(event: Event) {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  input.value = ''
  publishError.value = ''
  publishedMessage.value = ''

  const availableSlots = 3 - selectedImages.value.length
  if (availableSlots <= 0) {
    publishError.value = '每条分享最多上传 3 张图片。'
    return
  }

  const accepted: { file: File; previewUrl: string }[] = []
  for (const file of files.slice(0, availableSlots)) {
    if (!allowedImageTypes.has(file.type)) {
      publishError.value = '图片只支持 PNG、JPG、WebP 或 GIF 格式。'
      continue
    }
    if (file.size > maxImageSize) {
      publishError.value = `${file.name} 超过 5 MB，请压缩后再上传。`
      continue
    }
    accepted.push({ file, previewUrl: URL.createObjectURL(file) })
  }
  selectedImages.value.push(...accepted)
  if (files.length > availableSlots) publishError.value = '每条分享最多上传 3 张图片。'
}

function removeSelectedImage(index: number) {
  const [removed] = selectedImages.value.splice(index, 1)
  if (removed) URL.revokeObjectURL(removed.previewUrl)
  publishError.value = ''
}

function clearSelectedImages() {
  selectedImages.value.forEach(image => URL.revokeObjectURL(image.previewUrl))
  selectedImages.value = []
}

function setBusyPost(target: typeof likingPostIds, postId: number, busy: boolean) {
  const next = new Set(target.value)
  if (busy) next.add(postId)
  else next.delete(postId)
  target.value = next
}

function setInteractionError(postId: number, message = '') {
  interactionErrors.value = { ...interactionErrors.value, [postId]: message }
}

async function toggleLike(post: CommunityPost) {
  if (likingPostIds.value.has(post.id)) return
  setBusyPost(likingPostIds, post.id, true)
  setInteractionError(post.id)
  try {
    const result = await toggleCommunityPostLike(post.id)
    post.likeCount = result.likeCount
    post.likedByCurrentUser = result.liked
  } catch (cause) {
    setInteractionError(post.id, cause instanceof Error ? cause.message : '点赞失败，请稍后重试')
  } finally {
    setBusyPost(likingPostIds, post.id, false)
  }
}

function toggleComments(postId: number) {
  const next = new Set(openCommentPosts.value)
  if (next.has(postId)) next.delete(postId)
  else next.add(postId)
  openCommentPosts.value = next
  setInteractionError(postId)
}

async function submitComment(post: CommunityPost) {
  const content = (commentDrafts.value[post.id] ?? '').trim()
  if (!content || commentingPostIds.value.has(post.id)) return
  setBusyPost(commentingPostIds, post.id, true)
  setInteractionError(post.id)
  try {
    const comment = await publishCommunityComment(post.id, content)
    post.comments = [...post.comments, comment]
    post.commentCount = post.comments.length
    commentDrafts.value = { ...commentDrafts.value, [post.id]: '' }
  } catch (cause) {
    setInteractionError(post.id, cause instanceof Error ? cause.message : '评论发布失败，请稍后重试')
  } finally {
    setBusyPost(commentingPostIds, post.id, false)
  }
}

function formatFileSize(size: number) {
  return size >= 1024 * 1024
    ? `${(size / 1024 / 1024).toFixed(1)} MB`
    : `${Math.max(1, Math.round(size / 1024))} KB`
}

function formatTime(value: string) {
  const milliseconds = Date.now() - new Date(value).getTime()
  const minutes = Math.max(0, Math.floor(milliseconds / 60000))
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} 小时前`
  return new Date(value).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

const roleLabel = (role: CommunityPost['authorRole']) => ({
  STUDENT: '建站同学',
  TEACHER: '课程导师',
  ADMIN: '社区管理员',
}[role])

onMounted(async () => {
  await loadPosts()
  if (route.query.compose !== 'journey' && route.query.compose !== 'website') return
  postType.value = route.query.compose === 'website' ? 'WEBSITE' : 'JOURNEY'
  title.value = typeof route.query.title === 'string' ? route.query.title : ''
  content.value = typeof route.query.content === 'string' ? route.query.content : ''
  websiteUrl.value = typeof route.query.url === 'string' ? route.query.url : ''
  await nextTick()
  openComposer()
})
onBeforeUnmount(clearSelectedImages)
</script>

<template>
  <section class="community-page">
    <header class="journey-heading community-heading">
      <div><span class="eyebrow"><i></i> BUILD TOGETHER</span><h2>建站社区</h2><p>记录一次突破，展示一个小网站，也看看同学们怎样把想法做出来。</p></div>
      <div class="community-heading-actions">
        <div class="community-rule"><span>社区约定</span><p>分享真实过程，说清做了什么、遇到什么问题，以及下一步准备做什么。</p></div>
        <button class="community-compose-trigger" type="button" @click="openComposer"><span>＋</span> 发表分享</button>
      </div>
    </header>

    <dialog ref="composerDialog" class="community-composer-dialog" aria-labelledby="community-composer-title" @cancel.prevent="closeComposer()" @click="event => { if (event.target === composerDialog) closeComposer() }">
      <aside class="community-composer glass-card" @click.stop>
        <header class="community-composer-header">
          <div><span class="composer-kicker">写下这一步</span><h3 id="community-composer-title">分享你的建站进展</h3></div>
          <button type="button" :disabled="publishing" aria-label="关闭发布窗口" @click="closeComposer()">×</button>
        </header>
        <p>不必等网站完美。第一张页面、第一次解决报错，都值得记录。</p>
        <form @submit.prevent="publish">
          <div class="share-type-switch">
            <button type="button" :class="{ active: postType === 'JOURNEY' }" @click="postType = 'JOURNEY'; publishError = ''">建站历程</button>
            <button type="button" :class="{ active: postType === 'WEBSITE' }" @click="postType = 'WEBSITE'; publishError = ''">展示小网站</button>
          </div>
          <label class="community-field"><span>标题</span><input v-model="title" maxlength="80" placeholder="例如：终于让按钮动起来了" autofocus /></label>
          <label class="community-field"><span>分享内容</span><textarea v-model="content" maxlength="800" placeholder="说说你做了什么、解决了什么问题……"></textarea><small>{{ content.length }} / 800</small></label>
          <label v-if="postType === 'WEBSITE'" class="community-field"><span>作品链接</span><input v-model="websiteUrl" type="url" maxlength="400" placeholder="https://你的网站地址" /></label>
          <div class="community-upload">
            <input ref="fileInput" class="community-file-input" type="file" accept="image/png,image/jpeg,image/webp,image/gif" multiple @change="chooseImages" />
            <button type="button" :disabled="selectedImages.length >= 3" @click="openFilePicker"><span>＋</span> 添加图片</button>
            <small>{{ selectedImages.length }}/3 · 单张不超过 5 MB</small>
          </div>
          <div v-if="selectedImages.length" class="community-upload-previews">
            <figure v-for="(image, index) in selectedImages" :key="image.previewUrl">
              <img :src="image.previewUrl" :alt="`待上传图片 ${index + 1}`" />
              <button type="button" :aria-label="`移除 ${image.file.name}`" @click="removeSelectedImage(index)">×</button>
              <figcaption><span>{{ image.file.name }}</span><small>{{ formatFileSize(image.file.size) }}</small></figcaption>
            </figure>
          </div>
          <p v-if="publishError" class="practice-error">{{ publishError }}</p>
          <button class="community-submit" type="submit" :disabled="!canPublish || publishing">{{ publishing ? '正在发布…' : '发布到社区 →' }}</button>
        </form>
      </aside>
    </dialog>

    <p v-if="publishedMessage" class="community-success community-publish-notice" role="status">{{ publishedMessage }}</p>

    <div class="community-layout">
      <section class="community-feed">
        <header class="community-feed-header">
          <div><span>同学们的最新动态</span><strong>{{ posts.length }} 条分享</strong></div>
          <div class="community-filters"><button v-for="filter in filters" :key="filter.id" type="button" :class="{ active: activeFilter === filter.id }" @click="changeFilter(filter.id)">{{ filter.label }}</button></div>
        </header>

        <div v-if="loading" class="state-card glass-card"><span class="loader"></span><p>正在加载社区动态…</p></div>
        <div v-else-if="feedError" class="state-card glass-card"><strong>社区暂时没有连上</strong><p>{{ feedError }}</p><button type="button" @click="loadPosts">重新加载</button></div>
        <div v-else-if="!posts.length" class="community-empty glass-card"><span>✦</span><h3>还没有人分享这一类内容</h3><p>点击“发表分享”，写下第一条建站记录吧。</p><button type="button" @click="openComposer">发表第一条分享</button></div>
        <div v-else class="community-posts">
          <article v-for="post in posts" :key="post.id" class="community-post glass-card">
            <header>
              <div class="community-avatar">{{ post.authorName.slice(0, 1) }}</div>
              <div><strong>{{ post.authorName }}</strong><span>{{ roleLabel(post.authorRole) }} · {{ formatTime(post.createdAt) }}</span></div>
              <em :class="post.type.toLowerCase()">{{ post.type === 'WEBSITE' ? '作品展示' : '建站历程' }}</em>
            </header>
            <h3>{{ post.title }}</h3>
            <p>{{ post.content }}</p>
            <div v-if="post.imageUrls?.length" class="community-gallery" :class="`count-${Math.min(post.imageUrls.length, 3)}`">
              <a v-for="(imageUrl, index) in post.imageUrls" :key="imageUrl" :href="imageUrl" target="_blank" rel="noopener noreferrer">
                <img :src="imageUrl" :alt="`${post.title} 的分享图片 ${index + 1}`" loading="lazy" />
              </a>
            </div>
            <footer><span>{{ post.stackSummary }}</span><a v-if="post.websiteUrl" :href="post.websiteUrl" target="_blank" rel="noopener noreferrer">访问作品 ↗</a></footer>
            <div class="community-interactions">
              <button type="button" :class="{ active: post.likedByCurrentUser }" :disabled="likingPostIds.has(post.id)" :aria-pressed="post.likedByCurrentUser" @click="toggleLike(post)"><span>{{ post.likedByCurrentUser ? '♥' : '♡' }}</span>{{ post.likedByCurrentUser ? '已鼓励' : '给他鼓励' }}<b>{{ post.likeCount }}</b></button>
              <button type="button" :class="{ active: openCommentPosts.has(post.id) }" :aria-expanded="openCommentPosts.has(post.id)" @click="toggleComments(post.id)"><span>◌</span>评论<b>{{ post.commentCount }}</b></button>
            </div>
            <section v-if="openCommentPosts.has(post.id)" class="community-comments" :aria-label="`${post.title}的评论`">
              <div v-if="post.comments.length" class="community-comment-list"><article v-for="comment in post.comments" :key="comment.id"><div class="community-comment-avatar">{{ comment.authorName.slice(0, 1) }}</div><div><header><strong>{{ comment.authorName }}</strong><span>{{ roleLabel(comment.authorRole) }} · {{ formatTime(comment.createdAt) }}</span></header><p>{{ comment.content }}</p></div></article></div>
              <p v-else class="community-no-comments">还没有评论，写下第一句鼓励或建议吧。</p>
              <form class="community-comment-form" @submit.prevent="submitComment(post)"><label><span class="sr-only">评论内容</span><textarea v-model="commentDrafts[post.id]" maxlength="300" rows="2" placeholder="写一句鼓励，或给出具体的建站建议……"></textarea><small>{{ (commentDrafts[post.id] ?? '').length }} / 300</small></label><button type="submit" :disabled="!(commentDrafts[post.id] ?? '').trim() || commentingPostIds.has(post.id)">{{ commentingPostIds.has(post.id) ? '发送中…' : '发表评论' }}</button></form>
              <p v-if="interactionErrors[post.id]" class="practice-error">{{ interactionErrors[post.id] }}</p>
            </section>
            <p v-else-if="interactionErrors[post.id]" class="practice-error community-interaction-error">{{ interactionErrors[post.id] }}</p>
          </article>
        </div>
      </section>
    </div>
  </section>
</template>
