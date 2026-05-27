# 图书管理系统前端与桌面端

该目录包含 Vue 3 Web 前端和 Electron 桌面端。Web 前端负责业务界面，Electron 主进程负责桌面菜单、本地后端启动和数据库切换配置。

## 技术栈

- Vue 3
- Vite
- Element Plus
- Pinia
- Axios
- Electron
- electron-builder

## Web 开发

```powershell
npm install
npm run dev
```

默认开发地址通常为：

```text
http://localhost:5173
```

默认 API 地址：

```text
http://localhost:8080/api
```

可通过环境变量覆盖：

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080/api"
npm run dev
```

## Electron 开发

先启动 Vite：

```powershell
npm run dev
```

再启动 Electron：

```powershell
npm run electron:dev
```

Electron 开发模式会自动执行：

```powershell
npm run build:backend
```

并启动后端 Jar。默认桌面端 API 地址：

```text
http://127.0.0.1:18080/api
```

## 数据库切换

桌面端菜单：

```text
设置 -> 切换数据库
```

支持的模式：

- 内置本地 H2 文件数据库
- 自定义本地 H2 文件数据库
- 本机 MySQL 数据库
- 云端 API 服务

云端 API 地址可以填写 Render 服务根地址或完整 `/api` 地址。程序会自动规范为：

```text
https://YOUR-RENDER-SERVICE.onrender.com/api
```

## 构建

Web 构建：

```powershell
npm run build
```

后端 Jar 构建：

```powershell
npm run build:backend
```

桌面端安装包构建：

```powershell
npm run build:desktop
```

桌面端构建产物输出到：

```text
release/
```
