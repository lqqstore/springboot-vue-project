# 宿舍管理系统 — 核心服务类实现详解

> 生成日期: 2026-06-06  
> 项目: DMS (Dormitory Management System)  
> 技术栈: Spring Boot 3.2 + MyBatis-Plus 3.5 + Sa-Token + MySQL 8.0

---

## 目录

1. [数据模型概览](#1-数据模型概览)
2. [AuthServiceImpl — 认证服务](#2-authserviceimpl--认证服务)
3. [DormBuildingServiceImpl — 楼栋管理服务](#3-dormbuildingserviceimpl--楼栋管理服务)
4. [DormRoomServiceImpl — 房间管理服务（含值日生）](#4-dormroomserviceimpl--房间管理服务含值日生)
5. [NoticeServiceImpl — 公告管理服务](#5-noticeserviceimpl--公告管理服务)
6. [RepairOrderServiceImpl — 报修管理服务](#6-repairorderserviceimpl--报修管理服务)
7. [StudentServiceImpl — 学生管理服务](#7-studentserviceimpl--学生管理服务)
8. [跨模块依赖总览](#8-跨模块依赖总览)
9. [通用设计模式与约定](#9-通用设计模式与约定)

---

## 1. 数据模型概览

系统共 8 张核心表，ER 关系如下：

```
sys_user (系统用户)
    │
    └──[user_id]──── student (学生)
                        │
                        ├──[student_id]── student_dorm (学生-宿舍关联)
                        │                      │
                        │                      └──[room_id]── dorm_room (房间)
                        │                                        │
                        └──[student_id]── room_duty (值日生)     │
                                               │                 │
                                               └──[room_id]──────┘
                                                              │
                                          dorm_building (楼栋) ←──[building_id]

repair_order (报修单) ──[room_id]──→ dorm_room
                    ──[student_id]──→ student

notice (公告) — 独立表，不关联其他实体
```

### 各实体核心字段

| 实体 | 表名 | 核心字段 |
|------|------|---------|
| `SysUser` | `sys_user` | id, username, password(bcrypt), role(ADMIN/dorm_admin/STUDENT), status |
| `Student` | `student` | id, user_id, name, gender, phone, major |
| `StudentDorm` | `student_dorm` | id, student_id, room_id, check_in_date |
| `DormBuilding` | `dorm_building` | id, name, location |
| `DormRoom` | `dorm_room` | id, building_id, room_number, capacity, current_count |
| `RoomDuty` | `room_duty` | id, room_id, student_id, duty_date, created_at |
| `RepairOrder` | `repair_order` | id, student_id, reporter_name, room_id, description, status, handler_name |
| `Notice` | `notice` | id, title, content, publish_time |

---

## 2. AuthServiceImpl — 认证服务

**文件**: `server/src/main/java/.../auth/service/AuthServiceImpl.java`

**依赖的 Mapper**: `SysUserMapper`  
**是否有事务**: 无（仅涉及单表单次操作）  
**SQL 次数**: 1 次查询

### 方法: `login(LoginRequestDTO dto) → LoginResponseDTO`

```
数据流向: LoginRequestDTO → SysUser(查库) → 密码验证 → Sa-Token登录 → LoginResponseDTO
```

**第一步：按用户名查询用户**
```java
LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery()
        .eq(SysUser::getUsername, dto.getUsername())
        .last("limit 1");
SysUser user = sysUserMapper.selectOne(wrapper);
```
使用 `Wrappers.<SysUser>lambdaQuery()` 工厂方法创建查询条件（等效于 `new LambdaQueryWrapper<>()`），拼接 `.last("limit 1")` 在 SQL 末尾追加 `LIMIT 1`，防止用户名重复时查出多条。

**第二步：账号存在性校验**
```java
if (user == null) {
    throw new IllegalArgumentException("账号不存在");
}
```
直接抛 `IllegalArgumentException`，由 `GlobalExceptionHandler` 统一捕获并返回错误响应。

**第三步：账号状态校验**
```java
if (user.getStatus() == null || user.getStatus() == 0) {
    throw new IllegalArgumentException("账号已禁用");
}
```
`status=1` 为启用，`status=0` 或 null 为禁用。

**第四步：密码 bcrypt 验证**
```java
if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
    throw new IllegalArgumentException("密码错误");
}
```
使用 Hutool 的 `BCrypt.checkpw(明文, 密文)` 进行比对。密码在入库时已通过 `BCrypt.hashpw()` 加密，数据库中不存明文。

**第五步：Sa-Token 登录并返回凭证**
```java
StpUtil.login(user.getId());
return new LoginResponseDTO(
        StpUtil.getTokenValue(),  // token 字符串
        user.getId(),             // 用户ID
        user.getRole()            // 角色: ADMIN / dorm_admin / STUDENT
);
```
`StpUtil.login(userId)` 在服务端创建会话，`StpUtil.getTokenValue()` 获取本次登录的 token。前端拿到 token 后存入 `localStorage`，后续请求放 `Authorization` header。

### 配套 Controller: `LoginController`

| 端点 | 方法 | 说明 |
|------|------|------|
| `POST /auth/login` | `login(dto)` | 委托 AuthService，返回 token + 用户信息 |
| `POST /auth/logout` | `logout()` | 直接调用 `StpUtil.logout()` 销毁会话 |
| `GET /auth/current-user` | `getCurrentUser()` | 返回当前登录用户 ID |

---

## 3. DormBuildingServiceImpl — 楼栋管理服务

**文件**: `server/src/main/java/.../dorm/service/impl/DormBuildingServiceImpl.java`

**依赖的 Mapper**: `DormBuildingMapper`（继承自父类）  
**是否有事务**: 无  
**自定义方法**: 无

### 实现方式

```java
@Service
public class DormBuildingServiceImpl
        extends ServiceImpl<DormBuildingMapper, DormBuilding>
        implements DormBuildingService {
    // 空的类体 —— 全部能力来自父类 ServiceImpl
}
```

这是一个**纯空壳实现**——所有 CRUD 完全依赖 MyBatis-Plus 的 `ServiceImpl<M, T>` 父类。父类提供的能力包括：

| 父类方法 | 对应 SQL | 用途 |
|---------|---------|------|
| `getById(id)` | `SELECT * WHERE id=?` | 按主键查 |
| `list(wrapper)` | `SELECT * WHERE ...` | 条件查询 |
| `page(page, wrapper)` | `SELECT * WHERE ... LIMIT ...` | 分页查询 |
| `save(entity)` | `INSERT INTO ...` | 新增 |
| `updateById(entity)` | `UPDATE ... WHERE id=?` | 按主键更新 |
| `removeById(id)` | `DELETE WHERE id=?` | 按主键删除 |

**调用方**：Controller 直接使用继承来的方法，无需写任何实现代码。这是 MyBatis-Plus `ServiceImpl` 的典型用法——对于纯单表 CRUD，不需要写 Service 实现。

---

## 4. DormRoomServiceImpl — 房间管理服务（含值日生）

**文件**: `server/src/main/java/.../dorm/service/impl/DormRoomServiceImpl.java`

**依赖的 Mapper**: `DormBuildingMapper`, `RoomDutyMapper`, `StudentMapper`（外加父类的 `DormRoomMapper`）  
**是否有事务**: 无（纯查询）  
**SQL 次数**: 
- `getRoomPageWithDuty`: 4 次 SQL
- `getAvailableRooms`: 2 次 SQL

### 4.1 方法: `getRoomPageWithDuty(page, buildingId, roomNumber) → Page<DormRoomVO>`

这是房间列表分页查询，同时联查楼栋名称和**今日值日生**信息。

```
数据流向: 5表关联 → DormRoomVO:
  DormRoom (分页)
  └── building_id → DormBuilding → buildingName
  └── room_id → RoomDuty(今天) → student_id → Student → dutyStudentName
```

**第一步：条件分页查房间**
```java
LambdaQueryWrapper<DormRoom> wrapper = new LambdaQueryWrapper<>();
if (buildingId != null) {
    wrapper.eq(DormRoom::getBuildingId, buildingId);
}
if (StrUtil.isNotBlank(roomNumber)) {
    wrapper.like(DormRoom::getRoomNumber, roomNumber.trim());
}
Page<DormRoom> roomPage = this.page(page, wrapper);
```
- `buildingId` 不为空时，精确匹配楼栋
- `roomNumber` 不为空时，模糊查询房间号
- `this.page(page, wrapper)` 是父类 `ServiceImpl` 的分页方法，返回 `Page<DormRoom>`，包含 `records`（当前页数据）、`total`（总记录数）、`current`、`size`

**第二步：批量查楼栋名称**
```java
List<Long> buildingIds = roomPage.getRecords().stream()
        .map(DormRoom::getBuildingId).distinct().toList();
Map<Long, String> buildingNameMap = dormBuildingMapper.selectBatchIds(buildingIds)
        .stream().collect(Collectors.toMap(DormBuilding::getId, DormBuilding::getName));
```
从当前页房间中提取所有不同的 `buildingId`，用 `selectBatchIds` 一次 `WHERE id IN (...)` 批量查出楼栋，转成 `Map<楼栋ID, 楼栋名称>`。

**第三步：查今天的值日生**
```java
LocalDate today = LocalDate.now();
List<Long> roomIds = roomPage.getRecords().stream()
        .map(DormRoom::getId).toList();
LambdaQueryWrapper<RoomDuty> dutyWrapper = new LambdaQueryWrapper<>();
dutyWrapper.in(RoomDuty::getRoomId, roomIds)
        .eq(RoomDuty::getDutyDate, today);
List<RoomDuty> dutyList = roomDutyMapper.selectList(dutyWrapper);
Map<Long, Long> roomDutyStudentMap = dutyList.stream()
        .collect(Collectors.toMap(RoomDuty::getRoomId, RoomDuty::getStudentId));
```
- 使用 `.in(RoomDuty::getRoomId, roomIds)` 批量匹配当前页的所有房间
- 追加 `.eq(RoomDuty::getDutyDate, today)` 只取今天的值日记录
- 转成 `Map<房间ID, 值日学生ID>`

**第四步：批量查学生姓名**
```java
List<Long> studentIds = dutyList.stream()
        .map(RoomDuty::getStudentId).distinct().toList();
Map<Long, String> studentNameMap;
if (!studentIds.isEmpty()) {
    studentNameMap = studentMapper.selectBatchIds(studentIds)
            .stream().collect(Collectors.toMap(Student::getId, Student::getName));
} else {
    studentNameMap = Map.of();
}
```

**第五步：组装 VO**
```java
List<DormRoomVO> voList = roomPage.getRecords().stream().map(room -> {
    DormRoomVO vo = new DormRoomVO();
    BeanUtils.copyProperties(room, vo);  // 拷贝同名字段
    vo.setBuildingName(buildingNameMap.getOrDefault(room.getBuildingId(), "-"));
    Long dutyStudentId = roomDutyStudentMap.get(room.getId());
    vo.setDutyStudentId(dutyStudentId);
    vo.setDutyStudentName(dutyStudentId != null ? studentNameMap.get(dutyStudentId) : null);
    return vo;
}).toList();
```
- `BeanUtils.copyProperties` 一次性拷贝 room → vo 的同名字段（id, buildingId, roomNumber, capacity, currentCount）
- 手动设置联表查出的 `buildingName`、`dutyStudentId`、`dutyStudentName`

**第六步：构造分页 VO**
```java
Page<DormRoomVO> voPage = new Page<>(roomPage.getCurrent(), roomPage.getSize(), roomPage.getTotal());
voPage.setRecords(voList);
return voPage;
```
保留了原始分页的页码、每页大小、总记录数，仅替换 records 为 VO 列表。

### 4.2 方法: `getAvailableRooms() → List<DormRoomVO>`

查询所有**还有空位**的房间。

```java
LambdaQueryWrapper<DormRoom> wrapper = new LambdaQueryWrapper<>();
wrapper.apply("current_count < capacity");
List<DormRoom> rooms = this.list(wrapper);
```
使用 `.apply()` 拼接原始 SQL 片段 `current_count < capacity`——因为 MyBatis-Plus 的 Lambda 表达式不支持 `<` 比较（只有 `eq`, `ne`, `gt`, `ge`, `lt`, `le` 等），这里用 `apply` 直接写条件。

后续步骤与 4.1 类似：批量查楼栋名称 → 组装 VO（不含值日生信息）。

---

## 5. NoticeServiceImpl — 公告管理服务

**文件**: `server/src/main/java/.../notice/service/impl/NoticeServiceImpl.java`

**依赖的 Mapper**: `NoticeMapper`（继承自父类）  
**是否有事务**: 无  
**SQL 次数**: 每个方法 1 次

这是**最简单的业务模块**，公告表不关联其他任何实体，纯单表 CRUD。

### 5.1 `getNoticeList() → List<Notice>`

```java
return noticeMapper.selectList(null);
```
全表查询，传 `null` 即无条件。返回数据库全部公告记录。

### 5.2 `getNoticeById(Long id) → Notice`

```java
return noticeMapper.selectById(id);
```
MyBatis-Plus 主键查询。查不到返回 `null`。

### 5.3 `addNotice(NoticeAddRequestDTO dto) → Notice`

```java
Notice notice = new Notice();
notice.setTitle(dto.getTitle());
notice.setContent(dto.getContent());
notice.setPublishTime(LocalDateTime.now());  // 发布时间 = 当前时间
noticeMapper.insert(notice);
return notice;
```
- DTO → 实体手动映射（只有 title 和 content 两个字段）
- `publishTime` 设为服务端当前时间，不由前端传入（防止时间被篡改）
- `insert` 后主键自动回填到 `notice.id`

### 5.4 `updateNotice(Long id, NoticeUpdateRequestDTO dto) → Notice`

```java
Notice notice = noticeMapper.selectById(id);
if (notice == null) {
    throw new IllegalArgumentException("Notice not found");
}
notice.setTitle(dto.getTitle());
notice.setContent(dto.getContent());
noticeMapper.updateById(notice);
return notice;
```
经典"先查再改"模式：查出原记录 → 校验存在性 → 局部字段覆盖 → 按主键更新。

### 5.5 `deleteNotice(Long id)`

```java
noticeMapper.deleteById(id);
```
单行删除，无级联依赖（公告表独立）。

---

## 6. RepairOrderServiceImpl — 报修管理服务

**文件**: `server/src/main/java/.../repair/service/impl/RepairOrderServiceImpl.java`

**依赖的 Mapper**: `RepairOrderMapper`, `DormRoomMapper`, `DormBuildingMapper`, `StudentMapper`, `StudentDormMapper`  
**是否有事务**: 无  
**SQL 次数**: 
- `getRepairOrderList`: 1 + (3~4 在 convertToVOList 中) = 4~5 次
- `getRepairOrderById`: 同上
- `addRepairOrder`: 3 次

### 6.1 核心私有方法: `convertToVOList(List<RepairOrder>) → List<RepairOrderVO>`

这是一个**可复用的批量转换方法**，`getRepairOrderList` 和 `getRepairOrderById` 都复用它。核心思想与 `StudentServiceImpl.getStudentList()` 一致：先批量查关联表，再在内存中组装。

**第一步：空列表快速返回**
```java
if (orders.isEmpty()) return List.of();
```

**第二步：批量查房间**
```java
List<Long> roomIds = orders.stream()
        .map(RepairOrder::getRoomId).distinct().toList();
Map<Long, DormRoom> roomMap = dormRoomMapper.selectBatchIds(roomIds)
        .stream().collect(Collectors.toMap(DormRoom::getId, r -> r));
```

**第三步：从房间提取楼栋ID，批量查楼栋名称**
```java
List<Long> buildingIds = roomMap.values().stream()
        .map(DormRoom::getBuildingId).distinct().toList();
Map<Long, String> buildingNameMap = buildingIds.isEmpty() ? Map.of()
        : dormBuildingMapper.selectBatchIds(buildingIds)
                .stream().collect(Collectors.toMap(DormBuilding::getId, DormBuilding::getName));
```
这里增加了空判断——如果所有报修单都没关联到房间（roomMap 为空），直接用 `Map.of()` 而不发 SQL。

**第四步：批量查学生姓名**
```java
List<Long> studentIds = orders.stream()
        .map(RepairOrder::getStudentId)
        .filter(id -> id != null).distinct().toList();
Map<Long, String> studentNameMap = studentIds.isEmpty() ? Map.of()
        : studentMapper.selectBatchIds(studentIds)
                .stream().collect(Collectors.toMap(Student::getId, Student::getName));
```
注意 `filter(id -> id != null)`——报修单的 `studentId` 可能为空（非学生报修场景），过滤掉 null 避免 `selectBatchIds` 异常。

**第五步：组装 VO**
```java
return orders.stream().map(order -> {
    RepairOrderVO vo = new RepairOrderVO();
    BeanUtils.copyProperties(order, vo);
    DormRoom room = roomMap.get(order.getRoomId());
    if (room != null) {
        vo.setRoomNumber(room.getRoomNumber());
        vo.setBuildingName(buildingNameMap.getOrDefault(room.getBuildingId(), ""));
    }
    if (order.getStudentId() != null) {
        vo.setStudentName(studentNameMap.get(order.getStudentId()));
    }
    return vo;
}).toList();
```

### 6.2 `getRepairOrderList() → List<RepairOrderVO>`

```java
List<RepairOrder> orders = repairOrderMapper.selectList(null);
return convertToVOList(orders);
```
全量查报修单 → 调用转换方法批量填充关联信息。

### 6.3 `getRepairOrderById(Long id) → RepairOrderVO`

```java
RepairOrder order = repairOrderMapper.selectById(id);
if (order == null) {
    throw new IllegalArgumentException("报修单不存在");
}
List<RepairOrderVO> voList = convertToVOList(List.of(order));
return voList.get(0);
```
查单条 → 包装成单元素 List → 复用 `convertToVOList` → 取第一个元素。设计上复用了批量转换逻辑，代价是发了一组 SQL 只为一条记录。

### 6.4 `addRepairOrder(RepairOrderAddRequestDTO dto) → RepairOrder`

```
数据流向: dto.roomId → 查房间（校验存在）
                     → 查 student_dorm（获取房间内任意学生ID）
                     → 构建 RepairOrder → 插入
```

```java
DormRoom room = dormRoomMapper.selectById(dto.getRoomId());
if (room == null) {
    throw new IllegalArgumentException("房间不存在");
}
```
先校验房间是否存在。

```java
StudentDorm studentDorm = studentDormMapper.selectOne(
        new LambdaQueryWrapper<StudentDorm>()
                .eq(StudentDorm::getRoomId, dto.getRoomId())
                .last("limit 1")
);
Long studentId = studentDorm != null ? studentDorm.getStudentId() : null;
```
查询该房间中任意一个住宿学生——用于关联学生信息。`studentId` 可能为 null（房间暂无学生入住）。

```java
RepairOrder order = new RepairOrder();
order.setStudentId(studentId);
order.setReporterName(dto.getReporterName());
order.setRoomId(dto.getRoomId());
order.setDescription(dto.getDescription());
order.setStatus(0);  // 0 = 待处理
repairOrderMapper.insert(order);
return order;
```
- `status=0` 表示"待处理"（新提交的报修单默认状态）
- `reporterName` 是报修人姓名（字符串），不要求是系统用户

### 6.5 `updateRepairOrder(Long id, RepairOrderUpdateRequestDTO dto) → RepairOrder`

```java
RepairOrder order = repairOrderMapper.selectById(id);
if (order == null) {
    throw new IllegalArgumentException("报修单不存在");
}
// 仅更新非 null 字段（局部更新）
if (dto.getDescription() != null) {
    order.setDescription(dto.getDescription());
}
if (dto.getStatus() != null) {
    order.setStatus(dto.getStatus());
}
if (dto.getHandlerName() != null) {
    order.setHandlerName(dto.getHandlerName());
}
repairOrderMapper.updateById(order);
return order;
```
**局部更新**模式：只有 DTO 中传入的非 null 字段才更新，未传入的字段保持原值。适用于 PATCH 语义的更新场景（比如只改状态不改描述）。

### 6.6 `deleteRepairOrder(Long id)`

```java
repairOrderMapper.deleteById(id);
```
单行删除，无级联清理（报修单独立）。

---

## 7. StudentServiceImpl — 学生管理服务

**文件**: `server/src/main/java/.../student/service/impl/StudentServiceImpl.java`

**依赖的 Mapper**: `StudentMapper`, `StudentDormMapper`, `DormRoomMapper`, `DormBuildingMapper`, `SysUserMapper`  
**事务方法**: `addStudent`, `deleteStudent`, `assignDorm`  
**SQL 次数**: 见各方法明细

### 7.1 `getStudentList() → List<StudentVO>`

**SQL 次数**: 4 次（全表查 student + 全表查 student_dorm + 批量查 dorm_room + 批量查 dorm_building）

```
数据流向:
  student (全表)
    └── [id] → student_dorm (全表) → [room_id] → dorm_room (批量)
                                                      └── [building_id] → dorm_building (批量)
  → 组装 StudentVO，宿舍显示为 "楼栋名 - 房间号"
```

**第一步：全表查学生和宿舍关联**
```java
List<Student> students = studentMapper.selectList(null);
List<StudentDorm> allDorms = studentDormMapper.selectList(null);
Map<Long, Long> studentRoomMap = allDorms.stream()
        .collect(Collectors.toMap(StudentDorm::getStudentId, StudentDorm::getRoomId));
```
一次性查出所有学生和所有宿舍分配记录，在内存中构建 `Map<学生ID, 房间ID>`。

**第二步：批量查房间和楼栋**
```java
List<Long> roomIds = allDorms.stream().map(StudentDorm::getRoomId).distinct().toList();
Map<Long, DormRoom> roomMap = dormRoomMapper.selectBatchIds(roomIds)
        .stream().collect(Collectors.toMap(DormRoom::getId, r -> r));

List<Long> buildingIds = finalRoomMap.values().stream().map(DormRoom::getBuildingId).distinct().toList();
Map<Long, String> buildingNameMap = dormBuildingMapper.selectBatchIds(buildingIds)
        .stream().collect(Collectors.toMap(DormBuilding::getId, DormBuilding::getName));
```

**第三步：组装 VO**
```java
return students.stream().map(student -> {
    StudentVO vo = new StudentVO();
    // ... set 基本字段 ...
    Long roomId = studentRoomMap.get(student.getId());
    if (roomId != null) {
        DormRoom room = finalRoomMap.get(roomId);
        if (room != null) {
            String buildingName = finalBuildingNameMap.getOrDefault(room.getBuildingId(), "");
            vo.setDormRoom(buildingName + " - " + room.getRoomNumber());
        } else {
            vo.setDormRoom("房间ID:" + roomId);  // 防御性编码：房间被删了也能显示ID
        }
    }
    return vo;
}).toList();
```
最终 `dormRoom` 字段格式为 `"2号楼 - 301"`。

### 7.2 `getStudentById(Long id) → Student`

```java
return studentMapper.selectById(id);
```
简单主键查询。

### 7.3 `getStudentByUserId(Long userId) → Student`

```java
LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Student::getUserId, userId);
return studentMapper.selectOne(wrapper);
```
按 `user_id` 反向查找学生。用于从登录态（Sa-Token 中存的是 `sys_user.id`）获取对应的学生档案。

### 7.4 `addStudent(StudentAddRequestDTO dto) → Student`

**事务**: `@Transactional`  
**SQL 次数**: 2 次（INSERT sys_user + INSERT student）

```
数据流向: DTO → 创建 SysUser → 获取 userId → 创建 Student
```

```java
SysUser sysUser = new SysUser();
sysUser.setUsername("stu_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000));
sysUser.setPassword(BCrypt.hashpw("123456"));
sysUser.setRole("STUDENT");
sysUser.setStatus(1);
sysUserMapper.insert(sysUser);  // 插入后 sysUser.id 自动回填

Student student = new Student();
student.setUserId(sysUser.getId());
student.setName(dto.getName());
// ... 拷贝其他字段 ...
studentMapper.insert(student);
```
- **用户名生成**：`stu_` + 毫秒时间戳 + 4位随机数，保证高并发下不冲突
- **初始密码**：固定 `123456`，经 bcrypt 加密后存储
- **先建用户再建学生**：学生必须关联一个系统登录账号，两表通过 `student.user_id = sys_user.id` 关联

### 7.5 `updateStudent(Long id, StudentUpdateRequestDTO dto) → Student`

**SQL 次数**: 2 次（SELECT + UPDATE）

```java
Student student = studentMapper.selectById(id);
if (student == null) {
    throw new IllegalArgumentException("Student not found");
}
student.setName(dto.getName());
student.setGender(dto.getGender());
student.setPhone(dto.getPhone());
student.setMajor(dto.getMajor());
studentMapper.updateById(student);
return student;
```
先查再改的经典模式。注意这里只更新学生的个人档案字段，不涉及 `sys_user` 表。

### 7.6 `deleteStudent(Long id)`

**事务**: `@Transactional`  
**SQL 次数**: 最多 5 次（查 student_dorm + 查 dorm_room + 更新 dorm_room + 删 student_dorm + 查 student + 删 sys_user + 删 student）

**级联清理逻辑**：

```java
// 1. 清理宿舍分配
LambdaQueryWrapper<StudentDorm> dormWrapper = new LambdaQueryWrapper<>();
dormWrapper.eq(StudentDorm::getStudentId, id);
StudentDorm studentDorm = studentDormMapper.selectOne(dormWrapper);
if (studentDorm != null) {
    DormRoom room = dormRoomMapper.selectById(studentDorm.getRoomId());
    if (room != null && room.getCurrentCount() > 0) {
        room.setCurrentCount(room.getCurrentCount() - 1);
        dormRoomMapper.updateById(room);
    }
    studentDormMapper.delete(dormWrapper);
}

// 2. 删除关联的系统用户（防止孤儿数据）
Student student = studentMapper.selectById(id);
if (student != null && student.getUserId() != null) {
    sysUserMapper.deleteById(student.getUserId());
}

// 3. 删除学生
studentMapper.deleteById(id);
```

级联顺序：①释放宿舍资源（房间人数-1） → ②删登录账号 → ③删学生档案。任何一步失败都回滚（`@Transactional`）。

### 7.7 `assignDorm(StudentDormAssignRequestDTO dto) → StudentDorm`

**事务**: `@Transactional`  
**SQL 次数**: 最多 6 次

这是整个系统中**业务逻辑最复杂**的方法，涵盖以下场景：

```
场景A: 学生未分配宿舍 → 直接分配
场景B: 学生已有宿舍，换到新房间 → 旧房间人数-1，新房间人数+1
场景C: 学生已有宿舍，分配到同一房间 → 抛异常
场景D: 新房间已满 → 抛异常
```

**完整流程**：

```java
// 1. 校验学生和房间存在性
Student student = studentMapper.selectById(dto.getStudentId());
if (student == null) throw new IllegalArgumentException("学生不存在");
DormRoom newRoom = dormRoomMapper.selectById(dto.getRoomId());
if (newRoom == null) throw new IllegalArgumentException("房间不存在");

// 2. 处理已有分配（换房逻辑）
LambdaQueryWrapper<StudentDorm> existingWrapper = new LambdaQueryWrapper<>();
existingWrapper.eq(StudentDorm::getStudentId, dto.getStudentId());
StudentDorm existingAssignment = studentDormMapper.selectOne(existingWrapper);

if (existingAssignment != null) {
    // 2a. 检查是否分配到了同一个房间
    if (existingAssignment.getRoomId().equals(dto.getRoomId())) {
        throw new IllegalArgumentException("该学生已在此房间，无需重复分配");
    }
    // 2b. 旧房间人数 -1
    DormRoom oldRoom = dormRoomMapper.selectById(existingAssignment.getRoomId());
    if (oldRoom != null && oldRoom.getCurrentCount() > 0) {
        oldRoom.setCurrentCount(oldRoom.getCurrentCount() - 1);
        dormRoomMapper.updateById(oldRoom);
    }
    // 2c. 删除旧的分配记录
    studentDormMapper.delete(existingWrapper);
}

// 3. 检查新房间容量
long currentCount = studentDormMapper.selectCount(
    new LambdaQueryWrapper<StudentDorm>().eq(StudentDorm::getRoomId, dto.getRoomId())
);
if (currentCount >= newRoom.getCapacity()) {
    throw new IllegalArgumentException("房间已满");
}
```

**潜在并发问题**：第 3 步的"查人数 → 判断容量 → 插入"不是原子操作。在高并发场景下，两个请求可能同时通过容量检查，导致超分配。解决方案可以给 `dorm_room` 表加乐观锁（version 字段）或使用 `UPDATE dorm_room SET current_count = current_count + 1 WHERE id = ? AND current_count < capacity`。

```java
// 4. 创建分配记录 + 更新房间人数
StudentDorm studentDorm = new StudentDorm();
studentDorm.setStudentId(dto.getStudentId());
studentDorm.setRoomId(dto.getRoomId());
studentDorm.setCheckInDate(dto.getCheckInDate());
studentDormMapper.insert(studentDorm);

newRoom.setCurrentCount((int) currentCount + 1);
dormRoomMapper.updateById(newRoom);
return studentDorm;
```

### 7.8 `removeDormAssignment(Long studentId)`

**SQL 次数**: 最多 3 次

```java
LambdaQueryWrapper<StudentDorm> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(StudentDorm::getStudentId, studentId);
StudentDorm studentDorm = studentDormMapper.selectOne(wrapper);
if (studentDorm != null) {
    DormRoom room = dormRoomMapper.selectById(studentDorm.getRoomId());
    if (room != null && room.getCurrentCount() > 0) {
        room.setCurrentCount(room.getCurrentCount() - 1);
        dormRoomMapper.updateById(room);
    }
    studentDormMapper.delete(wrapper);
}
```
逻辑与 `deleteStudent` 中的宿舍清理部分相同——找到分配 → 房间人数-1 → 删除关联。

### 7.9 `getStudentsByRoomId(Long roomId) → List<Student>`

**SQL 次数**: N+1 次（1 次查 student_dorm + N 次逐个查 student）

```java
LambdaQueryWrapper<StudentDorm> dormWrapper = new LambdaQueryWrapper<>();
dormWrapper.eq(StudentDorm::getRoomId, roomId);
List<StudentDorm> studentDorms = studentDormMapper.selectList(dormWrapper);

return studentDorms.stream()
        .map(sd -> studentMapper.selectById(sd.getStudentId()))
        .filter(s -> s != null)
        .toList();
```

**⚠️ 潜在 N+1 问题**：如果房间住了 4 个学生，会执行 1 + 4 = 5 次 SQL。优化方案：收集所有 `studentId` 后用 `selectBatchIds` 批量查询，与 `getStudentList()` 的做法一致：

```java
// 优化后（仅 2 次 SQL）
List<Long> studentIds = studentDorms.stream()
        .map(StudentDorm::getStudentId).toList();
List<Student> students = studentMapper.selectBatchIds(studentIds);
Map<Long, Student> studentMap = students.stream()
        .collect(Collectors.toMap(Student::getId, s -> s));
return studentDorms.stream()
        .map(sd -> studentMap.get(sd.getStudentId()))
        .filter(s -> s != null)
        .toList();
```

### 7.10 `getStudentDormAssignment(Long studentId) → StudentDorm`

```java
LambdaQueryWrapper<StudentDorm> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(StudentDorm::getStudentId, studentId);
return studentDormMapper.selectOne(wrapper);
```
单条件查询，返回该学生的宿舍分配记录（null 表示未分配宿舍）。

---

## 8. 跨模块依赖总览

下图展示了各 Service 实现类之间的 Mapper 依赖关系：

```
                         ┌─────────────────┐
                         │   AuthServiceImpl│
                         └────────┬────────┘
                                  │
                         SysUserMapper
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
    ┌─────────┴─────────┐  ┌─────┴──────┐  ┌─────────┴─────────┐
    │ StudentServiceImpl │  │            │  │RepairOrderServiceImpl│
    └─────────┬─────────┘  │            │  └─────────┬─────────┘
              │             │            │            │
    ┌─────────┼─────────┐   │            │   ┌────────┼────────┐
    │         │         │   │            │   │        │        │
StudentMapper│  StudentDormMapper        │   │RepairOrderMapper│
             │         │                 │   │        │        │
    ┌────────┴────┐    │                 │   │  ┌─────┴─────┐  │
    │             │    │                 │   │  │           │  │
DormRoomMapper   │    │                 │   │StudentMapper│  │
    │            │    │                 │   │           │  │
    └─────┬──────┘    │                 │   └─────┬─────┘  │
          │           │                 │         │        │
DormBuildingMapper    │                 │StudentDormMapper │
          │           │                 │         │        │
          └───────────┴─────────────────┴─────────┴────────┘
                              │
                   ┌──────────┴──────────┐
                   │                     │
          DormRoomServiceImpl    DormBuildingServiceImpl
                   │                     │
        ┌──────────┼──────────┐         (空实现，全继承)
        │          │          │
  DormRoomMapper │   RoomDutyMapper
                 │
        StudentMapper
                 │
        DormBuildingMapper


  NoticeServiceImpl — 独立模块，仅依赖 NoticeMapper
```

---

## 9. 通用设计模式与约定

### 9.1 继承体系

所有 Service 实现类统一遵循：

```
ServiceImpl<Mapper, Entity>  ←  继承  ←  XxxServiceImpl  →  实现  →  XxxService
       (MyBatis-Plus)                            (自定义接口)
```

- `ServiceImpl` 提供 `save`, `removeById`, `updateById`, `getById`, `list`, `page` 等开箱即用的方法
- 模块接口（如 `StudentService`）定义该模块特有的业务方法

### 9.2 批量查询模式（避免 N+1）

项目中多个方法采用了统一的批量查询模式：

```
1. 收集所有需要查询的 ID 列表（distinct）
2. selectBatchIds 一次性批量查询
3. 转成 Map<ID, Entity> 供后续查找
```

遵循此模式的方法：
- `StudentServiceImpl.getStudentList()` — ✅ 4 次 SQL
- `DormRoomServiceImpl.getRoomPageWithDuty()` — ✅ 4 次 SQL
- `RepairOrderServiceImpl.convertToVOList()` — ✅ 4 次 SQL

未遵循此模式的方法：
- `StudentServiceImpl.getStudentsByRoomId()` — ⚠️ N+1 次 SQL（已在 7.9 节标注）

### 9.3 VO 组装模式

联表查询统一采用"先批量查 → 内存组装"的方式，而非 JOIN SQL。好处是：
- 各表查询可被 MyBatis-Plus 的二级缓存命中
- 避免 JOIN 导致的笛卡尔积放大
- 代码逻辑清晰，易于维护

代价是多发了几次 SQL——但在宿舍管理这种数据量不大的场景下，影响可忽略。

### 9.4 事务管理

| 方法 | 事务原因 |
|------|---------|
| `StudentServiceImpl.addStudent()` | 同时写 sys_user + student，任一失败需回滚 |
| `StudentServiceImpl.deleteStudent()` | 涉及 3 张表的级联删除和更新 |
| `StudentServiceImpl.assignDorm()` | 涉及 student_dorm 的增删 + dorm_room 的人数更新 |

判断标准：**是否修改了多张表**。单表操作不加 `@Transactional`（MyBatis-Plus 的 `insert`/`updateById`/`deleteById` 本身是原子操作）。

### 9.5 空值防御

- `StudentServiceImpl.getStudentList()`：房间被删除时显示 `"房间ID:123"` 而非 NPE
- `RepairOrderServiceImpl.convertToVOList()`：报修单无关联学生时 `studentName` 为 null 而不报错
- `DormRoomServiceImpl.getRoomPageWithDuty()`：值日生 Map 为空时用 `Map.of()` 而非 NPE
- `DormRoomServiceImpl.getAvailableRooms()`：buildingName 不存在时用 `getOrDefault(id, "-")`

### 9.6 Lambda 查询包装器

全项目大量使用 `LambdaQueryWrapper` 构造类型安全的查询条件：

```java
// ✅ 类型安全 — 编译期检查字段名
wrapper.eq(Student::getUserId, userId);
wrapper.like(DormRoom::getRoomNumber, "301");
wrapper.in(RoomDuty::getRoomId, roomIds);
wrapper.eq(RoomDuty::getDutyDate, today);

// ✅ 原始 SQL 片段（Lambda 无法表达的比较时使用）
wrapper.apply("current_count < capacity");
wrapper.last("limit 1");
```

`LambdaQueryWrapper` 的核心优势：字段名是 lambda 表达式 `Entity::getField`，数据库列名变化只需改实体类的 `@TableField`，不会在运行时才发现字段不匹配。

### 9.7 密码处理

统一使用 Hutool 的 BCrypt：

```java
// 加密（addStudent 中）
BCrypt.hashpw("123456")

// 验证（login 中）
BCrypt.checkpw(明文, 密文)
```

### 9.8 异常处理

业务校验失败统一抛 `IllegalArgumentException`，由 `common/GlobalExceptionHandler` 统一捕获并转成 `Result<T>` 错误响应返回前端。

---

> **文档维护**: 本文件反映 2026-06-06 时的代码状态，后续如有新增模块或重构，请同步更新。
