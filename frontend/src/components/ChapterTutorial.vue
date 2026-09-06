<script setup lang="ts">
import type { ChapterTutorial } from '../content/chapterTutorials'
import NetworkMechanismFigure from './NetworkMechanismFigure.vue'
import DatabaseMechanismFigure from './DatabaseMechanismFigure.vue'
import KnowledgePointDiagram from './KnowledgePointDiagram.vue'
import '../styles/tutorial-theme.css'

defineProps<{ tutorial: ChapterTutorial; chapterTitle: string; courseTitle: string }>()
function goToSection(id: string) {
  const target = document.getElementById(`tutorial-${id}`)
  target?.scrollIntoView({ behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth', block: 'start' })
  target?.focus({ preventScroll: true })
}
function printTutorial() { window.print() }
</script>

<template>
  <article class="chapter-tutorial glass-card" :aria-label="`${chapterTitle}完整图文讲解`">
    <header class="tutorial-opening">
      <div class="reader-toolbar"><span>原理 · 推演 · 验证</span><button type="button" @click="printTutorial">打印 / 保存 PDF</button></div>
      <h3>{{ chapterTitle }}</h3>
      <p class="tutorial-lead">{{ tutorial.lead }}</p>
      <p class="tutorial-scenario">{{ tutorial.scenario }}</p>
    </header>
    <nav class="tutorial-toc" aria-label="本章阅读目录"><strong>阅读路线</strong><ol><li v-for="section in tutorial.sections" :key="section.id"><button type="button" @click="goToSection(section.id)">{{ section.title }}</button></li></ol></nav>
    <section v-if="tutorial.guidedPractice" class="guided-practice" aria-label="完整跟做示例">
      <span>先完整跟做一次</span>
      <h4>{{ tutorial.guidedPractice.title }}</h4>
      <p>{{ tutorial.guidedPractice.scenario }}</p>
      <ol><li v-for="(step, index) in tutorial.guidedPractice.steps" :key="`${step.label}-${index}`"><b>{{ String(index + 1).padStart(2, '0') }}</b><div><small>{{ step.label }}</small><strong>{{ step.action }}</strong><p>{{ step.explanation }}</p></div></li></ol>
      <div class="guided-result"><div><small>应得到的结果</small><p>{{ tutorial.guidedPractice.result }}</p></div><div><small>换个条件再做</small><p>{{ tutorial.guidedPractice.tryIt }}</p></div></div>
    </section>
    <section v-for="section in tutorial.sections" :id="`tutorial-${section.id}`" :key="section.id" class="tutorial-section" tabindex="-1">
      <h4>{{ section.title }}</h4>
      <p v-for="paragraph in section.paragraphs" :key="paragraph">{{ paragraph }}</p>
      <NetworkMechanismFigure v-if="section.figure" :kind="section.figure" />
      <DatabaseMechanismFigure v-if="section.databaseFigure" :key="section.id" :kind="section.databaseFigure" />
      <KnowledgePointDiagram v-if="section.visualIndex !== undefined" :chapter-title="chapterTitle" :course-title="courseTitle" :point-index="section.visualIndex" :title="section.title" />
      <ol v-if="section.conceptDiagram?.length" class="tutorial-concept-flow" aria-label="知识点推演过程"><li v-for="(step, index) in section.conceptDiagram" :key="`${step.label}-${index}`"><small>{{ step.label }}</small><strong>{{ step.content }}</strong></li></ol>
      <div v-if="section.code" class="tutorial-code"><span>{{ section.code.title }}</span><pre><code>{{ section.code.text }}</code></pre></div>
      <div v-if="section.table" class="tutorial-table" tabindex="0" aria-label="知识对比表，可横向滚动"><table><thead><tr><th v-for="heading in section.table.headings" :key="heading" scope="col">{{ heading }}</th></tr></thead><tbody><tr v-for="(row,index) in section.table.rows" :key="index"><td v-for="(cell,i) in row" :key="i">{{ cell }}</td></tr></tbody></table></div>
      <p v-for="paragraph in section.after" :key="paragraph">{{ paragraph }}</p>
      <aside v-if="section.example" class="tutorial-example"><strong>放进具体例子</strong><p>{{ section.example }}</p></aside>
      <aside v-if="section.warning" class="tutorial-warning"><strong>常见误区与修正</strong><p>{{ section.warning }}</p></aside>
      <aside v-if="section.note" class="tutorial-note"><strong>{{ section.note.title }}</strong><p>{{ section.note.body }}</p></aside>
      <details v-if="section.check" class="tutorial-check"><summary>{{ section.check.question }}<span>先思考，再展开解析</span></summary><p>{{ section.check.answer }}</p></details>
    </section>
    <footer class="tutorial-recap"><h4>把这些关系带走</h4><ul><li v-for="point in tutorial.recap" :key="point">{{ point }}</li></ul><div v-if="tutorial.sources.length" class="tutorial-sources"><strong>继续核对原理与实现细节</strong><p>正文使用简化示例帮助理解；具体实现、选项和异常分支应以相应规范与产品行为为准。</p><ul><li v-for="source in tutorial.sources" :key="source.href"><a :href="source.href" target="_blank" rel="noopener noreferrer">{{ source.label }} ↗</a></li></ul></div></footer>
  </article>
</template>

<style scoped>
.chapter-tutorial { background: #fff; color: #263e50; border: 1px solid #dce5ec; border-radius: 12px; padding: 38px clamp(22px, 4vw, 52px) 42px; min-width: 0; font-size: 17px; line-height: 2.05; overflow-wrap: anywhere; }
.reader-toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; color: #567586; font-size: 12px; letter-spacing: 1.2px; }
.reader-toolbar button { font: inherit; letter-spacing: 0; border: 1px solid #b8cbd7; border-radius: 5px; background: #fff; color: #365e77; padding: 7px 11px; cursor: pointer; }
.tutorial-opening h3 { margin: 24px 0 12px; font-size: clamp(26px, 3vw, 34px); line-height: 1.45; color: #193b53; letter-spacing: -.5px; }
.tutorial-lead { font-size: 19px; line-height: 1.9; color: #244f6c; }
.tutorial-scenario { margin: 22px 0 0; padding-left: 17px; border-left: 3px solid #a1c6d7; color: #577184; font-size: 15px; }
.tutorial-toc { margin: 32px 0 40px; padding: 22px 0; border-block: 1px solid #dae5ed; font-size: 15px; }
.tutorial-toc > strong { color: #284f67; font-size: 15px; }
.tutorial-toc ol { padding-left: 24px; margin: 10px 0 0; }
.tutorial-toc li { padding: 3px 0; color: #758c9b; }
.tutorial-toc button { border: 0; background: transparent; padding: 2px 0; font: inherit; color: #246789; text-align: left; cursor: pointer; line-height: 1.8; }
.tutorial-toc button:hover { text-decoration: underline; text-underline-offset: 4px; }
.guided-practice { margin: 36px 0 44px; padding: 26px; border: 1px solid #cfe0e9; border-radius: 9px; background: linear-gradient(145deg, #f5fafc, #edf5f8); }
.guided-practice > span { color: #25708c; font-size: 12px; letter-spacing: 1px; }
.guided-practice > h4 { margin: 7px 0 8px; color: #173e55; font-size: 22px; }
.guided-practice > p { margin: 0; color: #597586; font-size: 15px; }
.guided-practice > ol { margin: 22px 0 0; padding: 0; display: grid; gap: 10px; list-style: none; }
.guided-practice > ol li { padding: 15px; display: grid; grid-template-columns: auto 1fr; gap: 14px; border: 1px solid #d8e5eb; border-radius: 7px; background: rgba(255,255,255,.72); }
.guided-practice > ol b { width: 34px; height: 34px; display: grid; place-items: center; border-radius: 50%; color: #fff; background: #347d98; font-size: 11px; }
.guided-practice > ol small { display: block; color: #598195; font-size: 12px; }
.guided-practice > ol strong { display: block; margin-top: 3px; color: #274a5d; font-size: 15px; }
.guided-practice > ol p { margin: 6px 0 0; color: #5e7482; font-size: 14px; line-height: 1.8; }
.guided-result { margin-top: 12px; display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.guided-result > div { padding: 15px; border-left: 3px solid #55a6bc; background: #fff; }
.guided-result > div:last-child { border-left-color: #d4a54d; }
.guided-result small { color: #597f91; font-size: 12px; }
.guided-result p { margin: 6px 0 0; color: #3f5969; font-size: 14px; line-height: 1.8; }
.tutorial-section { margin-top: 42px; scroll-margin-top: 24px; }
.tutorial-section:focus { outline: none; }
.tutorial-section:focus-visible { outline: 2px solid #6eacc8; outline-offset: 8px; }
.tutorial-section h4, .tutorial-recap h4 { margin: 0 0 21px; padding-bottom: 12px; border-bottom: 1px solid #dde6ed; font-size: 24px; line-height: 1.65; color: #183c55; font-weight: 650; }
.tutorial-section > p { margin: 20px 0; }
.tutorial-code { margin: 26px 0; }
.tutorial-code > span { color: #526f82; font-size: 13px; line-height: 1.8; display: block; margin-bottom: 8px; }
pre { padding: 20px; overflow-x: auto; border: 1px solid #d5e2eb; border-radius: 5px; background: #f3f7fa; font-size: 14px; line-height: 1.9; white-space: pre; color: #244f6c; }
code { font-family: Consolas, 'Microsoft YaHei', monospace; }
.tutorial-table { overflow-x: auto; margin: 28px 0; }
.tutorial-concept-flow { margin: 28px 0; padding: 0; display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 22px; list-style: none; }
.tutorial-concept-flow li { position: relative; min-height: 98px; padding: 16px; display: flex; flex-direction: column; justify-content: center; border: 1px solid #cfdee7; border-radius: 7px; background: #f4f8fb; }
.tutorial-concept-flow li:not(:last-child)::after { content: '→'; position: absolute; right: -17px; top: 50%; z-index: 1; width: 12px; color: #7398aa; transform: translateY(-50%); }
.tutorial-concept-flow small { color: #24718d; font-size: 11px; }
.tutorial-concept-flow strong { margin-top: 6px; color: #29495c; font-size: 14px; font-weight: 600; line-height: 1.7; }
table { width: 100%; min-width: 520px; border-collapse: collapse; font-size: 15px; line-height: 1.85; }
th, td { border: 1px solid #d7e3eb; text-align: left; padding: 12px 14px; vertical-align: top; }
th { font-weight: 600; background: #eff5f9; color: #204d68; }
.tutorial-note { margin: 28px 0; padding: 4px 0 4px 18px; border-left: 3px solid #d4a54d; color: #5c523c; font-size: 15px; }
.tutorial-note strong { color: #785b25; }
.tutorial-note p { margin: 8px 0 0; }
.tutorial-example, .tutorial-warning { margin: 22px 0; padding: 17px 19px; border-radius: 7px; }
.tutorial-example { border-left: 3px solid #3e9f88; background: #edf8f4; color: #345f54; }
.tutorial-warning { border-left: 3px solid #d39b47; background: #fbf6ea; color: #62533b; }
.tutorial-example strong { color: #26745f; }
.tutorial-warning strong { color: #8a6328; }
.tutorial-example p, .tutorial-warning p { margin: 7px 0 0; font-size: 15px; line-height: 1.9; }
.tutorial-check { margin: 28px 0; padding: 18px 0; border-block: 1px dashed #b5cad7; }
.tutorial-check summary { color: #1d617f; font-size: 16px; line-height: 1.9; cursor: pointer; }
.tutorial-check summary span { display: block; margin: 7px 0 0; color: #6b8291; font-size: 13px; }
.tutorial-check p { font-size: 16px; margin: 15px 0 0; }
.tutorial-recap { margin-top: 44px; padding-top: 24px; border-top: 2px solid #d0e1eb; }
.tutorial-recap > ul { padding-left: 24px; }
.tutorial-recap > ul li { margin: 14px 0; }
.tutorial-sources { margin-top: 32px; color: #607c8d; font-size: 13px; line-height: 1.9; }
.tutorial-sources p { margin: 6px 0; }
.tutorial-sources ul { list-style: none; padding: 0; display: flex; flex-wrap: wrap; gap: 4px 20px; }
.tutorial-sources a { color: #216989; text-decoration: underline; text-underline-offset: 3px; }
button:focus-visible, summary:focus-visible, a:focus-visible, .tutorial-table:focus-visible { outline: 2px solid #347b9a; outline-offset: 4px; }
@media (max-width: 600px) { .chapter-tutorial { padding: 25px 20px 30px; font-size: 16px; border-radius: 8px; } .tutorial-section h4, .tutorial-recap h4 { font-size: 21px; } .tutorial-lead { font-size: 17px; } .guided-practice { padding: 19px; } .guided-result { grid-template-columns: 1fr; } .tutorial-concept-flow { grid-template-columns: 1fr; gap: 10px; } .tutorial-concept-flow li:not(:last-child)::after { content: '↓'; top: auto; right: 50%; bottom: -14px; transform: translateX(50%); } }
@media print { .chapter-tutorial { border: 0; padding: 0; font-size: 12pt; } .reader-toolbar, .tutorial-toc { display: none; } .tutorial-section h4 { break-after: avoid; } .tutorial-table { overflow: visible; } table { min-width: 0; } .tutorial-check { display: none; } a { color: #222; } }
</style>
