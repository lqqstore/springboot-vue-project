-- MySQL 8.0 + UTF8MB4
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS dms
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE dms;

-- =========================
-- 1) sys_user
-- =========================
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(255) NOT NULL COMMENT '密码（建议 bcrypt）',
  role VARCHAR(50) NOT NULL COMMENT '角色（如 ADMIN/TEACHER/STUDENT）',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用/0禁用）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 2) dorm_building
-- =========================
CREATE TABLE IF NOT EXISTS dorm_building (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '楼栋名称',
  location VARCHAR(255) DEFAULT NULL COMMENT '位置',
  PRIMARY KEY (id),
  KEY idx_dorm_building_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 3) dorm_room
-- =========================
CREATE TABLE IF NOT EXISTS dorm_room (
  id BIGINT NOT NULL AUTO_INCREMENT,
  building_id BIGINT NOT NULL COMMENT '楼栋ID',
  room_number VARCHAR(50) NOT NULL COMMENT '房间号',
  capacity INT NOT NULL COMMENT '容量',
  current_count INT NOT NULL DEFAULT 0 COMMENT '当前入住数',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dorm_room_building_room (building_id, room_number),
  KEY idx_dorm_room_building_id (building_id),
  CONSTRAINT fk_dorm_room_building
    FOREIGN KEY (building_id) REFERENCES dorm_building (id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 4) student
-- =========================
CREATE TABLE IF NOT EXISTS student (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID（关联 sys_user）',
  name VARCHAR(100) NOT NULL COMMENT '姓名',
  gender VARCHAR(10) DEFAULT NULL COMMENT '性别（M/F/其他）',
  phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  major VARCHAR(100) DEFAULT NULL COMMENT '专业',
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_user_id (user_id),
  KEY idx_student_name (name),
  CONSTRAINT fk_student_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 5) student_dorm
-- =========================
CREATE TABLE IF NOT EXISTS student_dorm (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NOT NULL COMMENT '学生ID',
  room_id BIGINT NOT NULL COMMENT '房间ID',
  check_in_date DATE DEFAULT NULL COMMENT '入住日期',
  PRIMARY KEY (id),
  KEY idx_student_dorm_student_id (student_id),
  KEY idx_student_dorm_room_id (room_id),
  CONSTRAINT fk_student_dorm_student
    FOREIGN KEY (student_id) REFERENCES student (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_student_dorm_room
    FOREIGN KEY (room_id) REFERENCES dorm_room (id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 6) repair_order
-- =========================
CREATE TABLE IF NOT EXISTS repair_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT DEFAULT NULL COMMENT '学生ID（可为空，管理员代报修时无学生）',
  reporter_name VARCHAR(100) DEFAULT NULL COMMENT '报修人姓名',
  room_id BIGINT NOT NULL COMMENT '房间ID',
  description TEXT NOT NULL COMMENT '报修描述',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0待处理/1处理中/2已完成）',
  handler_name VARCHAR(100) DEFAULT NULL COMMENT '处理人姓名（修理工）',
  PRIMARY KEY (id),
  KEY idx_repair_order_student_id (student_id),
  KEY idx_repair_order_room_id (room_id),
  KEY idx_repair_order_status (status),
  CONSTRAINT fk_repair_order_room
    FOREIGN KEY (room_id) REFERENCES dorm_room (id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 7) notice
-- =========================
CREATE TABLE IF NOT EXISTS notice (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL COMMENT '公告标题',
  content TEXT NOT NULL COMMENT '公告内容',
  publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
  PRIMARY KEY (id),
  KEY idx_notice_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 初始化管理员账号（默认账号：admin；默认密码：admin123456）
-- password 字段存储 bcrypt 哈希
-- =========================
INSERT INTO sys_user (username, password, role, status)
SELECT
  'admin' AS username,
  '$2b$10$CVm0tVlkp1hZJc7m4W.D8elgYKdkoKehvG/zEY3F9gnuQaegahBB6' AS password,
  'ADMIN' AS role,
  1 AS status
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM sys_user WHERE username = 'admin'
);

-- =========================
-- 测试数据：楼栋 & 房间（可重复执行）
-- =========================
INSERT INTO dorm_building (id, name, location)
SELECT 1, '1号楼', '校区A-东侧'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM dorm_building WHERE id = 1);

INSERT INTO dorm_room (building_id, room_number, capacity, current_count)
SELECT 1, '101', 6, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM dorm_room WHERE building_id = 1 AND room_number = '101');

INSERT INTO dorm_room (building_id, room_number, capacity, current_count)
SELECT 1, '102', 6, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM dorm_room WHERE building_id = 1 AND room_number = '102');

INSERT INTO dorm_room (building_id, room_number, capacity, current_count)
SELECT 1, '201', 6, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM dorm_room WHERE building_id = 1 AND room_number = '201');

INSERT INTO dorm_room (building_id, room_number, capacity, current_count)
SELECT 1, '202', 6, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM dorm_room WHERE building_id = 1 AND room_number = '202');

INSERT INTO dorm_room (building_id, room_number, capacity, current_count)
SELECT 1, '301', 6, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM dorm_room WHERE building_id = 1 AND room_number = '301');

-- =========================
-- 8) room_duty - 值日生表
-- =========================
CREATE TABLE IF NOT EXISTS room_duty (
  id BIGINT NOT NULL AUTO_INCREMENT,
  room_id BIGINT NOT NULL COMMENT '房间ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  duty_date DATE NOT NULL COMMENT '值日日期',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_room_date (room_id, duty_date),
  KEY idx_room_id (room_id),
  KEY idx_duty_date (duty_date),
  CONSTRAINT fk_room_duty_room
    FOREIGN KEY (room_id) REFERENCES dorm_room (id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_room_duty_student
    FOREIGN KEY (student_id) REFERENCES student (id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

