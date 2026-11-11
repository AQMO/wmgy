-- 万亩果园多商户平台 数据库初始化
-- 表结构由 Hibernate (ddl-auto: update) 启动时自动创建，本脚本仅创建库与可选的种子说明。
CREATE DATABASE IF NOT EXISTS orchard_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 默认种子账号（由后端 DataInitializer 自动创建）：
--   平台管理员:  admin      / admin123
--   消费者:      guohr      / user123
--   演示商家:    demo       / merchant123（万亩一号果园，含 4 个演示商品）
