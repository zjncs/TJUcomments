# 天津大学美食社区平台数据库架构

[TOC]



## 实体关系图概述

本数据库架构基于ER模型设计，主要包含以下核心实体与关系：

1.  **用户实体**：包括普通用户、管理员和系统管理者
2.  **校区与食堂实体**：学校的不同校区及其食堂
3.  **窗口与菜品实体**：食堂内的窗口及其提供的菜品
4.  **博客与内容实体**：用户发布的美食分享内容
5.  **评价与评论实体**：用户对食堂、窗口、菜品的评价及评论
6.  **社交互动实体**：包括点赞、收藏、关注等社交功能
7.  **激励系统实体**：包括积分、勋章、优惠券等激励机制
8.  **系统管理实体**：包括系统配置、日志、公告等系统功能



## 数据库表设计

### 1. 用户与权限模块 (User & Permissions)

管理用户账号、认证、角色及权限。

#### 1.1 `tb_user` - 用户表
**描述**：存储系统所有用户的基本信息。

| 字段名        | 类型           | 说明                                      | 约束说明           |
| ------------- | -------------- | ----------------------------------------- | ------------------ |
| `id`          | `BIGINT`       | 用户ID                                    | 主键, 自增         |
| `username`    | `VARCHAR(50)`  | 登录用户名                                | 唯一, 非空         |
| `password`    | `VARCHAR(100)` | 密码（DES加密存储）                       | 非空               |
| `phone`       | `VARCHAR(20)`  | 手机号                                    | 唯一, 可空         |
| `email`       | `VARCHAR(100)` | 邮箱                                      | 唯一, 可空         |
| `nickname`    | `VARCHAR(50)`  | 昵称                                      | 非空               |
| `icon`        | `VARCHAR(255)` | 头像URL                                   | 可空, 默认系统头像 |
| `gender`      | `TINYINT`      | 性别 (0=女, 1=男, 2=未知)                 | 非空, 默认2        |
| `birthday`    | `DATE`         | 生日                                      | 可空               |
| `bio`         | `VARCHAR(300)` | 个人简介                                  | 可空               |
| `campus`      | `VARCHAR(50)`  | 校区                                      | 可空               |
| `credits`     | `INT`          | 积分                                      | 非空, 默认0        |
| `level`       | `INT`          | 用户等级                                  | 非空, 默认1        |
| `status`      | `TINYINT`      | 状态 (0=正常, 1=禁用)                     | 非空, 默认0        |
| `role`        | `TINYINT`      | 角色 (0=普通用户, 1=管理员, 2=系统管理者) | 非空, 默认0        |
| `create_time` | `DATETIME`     | 创建时间                                  | 非空, 默认当前时间 |
| `update_time` | `DATETIME`     | 更新时间                                  | 非空, 默认当前时间 |

**插入样例**：
```sql
INSERT INTO tb_user (username, password, phone, email, nickname, gender, campus, credits, level, role, create_time)
VALUES ('zhangsan', 'DES_ENCRYPT_PASSWORD', '13800138000', 'zhangsan@tju.edu.cn', '张三', 1, '北洋园校区', 100, 2, 0, NOW());
```

#### 1.2 `tb_verification` - 验证码表

**描述**：存储短信或邮箱验证码。

| 字段名        | 类型           | 说明                                  | 约束说明           |
| ------------- | -------------- | ------------------------------------- | ------------------ |
| `id`          | `BIGINT`       | 验证码ID                              | 主键, 自增         |
| `phone`       | `VARCHAR(20)`  | 手机号                                | 可空               |
| `email`       | `VARCHAR(100)` | 邮箱                                  | 可空               |
| `code`        | `VARCHAR(10)`  | 验证码                                | 非空               |
| `type`        | `TINYINT`      | 验证类型 (0=注册, 1=登录, 2=找回密码) | 非空               |
| `expire_time` | `DATETIME`     | 过期时间                              | 非空               |
| `status`      | `TINYINT`      | 状态 (0=未使用, 1=已使用, 2=已过期)   | 非空, 默认0        |
| `create_time` | `DATETIME`     | 创建时间                              | 非空, 默认当前时间 |

**插入样例**：

```sql
INSERT INTO tb_verification (phone, code, type, expire_time, create_time)
VALUES ('13800138000', '123456', 0, DATE_ADD(NOW(), INTERVAL 5 MINUTE), NOW());
```

#### 1.3 `tb_admin` - 管理员表

**描述**：存储管理员的额外信息。

| 字段名         | 类型          | 说明                  | 约束说明                         |
| -------------- | ------------- | --------------------- | -------------------------------- |
| `id`           | `BIGINT`      | 管理员ID              | 主键, 自增                       |
| `user_id`      | `BIGINT`      | 对应用户ID            | 外键 -> `tb_user.id`, 唯一, 非空 |
| `name`         | `VARCHAR(50)` | 姓名                  | 非空                             |
| `status`       | `TINYINT`     | 状态 (0=正常, 1=禁用) | 非空, 默认0                      |
| `appointed_by` | `BIGINT`      | 任命管理员ID          | 外键 -> `tb_admin.id`, 可空      |
| `create_time`  | `DATETIME`    | 创建时间              | 非空, 默认当前时间               |
| `update_time`  | `DATETIME`    | 更新时间              | 非空, 默认当前时间               |

**插入样例**：



```sql
INSERT INTO tb_admin (user_id, name, appointed_by, create_time)
VALUES (2, '李四', 1, NOW());
```

#### 1.4 `tb_role` - 角色表

**描述**：定义系统中的角色。

| 字段名        | 类型           | 说明                  | 约束说明           |
| ------------- | -------------- | --------------------- | ------------------ |
| `id`          | `BIGINT`       | 角色ID                | 主键, 自增         |
| `name`        | `VARCHAR(50)`  | 角色名称              | 唯一, 非空         |
| `description` | `VARCHAR(255)` | 角色描述              | 可空               |
| `status`      | `TINYINT`      | 状态 (0=正常, 1=禁用) | 非空, 默认0        |
| `create_time` | `DATETIME`     | 创建时间              | 非空, 默认当前时间 |
| `update_time` | `DATETIME`     | 更新时间              | 非空, 默认当前时间 |

**插入样例**：



```sql
INSERT INTO tb_role (name, description, create_time)
VALUES ('内容审核员', '负责审核用户发布的评价和评论', NOW());
```

#### 1.5 `tb_admin_role` - 管理员角色表

**描述**：管理员与角色的多对多关联。

| 字段名                           | 类型       | 说明       | 约束说明              |
| -------------------------------- | ---------- | ---------- | --------------------- |
| `id`                             | `BIGINT`   | ID         | 主键, 自增            |
| `admin_id`                       | `BIGINT`   | 管理员ID   | 外键 -> `tb_admin.id` |
| `role_id`                        | `BIGINT`   | 角色ID     | 外键 -> `tb_role.id`  |
| `create_time`                    | `DATETIME` | 创建时间   | 非空, 默认当前时间    |
| `UNIQUE KEY (admin_id, role_id)` |            | 复合唯一键 | 防止重复分配          |

**插入样例**：

```
INSERT INTO tb_admin_role (admin_id, role_id, create_time)
VALUES (2, 1, NOW());
```

#### 1.6 `tb_permission` - 权限表

**描述**：定义系统中的权限项。

| 字段名        | 类型           | 说明     | 约束说明           |
| ------------- | -------------- | -------- | ------------------ |
| `id`          | `BIGINT`       | 权限ID   | 主键, 自增         |
| `name`        | `VARCHAR(50)`  | 权限名称 | 非空               |
| `permission`  | `VARCHAR(100)` | 权限标识 | 唯一, 非空         |
| `description` | `VARCHAR(255)` | 权限描述 | 可空               |
| `create_time` | `DATETIME`     | 创建时间 | 非空, 默认当前时间 |
| `update_time` | `DATETIME`     | 更新时间 | 非空, 默认当前时间 |

**插入样例**：

```
INSERT INTO tb_permission (name, permission, description, create_time)
VALUES ('评论审核', 'comment:audit', '审核用户发布的评论', NOW());
```

#### 1.7 `tb_role_permission` - 角色权限关联表

**描述**：角色与权限的多对多关联。

