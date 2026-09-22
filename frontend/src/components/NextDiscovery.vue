<script setup lang="ts">
const trials = [
  { title: '我喜欢让页面更好看、更顺手', direction: '试试前端与交互', task: '给现有网站做一个深浅主题切换按钮。先画出两种效果，再实现点击切换。', result: '朋友可以选自己喜欢的阅读模式。', route: '/courses/interaction-workshop' },
  { title: '我好奇点击之后，服务器做了什么', direction: '试试后端开发', task: '在技术路线中选 Java 或 Python，做一个返回两条作品的接口，再让页面显示它们。', result: '第一次看见数据从自己的服务来到网页。', route: '/courses' },
  { title: '我喜欢整理信息、发现规律', direction: '试试数据与 SQL', task: '在知识工具箱找数据库入门，用几条校园活动数据练习按时间排序、按类别统计。', result: '回答“哪一类活动最多”这个具体问题。', route: '/knowledge' },
  { title: '我想让电脑帮我做重复的事', direction: '试试 Python 小工具', task: '在知识工具箱找 Python 入门，用自己写的三个文件名练习循环，生成一份编号清单。', result: '把一次重复劳动变成一段可以复用的程序。', route: '/knowledge' },
]
</script>
<template>
  <section class="next-discovery glass-card"><span>做完第一个网站之后</span><h3>哪一部分让你想继续研究？</h3><p>选一个小尝试就好。做过之后，再决定要不要深入这个方向。</p><div><details v-for="trial in trials" :key="trial.direction"><summary><small>{{ trial.title }}</small><strong>{{ trial.direction }} <b>＋</b></strong></summary><p>{{ trial.task }}</p><p><b>会得到什么：</b>{{ trial.result }}</p><RouterLink :to="trial.route">去找这一步的帮助 →</RouterLink></details></div></section>
</template>
<style scoped>
.next-discovery { padding: 26px; margin-top: 24px; }.next-discovery > span { font-size: 12px; color: #376c96; }h3 { font-size: 23px; margin: 12px 0; }p { color: var(--muted); font-size: 14px; line-height: 1.8; }.next-discovery > div { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 14px; margin-top: 20px; align-items: start; }details { border: 1px solid var(--border); border-radius: 12px; padding: 18px; }summary { cursor: pointer; }small { display: block; color: var(--muted); font-size: 12px; line-height: 1.6; margin: 8px 0; }strong { display: block; font-size: 16px; }strong b { float: right; }a { color: #376c96; font-size: 14px; }@media(max-width: 760px) { .next-discovery > div { grid-template-columns: 1fr; } }
</style>
