# 图书管理系统

图书管理系统包含 Spring Boot 后端、Vue 3 Web 前端和 Electron 桌面端。系统提供图书管理、读者管理、借阅登记、归还登记、借阅记录删除、统计概览，以及本地/云端 API 数据源切换能力。

## 技术栈

- 后端：Spring Boot 4、Spring Data JPA、MySQL、H2
- 前端：Vue 3、Vite、Element Plus、Pinia、Axios
- 桌面端：Electron、electron-builder
- 部署：Render Web Service、Netlify

## 项目结构

```text
library-management/      Spring Boot 后端服务
library-management-web/  Vue Web 前端与 Electron 桌面端
DEPLOYMENT.md            云端部署说明
render.yaml              Render Blueprint 配置
netlify.toml             Netlify 构建配置
```

## 功能概览

- 登录入口：默认账号 `admin`，默认密码 `admin123`
- 首页概览：馆藏数量、读者数量、当前借阅、逾期记录、近期借阅记录
- 图书管理：新增、编辑、删除、查询、分类过滤、状态过滤、排序
- 读者管理：新增、编辑、删除、查询、状态过滤、院系/性别过滤、排序
- 借阅归还：登记借阅、登记归还、删除借阅记录、查询、状态过滤、排序
- 数据源切换：
  - 内置本地 H2 文件数据库
  - 自定义本地 H2 文件数据库
  - 本机 MySQL 数据库
  - 云端 API 服务

## 本地开发

### 后端开发服务

```powershell
cd library-management
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

`dev` 模式使用 H2 内存数据库，启动时会初始化示例图书和读者数据。后端默认地址：

```text
http://localhost:8080
```

### 前端开发服务

```powershell
cd library-management-web
npm install
npm run dev
```

前端默认地址通常为：

```text
http://localhost:5173
```

### Electron 开发模式

先启动 Vite：

```powershell
cd library-management-web
npm run dev
```

再启动 Electron：

```powershell
cd library-management-web
npm run electron:dev
```

Electron 开发模式会先构建后端 Jar，再由 Electron 主进程自动启动本地后端。默认本地 API 地址：

```text
http://127.0.0.1:18080/api
```

## 数据库模式

### H2 开发库

后端 `dev` profile 使用 H2 内存数据库，适合开发调试。

### Electron 内置本地数据库

桌面端默认使用 H2 文件数据库，数据保存在 Electron 的用户数据目录。用户可以在桌面端菜单中打开：

```text
设置 -> 切换数据库
```

可切换为内置 H2、自定义 H2 文件、本机 MySQL 或云端 API。

### 本机 MySQL

先创建数据库：

```sql
CREATE DATABASE library_management DEFAULT CHARACTER SET utf8mb4;
```

再配置环境变量启动后端：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/library_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的MySQL密码"

cd library-management
.\mvnw.cmd spring-boot:run
```

## 云端部署

推荐部署组合：

- 后端：Render Web Service
- 前端：Netlify
- 数据库：MySQL 云数据库

后端 API 基础地址格式：

```text
https://YOUR-RENDER-SERVICE.onrender.com/api
```

部署细节见 [DEPLOYMENT.md](./DEPLOYMENT.md)。

## 常用命令

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

桌面端构建：

```powershell
cd library-management-web
npm run build:desktop
```
