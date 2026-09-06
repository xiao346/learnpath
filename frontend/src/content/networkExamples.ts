export const totalBytes = 3600
export const segmentLimit = 1400
export function splitBytes(total: number, limit: number) {
  if (!Number.isSafeInteger(total) || !Number.isSafeInteger(limit) || total < 0 || limit <= 0) throw new RangeError('Invalid byte counts')
  const segments: { start: number; end: number; size: number }[] = []
  for (let start = 1; start <= total; start += limit) {
    const end = Math.min(start + limit - 1, total)
    segments.push({ start, end, size: end - start + 1 })
  }
  return segments
}
export const segments = splitBytes(totalBytes, segmentLimit)
export const segmentationSteps = [
  ['应用交出3600字节', '这是一个连续字节流。一次应用写入不规定后续产生几个 TCP 段。'],
  ['按示例上限组织成三段', '1400＋1400＋800＝3600。seq 标记每段首字节的位置，第三段从2801开始，不是从3开始。'],
  ['中间缺失，后段先到', '接收端已有1～1400，并假设缓存了2801～3600。因为1401～2800存在缺口，累计 ACK 仍为1401。'],
  ['补齐缺失范围', '示例中恢复1401～2800这段字节；不需要把已收到的整个3600字节流重新作为新数据交付。真实重传范围依算法与反馈确定。'],
  ['连续收到，确认推进', '缺口补齐后，1～3600连续可用，累计 ACK 推进到3601。应用按顺序读取字节，不会收到先尾部、后中间的乱序内容。'],
] as const
export const handshakeSteps = [
  ['尚未完成同步', '点击下一步，从客户端的 SYN 开始。图中时间向下，箭头表示发送方向，间距不代表真实耗时。'],
  ['客户端提出同步', 'SYN，seq=100。客户端选择自己的初始序号；它还不知道服务器的初始序号。'],
  ['服务器确认并同步', 'SYN＋ACK，seq=900，ack=101。它确认客户端 SYN，并给出自己的初始序号。SYN 占用一个序号位置。'],
  ['客户端确认服务器同步', 'ACK，seq=101，ack=901。服务器收到这个确认后，常规建立路径完成。这里省略数据携带、丢包重传等分支。'],
] as const
export const closingSteps = [
  ['两个方向都可以通信', '这是一个独立的关闭示例，不沿用握手图或3600字节数据流的编号。'],
  ['客户端结束自己的发送方向', 'FIN，seq=501。表示客户端不再发送新数据，不表示服务器也已经发送完。'],
  ['服务器确认客户端 FIN', 'ACK，ack=502。服务器还可以继续发送它自己的剩余数据，形成半关闭状态。'],
  ['服务器也结束发送', 'FIN，seq=901。服务器完成自己的发送后，再关闭这个方向。'],
  ['客户端确认并等待', 'ACK，ack=902。常规路径中主动关闭方进入 TIME_WAIT，保留状态以应对最后确认丢失等情况。'],
] as const
export const windowScenarios = [
  { name: '基准场景', rwnd: 4000, cwnd: 3000, flight: 2000, explanation: '网络侧的拥塞窗口更小。min(4000,3000)−2000＝1000，还可发送的简化上界为1000字节。' },
  { name: '接收空间较小', rwnd: 2200, cwnd: 6000, flight: 2000, explanation: '接收窗口更小。min(2200,6000)−2000＝200，接收能力成为当前瓶颈。' },
  { name: '拥塞窗口较小', rwnd: 8000, cwnd: 2400, flight: 2000, explanation: '对端有较大空间，但网络侧约束更紧。min(8000,2400)−2000＝400，增大接收空间并不能消除这个约束。' },
] as const
export const availableWindow = (rwnd: number, cwnd: number, flight: number) => Math.max(0, Math.min(rwnd, cwnd) - flight)

export const routeNodes = [
  { name: 'A', x: 120, y: 105 }, { name: 'B', x: 320, y: 65 },
  { name: 'C', x: 475, y: 220 }, { name: 'D', x: 667, y: 107 },
] as const
export const routeEdges = [
  { from: 'A', to: 'B', weight: 1, labelX: 210, labelY: 59 },
  { from: 'A', to: 'C', weight: 5, labelX: 265, labelY: 185 },
  { from: 'B', to: 'C', weight: 2, labelX: 416, labelY: 142 },
  { from: 'B', to: 'D', weight: 6, labelX: 500, labelY: 64 },
  { from: 'C', to: 'D', weight: 1, labelX: 598, labelY: 194 },
] as const
// Keep diagram distances derived from the same graph that the learner sees.
export function traceShortestPaths(edges: ReadonlyArray<{ from: string; to: string; weight: number }>) {
  const distances: Record<string, number> = { A: 0, B: Infinity, C: Infinity, D: Infinity }
  const previous: Record<string, string> = {}
  const settled: string[] = []
  const snapshot = () => ({ distances: { ...distances }, previous: { ...previous }, settled: [...settled] })
  const states = [snapshot()]
  while (settled.length < routeNodes.length) {
    const next = routeNodes.filter(n => !settled.includes(n.name)).sort((a, b) => distances[a.name]! - distances[b.name]!)[0]!.name
    if (!Number.isFinite(distances[next])) break
    settled.push(next)
    for (const edge of edges) {
      const neighbor = edge.from === next ? edge.to : edge.to === next ? edge.from : null
      if (!neighbor || settled.includes(neighbor)) continue
      const candidate = distances[next]! + edge.weight
      if (candidate < distances[neighbor]!) { distances[neighbor] = candidate; previous[neighbor] = next }
    }
    states.push(snapshot())
  }
  return states
}
export const routeStates = traceShortestPaths(routeEdges)
export const routeSteps = [
  ['起点已知，其余未知', 'A到自身的距离是0；∞表示当前还没有找到路径。先比较未确定节点的候选距离，不能一开始就把最终绿色路线当成已知答案。'],
  ['先确定A，更新它的邻居', 'A的距离最小，为0。经A到B得到0＋1＝1，到C得到0＋5＝5；D还没有候选路径。橙色虚线表示暂定前驱，后续仍可能改变。'],
  ['确定B：C的候选距离变小', '未确定点中B=1最小。经B到C得到1＋2＝3，比原来的5小，因此把C的前驱改为B；经B到D得到1＋6＝7。'],
  ['确定C：再改进D', '现在C=3小于D=7。经C到D得到3＋1＝4，替代原来的7，D的前驱由B改为C。绿色是已确定的最短路径树边，不是所有原始链路。'],
  ['最后确定D，沿前驱还原路线', 'D=4。逆着前驱得到D←C←B←A，因此A到D的最短路径是A→B→C→D，代价1＋2＋1＝4。灰色边依然存在，只是没有成为本次最短路径树的一部分。'],
] as const
