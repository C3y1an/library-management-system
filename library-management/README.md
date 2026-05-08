# 图书管理系统后端

Spring Boot + Spring Data JPA 后端服务，默认接口前缀为 `/api`。

## 启动方式

使用默认 MySQL 配置：

```powershell
.\mvnw.cmd spring-boot:run
```

如果本机还没有 MySQL，可以先用 H2 内存库启动：

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

开发模式会自动初始化两本图书和两名读者。H2 控制台地址为 `http://localhost:8080/h2-console`。

## 默认账号

```text
用户名: admin
密码: admin123
```

## 主要接口

### 登录

- `POST /api/auth/login`

```json
{
  "username": "admin",
  "password": "admin123"
}
```

### 图书管理

- `GET /api/books?keyword=&category=&status=available`
- `GET /api/books/{id}`
- `POST /api/books`
- `PUT /api/books/{id}`
- `DELETE /api/books/{id}`

```json
{
  "title": "软件工程导论",
  "author": "张海藩",
  "isbn": "9787302142180",
  "publisher": "清华大学出版社",
  "category": "软件工程",
  "location": "A-01-01",
  "publishDate": "2022-01-01",
  "totalCopies": 5,
  "description": "软件工程课程设计参考书"
}
```

### 读者管理

- `GET /api/readers?keyword=&status=ACTIVE`
- `GET /api/readers/{id}`
- `POST /api/readers`
- `PUT /api/readers/{id}`
- `DELETE /api/readers/{id}`

```json
{
  "name": "李明",
  "gender": "男",
  "phone": "13800000001",
  "email": "liming@example.com",
  "department": "软件工程1班",
  "cardNumber": "R20260001",
  "registeredDate": "2026-05-06",
  "status": "ACTIVE"
}
```

### 借阅归还

- `GET /api/borrows?keyword=&status=BORROWED`
- `GET /api/borrows/{id}`
- `POST /api/borrows`
- `POST /api/borrows/{id}/return`

```json
{
  "bookId": 1,
  "readerId": 1,
  "borrowDate": "2026-05-06",
  "dueDate": "2026-06-05"
}
```

### 统计

- `GET /api/statistics/overview`

返回图书种类数、馆藏册数、可借册数、读者数、当前借阅数和逾期数。
