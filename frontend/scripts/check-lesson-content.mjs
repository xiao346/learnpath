import assert from 'node:assert/strict'
import { readFileSync, existsSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { chapterPictures, illustrationFor } from '../src/content/knowledgeIllustrations.ts'
import { pictureReadings } from '../src/content/pictureReadings.ts'
import { chapterChecks } from '../src/content/chapterChecks.ts'

const root = new URL('../../', import.meta.url)
const catalog = readFileSync(new URL('backend/src/main/java/com/learnpath/course/ChapterContentCatalog.java', root), 'utf8')
const titles = [...catalog.matchAll(/\bentry\("([^"]+)"/g)].map(match => match[1])
assert.equal(titles.length, 58)
const expected = [...titles].sort()
for (const content of [chapterPictures, pictureReadings, chapterChecks]) {
  assert.deepEqual(Object.keys(content).sort(), expected, 'Chapter titles must match the backend catalog exactly')
}
const courses = ['数据结构与算法', '数据库原理', 'Java Web 应用开发', '计算机网络', '大学英语进阶', '人工智能导论', 'Python 数据分析基础', '软件工程与 Git 协作']
const counts = [8, 7, 9, 8, 6, 7, 7, 6]
let offset = 0
const assets = new Set()
for (const [courseIndex, course] of courses.entries()) {
  for (const title of titles.slice(offset, offset + counts[courseIndex])) {
    assert.equal(chapterPictures[title].length, 6, title)
    assert.equal(pictureReadings[title].length, 6, title)
    assert.equal(new Set(pictureReadings[title]).size, 6, `${title}: no repeated reading template`)
    assert.equal(chapterChecks[title].length, 2, title)
    for (const [index, reading] of pictureReadings[title].entries()) {
      assert.ok(reading.length >= 45, `${title} point ${index + 1}: needs a real explanation`)
      const picture = illustrationFor(course, title, index)
      assert.ok(picture && picture.tile >= 0 && picture.tile < 12, title)
      assert.ok(picture.frame.x >= 0 && picture.frame.y >= 0 && picture.frame.width > 0 && picture.frame.height > 0)
      assert.ok(picture.frame.x + picture.frame.width <= 1330 && picture.frame.y + picture.frame.height <= 1182)
      assert.ok(picture.alt.length > 8 && picture.caption.length > 20, title)
      const path = new URL(`frontend/public${picture.src}`, root)
      assert.ok(existsSync(path), fileURLToPath(path))
      assert.equal(readFileSync(path).subarray(0, 8).toString('hex'), '89504e470d0a1a0a')
      assets.add(picture.src)
    }
  }
  offset += counts[courseIndex]
}
assert.equal(assets.size, 8)
assert.equal(illustrationFor('未知课程', '未知章节', 0), null)
const view = readFileSync(new URL('../src/views/ChapterLessonView.vue', import.meta.url), 'utf8')
for (const removed of ['learning-outcomes', 'lesson.objectives', 'ChapterKnowledgeMap', 'lesson.workedExample', 'item.plainExplanation', 'item.diagram']) {
  assert.ok(!view.includes(removed), `Removed teaching template must not return: ${removed}`)
}
console.log('PASS: 58 chapters, 348 picture mappings and unique point readings, 58 answered checks, 8 local PNG atlases; obsolete blocks absent.')
