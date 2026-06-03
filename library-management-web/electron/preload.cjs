const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('libraryDesktop', {
  apiBaseUrl: process.env.DESKTOP_API_BASE_URL || 'http://127.0.0.1:18080/api',
  mode: process.env.DESKTOP_DATABASE_MODE || 'local',
  getDatabaseSettings: () => ipcRenderer.invoke('database-settings:get'),
  saveDatabaseSettings: (settings) => ipcRenderer.invoke('database-settings:save', settings),
  openDatabaseSettings: () => ipcRenderer.invoke('database-settings:open'),
  chooseH2DatabaseFile: () => ipcRenderer.invoke('database-settings:choose-h2-file'),
  testCloudConnection: (apiBaseUrl) => ipcRenderer.invoke('database-settings:test-cloud', apiBaseUrl),
  restartApp: () => ipcRenderer.invoke('database-settings:restart'),
  onDatabaseSettingsChanged: (callback) => {
    ipcRenderer.on('database-settings:changed', callback)
  },
})
