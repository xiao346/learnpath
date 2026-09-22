import type { JourneyConfig, JourneyStageId } from '../services/journey'

export const projectGoals: Record<string, { name: string; audience: string; purpose: string; first: string }> = {
  portfolio: { name: '个人作品集', audience: '想认识你的同学、社团伙伴', purpose: '让别人看到你的兴趣和亲手做过的东西。', first: '写一段自我介绍，放上一张摄影、一幅画或一个小作品。' },
  blog: { name: '兴趣博客', audience: '和你有共同爱好的朋友', purpose: '把游戏心得、观影感受或生活发现分享出去。', first: '选一个你愿意聊的话题，写下第一篇短文章。' },
  campus: { name: '校园信息站', audience: '刚到学校、还不熟悉校园的同学', purpose: '让同学能找到活动时间、地点和参加方式。', first: '整理一条真实活动，写清时间、地点和参加方式。' },
}

export type StageGuidance = { why: string; before: string; after: string; need: string; enough: string; later: string }

export function getStageGuidance(config: Partial<JourneyConfig>, stage: JourneyStageId): StageGuidance {
  const item = config.project === 'blog' ? '文章' : config.project === 'campus' ? '活动' : '作品'
  const backend = config.backend === 'python' ? 'FastAPI' : 'Spring Boot'
  const database = config.database === 'sqlite' ? 'SQLite' : 'MySQL'
  const guides: Record<JourneyStageId, StageGuidance> = {
    intro: { why: `先把想分享的${item}放到网页上，你就有了一个可以不断改进的起点。`, before: '只有一个想法，还没有网页', after: `在浏览器里打开自己的介绍和第一条${item}`, need: '创建文件、保存与刷新；h1 标题、p 段落、section 内容区、a 链接。', enough: '在自己的 HTML 文件里改一句话，保存后在浏览器看到它。', later: '精美排版、框架、后端和数据库。今天先让内容出现。' },
    style: { why: `访客需要一眼找到${item}标题和重点，排版能帮助他们读懂内容。`, before: '内容已经有了，但挤在一起', after: `${item}有清楚的标题、留白和手机布局`, need: '连接 CSS 文件；字号、颜色、间距、Flex 和简单的媒体查询。', enough: '一张卡片在电脑和手机宽度下都能读清，没有横向溢出。', later: '复杂动画、完整设计系统、所有 CSS 属性。' },
    interaction: { why: `${item}变多后，访客需要按兴趣筛选，或者点开详情。`, before: '页面只能看，点击没有变化', after: `点击按钮能筛选或展开${item}`, need: '找到元素、监听点击、记录状态、更新页面。', enough: '做通一种核心操作，连续点击和切换后结果仍然正确。', later: '登录、支付、复杂状态管理。先把一个按钮做通。' },
    framework: { why: `${item}卡片越写越多，复制 HTML 会让修改变麻烦。Vue 可以让它们共用一个组件。`, before: '新增内容要复制整段页面代码', after: `往数组加一条${item}，页面自动多一张卡片`, need: '启动 Vue 项目、ref、v-for、组件和 Props。', enough: '用一个组件显示列表，保留上一站已做好的交互。', later: '大型组件库、全局状态库、服务端渲染。' },
    backend: { why: `让${item}数据从独立服务送到页面，为以后管理内容做准备。`, before: '数据直接写在页面代码里', after: `页面从 ${backend} 接口读取${item}`, need: `${backend} 的一个 GET 接口、JSON、fetch，以及加载失败提示。`, enough: '接口能返回两条数据，前端能显示；关闭服务后能看到错误提示。', later: '账号权限、微服务、高并发。先理解一次请求和响应。' },
    database: { why: `服务里的临时数据会在重启时丢失，把${item}存进数据库才能保留下来。`, before: '内容只存在程序的临时数据里', after: `重启服务后仍能从 ${database} 读到${item}`, need: `${database} 的一张表、主键、插入与查询，以及接口连接数据库。`, enough: '新增一条真实数据，重启后通过页面仍能找到它。', later: '复杂联表、索引优化、分库分表。' },
    publish: { why: '电脑里的文件只有你能打开。发布后，朋友才可以通过网址访问。', before: '只在自己的电脑上运行', after: '得到朋友也能打开的网址', need: config.backend === 'later' ? (config.frontend === 'vue' ? '保存代码、构建 dist、静态托管、检查资源路径。' : '保存代码、检查 index.html 和资源路径、静态托管。') : `部署页面和 ${backend} 服务${config.database === 'later' ? '' : `、配置 ${database}`}，连接公开 API。`, enough: config.backend === 'later' ? '用另一台设备打开公开网址，页面与交互都能工作。' : '从公开页面读到接口数据，在另一台设备走通核心操作。', later: '自动化部署、容器编排、复杂云架构。' },
    launch: { why: '第一次访问的人不知道你的网站怎么用。他们的体验能帮你发现自己忽略的问题。', before: '网站已发布，尚未请别人试用', after: '修好一个真实问题，能介绍和分享自己的作品', need: '手机访问、链接检查、一次朋友试用、一段作品说明。', enough: '朋友能独立完成核心操作，你根据反馈完成一次修订。', later: '继续加功能前，先想想自己最喜欢做哪一部分。' },
  }
  return guides[stage]
}

