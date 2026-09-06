<script setup lang="ts">
import { computed } from 'vue'

type SceneKind = 'sequence' | 'compare' | 'hierarchy' | 'network' | 'table' | 'timeline' | 'branch' | 'chart' | 'document' | 'conversation' | 'code'
type Scene = { kind: SceneKind; title: string; caption: string; labels: string[] }

const props = defineProps<{ chapterTitle: string; courseTitle: string }>()

const scenes: Record<string, Scene> = {
  '算法与复杂度入门': { kind: 'chart', title: '同样找一个名字，数据越多差距越大', caption: '顺序查找逐项增加；二分查找每次砍掉一半。', labels: ['10 个数据', '100 个数据', '1000 个数据', '顺序 1000 次', '二分约 10 次'] },
  '线性表与顺序存储': { kind: 'sequence', title: '数组是一排连续编号的格子', caption: '按下标能直接定位；中间插入时，右侧元素需要依次后移。', labels: ['0 小明', '1 小红', '2 小刚', '插入小李', '元素右移'] },
  '栈、队列与应用': { kind: 'compare', title: '盘子堆与食堂队伍', caption: '栈从同一端放入和取出；队列从一端进入、另一端离开。', labels: ['栈：C 先出', 'A', 'B', 'C', '队列：A 先出'] },
  '二叉树与遍历算法': { kind: 'hierarchy', title: '遍历顺序取决于什么时候访问根节点', caption: '这棵小树足够演示前序 A-B-C、中序 B-A-C、后序 B-C-A。', labels: ['A 根', 'B 左孩子', 'C 右孩子', '根→左→右'] },
  '图的表示与遍历': { kind: 'network', title: '站点是顶点，线路是边', caption: 'BFS 一层层扩散；DFS 沿一条路走到底再回头。', labels: ['A 起点', 'B', 'C', 'D', 'BFS：A-B-C-D'] },
  '查找算法': { kind: 'sequence', title: '二分查找怎样排除一半', caption: '在有序数据中先看中间 8；目标 12 更大，所以只保留右半边。', labels: ['2', '5', '8 中间', '12 目标', '20'] },
  '排序算法': { kind: 'chart', title: '插入排序像整理手中的扑克牌', caption: '每拿一张牌，都把它插进左侧已经排好的区域。', labels: ['3', '1', '2', '1 2', '1 2 3'] },
  '综合实践与复盘': { kind: 'network', title: '一个任务管理器会组合多种结构', caption: '按编号查找与按优先级取出是两类操作，应使用不同工具。', labels: ['新任务', '哈希表', '优先队列', '编号查询', '最高优先级'] },

  '数据库系统概述': { kind: 'table', title: '把零散档案放进结构统一的数据表', caption: '一行代表一名学生，一列代表一种属性，学号负责唯一标识。', labels: ['学号', '姓名', '专业', '20240001', '林知夏', '计算机'] },
  '关系模型与关系代数': { kind: 'table', title: '选择行、投影列、连接两张表', caption: '先按条件留下需要的行，再取需要的列，最后按学号拼回成绩。', labels: ['学生表', '成绩表', '学号连接', '姓名', '分数', '林知夏 92'] },
  'SQL 数据查询': { kind: 'sequence', title: '数据库实际按什么顺序理解 SELECT', caption: '书写从 SELECT 开始，但逻辑处理通常先确定数据来源，再过滤和分组。', labels: ['FROM 找表', 'WHERE 过滤', 'GROUP 分组', 'SELECT 取列', 'ORDER 排序'] },
  '数据库完整性': { kind: 'compare', title: '约束像数据进入仓库前的检票口', caption: '合法数据放行；空姓名、重复学号和 120 分会被挡在表外。', labels: ['待写入数据', 'NOT NULL', 'UNIQUE', 'CHECK', '合法行入库'] },
  '关系数据库设计': { kind: 'network', title: '把重复的大宽表拆成可关联的小表', caption: '客户电话只保存一处，订单通过客户编号建立关系。', labels: ['客户表', '订单表', '商品表', '订单明细', '外键'] },
  '事务与并发控制': { kind: 'compare', title: '转账的扣款和加款必须共同成功', caption: '任一步失败就回滚到起点，不能留下只扣款未到账的中间状态。', labels: ['A：500 元', '扣 100', '事务边界', '加 100', 'B：300 元'] },
  '索引优化与课程实践': { kind: 'hierarchy', title: 'B+ 树索引先缩小范围，再定位数据行', caption: '查询不必从第一行扫到最后一行，但新增数据需要同步维护目录。', labels: ['根目录', '1—50', '51—100', '订单 68', '数据行'] },

  '现代 Web 开发概览': { kind: 'sequence', title: '点击课程后，一次请求走过的完整路线', caption: '浏览器负责展示，控制器接收请求，服务处理规则，数据库保存数据。', labels: ['浏览器', 'HTTP 请求', '控制器', '业务服务', 'MySQL'] },
  'Spring Boot 快速入门': { kind: 'code', title: '最小可运行的 Spring Boot 接口', caption: '启动应用后访问 /hello；控制器收到 GET 请求并返回“你好”。', labels: ['@RestController', '@GetMapping', '启动应用', 'GET /hello', '200 你好'] },
  'RESTful API 设计': { kind: 'table', title: '同一个资源，用方法表达不同动作', caption: 'URI 指向课程，GET、POST、PUT、DELETE 表达查询、新增、替换和删除。', labels: ['方法', '地址', '含义', 'GET', '/courses/1', '查询课程'] },
  '数据持久化': { kind: 'sequence', title: 'Java 对象怎样变成数据库中的一行', caption: '实体描述映射，Repository 执行保存，事务保证一组修改的边界。', labels: ['Course 对象', '@Entity 映射', 'Repository', 'INSERT', 'course 表'] },
  '参数校验与异常处理': { kind: 'compare', title: '错误输入要在系统入口被清楚拦下', caption: '校验器指出具体字段，异常处理器统一返回状态码和可读消息。', labels: ['空姓名', '@NotBlank', '校验失败', '400', '姓名不能为空'] },
  '身份认证与权限控制': { kind: 'sequence', title: '先确认是谁，再判断能做什么', caption: '令牌证明登录身份；角色和资源归属决定请求是否放行。', labels: ['账号密码', '登录成功', '访问令牌', '权限检查', '允许 / 403'] },
  'Redis 缓存实践': { kind: 'network', title: '课程详情的两次读取路线不同', caption: '第一次缓存未命中才查 MySQL；第二次直接从 Redis 返回。', labels: ['浏览器', '应用', 'Redis', 'MySQL', '回填缓存'] },
  '接口测试': { kind: 'compare', title: '同一个接口要同时验证成功与失败', caption: '测试准备输入、发送请求，再逐项检查状态码和响应内容。', labels: ['课程 1', 'GET 请求', '200 标题正确', '课程 999', '404'] },
  '部署与综合实战': { kind: 'sequence', title: '从构建产物到可检查的线上服务', caption: '配置、迁移、启动、健康检查和回滚缺一不可。', labels: ['构建', '注入配置', '数据库迁移', '健康检查', '监控 / 回滚'] },

  '网络体系结构': { kind: 'hierarchy', title: '发送时逐层包装，接收时逐层拆开', caption: '应用数据向下经过传输层、网际层和链路层，接收端反向处理。', labels: ['HTTP 数据', 'TCP 段', 'IP 包', '以太网帧', '比特'] },
  '物理层基础': { kind: 'chart', title: '文件大小、带宽与传输时间', caption: '100 MB 约等于 800 Mb；在理想 100 Mbps 链路上至少需要 8 秒。', labels: ['100 MB', '× 8', '800 Mb', '÷100 Mbps', '8 秒'] },
  '数据链路层': { kind: 'network', title: '交换机通过学习 MAC 地址减少泛洪', caption: '第一次不知道 B 在哪会多端口发送，收到回复后记住 B 所在端口。', labels: ['电脑 A', '交换机', '电脑 B', '电脑 C', 'MAC 地址表'] },
  '网络层与 IP': { kind: 'table', title: '/24 把地址分成网络部分和主机部分', caption: '192.168.1.10/24 的前 24 位是网络前缀，网络地址为 192.168.1.0。', labels: ['192', '168', '1', '10', '网络前缀', '主机号'] },
  '路由选择': { kind: 'network', title: '路由器逐跳选择下一站', caption: '多条路都匹配时优先选择前缀更长、范围更具体的路由。', labels: ['源主机', '路由器 A', '路由器 B', '目标网络', '最长前缀'] },
  'TCP 与 UDP': { kind: 'conversation', title: 'TCP 三次握手确认双方收发能力', caption: '客户端发 SYN，服务端回 SYN-ACK，客户端再发 ACK，连接才建立。', labels: ['客户端', 'SYN →', '← SYN-ACK', 'ACK →', '连接建立'] },
  '应用层协议': { kind: 'sequence', title: '输入域名后网页是怎样回来的', caption: '先通过 DNS 找到 IP，再建立连接并发送 HTTP 请求。', labels: ['输入域名', 'DNS 查 IP', '建立连接', 'GET /', '200 页面'] },
  '网络安全基础': { kind: 'sequence', title: 'HTTPS 同时解决身份、密钥和加密传输', caption: '浏览器先验证证书，再协商会话密钥，最后加密交换网页数据。', labels: ['访问域名', '验证证书', '协商密钥', '加密数据', '安全连接'] },

  'Campus Life': { kind: 'conversation', title: '先用三种句型完成校园沟通', caption: '介绍自己、描述日常、礼貌问路，替换关键信息就能开口。', labels: ['I am…', 'I usually…', 'Could you tell me…?', '时间', '地点'] },
  'Technology and Society': { kind: 'compare', title: '观点要同时有理由、例子和限制', caption: 'I think 给出立场，because 解释理由，for example 提供证据，however 补充另一面。', labels: ['观点', 'because 理由', 'for example', 'however', '完整论证'] },
  'Academic Reading': { kind: 'document', title: '先看文章地图，再进入句子细节', caption: '标题和段首句帮助定位主旨，数字、例子和转折词帮助核对。', labels: ['标题关键词', '段首句', '段落功能', '证据', '主旨'] },
  'Presentation Skills': { kind: 'timeline', title: '短演讲需要让听众始终知道走到哪里', caption: '开头预告路线，中间用路标词分段，结尾回收最重要结论。', labels: ['开场', 'First', 'Next', 'Finally', 'In short'] },
  'Critical Writing': { kind: 'document', title: 'PEEL 段落把观点与证据扣在一起', caption: '观点、证据、解释、回扣各有职责，少一步都会让论证松动。', labels: ['P 观点', 'E 证据', 'E 解释', 'L 回扣', '完整段落'] },
  'Final Project': { kind: 'timeline', title: '把大主题缩成一个可以回答的问题', caption: '先限定对象与范围，再找资料、写报告、做展示并根据反馈修改。', labels: ['研究问题', '资料', '提纲', '报告', '展示'] },

  '人工智能的过去与现在': { kind: 'timeline', title: '人工智能从规则系统走向数据学习', caption: '不同阶段解决问题的方式不同，能力边界也不同。', labels: ['规则程序', '专家系统', '机器学习', '深度学习', '生成式 AI'] },
  '问题求解与搜索': { kind: 'network', title: '把迷宫画成“状态 + 动作”的搜索图', caption: 'BFS 用队列逐层扩展，第一次到达出口时得到最少步数。', labels: ['起点', '可走格子', '已访问', '队列', '出口'] },
  '知识表示': { kind: 'network', title: '事实可以拆成实体—关系—实体', caption: '三元组组成知识图谱，规则还能从已有事实推出新事实。', labels: ['小明', '选修', '数据库', '属于', '计算机基础'] },
  '机器学习基础': { kind: 'chart', title: '训练集学习，验证集调节，测试集验收', caption: '测试集必须留到最后，避免把答案提前泄露给模型。', labels: ['100 条数据', '训练 70', '验证 15', '测试 15', '最终指标'] },
  '神经网络初步': { kind: 'hierarchy', title: '神经网络把输入逐层变成预测', caption: '损失衡量预测误差，反向传播告诉参数应向哪个方向调整。', labels: ['像素输入', '隐藏层 1', '隐藏层 2', '0—9 概率', '损失'] },
  '自然语言处理': { kind: 'sequence', title: '文字先被切分，再变成数字参与计算', caption: '分词、向量、模型和输出构成最小的文本分类流程。', labels: ['这门课很好', '分词', '词向量', '分类模型', '正面 0.92'] },
  '负责任的人工智能': { kind: 'compare', title: '不能只看总准确率，还要分群体检查', caption: '比较不同群体的通过率和误拒率，才能发现被平均数掩盖的问题。', labels: ['群体 A', '通过率 82%', '误拒率 8%', '群体 B', '差异检查'] },

  'Python 与开发环境': { kind: 'code', title: '每个项目使用自己的 Python 工具箱', caption: '创建并激活虚拟环境后安装依赖，避免不同项目互相影响。', labels: ['python -m venv', 'activate', 'pip install', 'main.py', '运行成功'] },
  '数据类型与控制流程': { kind: 'sequence', title: '数据沿着循环和条件一步步变化', caption: 'for 每次取一个分数，if 判断是否及格，并累计总分。', labels: ['[70,85,92]', 'for 取一项', 'if >=60', '累加', '平均 82.3'] },
  '函数与模块': { kind: 'code', title: '函数把输入经过处理变成输出', caption: '参数进入函数体，return 把结果交回调用位置，测试负责检查边界。', labels: ['scores 参数', '检查空列表', 'sum / len', 'return', '85.0'] },
  'NumPy 数组计算': { kind: 'table', title: '向量化一次处理整块数组', caption: 'a * 2 不是把列表重复两遍，而是把数组中的每个元素乘以 2。', labels: ['1', '2', '3', '× 2', '2 4 6', 'shape=(3,)'] },
  'Pandas 数据处理': { kind: 'table', title: '从原始 CSV 到分组结果', caption: '先查看与清洗，再筛选金额，最后按商品汇总销售额。', labels: ['商品', '金额', 'A', '120', 'B', '80'] },
  '数据可视化': { kind: 'chart', title: '图形要匹配你想回答的问题', caption: '比较类别用条形图，看时间趋势用折线图，看分布用直方图。', labels: ['比较', '趋势', '分布', '条形图', '折线图'] },
  '综合分析项目': { kind: 'timeline', title: '一份可复现分析的完整流水线', caption: '别人从原始数据开始运行，也应得到相同的图表和结论。', labels: ['问题', '数据字典', '清洗', '分析', '报告'] },

  '软件过程与需求': { kind: 'timeline', title: '需求要一路追踪到实现和验收', caption: '每个目标都应能找到对应用户故事、代码变更和测试证据。', labels: ['业务目标', '用户故事', '验收标准', '实现', '测试'] },
  'Git 核心模型': { kind: 'sequence', title: 'Git 的三个区域不是一回事', caption: '编辑发生在工作区，git add 放入暂存区，git commit 形成仓库快照。', labels: ['工作区', 'git add', '暂存区', 'git commit', '本地仓库'] },
  '分支与合并': { kind: 'branch', title: '功能分支从主线分开，再安全汇合', caption: '分支只是指向提交的可移动指针；合并会把两条历史连接起来。', labels: ['main', 'feature', '提交 A', '提交 B', 'merge'] },
  '代码评审与冲突处理': { kind: 'document', title: '冲突标记只是提示，最终要理解双方意图', caption: '保留正确语义、删除标记、重新测试，再把解决结果加入暂存区。', labels: ['<<<<<<< ours', '共同修改', '=======', '另一版本', '>>>>>>> theirs'] },
  '自动化测试与持续集成': { kind: 'sequence', title: '每次推送都自动走过质量检查门', caption: '安装依赖、构建、测试任一步失败，流水线都应停止并给出日志。', labels: ['git push', '安装', '构建', '测试', '通过 / 失败'] },
  '版本发布实践': { kind: 'timeline', title: '版本号说明变化大小，标签固定发布位置', caption: '发布还要包含变更说明、验证结果、监控和回滚入口。', labels: ['1.0.0', '新增功能', '1.1.0', '发布标签', '回滚点'] },
}

