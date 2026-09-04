<script setup lang="ts">
import { computed } from 'vue'
import type { DiagramStep } from '../services/api'

const props = defineProps<{ id: string; title: string; steps: DiagramStep[] }>()

const width = 760
const gap = 24
const nodeWidth = computed(() => props.steps.length === 2 ? 280 : props.steps.length === 3 ? 210 : 155)
const startX = computed(() => (width - (nodeWidth.value * props.steps.length + gap * (props.steps.length - 1))) / 2)
const nodeX = (index: number) => startX.value + index * (nodeWidth.value + gap)
const markerId = computed(() => `point-arrow-${props.id}`)
</script>

<template>
  <figure class="point-diagram">
    <svg viewBox="0 0 760 210" role="img" :aria-label="`${title}图解`">
      <defs>
        <marker :id="markerId" markerWidth="10" markerHeight="10" refX="8" refY="5" orient="auto">
          <path d="M0,0 L10,5 L0,10 Z" fill="#7289d9" />
        </marker>
      </defs>
      <line
        v-for="(_, index) in steps.slice(0, -1)" :key="`line-${index}`"
        :x1="nodeX(index) + nodeWidth" y1="105" :x2="nodeX(index + 1) - 7" y2="105"
        class="diagram-line" :style="{ markerEnd: `url(#${markerId})` }"
      />
      <foreignObject v-for="(step, index) in steps" :key="`${id}-${index}`" :x="nodeX(index)" y="30" :width="nodeWidth" height="150">
        <div xmlns="http://www.w3.org/1999/xhtml" class="diagram-node" :class="`tone-${index % 3}`">
          <small>{{ step.label }}</small><strong>{{ step.content }}</strong>
        </div>
      </foreignObject>
    </svg>
    <figcaption>{{ title }} · 图解</figcaption>
  </figure>
</template>

<style scoped>
.point-diagram { margin: 14px 0 0; border: 1px solid rgba(105,139,220,.22); border-radius: 14px; overflow-x: auto; background: radial-gradient(circle at 50% 20%, rgba(62,137,183,.12), transparent 60%), #080d2a; }
.point-diagram svg { width: 100%; min-width: 660px; height: auto; display: block; }
.diagram-line { stroke: #7289d9; stroke-width: 3; stroke-linecap: round; }
.diagram-node { height: 100%; box-sizing: border-box; padding: 16px 14px; display: flex; flex-direction: column; justify-content: center; border: 1px solid rgba(120,151,232,.27); border-radius: 14px; color: #dfe5f8; background: linear-gradient(145deg, #202a62, #131a43); text-align: center; font-family: Inter, "Microsoft YaHei", sans-serif; }
.diagram-node small { color: #89dbea; font-size: 10px; letter-spacing: .8px; }
.diagram-node strong { margin-top: 9px; font-size: 13px; line-height: 1.7; }
.tone-1 { border-color: rgba(146,127,238,.3); background: linear-gradient(145deg, #292360, #181842); }
.tone-2 { border-color: rgba(75,199,166,.25); background: linear-gradient(145deg, #194153, #111d3f); }
figcaption { padding: 0 12px 12px; color: #7180ac; font-size: 9px; text-align: center; }
</style>
