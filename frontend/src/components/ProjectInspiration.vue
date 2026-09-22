<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { projectGoals } from '../content/beginnerGuidance'
const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
const category = ref('全部')
const opened = ref<string | null>(null)
const demos: Record<string, { title: string; intro: string; hint: string; items: { title: string; category: string; detail: string }[] }> = {
  portfolio: { title: '小林的灵感收藏夹', intro: '刚上大学，也有值得展示的东西。', hint: '试试只看摄影，再展开一件作品。', items: [
    { title: '镜头里的校园黄昏', category: '摄影', detail: '用手机记录操场与晚霞。作品介绍可以写拍摄地点、当时的想法，再配上自己的照片。' },
    { title: '我画的社团招新海报', category: '设计', detail: '这是我的第一张海报。介绍想传达的信息和一次修改，就能让别人看懂你的创作过程。' },
    { title: '宿舍窗边的光', category: '摄影', detail: '普通生活也是作品的素材。没有编程项目时，先从你已经做过的事开始。' },
  ] },
  blog: { title: '课后随便聊聊', intro: '把自己喜欢的事，写给同样好奇的人。', hint: '选一个主题，点开一篇短文章。', items: [
    { title: '让我想重看一次的电影', category: '电影', detail: '最打动我的不是结局，而是主角第一次认真说出自己的想法。第一篇文章，写下这样一个具体感受就够了。' },
    { title: '第一次尝试独立游戏', category: '游戏', detail: '关卡不多，但每一次操作都有回应。我开始好奇：网页上的按钮是不是也能这样有趣？' },
    { title: '我的周末观影清单', category: '电影', detail: '写下片名、推荐理由和适合谁看，就可以成为别人真正会用的清单。' },
  ] },
  campus: { title: '这周校园有什么？', intro: '帮还不熟悉学校的同学找到第一场活动。', hint: '筛选你感兴趣的活动，展开查看时间和地点。', items: [
    { title: '新生摄影散步', category: '社团', detail: '示例活动 · 周六 16:00 · 图书馆门口集合。带手机即可，和摄影社一起认识校园。' },
    { title: '第一次编程交流会', category: '分享', detail: '示例活动 · 周日 14:00 · 学生活动中心 201。聊聊大家想做的小项目，零基础也能参加。' },
    { title: '桌游之夜', category: '社团', detail: '示例活动 · 周五 19:00 · 社团活动室。第一次参加可以先从规则简单的合作游戏开始。' },
  ] },
}
const demo = computed(() => demos[props.modelValue] ?? demos.portfolio!)
const goal = computed(() => projectGoals[props.modelValue] ?? projectGoals.portfolio!)
const categories = computed(() => ['全部', ...new Set(demo.value.items.map(item => item.category))])
const visibleItems = computed(() => demo.value.items.filter(item => category.value === '全部' || item.category === category.value))
watch(() => props.modelValue, () => { category.value = '全部'; opened.value = null })
function filter(value: string) { category.value = value; opened.value = null }
</script>

<template>
  <section class="project-inspiration" aria-label="选择想做的作品">
    <div class="goal-choices"><button v-for="(item, id) in projectGoals" :key="id" type="button" :aria-pressed="modelValue === id" :class="{ selected: modelValue === id }" @click="emit('update:modelValue', String(id))"><strong>{{ item.name }}</strong><span>{{ item.purpose }}</span><small>{{ modelValue === id ? '✓ 已选择' : '选择这个作品' }}</small></button></div>
    <details class="optional-demo"><summary>想先看效果？体验{{ goal.name }}示例 <span>可选</span></summary><div class="inspiration-detail">
      <div class="demo-browser">
        <div class="demo-bar"><span>● ● ●</span> 可交互示例 · 用来体验目标</div>
        <div class="demo-content"><small>你也可以做出这样的第一版</small><h3>{{ demo.title }}</h3><p>{{ demo.intro }}</p><div class="demo-filters" aria-label="示例内容分类"><button v-for="tag in categories" :key="tag" type="button" :aria-pressed="category === tag" @click="filter(tag)">{{ tag }}</button></div><div class="demo-items" aria-live="polite"><article v-for="item in visibleItems" :key="item.title"><button type="button" :aria-expanded="opened === item.title" @click="opened = opened === item.title ? null : item.title"><span><small>{{ item.category }}</small><strong>{{ item.title }}</strong></span><b>{{ opened === item.title ? '−' : '＋' }}</b></button><p v-if="opened === item.title">{{ item.detail }}</p></article></div></div>
      </div>
      <aside><span class="goal-kicker">先体验，再决定</span><h3>这个网站可以帮谁？</h3><p>{{ goal.audience }}。</p><h4>第一版从什么内容开始？</h4><p>{{ goal.first }}</p><div class="demo-hint">{{ demo.hint }}</div><p class="goal-reassurance">暂时没想法？就从个人作品集开始，介绍自己和一个兴趣。目标可以在做的过程中慢慢变清楚。</p><small>示例里的筛选和详情将在交互站完成；第一站先写出内容，不必一次做到最终效果。</small></aside>
    </div></details>
  </section>
