import type { FirstPageData } from '../services/journey'

export function escapeHtml(value: string) {
  return value.replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char]!)
}

export function buildFirstPageDocument(data: FirstPageData, section: string) {
  return `<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${escapeHtml(data.name)}的网站</title>
</head>
<body>
  <main>
    <h1>${escapeHtml(data.name)}的网站</h1>
    <p>${escapeHtml(data.introduction)}</p>
    <section class="intro-card" id="first-item">
      <h2>${escapeHtml(section)}</h2>
      <p>${escapeHtml(data.interest)}</p>
    </section>
    <a href="#first-item">回到${escapeHtml(section)}</a>
  </main>
</body>
</html>`
}
