const { app, BrowserWindow, Menu, dialog, ipcMain } = require('electron')
const path = require('node:path')
const fs = require('node:fs')
const { spawn } = require('node:child_process')

const API_PORT = process.env.DESKTOP_API_PORT || '18080'
const LOCAL_API_BASE_URL = `http://127.0.0.1:${API_PORT}/api`

let backendProcess = null
let mainWindow = null
let settingsWindow = null
let activeSettings = null
let activeApiBaseUrl = LOCAL_API_BASE_URL

function normalizeH2FilePath(filePath) {
  const cleanPath = String(filePath || '').trim()
  if (!cleanPath) {
    return ''
  }

  return cleanPath.replace(/\.mv\.db$/i, '')
}

function normalizeApiBaseUrl(url) {
  const cleanUrl = String(url || '').trim().replace(/\/+$/, '')
  if (!cleanUrl) {
    return ''
  }

  return cleanUrl.endsWith('/api') ? cleanUrl : `${cleanUrl}/api`
}

function defaultSettings() {
  return {
    databaseMode: 'local',
    localDatabaseType: 'embedded',
    customH2Path: '',
    mysqlHost: '127.0.0.1',
    mysqlPort: '3306',
    mysqlDatabase: 'library_management',
    mysqlUsername: 'root',
    mysqlPassword: '',
    cloudApiBaseUrl: '',
  }
}

function getSettingsPath() {
  return path.join(app.getPath('userData'), 'settings.json')
}

function getBackendLogPath() {
  const logsPath = path.join(app.getPath('userData'), 'logs')
  fs.mkdirSync(logsPath, { recursive: true })
  return path.join(logsPath, 'backend.log')
}

function readSettings() {
  const fallback = defaultSettings()

  try {
    const settingsPath = getSettingsPath()
    if (!fs.existsSync(settingsPath)) {
      return fallback
    }

    return sanitizeSettings({
      ...fallback,
      ...JSON.parse(fs.readFileSync(settingsPath, 'utf8')),
    })
  } catch (_) {
    return fallback
  }
}

function sanitizeSettings(settings) {
  const fallback = defaultSettings()
  const nextSettings = {
    ...fallback,
    ...settings,
  }

  nextSettings.databaseMode = nextSettings.databaseMode === 'cloud' ? 'cloud' : 'local'
  nextSettings.localDatabaseType = ['embedded', 'h2File', 'mysql'].includes(nextSettings.localDatabaseType)
    ? nextSettings.localDatabaseType
    : 'embedded'
  nextSettings.customH2Path = normalizeH2FilePath(nextSettings.customH2Path)
  nextSettings.mysqlHost = String(nextSettings.mysqlHost || fallback.mysqlHost).trim()
  nextSettings.mysqlPort = String(nextSettings.mysqlPort || fallback.mysqlPort).trim()
  nextSettings.mysqlDatabase = String(nextSettings.mysqlDatabase || fallback.mysqlDatabase).trim()
  nextSettings.mysqlUsername = String(nextSettings.mysqlUsername || fallback.mysqlUsername).trim()
  nextSettings.mysqlPassword = String(nextSettings.mysqlPassword || '')
  nextSettings.cloudApiBaseUrl = normalizeApiBaseUrl(nextSettings.cloudApiBaseUrl)

  return nextSettings
}

function writeSettings(settings) {
  const nextSettings = sanitizeSettings(settings)
  fs.writeFileSync(getSettingsPath(), `${JSON.stringify(nextSettings, null, 2)}\n`, 'utf8')
  return nextSettings
}

function getBackendJarPath() {
  const packagedJar = path.join(process.resourcesPath, 'backend', 'library-management.jar')
  if (fs.existsSync(packagedJar)) {
    return packagedJar
  }

  return path.resolve(__dirname, '..', '..', 'library-management', 'target', 'library-management-0.0.1-SNAPSHOT.jar')
}

function getEmbeddedDatabasePath() {
  const dataPath = path.join(app.getPath('userData'), 'data')
  fs.mkdirSync(dataPath, { recursive: true })
  return path.join(dataPath, 'library_management')
}

function ensureH2Directory(databasePath) {
  const directory = path.dirname(databasePath)
  fs.mkdirSync(directory, { recursive: true })
}

