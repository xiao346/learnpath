import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { adaptiveTutorialFor, courseDepthProfiles } from '../src/content/adaptiveCourseTutorials.ts'

const expectedCourses = [
  '数据结构与算法', 'Java Web 应用开发', '大学英语进阶', '人工智能导论',
  'Python 数据分析基础', '软件工程与 Git 协作', 'HTML 与 CSS 网页设计',
  'JavaScript 网页交互', 'Vue 3 前端开发', 'FastAPI 后端开发',
]
assert.deepEqual(Object.keys(courseDepthProfiles).sort(), expectedCourses.sort())

for (const [courseIndex, courseTitle] of expectedCourses.entries()) {
  const lesson = {
    courseId: courseIndex + 1,
    courseTitle,
    chapterId: courseIndex + 101,
    chapterTitle: `${courseTitle}测试章节`,
    orderIndex: 1,
    durationMinutes: 45,
    completed: false,
    overview: '测试章节概览',
    beginnerIntro: '从一个可以观察的真实问题出发建立本章知识结构。',
    beginnerAnalogy: '先用熟悉场景建立直觉，再回到准确概念。',
    beginnerWalkthrough: ['一', '二', '三', '四'],
    objectives: ['理解', '推演', '验证'],
    keyPoints: [],
    workedExample: {
      title: '完整跟做示例',
      scenario: '用一组具体输入跟踪完整处理过程。',
      steps: Array.from({ length: 4 }, (_, index) => ({
        label: `步骤${index + 1}`,
        action: `完成操作${index + 1}`,
        explanation: `记录第${index + 1}步的输入、处理和输出，并说明结果产生的原因。`,
      })),
      result: '得到可以与预期逐项核对的结果。',
      tryIt: '替换一个条件并重新推演。',
    },
    learningPath: [],
    knowledgeAnalyses: Array.from({ length: 6 }, (_, index) => ({
      id: `analysis-${index + 1}`,
      title: `知识节点${index + 1}`,
      category: '原理与方法',
      conclusion: `第${index + 1}个知识节点给出准确结论，并明确适用前提、处理规则和结果。`,
      plainExplanation: '先通过最小示例观察变化，再回到术语解释每一步为什么会发生。',
      whyItMatters: '理解这层原因以后，换数据或出现失败时才能判断应当检查哪个环节。',
      diagram: [{ label: '输入', content: '具体数据' }, { label: '处理', content: '应用规则' }, { label: '输出', content: '核对结果' }],
      example: '把知识点放进一个真实任务，逐步写下中间状态。',
      commonMistake: '只记结论而没有验证，修正方式是增加一个边界输入。',
      checkQuestion: '你能换一组数据重新解释结果吗？',
    })),
    studySections: [],
    selfCheckQuestions: [],
    practiceTask: '独立完成一个可检查的小任务并记录验证证据。',
    previousChapterId: null,
    nextChapterId: null,
  }
  const tutorial = adaptiveTutorialFor(lesson)
  assert.equal(tutorial.sections.length, 6, courseTitle)
  assert.equal(tutorial.guidedPractice?.steps.length, 4, courseTitle)
  assert.ok(tutorial.sources.length >= 2, courseTitle)
  assert.ok(tutorial.recap.length >= 7, courseTitle)
  for (const section of tutorial.sections) {
    assert.equal(section.paragraphs.length, 3)
    assert.equal(section.conceptDiagram?.length, 3)
    assert.ok(section.example?.length > 20)
    assert.ok(section.warning?.length > 20)
    assert.ok(section.check?.answer.length > 40)
  }
}

const component = readFileSync(new URL('../src/components/ChapterTutorial.vue', import.meta.url), 'utf8')
for (const field of ['guidedPractice', 'conceptDiagram', 'visualIndex', 'section.example', 'section.warning', 'section.check']) {
  assert.ok(component.includes(field), `Deep tutorial renderer must display ${field}`)
}
console.log('PASS: 10 additional courses use six-node deep tutorials with guided practice, diagrams, examples, mistakes, checks and sources.')
