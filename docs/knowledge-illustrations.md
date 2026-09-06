# 章节教学图片与解析

使用内置 image_gen 生成（非 CLI）。8 张原始图集各包含 3 列 × 4 行，共 96 个场景；按相关知识点复用，不声称 348 张不同图片。图片是类比，精确数据、例子及适用边界由可维护的 HTML 文本说明。

图片在 frontend/public/images/knowledge/，全部保留于项目。前端按图集裁切显示单个场景，未进行栅格后期编辑。

- 图片映射：frontend/src/content/knowledgeIllustrations.ts
- 每点例子解析：frontend/src/content/pictureReadings.ts（58 × 6）
- 章末变式与答案：frontend/src/content/chapterChecks.ts（58）
- 图解、放大与无障碍：frontend/src/components/KnowledgePointDiagram.vue

## 最终提示词及保存路径

### database

项目路径：`frontend/public/images/knowledge/database.png`

```text
Use case: scientific-educational. Asset type: database teaching illustration sprite atlas for a Chinese beginner course. Create ONE large landscape image containing exactly 12 equally sized pictorial scenes in a precise 3-column by 4-row grid. Each tile has aspect ratio 3:2, whole image aspect 9:8. NO gutters or panel borders; every scene contained inside its equal tile with generous 10% inner margin, consistent pale ivory background. Crisp charming editorial 3D clay / isometric educational illustration, rich teal, indigo, coral, warm amber; visible real objects, people and meaningful actions, not text cards or abstract infographic boxes. Absolutely NO words, letters, captions, numerals, logos or watermarks. Read left to right row by row:
1 librarian organizing many folders into one central filing cabinet, student at desk asking for a record (DBMS).
2 cutaway three storeys of archive: students at top viewing select files, middle orderly catalog, bottom physical disks (three schemas).
3 two tidy grid-like trays of student portrait tokens joined by matching colored key-shaped tokens (relational primary foreign keys).
4 a hand lifting only green tokens from mixed red/green rows, spotlight selecting matching rows (SQL filter).
5 two trays with matching colored tokens aligning and interlocking like a zipper (join).
6 grouped colored fruit baskets on a scale measuring each group's total (aggregation).
7 archive entrance guarded by inspector rejecting duplicate and broken folders while allowing intact unique folder (constraints).
8 oversized repetitive messy file split into three clean linked folders, same portrait stored only once (normalization).
9 two coin jars exchanging one sealed coin parcel inside one glass enclosure; small undo semicircle (atomic transfer).
10 two clerks politely waiting for one shared cabinet key while another reads a dated photocopy (locks MVCC).
11 librarian follows small colored bookmark index directly to one book on a very long shelf (index).
12 engineer with magnifying glass comparing a long shelf search route to a short indexed route, tiny stopwatch object (query plan).
The imagery itself must make each concept concrete and memorable. Keep every scene self-contained and do not merge scenes. No text of any kind.
```

### algorithms

项目路径：`frontend/public/images/knowledge/algorithms.png`

```text
Use case: scientific-educational. Create ONE teaching illustration sprite atlas with exactly 12 scenes in a precise 3 columns by 4 rows equal grid, whole image aspect 9:8 (each scene 3:2). No gutters or borders. 10% inner safe margins inside each equal tile. Pale ivory background. Charming detailed 3D clay/isometric editorial illustration, teal indigo coral amber. Concrete recognizable objects, people doing meaningful actions, rich texture, expressive and clear. Absolutely NO text, words, letters, numerals, captions, logos or watermark. No text cards or abstract rectangle diagrams. Each tile self-contained. Read left to right, top to bottom, scenes: 1 cook following visible recipe actions weighing chopping cooking, algorithm finite steps; 2 one student scanning an entire bookshelf versus another halving an ordered shelf with two hands, binary search; 3 repeated nested rows of marbles with stopwatch and small stack of empty bowls, time and space costs; 4 a tidy row of theater seats with people shifting one seat to let newcomer insert, arrays; 5 adjustable drawer organizer expanding with added empty compartments and copied colored balls, dynamic capacity; 6 stack of plates with hand taking top plate next to a cafeteria queue served from front, LIFO FIFO; 7 circular conveyor of colored parcels with separate loading and unloading worker, circular queue; 8 branching wooden tree of balls with root and two child subtrees, traversal walker moving down one branch; 9 toy city rail network, traveler exploring neighboring stations, graph; 10 mail sorting cubbies with two different envelopes assigned same slot, hash collision; 11 hands sorting differently sized colored playing tiles by moving one into an ordered row, sorting stability; 12 small workshop combining searchable labeled-by-colors drawers and priority parcel conveyor, integrated task scheduler.
```

### web

项目路径：`frontend/public/images/knowledge/web.png`

