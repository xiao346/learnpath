<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, useId, watch } from 'vue'
import type { NetworkFigureKind } from '../content/chapterTutorials'
import { segments, segmentationSteps, handshakeSteps, closingSteps, windowScenarios, availableWindow, routeNodes, routeEdges, routeStates, routeSteps } from '../content/networkExamples'

const props = defineProps<{ kind: NetworkFigureKind }>()
const step = ref(0)
const scenarioIndex = ref(0)
const marker = `network-arrow-${useId().replace(/[^a-zA-Z0-9_-]/g, '')}`
const arrow = `url(#${marker})`
const stages = computed(() => props.kind === 'segmentation' ? segmentationSteps : props.kind === 'handshake' ? handshakeSteps : props.kind === 'closing' ? closingSteps : props.kind === 'route' ? routeSteps : null)
const routeState = computed(() => routeStates[Math.min(step.value, routeStates.length - 1)]!)
const graphEdges = computed(() => routeEdges.map(edge => {
  const from = routeNodes.find(node => node.name === edge.from)!
  const to = routeNodes.find(node => node.name === edge.to)!
  const child = routeState.value.previous[to.name] === from.name ? to.name : routeState.value.previous[from.name] === to.name ? from.name : null
  return { ...edge, path: `M ${from.x} ${from.y} L ${to.x} ${to.y}`, state: child ? routeState.value.settled.includes(child) ? 'settled' : 'candidate' : 'unused' }
}))
const stage = computed(() => stages.value?.[step.value])
const scenario = computed(() => windowScenarios[scenarioIndex.value])
const available = computed(() => availableWindow(scenario.value.rwnd, scenario.value.cwnd, scenario.value.flight))
const limits = computed(() => Math.min(scenario.value.rwnd, scenario.value.cwnd))
const figureNames: Record<NetworkFigureKind, string> = {
  layers: '同一请求在两端协议栈中的方向', encapsulation: '原始内容不变，逐层添加控制字段', ports: '同一服务端口可以接待多条连接', hop: '目的 IP 与当前一跳的 MAC 分工', segmentation: '3600字节的分段、丢失与恢复', handshake: '用具体序号推演三次握手', stream: '写入边界与读取边界不必一致', window: '接收窗口与拥塞窗口共同限制发送', closing: '两个发送方向分别关闭',
  signal: '四种电平分别对应两比特信息', frame: '一帧中哪些字节是控制信息，哪些是载荷', subnet: '/24划分成四个/26地址块', route: '非负代价图中的最短路径更新', dns: '没有所需缓存时的一次简化解析', trust: '加密前还要确认对方是谁',
}
const figureCaption: Partial<Record<NetworkFigureKind, string>> = {
  layers: '沿实线追踪发送和接收方向；横向虚线只表示对等层协议关系。真实数据经过下层与网络，不沿虚线直接穿越。',
  encapsulation: '同一份应用内容始终为橙色。每层读取自己需要的控制字段，向上交付内层内容。本图解释包含关系，宽度不按真实首部长度绘制。',
  ports: '两条连接的服务器端相同，但客户端临时端口不同。完整端点组合不同，所以可以分别维护连接状态；端口不是进程 ID。',
  hop: '在普通无 NAT 转发假设下，目的 IP 保持指向服务器；承载它的以太网帧在下一链路使用新 MAC 首部。MAC-A、MAC-R1、MAC-R2、MAC-S 是教学代号。',
  stream: '内容仍按 A→B→C→D 的顺序出现，改变的是每次读取拿到多少字节。应用必须自己规定消息结束在哪里。图中展示一种可能拆分，不是唯一拆分。',
  window: '蓝色是在途未确认字节，绿色是简化窗口条件下还能发送的额度。三个按钮是独立假设场景，不表示接收端随意撤回已经公告的窗口。',
  signal: '虚线分隔等长符号时间。这个教学映射有四种状态，每个符号对应两比特；不是把一个符号强行理解为一个比特，也不是具体网卡的实际波形。',
  frame: '载荷位于地址和类型字段之后，FCS用于检错。图中宽度为标签可读性调整，不按字节比例绘制；省略前导、帧开始标志与帧间隙，未加VLAN标签。',
  subnet: '四个块各有64个地址。相同/26前缀下最后6位是主机部分；普通广播子网中首尾地址承担网络与广播用途。',
  route: '绿色路线A→B→C→D的代价为1＋2＋1＝4。灰色边仍存在，但这次不是最优路径；所有权重都是非负教学代价。',
  dns: '客户端请求递归服务；解析器逐步询问其他服务器，得到相应记录后答复客户端。箭头省略了响应返回线和CNAME等其他分支；缓存命中时可以不走完整路径。',
  trust: '证书检查、密钥建立与数据保护有不同职责。图中列出常见身份检查条件，不是完整TLS握手或完整证书验证算法；不能用“有加密”替代身份验证。',
}
const handshake = [
  { from: 180, to: 600, y: 105, text: 'SYN  ·  seq = 100' },
  { from: 600, to: 180, y: 190, text: 'SYN + ACK  ·  seq = 900  ·  ack = 101' },
  { from: 180, to: 600, y: 275, text: 'ACK  ·  seq = 101  ·  ack = 901' },
]
const closing = [
  { from: 180, to: 600, y: 100, text: 'FIN  ·  seq = 501' },
  { from: 600, to: 180, y: 170, text: 'ACK  ·  ack = 502' },
  { from: 600, to: 180, y: 240, text: 'FIN  ·  seq = 901' },
  { from: 180, to: 600, y: 310, text: 'ACK  ·  ack = 902' },
]
const timeline = computed(() => props.kind === 'handshake' ? handshake : closing)
watch(() => props.kind, () => { step.value = 0; scenarioIndex.value = 0 })
let stepBeforePrint: number | null = null
function preparePrint() { if (stages.value && stepBeforePrint === null) { stepBeforePrint = step.value; step.value = stages.value.length - 1 } }
function restoreAfterPrint() { if (stepBeforePrint !== null) { step.value = stepBeforePrint; stepBeforePrint = null } }
onMounted(() => { window.addEventListener('beforeprint', preparePrint); window.addEventListener('afterprint', restoreAfterPrint) })
onBeforeUnmount(() => { window.removeEventListener('beforeprint', preparePrint); window.removeEventListener('afterprint', restoreAfterPrint) })
</script>

