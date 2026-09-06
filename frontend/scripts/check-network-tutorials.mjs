import assert from 'node:assert/strict'
import { chapterTutorials } from '../src/content/chapterTutorials.ts'
import { networkFoundations } from '../src/content/networkFoundations.ts'
import { segments, splitBytes, segmentationSteps, handshakeSteps, closingSteps, windowScenarios, availableWindow, routeStates, routeSteps, routeEdges, traceShortestPaths } from '../src/content/networkExamples.ts'

assert.deepEqual(segments, [{ start:1, end:1400, size:1400 }, { start:1401, end:2800, size:1400 }, { start:2801, end:3600, size:800 }])
assert.equal(segments.reduce((sum, item) => sum + item.size, 0), 3600)
assert.deepEqual(splitBytes(0, 1400), [])
assert.deepEqual(splitBytes(2, 1).map(x => x.start), [1, 2])
assert.throws(() => splitBytes(10, 0), RangeError)
assert.throws(() => splitBytes(-1, 2), RangeError)
assert.throws(() => splitBytes(1.5, 2), RangeError)
assert.equal(segmentationSteps.length, 5)
assert.ok(segmentationSteps[2][1].includes('ACK 仍为1401'))
assert.ok(segmentationSteps[4][1].includes('3601'))
assert.equal(handshakeSteps.length, 4)
assert.equal(closingSteps.length, 5)
assert.deepEqual(windowScenarios.map(s => availableWindow(s.rwnd, s.cwnd, s.flight)), [1000, 200, 400])
assert.equal(availableWindow(1000, 2000, 1500), 0)
assert.equal(routeStates.length, routeSteps.length)
assert.deepEqual(routeStates.map(s => s.distances.C), [Infinity, 5, 3, 3, 3])
assert.deepEqual(routeStates.map(s => s.distances.D), [Infinity, Infinity, 7, 4, 4])
assert.deepEqual(routeStates.at(-1).previous, { B: 'A', C: 'B', D: 'C' })
assert.deepEqual(routeStates.at(-1).settled, ['A', 'B', 'C', 'D'])
assert.equal(traceShortestPaths(routeEdges.filter(e => !(e.from === 'B' && e.to === 'C'))).at(-1).distances.D, 6)
const expectedKinds = new Set(['layers','encapsulation','ports','hop','segmentation','handshake','stream','window','closing','signal','frame','subnet','route','dns','trust'])
const used = new Set()
const tutorials = { ...chapterTutorials, ...networkFoundations }
assert.deepEqual(Object.keys(tutorials).sort(), ['网络体系结构','物理层基础','数据链路层','网络层与 IP','路由选择','TCP 与 UDP','应用层协议','网络安全基础'].sort())
for (const [chapter, tutorial] of Object.entries(tutorials)) {
  assert.ok(tutorial.sections.length >= 6, chapter)
  assert.equal(new Set(tutorial.sections.map(s => s.id)).size, tutorial.sections.length)
  for (const section of tutorial.sections) {
    assert.match(section.id, /^[a-z][a-z-]+$/)
    assert.ok(section.paragraphs.length >= 2)
    assert.ok(section.paragraphs.every(p => p.length > 40))
    if (section.figure) { assert.ok(expectedKinds.has(section.figure)); used.add(section.figure) }
    if (section.table) for (const row of section.table.rows) assert.equal(row.length, section.table.headings.length)
    if (section.check) assert.ok(section.check.answer.length > 25)
  }
  assert.ok(tutorial.sources.length > 0)
  assert.ok(tutorial.sources.every(source => ['www.rfc-editor.org', 'www.cisco.com', 'ocw.mit.edu'].includes(new URL(source.href).hostname)))
  console.log(`${chapter}: ${tutorial.sections.length} sections, ${tutorial.sections.filter(s => s.figure).length} explanatory figures`)
}
assert.deepEqual(used, expectedKinds)
console.log('PASS: exact byte ranges, cumulative ACK examples, handshake/close stages, window calculations, chapter structure and figure coverage.')
