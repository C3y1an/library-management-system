# 图书管理系统前端

这是图书管理系统的 Vue 3 前端，使用 Vite 和 Element Plus。

## 启动前端

```powershell
npm install
npm run dev
```

前端默认地址通常是：

```text
http://localhost:5173
```

## 后端地址

前端默认请求后端：

```text
http://localhost:8080/api
```

如果后端地址不同，可以通过环境变量 `VITE_API_BASE_URL` 指定。

PowerShell 示例：

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080/api"
npm run dev
```

## 构建

```powershell
npm run build
```