function buildLocalDatabaseEnv(settings) {
  if (settings.localDatabaseType === 'mysql') {
    return {
      DB_URL: `jdbc:mysql://${settings.mysqlHost}:${settings.mysqlPort}/${settings.mysqlDatabase}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai`,
      DB_DRIVER_CLASS_NAME: 'com.mysql.cj.jdbc.Driver',
      DB_USERNAME: settings.mysqlUsername,
      DB_PASSWORD: settings.mysqlPassword,
    }
  }

  const h2Path = settings.localDatabaseType === 'h2File' && settings.customH2Path
    ? settings.customH2Path
    : getEmbeddedDatabasePath()

  ensureH2Directory(h2Path)

  return {
    DB_URL: `jdbc:h2:file:${h2Path.replace(/\\/g, '/')};MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE`,
    DB_DRIVER_CLASS_NAME: 'org.h2.Driver',
    DB_USERNAME: 'sa',
    DB_PASSWORD: '',
  }
}

function getLocalDatabaseLabel(settings) {
  if (settings.localDatabaseType === 'mysql') {
    return '本机 MySQL 数据库'
  }

  if (settings.localDatabaseType === 'h2File') {
    return '自定义本地数据库文件'
  }

  return '内置本地数据库'
}

function getActiveModeLabel(settings) {
  if (settings.databaseMode === 'cloud') {
    return '云端服务器数据库'
  }

  return getLocalDatabaseLabel(settings)
}

function resolveActiveApiBaseUrl(settings) {
  if (settings.databaseMode === 'cloud' && settings.cloudApiBaseUrl) {
    return settings.cloudApiBaseUrl
  }

  return LOCAL_API_BASE_URL
}

function applyActiveEnvironment(settings) {
  activeSettings = settings
  activeApiBaseUrl = resolveActiveApiBaseUrl(settings)
  process.env.DESKTOP_API_BASE_URL = activeApiBaseUrl
  process.env.DESKTOP_DATABASE_MODE = settings.databaseMode
}

function startBackend(settings) {
  if (settings.databaseMode === 'cloud') {
    return
  }

  const jarPath = getBackendJarPath()
  if (!fs.existsSync(jarPath)) {
    throw new Error(`找不到后端 Jar 文件：${jarPath}`)
  }

  const backendLog = fs.openSync(getBackendLogPath(), 'a')
  fs.writeSync(backendLog, `\n\n[${new Date().toISOString()}] 启动本地后端：${getLocalDatabaseLabel(settings)}\n`)

  backendProcess = spawn('java', [
    '-jar',
    jarPath,
    '--spring.profiles.active=desktop',
  ], {
    cwd: app.getPath('userData'),
    env: {
      ...process.env,
      ...buildLocalDatabaseEnv(settings),
      PORT: API_PORT,
      CORS_ALLOWED_ORIGIN_PATTERNS: 'http://localhost:*,http://127.0.0.1:*,file://*',
    },
    stdio: ['ignore', backendLog, backendLog],
    windowsHide: true,
  })

  backendProcess.on('exit', () => {
    backendProcess = null
  })
}

async function startLocalBackendWithFallback(settings) {
  startBackend(settings)

  try {
    await waitForApi(LOCAL_API_BASE_URL)
    return settings
  } catch (error) {
    if (backendProcess) {
      backendProcess.kill()
      backendProcess = null
    }

    const fallbackSettings = {
      ...settings,
      databaseMode: 'local',
      localDatabaseType: 'embedded',
    }
    applyActiveEnvironment(fallbackSettings)

    dialog.showMessageBoxSync({
      type: 'warning',
      title: '数据库连接失败',
      message: '所选本地数据库连接失败，已临时改用内置本地数据库启动。',
      detail: `原错误：${error.message}\n\n后端日志：${getBackendLogPath()}`,
      buttons: ['确定'],
    })

    startBackend(fallbackSettings)
    await waitForApi(LOCAL_API_BASE_URL)
    return fallbackSettings
  }
}

