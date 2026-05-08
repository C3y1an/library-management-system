# 图书管理系统

这是一个前后端分离的图书管理系统，包含 Spring Boot 后端和 Vue 3 前端。

## 项目结构

- `library-management/`：后端服务，提供图书、读者、借阅、归还、统计和登录接口。
- `library-management-web/`：前端页面，基于 Vue 3、Vite 和 Element Plus。
- `word/`：本地文档资料目录，不属于项目代码，已通过 `.gitignore` 排除，不会上传到 GitHub。

## 首次运行推荐方式

第一次下载项目的人，推荐使用后端的 `dev` 模式启动。这个模式使用 H2 内存数据库，不需要安装 MySQL，启动时会自动初始化示例图书和读者数据。

### 1. 启动后端

```powershell
cd library-management
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

后端默认地址：

```text
http://localhost:8080
```

### 2. 启动前端

新开一个终端：

```powershell
cd library-management-web
npm install
npm run dev
```

前端默认地址通常是：

```text
http://localhost:5173
```

### 3. 登录账号

```text
用户名：admin
密码：admin123
```

## 数据说明

`dev` 模式使用的是 H2 内存数据库，适合演示和开发：

- 不需要安装 MySQL。
- 启动时会自动生成示例数据。
- 服务关闭后，内存数据库的数据会清空。
- 下次启动会重新初始化示例数据。

如果需要长期保存真实数据，请使用 MySQL，并通过环境变量配置数据库连接。

## 使用 MySQL 启动

先在本机创建数据库，例如：

```sql
CREATE DATABASE library_management DEFAULT CHARACTER SET utf8mb4;
```

然后设置环境变量并启动后端：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/library_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的MySQL密码"

cd library-management
.\mvnw.cmd spring-boot:run
```

MySQL 中的数据只存在使用者自己的电脑或服务器上，不会因为项目上传到 GitHub 而一起上传。

## 常用开发命令

后端测试：

```powershell
cd library-management
.\mvnw.cmd test
```

前端构建：

```powershell
cd library-management-web
npm run build
```

## Git 维护建议

每次修改后可以按这个流程提交并上传：

```powershell
git status
git add .
git commit -m "说明这次修改了什么"
git push
```
