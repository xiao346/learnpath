import type { JourneyConfig, JourneyStageId } from '../services/journey'

export type JourneyPlanStep = {
  id: JourneyStageId
  title: string
  description: string
  time: string
  route: string
  courseTitle?: string
}

const projectContent: Record<string, { item: string; collection: string }> = {
  portfolio: { item: '作品', collection: '作品列表' },
  blog: { item: '文章', collection: '文章列表' },
  campus: { item: '活动', collection: '活动列表' },
}

export function requiredJourneyStageIds(config: JourneyConfig): JourneyStageId[] {
  const stages: JourneyStageId[] = ['intro', 'style', 'interaction']
  if (config.frontend === 'vue') stages.push('framework')
  if (config.backend !== 'later') {
    stages.push('backend')
    if (config.database !== 'later') stages.push('database')
  }
  stages.push('publish', 'launch')
  return stages
}

export function buildJourneyPlan(config: JourneyConfig): JourneyPlanStep[] {
  const content = projectContent[config.project] ?? projectContent.portfolio
  const backendName = config.backend === 'python' ? 'FastAPI' : 'Spring Boot'
  const databaseName = config.database === 'sqlite' ? 'SQLite' : 'MySQL'
  const steps: Record<JourneyStageId, JourneyPlanStep> = {
    intro: {
      id: 'intro', title: '你好，这是我的网站', time: '约 60 分钟', route: '/courses/first-page',
      description: `规划真实内容，读懂 HTML 结构，并在自己的首页加入第一条${content.item}。`,
    },
    style: {
      id: 'style', title: '给网站换件衣服', time: '约 90 分钟', route: '/courses/style-workshop',
      description: `完成文字层级、${content.item}卡片和手机适配，再把 CSS 写回项目。`,
    },
    interaction: {
      id: 'interaction', title: '让按钮真的有反应', time: '约 2 小时', route: '/courses/interaction-workshop',
      description: `为${content.collection}完成一个核心操作，并用控制台检查状态变化。`,
    },
    framework: {
      id: 'framework', title: '把页面装进 Vue', time: '5 个章节 + 项目实做', route: '/courses/project-stage/framework',
      description: `学习组件与响应式数据，再把现有${content.collection}迁移成 Vue 组件。`, courseTitle: 'Vue 3 前端开发',
    },
    backend: {
      id: 'backend', title: '给网站接上大脑', time: '5 个章节 + 项目实做', route: '/courses/project-stage/backend',
      description: `学习 ${backendName}，再让页面从项目专属接口读取${content.item}。`,
      courseTitle: config.backend === 'python' ? 'FastAPI 后端开发' : 'Java Web 应用开发',
    },
    database: {
      id: 'database', title: '让内容记得住', time: '7 个章节 + 项目实做', route: '/courses/project-stage/database',
      description: `学习数据建模与 SQL，再用 ${databaseName} 保存和读取真实${content.item}。`, courseTitle: '数据库原理',
    },
    publish: {
      id: 'publish', title: '把完整版本发给朋友', time: '约 60–90 分钟', route: '/courses/publish-workshop',
      description: config.backend === 'later'
        ? '保存可靠版本、准备静态文件，发布后从另一个设备验收。'
        : `完成前后端联调，发布页面与 ${backendName} 服务，再验证线上数据链路。`,
    },
    launch: {
      id: 'launch', title: '上线前的最后巡检', time: '约 1.5 小时', route: '/courses/launch-workshop',
      description: '从访客视角检查内容、手机布局、核心功能和公开地址，再完成一次修订。',
    },
  }
  return requiredJourneyStageIds(config).map((id) => steps[id])
}

export function courseTitleForStage(config: JourneyConfig, stageId: JourneyStageId) {
  return buildJourneyPlan(config).find((stage) => stage.id === stageId)?.courseTitle ?? null
}
