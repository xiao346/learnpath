import assert from 'node:assert/strict'
import { DatabaseSync } from 'node:sqlite'
import { databaseTutorials } from '../src/content/databaseTutorials.ts'
import { learningSchema, students, enrollments, selectionRows, joinedRows, transferStages, databaseFigureNames } from '../src/content/databaseExamples.ts'

// Execute portable teaching SQL only in an ephemeral in-memory database.
// This does not certify PostgreSQL planner choices or concurrent isolation behavior.
const db = new DatabaseSync(':memory:')
db.exec('PRAGMA foreign_keys = ON')
db.exec(learningSchema)
const rows = sql => db.prepare(sql).all().map(row => Object.values(row))
assert.deepEqual(rows('SELECT * FROM students ORDER BY id'), students)
assert.deepEqual(rows('SELECT * FROM enrollments ORDER BY student_id,course_id'), enrollments)
assert.deepEqual(rows('SELECT * FROM enrollments WHERE score>=80 ORDER BY student_id'), selectionRows)
assert.deepEqual(rows('SELECT s.name,e.course_id,e.score FROM students s JOIN enrollments e ON s.id=e.student_id ORDER BY s.id,e.course_id'), joinedRows)
assert.deepEqual(rows('SELECT COUNT(*),COUNT(score),SUM(score) FROM enrollments'), [[4,3,245]])
assert.deepEqual(rows('SELECT course_id,COUNT(*),COUNT(score),AVG(score) FROM enrollments GROUP BY course_id ORDER BY course_id'), [['C1',3,2,87.5],['C2',1,1,70]])
assert.deepEqual(rows("SELECT student_id FROM enrollments WHERE course_id IN ('C1','C2') GROUP BY student_id HAVING COUNT(DISTINCT course_id)=2"), [[101]])
assert.deepEqual(rows('SELECT student_id FROM enrollments WHERE score IS NULL'), [[102]])
assert.deepEqual(rows('SELECT student_id,course_id,score FROM enrollments WHERE score IS NOT NULL ORDER BY score DESC,student_id,course_id LIMIT 2'), [[101,'C1',90],[103,'C1',85]])
assert.throws(() => db.exec("INSERT INTO enrollments VALUES(1099,'C1',80)"), /FOREIGN KEY/)
assert.throws(() => db.exec("INSERT INTO enrollments VALUES(101,'C1',80)"), /UNIQUE/)
assert.throws(() => db.exec("INSERT INTO enrollments VALUES(102,'C2',120)"), /CHECK/)
assert.throws(() => db.exec("INSERT INTO enrollments VALUES(NULL,'C2',80)"), /NOT NULL/)
db.exec("INSERT INTO enrollments VALUES(102,'C2',NULL)")
assert.deepEqual(rows("SELECT score FROM enrollments WHERE student_id=102 AND course_id='C2'"), [[null]])
db.exec("CREATE TABLE accounts(id TEXT PRIMARY KEY,balance INTEGER NOT NULL CHECK(balance>=0)); INSERT INTO accounts VALUES('A',500),('B',300)")
const balance = () => rows('SELECT balance FROM accounts ORDER BY id').flat()
assert.deepEqual(balance(),transferStages[0].balances)
db.exec("BEGIN; UPDATE accounts SET balance=balance-100 WHERE id='A'")
assert.deepEqual(balance(),transferStages[1].balances)
db.exec('ROLLBACK')
assert.deepEqual(balance(),[500,300])
db.exec("BEGIN; UPDATE accounts SET balance=balance-100 WHERE id='A'; UPDATE accounts SET balance=balance+100 WHERE id='B'")
assert.deepEqual(balance(),transferStages[2].balances)
db.exec('COMMIT')
assert.deepEqual(balance(),transferStages[3].balances)
db.close()

assert.equal(Object.keys(databaseTutorials).length,7)
const used = new Set()
for (const [name,tutorial] of Object.entries(databaseTutorials)) {
  assert.ok(tutorial.sections.length>=6,name)
  assert.equal(new Set(tutorial.sections.map(s=>s.id)).size,tutorial.sections.length)
  for (const section of tutorial.sections) {
    assert.ok(section.paragraphs.length>=2,section.id)
    assert.ok(section.paragraphs.every(p=>p.length>40),section.id)
    if (section.table) section.table.rows.forEach(row=>assert.equal(row.length,section.table.headings.length))
    if (section.databaseFigure) { assert.ok(section.databaseFigure in databaseFigureNames); used.add(section.databaseFigure) }
  }
  assert.ok(tutorial.sources.length>0)
  console.log(`${name}: ${tutorial.sections.length} sections, ${tutorial.sections.filter(s=>s.databaseFigure).length} explanatory figures`)
}
assert.deepEqual([...used].sort(),Object.keys(databaseFigureNames).sort())
console.log('PASS: shared SQL dataset, filtering, join cardinality, NULL aggregates, constraints, rollback/commit, 7 tutorial chapters.')