```text
Use case: scientific-educational. Create ONE teaching illustration sprite atlas with exactly 12 scenes in a precise 3 columns by 4 rows equal grid, whole image aspect 9:8 (each scene 3:2). No gutters or borders. 10% inner safe margins inside each equal tile. Pale ivory background. Charming detailed 3D clay/isometric editorial illustration, teal indigo coral amber. Concrete recognizable objects, people doing meaningful actions, rich texture, expressive and clear. Absolutely NO text, words, letters, numerals, captions, logos or watermark. No text cards or abstract rectangle diagrams. Each tile self-contained. Read left to right, top to bottom, scenes: 1 restaurant cutaway customer waiter cook pantry, browser controller service database; 2 developer connecting ready-made toy building parts into a working small web server, boot starters; 3 two workshop workers passing required tool through constructor-shaped doorway, dependency injection; 4 four hands at library counter borrowing viewing replacing returning books with different gesture icons, REST operations; 5 clipboard figurine transforming into one persistent record in filing cabinet, JPA; 6 doorman examining form with missing portrait and returning it with highlighted blank field, validation; 7 employee showing identity badge at office lobby then restricted room gate checking role, auth; 8 desk holding often-used books next to far bookshelf, worker replacing outdated desktop copy, cache; 9 crowd arriving to empty cache shelf with one worker restocking while others wait, cache stampede; 10 testing engineer feeding good and broken toy parcels into tester with green and red lamps, API tests; 11 two linked factory machines being tested together using sample parcel, integration test; 12 packaged application crate delivered into server room with health lamp and spare rollback crate, deployment.
```

### network

项目路径：`frontend/public/images/knowledge/network.png`

```text
Use case: scientific-educational. Create ONE teaching illustration sprite atlas with exactly 12 scenes in a precise 3 columns by 4 rows equal grid, whole image aspect 9:8 (each scene 3:2). No gutters or borders. 10% inner safe margins inside each equal tile. Pale ivory background. Charming detailed 3D clay/isometric editorial illustration, teal indigo coral amber. Concrete recognizable objects, people doing meaningful actions, rich texture, expressive and clear. Absolutely NO text, words, letters, numerals, captions, logos or watermark. No text cards or abstract rectangle diagrams. Each tile self-contained. Read left to right, top to bottom, scenes: 1 courier nesting envelope inside addressed parcel inside shipping crate, encapsulation; 2 cutaway cable sending pulses of light from flashlight transmitter to receiver through fiber, physical layer; 3 large water pipe transferring same bucket volume faster than narrow pipe while pipe lengths identical, bandwidth analogy; 4 office switch appliance connecting three desktop computers with ethernet cables, link layer; 5 small mail carrier replacing local envelope at each relay house while sealed interior parcel remains same, MAC next hop; 6 miniature neighborhood houses grouped into colored districts with gate router, IP subnet; 7 miniature road network at crossroad with delivery van choosing more specific local street toward target house, routing; 8 two people at opposite desks exchanging three sequential envelopes with differently colored trails, handshake; 9 lost numbered-by-color parcel being resent to reorder a row while other fresh parcels arrive, reliable transport; 10 receptionist looking up contact in address book then caller reaching computer with webpage on screen, DNS HTTP; 11 certificate seal checked by browser user before locking shared message box with key, TLS; 12 firewall brick wall allowing approved parcels through small opening with monitoring operator, defense.
```

### english

项目路径：`frontend/public/images/knowledge/english.png`

```text
Use case: scientific-educational. Create ONE teaching illustration sprite atlas with exactly 12 scenes in a precise 3 columns by 4 rows equal grid, whole image aspect 9:8 (each scene 3:2). No gutters or borders. 10% inner safe margins inside each equal tile. Pale ivory background. Charming detailed 3D clay/isometric editorial illustration, teal indigo coral amber. Concrete recognizable objects, people doing meaningful actions, rich texture, expressive and clear. Absolutely NO text, words, letters, numerals, captions, logos or watermark. No text cards or abstract rectangle diagrams. Each tile self-contained. Read left to right, top to bottom, scenes: 1 university student asking another student directions toward campus library, polite campus conversation; 2 student planning weekly routine using illustrated calendar with book sport and meal icons, campus vocabulary; 3 students comparing helpful tablet feedback with distracting phone notifications, technology pros cons; 4 presenter balancing an opinion bubble with book and observed evidence on scales, argument; 5 reader first surveying headings and paragraph openings through broad spotlight on open illustrated journal without letters, skimming; 6 reader using magnifier to find single highlighted detail inside journal, scanning; 7 two adjacent pages with same portrait token linked by thread, pronoun reference; 8 presenter guiding audience through three visual milestones on miniature stage, presentation structure; 9 student explaining an actual rising line chart to attentive audience, chart explanation; 10 writer assembles four interlocking pieces with opinion lightbulb book evidence reasoning gears conclusion knot icons into one paragraph-shaped strip, PEEL; 11 researcher compares two books with different viewpoints and flags uncertainty with partially filled confidence gauge, critical sources; 12 student desk with bounded research question magnifier two source books draft and rehearsed small presentation, final project.
```

### ai

项目路径：`frontend/public/images/knowledge/ai.png`

