-- 添加category和level字段到courses表
ALTER TABLE courses
ADD COLUMN IF NOT EXISTS category VARCHAR(20),
ADD COLUMN IF NOT EXISTS level VARCHAR(20); 