</template>

<style scoped>
.project-inspiration { margin: 0; }
.optional-demo { margin-top: 20px; border-top: 1px solid #dce5ed; padding-top: 18px; }.optional-demo > summary { color: #285e87; font-size: 14px; cursor: pointer; line-height: 1.8; }.optional-demo > summary > span { margin-left: 8px; font-size: 12px; color: #455b70; }.goal-choices { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 12px; }
.goal-choices button { display: grid; gap: 10px; padding: 20px; border: 1px solid var(--border); border-radius: 14px; background: #fff; color: #18324a; text-align: left; cursor: pointer; }
.goal-choices button.selected { border-color: #326b96; background: #eaf3fa; box-shadow: inset 0 0 0 1px #326b96; }.goal-choices strong { font-size: 17px; color: #18324a; }.goal-choices button:focus-visible { outline: 3px solid #285e87; outline-offset: 3px; }.goal-choices button:hover { border-color: #326b96; }.goal-choices span { color: #455b70; font-size: 14px; line-height: 1.7; }.goal-choices small { color: #376c96; }
.inspiration-detail { display: grid; grid-template-columns: minmax(0,1.35fr) minmax(0,1fr); gap: 28px; margin-top: 22px; align-items: start; }.demo-browser { border: 1px solid #cbdbe7; border-radius: 16px; overflow: hidden; background: #fff; color: #243e51; }
.demo-bar { background: #eaf1f6; padding: 12px 18px; font-size: 12px; color: #57758b; }.demo-bar span { margin-right: 16px; color: #8eabbd; }.demo-content { padding: 24px; }.demo-content > small, .goal-kicker { color: #477e9f; font-size: 12px; font-weight: 600; }.demo-content h3 { font-size: 24px; margin: 9px 0; }.demo-content > p { font-size: 14px; color: #455b70; }
.demo-filters { display: flex; flex-wrap: wrap; gap: 8px; margin: 20px 0 14px; }.demo-filters button { border: 1px solid #d8e3eb; border-radius: 20px; background: white; padding: 7px 14px; color: #476a82; cursor: pointer; }.demo-filters button[aria-pressed=true] { color: white; background: #426f91; border-color: #426f91; }
.demo-items { display: grid; gap: 9px; }.demo-items article { border: 1px solid #e0e8ee; border-radius: 10px; overflow: hidden; }.demo-items button { display: flex; justify-content: space-between; align-items: center; gap: 14px; padding: 14px; border: 0; width: 100%; background: #f6f9fb; color: #29495f; text-align: left; cursor: pointer; }.demo-items small { display: block; font-size: 11px; color: #455b70; margin-bottom: 5px; }.demo-items strong { font-size: 14px; }.demo-items p { margin: 0; padding: 14px; font-size: 13px; line-height: 1.8; }
aside h3 { margin: 10px 0; font-size: 20px; }aside h4 { margin: 20px 0 8px; }aside p { color: #455b70; font-size: 14px; line-height: 1.8; }aside small { display: block; color: #455b70; font-size: 12px; line-height: 1.8; }.demo-hint { background: rgba(88,142,177,.09); border-radius: 10px; padding: 14px; color: #376c96; font-size: 14px; line-height: 1.7; }.goal-reassurance { border-top: 1px solid var(--border); padding-top: 16px; }
@media(max-width: 800px) { .inspiration-detail { grid-template-columns: 1fr; }.goal-choices { grid-template-columns: 1fr; }.goal-choices button { gap: 6px; padding: 15px; }.demo-content { padding: 18px; } }
</style>