| 字段名                                | 类型       | 说明       | 约束说明                   |
| ------------------------------------- | ---------- | ---------- | -------------------------- |
| `id`                                  | `BIGINT`   | ID         | 主键, 自增                 |
| `role_id`                             | `BIGINT`   | 角色ID     | 外键 -> `tb_role.id`       |
| `permission_id`                       | `BIGINT`   | 权限ID     | 外键 -> `tb_permission.id` |
| `create_time`                         | `DATETIME` | 创建时间   | 非空, 默认当前时间         |
| `UNIQUE KEY (role_id, permission_id)` |            | 复合唯一键 | 防止重复分配               |

**插入样例**：



```
INSERT INTO tb_role_permission (role_id, permission_id, create_time)
VALUES (1, 1, NOW());
```

### 2. 基础数据模块 (Core Data)

存储校区、食堂、窗口、菜品及相关类型信息。

#### 2.1 `tb_campus` - 校区表

**描述**：记录学校的不同校区信息。

| 字段名    | 类型           | 说明     | 约束说明    |
| --------- | -------------- | -------- | ----------- |
| `id`      | `BIGINT`       | 校区ID   | 主键, 自增  |
| `name`    | `VARCHAR(50)`  | 校区名称 | 唯一, 非空  |
| `address` | `VARCHAR(255)` | 地址     | 可空        |
| `x`       | `DOUBLE`       | 经度     | 可空        |
| `y`       | `DOUBLE`       | 纬度     | 可空        |
| `sort`    | `INT`          | 排序值   | 非空, 默认0 |

**插入样例**：

```
INSERT INTO tb_campus (name, address, x, y, sort)
VALUES ('北洋园校区', '天津市津南区雅观路135号', 117.36, 39.11, 1);
```

#### 2.2 `tb_canteen_type` - 食堂类型表

**描述**：定义食堂的不同类型分类。

| 字段名 | 类型           | 说明     | 约束说明    |
| ------ | -------------- | -------- | ----------- |
| `id`   | `BIGINT`       | 类型ID   | 主键, 自增  |
| `name` | `VARCHAR(50)`  | 类型名称 | 唯一, 非空  |
| `icon` | `VARCHAR(255)` | 图标URL  | 可空        |
| `sort` | `INT`          | 排序值   | 非空, 默认0 |

**插入样例**：

```
INSERT INTO tb_canteen_type (name, icon, sort)
VALUES ('学生食堂', '/icons/student_canteen.png', 1);
```

#### 2.3 `tb_canteen` - 食堂表

**描述**：存储各校区的食堂信息。

| 字段名              | 类型            | 说明                      | 约束说明                     |
| ------------------- | --------------- | ------------------------- | ---------------------------- |
| `id`                | `BIGINT`        | 食堂ID                    | 主键, 自增                   |
| `name`              | `VARCHAR(100)`  | 食堂名称                  | 非空                         |
| `campus_id`         | `BIGINT`        | 所属校区                  | 外键 -> `tb_campus.id`       |
| `type_id`           | `BIGINT`        | 食堂类型ID                | 外键 -> `tb_canteen_type.id` |
| `images`            | `VARCHAR(1024)` | 食堂图片，多个图片以,分隔 | 可空                         |
| `address`           | `VARCHAR(255)`  | 详细位置                  | 非空                         |
| `x`                 | `DOUBLE`        | 经度                      | 可空                         |
| `y`                 | `DOUBLE`        | 纬度                      | 可空                         |
| `floor`             | `VARCHAR(20)`   | 楼层信息                  | 可空                         |
| `open_hours`        | `VARCHAR(255)`  | 营业时间                  | 可空                         |
| `avg_price`         | `INT`           | 人均价格                  | 可空                         |
| `introduction`      | `VARCHAR(500)`  | 食堂介绍                  | 可空                         |
| `score`             | `DOUBLE`        | 综合评分                  | 非空, 默认0                  |
| `taste_score`       | `DOUBLE`        | 口味评分                  | 非空, 默认0                  |
| `environment_score` | `DOUBLE`        | 环境评分                  | 非空, 默认0                  |
| `service_score`     | `DOUBLE`        | 服务评分                  | 非空, 默认0                  |
| `liked`             | `INT`           | 点赞数                    | 非空, 默认0                  |
| `comments`          | `INT`           | 评论数                    | 非空, 默认0                  |
| `open_status`       | `TINYINT`       | 营业状态 (0=休息, 1=营业) | 非空, 默认1                  |
| `create_time`       | `DATETIME`      | 创建时间                  | 非空, 默认当前时间           |
| `update_time`       | `DATETIME`      | 更新时间                  | 非空, 默认当前时间           |

**插入样例**：

```sql
INSERT INTO tb_canteen (name, campus_id, type_id, address, open_hours, avg_price, introduction, create_time)
VALUES ('第一食堂', 1, 1, '北洋园校区西区', '6:30-22:00', 15, '北洋园校区最大的学生食堂，提供多种特色菜品', NOW());
```

#### 2.4 `tb_stall_type` - 窗口类型表

**描述**：定义食堂窗口的不同类型分类。

| 字段名 | 类型           | 说明     | 约束说明    |
| ------ | -------------- | -------- | ----------- |
| `id`   | `BIGINT`       | 类型ID   | 主键, 自增  |
| `name` | `VARCHAR(50)`  | 类型名称 | 唯一, 非空  |
| `icon` | `VARCHAR(255)` | 图标URL  | 可空        |
| `sort` | `INT`          | 排序值   | 非空, 默认0 |

**插入样例**：

```sql 
INSERT INTO tb_stall_type (name, icon, sort)
VALUES ('中式炒菜', '/icons/chinese_food.png', 1);
```

#### 2.5 `tb_stall` - 窗口表

**描述**：存储食堂中各个窗口的信息。

| 字段名         | 类型            | 说明                      | 约束说明                   |
| -------------- | --------------- | ------------------------- | -------------------------- |
| `id`           | `BIGINT`        | 窗口ID                    | 主键, 自增                 |
| `name`         | `VARCHAR(100)`  | 窗口名称                  | 非空                       |
| `canteen_id`   | `BIGINT`        | 所属食堂ID                | 外键 -> `tb_canteen.id`    |
| `type_id`      | `BIGINT`        | 窗口类型ID                | 外键 -> `tb_stall_type.id` |
| `images`       | `VARCHAR(1024)` | 窗口图片，多个图片以,分隔 | 可空                       |
| `location`     | `VARCHAR(50)`   | 窗口位置编号              | 可空                       |
| `introduction` | `VARCHAR(500)`  | 窗口介绍                  | 可空                       |
| `open_hours`   | `VARCHAR(255)`  | 营业时间                  | 可空                       |
| `score`        | `DOUBLE`        | 综合评分                  | 非空, 默认0                |
| `taste_score`  | `DOUBLE`        | 口味评分                  | 非空, 默认0                |
| `price_score`  | `DOUBLE`        | 性价比评分                | 非空, 默认0                |
| `comments`     | `INT`           | 评论数                    | 非空, 默认0                |
| `open_status`  | `TINYINT`       | 营业状态 (0=休息, 1=营业) | 非空, 默认1                |
| `create_time`  | `DATETIME`      | 创建时间                  | 非空, 默认当前时间         |
| `update_time`  | `DATETIME`      | 更新时间                  | 非空, 默认当前时间         |

**插入样例**：

```sql
INSERT INTO tb_stall (name, canteen_id, type_id, location, introduction, open_hours, create_time)
VALUES ('老王炒菜', 1, 1, 'A12', '提供各种家常炒菜，以鱼香肉丝最为出名', '10:00-20:00', NOW());
```

#### 2.6 `tb_dish` - 菜品表

**描述**：存储窗口提供的菜品信息。