async function waitForApi(apiBaseUrl, retries = 60) {
  for (let index = 0; index < retries; index += 1) {
    try {
      const response = await fetch(`${apiBaseUrl}/statistics/overview`)
      if (response.ok) {
        return
      }
    } catch (_) {
      // The API is still starting or unavailable.
    }
    await new Promise((resolve) => setTimeout(resolve, 500))
  }

  throw new Error('数据库服务连接超时，请检查配置后重试。')
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 820,
    minWidth: 1024,
    minHeight: 680,
    title: '图书管理系统',
    webPreferences: {
      preload: path.join(__dirname, 'preload.cjs'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  })

  if (!app.isPackaged && process.env.VITE_DEV_SERVER_URL) {
    mainWindow.loadURL(process.env.VITE_DEV_SERVER_URL)
  } else {
    mainWindow.loadFile(path.join(__dirname, '..', 'dist', 'index.html'))
  }

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

function openDatabaseSettingsWindow() {
  if (settingsWindow) {
    settingsWindow.focus()
    return
  }

  settingsWindow = new BrowserWindow({
    width: 720,
    height: 720,
    minWidth: 640,
    minHeight: 620,
    parent: mainWindow || undefined,
    modal: false,
    title: '切换数据库',
    resizable: true,
    webPreferences: {
      preload: path.join(__dirname, 'preload.cjs'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  })

  settingsWindow.setMenu(null)
  settingsWindow.loadFile(path.join(__dirname, 'database-settings.html'))
  settingsWindow.on('closed', () => {
    settingsWindow = null
  })
}

function setupMenu() {
  const template = [
    {
      label: '文件',
      submenu: [
        {
          label: '退出',
          accelerator: 'Alt+F4',
          click: () => app.quit(),
        },
      ],
    },
    {
      label: '设置',
      submenu: [
        {
          label: '切换数据库',
          click: () => openDatabaseSettingsWindow(),
        },
      ],
    },
  ]

  Menu.setApplicationMenu(Menu.buildFromTemplate(template))
}

app.whenReady().then(async () => {
  try {
    setupMenu()
    activeSettings = readSettings()
    applyActiveEnvironment(activeSettings)

    if (activeSettings.databaseMode === 'cloud' && !activeSettings.cloudApiBaseUrl) {
      dialog.showErrorBox('配置错误', '云端服务器数据库未配置 API 地址，已改用本地数据库启动。')
      activeSettings.databaseMode = 'local'
      applyActiveEnvironment(activeSettings)
    }

    if (activeSettings.databaseMode === 'local') {
      activeSettings = await startLocalBackendWithFallback(activeSettings)
    }

    createWindow()
  } catch (error) {
    dialog.showErrorBox('启动失败', error.message)
    app.quit()
  }
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow()
  }
})

app.on('before-quit', () => {
  if (backendProcess) {
    backendProcess.kill()
    backendProcess = null
  }
})

ipcMain.handle('database-settings:get', () => {
  const settings = activeSettings || readSettings()
  return {
    ...settings,
    activeApiBaseUrl,
    activeDatabaseMode: settings.databaseMode,
    activeModeLabel: getActiveModeLabel(settings),
    embeddedDatabasePath: getEmbeddedDatabasePath(),
  }
})

ipcMain.handle('database-settings:save', (_event, settings) => {
  const savedSettings = writeSettings(settings || {})
  return {
    ...savedSettings,
    activeApiBaseUrl,
    activeDatabaseMode: (activeSettings || savedSettings).databaseMode,
    activeModeLabel: getActiveModeLabel(activeSettings || savedSettings),
    embeddedDatabasePath: getEmbeddedDatabasePath(),
    requiresRestart: true,
  }
})

ipcMain.handle('database-settings:choose-h2-file', async () => {
  const result = await dialog.showSaveDialog(settingsWindow || mainWindow, {
    title: '选择本地数据库文件',
    defaultPath: path.join(app.getPath('documents'), 'library_management'),
    buttonLabel: '选择',
    filters: [
      { name: 'H2 数据库', extensions: ['mv.db'] },
      { name: '所有文件', extensions: ['*'] },
    ],
  })

  if (result.canceled || !result.filePath) {
    return ''
  }

  return normalizeH2FilePath(result.filePath)
})

ipcMain.handle('database-settings:test-cloud', async (_event, apiBaseUrl) => {
  const baseUrl = normalizeApiBaseUrl(apiBaseUrl)
  if (!/^https?:\/\/.+/i.test(baseUrl)) {
    return {
      ok: false,
      message: '请输入以 http:// 或 https:// 开头的 API 地址。',
    }
  }

  try {
    const response = await fetch(`${baseUrl}/statistics/overview`, {
      method: 'GET',
      signal: AbortSignal.timeout(45000),
    })

    if (!response.ok) {
      return {
        ok: false,
        message: `连接失败，服务器返回 ${response.status}。`,
      }
    }

    return {
      ok: true,
      message: `连接成功，云端 API 可访问：${baseUrl}`,
    }
  } catch (error) {
    return {
      ok: false,
      message: `连接失败：${error.message}。如果使用 Render 免费服务，请先在浏览器打开服务地址唤醒后再试。`,
    }
  }
})

ipcMain.handle('database-settings:restart', () => {
  app.relaunch()
  app.exit(0)
})