```text
Use case: scientific-educational. Create ONE teaching illustration sprite atlas with exactly 12 scenes in a precise 3 columns by 4 rows equal grid, whole image aspect 9:8 (each scene 3:2). No gutters or borders. 10% inner safe margins inside each equal tile. Pale ivory background. Charming detailed 3D clay/isometric editorial illustration, teal indigo coral amber. Concrete recognizable objects, people doing meaningful actions, rich texture, expressive and clear. Absolutely NO text, words, letters, numerals, captions, logos or watermark. No text cards or abstract rectangle diagrams. Each tile self-contained. Read left to right, top to bottom, scenes: 1 old rule-driven mechanical sorter next to modern learning robot observing examples, AI rules versus learning; 2 toy robot exploring grid maze with obstacles and exit, search; 3 explorer using compass and map choosing shorter route through maze, heuristic; 4 miniature student figure connected to course book and subject bookshelf with colored threads, knowledge graph; 5 detective uses known facts shown as object evidence to infer missing piece, rule reasoning; 6 teacher shows robot baskets of labeled-by-color apples oranges then robot sorts new fruit, supervised learning; 7 collection of objects physically separated into large training tray medium validation tray small sealed test tray, dataset split; 8 two fitted flexible curves one smoothly matching pegs one overly wriggly matching every peg, overfitting; 9 interconnected three layers of bead neurons transmitting glowing signal from pixel mosaic to digit-shaped tile, neural network; 10 engineer adjusting weight knobs while error gauge decreases, training loss; 11 robot separates sentence represented by distinct word-shaped blank wooden pieces then places into coordinate landscape, token embeddings; 12 human reviewers comparing model mistakes for diverse groups with privacy lock and emergency stop, responsible AI.
```

### python

项目路径：`frontend/public/images/knowledge/python.png`

```text
Use case: scientific-educational. Create ONE teaching illustration sprite atlas with exactly 12 scenes in a precise 3 columns by 4 rows equal grid, whole image aspect 9:8 (each scene 3:2). No gutters or borders. 10% inner safe margins inside each equal tile. Pale ivory background. Charming detailed 3D clay/isometric editorial illustration, teal indigo coral amber. Concrete recognizable objects, people doing meaningful actions, rich texture, expressive and clear. Absolutely NO text, words, letters, numerals, captions, logos or watermark. No text cards or abstract rectangle diagrams. Each tile self-contained. Read left to right, top to bottom, scenes: 1 two independent toolbox workspaces with separate python-shaped mascots and package boxes, virtual environments; 2 programmer sorting text number list and key-value objects into appropriate trays, data types; 3 conveyor sends colored balls past fork gate according to color, loop and condition; 4 juicer receives fruit and returns glass of juice with reusable input spout, function input return; 5 modular workshop connected tools separated into organized boxes, modules; 6 hand multiplies every colored cube in rectangular numeric array together using one lever, vectorization; 7 rectangular two-dimensional bead tray shown next to sums collected down columns and across rows, numpy axes; 8 office analyst opening CSV-like paper roll into organized grid spreadsheet with row portraits, DataFrame; 9 analyst removes duplicates and marks missing objects in messy tray before restoring tidy arrangement, data cleaning; 10 fruit sales sorted by fruit type and weighed in grouped baskets, groupby; 11 analyst choosing between real bar chart line chart and histogram miniatures, visualization; 12 raw data crate through washing sorting measuring stations ending in illustrated report on desk, reproducible project.
```

### git

项目路径：`frontend/public/images/knowledge/git.png`

```text
Use case: scientific-educational. Create ONE teaching illustration sprite atlas with exactly 12 scenes in a precise 3 columns by 4 rows equal grid, whole image aspect 9:8 (each scene 3:2). No gutters or borders. 10% inner safe margins inside each equal tile. Pale ivory background. Charming detailed 3D clay/isometric editorial illustration, teal indigo coral amber. Concrete recognizable objects, people doing meaningful actions, rich texture, expressive and clear. Absolutely NO text, words, letters, numerals, captions, logos or watermark. No text cards or abstract rectangle diagrams. Each tile self-contained. Read left to right, top to bottom, scenes: 1 user explains desired app action to designer drawing small interface while tester checks resulting behavior, requirements; 2 engineer uses ruler and stopwatch to measure application's quality goals, acceptance criteria; 3 artist working on canvas then selects pieces into staging tray then seals snapshot in photo album, working staging commit; 4 three successive photo snapshots connected by thread with magnifier comparing exact changes, diff history; 5 miniature rail track splits into main track and experimental branch and rejoins, branches merge; 6 train carriage moved to new base rail while old transparent track remains, rebase analogy; 7 two editors each change same illustration on separate papers then collaborate to combine final version, conflict; 8 team reviews code-like blank pattern on screen with magnifier and test result lamp, review; 9 small toy component tested individually then assembled machine tested together, test pyramid; 10 conveyor automatically assembles and tests each parcel showing green or red lamps stopping faulty parcel, CI; 11 software package museum with successive small medium large upgrade boxes and fixed tag seals, versions release; 12 operator switches server traffic from failed new crate back to tested spare crate with backup database cylinder, rollback.
```