| 字段名        | 类型             | 说明                      | 约束说明              |
| ------------- | ---------------- | ------------------------- | --------------------- |
| `id`          | `BIGINT`         | 菜品ID                    | 主键, 自增            |
| `name`        | `VARCHAR(100)`   | 菜品名称                  | 非空                  |
| `stall_id`    | `BIGINT`         | 所属窗口ID                | 外键 -> `tb_stall.id` |
| `category`    | `VARCHAR(50)`    | 菜品分类                  | 可空                  |
| `price`       | `DECIMAL(10, 2)` | 价格                      | 非空                  |
| `description` | `VARCHAR(500)`   | 菜品描述                  | 可空                  |
| `images`      | `VARCHAR(1024)`  | 菜品图片，多个图片以,分隔 | 可空                  |
| `nutrition`   | `VARCHAR(500)`   | 营养成分(JSON字符串)      | 可空                  |
| `is_special`  | `TINYINT`        | 是否招牌菜 (0=否, 1=是)   | 非空, 默认0           |
| `is_limited`  | `TINYINT`        | 是否限量 (0=否, 1=是)     | 非空, 默认0           |
| `score`       | `DOUBLE`         | 评分                      | 非空, 默认0           |
| `comments`    | `INT`            | 评论提及次数              | 非空, 默认0           |
| `status`      | `TINYINT`        | 状态 (0=下架, 1=上架)     | 非空, 默认1           |
| `create_time` | `DATETIME`       | 创建时间                  | 非空, 默认当前时间    |
| `update_time` | `DATETIME`       | 更新时间                  | 非空, 默认当前时间    |

**插入样例**：

```sql
INSERT INTO tb_dish (name, stall_id, category, price, description, is_special, create_time)
VALUES ('鱼香肉丝', 1, '川菜', 12.00, '经典川菜，酸甜可口，搭配米饭绝佳', 1, NOW());
```

### 3. 内容与互动模块 (Content & Interaction)

管理用户生成的内容（博客、评价、评论）及社交互动（点赞、收藏、关注、消息、搜索）。

#### 3.1 `tb_blog` - 博客/笔记表

**描述**：存储用户发布的美食博客或笔记内容。

| 字段名        | 类型            | 说明                  | 约束说明                      |
| ------------- | --------------- | --------------------- | ----------------------------- |
| `id`          | `BIGINT`        | 博客ID                | 主键, 自增                    |
| `user_id`     | `BIGINT`        | 作者ID                | 外键 -> `tb_user.id`          |
| `title`       | `VARCHAR(200)`  | 标题                  | 可空                          |
| `content`     | `VARCHAR(2000)` | 正文内容              | 非空                          |
| `images`      | `VARCHAR(1024)` | 图片，多个图片以,分隔 | 可空                          |
| `canteen_id`  | `BIGINT`        | 关联食堂ID            | 外键 -> `tb_canteen.id`, 可空 |
| `stall_id`    | `BIGINT`        | 关联窗口ID            | 外键 -> `tb_stall.id`, 可空   |
| `liked`       | `INT`           | 点赞数                | 非空, 默认0                   |
| `comments`    | `INT`           | 评论数                | 非空, 默认0                   |
| `status`      | `TINYINT`       | 状态 (0=正常, 1=隐藏) | 非空, 默认0                   |
| `is_top`      | `TINYINT`       | 是否置顶 (0=否, 1=是) | 非空, 默认0                   |
| `create_time` | `DATETIME`      | 创建时间              | 非空, 默认当前时间            |
| `update_time` | `DATETIME`      | 更新时间              | 非空, 默认当前时间            |

**插入样例**：



```sql
INSERT INTO tb_blog (user_id, title, content, canteen_id, stall_id, create_time)
VALUES (1, '今天发现了一家超好吃的炒菜窗口', '在第一食堂的老王炒菜窗口，点了招牌菜鱼香肉丝，味道非常正宗，下次还会再去！', 1, 1, NOW());
```

#### 3.2 `tb_blog_dish` - 博客菜品关联表

**描述**：博客与菜品的多对多关联。

| 字段名                          | 类型       | 说明                  | 约束说明             |
| ------------------------------- | ---------- | --------------------- | -------------------- |
| `id`                            | `BIGINT`   | 关联ID                | 主键, 自增           |
| `blog_id`                       | `BIGINT`   | 博客ID                | 外键 -> `tb_blog.id` |
| `dish_id`                       | `BIGINT`   | 菜品ID                | 外键 -> `tb_dish.id` |
| `is_recommended`                | `TINYINT`  | 是否推荐 (0=否, 1=是) | 非空, 默认1          |
| `create_time`                   | `DATETIME` | 创建时间              | 非空, 默认当前时间   |
| `UNIQUE KEY (blog_id, dish_id)` |            | 复合唯一键            | 防止重复关联         |

**插入样例**：



```
INSERT INTO tb_blog_dish (blog_id, dish_id, is_recommended, create_time)
VALUES (1, 1, 1, NOW());
```

#### 3.3 `tb_topic` - 话题表

**描述**：存储系统中的话题。

| 字段名        | 类型           | 说明                  | 约束说明           |
| ------------- | -------------- | --------------------- | ------------------ |
| `id`          | `BIGINT`       | 话题ID                | 主键, 自增         |
| `name`        | `VARCHAR(100)` | 话题名称              | 唯一, 非空         |
| `description` | `VARCHAR(500)` | 话题描述              | 可空               |
| `cover`       | `VARCHAR(255)` | 封面图URL             | 可空               |
| `sort`        | `INT`          | 排序值                | 非空, 默认0        |
| `status`      | `TINYINT`      | 状态 (0=正常, 1=隐藏) | 非空, 默认0        |
| `create_time` | `DATETIME`     | 创建时间              | 非空, 默认当前时间 |
| `update_time` | `DATETIME`     | 更新时间              | 非空, 默认当前时间 |

**插入样例**：

```sql
INSERT INTO tb_topic (name, description, cover, sort, create_time)
VALUES ('校园美食', '分享校园内的各种美食体验', '/covers/campus_food.jpg', 1, NOW());
```

#### 3.4 `tb_blog_topic` - 博客话题关联表

**描述**：博客与话题的多对多关联。

| 字段名                           | 类型       | 说明       | 约束说明              |
| -------------------------------- | ---------- | ---------- | --------------------- |
| `id`                             | `BIGINT`   | 关联ID     | 主键, 自增            |
| `blog_id`                        | `BIGINT`   | 博客ID     | 外键 -> `tb_blog.id`  |
| `topic_id`                       | `BIGINT`   | 话题ID     | 外键 -> `tb_topic.id` |
| `create_time`                    | `DATETIME` | 创建时间   | 非空, 默认当前时间    |
| `UNIQUE KEY (blog_id, topic_id)` |            | 复合唯一键 | 防止重复关联          |

**插入样例**：

```sql
INSERT INTO tb_blog_topic (blog_id, topic_id, create_time)
VALUES (1, 1, NOW());
```

#### 3.5 `tb_tag` - 标签表

**描述**：存储系统中的标签。

| 字段名        | 类型          | 说明     | 约束说明           |
| ------------- | ------------- | -------- | ------------------ |
| `id`          | `BIGINT`      | 标签ID   | 主键, 自增         |
| `name`        | `VARCHAR(50)` | 标签名称 | 唯一, 非空         |
| `usage_count` | `INT`         | 使用次数 | 非空, 默认0        |
| `create_time` | `DATETIME`    | 创建时间 | 非空, 默认当前时间 |

**插入样例**：



```
INSERT INTO tb_tag (name, usage_count, create_time)
VALUES ('川菜', 5, NOW());
```

#### 3.6 `tb_blog_tag` - 博客标签关联表

**描述**：博客与标签的多对多关联。

| 字段名                         | 类型       | 说明       | 约束说明             |
| ------------------------------ | ---------- | ---------- | -------------------- |
| `id`                           | `BIGINT`   | 关联ID     | 主键, 自增           |
| `blog_id`                      | `BIGINT`   | 博客ID     | 外键 -> `tb_blog.id` |
| `tag_id`                       | `BIGINT`   | 标签ID     | 外键 -> `tb_tag.id`  |
| `create_time`                  | `DATETIME` | 创建时间   | 非空, 默认当前时间   |
| `UNIQUE KEY (blog_id, tag_id)` |            | 复合唯一键 | 防止重复关联         |

**插入样例**：

```
INSERT INTO tb_blog_tag (blog_id, tag_id, create_time)
VALUES (1, 1, NOW());
```

#### 3.7 `tb_review` - 评价表

**描述**：存储用户对食堂、窗口或菜品的评价内容和评分。