<template>
  <figure class="mechanism-figure">
    <header><span>原理图解</span><strong>{{ figureNames[kind] }}</strong></header>
    <small class="scroll-hint">窄屏可左右滑动查看完整图，文字解析在图下方。</small>
    <div class="diagram-scroll" tabindex="0" :aria-label="`${figureNames[kind]}，可横向滚动`">
      <svg class="network-svg" :viewBox="`0 0 780 ${kind === 'handshake' || kind === 'closing' ? 400 : kind === 'route' ? 365 : kind === 'layers' ? 355 : kind === 'segmentation' ? 345 : 320}`" role="img" :aria-label="figureNames[kind]">
        <title>{{ figureNames[kind] }}</title>
        <desc>{{ stage ? `${stage[0]}。${stage[1]}` : figureCaption[kind] }}</desc>
        <defs><marker :id="marker" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M 0 0 L 10 5 L 0 10 z" fill="#476a85" /></marker></defs>

        <g v-if="kind === 'signal'">
          <text x="390" y="27" class="major">示例映射：00→电平0　01→电平1　10→电平2　11→电平3</text>
          <path d="M 86 245 V 53 M 86 245 H 740" class="path" :marker-end="arrow" />
          <g v-for="i in [0,1,2,3]" :key="i"><path :d="`M 92 ${225-i*46} H 728`" stroke="#dbe5ec" stroke-dasharray="3 6" /><text x="58" :y="230-i*46" class="minor">{{ i }}</text><path :d="`M ${105+i*154} 65 V 240`" class="relation" /></g>
          <path d="M 105 225 H 259 V 179 H 413 V 87 H 567 V 133 H 721" fill="none" stroke="#2b8199" stroke-width="5" />
          <text x="182" y="267">00</text><text x="336" y="267">01</text><text x="490" y="267">11</text><text x="644" y="267">10</text><text x="390" y="308" class="major">4个符号承载8比特；每段横向宽度表示一个符号时间</text>
        </g>

        <g v-else-if="kind === 'frame'">
          <text x="390" y="35" class="major">未加VLAN标签的以太网II帧（简化）</text>
          <g v-for="(field,i) in [{x:35,w:125,label:'目的MAC',size:'6字节'},{x:160,w:125,label:'源MAC',size:'6字节'},{x:285,w:105,label:'类型',size:'2字节'},{x:390,w:245,label:'上层载荷＋必要填充',size:'通常46～1500字节'},{x:635,w:110,label:'FCS',size:'4字节'}]" :key="field.label"><rect :x="field.x" y="90" :width="field.w" height="84" :fill="i===3 ? '#ffe0ad' : i===4 ? '#ccebd9' : '#cbe3f6'" stroke="#91a9bb" /><text :x="field.x+field.w/2" y="123">{{ field.label }}</text><text :x="field.x+field.w/2" y="152" class="minor">{{ field.size }}</text></g>
          <path d="M 36 191 V 204 H 744 V 191" class="path" /><text x="390" y="240" class="major">最小64字节 = 6＋6＋2＋46＋4</text><text x="390" y="282" class="minor">源地址用于学习入口；目的地址用于查找转发出口</text>
        </g>

        <g v-else-if="kind === 'subnet'">
          <text x="390" y="28" class="major">192.168.10.0/24 → 四个 /26</text>
          <rect x="45" y="56" width="480" height="54" fill="#cbe3f6" /><text x="285" y="88">原来的24位网络前缀</text>
          <rect x="525" y="56" width="62" height="54" fill="#ffe0ad" /><text x="556" y="88">2位</text><rect x="587" y="56" width="150" height="54" fill="#ccebd9" /><text x="662" y="88">6位主机</text>
          <path d="M 555 120 V 147" class="path" :marker-end="arrow" /><text x="284" y="145" class="minor">用新增2位区分四个子网</text>
          <g v-for="(part,i) in [{bits:'00',range:'0～63',net:'.0/26'},{bits:'01',range:'64～127',net:'.64/26'},{bits:'10',range:'128～191',net:'.128/26'},{bits:'11',range:'192～255',net:'.192/26'}]" :key="part.bits"><rect :x="45+i*174" y="170" width="170" height="78" rx="4" :fill="['#cbe3f6','#ccebd9','#ffe0ad','#e2dcf7'][i]" /><text :x="130+i*174" y="197">子网位 {{ part.bits }}</text><text :x="130+i*174" y="230" class="major">{{ part.range }}</text><text :x="130+i*174" y="280" class="minor">网络后缀 {{ part.net }}</text></g>
          <text x="390" y="313" class="minor">图中范围表示IPv4地址最后一个八位组，每个范围包含64个地址</text>
        </g>

        <g v-else-if="kind === 'route'">
          <g v-for="edge in graphEdges" :key="`${edge.from}-${edge.to}`">
            <path :d="edge.path" fill="none" :stroke="edge.state==='settled' ? '#319a76' : edge.state==='candidate' ? '#bf812c' : '#b8c5cf'" :stroke-width="edge.state==='unused' ? 2 : 4" :stroke-dasharray="edge.state==='candidate' ? '8 6' : undefined" />
            <text :x="edge.labelX" :y="edge.labelY">{{ edge.weight }}</text>
          </g>
          <g v-for="node in routeNodes" :key="node.name">
            <circle :cx="node.x" :cy="node.y" r="27" :fill="routeState.settled.includes(node.name) ? '#ccebd9' : '#f4f7fa'" :stroke="routeState.settled.includes(node.name) ? '#43806b' : '#91a4b2'" stroke-width="2" />
            <text :x="node.x" :y="node.y+6" class="major">{{ node.name }}</text>
            <text :x="node.x" :y="node.y+49" class="minor">{{ routeState.settled.includes(node.name) ? '已确定' : '候选' }} {{ Number.isFinite(routeState.distances[node.name]) ? routeState.distances[node.name] : '∞' }}</text>
          </g>
          <text x="390" y="309" class="major">已确定顺序：{{ routeState.settled.join(' → ') || '尚未选择节点' }}</text>
          <text x="390" y="345" class="minor">绿色实线：已确定　橙色虚线：暂定前驱　灰色：其他链路</text>
        </g>

        <g v-else-if="kind === 'dns'">
          <rect x="28" y="53" width="180" height="65" rx="7" fill="#cbe3f6" /><text x="118" y="91">客户端</text><rect x="272" y="53" width="220" height="65" rx="7" fill="#ccebd9" /><text x="382" y="82">递归解析器</text><text x="382" y="105" class="minor">先检查可用缓存</text><path d="M 213 84 H 263" class="path" :marker-end="arrow" />
          <g v-for="(item,i) in ['根服务器：指引顶级域','顶级域：提供下一层委派','权威服务器：相应记录']" :key="item"><rect x="538" :y="32+i*87" width="222" height="57" rx="5" :fill="['#e2dcf7','#ffe0ad','#cbe3f6'][i]" /><text x="649" :y="66+i*87" class="minor">{{ item }}</text><path :d="`M 500 ${75+i*13} L 532 ${60+i*87}`" class="path" :marker-end="arrow" /><text x="510" :y="53+i*74" class="minor">{{ i+1 }}</text></g>
          <path d="M 381 129 V 220 H 118 V 129" class="path" :marker-end="arrow" /><text x="263" y="250" class="minor">获得结果后，答复客户端并按规则缓存</text><text x="390" y="299" class="minor">不是每次查询都走一遍：缓存与委派信息可省略部分交互</text>
        </g>

        <g v-else-if="kind === 'trust'">
          <rect x="28" y="99" width="156" height="82" rx="7" fill="#cbe3f6" /><text x="106" y="129">客户端</text><text x="106" y="158" class="minor">预期域名已知</text><path d="M 191 140 H 250" class="path" :marker-end="arrow" />
          <path d="M 274 42 H 481 L 515 76 V 253 H 274 Z" fill="#fff4d9" stroke="#c3a76d" stroke-width="2" /><path d="M 481 42 V 76 H 515" fill="none" stroke="#c3a76d" stroke-width="2" />
          <text x="393" y="81" class="major">服务器证书</text><text x="393" y="125">名称匹配预期身份？</text><text x="393" y="169">时间与用途符合要求？</text><text x="393" y="213">签名链连接可信依据？</text><path d="M 526 140 H 585" class="path" :marker-end="arrow" />
          <path d="M 644 111 V 85 A 27 27 0 0 1 698 85 V 111" fill="none" stroke="#53826d" stroke-width="8" /><rect x="622" y="112" width="98" height="83" rx="8" fill="#ccebd9" stroke="#53826d" /><circle cx="671" cy="148" r="9" fill="#53826d" /><path d="M 671 155 V 173" stroke="#53826d" stroke-width="5" />
          <text x="672" y="228" class="minor">配合密钥建立</text><text x="672" y="250" class="minor">保护后续数据</text><text x="390" y="302" class="minor">验证失败时处理原因；加密不会把错误对象自动变成正确对象</text>
        </g>

        <g v-else-if="kind === 'layers'">
          <text x="160" y="27" class="major">发送端：浏览器所在主机</text><text x="620" y="27" class="major">接收端：服务所在主机</text>
          <g v-for="(layer, i) in [['应用层','业务消息'],['传输层','端口、交付服务'],['网际层','IP、路由'],['链路层','当前链路的帧']]" :key="i">
            <rect x="40" :y="48+i*63" width="235" height="47" rx="7" :fill="['#ffe0ad','#ccebd9','#cbe3f6','#e2dcf7'][i]" />
            <rect x="505" :y="48+i*63" width="235" height="47" rx="7" :fill="['#ffe0ad','#ccebd9','#cbe3f6','#e2dcf7'][i]" />
            <text x="157" :y="77+i*63">{{ layer[0] }} · {{ layer[1] }}</text><text x="622" :y="77+i*63">{{ layer[0] }} · {{ layer[1] }}</text>
            <path :d="`M 286 ${72+i*63} H 493`" class="relation" :marker-end="arrow" />
          </g>
          <path d="M 23 65 V 267" class="path" :marker-end="arrow" /><path d="M 758 267 V 65" class="path" :marker-end="arrow" />
          <path d="M 157 286 V 320 H 622 V 288" class="path" :marker-end="arrow" />
          <text x="390" y="307" class="minor">经过实际链路与中间网络</text><text x="390" y="42" class="minor">虚线：对等协议关系</text>
        </g>

        <g v-else-if="kind === 'encapsulation'">
          <g v-for="(name, row) in ['应用消息','TCP 段','IP 包','以太网帧']" :key="name">
            <text x="70" :y="56+row*70" class="minor">{{ name }}</text>
            <rect x="414" :y="26+row*70" width="305" height="48" rx="3" fill="#ffe0ad" stroke="#bc8b45" /><text x="566" :y="56+row*70">同一份应用数据</text>
            <g v-if="row>=1"><rect x="317" :y="26+row*70" width="97" height="48" fill="#ccebd9" stroke="#609677" /><text x="365" :y="56+row*70">TCP 首部</text></g>
            <g v-if="row>=2"><rect x="220" :y="26+row*70" width="97" height="48" fill="#cbe3f6" stroke="#739ab7" /><text x="268" :y="56+row*70">IP 首部</text></g>
            <g v-if="row>=3"><rect x="123" :y="26+row*70" width="97" height="48" fill="#e2dcf7" stroke="#9687b8" /><text x="171" :y="56+row*70">MAC 首部</text><rect x="719" :y="26+row*70" width="45" height="48" fill="#e2dcf7" stroke="#9687b8" /><text x="742" :y="56+row*70" class="minor">FCS</text></g>
          </g>
          <text x="445" y="312" class="minor">端口 / 序号　　　IP 地址　　　MAC 地址：各层增加各自需要的信息</text>
        </g>

        <g v-else-if="kind === 'ports'">
          <rect x="27" y="57" width="227" height="177" rx="10" fill="#eef5fc" stroke="#8ca9c2" /><path d="M 80 234 H 200 M 139 234 V 250 M 92 250 H 186" class="path" />
          <text x="140" y="35" class="major">客户端 198.51.100.24</text>
          <text x="140" y="106">套接字 A · 端口52110</text><text x="140" y="168">套接字 B · 端口52111</text>
          <rect x="544" y="57" width="210" height="194" rx="10" fill="#eff5f1" stroke="#85a38e" /><text x="648" y="35" class="major">服务器 203.0.113.8</text>
          <rect x="558" y="82" width="181" height="57" rx="5" fill="#ccebd9" /><text x="648" y="115">网站服务 · TCP 443</text>
          <rect x="558" y="169" width="181" height="57" rx="5" fill="#e2dcf7" /><text x="648" y="203">另一服务 · TCP 22</text>
          <path d="M 260 99 H 525 L 541 105" class="path" :marker-end="arrow" /><path d="M 260 162 H 475 L 540 123" class="path" :marker-end="arrow" />
          <text x="389" y="84" class="minor">连接 A</text><text x="373" y="189" class="minor">连接 B：源端口不同</text>
          <text x="390" y="287">连接标识：源 IP ＋ 源端口 ＋ 目的 IP ＋ 目的端口</text>
        </g>

        <g v-else-if="kind === 'hop'">
          <rect x="40" y="38" width="120" height="62" rx="6" fill="#cbe3f6" stroke="#739ab7" /><path d="M 100 100 V 115 M 68 115 H 132" class="path" /><text x="100" y="76">本地主机</text>
          <rect x="321" y="40" width="140" height="58" rx="12" fill="#e2dcf7" stroke="#9687b8" /><text x="391" y="76">路由器</text><text x="347" y="121" class="minor">入口 R1</text><text x="434" y="121" class="minor">出口 R2</text>
          <rect x="640" y="31" width="90" height="82" rx="5" fill="#ccebd9" stroke="#609677" /><path d="M 654 52 H 716 M 654 76 H 716" class="path" /><text x="685" y="139">目标服务器</text>
          <path d="M 173 70 H 308 M 473 70 H 628" class="path" :marker-end="arrow" />
          <text x="237" y="48" class="minor">第一条链路</text><text x="556" y="48" class="minor">下一条链路</text>
          <rect x="27" y="162" width="344" height="98" rx="6" fill="#f0ebfb" /><rect x="410" y="162" width="344" height="98" rx="6" fill="#f0ebfb" />
          <text x="199" y="189">帧：MAC-A → MAC-R1</text><text x="582" y="189">新帧：MAC-R2 → MAC-S</text>
          <rect x="42" y="203" width="314" height="42" rx="3" fill="#cbe3f6" /><rect x="425" y="203" width="314" height="42" rx="3" fill="#cbe3f6" />
          <text x="199" y="230">内部 IP 包：目的仍是服务器</text><text x="582" y="230">内部 IP 包：目的仍是服务器</text>
          <text x="390" y="296" class="minor">路由器换的是当前链路封装；IP 的最终目的并未改成“下一跳”</text>
        </g>

        <g v-else-if="kind === 'segmentation'">
          <text x="390" y="24" class="major">发送方：连续3600字节</text>
          <rect x="40" y="41" width="700" height="49" rx="5" fill="#ffe0ad" stroke="#bc8b45" /><text x="390" y="71">字节 1 ………………………………………… 3600</text>
          <g v-if="step>=1" class="revealed">
            <path d="M 390 97 V 128" class="path" :marker-end="arrow" />
            <g v-for="(part,i) in segments" :key="part.start">
              <rect :x="40+(part.start-1)/3600*700+2" y="140" :width="part.size/3600*700-4" height="55" rx="5" :fill="i===1 && step===2 ? '#ffe3dc' : '#ccebd9'" :stroke="i===1 && step===2 ? '#b94132' : '#609677'" />
              <text :x="40+(part.start-1+part.size/2)/3600*700" y="163">{{ part.size }}字节</text>
              <text :x="40+(part.start-1+part.size/2)/3600*700" y="183" class="minor">seq={{ part.start }}</text>
            </g>
          </g>
          <g v-if="step>=2" class="revealed">
            <text x="390" y="226" class="minor">{{ step===2 ? '第二段丢失 ×；第三段先到并缓存' : step===3 ? '↓ 恢复缺口 1401～2800' : '缺口补齐，按序交付给应用' }}</text>
            <g v-for="(part,i) in step===4 ? [] : segments" :key="part.start">
              <rect :x="40+(part.start-1)/3600*700+2" y="241" :width="part.size/3600*700-4" height="49" rx="5" :fill="i===1 && step===2 ? '#fff5f2' : '#cbe3f6'" :stroke="i===1 && step===2 ? '#b94132' : '#739ab7'" :stroke-dasharray="i===1 && step===2 ? '7 5' : undefined" />
              <text :x="40+(part.start-1+part.size/2)/3600*700" y="272">{{ i===1 && step===2 ? '缺失' : `${part.start}～${part.end}` }}</text>
            </g>
            <g v-if="step===4"><rect x="42" y="241" width="696" height="49" rx="5" fill="#ccebd9" stroke="#609677" /><text x="390" y="272">连续字节流 1～3600 · 按序交付</text></g>
            <text x="390" y="323" class="major">{{ step===2 ? '累计 ACK = 1401（不能越过缺口）' : '累计 ACK = 3601（下一期望字节）' }}</text>
          </g>
          <text v-if="step===0" x="390" y="168" class="minor">点击下一步：观察分段，而不是直接把应用写入当成一个包</text>
          <text v-if="step===1" x="390" y="263" class="minor">MSS 限制数据部分；本示例不绘制 TCP / IP 首部</text>
        </g>

        <g v-else-if="kind === 'handshake' || kind === 'closing'">
          <text x="180" y="36" class="major">客户端{{ kind==='closing' ? '（主动关闭）' : '' }}</text><text x="600" y="36" class="major">服务器</text>
          <path d="M 180 60 V 360 M 600 60 V 360" class="relation" /><text x="716" y="209" class="minor">时间 ↓</text>
          <g v-for="(line,i) in timeline" :key="i">
            <g v-if="step>i" class="revealed"><path :d="`M ${line.from} ${line.y} L ${line.to} ${line.y+23}`" class="path" :marker-end="arrow" /><circle :cx="line.from" :cy="line.y" r="5" fill="#2f6e96" /><text x="390" :y="line.y-10">{{ line.text }}</text></g>
          </g>
          <text v-if="step===0" x="390" y="179" class="minor">点击下一步，逐条显示报文</text>
          <g v-if="step===stages!.length-1"><text x="180" y="382" class="major">{{ kind==='closing' ? 'TIME_WAIT' : 'ESTABLISHED' }}</text><text x="600" y="382" class="major">{{ kind==='closing' ? '收到最后 ACK 后关闭' : '收到第三步后 ESTABLISHED' }}</text></g>
        </g>

        <g v-else-if="kind === 'stream'">
          <text x="127" y="64">应用写入</text><text x="127" y="156">TCP 字节流</text><text x="127" y="252">一种读取结果</text>
          <rect x="248" y="34" width="191" height="50" rx="5" fill="#ffe0ad" stroke="#bc8b45" /><rect x="451" y="34" width="191" height="50" rx="5" fill="#ffe0ad" stroke="#bc8b45" />
          <text x="343" y="65">第1次：AB</text><text x="546" y="65">第2次：CD</text>
          <path d="M 445 94 V 119 M 445 181 V 213" class="path" :marker-end="arrow" />
          <g v-for="(letter,i) in ['A','B','C','D']" :key="letter"><rect :x="248+i*100" y="129" width="94" height="43" fill="#cbe3f6" /><text :x="295+i*100" y="157" class="major">{{ letter }}</text></g>
          <rect x="248" y="224" width="94" height="50" rx="5" fill="#ccebd9" stroke="#609677" /><rect x="351" y="224" width="291" height="50" rx="5" fill="#ccebd9" stroke="#609677" /><text x="295" y="255">A</text><text x="496" y="255">BCD</text>
          <text x="446" y="309" class="minor">内容顺序没有变化，应用调用边界没有被保留</text>
        </g>

        <g v-else-if="kind === 'window'">
          <text x="390" y="30" class="major">{{ scenario.name }} · 所有数值单位为字节</text>
          <text x="220" y="69">接收端公告 rwnd = {{ scenario.rwnd }}</text><text x="576" y="69">发送端维护 cwnd = {{ scenario.cwnd }}</text>
          <path d="M 220 83 L 390 113 M 576 83 L 390 113" class="path" :marker-end="arrow" />
          <text x="390" y="142">取较小值：{{ limits }}　减去在途：{{ scenario.flight }}</text>
          <rect x="80" y="171" width="620" height="58" rx="5" fill="#eef1f5" />
          <rect x="80" y="171" :width="scenario.flight/8000*620" height="58" fill="#8dbbdc" />
          <rect :x="80+scenario.flight/8000*620" y="171" :width="available/8000*620" height="58" fill="#83c9aa" />
          <path :d="`M ${80+limits/8000*620} 165 V 237`" stroke="#365f78" stroke-width="3" />
          <text x="80" y="255" class="minor">0</text><text x="700" y="255" class="minor">8000（固定比较尺度）</text>
          <rect x="103" y="279" width="14" height="14" fill="#8dbbdc" /><text x="215" y="291" class="minor">在途未确认 {{ scenario.flight }}</text>
          <rect x="389" y="279" width="14" height="14" fill="#83c9aa" /><text x="528" y="291" class="major">还能发送 ≤ {{ available }}</text>
        </g>
      </svg>
    </div>
    <div v-if="stages" class="figure-controls" role="group" :aria-label="`${figureNames[kind]}步骤控制`">
      <button type="button" :disabled="step===0" @click="step--">← 上一步</button><span>{{ step+1 }} / {{ stages.length }}</span><button type="button" :disabled="step===stages.length-1" @click="step++">下一步 →</button><button class="restart" type="button" :disabled="step===0" @click="step=0">重看</button>
    </div>
    <div v-if="kind==='window'" class="figure-controls scenarios" role="group" aria-label="对比窗口条件"><button v-for="(item,i) in windowScenarios" :key="item.name" type="button" :aria-pressed="scenarioIndex===i" @click="scenarioIndex=i">{{ item.name }}</button></div>
    <figcaption aria-live="polite" aria-atomic="true"><template v-if="stage"><strong>{{ stage[0] }}</strong><p>{{ stage[1] }}</p></template><template v-else-if="kind==='window'"><strong>{{ scenario.explanation }}</strong><p>{{ figureCaption.window }}</p></template><p v-else>{{ figureCaption[kind] }}</p></figcaption>
  </figure>