const scene = computed<Scene>(() => scenes[props.chapterTitle] ?? {
  kind: 'sequence',
  title: `${props.chapterTitle}学习路线`,
  caption: '先认识对象，再跟随步骤操作，最后检查结果。',
  labels: ['看场景', '认对象', '跟步骤', '看结果', '自己做'],
})

const safeLabels = computed(() => [...scene.value.labels, '', '', '', '', ''])
</script>

<template>
  <figure class="lesson-illustration">
    <div class="illustration-copy"><span>本章第一张图</span><h3>{{ scene.title }}</h3><p>{{ scene.caption }}</p></div>
    <svg viewBox="0 0 920 360" role="img" :aria-label="`${chapterTitle}：${scene.title}`">
      <defs>
        <linearGradient id="panel" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#172861"/><stop offset="1" stop-color="#0c173d"/></linearGradient>
        <linearGradient id="accent" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#72ddff"/><stop offset="1" stop-color="#8a79ff"/></linearGradient>
        <filter id="glow"><feGaussianBlur stdDeviation="7" result="blur"/><feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
        <marker id="arrow" markerWidth="10" markerHeight="10" refX="8" refY="5" orient="auto"><path d="M0 0L10 5L0 10Z" fill="#7188d9"/></marker>
      </defs>

      <g v-if="scene.kind === 'sequence' || scene.kind === 'timeline'">
        <path d="M105 180H815" stroke="#526aa9" stroke-width="5" stroke-linecap="round" marker-end="url(#arrow)"/>
        <g v-for="(label, i) in scene.labels.slice(0, 5)" :key="label" :transform="`translate(${105 + i * 170} 180)`">
          <circle r="42" fill="url(#panel)" stroke="#6b82cf" stroke-width="2"/><circle r="18" fill="url(#accent)" opacity=".9" filter="url(#glow)"/>
          <text y="76" text-anchor="middle" class="svg-label">{{ label }}</text><text y="6" text-anchor="middle" class="svg-index">{{ i + 1 }}</text>
        </g>
      </g>

      <g v-else-if="scene.kind === 'compare'">
        <path d="M460 66V286M322 285H598" stroke="#6077bd" stroke-width="6" stroke-linecap="round"/>
        <path d="M210 105H390L350 230H250Z M530 105H710L670 230H570Z" fill="url(#panel)" stroke="#7188d9" stroke-width="2"/>
        <circle cx="460" cy="72" r="25" fill="url(#accent)" filter="url(#glow)"/>
        <text x="300" y="150" text-anchor="middle" class="svg-title">{{ safeLabels[0] }}</text><text x="300" y="184" text-anchor="middle" class="svg-label">{{ safeLabels[1] }}</text>
        <text x="620" y="150" text-anchor="middle" class="svg-title">{{ safeLabels[3] }}</text><text x="620" y="184" text-anchor="middle" class="svg-label">{{ safeLabels[4] }}</text>
        <text x="460" y="330" text-anchor="middle" class="svg-label">{{ safeLabels[2] }}</text>
      </g>

      <g v-else-if="scene.kind === 'hierarchy'">
        <path d="M460 92V130M460 130L250 208M460 130L670 208M250 248V286M670 248V286" stroke="#657dca" stroke-width="4" fill="none"/>
        <g v-for="(pos, i) in [{x:460,y:70},{x:250,y:220},{x:670,y:220},{x:250,y:305},{x:670,y:305}]" :key="i">
          <circle :cx="pos.x" :cy="pos.y" :r="i === 0 ? 49 : 40" fill="url(#panel)" stroke="#7890df" stroke-width="2"/>
          <text :x="pos.x" :y="pos.y + 5" text-anchor="middle" class="svg-label">{{ safeLabels[i] }}</text>
        </g>
      </g>

      <g v-else-if="scene.kind === 'network'">
        <path d="M205 180L380 82L600 105L730 235L470 290L205 180L470 290L380 82L730 235" stroke="#526aa9" stroke-width="4" fill="none"/>
        <g v-for="(pos, i) in [{x:205,y:180},{x:380,y:82},{x:600,y:105},{x:730,y:235},{x:470,y:290}]" :key="i">
          <circle :cx="pos.x" :cy="pos.y" r="48" fill="url(#panel)" stroke="#7991df" stroke-width="2"/><circle :cx="pos.x - 20" :cy="pos.y - 23" r="8" fill="#72ddff"/>
          <text :x="pos.x" :y="pos.y + 6" text-anchor="middle" class="svg-label">{{ safeLabels[i] }}</text>
        </g>
      </g>

      <g v-else-if="scene.kind === 'table'">
        <rect x="145" y="48" width="630" height="264" rx="18" fill="url(#panel)" stroke="#7088da" stroke-width="2"/>
        <path d="M145 118H775M145 188H775M145 258H775M355 48V312M565 48V312" stroke="#40558f" stroke-width="2"/>
        <rect x="145" y="48" width="630" height="70" rx="18" fill="#273875" opacity=".75"/>
        <g v-for="(label, i) in scene.labels.slice(0, 6)" :key="label">
          <text :x="250 + (i % 3) * 210" :y="90 + Math.floor(i / 3) * 140" text-anchor="middle" class="svg-label">{{ label }}</text>
        </g>
        <rect x="580" y="205" width="168" height="40" rx="8" fill="#4ac7a0" opacity=".18" stroke="#5ad8b1"/>
      </g>

      <g v-else-if="scene.kind === 'chart'">
        <path d="M150 55V300H790" stroke="#7188d9" stroke-width="4"/>
        <g v-for="(h, i) in [80,145,220,120,255]" :key="i">
          <rect :x="190 + i * 115" :y="300 - h" width="68" :height="h" rx="10" :fill="i === 4 ? 'url(#accent)' : '#344c91'" :opacity=".58 + i * .08"/>
          <text :x="224 + i * 115" y="332" text-anchor="middle" class="svg-label">{{ safeLabels[i] }}</text>
        </g>
      </g>

      <g v-else-if="scene.kind === 'branch'">
        <path d="M120 220H800M275 220C330 220 320 102 390 102H620C680 102 670 220 730 220" stroke="#6f87d8" stroke-width="6" fill="none"/>
        <g v-for="(pos, i) in [{x:170,y:220},{x:390,y:102},{x:510,y:102},{x:650,y:102},{x:730,y:220}]" :key="i">
          <circle :cx="pos.x" :cy="pos.y" r="22" fill="url(#accent)" filter="url(#glow)"/><text :x="pos.x" :y="pos.y + 56" text-anchor="middle" class="svg-label">{{ safeLabels[i] }}</text>
        </g>
      </g>

      <g v-else-if="scene.kind === 'document'">
        <path d="M260 40H610L690 120V320H260Z" fill="#eef3ff" opacity=".94"/><path d="M610 40V120H690" fill="#bdc9f4"/>
        <rect x="315" y="95" width="240" height="18" rx="5" fill="#314577"/><rect x="315" y="145" width="305" height="12" rx="4" fill="#9ba8c9"/>
        <rect x="315" y="180" width="270" height="12" rx="4" fill="#9ba8c9"/><rect x="305" y="210" width="326" height="40" rx="8" fill="#ffdb68" opacity=".7"/>
        <rect x="315" y="267" width="230" height="12" rx="4" fill="#9ba8c9"/>
        <g v-for="(label, i) in scene.labels.slice(0, 5)" :key="label"><text x="755" :y="76 + i * 54" class="svg-label">{{ label }}</text><circle cx="728" :cy="71 + i * 54" r="6" fill="#72ddff"/></g>
      </g>

      <g v-else-if="scene.kind === 'conversation'">
        <circle cx="210" cy="170" r="62" fill="url(#panel)" stroke="#7390e0" stroke-width="3"/><circle cx="710" cy="170" r="62" fill="url(#panel)" stroke="#8a79ff" stroke-width="3"/>
        <circle cx="210" cy="150" r="18" fill="#72ddff"/><path d="M175 205Q210 172 245 205" fill="#72ddff" opacity=".7"/><circle cx="710" cy="150" r="18" fill="#a38aff"/><path d="M675 205Q710 172 745 205" fill="#a38aff" opacity=".7"/>
        <path d="M290 100H560Q580 100 580 120V158Q580 178 560 178H355L320 203V178H290Q270 178 270 158V120Q270 100 290 100Z" fill="#25376f" stroke="#7188d9"/>
        <path d="M360 210H630Q650 210 650 230V268Q650 288 630 288H600V313L565 288H360Q340 288 340 268V230Q340 210 360 210Z" fill="#27205f" stroke="#8a79ff"/>
        <text x="425" y="143" text-anchor="middle" class="svg-label">{{ safeLabels[1] }} {{ safeLabels[2] }}</text><text x="495" y="253" text-anchor="middle" class="svg-label">{{ safeLabels[3] }} {{ safeLabels[4] }}</text>
        <text x="210" y="260" text-anchor="middle" class="svg-label">{{ safeLabels[0] }}</text>
      </g>

      <g v-else>
        <rect x="150" y="45" width="620" height="270" rx="24" fill="url(#panel)" stroke="#7188d9" stroke-width="2"/><circle cx="185" cy="80" r="8" fill="#ff7a96"/><circle cx="211" cy="80" r="8" fill="#ffd66b"/><circle cx="237" cy="80" r="8" fill="#56d4aa"/>
        <g v-for="(label, i) in scene.labels.slice(0, 5)" :key="label"><text x="205" :y="137 + i * 36" class="svg-code">{{ i + 1 }}  {{ label }}</text></g>
        <path d="M575 132L650 176L575 220Z" fill="url(#accent)" filter="url(#glow)"/>
      </g>
    </svg>
    <figcaption>{{ scene.caption }}</figcaption>
  </figure>