| 字段名              | 类型            | 说明                                | 约束说明                                        |
| ------------------- | --------------- | ----------------------------------- | ----------------------------------------------- |
| `id`                | `BIGINT`        | 评价ID                              | 主键, 自增                                      |
| `user_id`           | `BIGINT`        | 用户ID                              | 外键 -> `tb_user.id`                            |
| `content`           | `VARCHAR(1024)` | 评价内容                            | 非空                                            |
| `images`            | `VARCHAR(1024)` | 图片，多个图片以,分隔               | 可空                                            |
| `canteen_id`        | `BIGINT`        | 评价食堂ID                          | 外键 -> `tb_canteen.id`, 可空                   |
| `stall_id`          | `BIGINT`        | 评价窗口ID                          | 外键 -> `tb_stall.id`, 可空                     |
| `dish_id`           | `BIGINT`        | 评价菜品ID                          | 外键 -> `tb_dish.id`, 可空                      |
| `overall_score`     | `INT`           | 总体评分(1-5)                       | 非空, CHECK (overall_score BETWEEN 1 AND 5)     |
| `taste_score`       | `INT`           | 口味评分(1-5)                       | 可空, CHECK (taste_score BETWEEN 1 AND 5)       |
| `environment_score` | `INT`           | 环境评分(1-5)                       | 可空, CHECK (environment_score BETWEEN 1 AND 5) |
| `service_score`     | `INT`           | 服务评分(1-5)                       | 可空, CHECK (service_score BETWEEN 1 AND 5)     |
| `price_score`       | `INT`           | 性价比评分(1-5)                     | 可空, CHECK (price_score BETWEEN 1 AND 5)       |
| `liked`             | `INT`           | 点赞数                              | 非空, 默认0                                     |
| `status`            | `TINYINT`       | 状态 (0=待审核, 1=已通过, 2=已拒绝) | 非空, 默认0                                     |
| `auditor_id`        | `BIGINT`        | 审核管理员ID                        | 外键 -> `tb_admin.id`, 可空                     |
| `audit_time`        | `DATETIME`      | 审核时间                            | 可空                                            |
| `audit_note`        | `VARCHAR(255)`  | 审核备注                            | 可空                                            |
| `create_time`       | `DATETIME`      | 创建时间                            | 非空, 默认当前时间                              |
| `update_time`       | `DATETIME`      | 更新时间                            | 非空, 默认当前时间                              |

**插入样例**：



```
INSERT INTO tb_review (user_id, content, dish_id, overall_score, taste_score, price_score, create_time)
VALUES (1, '鱼香肉丝味道正宗，分量也足，性价比很高', 1, 5, 5, 4, NOW());
```

#### 3.8 `tb_comment` - 评论表

**描述**：存储用户对博客或评价的评论。

| 字段名        | 类型           | 说明                                | 约束说明                      |
| ------------- | -------------- | ----------------------------------- | ----------------------------- |
| `id`          | `BIGINT`       | 评论ID                              | 主键, 自增                    |
| `user_id`     | `BIGINT`       | 用户ID                              | 外键 -> `tb_user.id`          |
| `blog_id`     | `BIGINT`       | 博客ID                              | 外键 -> `tb_blog.id`, 可空    |
| `review_id`   | `BIGINT`       | 评价ID                              | 外键 -> `tb_review.id`, 可空  |
| `parent_id`   | `BIGINT`       | 父评论ID                            | 外键 -> `tb_comment.id`, 可空 |
| `content`     | `VARCHAR(255)` | 评论内容                            | 非空                          |
| `liked`       | `INT`          | 点赞数                              | 非空, 默认0                   |
| `status`      | `TINYINT`      | 状态 (0=待审核, 1=已通过, 2=已拒绝) | 非空, 默认0                   |
| `auditor_id`  | `BIGINT`       | 审核管理员ID                        | 外键 -> `tb_admin.id`, 可空   |
| `audit_time`  | `DATETIME`     | 审核时间                            | 可空                          |
| `create_time` | `DATETIME`     | 创建时间                            | 非空, 默认当前时间            |

**插入样例**：

```
INSERT INTO tb_comment (user_id, blog_id, content, create_time)
VALUES (2, 1, '看起来真不错，我也要去尝尝这个鱼香肉丝', NOW());
```

#### 3.9 `tb_like` - 点赞表

**描述**：记录用户对博客、评价或评论的点赞。

| 字段名                                 | 类型       | 说明                                  | 约束说明             |
| -------------------------------------- | ---------- | ------------------------------------- | -------------------- |
| `id`                                   | `BIGINT`   | 点赞ID                                | 主键, 自增           |
| `user_id`                              | `BIGINT`   | 用户ID                                | 外键 -> `tb_user.id` |
| `liked_id`                             | `BIGINT`   | 被点赞对象ID                          | 非空                 |
| `type`                                 | `TINYINT`  | 点赞对象类型 (1=博客, 2=评价, 3=评论) | 非空                 |
| `create_time`                          | `DATETIME` | 点赞时间                              | 非空, 默认当前时间   |
| `UNIQUE KEY (user_id, liked_id, type)` |            | 复合唯一键                            | 防止重复点赞         |

**插入样例**：



```
INSERT INTO tb_like (user_id, liked_id, type, create_time)
VALUES (2, 1, 1, NOW());
```

#### 3.10 `tb_favorite` - 收藏表

**描述**：记录用户对博客、评价、食堂、窗口或菜品的收藏。

| 字段名                                    | 类型       | 说明                                                  | 约束说明             |
| ----------------------------------------- | ---------- | ----------------------------------------------------- | -------------------- |
| `id`                                      | `BIGINT`   | 收藏ID                                                | 主键, 自增           |
| `user_id`                                 | `BIGINT`   | 用户ID                                                | 外键 -> `tb_user.id` |
| `favorite_id`                             | `BIGINT`   | 被收藏对象ID                                          | 非空                 |
| `type`                                    | `TINYINT`  | 收藏对象类型 (1=博客, 2=评价, 3=食堂, 4=窗口, 5=菜品) | 非空                 |
| `create_time`                             | `DATETIME` | 收藏时间                                              | 非空, 默认当前时间   |
| `UNIQUE KEY (user_id, favorite_id, type)` |            | 复合唯一键                                            | 防止重复收藏         |

**插入样例**：

```
INSERT INTO tb_favorite (user_id, favorite_id, type, create_time)
VALUES (1, 1, 5, NOW());
```

#### 3.11 `tb_follow` - 关注表

**描述**：记录用户间的关注关系。

| 字段名                                 | 类型       | 说明       | 约束说明             |
| -------------------------------------- | ---------- | ---------- | -------------------- |
| `id`                                   | `BIGINT`   | 关系ID     | 主键, 自增           |
| `user_id`                              | `BIGINT`   | 关注人ID   | 外键 -> `tb_user.id` |
| `follow_user_id`                       | `BIGINT`   | 被关注人ID | 外键 -> `tb_user.id` |
| `create_time`                          | `DATETIME` | 关注时间   | 非空, 默认当前时间   |
| `UNIQUE KEY (user_id, follow_user_id)` |            | 复合唯一键 | 防止重复关注         |

**插入样例**：

```
INSERT INTO tb_follow (user_id, follow_user_id, create_time)
VALUES (2, 1, NOW());
```

#### 3.12 `tb_message` - 消息表

**描述**：存储系统中的消息通知。

| 字段名         | 类型           | 说明                                          | 约束说明                   |
| -------------- | -------------- | --------------------------------------------- | -------------------------- |
| `id`           | `BIGINT`       | 消息ID                                        | 主键, 自增                 |
| `from_user_id` | `BIGINT`       | 发送者ID                                      | 外键 -> `tb_user.id`, 可空 |
| `to_user_id`   | `BIGINT`       | 接收者ID                                      | 外键 -> `tb_user.id`       |
| `type`         | `TINYINT`      | 消息类型 (0=系统消息, 1=点赞, 2=评论, 3=关注) | 非空                       |
| `source_id`    | `BIGINT`       | 来源ID                                        | 可空                       |
| `source_type`  | `TINYINT`      | 来源类型                                      | 可空                       |
| `content`      | `VARCHAR(255)` | 消息内容                                      | 可空                       |
| `read_status`  | `TINYINT`      | 已读状态 (0=未读, 1=已读)                     | 非空, 默认0                |
| `create_time`  | `DATETIME`     | 创建时间                                      | 非空, 默认当前时间         |

