/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : dms

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 07/06/2026 01:51:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dorm_building
-- ----------------------------
DROP TABLE IF EXISTS `dorm_building`;
CREATE TABLE `dorm_building`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '楼栋名称',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '位置',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dorm_building_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dorm_building
-- ----------------------------
INSERT INTO `dorm_building` VALUES (1, '1号楼', '校区A-东侧');
INSERT INTO `dorm_building` VALUES (5, '2号楼', '校区a东侧');

-- ----------------------------
-- Table structure for dorm_room
-- ----------------------------
DROP TABLE IF EXISTS `dorm_room`;
CREATE TABLE `dorm_room`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building_id` bigint NOT NULL COMMENT '楼栋ID',
  `room_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房间号',
  `capacity` int NOT NULL COMMENT '容量',
  `current_count` int NOT NULL DEFAULT 0 COMMENT '当前入住数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dorm_room_building_room`(`building_id` ASC, `room_number` ASC) USING BTREE,
  INDEX `idx_dorm_room_building_id`(`building_id` ASC) USING BTREE,
  CONSTRAINT `fk_dorm_room_building` FOREIGN KEY (`building_id`) REFERENCES `dorm_building` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dorm_room
-- ----------------------------
INSERT INTO `dorm_room` VALUES (4, 1, '202', 6, 2);
INSERT INTO `dorm_room` VALUES (5, 1, '301', 6, 0);
INSERT INTO `dorm_room` VALUES (8, 1, '302', 6, 0);
INSERT INTO `dorm_room` VALUES (9, 1, '101', 6, 0);
INSERT INTO `dorm_room` VALUES (10, 1, '102', 6, 0);
INSERT INTO `dorm_room` VALUES (11, 1, '201', 6, 0);
INSERT INTO `dorm_room` VALUES (12, 1, '103', 6, 0);
INSERT INTO `dorm_room` VALUES (13, 1, '104', 6, 0);
INSERT INTO `dorm_room` VALUES (14, 1, '105', 6, 0);
INSERT INTO `dorm_room` VALUES (15, 1, '106', 6, 0);
INSERT INTO `dorm_room` VALUES (16, 1, '203', 6, 0);
INSERT INTO `dorm_room` VALUES (17, 1, '204', 6, 0);
INSERT INTO `dorm_room` VALUES (18, 1, '205', 6, 0);

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notice_publish_time`(`publish_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (2, '明天放假', 'xxxxxxxxxx', '2026-06-01 12:33:16');
INSERT INTO `notice` VALUES (3, '明天停电', 'xxxxxxxxx', '2026-06-01 12:47:27');

-- ----------------------------
-- Table structure for repair_order
-- ----------------------------
DROP TABLE IF EXISTS `repair_order`;
CREATE TABLE `repair_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NULL DEFAULT NULL COMMENT '学生ID（可为空）',
  `reporter_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报修人姓名',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报修描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0待处理/1处理中/2已完成）',
  `handler_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理人姓名（修理工）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_repair_order_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_repair_order_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_repair_order_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_repair_order_room` FOREIGN KEY (`room_id`) REFERENCES `dorm_room` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repair_order
-- ----------------------------
INSERT INTO `repair_order` VALUES (5, NULL, '张三', 9, 'xxxxxxxxx', 0, NULL);

-- ----------------------------
-- Table structure for room_duty
-- ----------------------------
DROP TABLE IF EXISTS `room_duty`;
CREATE TABLE `room_duty`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `duty_date` date NOT NULL COMMENT '值日日期',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_room_date`(`room_id` ASC, `duty_date` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_duty_date`(`duty_date` ASC) USING BTREE,
  INDEX `fk_room_duty_student`(`student_id` ASC) USING BTREE,
  CONSTRAINT `fk_room_duty_room` FOREIGN KEY (`room_id`) REFERENCES `dorm_room` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_room_duty_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of room_duty
-- ----------------------------

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID（关联 sys_user）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别（M/F/其他）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专业',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_student_name`(`name` ASC) USING BTREE,
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (5, 1, '张三', 'M', '13813813813', '数据统计');
INSERT INTO `student` VALUES (13, 2, '李四', 'M', '19219291919', 'xxxxxxxx');

-- ----------------------------
-- Table structure for student_dorm
-- ----------------------------
DROP TABLE IF EXISTS `student_dorm`;
CREATE TABLE `student_dorm`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `check_in_date` date NULL DEFAULT NULL COMMENT '入住日期',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_dorm_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_student_dorm_room_id`(`room_id` ASC) USING BTREE,
  CONSTRAINT `fk_student_dorm_room` FOREIGN KEY (`room_id`) REFERENCES `dorm_room` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_student_dorm_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student_dorm
-- ----------------------------
INSERT INTO `student_dorm` VALUES (4, 5, 4, '2026-06-01');
INSERT INTO `student_dorm` VALUES (5, 13, 4, '2026-06-01');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（建议 bcrypt）',
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色（如 ADMIN/TEACHER/STUDENT）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1启用/0禁用）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2b$10$CVm0tVlkp1hZJc7m4W.D8elgYKdkoKehvG/zEY3F9gnuQaegahBB6', 'ADMIN', 1);
INSERT INTO `sys_user` VALUES (2, 'stu_1780288142126', '$2a$10$CDh596Y3fUZfMgVU3Z4wBuUeMLcCZd6vVVS96nQmoTLrBYDO10ix6', 'STUDENT', 1);

SET FOREIGN_KEY_CHECKS = 1;
