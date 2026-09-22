import assert from 'node:assert/strict'
import { buildJourneyPlan } from '../src/content/journeyPlan.ts'
import { getStageGuidance, helpForStage, projectGoals } from '../src/content/beginnerGuidance.ts'
import { buildFirstPageDocument } from '../src/content/firstPageDocument.ts'

let routeCount = 0
for (const project of ['portfolio', 'blog', 'campus']) {
  assert.ok(projectGoals[project].first)
  for (const frontend of ['vanilla', 'vue']) {
    for (const backend of ['later', 'java', 'python']) {
      for (const database of backend === 'later' ? ['later'] : ['later', 'mysql', 'sqlite']) {
        const config = { project, frontend, backend, database }
        const plan = buildJourneyPlan(config)
        assert.equal(plan.some(step => step.id === 'framework'), frontend === 'vue')
        assert.equal(plan.some(step => step.id === 'backend'), backend !== 'later')
        assert.equal(plan.some(step => step.id === 'database'), backend !== 'later' && database !== 'later')
        assert.deepEqual(plan.slice(-2).map(step => step.id), ['publish', 'launch'])
        for (const step of plan) {
          const guide = getStageGuidance(config, step.id)
          for (const field of ['why', 'before', 'after', 'need', 'enough', 'later']) assert.ok(guide[field], `${project}/${frontend}/${backend}/${database}/${step.id}: missing ${field}`)
          assert.ok(helpForStage(step.id).every(item => item.steps.length >= 3))
          const text = Object.values(guide).join(' ')
          if (backend === 'python') assert.ok(!text.includes('Spring Boot'))
          if (backend === 'java') assert.ok(!text.includes('FastAPI'))
          if (database === 'sqlite') assert.ok(!text.includes('MySQL'))
          if (database === 'mysql') assert.ok(!text.includes('SQLite'))
        }
        routeCount++
      }
    }
  }
}
assert.equal(routeCount, 42)

// Text typed by a beginner must remain text in the exported, standalone HTML.
const html = buildFirstPageDocument({ name: '<script>alert(1)</script>', introduction: 'A & B "你好"', interest: '</p><img src=x onerror=alert(1)>', theme: 'blue' }, '我的第一件作品')
assert.ok(html.startsWith('<!doctype html>'))
assert.ok(html.includes('<meta charset="UTF-8">'))
assert.ok(html.includes('&lt;script&gt;alert(1)&lt;/script&gt;'))
assert.ok(html.includes('A &amp; B &quot;你好&quot;'))
assert.ok(html.includes('&lt;/p&gt;&lt;img'))
assert.ok(!html.includes('<script>') && !html.includes('<img'))
assert.ok(!html.includes('style.css'), 'First HTML file must not depend on a CSS file that does not exist yet')
assert.ok(html.includes('id="first-item"') && html.includes('href="#first-item"'))
console.log(`Beginner guidance: ${routeCount} routes covered; standalone HTML export and text escaping passed.`)