**插入样例**：

```
INSERT INTO tb_message (from_user_id, to_user_id, type, source_id, source_type, content, create_time)
VALUES (2, 1, 1, 1, 1, '李四点赞了你的博客"今天发现了一家超好吃的炒菜窗口"', NOW());
```

#### 3.13 `tb_search_history` - 搜索历史表

**描述**：记录用户的搜索关键词。

| 字段名        | 类型           | 说明     | 约束说明             |
| ------------- | -------------- | -------- | -------------------- |
| `id`          | `BIGINT`       | 历史ID   | 主键, 自增           |
| `user_id`     | `BIGINT`       | 用户ID   | 外键 -> `tb_user.id` |
| `keyword`     | `VARCHAR(100)` | 关键词   | 非空                 |
| `create_time` | `DATETIME`     | 搜索时间 | 非空, 默认当前时间   |

**插入样例**：

```
INSERT INTO tb_search_history (user_id, keyword, create_time)
VALUES (1, '鱼香肉丝', NOW());
```

### 4. 用户激励与活动模块 (User Engagement & Activities)

管理积分、签到、勋章、优惠券、等级及活动。

#### 4.1 `tb_credit_record` - 积分记录表

**描述**：记录用户积分的变动情况。

| 字段名        | 类型           | 说明       | 约束说明             |
| ------------- | -------------- | ---------- | -------------------- |
| `id`          | `BIGINT`       | 记录ID     | 主键, 自增           |
| `user_id`     | `BIGINT`       | 用户ID     | 外键 -> `tb_user.id` |
| `type`        | `VARCHAR(50)`  | 积分类型   | 非空                 |
| `credits`     | `INT`          | 积分变动值 | 非空                 |
| `description` | `VARCHAR(255)` | 积分描述   | 可空                 |
| `create_time` | `DATETIME`     | 创建时间   | 非空, 默认当前时间   |

**插入样例**：

```
INSERT INTO tb_credit_record (user_id, type, credits, description, create_time)
VALUES (1, 'PUBLISH_BLOG', 10, '发布博客获得积分奖励', NOW());
```

#### 4.2 `tb_sign` - 签到表

**描述**：记录用户的签到情况。

| 字段名                       | 类型       | 说明       | 约束说明             |
| ---------------------------- | ---------- | ---------- | -------------------- |
| `id`                         | `BIGINT`   | 签到ID     | 主键, 自增           |
| `user_id`                    | `BIGINT`   | 用户ID     | 外键 -> `tb_user.id` |
| `year`                       | `INT`      | 签到年份   | 非空                 |
| `month`                      | `INT`      | 签到月份   | 非空                 |
| `date`                       | `DATE`     | 签到日期   | 非空                 |
| `create_time`                | `DATETIME` | 创建时间   | 非空, 默认当前时间   |
| `UNIQUE KEY (user_id, date)` |            | 复合唯一键 | 防止重复签到         |

**插入样例**：

```
INSERT INTO tb_sign (user_id, year, month, date, create_time)
VALUES (1, 2023, 5, '2023-05-15', NOW());
```

#### 4.3 `tb_medal` - 勋章表

**描述**：定义系统中的勋章。

| 字段名        | 类型           | 说明                              | 约束说明           |
| ------------- | -------------- | --------------------------------- | ------------------ |
| `id`          | `BIGINT`       | 勋章ID                            | 主键, 自增         |
| `name`        | `VARCHAR(50)`  | 勋章名称                          | 唯一, 非空         |
| `description` | `VARCHAR(255)` | 勋章描述                          | 非空               |
| `icon`        | `VARCHAR(255)` | 勋章图标URL                       | 非空               |
| `condition`   | `VARCHAR(255)` | 获取条件                          | 非空               |
| `type`        | `TINYINT`      | 勋章类型 (0=普通, 1=活动, 2=成就) | 非空, 默认0        |
| `credits`     | `INT`          | 获得积分                          | 非空, 默认0        |
| `create_time` | `DATETIME`     | 创建时间                          | 非空, 默认当前时间 |
| `update_time` | `DATETIME`     | 更新时间                          | 非空, 默认当前时间 |

**插入样例**：

```
INSERT INTO tb_medal (name, description, icon, condition, type, credits, create_time)
VALUES ('美食达人', '发布10篇高质量美食博客', '/medals/food_master.png', '发布10篇获得50个以上点赞的博客', 2, 100, NOW());
```

#### 4.4 `tb_user_medal` - 用户勋章表

**描述**：记录用户获得的勋章。

| 字段名                           | 类型       | 说明                  | 约束说明              |
| -------------------------------- | ---------- | --------------------- | --------------------- |
| `id`                             | `BIGINT`   | ID                    | 主键, 自增            |
| `user_id`                        | `BIGINT`   | 用户ID                | 外键 -> `tb_user.id`  |
| `medal_id`                       | `BIGINT`   | 勋章ID                | 外键 -> `tb_medal.id` |
| `obtain_time`                    | `DATETIME` | 获得时间              | 非空, 默认当前时间    |
| `is_display`                     | `TINYINT`  | 是否展示 (0=否, 1=是) | 非空, 默认1           |
| `UNIQUE KEY (user_id, medal_id)` |            | 复合唯一键            | 防止重复获得          |

**插入样例**：

```
INSERT INTO tb_user_medal (user_id, medal_id, obtain_time, is_display)
VALUES (1, 1, NOW(), 1);
```

#### 4.5 `tb_voucher` - 优惠券表

**描述**：定义系统中的优惠券。

| 字段名             | 类型             | 说明                                          | 约束说明                      |
| ------------------ | ---------------- | --------------------------------------------- | ----------------------------- |
| `id`               | `BIGINT`         | 优惠券ID                                      | 主键, 自增                    |
| `title`            | `VARCHAR(100)`   | 优惠券标题                                    | 非空                          |
| `description`      | `VARCHAR(255)`   | 优惠券描述                                    | 可空                          |
| `type`             | `TINYINT`        | 优惠券类型 (0=满减券, 1=折扣券)               | 非空, 默认0                   |
| `price`            | `DECIMAL(10, 2)` | 优惠券金额(满减券)                            | 可空                          |
| `discount`         | `DECIMAL(5, 2)`  | 折扣率(0.1-1折扣券)                           | 可空                          |
| `min_amount`       | `DECIMAL(10, 2)` | 最低消费金额                                  | 非空, 默认0                   |
| `canteen_id`       | `BIGINT`         | 限定食堂ID                                    | 外键 -> `tb_canteen.id`, 可空 |
| `stall_id`         | `BIGINT`         | 限定窗口ID                                    | 外键 -> `tb_stall.id`, 可空   |
| `start_time`       | `DATETIME`       | 生效时间                                      | 非空                          |
| `end_time`         | `DATETIME`       | 过期时间                                      | 非空                          |
| `stock`            | `INT`            | 库存                                          | 非空, 默认0                   |
| `required_credits` | `INT`            | 兑换所需积分                                  | 非空, 默认0                   |
| `status`           | `TINYINT`        | 状态 (0=未开始, 1=进行中, 2=已结束, 3=已下线) | 非空, 默认0                   |
| `create_time`      | `DATETIME`       | 创建时间                                      | 非空, 默认当前时间            |
| `update_time`      | `DATETIME`       | 更新时间                                      | 非空, 默认当前时间            |

**插入样例**：

```
INSERT INTO tb_voucher (title, description, type, price, min_amount, canteen_id, start_time, end_time, stock, required_credits, create_time)
VALUES ('第一食堂5元代金券', '可在第一食堂任意窗口使用', 0, 5.00, 15.00, 1, '2023-05-01 00:00:00', '2023-05-31 23:59:59', 1000, 50, NOW());
```

#### 4.6 `tb_user_voucher` - 用户优惠券表

**描述**：记录用户持有的优惠券。

