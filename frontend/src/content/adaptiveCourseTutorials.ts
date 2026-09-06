import type { ChapterLesson } from '../services/api'
import type { ChapterTutorial } from './chapterTutorials'

type DepthProfile = {
  learningMethod: string
  sources: { label: string; href: string }[]
  illustrated: boolean
}

export const courseDepthProfiles: Record<string, DepthProfile> = {
  '数据结构与算法': {
    learningMethod: '在一组可以手算的小数据上跟踪结构变化，再比较复杂度与边界输入',
    illustrated: true,
    sources: [
      { label: 'VisuAlgo 数据结构可视化', href: 'https://visualgo.net/zh' },
      { label: 'LeetCode 学习计划', href: 'https://leetcode.cn/studyplan/' },
    ],
  },
  'Java Web 应用开发': {
    learningMethod: '沿一次真实 HTTP 请求追踪控制器、服务、数据层和异常响应',
    illustrated: true,
    sources: [
      { label: 'Spring Boot Reference', href: 'https://docs.spring.io/spring-boot/index.html' },
      { label: 'Spring Data JPA Reference', href: 'https://docs.spring.io/spring-data/jpa/reference/' },
    ],
  },
  '大学英语进阶': {
    learningMethod: '先识别表达任务和篇章功能，再替换主题、听众与证据完成一次输出',
    illustrated: true,
    sources: [
      { label: 'British Council · Skills', href: 'https://learnenglish.britishcouncil.org/skills' },
      { label: 'Purdue OWL · Academic Writing', href: 'https://owl.purdue.edu/owl/general_writing/academic_writing/index.html' },
    ],
  },
  '人工智能导论': {
    learningMethod: '写清输入、模型处理、输出和评价指标，再检查失败样本与使用边界',
    illustrated: true,
    sources: [
      { label: 'Google 机器学习速成课程', href: 'https://developers.google.com/machine-learning/crash-course?hl=zh-cn' },
      { label: 'NIST AI Risk Management Framework', href: 'https://www.nist.gov/itl/ai-risk-management-framework' },
    ],
  },
  'Python 数据分析基础': {
    learningMethod: '运行最小代码、查看中间值与数据形状，再用异常输入复核结果',
    illustrated: true,
    sources: [
      { label: 'Python 官方中文教程', href: 'https://docs.python.org/zh-cn/3/tutorial/' },
      { label: 'NumPy User Guide', href: 'https://numpy.org/doc/stable/user/' },
    ],
  },
  '软件工程与 Git 协作': {
    learningMethod: '画出操作前后的提交与流程状态，并用日志、差异和测试结果留下证据',
    illustrated: true,
    sources: [
      { label: 'Pro Git 中文版', href: 'https://git-scm.com/book/zh/v2' },
      { label: 'GitHub Actions 中文文档', href: 'https://docs.github.com/zh/actions' },
    ],
  },
  'HTML 与 CSS 网页设计': {
    learningMethod: '在浏览器中观察结构与样式变化，并用开发者工具解释最终布局',
    illustrated: false,
    sources: [
      { label: 'MDN · HTML 内容结构', href: 'https://developer.mozilla.org/zh-CN/docs/Learn_web_development/Core/Structuring_content' },
      { label: 'MDN · CSS 样式基础', href: 'https://developer.mozilla.org/zh-CN/docs/Learn_web_development/Core/Styling_basics' },
    ],
  },
  'JavaScript 网页交互': {
    learningMethod: '跟踪事件发生前后的状态和 DOM 变化，并补齐等待、失败与空数据场景',
    illustrated: false,
    sources: [
      { label: 'MDN · JavaScript 脚本', href: 'https://developer.mozilla.org/zh-CN/docs/Learn_web_development/Core/Scripting' },
      { label: 'MDN · Fetch API', href: 'https://developer.mozilla.org/zh-CN/docs/Web/API/Fetch_API/Using_Fetch' },
    ],
  },
  'Vue 3 前端开发': {
    learningMethod: '从响应式状态出发追踪组件渲染、事件传递、路由和接口状态',
    illustrated: false,
    sources: [
      { label: 'Vue 3 官方中文指南', href: 'https://cn.vuejs.org/guide/introduction.html' },
      { label: 'Vue Router 中文文档', href: 'https://router.vuejs.org/zh/' },
    ],
  },
  'FastAPI 后端开发': {
    learningMethod: '从一条请求开始核对路由、校验、业务、数据库和响应契约',
    illustrated: false,
    sources: [
      { label: 'FastAPI 官方中文教程', href: 'https://fastapi.tiangolo.com/zh/tutorial/' },
      { label: 'Pydantic Documentation', href: 'https://docs.pydantic.dev/latest/' },
    ],
  },
}

export function adaptiveTutorialFor(lesson: ChapterLesson): ChapterTutorial {
  const profile = courseDepthProfiles[lesson.courseTitle]
  if (!profile) throw new Error(`缺少课程深度教学配置：${lesson.courseTitle}`)

  return {
    lead: `${lesson.beginnerIntro} 本章会把结论拆成六个可以观察和验证的知识节点，每一步都要说清对象、规则、结果与边界。`,
    scenario: `${lesson.beginnerAnalogy} 阅读时始终围绕“${lesson.workedExample.scenario}”推进，并采用这门课的验证方法：${profile.learningMethod}。`,
    guidedPractice: {
      title: lesson.workedExample.title,
      scenario: lesson.workedExample.scenario,
      steps: lesson.workedExample.steps,
      result: lesson.workedExample.result,
      tryIt: lesson.workedExample.tryIt,
    },
    sections: lesson.knowledgeAnalyses.map((item, index) => ({
      id: `${item.id}-${lesson.chapterId}`,
      title: item.title,
      paragraphs: [item.conclusion, item.plainExplanation, item.whyItMatters],
      conceptDiagram: item.diagram,
      visualIndex: profile.illustrated ? index : undefined,
      example: item.example,
      warning: item.commonMistake,
      check: {
        question: item.checkQuestion,
        answer: `${item.conclusion} ${item.plainExplanation}`,
      },
    })),
    recap: [
      ...lesson.knowledgeAnalyses.map(item => item.conclusion),
      `完成标准：${lesson.practiceTask}`,
    ],
    sources: profile.sources,
  }
}
