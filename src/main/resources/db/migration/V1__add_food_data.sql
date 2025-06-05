-- 创建foods表
CREATE TABLE IF NOT EXISTS foods (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    calories DOUBLE PRECISION NOT NULL,
    protein DOUBLE PRECISION NOT NULL,
    fat DOUBLE PRECISION NOT NULL,
    carbs DOUBLE PRECISION NOT NULL,
    description TEXT,
    image_url VARCHAR(255)
);

-- 插入一些示例食物数据
INSERT INTO foods (name, category, calories, protein, fat, carbs, description) VALUES
-- 水果类
('苹果', 'fruit', 52, 0.3, 0.2, 13.8, '富含维生素C和膳食纤维'),
('香蕉', 'fruit', 89, 1.1, 0.3, 22.8, '富含钾和维生素B6'),
('橙子', 'fruit', 47, 0.9, 0.1, 11.8, '富含维生素C和膳食纤维'),

-- 蔬菜类
('西兰花', 'vegetable', 34, 2.8, 0.4, 6.6, '富含维生素C和膳食纤维'),
('胡萝卜', 'vegetable', 41, 0.9, 0.2, 9.6, '富含维生素A和膳食纤维'),
('菠菜', 'vegetable', 23, 2.9, 0.4, 3.6, '富含铁和维生素K'),

-- 肉类
('鸡胸肉', 'meat', 165, 31, 3.6, 0, '优质蛋白质来源'),
('三文鱼', 'meat', 208, 22, 13, 0, '富含omega-3脂肪酸'),
('瘦牛肉', 'meat', 250, 26, 17, 0, '富含铁和蛋白质'),

-- 主食
('米饭', 'staple', 130, 2.6, 0.3, 28.2, '碳水化合物主要来源'),
('全麦面包', 'staple', 247, 9.4, 3.2, 41.0, '富含膳食纤维'),
('燕麦片', 'staple', 389, 16.9, 6.9, 66.3, '富含膳食纤维和蛋白质'),

-- 饮品
('牛奶', 'beverage', 66, 3.3, 3.6, 4.8, '富含钙和蛋白质'),
('豆浆', 'beverage', 31, 2.9, 1.5, 2.8, '植物蛋白来源'),
('绿茶', 'beverage', 0, 0, 0, 0, '富含抗氧化物质'),

-- 坚果
('杏仁', 'nut', 579, 21.2, 49.9, 21.7, '富含健康脂肪和蛋白质'),
('核桃', 'nut', 654, 15.2, 65.2, 13.7, '富含omega-3脂肪酸'),
('花生', 'nut', 567, 25.8, 49.2, 16.1, '富含蛋白质和健康脂肪'); 