<script setup lang="ts">
import { computed, ref, useId } from 'vue'
import type { TutorialSection } from '../content/chapterTutorials'
import { students, enrollments, selectionRows, joinedRows, transferStages, databaseFigureNames } from '../content/databaseExamples'
const props = defineProps<{ kind: NonNullable<TutorialSection['databaseFigure']> }>()
const step = ref(0)
const rolledBack = ref(false)
const marker = `database-arrow-${useId().replace(/[^\w-]/g, '')}`
const arrow = `url(#${marker})`
const titles = computed(() => props.kind==='transaction' ? transferStages.map(s=>s.title) : props.kind==='selection' ? ['原始选课行','只留下score ≥ 80的行','投影：只保留学号'] : props.kind==='join' ? ['先看学生表与选课表','沿学号101匹配，得到两条选课','完成匹配：三个学生对应四条选课'] : props.kind==='index' ? ['从根页比较目标键27','27落在中间叶页','范围[27,42]沿叶子继续向右'] : [])
const caption = computed(() => {
  if (rolledBack.value) return '回滚后恢复A=500、B=300。事务中的扣款和加款都未作为本次转账结果保留；这不是再创建一笔反向业务转账。'
  if (props.kind==='transaction') return transferStages[step.value]!.detail
  if (props.kind==='selection') return ['每行表示一条选课，不是一名学生；101出现两次，因为他选了两门课。NULL表示尚无成绩。','90和85符合条件。70被过滤；NULL与80比较得到未知，WHERE也不会保留。注意列仍然有三列。','保留学号列后得到101、103。经典关系代数按集合解释投影；普通SQL SELECT不会自动去重，需要时使用DISTINCT。'][step.value]
  if (props.kind==='join') return ['先按学生编号认人，再找到所有student_id相等的选课行。姓名只是展示字段，不用姓名相同来证明是同一个人。','学生101与两条选课行匹配，所以小林出现两次。这是一个学生对应多条选课的结果，并非数据库重复读取了错误数据。','三名学生合计四条选课，内连接产生四行。若学生没有任何选课，内连接不保留他；需要保留时考虑LEFT JOIN。'][step.value]
  if (props.kind==='index') return ['本图是人为构造的两层B+树，不是某个数据库真实页布局。根页分隔键20、40把查找空间划成三个范围。','27 ≥ 20且27 < 40，因此进入中间叶页，找到27，不必依次扫描左侧全部键。命中索引后是否还要访问表数据取决于索引与查询。','找到起点27后，读取27、31；再沿叶子链接取得42。50超过上界，停止。这解释了有序索引为何适合范围检索。'][step.value]
  return {schema:'两种视图可读取同一套逻辑数据。DBMS负责解释查询、检查权限、安排访问并管理存储；箭头是职责路线，不是精确执行计划。',foreign:'101能连到已存在的学生行；1099找不到目标。本例student_id同时为NOT NULL，因此缺省引用也不能靠NULL绕开。主键负责唯一标识学生，外键负责引用关系。',normalize:'原表把学生姓名重复写在每条选课中。拆分后，姓名在学生表保存一次，选课表保留学号与课程。连线表示相等编号，不是把两张表按行号拼起来。'}[props.kind]
})
const visibleEnrollment = computed(() => props.kind==='selection' && step.value>0 ? selectionRows : enrollments)
const shownJoins = computed(() => step.value===1 ? joinedRows.slice(0,2) : joinedRows)
const balances = computed(() => rolledBack.value ? [500,300] : transferStages[step.value]!.balances)
function restart() { step.value=0; rolledBack.value=false }
</script>
<template>
  <figure class="mechanism-figure database-figure">
    <header><span>原理图解</span><strong>{{ databaseFigureNames[kind] }}</strong></header>
    <small class="scroll-hint">窄屏可左右滑动查看完整图，图下有逐步解析。</small>
    <div class="diagram-scroll" tabindex="0" :aria-label="`${databaseFigureNames[kind]}，可横向滚动`">
      <svg class="network-svg" viewBox="0 0 780 380" role="img" :aria-label="databaseFigureNames[kind]">
        <title>{{ databaseFigureNames[kind] }}</title><desc>{{ caption }}</desc>
        <defs><marker :id="marker" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0L10 5L0 10Z" fill="#476a85" /></marker></defs>
        <g v-if="kind==='schema'">
          <g v-for="(label,i) in ['学生：只看自己的成绩','教师：查看本课程成绩']" :key="label"><rect :x="45+i*390" y="20" width="300" height="58" rx="8" fill="#cbe3f6"/><text :x="195+i*390" y="54">{{ label }}</text><path :d="`M${195+i*390} 84V106H390V126`" class="path" :marker-end="arrow"/></g>
          <rect x="200" y="140" width="380" height="78" rx="9" fill="#e2dcf7"/><text x="390" y="171" class="major">DBMS：查询、权限、并发与恢复</text><text x="390" y="200" class="minor">逻辑表：学生 — 选课 — 课程</text><path d="M390 224V260" class="path" :marker-end="arrow"/>
          <path d="M255 289V342C255 366 525 366 525 342V289" fill="#ccebd9" stroke="#609677"/><ellipse cx="390" cy="289" rx="135" ry="20" fill="#ccebd9" stroke="#609677"/><text x="390" y="336">数据页、索引页与日志</text>
        </g>
        <g v-else-if="kind==='selection'">
          <text x="220" y="30" class="major">选课表：一行是一条选课</text>
          <g v-for="(row,i) in [['学号','课程','成绩'],...visibleEnrollment]" :key="i"><g v-for="(cell,j) in row" :key="j"><rect :x="35+j*125" :y="50+i*49" width="125" height="49" :fill="i===0 ? '#e2dcf7' : '#cbe3f6'" stroke="#739ab7"/><text :x="97+j*125" :y="81+i*49">{{ cell===null ? 'NULL' : cell }}</text></g></g>
          <path d="M430 167H500" class="path" :marker-end="arrow"/>
          <text x="626" y="101">{{ step===0 ? '观察原始4行' : step===1 ? '行数：4 → 2' : '列数：3 → 1' }}</text>
          <g v-if="step===2"><rect x="548" y="129" width="155" height="49" fill="#ccebd9"/><rect x="548" y="182" width="155" height="49" fill="#ccebd9"/><text x="625" y="161">101</text><text x="625" y="214">103</text></g>
          <text x="390" y="349" class="minor">{{ step===2 ? 'π 学号 ( σ 成绩≥80 (选课) )' : 'σ 选择操作改变行；π 投影操作改变属性集合' }}</text>
        </g>
        <g v-else-if="kind==='join' || kind==='foreign'">
          <text x="175" y="28" class="major">学生表</text><text x="578" y="28" class="major">{{ kind==='join' ? '选课表' : '待检查的学号引用' }}</text>
          <g v-for="(student,i) in students" :key="student[0]"><rect x="35" :y="50+i*53" width="280" height="43" rx="4" :fill="i===0 ? '#ccebd9' : '#cbe3f6'"/><text x="175" :y="78+i*53">{{ student[0] }}　{{ student[1] }}　{{ student[2] }}</text></g>
          <g v-if="kind==='join'"><g v-for="(row,i) in enrollments" :key="i"><rect x="450" :y="50+i*47" width="280" height="39" rx="4" :fill="i<2 ? '#ccebd9' : '#cbe3f6'"/><text x="590" :y="76+i*47">{{ row[0] }}　{{ row[1] }}　{{ row[2]===null ? 'NULL' : row[2] }}</text></g><g v-if="step>0"><path d="M323 72H441M323 72L441 117" class="path" :marker-end="arrow"/><path v-if="step===2" d="M323 125L441 164M323 178L441 211" class="path" :marker-end="arrow"/><text x="390" y="277" class="major">连接结果：{{ shownJoins.map(r=>`${r[0]}·${r[1]}`).join('，') }}</text><text x="390" y="320" class="minor">按编号相等匹配，一对多关系会使姓名在结果中重复出现</text></g></g>
          <g v-else><rect x="450" y="50" width="280" height="55" rx="4" fill="#ccebd9"/><text x="590" y="84">101 · 找到父表行 ✓</text><path d="M441 76H323" class="path" :marker-end="arrow"/><rect x="450" y="155" width="280" height="55" rx="4" fill="#ffe3dc"/><text x="590" y="189">1099 · 找不到父表行 ×</text><path d="M440 184H363" class="relation"/><path d="M347 170L373 196M347 196L373 170" stroke="#b94132" stroke-width="3"/><text x="390" y="288" class="major">不存在的非空引用被拒绝，不能留下悬空关系</text></g>
        </g>
        <g v-else-if="kind==='normalize'">
          <text x="203" y="27" class="major">拆分前：同一姓名重复保存</text><g v-for="(label,i) in ['101  小林  C1  90','101  小林  C2  70']" :key="label"><rect x="38" :y="53+i*57" width="326" height="47" fill="#ffe0ad"/><text x="201" :y="84+i*57">{{ label }}</text></g>
          <path d="M383 103H428" class="path" :marker-end="arrow"/><text x="588" y="27" class="major">拆分后：事实分别存放</text><rect x="446" y="53" width="296" height="47" fill="#cbe3f6"/><text x="594" y="84">学生：101　小林</text>
          <path d="M590 107V145" class="path" :marker-end="arrow"/><g v-for="(label,i) in ['选课：101  C1  90','选课：101  C2  70']" :key="label"><rect x="446" :y="156+i*57" width="296" height="47" fill="#ccebd9"/><text x="594" :y="187+i*57">{{ label }}</text></g>
          <text x="207" y="236">改名时要同时改两行</text><text x="390" y="321" class="major">学号 → 姓名；(学号,课程号) → 成绩</text><text x="390" y="353" class="minor">这条函数依赖来自业务规则，不是从当前两条数据偶然猜出来的</text>
        </g>
        <g v-else-if="kind==='transaction'">
          <text x="390" y="31" class="major">{{ rolledBack ? 'ROLLBACK：恢复初始状态' : titles[step] }}</text>
          <g v-for="(value,i) in balances" :key="i"><rect :x="100+i*410" y="90" width="170" height="185" rx="10" fill="#eef1f5"/><rect :x="120+i*410" :y="254-value/5" width="130" :height="value/5" rx="5" :fill="i===0 ? '#cbe3f6' : '#ccebd9'"/><text :x="185+i*410" y="121">账户{{ i===0 ? 'A' : 'B' }}</text><text :x="185+i*410" y="305" class="major">余额 {{ value }}</text></g>
          <path d="M292 183H478" class="path" :marker-end="arrow"/><text x="390" y="163">转账100</text><text x="390" y="352" class="major">当前示例合计：{{ balances[0]!+balances[1]! }}</text>
        </g>
        <g v-else-if="kind==='index'">
          <rect x="277" y="30" width="226" height="58" rx="6" fill="#e2dcf7"/><text x="390" y="67" class="major">分隔键　20 | 40</text><path d="M302 94L138 175M390 94V175M478 94L645 175" class="path" :marker-end="arrow"/>
          <g v-for="(keys,i) in [[3,8,12],[20,27,31],[42,50,65]]" :key="i"><rect :x="35+i*255" y="186" width="205" height="63" rx="5" :fill="step>0 && (i===1 || step===2 && i===2) ? '#ccebd9' : '#cbe3f6'"/><text :x="137+i*255" y="223">{{ keys.join(' | ') }}</text><text :x="137+i*255" y="282" class="minor">{{ ['键 < 20','20 ≤ 键 < 40','键 ≥ 40'][i] }}</text></g>
          <path d="M245 220H282M500 220H537" class="path" :marker-end="arrow"/><text x="390" y="341" class="major">{{ step===0 ? '查找27：先比较分隔键' : step===1 ? '命中中间叶页中的27' : '范围结果：27、31、42；到50停止' }}</text>
        </g>
      </svg>
    </div>
    <div v-if="titles.length" class="figure-controls" role="group" :aria-label="`${databaseFigureNames[kind]}步骤控制`">
      <button type="button" :disabled="step===0 || rolledBack" @click="step--">← 上一步</button><span>{{ rolledBack ? '已回滚' : `${step+1} / ${titles.length}` }}</span><button type="button" :disabled="step===titles.length-1 || rolledBack" @click="step++">下一步 →</button>
      <button v-if="kind==='transaction'" type="button" :disabled="step===0 || step===3 || rolledBack" @click="rolledBack=true">此时回滚</button><button type="button" :disabled="step===0 && !rolledBack" @click="restart">重看</button>
    </div>
    <figcaption aria-live="polite" aria-atomic="true"><strong v-if="titles.length">{{ rolledBack ? '全部撤销本次事务更新' : titles[step] }}</strong><p>{{ caption }}</p></figcaption>
  </figure>
