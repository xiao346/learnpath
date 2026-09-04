<script setup lang="ts">
import type { LearningStep } from '../services/api'

defineProps<{ steps: LearningStep[]; title: string }>()

const positions = [
  { x: 40, y: 30 }, { x: 420, y: 30 },
  { x: 420, y: 180 }, { x: 40, y: 180 },
  { x: 40, y: 330 }, { x: 420, y: 330 },
]
</script>

<template>
  <figure class="chapter-map">
    <svg viewBox="0 0 760 460" role="img" :aria-label="`${title}知识总图`">
      <defs>
        <marker id="chapter-map-arrow" markerWidth="10" markerHeight="10" refX="8" refY="5" orient="auto">
          <path d="M0,0 L10,5 L0,10 Z" fill="#7089dd" />
        </marker>
      </defs>
      <path class="map-line" d="M340 80 H410" />
      <path class="map-line" d="M570 130 V170" />
      <path class="map-line" d="M420 230 H350" />
      <path class="map-line" d="M190 280 V320" />
      <path class="map-line" d="M340 380 H410" />
      <foreignObject v-for="(step, index) in steps.slice(0, 6)" :key="step.id" :x="positions[index].x" :y="positions[index].y" width="300" height="100">
        <div xmlns="http://www.w3.org/1999/xhtml" class="map-node" :class="`tone-${index % 3}`">
          <span>{{ String(index + 1).padStart(2, '0') }}</span>
          <div><small>{{ step.stage }}</small><strong>{{ step.title }}</strong></div>
        </div>
      </foreignObject>
    </svg>
    <figcaption>从左到右、从上到下依次学习</figcaption>
  </figure>
</template>

<style scoped>
.chapter-map { margin: 22px 0 0; padding: 16px; border: 1px solid rgba(115,140,225,.18); border-radius: 16px; background: radial-gradient(circle at 50% 0, rgba(66,142,190,.12), transparent 55%), rgba(6,11,35,.4); }
.chapter-map svg { width: 100%; height: auto; display: block; }
.map-line { fill: none; stroke: #7089dd; stroke-width: 3; stroke-linecap: round; marker-end: url(#chapter-map-arrow); }
.map-node { height: 100%; box-sizing: border-box; padding: 16px; display: grid; grid-template-columns: 42px 1fr; align-items: center; gap: 13px; border: 1px solid rgba(120,151,232,.28); border-radius: 14px; color: #edf1ff; background: linear-gradient(135deg, #1c285d, #11183f); font-family: Inter, "Microsoft YaHei", sans-serif; }
.map-node > span { width: 38px; height: 38px; display: grid; place-items: center; border-radius: 11px; color: #83e3ef; background: rgba(69,185,207,.13); font-size: 11px; font-weight: 700; }
.map-node small { display: block; margin-bottom: 6px; color: #919cff; font-size: 10px; letter-spacing: 1px; }
.map-node strong { display: block; font-size: 14px; line-height: 1.55; }
.tone-1 { border-color: rgba(147,125,244,.3); background: linear-gradient(135deg, #26205e, #14163e); }
.tone-2 { border-color: rgba(75,201,168,.25); background: linear-gradient(135deg, #173e52, #101b3d); }
figcaption { margin-top: 6px; color: #7380ab; font-size: 10px; text-align: center; }
@media (max-width: 720px) { .chapter-map { padding: 8px; overflow-x: auto; } .chapter-map svg { min-width: 680px; } }
</style>
