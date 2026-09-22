<script setup lang="ts">
import { computed } from 'vue'
import type { JourneyStageId } from '../services/journey'
import { helpForStage } from '../content/beginnerGuidance'
const props = defineProps<{ stage: JourneyStageId }>()
const items = computed(() => helpForStage(props.stage))
</script>

<template>
  <details class="beginner-help">
    <summary>卡住了？按你看到的现象找办法 <span>＋</span></summary>
    <p>先选最接近的现象，按顺序检查。解决后就回到当前任务。</p>
    <details v-for="item in items" :key="item.title"><summary>{{ item.title }}</summary><ol><li v-for="step in item.steps" :key="step">{{ step }}</li></ol></details>
    <p>还没解决：记下“我想做什么、实际发生什么、完整报错、已经试过什么”，带上这四项去 <RouterLink to="/community">建站社区提问 →</RouterLink></p>
  </details>
</template>

<style scoped>
.beginner-help { margin-top: 18px; padding: 16px 18px; border: 1px solid var(--border); border-radius: 14px; background: rgba(127,157,180,.06); color: var(--text, #18324a); }
summary { cursor: pointer; font-weight: 600; line-height: 1.6; }
summary span { float: right; }
p, li { color: var(--muted); font-size: 14px; line-height: 1.85; }
details details { padding: 12px 0; border-top: 1px solid var(--border); }
ol { padding-left: 23px; } li + li { margin-top: 8px; }
a { color: #376c96; }
</style>
