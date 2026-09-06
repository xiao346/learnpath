<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { illustrationFor } from '../content/knowledgeIllustrations'
import { pictureReadings } from '../content/pictureReadings'

const props = defineProps<{ chapterTitle: string; courseTitle: string; pointIndex: number; title: string }>()
const picture = computed(() => illustrationFor(props.courseTitle, props.chapterTitle, props.pointIndex))
const reading = computed(() => pictureReadings[props.chapterTitle]?.[props.pointIndex])
const dialog = ref<HTMLDialogElement | null>(null)
const trigger = ref<HTMLButtonElement | null>(null)
const failed = ref(false)
const crop = computed(() => {
  const frame = picture.value?.frame
  if (!frame) return {}
  return { left: `-${frame.x / frame.width * 100}%`, top: `-${frame.y / frame.height * 100}%`, width: `${1330 / frame.width * 100}%`, height: `${1182 / frame.height * 100}%` }
})
function close() { if (dialog.value?.open) { dialog.value.close(); trigger.value?.focus() } }
watch(() => [props.chapterTitle, props.pointIndex], () => { close(); failed.value = false })
</script>

<template>
  <figure v-if="picture" class="knowledge-picture">
    <button v-if="!failed" ref="trigger" class="picture-trigger" type="button" :aria-label="`放大图片：${picture.alt}`" @click="dialog?.showModal()">
      <span class="picture-window" :style="{ aspectRatio: `${picture.frame.width} / ${picture.frame.height}` }">
        <img :src="picture.src" :alt="picture.alt" :style="crop" loading="lazy" decoding="async" @error="failed = true" />
      </span>
      <span class="picture-zoom" aria-hidden="true">⤢ 点击放大</span>
    </button>
    <p v-else class="picture-error">图片暂时未能加载，请刷新页面重试。</p>
    <figcaption><span>看图理解</span><p>{{ picture.caption }}</p></figcaption>
    <div v-if="reading" class="picture-reading"><h5>例子与解析</h5><p>{{ reading }}</p></div>
    <dialog ref="dialog" class="picture-dialog" :aria-label="title" @click="event => { if (event.target === dialog) close() }" @cancel.prevent="close">
      <header><strong>{{ title }}</strong><button autofocus type="button" aria-label="关闭大图" @click="close">关闭 ×</button></header>
      <div class="picture-window" :style="{ aspectRatio: `${picture.frame.width} / ${picture.frame.height}` }"><img :src="picture.src" :alt="picture.alt" :style="crop" decoding="async" /></div>
      <p>{{ picture.caption }}</p>
      <section v-if="reading" class="enlarged-reading"><h5>例子与解析</h5><p>{{ reading }}</p></section>
    </dialog>
  </figure>
</template>

<style scoped>
.knowledge-picture { margin: 22px 0; }
.picture-trigger { display: block; position: relative; width: min(100%, 680px); margin-inline: auto; padding: 0; border: 0; border-radius: 14px; overflow: hidden; background: #fbf2df; cursor: zoom-in; }
.picture-window { display: block; position: relative; aspect-ratio: 3 / 2; overflow: hidden; width: 100%; background: #fbf2df; }
.picture-window img { position: absolute; width: 300%; height: 400%; max-width: none; object-fit: fill; display: block; }
.picture-zoom { position: absolute; right: 12px; bottom: 12px; padding: 7px 11px; border-radius: 20px; background: rgba(18,29,49,.82); color: #fff; font-size: 12px; }
.picture-trigger:focus-visible { outline: 3px solid #7bdacd; outline-offset: 5px; }
figcaption { max-width: 680px; margin: 13px auto 0; display: grid; grid-template-columns: auto 1fr; gap: 12px; align-items: baseline; text-align: left; }
figcaption > span { color: #88e2cc; font-size: 12px; font-weight: 600; white-space: nowrap; }
figcaption p { margin: 0; color: #c3cee8; font-size: 14px; line-height: 1.9; }
.picture-reading { max-width: 680px; margin: 24px auto 0; padding-top: 18px; border-top: 1px solid rgba(136,226,204,.2); }
.picture-reading h5, .enlarged-reading h5 { margin: 0 0 8px; color: #a8ead8; font-size: 14px; font-weight: 600; }
.picture-reading p { margin: 0; color: #e2e9fa; font-size: 16px; line-height: 2; overflow-wrap: anywhere; }
.enlarged-reading { margin-top: 20px; padding-top: 16px; border-top: 1px solid #405472; }
.picture-error { color: #e7b486; font-size: 14px; }
.picture-dialog { width: min(900px, calc(100vw - 32px)); max-height: calc(100dvh - 40px); box-sizing: border-box; padding: 20px; border: 1px solid #4e638c; border-radius: 18px; color: #edf2ff; background: #131d38; overflow-y: auto; }
.picture-dialog::backdrop { background: rgba(2,7,20,.86); }
.picture-dialog header { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 15px; }
.picture-dialog strong { font-size: 17px; line-height: 1.6; }
.picture-dialog button { padding: 8px 12px; white-space: nowrap; color: #fff; border: 1px solid #667aa7; border-radius: 8px; background: #263859; cursor: pointer; }
.picture-dialog p { margin: 15px 0 0; font-size: 15px; line-height: 1.8; }
.picture-dialog .picture-window { border-radius: 10px; }
@media (max-width: 600px) { figcaption { grid-template-columns: 1fr; gap: 5px; } .picture-dialog { padding: 12px; } }
@media print { .picture-zoom, .picture-dialog { display: none !important; } .picture-trigger { break-inside: avoid; } figcaption p, figcaption > span, .picture-reading h5, .picture-reading p { color: #222; } }
</style>
