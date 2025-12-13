-- 1. 创建数据库（指定字符集和排序规则，避免中文乱码）
CREATE DATABASE IF NOT EXISTS community_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2. 使用创建的数据库
USE community_system;

 -- 3. 创建用户表

create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    vipExpireTime datetime     null comment '会员过期时间',
    vipCode       varchar(128) null comment '会员兑换码',
    vipNumber     bigint       null comment '会员编号',
    index idx_unionId (unionId)
    ) comment '用户' collate = utf8mb4_unicode_ci;
-- 4. 题库题目表
-- 题库表
create table if not exists question_bank
(
    id          bigint auto_increment comment 'id' primary key,
    title       varchar(256)                       null comment '标题',
    description text                               null comment '描述',
    picture     varchar(2048)                      null comment '图片',
    userId      bigint                             not null comment '创建用户 id',
    editTime    datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    viewNum  int  default 0  not null comment '浏览量', -- 这个可以设置根据热度去 排序
    index idx_title (title)
    ) comment '题库' collate = utf8mb4_unicode_ci;

-- 题目表
create table if not exists question
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(256)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    answer     text                               null comment '推荐答案',
    userId     bigint                             not null comment '创建用户 id',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    viewNum       int      default 0    not null comment '浏览量',
    thumbNum      int      default 0    not null comment '点赞数',
    favourNum     int      default 0    not null comment '收藏数',
    index idx_title (title),
    index idx_userId (userId)
    ) comment '题目' collate = utf8mb4_unicode_ci;

-- 题库题目表（硬删除）
create table if not exists question_bank_question
(
    id             bigint auto_increment comment 'id' primary key,
    questionBankId bigint                             not null comment '题库 id',
    questionId     bigint                             not null comment '题目 id',
    userId         bigint                             not null comment '创建用户 id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    UNIQUE (questionBankId, questionId)
    ) comment '题库题目' collate = utf8mb4_unicode_ci;


-- 5. 题目点赞表
CREATE TABLE IF NOT EXISTS question_thumb (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    questionId BIGINT NOT NULL COMMENT '题目ID',
    userId BIGINT NOT NULL COMMENT '用户ID',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_question_user (questionId, userId),
    INDEX idx_userId (userId)
) COMMENT '题目点赞' COLLATE = utf8mb4_unicode_ci;

-- 6. 题目收藏表
CREATE TABLE IF NOT EXISTS question_favour (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    questionId BIGINT NOT NULL COMMENT '题目ID',
    userId BIGINT NOT NULL COMMENT '用户ID',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_question_user (questionId, userId),
    INDEX idx_userId (userId)
) COMMENT '题目收藏' COLLATE = utf8mb4_unicode_ci;

-- 7. 题目评论表（树形结构）
CREATE TABLE IF NOT EXISTS question_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    questionId BIGINT NOT NULL COMMENT '题目ID',
    userId BIGINT NOT NULL COMMENT '评论用户ID',
    parentId BIGINT DEFAULT 0 NOT NULL COMMENT '父评论ID（0表示顶级评论）',
    replyUserId BIGINT DEFAULT NULL COMMENT '被回复用户ID',
    content VARCHAR(1000) NOT NULL COMMENT '评论内容',
    thumbNum INT DEFAULT 0 NOT NULL COMMENT '点赞数',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    INDEX idx_questionId (questionId),
    INDEX idx_userId (userId),
    INDEX idx_parentId (parentId)
) COMMENT '题目评论' COLLATE = utf8mb4_unicode_ci;

-- 8. 评论点赞表
CREATE TABLE IF NOT EXISTS question_comment_thumb (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    commentId BIGINT NOT NULL COMMENT '评论ID',
    userId BIGINT NOT NULL COMMENT '用户ID',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_comment_user (commentId, userId),
    INDEX idx_userId (userId)
) COMMENT '评论点赞' COLLATE = utf8mb4_unicode_ci;
