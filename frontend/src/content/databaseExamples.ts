export const students = [[101, '小林', '一班'], [102, '小周', '一班'], [103, '小陈', '二班']] as const
export const enrollments = [[101, 'C1', 90], [101, 'C2', 70], [102, 'C1', null], [103, 'C1', 85]] as const
export const learningSchema = `
CREATE TABLE students (
  id INTEGER PRIMARY KEY NOT NULL,
  name VARCHAR(20) NOT NULL,
  class_name VARCHAR(20) NOT NULL
);
CREATE TABLE courses (id VARCHAR(10) PRIMARY KEY NOT NULL, title VARCHAR(40) NOT NULL);
CREATE TABLE enrollments (
  student_id INTEGER NOT NULL REFERENCES students(id),
  course_id VARCHAR(10) NOT NULL REFERENCES courses(id),
  score INTEGER CHECK (score BETWEEN 0 AND 100),
  PRIMARY KEY (student_id, course_id)
);
INSERT INTO students VALUES (101,'小林','一班'),(102,'小周','一班'),(103,'小陈','二班');
INSERT INTO courses VALUES ('C1','数据库'),('C2','网络');
INSERT INTO enrollments VALUES (101,'C1',90),(101,'C2',70),(102,'C1',NULL),(103,'C1',85);
`
export const selectionRows = enrollments.filter(row => row[2] !== null && row[2] >= 80)
export const joinedRows = enrollments.map(row => [students.find(s => s[0] === row[0])![1], row[1], row[2]] as const)
export const transferStages = [
  { balances:[500,300], title:'开始前：两账户合计800', detail:'转账100的目标是把A的100移动到B，不是改变账户总额。此处仅模拟同一数据库内的两次更新。' },
  { balances:[400,300], title:'事务内部：已经扣款，尚未加款', detail:'700只是事务内部的中间合计，不能把这一步当作转账成功。在本例隔离假设下，其他事务不会读到这份未提交的半成品。' },
  { balances:[400,400], title:'事务内部：两次更新都完成', detail:'合计回到800，但还没有提交。仍可以选择回滚；SQL没有报错也不代表业务条件已经全部检查完毕。' },
  { balances:[400,400], title:'提交成功：本次转账作为整体生效', detail:'提交的是两次更新组成的整体。持久性依赖数据库的正确配置和存储保证，不能替代独立备份与灾难恢复。' },
] as const
export const databaseFigureNames = {
  schema:'一次查询怎样经过DBMS到达存储', selection:'先筛行，再保留需要的列', join:'按学号把两张表中的相关行接起来', foreign:'外键检查的是被引用的那一行是否存在', normalize:'拆开重复事实，再用编号连接', transaction:'转账的中间状态与提交、回滚', index:'B+树用分隔键缩小范围，再沿叶子做范围读取',
} as const