| 字段名        | 类型       | 说明                                | 约束说明                |
| ------------- | ---------- | ----------------------------------- | ----------------------- |
| `id`          | `BIGINT`   | 用户券ID                            | 主键, 自增              |
| `user_id`     | `BIGINT`   | 用户ID                              | 外键 -> `tb_user.id`    |
| `voucher_id`  | `BIGINT`   | 优惠券ID                            | 外键 -> `tb_voucher.id` |
| `status`      | `TINYINT`  | 状态 (0=未使用, 1=已使用, 2=已过期) | 非空, 默认0             |
| `create_time` | `DATETIME` | 领取时间                            | 非空, 默认当前时间      |
| `use_time`    | `DATETIME` | 使用时间                            | 可空                    |

**插入样例**：

```
INSERT INTO tb_user_voucher (user_id, voucher_id, status, create_time)
VALUES (1, 1, 0, NOW());
```

#### 4.7 `tb_level_rule` - 等级规则表

**描述**：定义用户等级晋升规则和特权。

| 字段名        | 类型           | 说明                 | 约束说明           |
| ------------- | -------------- | -------------------- | ------------------ |
| `id`          | `BIGINT`       | 规则ID               | 主键, 自增         |
| `level`       | `INT`          | 等级                 | 唯一, 非空         |
| `name`        | `VARCHAR(50)`  | 等级名称             | 非空               |
| `min_credits` | `INT`          | 最低积分要求         | 非空               |
| `max_credits` | `INT`          | 最高积分限制         | 非空               |
| `benefits`    | `VARCHAR(500)` | 等级特权(JSON字符串) | 可空               |
| `icon`        | `VARCHAR(255)` | 等级图标URL          | 可空               |
| `create_time` | `DATETIME`     | 创建时间             | 非空, 默认当前时间 |
| `update_time` | `DATETIME`     | 更新时间             | 非空, 默认当前时间 |

**插入样例**：

```
INSERT INTO tb_level_rule (level, name, min_credits, max_credits, benefits, icon, create_time)
VALUES (2, '美食先锋', 100, 499, '{"daily_credits": 5, "comment_weight": 1.2}', '/levels/level2.png', NOW());
```

#### 4.8 `tb_activity` - 活动表

**描述**：存储系统发布的各类活动信息。

| 字段名        | 类型           | 说明                                          | 约束说明              |
| ------------- | -------------- | --------------------------------------------- | --------------------- |
| `id`          | `BIGINT`       | 活动ID                                        | 主键, 自增            |
| `title`       | `VARCHAR(100)` | 活动标题                                      | 非空                  |
| `description` | `VARCHAR(500)` | 活动描述                                      | 非空                  |
| `cover`       | `VARCHAR(255)` | 封面图URL                                     | 可空                  |
| `type`        | `TINYINT`      | 活动类型 (0=普通活动, 1=积分活动, 2=社区活动) | 非空                  |
| `start_time`  | `DATETIME`     | 开始时间                                      | 非空                  |
| `end_time`    | `DATETIME`     | 结束时间                                      | 非空                  |
| `rules`       | `TEXT`         | 活动规则                                      | 非空                  |
| `rewards`     | `VARCHAR(500)` | 活动奖励                                      | 非空                  |
| `status`      | `TINYINT`      | 状态 (0=未开始, 1=进行中, 2=已结束, 3=已取消) | 非空, 默认0           |
| `admin_id`    | `BIGINT`       | 发布管理员ID                                  | 外键 -> `tb_admin.id` |
| `create_time` | `DATETIME`     | 创建时间                                      | 非空, 默认当前时间    |
| `update_time` | `DATETIME`     | 更新时间                                      | 非空, 默认当前时间    |

**插入样例**：

```
INSERT INTO tb_activity (title, description, cover, type, start_time, end_time, rules, rewards, status, admin_id, create_time)
VALUES ('美食点评大赛', '参与食堂美食点评，赢取丰厚奖品', '/activities/food_review_contest.jpg', 1, '2023-06-01 00:00:00', '2023-06-30 23:59:59', '1. 活动期间发布评价获得双倍积分\n2. 评价需包含图片和详细描述', '第一名：200积分，第二名：100积分，第三名：50积分', 0, 1, NOW());
```

#### 4.9 `tb_user_activity` - 用户活动参与表

**描述**：记录用户参与活动的情况。

| 字段名                              | 类型       | 说明                                | 约束说明                 |
| ----------------------------------- | ---------- | ----------------------------------- | ------------------------ |
| `id`                                | `BIGINT`   | 参与ID                              | 主键, 自增               |
| `user_id`                           | `BIGINT`   | 用户ID                              | 外键 -> `tb_user.id`     |
| `activity_id`                       | `BIGINT`   | 活动ID                              | 外键 -> `tb_activity.id` |
| `join_time`                         | `DATETIME` | 参与时间                            | 非空, 默认当前时间       |
| `status`                            | `TINYINT`  | 状态 (0=参与中, 1=已完成, 2=已放弃) | 非空, 默认0              |
| `progress`                          | `INT`      | 完成进度 (百分比)                   | 非空, 默认0              |
| `reward_status`                     | `TINYINT`  | 奖励状态 (0=未发放, 1=已发放)       | 非空, 默认0              |
| `create_time`                       | `DATETIME` | 创建时间                            | 非空, 默认当前时间       |
| `update_time`                       | `DATETIME` | 更新时间                            | 非空, 默认当前时间       |
| `UNIQUE KEY (user_id, activity_id)` |            | 复合唯一键                          | 防止重复参与             |

**插入样例**：

```
INSERT INTO tb_user_activity (user_id, activity_id, status, progress, reward_status, create_time)
VALUES (1, 1, 0, 50, 0, NOW());
```

### 5. 系统管理与支持模块 (System Management & Support)

管理系统配置、日志、公告、版本、反馈、举报及敏感词。

#### 5.1 `tb_config` - 系统配置表

**描述**：存储系统的各项配置参数。

| 字段名        | 类型            | 说明     | 约束说明           |
| ------------- | --------------- | -------- | ------------------ |
| `id`          | `BIGINT`        | 配置ID   | 主键, 自增         |
| `key`         | `VARCHAR(100)`  | 配置键   | 唯一, 非空         |
| `value`       | `VARCHAR(1000)` | 配置值   | 非空               |
| `description` | `VARCHAR(255)`  | 描述     | 可空               |
| `create_time` | `DATETIME`      | 创建时间 | 非空, 默认当前时间 |
| `update_time` | `DATETIME`      | 更新时间 | 非空, 默认当前时间 |

**插入样例**：

```
INSERT INTO tb_config (key, value, description, create_time)
VALUES ('daily_sign_credits', '5', '每日签到获得的积分数', NOW());
```

#### 5.2 `tb_sensitive_word` - 敏感词表

**描述**：存储系统敏感词过滤配置。

| 字段名         | 类型           | 说明                                  | 约束说明           |
| -------------- | -------------- | ------------------------------------- | ------------------ |
| `id`           | `BIGINT`       | 配置ID                                | 主键, 自增         |
| `word`         | `VARCHAR(100)` | 敏感词                                | 唯一, 非空         |
| `type`         | `TINYINT`      | 类型 (0=普通, 1=政治, 2=色情, 3=暴力) | 非空, 默认0        |
| `replace_word` | `VARCHAR(100)` | 替换词                                | 可空               |
| `create_time`  | `DATETIME`     | 创建时间                              | 非空, 默认当前时间 |
| `update_time`  | `DATETIME`     | 更新时间                              | 非空, 默认当前时间 |

**插入样例**：

```
INSERT INTO tb_sensitive_word (word, type, replace_word, create_time)
VALUES ('敏感词示例', 0, '***', NOW());
```

#### 5.3 `tb_log` - 系统日志表

**描述**：记录系统操作日志。

| 字段名           | 类型            | 说明           | 约束说明           |
| ---------------- | --------------- | -------------- | ------------------ |
| `id`             | `BIGINT`        | 日志ID         | 主键, 自增         |
| `user_id`        | `BIGINT`        | 用户ID         | 可空               |
| `admin_id`       | `BIGINT`        | 管理员ID       | 可空               |
| `operation`      | `VARCHAR(100)`  | 操作类型       | 非空               |
| `method`         | `VARCHAR(255)`  | 请求方法       | 可空               |
| `params`         | `VARCHAR(1000)` | 请求参数       | 可空               |
| `ip`             | `VARCHAR(50)`   | IP地址         | 可空               |
| `user_agent`     | `VARCHAR(255)`  | 用户代理       | 可空               |
| `operation_time` | `INT`           | 执行时长(毫秒) | 可空               |
| `create_time`    | `DATETIME`      | 创建时间       | 非空, 默认当前时间 |

