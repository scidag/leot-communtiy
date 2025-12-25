-- 测试数据 SQL
-- 请在执行 create_table.sql 后执行此文件

USE community_system;

-- 1. 插入测试用户（密码为 123456 的 MD5 加密）
-- 管理员账号: admin / 123456
-- 普通用户: user1 / 123456
INSERT INTO user (userAccount, userPassword, userName, userRole, userProfile) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 'admin', '系统管理员'),
('user1', 'e10adc3949ba59abbe56e057f20f883e', '测试用户', 'user', '普通测试用户'),
('user2', 'e10adc3949ba59abbe56e057f20f883e', '学习者', 'user', '热爱学习的程序员');

-- 2. 插入题库数据
INSERT INTO question_bank (title, description, picture, userId) VALUES
('Java 基础', 'Java 语言基础知识，包括语法、面向对象、集合框架等', 'https://img.icons8.com/color/480/java-coffee-cup-logo.png', 1),
('Spring 框架', 'Spring 全家桶相关知识，包括 Spring Boot、Spring Cloud 等', 'https://img.icons8.com/color/480/spring-logo.png', 1),
('MySQL 数据库', 'MySQL 数据库原理与优化，包括索引、事务、锁机制等', 'https://img.icons8.com/color/480/mysql-logo.png', 1),
('Redis 缓存', 'Redis 缓存技术，包括数据结构、持久化、集群等', 'https://img.icons8.com/color/480/redis.png', 1),
('JVM 虚拟机', 'JVM 内存模型、垃圾回收、性能调优等', 'https://img.icons8.com/color/480/java-coffee-cup-logo.png', 1),
('并发编程', 'Java 多线程与并发编程，包括线程池、锁、并发容器等', 'https://img.icons8.com/color/480/process.png', 1),
('计算机网络', '网络协议、HTTP、TCP/IP、网络安全等', 'https://img.icons8.com/color/480/network.png', 1),
('设计模式', '常用设计模式及其应用场景', 'https://img.icons8.com/color/480/design.png', 1);

-- 3. 插入题目数据
-- Java 基础题目
INSERT INTO question (title, content, answer, tags, userId, viewNum, thumbNum, favourNum) VALUES
('什么是 Java 的面向对象特性？', 
'<p>请详细说明 Java 面向对象的四大特性。</p>', 
'<p><strong>Java 面向对象四大特性：</strong></p>
<ol>
<li><strong>封装</strong>：将数据和操作数据的方法绑定在一起，对外隐藏实现细节</li>
<li><strong>继承</strong>：子类继承父类的属性和方法，实现代码复用</li>
<li><strong>多态</strong>：同一操作作用于不同对象产生不同的行为</li>
<li><strong>抽象</strong>：将共同特征抽取出来形成抽象类或接口</li>
</ol>', 
'["Java", "面向对象", "基础"]', 1, 156, 23, 12),

('HashMap 的底层实现原理？', 
'<p>请详细说明 HashMap 在 JDK 1.8 中的底层数据结构和实现原理。</p>', 
'<p><strong>HashMap 底层实现：</strong></p>
<p>JDK 1.8 中 HashMap 采用 <strong>数组 + 链表 + 红黑树</strong> 的数据结构。</p>
<ul>
<li>默认初始容量为 16，负载因子为 0.75</li>
<li>当链表长度超过 8 且数组长度超过 64 时，链表转换为红黑树</li>
<li>当红黑树节点数小于 6 时，退化为链表</li>
<li>扩容时容量翻倍，重新计算元素位置</li>
</ul>', 
'["Java", "集合", "HashMap"]', 1, 234, 45, 28),

('String、StringBuilder、StringBuffer 的区别？', 
'<p>请说明这三者的区别和使用场景。</p>', 
'<p><strong>区别：</strong></p>
<ul>
<li><strong>String</strong>：不可变字符串，每次修改都会创建新对象</li>
<li><strong>StringBuilder</strong>：可变字符串，线程不安全，性能高</li>
<li><strong>StringBuffer</strong>：可变字符串，线程安全，性能较低</li>
</ul>
<p><strong>使用场景：</strong></p>
<ul>
<li>字符串不经常变化：使用 String</li>
<li>单线程大量字符串操作：使用 StringBuilder</li>
<li>多线程大量字符串操作：使用 StringBuffer</li>
</ul>', 
'["Java", "String", "基础"]', 1, 189, 34, 19);

