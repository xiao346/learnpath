package com.learnpath.practice;

import java.util.List;

/** Practice questions that follow the first-website learning route. */
public final class WebPracticeQuestionCatalog {

    private WebPracticeQuestionCatalog() {
    }

    public static List<PracticeQuestion> questions() {
        return List.of(
                q("HTML 与 CSS", "网页中表示最主要页面标题的语义标签是？", "<title>", "<h1>", "<header>", "<strong>", "B", "h1 表示页面或内容区域的一级标题；title 则显示在浏览器标签页。", "基础"),
                q("HTML 与 CSS", "为图片提供替代文字应使用哪个属性？", "href", "title", "alt", "srcset-only", "C", "alt 在图片无法显示或由读屏软件读取时表达图片含义。", "基础"),
                q("HTML 与 CSS", "希望元素声明的宽度包含 padding 和 border，应设置？", "box-sizing: border-box", "display: none", "overflow: hidden", "position: fixed", "A", "border-box 会把内边距和边框计入声明宽度，布局尺寸更容易控制。", "基础"),
                q("HTML 与 CSS", "Flex 容器中让项目在主轴两端分散排列，常用哪个值？", "align-items: center", "justify-content: space-between", "flex-direction: column", "flex-wrap: nowrap", "B", "justify-content 控制主轴分布，space-between 会把首尾项目推向两端。", "基础"),
                q("HTML 与 CSS", "在窄屏下改变页面布局最常用的 CSS 能力是？", "媒体查询", "伪元素", "字体继承", "变量提升", "A", "媒体查询可根据视口宽度应用不同规则，是响应式布局的重要工具。", "基础"),
                q("HTML 与 CSS", "表单输入框与文字标签建立明确关联的推荐写法是？", "给 label 的 for 对应 input 的 id", "只设置 placeholder", "把 input 设为 disabled", "使用多个 br", "A", "for 与 id 的对应关系让点击标签可聚焦输入框，也方便辅助技术理解。", "进阶"),
                q("HTML 与 CSS", "两个同等优先级的 CSS 规则都设置 color 时，通常谁生效？", "文件最短的", "最后出现的", "属性最少的", "标签名字最长的", "B", "来源与优先级相同时，层叠规则会采用后出现的声明。", "基础"),
                q("HTML 与 CSS", "避免手机页面产生横向滚动，更合理的图片规则是？", "width: 1200px", "max-width: 100%; height: auto", "position: absolute", "white-space: nowrap", "B", "max-width 让图片不超过容器，自动高度会保持原始比例。", "进阶"),

                q("JavaScript", "声明一个不会重新赋值的变量，优先使用？", "var", "let", "const", "static", "C", "const 清楚表达绑定不会重新赋值，也能减少意外修改。", "基础"),
                q("JavaScript", "给按钮注册点击处理函数的标准方法是？", "addEventListener", "queryValue", "appendStyle", "setIntervalOnly", "A", "addEventListener 将 click 等事件与处理函数连接起来。", "基础"),
                q("JavaScript", "document.querySelector('.card') 返回什么？", "所有卡片组成的数组", "第一个匹配元素或 null", "CSS 文件", "固定字符串", "B", "querySelector 只返回第一个匹配元素，找不到时返回 null。", "基础"),
                q("JavaScript", "切换元素的 dark 类名最直接的方法是？", "element.classList.toggle('dark')", "element.remove()", "element.innerHTML = null", "document.reload()", "A", "classList.toggle 会在类存在时移除、不存在时添加。", "基础"),
                q("JavaScript", "表单提交时不希望浏览器刷新页面，应调用？", "event.preventDefault()", "event.reload()", "form.close()", "window.pause()", "A", "preventDefault 阻止事件的默认提交行为，之后可以用脚本处理数据。", "基础"),
                q("JavaScript", "await fetch(url) 得到的第一项结果通常是什么？", "已经解析好的任意对象", "Response 对象", "HTML 元素", "数据库连接", "B", "fetch 先返回 Response，还需检查 response.ok 并调用 json 等方法读取内容。", "进阶"),
                q("JavaScript", "数组 projects 只保留 featured 为 true 的项目，应使用？", "projects.filter(...) ", "projects.join(...) ", "projects.popAll()", "projects.reload()", "A", "filter 根据条件产生一个包含匹配项的新数组。", "基础"),
                q("JavaScript", "把未经处理的用户输入直接赋给 innerHTML 的主要风险是？", "字体变小", "脚本注入", "数组越界", "网络变慢", "B", "用户内容可能被浏览器解释为 HTML 或脚本，普通文字应优先使用 textContent。", "进阶"),

                q("Vue 3", "Vue 3 单文件组件通常使用哪个扩展名？", ".vue", ".java", ".sql", ".spring", "A", ".vue 文件可以在同一组件中组织 template、script 与 style。", "基础"),
                q("Vue 3", "组合式 API 中保存单个响应式数值常用？", "ref", "fetch", "route", "emitOnly", "A", "ref 创建响应式引用，脚本中用 .value 读写，模板中会自动解包。", "基础"),
                q("Vue 3", "父组件向子组件传递只读数据通常使用？", "Props", "Emit", "Router", "CSS var", "A", "Props 建立从父到子的单向数据流，子组件不应直接修改。", "基础"),
                q("Vue 3", "子组件要通知父组件发生了选择事件，应使用？", "emit", "props.push", "window.reload", "style scoped", "A", "emit 发送命名事件，父组件监听后决定怎样更新状态。", "基础"),
                q("Vue 3", "使用 v-for 渲染列表时 key 最适合选择？", "稳定且唯一的业务 id", "每次随机数", "相同固定字符串", "当前日期", "A", "稳定唯一的 key 帮助 Vue 正确复用节点并维护组件状态。", "进阶"),
                q("Vue 3", "根据其他响应式数据推导一个只读结果，优先使用？", "computed", "setTimeout", "document.write", "location.href", "A", "computed 会跟踪依赖并缓存推导值，适合总价、筛选结果等数据。", "基础"),
                q("Vue 3", "Vue Router 的主要职责是？", "管理数据库事务", "把 URL 映射到页面组件", "编译 Java", "压缩图片", "B", "路由让单页应用拥有可访问、可刷新和可分享的页面地址。", "基础"),
                q("Vue 3", "运行 Vite 项目生产构建的常见命令是？", "npm run build", "git open", "vue database", "css publish", "A", "项目通常在 package.json 中将生产构建配置为 npm run build。", "基础"),

                q("FastAPI", "创建一个 FastAPI 应用实例的常见写法是？", "app = FastAPI()", "app = Vue()", "server = HTML()", "db = CSS()", "A", "FastAPI() 创建应用对象，路由装饰器会注册到这个对象。", "基础"),
                q("FastAPI", "定义读取资源的 GET 路由通常使用？", "@app.get('/items')", "@app.sql('/items')", "@app.css('/items')", "@app.vue('/items')", "A", "@app.get 将路径和处理函数注册为 HTTP GET 接口。", "基础"),
                q("FastAPI", "Pydantic 模型在接口中的主要作用是？", "数据校验与结构描述", "改变网页颜色", "管理 Git 分支", "编译浏览器", "A", "Pydantic 根据类型和约束校验输入，并帮助生成接口文档。", "基础"),
                q("FastAPI", "请求创建成功时常见的 HTTP 状态码是？", "201", "301", "404", "500", "A", "201 Created 明确表示服务器成功创建了新资源。", "基础"),
                q("FastAPI", "查询不存在的文章时更合适返回？", "200 且空字符串", "404", "302", "101", "B", "404 Not Found 准确表达指定资源不存在。", "基础"),
                q("FastAPI", "浏览器前端与 API 来源不同而被阻止访问，通常需要正确配置？", "CORS", "Flex", "HTML alt", "Git tag", "A", "CORS 由后端声明允许访问接口的可信前端来源。", "进阶"),
                q("FastAPI", "数据库事务发生异常时，为避免保留部分修改通常应？", "rollback", "继续 commit", "删除 Python", "刷新浏览器", "A", "回滚让事务中的未提交修改一起撤销，保持数据一致。", "进阶"),
                q("FastAPI", "部署接口时，密钥和数据库地址更适合放在哪里？", "环境变量或安全配置", "提交到公开仓库", "写进页面标题", "放在 CSS 注释", "A", "环境变量等外部配置可避免泄露敏感信息并适配不同环境。", "进阶")
        );
    }

    private static PracticeQuestion q(String subject, String prompt, String a, String b, String c, String d,
                                      String correct, String explanation, String difficulty) {
        return new PracticeQuestion(subject, prompt, a, b, c, d, correct, explanation, difficulty,
                "进阶".equals(difficulty) ? 15 : 10);
    }
}