</template>

<style scoped>
.mechanism-figure { margin: 30px 0 32px; padding: 20px 0 16px; border-top: 1px solid #d9e4ed; border-bottom: 1px solid #d9e4ed; color: #24425a; }
.mechanism-figure header { display: flex; align-items: baseline; flex-wrap: wrap; gap: 8px 14px; margin-bottom: 18px; }
.mechanism-figure header > span { color: #347893; font-size: 12px; letter-spacing: 1px; }
.mechanism-figure header strong { font-size: 16px; line-height: 1.7; }
.diagram-scroll { overflow-x: auto; border-radius: 5px; background: #fff; scrollbar-width: thin; }
.diagram-scroll:focus-visible { outline: 2px solid #347893; outline-offset: 3px; }
.network-svg { display: block; width: 100%; min-width: 620px; font: 16px 'Microsoft YaHei', 'PingFang SC', sans-serif; color: #24425a; }
.network-svg text { fill: currentColor; text-anchor: middle; }
.network-svg .major { font-weight: 600; font-size: 17px; }
.network-svg .minor { font-size: 14px; fill: #526b7d; }
.network-svg .path { stroke: #476a85; stroke-width: 2; fill: none; }
.network-svg .relation { stroke: #8196a7; stroke-width: 1.5; fill: none; stroke-dasharray: 6 5; }
.revealed { animation: diagram-reveal .2s ease-out; }
.figure-controls { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin: 18px 0 16px; }
.figure-controls button { border: 1px solid #b5c8d6; border-radius: 5px; background: #fff; color: #245775; font: inherit; font-size: 14px; padding: 9px 12px; min-height: 40px; cursor: pointer; }
.figure-controls button:hover:not(:disabled), .figure-controls button[aria-pressed='true'] { background: #e7f1f7; border-color: #397a9a; }
.figure-controls button:focus-visible { outline: 2px solid #216789; outline-offset: 3px; }
.figure-controls button:disabled { opacity: .45; cursor: default; }
.figure-controls span { font-size: 13px; color: #577181; }
.figure-controls .restart { margin-left: auto; }
figcaption { margin-top: 18px; font-size: 15px; line-height: 1.9; color: #405e73; }
figcaption strong { font-weight: 600; color: #235d79; }
figcaption p { margin: 6px 0 0; }
.scroll-hint { display: none; margin-bottom: 9px; color: #526b7d; font-size: 12px; line-height: 1.7; }
@media (max-width: 900px) { .scroll-hint { display: block; } }
@media (prefers-reduced-motion: reduce) { .revealed { animation: none; } }
@media print { .figure-controls, .scroll-hint { display: none; } .network-svg { min-width: 0; } .diagram-scroll { overflow: visible; } }
@keyframes diagram-reveal { from { opacity: .3; } to { opacity: 1; } }
</style>