-- Spring 框架题目
INSERT INTO question (title, content, answer, tags, userId, viewNum, thumbNum, favourNum) VALUES
('Spring IOC 和 AOP 的理解？', 
'<p>请详细说明 Spring 的 IOC 和 AOP 原理。</p>', 
'<p><strong>IOC（控制反转）：</strong></p>
<p>将对象的创建和依赖关系的管理交给 Spring 容器，而不是由程序员手动创建。</p>
<p><strong>AOP（面向切面编程）：</strong></p>
<p>将横切关注点（如日志、事务）从业务逻辑中分离出来，通过动态代理实现。</p>', 
'["Spring", "IOC", "AOP"]', 1, 267, 56, 34),

('Spring Boot 自动配置原理？', 
'<p>请说明 Spring Boot 是如何实现自动配置的。</p>', 
'<p><strong>自动配置原理：</strong></p>
<ol>
<li>@SpringBootApplication 包含 @EnableAutoConfiguration</li>
<li>通过 SpringFactoriesLoader 加载 META-INF/spring.factories</li>
<li>根据 @Conditional 条件判断是否加载配置类</li>
<li>配置类通过 @ConfigurationProperties 绑定配置属性</li>
</ol>', 
'["Spring Boot", "自动配置"]', 1, 198, 42, 25);

-- MySQL 题目
INSERT INTO question (title, content, answer, tags, userId, viewNum, thumbNum, favourNum) VALUES
('MySQL 索引的类型和原理？', 
'<p>请说明 MySQL 中常见的索引类型及其底层原理。</p>', 
'<p><strong>索引类型：</strong></p>
<ul>
<li><strong>B+Tree 索引</strong>：最常用，支持范围查询</li>
<li><strong>Hash 索引</strong>：等值查询快，不支持范围查询</li>
<li><strong>全文索引</strong>：用于全文搜索</li>
<li><strong>空间索引</strong>：用于地理数据</li>
</ul>
<p><strong>B+Tree 特点：</strong></p>
<ul>
<li>所有数据存储在叶子节点</li>
<li>叶子节点通过指针连接，支持范围查询</li>
<li>非叶子节点只存储索引键</li>
</ul>', 
'["MySQL", "索引", "B+Tree"]', 1, 312, 67, 41),

('事务的 ACID 特性？', 
'<p>请详细说明数据库事务的四大特性。</p>', 
'<p><strong>ACID 特性：</strong></p>
<ul>
<li><strong>原子性（Atomicity）</strong>：事务是不可分割的最小单位</li>
<li><strong>一致性（Consistency）</strong>：事务前后数据保持一致</li>
<li><strong>隔离性（Isolation）</strong>：多个事务之间相互隔离</li>
<li><strong>持久性（Durability）</strong>：事务提交后数据永久保存</li>
</ul>', 
'["MySQL", "事务", "ACID"]', 1, 245, 52, 31);

-- Redis 题目
INSERT INTO question (title, content, answer, tags, userId, viewNum, thumbNum, favourNum) VALUES
('Redis 的数据类型有哪些？', 
'<p>请说明 Redis 支持的数据类型及其使用场景。</p>', 
'<p><strong>五种基本数据类型：</strong></p>
<ul>
<li><strong>String</strong>：缓存、计数器、分布式锁</li>
<li><strong>Hash</strong>：存储对象</li>
<li><strong>List</strong>：消息队列、最新列表</li>
<li><strong>Set</strong>：去重、交集并集</li>
<li><strong>ZSet</strong>：排行榜、延迟队列</li>
</ul>', 
'["Redis", "数据类型"]', 1, 278, 58, 35);

-- 4. 建立题库与题目的关联
INSERT INTO question_bank_question (questionBankId, questionId, userId) VALUES
-- Java 基础题库
(1, 1, 1), (1, 2, 1), (1, 3, 1),
-- Spring 框架题库
(2, 4, 1), (2, 5, 1),
-- MySQL 题库
(3, 6, 1), (3, 7, 1),
-- Redis 题库
(4, 8, 1);

-- 5. 添加一些点赞和收藏数据
INSERT INTO question_thumb (questionId, userId) VALUES
(1, 2), (1, 3), (2, 2), (4, 2), (6, 2);

INSERT INTO question_favour (questionId, userId) VALUES
(1, 2), (2, 2), (4, 2), (6, 3);

-- 6. 添加一些评论数据
INSERT INTO question_comment (questionId, userId, parentId, content, thumbNum) VALUES
(1, 2, 0, '讲解得很清楚，感谢分享！', 5),
(1, 3, 0, '面向对象是 Java 的核心，必须掌握', 3),
(2, 2, 0, 'HashMap 面试必问，这个总结很全面', 8),
(2, 3, 3, '确实，我面试就被问到了', 2);

SELECT '测试数据插入完成！' AS message;
SELECT CONCAT('用户数量: ', COUNT(*)) AS info FROM user;
SELECT CONCAT('题库数量: ', COUNT(*)) AS info FROM question_bank;
SELECT CONCAT('题目数量: ', COUNT(*)) AS info FROM question;
