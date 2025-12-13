# 用户管理系统前端

基于 Vue 3 + TypeScript + Element Plus 开发的用户管理系统前端应用。

## 技术栈

- Vue 3 (Composition API)
- TypeScript
- Element Plus
- Pinia (状态管理)
- Vue Router (路由)
- Axios (HTTP 请求)

## 功能特性

- ✅ 用户登录/注册
- ✅ 用户仪表盘
- ✅ 用户管理后台（管理员权限）
  - 分页表格展示用户列表
  - 搜索功能
  - 新增用户
  - 编辑用户
  - 删除用户

## 项目结构

```
src/
├── api/              # API 接口封装
│   └── user.ts
├── stores/           # Pinia 状态管理
│   └── user.ts
├── router/           # 路由配置
│   └── index.ts
├── types/            # TypeScript 类型定义
│   └── user.ts
├── utils/            # 工具函数
│   └── request.ts
├── views/            # 页面组件
│   ├── auth/         # 认证相关页面
│   │   ├── Login.vue
│   │   └── Register.vue
│   ├── dashboard/    # 仪表盘
│   │   └── Dashboard.vue
│   └── admin/        # 管理后台
│       └── UserManagement.vue
├── App.vue           # 根组件
└── main.ts           # 入口文件
```

## 安装依赖

```bash
npm install
```

## 开发运行

```bash
npm run dev
```

应用将在 http://localhost:3000 启动。

## 构建生产版本

```bash
npm run build
```

## 后端接口说明

后端 API 运行在 `http://localhost:8080`，Vite 开发服务器已配置代理。

### 接口列表

- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/get/loginuser` - 获取当前登录用户
- `POST /api/user/logout` - 用户登出
- `POST /api/user/add` - 管理员创建用户
- `DELETE /api/user/delete` - 管理员删除用户
- `PUT /api/user/updateuserinfo` - 管理员更新用户信息
- `POST /api/user/list` - 管理员分页查询用户列表
- `GET /api/user/getone/{id}` - 管理员按 ID 获取用户详情
- `POST /api/user/getone/safe` - 普通用户按 ID 安全获取脱敏用户信息

## 注意事项

1. 登录成功后，token 会存储在 localStorage 中
2. 所有需要认证的请求会自动在请求头中添加 token
3. 管理员权限验证：只有 `userRole === 'admin'` 的用户才能访问管理后台
4. 非管理员用户访问管理页面会自动重定向到仪表盘

