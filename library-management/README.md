# 图书管理系统后端

Spring Boot 后端服务提供图书、读者、借阅、归还、统计和登录接口。接口默认以 `/api` 开头。

## 技术栈

- Spring Boot 4
- Spring Data JPA
- Jakarta Validation
- MySQL
- H2

## 启动方式

### dev 模式

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

`dev` 模式使用 H2 内存数据库，启动时自动初始化示例图书和读者数据。

H2 控制台：

```text
http://localhost:8080/h2-console
```

### MySQL 模式

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/library_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的MySQL密码"

.\mvnw.cmd spring-boot:run
```

### desktop 模式

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=desktop"
```

`desktop` 模式面向 Electron 桌面端，支持通过环境变量切换 H2 文件库或 MySQL：

- `DB_URL`
- `DB_DRIVER_CLASS_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `PORT`
- `CORS_ALLOWED_ORIGIN_PATTERNS`

## 默认登录账号

```text
用户名：admin
密码：admin123
```

## 接口列表

### 登录

- `POST /api/auth/login`

### 图书管理

- `GET /api/books?keyword=&category=&status=available`
- `GET /api/books/{id}`
- `POST /api/books`
- `PUT /api/books/{id}`
- `DELETE /api/books/{id}`

### 读者管理

- `GET /api/readers?keyword=&status=ACTIVE`
- `GET /api/readers/{id}`
- `POST /api/readers`
- `PUT /api/readers/{id}`
- `DELETE /api/readers/{id}`

### 借阅归还

- `GET /api/borrows?keyword=&status=BORROWED`
- `GET /api/borrows/{id}`
- `POST /api/borrows`
- `POST /api/borrows/{id}/return`
- `DELETE /api/borrows/{id}`

### 统计

- `GET /api/statistics/overview`

## 数据规则

- 借阅登记会减少图书可借库存。
- 归还登记会增加图书可借库存。
- 删除“借阅中”的借阅记录会恢复图书可借库存。
- 删除“已归还”的借阅记录不会重复调整库存。
- 存在借阅历史的图书或读者不能直接删除。

## 测试

```powershell
.\mvnw.cmd test
```