export type HelpItem = { title: string; steps: string[] }
const fileHelp: HelpItem[] = [
  { title: '双击文件后是记事本，或者显示一堆代码', steps: ['在文件资源管理器中开启“查看 → 显示 → 文件扩展名”（Windows 10 在“查看”中勾选）。确认名字是 index.html，不是 index.html.txt。', '右键 index.html → 打开方式 → 选择浏览器。要编辑代码时，再用“打开方式”选择记事本或代码编辑器。', '浏览器负责看效果，编辑器负责改文件。同一个文件可以同时在两边打开。'] },
  { title: '改了文字，网页却没有变化', steps: ['回到编辑器按 Ctrl + S 保存；Mac 使用 Command + S。', '切到浏览器刷新，检查地址栏指向的文件是否就是刚才编辑的那个，注意 Downloads 和桌面里的同名文件。', '本地 HTML 文件不需要启动服务。如果运行的是 Vue 项目，请打开终端打印的本地地址。'] },
  { title: '图片不显示，样式也没有生效', steps: ['检查图片或 style.css 是否真的在项目文件夹中，名字、大小写和后缀要与代码完全一致。', '同一层的图片写 src="photo.jpg"，放在 images 文件夹里则写 src="images/photo.jpg"。不要只写自己电脑的绝对路径。', 'CSS 需要在 HTML 的 head 中添加 <link rel="stylesheet" href="style.css">；第一站的原始 HTML 没有样式是正常的。'] },
]
const terminalHelp: HelpItem = { title: '命令应该输在哪里？提示找不到文件怎么办？', steps: ['在代码编辑器中打开整个项目文件夹，再选择“终端 → 新建终端”。命令输入在终端，不是浏览器地址栏或源代码文件里。', 'Windows 输入 dir，Mac 输入 ls，查看当前位置。npm 命令应在能看到 package.json 的目录运行；Java、Python 命令按这一站的项目目录执行。', '看到“不是内部或外部命令”通常表示对应工具尚未安装或终端需要重新打开。回到这一站的准备步骤确认所需工具。'] }
const apiHelp: HelpItem = { title: '页面一直加载，或者提示请求失败', steps: ['先看后端终端有没有启动成功，打开本阶段给出的接口地址，确认它返回 JSON。', '检查前端请求的主机、端口和路径与接口一致。浏览器开发者工具的“网络 / Network”中能看到失败请求。', '404 先查路径；连接被拒绝先查服务与端口；跨域错误查后端允许的前端地址。记录完整报错，避免只反复刷新。'] }

export function helpForStage(stage: JourneyStageId): HelpItem[] {
  if (stage === 'intro' || stage === 'style') return fileHelp
  if (stage === 'interaction') return [fileHelp[1]!, { title: '按钮没有反应', steps: ['确认 script 的路径正确，代码在页面元素创建之后运行（例如使用 defer）。', '检查 querySelector 的选择器是否与 HTML 中的 id 或 class 一致。', '打开开发者工具的“控制台 / Console”，先修复第一条红色报错，再点击重试。'] }, fileHelp[2]!]
  if (stage === 'framework') return [terminalHelp, fileHelp[1]!]
  if (stage === 'backend') return [terminalHelp, apiHelp]
  if (stage === 'database') return [apiHelp, { title: '数据重启后不见了', steps: ['确认写入和查询连接同一个数据库。SQLite 检查数据库文件路径，MySQL 检查库名。', '先在数据库直接查询刚写入的数据。如果不存在，检查插入操作与事务是否成功。', '确认接口读取数据库，而不是仍然返回示例数组；检查启动脚本是否每次重建表。'] }]
  return [{ title: '我能打开，朋友却打不开', steps: ['分享的不能是 file://、localhost 或 127.0.0.1 地址，这些地址指向访问者自己的电脑。', '使用发布平台提供的公开网址，在手机关闭 Wi-Fi 后测试。', '若首页能打开但图片或接口失败，检查资源路径、生产 API 地址与 HTTPS 配置。'] }, apiHelp]
}
