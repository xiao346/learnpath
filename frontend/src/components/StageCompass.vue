<script setup lang="ts">
import { computed } from 'vue'
import { getStageGuidance } from '../content/beginnerGuidance'
import type { JourneyConfig, JourneyStageId } from '../services/journey'
import BeginnerHelp from './BeginnerHelp.vue'
const props = defineProps<{ config: Partial<JourneyConfig>; stage: JourneyStageId; compact?: boolean }>()
const guide = computed(() => getStageGuidance(props.config, props.stage))
</script>

<template>
  <section class="stage-compass" :class="{ compact }" aria-label="这一站为什么做">
    <p class="compass-why"><b>为什么做这一步</b>{{ guide.why }}</p>
    <div class="compass-change"><span>{{ guide.before }}</span><b aria-label="变成">→</b><strong>{{ guide.after }}</strong></div>
    <details class="compass-scope"><summary>这一站学到哪里就够了？</summary><div class="compass-boundaries"><div><small>现在用得上的</small><p>{{ guide.need }}</p></div><div><small>做到这里就够了</small><p>{{ guide.enough }}</p></div><div><small>以后再慢慢学</small><p>{{ guide.later }}</p></div></div></details>
    <BeginnerHelp v-if="!compact" :stage="stage" />
  </section>
</template>

<style scoped>
.stage-compass { margin: 22px 0; padding: 24px; background: rgba(127,157,180,.07); border: 1px solid var(--border); border-radius: 18px; }
.stage-compass.compact { padding: 18px; margin: 18px 0 0; }
.compass-why { margin: 0; color: var(--muted); line-height: 1.8; font-size: 14px; }
.compass-why b { display: block; color: var(--text, #18324a); margin-bottom: 4px; }
.compass-change { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; margin: 16px 0; font-size: 14px; line-height: 1.6; }
.compass-change span { color: var(--muted); }.compass-change strong { color: #376c96; }
.compass-scope > summary { cursor: pointer; color: #376c96; font-size: 14px; line-height: 1.7; }.compass-scope[open] > summary { margin-bottom: 15px; }
.compass-boundaries { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 20px; border-top: 1px solid var(--border); padding-top: 16px; }
small { color: #376c96; font-weight: 600; }.compass-boundaries p { margin: 6px 0 0; color: var(--muted); font-size: 13px; line-height: 1.8; }
@media(max-width: 760px) { .compass-boundaries { grid-template-columns: 1fr; gap: 12px; }.stage-compass { padding: 18px; } }
</style>