**插入样例**：

```
INSERT INTO tb_log (admin_id, operation, method, ip, operation_time, create_time)
VALUES (1, 'AUDIT_REVIEW', 'POST /api/admin/review/audit', '192.168.1.1', 120, NOW());
```

#### 5.4 `tb_notice` - 系统公告表

**描述**：存储系统公告信息。

| 字段名        | 类型           | 说明                                          | 约束说明              |
| ------------- | -------------- | --------------------------------------------- | --------------------- |
| `id`          | `BIGINT`       | 公告ID                                        | 主键, 自增            |
| `admin_id`    | `BIGINT`       | 发布管理员ID                                  | 外键 -> `tb_admin.id` |
| `title`       | `VARCHAR(100)` | 公告标题                                      | 非空                  |
| `content`     | `TEXT`         | 公告内容                                      | 非空                  |
| `type`        | `TINYINT`      | 公告类型 (0=普通公告, 1=系统公告, 2=活动公告) | 非空, 默认0           |
| `status`      | `TINYINT`      | 状态 (0=未发布, 1=已发布, 2=已下线)           | 非空, 默认0           |
| `start_time`  | `DATETIME`     | 生效时间                                      | 可空                  |
| `end_time`    | `DATETIME`     | 结束时间                                      | 可空                  |
| `create_time` | `DATETIME`     | 创建时间                                      | 非空, 默认当前时间    |
| `update_time` | `DATETIME`     | 更新时间                                      | 非空, 默认当前时间    |

**插入样例**：

```
INSERT INTO tb_notice (admin_id, title, content, type, status, start_time, end_time, create_time)
VALUES (1, '系统升级公告', '系统将于本周六凌晨2点进行升级维护，预计2小时内完成', 1, 1, '2023-05-15 00:00:00', '2023-05-20 23:59:59', NOW());
```

#### 5.5 `tb_version` - 版本管理表

**描述**：存储系统版本信息。

| 字段名         | 类型            | 说明                                   | 约束说明           |
| -------------- | --------------- | -------------------------------------- | ------------------ |
| `id`           | `BIGINT`        | 版本ID                                 | 主键, 自增         |
| `version`      | `VARCHAR(50)`   | 版本号                                 | 非空               |
| `platform`     | `TINYINT`       | 平台 (0=全部, 1=Android, 2=iOS, 3=Web) | 非空, 默认0        |
| `description`  | `VARCHAR(1000)` | 版本描述                               | 非空               |
| `download_url` | `VARCHAR(255)`  | 下载链接                               | 可空               |
| `is_force`     | `TINYINT`       | 是否强制更新 (0=否, 1=是)              | 非空, 默认0        |
| `status`       | `TINYINT`       | 状态 (0=未发布, 1=已发布)              | 非空, 默认0        |
| `create_time`  | `DATETIME`      | 创建时间                               | 非空, 默认当前时间 |
| `update_time`  | `DATETIME`      | 更新时间                               | 非空, 默认当前时间 |

**插入样例**：

```
INSERT INTO tb_version (version, platform, description, download_url, is_force, status, create_time)
VALUES ('1.2.0', 1, '新增菜品排行榜功能，优化用户界面体验', '[https://download.tjufood.com/android/1.2.0](https://download.tjufood.com/android/1.2.0)', 0, 1, NOW());
```

#### 5.6 `tb_feedback` - 反馈表

**描述**：存储用户提交的反馈意见。

| 字段名        | 类型           | 说明                                      | 约束说明                    |
| ------------- | -------------- | ----------------------------------------- | --------------------------- |
| `id`          | `BIGINT`       | 反馈ID                                    | 主键, 自增                  |
| `user_id`     | `BIGINT`       | 用户ID                                    | 外键 -> `tb_user.id`        |
| `type`        | `TINYINT`      | 反馈类型 (0=功能建议, 1=内容举报, 2=其他) | 非空                        |
| `content`     | `VARCHAR(500)` | 反馈内容                                  | 非空                        |
| `contact`     | `VARCHAR(100)` | 联系方式                                  | 可空                        |
| `status`      | `TINYINT`      | 状态 (0=未处理, 1=已处理)                 | 非空, 默认0                 |
| `response`    | `VARCHAR(500)` | 回复内容                                  | 可空                        |
| `admin_id`    | `BIGINT`       | 处理管理员ID                              | 外键 -> `tb_admin.id`, 可空 |
| `create_time` | `DATETIME`     | 创建时间                                  | 非空, 默认当前时间          |
| `update_time` | `DATETIME`     | 更新时间                                  | 非空, 默认当前时间          |

**插入样例**：

```
INSERT INTO tb_feedback (user_id, type, content, contact, create_time)
VALUES (1, 0, '希望可以增加菜品月度排行榜功能', 'zhangsan@tju.edu.cn', NOW());
```

#### 5.7 `tb_report` - 举报表

**描述**：存储用户举报内容。

| 字段名            | 类型            | 说明                                      | 约束说明                    |
| ----------------- | --------------- | ----------------------------------------- | --------------------------- |
| `id`              | `BIGINT`        | 举报ID                                    | 主键, 自增                  |
| `user_id`         | `BIGINT`        | 举报用户ID                                | 外键 -> `tb_user.id`        |
| `target_id`       | `BIGINT`        | 举报目标ID                                | 非空                        |
| `type`            | `TINYINT`       | 举报类型 (0=博客, 1=评价, 2=评论, 3=用户) | 非空                        |
| `reason`          | `VARCHAR(255)`  | 举报原因                                  | 非空                        |
| `description`     | `VARCHAR(500)`  | 举报描述                                  | 可空                        |
| `images`          | `VARCHAR(1024)` | 图片，多个图片以,分隔                     | 可空                        |
| `status`          | `TINYINT`       | 状态 (0=未处理, 1=已处理, 2=已驳回)       | 非空, 默认0                 |
| `handle_admin_id` | `BIGINT`        | 处理管理员ID                              | 外键 -> `tb_admin.id`, 可空 |
| `handle_note`     | `VARCHAR(255)`  | 处理备注                                  | 可空                        |
| `create_time`     | `DATETIME`      | 创建时间                                  | 非空, 默认当前时间          |
| `update_time`     | `DATETIME`      | 更新时间                                  | 非空, 默认当前时间          |

**插入样例**：

```
INSERT INTO tb_report (user_id, target_id, type, reason, description, create_time)
VALUES (2, 3, 2, '内容不实', '评论中提到的价格与实际不符', NOW());
```

### 6. 媒体与展示模块 (Media & Display)

管理文件上传、轮播图及系统级图片。

#### 6.1 `tb_file` - 文件表

**描述**：存储系统中上传的文件信息（用户头像、博客图片、评价图片等）。

| 字段名          | 类型           | 说明                    | 约束说明             |
| --------------- | -------------- | ----------------------- | -------------------- |
| `id`            | `BIGINT`       | 文件ID                  | 主键, 自增           |
| `user_id`       | `BIGINT`       | 上传者ID                | 外键 -> `tb_user.id` |
| `name`          | `VARCHAR(255)` | 文件名                  | 非空                 |
| `original_name` | `VARCHAR(255)` | 原始文件名              | 非空                 |
| `url`           | `VARCHAR(255)` | 文件URL                 | 非空                 |
| `type`          | `VARCHAR(50)`  | 文件类型                | 非空                 |
| `size`          | `INT`          | 文件大小(字节)          | 非空                 |
| `width`         | `INT`          | 图片宽度                | 可空                 |
| `height`        | `INT`          | 图片高度                | 可空                 |
| `duration`      | `INT`          | 媒体时长(秒)            | 可空                 |
| `status`        | `TINYINT`      | 状态 (0=正常, 1=已删除) | 非空, 默认0          |
| `create_time`   | `DATETIME`     | 创建时间                | 非空, 默认当前时间   |

