<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  loadCommunityPosts,
  publishCommunityPost,
  type CommunityFilter,
  type CommunityPost,
  type CommunityPostType,
} from '../services/community'

const filters: { id: CommunityFilter; label: string }[] = [
  { id: 'ALL', label: '全部分享' },
  { id: 'JOURNEY', label: '建站历程' },
  { id: 'WEBSITE', label: '小网站' },
]

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

const canPublish = computed(() => title.value.trim().length >= 4
  && content.value.trim().length >= 10
  && (postType.value === 'JOURNEY' || websiteUrl.value.trim().length > 0))

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
    })
    title.value = ''
    content.value = ''
    websiteUrl.value = ''
    publishedMessage.value = '分享成功，大家现在可以看到你的记录了。'
    activeFilter.value = 'ALL'
    await loadPosts()
  } catch (cause) {
    publishError.value = cause instanceof Error ? cause.message : '分享发布失败'
  } finally {
    publishing.value = false
  }
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

onMounted(loadPosts)
</script>

<template>
  <section class="community-page">
    <header class="journey-heading community-heading">
      <div><span class="eyebrow"><i></i> BUILD TOGETHER</span><h2>建站社区</h2><p>记录一次突破，展示一个小网站，也看看同学们怎样把想法做出来。</p></div>
      <div class="community-rule"><span>社区约定</span><p>分享真实过程，说清做了什么、遇到什么问题，以及下一步准备做什么。</p></div>
    </header>

    <div class="community-layout">
      <aside class="community-composer glass-card">
        <span class="composer-kicker">写下这一步</span>
        <h3>分享你的建站进展</h3>
        <p>不必等网站完美。第一张页面、第一次解决报错，都值得记录。</p>
        <form @submit.prevent="publish">
          <div class="share-type-switch">
            <button type="button" :class="{ active: postType === 'JOURNEY' }" @click="postType = 'JOURNEY'; publishError = ''">建站历程</button>
            <button type="button" :class="{ active: postType === 'WEBSITE' }" @click="postType = 'WEBSITE'; publishError = ''">展示小网站</button>
          </div>
          <label class="community-field"><span>标题</span><input v-model="title" maxlength="80" placeholder="例如：终于让按钮动起来了" /></label>
          <label class="community-field"><span>分享内容</span><textarea v-model="content" maxlength="800" placeholder="说说你做了什么、解决了什么问题……"></textarea><small>{{ content.length }} / 800</small></label>
          <label v-if="postType === 'WEBSITE'" class="community-field"><span>作品链接</span><input v-model="websiteUrl" type="url" maxlength="400" placeholder="https://你的网站地址" /></label>
          <p v-if="publishError" class="practice-error">{{ publishError }}</p>
          <p v-if="publishedMessage" class="community-success">{{ publishedMessage }}</p>
          <button class="community-submit" type="submit" :disabled="!canPublish || publishing">{{ publishing ? '正在发布…' : '发布到社区 →' }}</button>
        </form>
      </aside>

      <section class="community-feed">
        <header class="community-feed-header">
          <div><span>同学们的最新动态</span><strong>{{ posts.length }} 条分享</strong></div>
          <div class="community-filters"><button v-for="filter in filters" :key="filter.id" type="button" :class="{ active: activeFilter === filter.id }" @click="changeFilter(filter.id)">{{ filter.label }}</button></div>
        </header>

        <div v-if="loading" class="state-card glass-card"><span class="loader"></span><p>正在加载社区动态…</p></div>
        <div v-else-if="feedError" class="state-card glass-card"><strong>社区暂时没有连上</strong><p>{{ feedError }}</p><button type="button" @click="loadPosts">重新加载</button></div>
        <div v-else-if="!posts.length" class="community-empty glass-card"><span>✦</span><h3>还没有人分享这一类内容</h3><p>把左边的第一条建站记录写下来吧。</p></div>
        <div v-else class="community-posts">
          <article v-for="post in posts" :key="post.id" class="community-post glass-card">
            <header>
              <div class="community-avatar">{{ post.authorName.slice(0, 1) }}</div>
              <div><strong>{{ post.authorName }}</strong><span>{{ roleLabel(post.authorRole) }} · {{ formatTime(post.createdAt) }}</span></div>
              <em :class="post.type.toLowerCase()">{{ post.type === 'WEBSITE' ? '作品展示' : '建站历程' }}</em>
            </header>
            <h3>{{ post.title }}</h3>
            <p>{{ post.content }}</p>
            <footer><span>{{ post.stackSummary }}</span><a v-if="post.websiteUrl" :href="post.websiteUrl" target="_blank" rel="noopener noreferrer">访问作品 ↗</a></footer>
          </article>
        </div>
      </section>
    </div>
  </section>
</template>
