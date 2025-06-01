ALTER TABLE roles DROP CONSTRAINT IF EXISTS uk_roles_name;
ALTER TABLE roles ADD CONSTRAINT uk_roles_name UNIQUE (name);

-- 初始化角色表
INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT ON CONSTRAINT uk_roles_name DO NOTHING;
-- <<<<<<< Friends
-- INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT ON CONSTRAINT uk_roles_name DO NOTHING;

-- --SELECT * FROM roles;

-- CREATE TABLE IF NOT EXISTS user_friends (
--                                             user_id BIGINT NOT NULL,
--                                             friend_id BIGINT NOT NULL,
--                                             PRIMARY KEY (user_id, friend_id),
--                                             FOREIGN KEY (user_id) REFERENCES users(id),
--                                             FOREIGN KEY (friend_id) REFERENCES users(id)
-- );
-- --DELETE FROM user_friends WHERE user_id = 2;
-- --TRUNCATE TABLE user_friends, friend_request;

-- -- 检查主键是否为复合主键
-- --ALTER TABLE user_friends
-- --    DROP CONSTRAINT IF EXISTS user_friends_pkey,
-- --    ADD PRIMARY KEY (user_id, friend_id);

-- -- 确保没有唯一索引干扰
-- DROP INDEX IF EXISTS idx_user_friends_unique;

-- SELECT * FROM user_friends WHERE user_id IN (1, 2) AND friend_id IN (2, 1);

-- --INSERT INTO user_friends (user_id, friend_id) VALUES (1,2);
-- --INSERT INTO user_friends (user_id, friend_id) VALUES (2,1);
-- =======
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT ON CONSTRAINT uk_roles_name DO NOTHING;
-- >>>>>>> zyh-1