</template>
<style scoped>
.database-figure { margin:30px 0; padding:20px 0; border-block:1px solid #d9e4ed; }
header { display:flex; flex-wrap:wrap; gap:14px; margin-bottom:18px; } header span { font-size:12px; } header strong { font-size:16px; }
.diagram-scroll { overflow-x:auto; } .network-svg { display:block; width:100%; min-width:620px; font:16px 'Microsoft YaHei',sans-serif; color:#24425a; }
text { fill:currentColor; text-anchor:middle; } .major { font-weight:600; font-size:17px; } .minor { font-size:14px; fill:#526b7d; }
.path { fill:none; stroke:#476a85; stroke-width:2; } .relation { fill:none; stroke:#8196a7; stroke-width:2; stroke-dasharray:6 5; }
.scroll-hint { display:block; font-size:12px; margin-bottom:12px; } .figure-controls { display:flex; flex-wrap:wrap; align-items:center; gap:10px; margin:18px 0; }
.figure-controls button { padding:9px 12px; border:1px solid #b5c8d6; background:#fff; color:#245775; border-radius:6px; font-size:14px; min-height:42px; }
.figure-controls button:disabled { opacity:.45; cursor:default; } .figure-controls span { font-size:13px; }
figcaption { font-size:15px; line-height:1.9; } figcaption p { margin:8px 0 0; }
button:focus-visible,.diagram-scroll:focus-visible { outline:2px solid #8cdddc; outline-offset:4px; }
@media print { .figure-controls,.scroll-hint { display:none; } .network-svg { min-width:0; } .diagram-scroll { overflow:visible; } }
</style>