**插入样例**：

```
INSERT INTO tb_file (user_id, name, original_name, url, type, size, width, height, create_time)
VALUES (1, 'img_1684123456789.jpg', '美食照片.jpg', '/uploads/images/img_1684123456789.jpg', 'image/jpeg', 1024000, 1920, 1080, NOW());
```

#### 6.2 `tb_banner` - 轮播图表

**描述**：存储首页轮播图信息。

| 字段名        | 类型           | 说明                                            | 约束说明           |
| ------------- | -------------- | ----------------------------------------------- | ------------------ |
| `id`          | `BIGINT`       | 轮播图ID                                        | 主键, 自增         |
| `title`       | `VARCHAR(100)` | 标题                                            | 非空               |
| `image`       | `VARCHAR(255)` | 图片URL                                         | 非空               |
| `link`        | `VARCHAR(255)` | 跳转链接                                        | 可空               |
| `target_id`   | `BIGINT`       | 目标ID                                          | 可空               |
| `target_type` | `TINYINT`      | 目标类型 (0=无, 1=食堂, 2=窗口, 3=博客, 4=话题) | 可空               |
| `sort`        | `INT`          | 排序值                                          | 非空, 默认0        |
| `status`      | `TINYINT`      | 状态 (0=正常, 1=下线)                           | 非空, 默认0        |
| `start_time`  | `DATETIME`     | 开始时间                                        | 可空               |
| `end_time`    | `DATETIME`     | 结束时间                                        | 可空               |
| `create_time` | `DATETIME`     | 创建时间                                        | 非空, 默认当前时间 |
| `update_time` | `DATETIME`     | 更新时间                                        | 非空, 默认当前时间 |

**插入样例**：

```
INSERT INTO tb_banner (title, image, target_id, target_type, sort, status, start_time, end_time, create_time)
VALUES ('第一食堂美食节', '/banners/food_festival.jpg', 1, 1, 1, 0, '2023-05-01 00:00:00', '2023-05-10 23:59:59', NOW());
```

#### 6.3 `tb_image_category` - 图片分类表

**描述**：定义系统管理图片的分类（如食堂环境图等）。

| 字段名        | 类型           | 说明     | 约束说明           |
| ------------- | -------------- | -------- | ------------------ |
| `id`          | `BIGINT`       | 分类ID   | 主键, 自增         |
| `name`        | `VARCHAR(50)`  | 分类名称 | 非空               |
| `description` | `VARCHAR(255)` | 分类描述 | 可空               |
| `sort`        | `INT`          | 排序值   | 非空, 默认0        |
| `create_time` | `DATETIME`     | 创建时间 | 非空, 默认当前时间 |
| `update_time` | `DATETIME`     | 更新时间 | 非空, 默认当前时间 |

**插入样例**：

```
INSERT INTO tb_image_category (name, description, sort, create_time)
VALUES ('食堂环境', '记录各食堂的环境照片', 1, NOW());
```

#### 6.4 `tb_image` - 图片管理表

**描述**：存储由系统管理员上传和管理的图片信息。

| 字段名        | 类型           | 说明                    | 约束说明                       |
| ------------- | -------------- | ----------------------- | ------------------------------ |
| `id`          | `BIGINT`       | 图片ID                  | 主键, 自增                     |
| `category_id` | `BIGINT`       | 分类ID                  | 外键 -> `tb_image_category.id` |
| `admin_id`    | `BIGINT`       | 上传管理员ID            | 外键 -> `tb_admin.id`          |
| `name`        | `VARCHAR(100)` | 图片名称                | 非空                           |
| `url`         | `VARCHAR(255)` | 图片URL                 | 非空                           |
| `width`       | `INT`          | 图片宽度                | 非空                           |
| `height`      | `INT`          | 图片高度                | 非空                           |
| `size`        | `INT`          | 图片大小(字节)          | 非空                           |
| `description` | `VARCHAR(255)` | 图片描述                | 可空                           |
| `status`      | `TINYINT`      | 状态 (0=正常, 1=已删除) | 非空, 默认0                    |
| `create_time` | `DATETIME`     | 创建时间                | 非空, 默认当前时间             |
| `update_time` | `DATETIME`     | 更新时间                | 非空, 默认当前时间             |

**插入样例**：

```
INSERT INTO tb_image (category_id, admin_id, name, url, width, height, size, description, create_time)
VALUES (1, 1, '第一食堂大厅', '/system/images/canteen1_hall.jpg', 1920, 1080, 2048000, '第一食堂一楼大厅全景', NOW());
```

### 7. 统计与分析模块 (Statistics & Analysis)

存储排行榜、统计数据及用户偏好。

#### 7.1 `tb_ranking` - 排行榜表

**描述**：存储系统中各类排行榜数据。

| 字段名                         | 类型       | 说明                                          | 约束说明           |
| ------------------------------ | ---------- | --------------------------------------------- | ------------------ |
| `id`                           | `BIGINT`   | 排行ID                                        | 主键, 自增         |
| `type`                         | `TINYINT`  | 排行类型 (0=菜品, 1=食堂, 2=窗口, 3=用户积分) | 非空               |
| `target_id`                    | `BIGINT`   | 目标ID                                        | 非空               |
| `score`                        | `DOUBLE`   | 评分/数值                                     | 非空               |
| `rank`                         | `INT`      | 排名                                          | 非空               |
| `update_time`                  | `DATETIME` | 更新时间                                      | 非空, 默认当前时间 |
| `UNIQUE KEY (type, target_id)` |            | 复合唯一键                                    | 防止重复           |

**插入样例**：

```
INSERT INTO tb_ranking (type, target_id, score, rank, update_time)
VALUES (0, 1, 4.8, 1, NOW());
```

#### 7.2 `tb_statistics` - 统计数据表

**描述**：存储系统各类统计数据。

| 字段名                                       | 类型          | 说明                                            | 约束说明           |
| -------------------------------------------- | ------------- | ----------------------------------------------- | ------------------ |
| `id`                                         | `BIGINT`      | 统计ID                                          | 主键, 自增         |
| `type`                                       | `TINYINT`     | 统计类型 (0=用户活跃度, 1=评分分布, 2=评论热度) | 非空               |
| `target_id`                                  | `BIGINT`      | 目标ID (可以是食堂/窗口/菜品ID)                 | 可空               |
| `period`                                     | `VARCHAR(20)` | 统计周期 (day/week/month/year)                  | 非空               |
| `date`                                       | `DATE`        | 统计日期                                        | 非空               |
| `data`                                       | `TEXT`        | 统计数据(JSON字符串)                            | 非空               |
| `create_time`                                | `DATETIME`    | 创建时间                                        | 非空, 默认当前时间 |
| `UNIQUE KEY (type, target_id, period, date)` |               | 复合唯一键                                      | 防止重复           |

**插入样例**：



```
INSERT INTO tb_statistics (type, target_id, period, date, data, create_time)
VALUES (1, 1, 'month', '2023-05-01', '{"score5": 120, "score4": 45, "score3": 15, "score2": 5, "score1": 2}', NOW());
```

#### 7.3 `tb_user_preference` - 用户偏好表

**描述**：记录用户的偏好信息，用于个性化推荐。

| 字段名             | 类型           | 说明                                  | 约束说明             |
| ------------------ | -------------- | ------------------------------------- | -------------------- |
| `id`               | `BIGINT`       | 偏好ID                                | 主键, 自增           |
| `user_id`          | `BIGINT`       | 用户ID                                | 外键 -> `tb_user.id` |
| `preference_type`  | `TINYINT`      | 偏好类型 (0=口味, 1=价格, 2=窗口类型) | 非空                 |
| `preference_value` | `VARCHAR(100)` | 偏好值                                | 非空                 |
| `weight`           | `DOUBLE`       | 权重值                                | 非空, 默认1.0        |
| `create_time`      | `DATETIME`     | 创建时间                              | 非空, 默认当前时间   |
| `update_time`      | `DATETIME`     | 更新时间                              | 非空, 默认当前时间   |

**插入样例**：

```
INSERT INTO tb_user_preference (user_id, preference_type, preference_value, weight, create_time)
VALUES (1, 0, '川菜', 1.5, NOW());
```
