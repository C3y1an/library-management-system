# 图书管理系统后端

这是图书管理系统的 Spring Boot 后端服务，接口默认以 `/api` 开头。

## 推荐启动方式：dev 模式

首次运行或本地演示时，推荐使用 `dev` 模式：

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

`dev` 模式会使用 H2 内存数据库：

- 不需要安装 MySQL。
- 启动时自动初始化示例图书和读者数据。
- 服务关闭后数据会清空。
- 下次启动会重新生成示例数据。

H2 控制台地址：

```text
http://localhost:8080/h2-console
```

## 使用 MySQL 启动

如果要长期保存真实数据，请使用 MySQL。默认配置会读取以下环境变量：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

PowerShell 示例：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/library_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的MySQL密码"

.\mvnw.cmd spring-boot:run
```

## 默认登录账号

```text
用户名：admin
密码：admin123
```

## 主要接口

登录：

- `POST /api/auth/login`

图书管理：

- `GET /api/books?keyword=&category=&status=available`
- `GET /api/books/{id}`
- `POST /api/books`
- `PUT /api/books/{id}`
- `DELETE /api/books/{id}`

读者管理：

- `GET /api/readers?keyword=&status=ACTIVE`
- `GET /api/readers/{id}`
- `POST /api/readers`
- `PUT /api/readers/{id}`
- `DELETE /api/readers/{id}`

借阅归还：

- `GET /api/borrows?keyword=&status=BORROWED`
- `GET /api/borrows/{id}`
- `POST /api/borrows`
- `POST /api/borrows/{id}/return`

统计：

- `GET /api/statistics/overview`
