-- 添加父评论ID列
ALTER TABLE comments ADD COLUMN parent_id BIGINT REFERENCES comments(id);

-- 添加被回复用户ID列
ALTER TABLE comments ADD COLUMN reply_to_user_id BIGINT REFERENCES users(id);

-- 创建索引以提高查询性能
CREATE INDEX idx_comments_parent_id ON comments(parent_id);
CREATE INDEX idx_comments_reply_to_user_id ON comments(reply_to_user_id); 