</template>

<style scoped>
.lesson-illustration { margin: 0; overflow: hidden; border: 1px solid rgba(119,147,232,.24); border-radius: 22px; background: radial-gradient(circle at 78% 12%, rgba(103,91,236,.22), transparent 32%), linear-gradient(145deg, rgba(14,25,63,.98), rgba(7,13,38,.98)); box-shadow: 0 20px 60px rgba(3,8,31,.32); }
.illustration-copy { padding: 26px 30px 6px; }
.illustration-copy span { color: #79ddec; font-size: 10px; letter-spacing: 1.1px; }
.illustration-copy h3 { margin: 8px 0 7px; color: #f1f4ff; font-size: 22px; }
.illustration-copy p { margin: 0; color: #93a0c7; font-size: 12px; line-height: 1.75; }
svg { width: 100%; height: auto; display: block; }
.svg-label, .svg-title, .svg-index, .svg-code { fill: #dce5ff; font-family: Inter, "Microsoft YaHei", sans-serif; font-size: 14px; }
.svg-title { fill: #fff; font-size: 17px; font-weight: 700; }
.svg-index { fill: #081333; font-size: 13px; font-weight: 800; }
.svg-code { fill: #a8e9f4; font-family: Consolas, monospace; }
figcaption { padding: 0 24px 18px; color: #7080ae; font-size: 10px; text-align: center; }
@media (max-width: 720px) { .lesson-illustration { overflow-x: auto; } .lesson-illustration svg { min-width: 720px; } .illustration-copy { position: sticky; left: 0; width: min(82vw, 560px); } }
</style>
