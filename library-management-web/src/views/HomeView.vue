<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Check,
  DataAnalysis,
  Delete,
  Edit,
  HomeFilled,
  Notebook,
  Plus,
  Reading,
  RefreshRight,
  Setting,
  Search,
  SwitchButton,
  Tickets,
  User,
} from '@element-plus/icons-vue'
import { useAppStore } from '../stores/app'
import {
  createBook,
  createBorrow,
  createReader,
  deleteBook,
  deleteBorrow,
  deleteReader,
  getOverview,
  listBooks,
  listBorrows,
  listReaders,
  login,
  returnBorrow,
  updateBook,
  updateReader,
} from '../api/library'
import { API_BASE_URL } from '../utils/request'

const appStore = useAppStore()
const activeSection = ref('dashboard')
const loading = ref(false)
const session = ref(JSON.parse(localStorage.getItem('library_user') || 'null'))
const isDesktop = Boolean(window.libraryDesktop)
const desktopSettings = ref(null)

const overview = ref({
  totalBooks: 0,
  totalCopies: 0,
  availableCopies: 0,
  totalReaders: 0,
  activeBorrows: 0,
  overdueBorrows: 0,
})

const books = ref([])
const readers = ref([])
const borrows = ref([])

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
})

const bookFilters = reactive({
  keyword: '',
  category: '',
  status: '',
  sort: '',
})

const readerFilters = reactive({
  keyword: '',
  status: '',
  department: '',
  gender: '',
  sort: '',
})

const borrowFilters = reactive({
  keyword: '',
  status: '',
  sort: '',
})

const bookDialog = reactive({
  visible: false,
  mode: 'create',
  form: emptyBookForm(),
})

const readerDialog = reactive({
  visible: false,
  mode: 'create',
  form: emptyReaderForm(),
})

const borrowDialog = reactive({
  visible: false,
  form: emptyBorrowForm(),
})

const returnDialog = reactive({
  visible: false,
  record: null,
  form: {
    returnDate: today(),
  },
})

const bookCategories = computed(() => {
  return [...new Set(books.value.map((book) => book.category).filter(Boolean))]
})

const sortedBooks = computed(() => {
  return sortItems(books.value, bookFilters.sort, {
    'title-asc': { field: 'title', direction: 'asc', type: 'text' },
    'title-desc': { field: 'title', direction: 'desc', type: 'text' },
    'author-asc': { field: 'author', direction: 'asc', type: 'text' },
    'author-desc': { field: 'author', direction: 'desc', type: 'text' },
    'location-asc': { field: 'location', direction: 'asc', type: 'natural' },
    'location-desc': { field: 'location', direction: 'desc', type: 'natural' },
    'publish-date-asc': { field: 'publishDate', direction: 'asc', type: 'date' },
    'publish-date-desc': { field: 'publishDate', direction: 'desc', type: 'date' },
  })
})

const readerDepartments = computed(() => {
  return [...new Set(readers.value.map((reader) => reader.department).filter(Boolean))]
})

const readerGenders = computed(() => {
  return [...new Set(readers.value.map((reader) => reader.gender).filter(Boolean))]
})

const filteredSortedReaders = computed(() => {
  const filteredReaders = readers.value.filter((reader) => {
    const matchesDepartment = !readerFilters.department || reader.department === readerFilters.department
    const matchesGender = !readerFilters.gender || reader.gender === readerFilters.gender
    return matchesDepartment && matchesGender
  })

  return sortItems(filteredReaders, readerFilters.sort, {
    'name-asc': { field: 'name', direction: 'asc', type: 'text' },
    'name-desc': { field: 'name', direction: 'desc', type: 'text' },
    'registered-date-asc': { field: 'registeredDate', direction: 'asc', type: 'date' },
    'registered-date-desc': { field: 'registeredDate', direction: 'desc', type: 'date' },
    'card-number-asc': { field: 'cardNumber', direction: 'asc', type: 'natural' },
    'card-number-desc': { field: 'cardNumber', direction: 'desc', type: 'natural' },
  })
})

const availableBooks = computed(() => {
  return books.value.filter((book) => Number(book.availableCopies) > 0)
})

const activeReaders = computed(() => {
  return readers.value.filter((reader) => reader.status === 'ACTIVE')
})

const sortedBorrows = computed(() => {
  return sortItems(borrows.value, borrowFilters.sort, {
    'borrow-date-asc': { field: 'borrowDate', direction: 'asc', type: 'date' },
    'borrow-date-desc': { field: 'borrowDate', direction: 'desc', type: 'date' },
    'due-date-asc': { field: 'dueDate', direction: 'asc', type: 'date' },
    'due-date-desc': { field: 'dueDate', direction: 'desc', type: 'date' },
    'return-date-asc': { field: 'returnDate', direction: 'asc', type: 'date' },
    'return-date-desc': { field: 'returnDate', direction: 'desc', type: 'date' },
    'book-title-asc': { field: 'book.title', direction: 'asc', type: 'text' },
    'book-title-desc': { field: 'book.title', direction: 'desc', type: 'text' },
    'reader-name-asc': { field: 'reader.name', direction: 'asc', type: 'text' },
    'reader-name-desc': { field: 'reader.name', direction: 'desc', type: 'text' },
    'card-number-asc': { field: 'reader.cardNumber', direction: 'asc', type: 'natural' },
    'card-number-desc': { field: 'reader.cardNumber', direction: 'desc', type: 'natural' },
  })
})

const recentBorrows = computed(() => {
  return [...borrows.value].sort((left, right) => {
    const leftId = Number(left.id || 0)
    const rightId = Number(right.id || 0)
    if (leftId !== rightId) {
      return rightId - leftId
    }
    return compareDates(right.borrowDate, left.borrowDate)
  })
})

const statCards = computed(() => [
  { label: '图书种类', value: overview.value.totalBooks, icon: Notebook, tone: 'blue' },
  { label: '馆藏册数', value: overview.value.totalCopies, icon: Tickets, tone: 'green' },
  { label: '可借册数', value: overview.value.availableCopies, icon: Check, tone: 'cyan' },
  { label: '读者人数', value: overview.value.totalReaders, icon: User, tone: 'violet' },
  { label: '当前借阅', value: overview.value.activeBorrows, icon: Reading, tone: 'orange' },
  { label: '逾期记录', value: overview.value.overdueBorrows, icon: DataAnalysis, tone: 'red' },
])

const desktopModeLabel = computed(() => {
  return desktopSettings.value?.activeModeLabel || (isDesktop ? '本地桌面模式' : 'Web 模式')
})

const stockUsageText = computed(() => {
  const total = Number(overview.value.totalCopies || 0)
  const available = Number(overview.value.availableCopies || 0)
  if (!total) {
    return '暂无馆藏数据'
  }
  const ratio = Math.round((available / total) * 100)
  return `可借率 ${ratio}%（${available}/${total}）`
})

const circulationAlertText = computed(() => {
  const active = Number(overview.value.activeBorrows || 0)
  const overdue = Number(overview.value.overdueBorrows || 0)
  if (overdue > 0) {
    return `当前有 ${overdue} 条逾期记录，建议优先处理。`
  }
  return active > 0 ? `当前有 ${active} 条借阅中记录。` : '当前没有未归还记录。'
})

onMounted(() => {
  loadDesktopSettings()
  if (session.value) {
    refreshAll()
  }
})

async function loadDesktopSettings() {
  if (!window.libraryDesktop?.getDatabaseSettings) {
    return
  }
  try {
    desktopSettings.value = await window.libraryDesktop.getDatabaseSettings()
  } catch {
    desktopSettings.value = null
  }
}

if (window.libraryDesktop?.onDatabaseSettingsChanged) {
  window.libraryDesktop.onDatabaseSettingsChanged(() => {
    loadDesktopSettings()
  })
}

function today() {
  return new Date().toISOString().slice(0, 10)
}

function addDays(dateText, days) {
  const date = dateText ? new Date(dateText) : new Date()
  date.setDate(date.getDate() + days)
  return date.toISOString().slice(0, 10)
}

function emptyBookForm() {
  return {
    id: null,
    title: '',
    author: '',
    isbn: '',
    publisher: '',
    category: '',
    location: '',
    publishDate: '',
    totalCopies: 1,
    description: '',
  }
}

function emptyReaderForm() {
  return {
    id: null,
    name: '',
    gender: '',
    phone: '',
    email: '',
    department: '',
    cardNumber: '',
    registeredDate: today(),
    status: 'ACTIVE',
  }
}

function emptyBorrowForm() {
  const borrowDate = today()
  return {
    bookId: '',
    readerId: '',
    borrowDate,
    dueDate: addDays(borrowDate, 30),
  }
}

function assignForm(target, source) {
  Object.keys(target).forEach((key) => {
    target[key] = source[key] ?? ''
  })
}

async function handleLogin() {
  loading.value = true
  try {
    const user = await login(loginForm)
    session.value = user
    localStorage.setItem('library_user', JSON.stringify(user))
    localStorage.setItem('library_token', user.token)
    ElMessage.success('登录成功')
    await refreshAll()
  } catch {
    ElMessage.error('请检查账号、密码或后端服务状态')
  } finally {
    loading.value = false
  }
}

function logout() {
  session.value = null
  localStorage.removeItem('library_user')
  localStorage.removeItem('library_token')
}

async function openDatabaseSettings() {
  if (window.libraryDesktop?.openDatabaseSettings) {
    await window.libraryDesktop.openDatabaseSettings()
  }
}

async function refreshAll() {
  loading.value = true
  try {
    await Promise.all([fetchOverview(), fetchBooks(), fetchReaders(), fetchBorrows()])
  } finally {
    loading.value = false
  }
}

async function fetchOverview() {
  overview.value = await getOverview()
}

async function fetchBooks() {
  const { keyword, category, status } = bookFilters
  books.value = await listBooks(cleanParams({ keyword, category, status }))
}

async function fetchReaders() {
  const { keyword, status } = readerFilters
  readers.value = await listReaders(cleanParams({ keyword, status }))
}

async function fetchBorrows() {
  const { keyword, status } = borrowFilters
  borrows.value = await listBorrows(cleanParams({ keyword, status }))
}

function cleanParams(params) {
  return Object.fromEntries(Object.entries(params).filter(([, value]) => value !== ''))
}

function sortItems(items, sortKey, sortRules) {
  const rule = sortRules[sortKey]
  if (!rule) {
    return items
  }

  return [...items].sort((left, right) => {
    const result = compareValues(getFieldValue(left, rule.field), getFieldValue(right, rule.field), rule.type)
    return rule.direction === 'desc' ? -result : result
  })
}

function getFieldValue(item, field) {
  return String(field)
    .split('.')
    .reduce((value, key) => value?.[key], item)
}

function compareValues(left, right, type) {
  if (type === 'date') {
    return compareDates(left, right)
  }
  return compareText(left, right, type === 'natural')
}

function compareText(left, right, numeric = false) {
  return String(left ?? '').localeCompare(String(right ?? ''), 'zh-Hans-CN', {
    numeric,
    sensitivity: 'base',
  })
}

function compareDates(left, right) {
  const leftTime = left ? new Date(left).getTime() : 0
  const rightTime = right ? new Date(right).getTime() : 0
  return leftTime - rightTime
}

function openBookDialog(book) {
  bookDialog.mode = book ? 'edit' : 'create'
  assignForm(bookDialog.form, book ? { ...emptyBookForm(), ...book } : emptyBookForm())
  bookDialog.visible = true
}

async function submitBook() {
  const payload = { ...bookDialog.form }
  delete payload.id
  loading.value = true
  try {
    if (bookDialog.mode === 'edit') {
      await updateBook(bookDialog.form.id, payload)
      ElMessage.success('图书已更新')
    } else {
      await createBook(payload)
      ElMessage.success('图书已新增')
    }
    bookDialog.visible = false
    await Promise.all([fetchBooks(), fetchOverview()])
  } catch {
    ElMessage.error('图书保存失败')
  } finally {
    loading.value = false
  }
}

async function confirmDeleteBook(book) {
  try {
    await ElMessageBox.confirm(`确定删除《${book.title}》吗？`, '删除图书', {
      type: 'warning',
    })
  } catch {
    return
  }
  loading.value = true
  try {
    await deleteBook(book.id)
    ElMessage.success('图书已删除')
    await Promise.all([fetchBooks(), fetchOverview()])
  } catch {
    ElMessage.error('图书删除失败')
  } finally {
    loading.value = false
  }
}

function openReaderDialog(reader) {
  readerDialog.mode = reader ? 'edit' : 'create'
  assignForm(readerDialog.form, reader ? { ...emptyReaderForm(), ...reader } : emptyReaderForm())
  readerDialog.visible = true
}

async function submitReader() {
  const payload = { ...readerDialog.form }
  delete payload.id
  loading.value = true
  try {
    if (readerDialog.mode === 'edit') {
      await updateReader(readerDialog.form.id, payload)
      ElMessage.success('读者已更新')
    } else {
      await createReader(payload)
      ElMessage.success('读者已新增')
    }
    readerDialog.visible = false
    await Promise.all([fetchReaders(), fetchOverview()])
  } catch {
    ElMessage.error('读者保存失败')
  } finally {
    loading.value = false
  }
}

async function confirmDeleteReader(reader) {
  try {
    await ElMessageBox.confirm(`确定删除读者 ${reader.name} 吗？`, '删除读者', {
      type: 'warning',
    })
  } catch {
    return
  }
  loading.value = true
  try {
    await deleteReader(reader.id)
    ElMessage.success('读者已删除')
    await Promise.all([fetchReaders(), fetchOverview()])
  } catch {
    ElMessage.error('读者删除失败')
  } finally {
    loading.value = false
  }
}

function openBorrowDialog() {
  assignForm(borrowDialog.form, emptyBorrowForm())
  borrowDialog.visible = true
}

async function submitBorrow() {
  if (!borrowDialog.form.bookId || !borrowDialog.form.readerId) {
    ElMessage.warning('请选择图书和读者')
    return
  }
  loading.value = true
  try {
    await createBorrow({
      ...borrowDialog.form,
      bookId: Number(borrowDialog.form.bookId),
      readerId: Number(borrowDialog.form.readerId),
    })
    ElMessage.success('借阅登记完成')
    borrowDialog.visible = false
    await refreshAll()
  } catch {
    ElMessage.error('借阅登记失败')
  } finally {
    loading.value = false
  }
}

function openReturnDialog(record) {
  returnDialog.record = record
  returnDialog.form.returnDate = today()
  returnDialog.visible = true
}

async function submitReturn() {
  loading.value = true
  try {
    await returnBorrow(returnDialog.record.id, returnDialog.form)
    ElMessage.success('归还登记完成')
    returnDialog.visible = false
    await refreshAll()
  } catch {
    ElMessage.error('归还登记失败')
  } finally {
    loading.value = false
  }
}

async function confirmDeleteBorrow(record) {
  const bookTitle = record.book?.title || '该图书'
  const readerName = record.reader?.name || '该读者'
  try {
    await ElMessageBox.confirm(`确定删除 ${readerName} 的《${bookTitle}》借阅记录吗？`, '删除借阅记录', {
      type: 'warning',
    })
  } catch {
    return
  }
  loading.value = true
  try {
    await deleteBorrow(record.id)
    ElMessage.success('借阅记录已删除')
    await refreshAll()
  } catch {
    ElMessage.error('借阅记录删除失败')
  } finally {
    loading.value = false
  }
}

function statusTag(status) {
  return status === 'ACTIVE' || status === 'BORROWED' ? 'success' : 'info'
}

function borrowStatusText(status) {
  return status === 'BORROWED' ? '借阅中' : '已归还'
}

function readerStatusText(status) {
  return status === 'ACTIVE' ? '正常' : '停用'
}

function bookAvailability(book) {
  return Number(book.availableCopies) > 0 ? '可借' : '不可借'
}

function bookAvailabilityType(book) {
  return Number(book.availableCopies) > 0 ? 'success' : 'danger'
}

function isOverdue(record) {
  return record.status === 'BORROWED' && record.dueDate && record.dueDate < today()
}
</script>

<template>
  <section v-if="!session" class="login-page">
    <div class="login-panel">
      <div class="login-copy">
        <p class="eyebrow">Library Console</p>
        <h1>{{ appStore.title }}</h1>
        <p>课程设计管理端，连接后端接口 {{ API_BASE_URL }}。</p>
      </div>
      <el-form class="login-form" :model="loginForm" label-position="top" @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="loginForm.username" size="large" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="loginForm.password"
            size="large"
            type="password"
            autocomplete="current-password"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button :loading="loading" class="login-button" type="primary" size="large" @click="handleLogin">
          登录系统
        </el-button>
      </el-form>
    </div>
  </section>

  <el-container v-else class="app-shell">
    <el-aside width="232px" class="sidebar">
      <div class="brand">
        <span class="brand-mark">L</span>
        <span>{{ appStore.title }}</span>
      </div>
      <el-menu :default-active="activeSection" class="menu" @select="activeSection = $event">
        <el-menu-item index="dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页概览</span>
        </el-menu-item>
        <el-menu-item index="books">
          <el-icon><Notebook /></el-icon>
          <span>图书管理</span>
        </el-menu-item>
        <el-menu-item index="readers">
          <el-icon><User /></el-icon>
          <span>读者管理</span>
        </el-menu-item>
        <el-menu-item index="borrows">
          <el-icon><Reading /></el-icon>
          <span>借阅归还</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div>
          <h2>{{ appStore.title }}</h2>
          <span>后端接口：{{ API_BASE_URL }}</span>
        </div>
        <div class="header-actions">
          <div class="data-source-chip" :class="{ desktop: isDesktop }">
            <span>{{ desktopModeLabel }}</span>
            <small>{{ isDesktop ? '桌面端自动后端' : 'Web API' }}</small>
          </div>
          <el-button v-if="isDesktop" :icon="Setting" @click="openDatabaseSettings">数据源设置</el-button>
          <el-button :icon="RefreshRight" :loading="loading" @click="refreshAll">刷新</el-button>
          <el-button :icon="SwitchButton" @click="logout">退出</el-button>
        </div>
      </el-header>

      <el-main v-loading="loading" class="main">
        <template v-if="activeSection === 'dashboard'">
          <div class="page-title">
            <p>Overview</p>
            <h1>馆藏与借阅状态</h1>
          </div>
          <div class="stat-grid">
            <article v-for="card in statCards" :key="card.label" class="stat-card" :class="`tone-${card.tone}`">
              <div class="stat-icon">
                <el-icon><component :is="card.icon" /></el-icon>
              </div>
              <span>{{ card.label }}</span>
              <strong>{{ card.value }}</strong>
            </article>
          </div>
          <section class="status-strip">
            <div>
              <span>库存健康度</span>
              <strong>{{ stockUsageText }}</strong>
            </div>
            <div>
              <span>流通提醒</span>
              <strong>{{ circulationAlertText }}</strong>
            </div>
            <div>
              <span>数据来源</span>
              <strong>{{ desktopModeLabel }}</strong>
            </div>
          </section>
          <section class="content-panel">
            <div class="panel-header">
              <div>
                <p>Borrowing</p>
                <h3>近期借阅记录</h3>
              </div>
              <el-button text type="primary" @click="activeSection = 'borrows'">查看全部</el-button>
            </div>
            <el-table :data="recentBorrows.slice(0, 6)" stripe>
              <el-table-column label="图书" min-width="180">
                <template #default="{ row }">{{ row.book?.title || '-' }}</template>
              </el-table-column>
              <el-table-column label="读者" width="120">
                <template #default="{ row }">{{ row.reader?.name || '-' }}</template>
              </el-table-column>
              <el-table-column prop="borrowDate" label="借阅日期" width="120" />
              <el-table-column prop="dueDate" label="应还日期" width="120" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="isOverdue(row) ? 'danger' : statusTag(row.status)" effect="light">
                    {{ isOverdue(row) ? '已逾期' : borrowStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="删除" fixed="right" width="90">
                <template #default="{ row }">
                  <el-button :icon="Delete" circle type="danger" @click="confirmDeleteBorrow(row)" />
                </template>
              </el-table-column>
            </el-table>
          </section>
        </template>

        <template v-if="activeSection === 'books'">
          <div class="page-title compact">
            <p>Books</p>
            <h1>图书管理</h1>
          </div>
          <section class="content-panel">
            <div class="toolbar">
              <el-input v-model="bookFilters.keyword" clearable placeholder="书名、作者、ISBN" :prefix-icon="Search" />
              <el-select v-model="bookFilters.category" clearable placeholder="分类">
                <el-option v-for="category in bookCategories" :key="category" :label="category" :value="category" />
              </el-select>
              <el-select v-model="bookFilters.status" clearable placeholder="借阅状态">
                <el-option label="可借" value="available" />
                <el-option label="不可借" value="unavailable" />
              </el-select>
              <el-select v-model="bookFilters.sort" clearable placeholder="排序方式">
                <el-option label="书名 A-Z" value="title-asc" />
                <el-option label="书名 Z-A" value="title-desc" />
                <el-option label="作者 A-Z" value="author-asc" />
                <el-option label="作者 Z-A" value="author-desc" />
                <el-option label="位置 从小到大" value="location-asc" />
                <el-option label="位置 从大到小" value="location-desc" />
                <el-option label="出版时间 正序" value="publish-date-asc" />
                <el-option label="出版时间 倒序" value="publish-date-desc" />
              </el-select>
              <el-button :icon="Search" type="primary" @click="fetchBooks">查询</el-button>
              <el-button :icon="Plus" type="success" @click="openBookDialog()">新增图书</el-button>
            </div>
            <el-table :data="sortedBooks" stripe>
              <el-table-column prop="title" label="书名" min-width="180" show-overflow-tooltip />
              <el-table-column prop="author" label="作者" width="120" />
              <el-table-column prop="isbn" label="ISBN" width="150" />
              <el-table-column prop="category" label="分类" width="120" />
              <el-table-column prop="location" label="位置" width="100" />
              <el-table-column label="库存" width="110">
                <template #default="{ row }">{{ row.availableCopies }} / {{ row.totalCopies }}</template>
              </el-table-column>
              <el-table-column label="状态" width="95">
                <template #default="{ row }">
                  <el-tag :type="bookAvailabilityType(row)" effect="light">{{ bookAvailability(row) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" fixed="right" width="150">
                <template #default="{ row }">
                  <el-button :icon="Edit" circle @click="openBookDialog(row)" />
                  <el-button :icon="Delete" circle type="danger" @click="confirmDeleteBook(row)" />
                </template>
              </el-table-column>
            </el-table>
          </section>
        </template>

        <template v-if="activeSection === 'readers'">
          <div class="page-title compact">
            <p>Readers</p>
            <h1>读者管理</h1>
          </div>
          <section class="content-panel">
            <div class="toolbar">
              <el-input v-model="readerFilters.keyword" clearable placeholder="姓名、证号、电话" :prefix-icon="Search" />
              <el-select v-model="readerFilters.status" clearable placeholder="状态">
                <el-option label="正常" value="ACTIVE" />
                <el-option label="停用" value="DISABLED" />
              </el-select>
              <el-select v-model="readerFilters.department" clearable placeholder="院系班级">
                <el-option
                  v-for="department in readerDepartments"
                  :key="department"
                  :label="department"
                  :value="department"
                />
              </el-select>
              <el-select v-model="readerFilters.gender" clearable placeholder="性别">
                <el-option v-for="gender in readerGenders" :key="gender" :label="gender" :value="gender" />
              </el-select>
              <el-select v-model="readerFilters.sort" clearable placeholder="排序方式">
                <el-option label="姓名 A-Z" value="name-asc" />
                <el-option label="姓名 Z-A" value="name-desc" />
                <el-option label="登记日期 正序" value="registered-date-asc" />
                <el-option label="登记日期 倒序" value="registered-date-desc" />
                <el-option label="借阅证号 从小到大" value="card-number-asc" />
                <el-option label="借阅证号 从大到小" value="card-number-desc" />
              </el-select>
              <el-button :icon="Search" type="primary" @click="fetchReaders">查询</el-button>
              <el-button :icon="Plus" type="success" @click="openReaderDialog()">新增读者</el-button>
            </div>
            <el-table :data="filteredSortedReaders" stripe>
              <el-table-column prop="name" label="姓名" width="120" />
              <el-table-column prop="cardNumber" label="借阅证号" width="140" />
              <el-table-column prop="gender" label="性别" width="80" />
              <el-table-column prop="phone" label="电话" width="140" />
              <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
              <el-table-column prop="department" label="院系班级" min-width="150" />
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="statusTag(row.status)" effect="light">{{ readerStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" fixed="right" width="150">
                <template #default="{ row }">
                  <el-button :icon="Edit" circle @click="openReaderDialog(row)" />
                  <el-button :icon="Delete" circle type="danger" @click="confirmDeleteReader(row)" />
                </template>
              </el-table-column>
            </el-table>
          </section>
        </template>

        <template v-if="activeSection === 'borrows'">
          <div class="page-title compact">
            <p>Circulation</p>
            <h1>借阅归还</h1>
          </div>
          <section class="content-panel">
            <div class="toolbar">
              <el-input v-model="borrowFilters.keyword" clearable placeholder="书名、读者、证号" :prefix-icon="Search" />
              <el-select v-model="borrowFilters.status" clearable placeholder="状态">
                <el-option label="借阅中" value="BORROWED" />
                <el-option label="已归还" value="RETURNED" />
              </el-select>
              <el-select v-model="borrowFilters.sort" clearable placeholder="排序方式">
                <el-option label="借阅日期 正序" value="borrow-date-asc" />
                <el-option label="借阅日期 倒序" value="borrow-date-desc" />
                <el-option label="应还日期 正序" value="due-date-asc" />
                <el-option label="应还日期 倒序" value="due-date-desc" />
                <el-option label="归还日期 正序" value="return-date-asc" />
                <el-option label="归还日期 倒序" value="return-date-desc" />
                <el-option label="图书 A-Z" value="book-title-asc" />
                <el-option label="图书 Z-A" value="book-title-desc" />
                <el-option label="读者 A-Z" value="reader-name-asc" />
                <el-option label="读者 Z-A" value="reader-name-desc" />
                <el-option label="证号 从小到大" value="card-number-asc" />
                <el-option label="证号 从大到小" value="card-number-desc" />
              </el-select>
              <el-button :icon="Search" type="primary" @click="fetchBorrows">查询</el-button>
              <el-button :icon="Plus" type="success" @click="openBorrowDialog">登记借阅</el-button>
            </div>
            <el-table :data="sortedBorrows" stripe>
              <el-table-column label="图书" min-width="180">
                <template #default="{ row }">{{ row.book?.title || '-' }}</template>
              </el-table-column>
              <el-table-column label="读者" width="120">
                <template #default="{ row }">{{ row.reader?.name || '-' }}</template>
              </el-table-column>
              <el-table-column label="证号" width="140">
                <template #default="{ row }">{{ row.reader?.cardNumber || '-' }}</template>
              </el-table-column>
              <el-table-column prop="borrowDate" label="借阅日期" width="120" />
              <el-table-column prop="dueDate" label="应还日期" width="120" />
              <el-table-column prop="returnDate" label="归还日期" width="120" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="isOverdue(row) ? 'danger' : statusTag(row.status)" effect="light">
                    {{ isOverdue(row) ? '已逾期' : borrowStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" fixed="right" width="150">
                <template #default="{ row }">
                  <el-button
                    v-if="row.status === 'BORROWED'"
                    :icon="Check"
                    type="success"
                    size="small"
                    @click="openReturnDialog(row)"
                  >
                    还书
                  </el-button>
                  <span v-else class="muted">完成</span>
                </template>
              </el-table-column>
              <el-table-column label="删除" fixed="right" width="90">
                <template #default="{ row }">
                  <el-button :icon="Delete" circle type="danger" @click="confirmDeleteBorrow(row)" />
                </template>
              </el-table-column>
            </el-table>
          </section>
        </template>
      </el-main>
    </el-container>
  </el-container>

  <el-dialog v-model="bookDialog.visible" :title="bookDialog.mode === 'edit' ? '编辑图书' : '新增图书'" width="720px">
    <el-form :model="bookDialog.form" label-width="92px">
      <div class="form-grid">
        <el-form-item label="书名" required>
          <el-input v-model="bookDialog.form.title" />
        </el-form-item>
        <el-form-item label="作者" required>
          <el-input v-model="bookDialog.form.author" />
        </el-form-item>
        <el-form-item label="ISBN" required>
          <el-input v-model="bookDialog.form.isbn" />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="bookDialog.form.publisher" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="bookDialog.form.category" />
        </el-form-item>
        <el-form-item label="馆藏位置">
          <el-input v-model="bookDialog.form.location" />
        </el-form-item>
        <el-form-item label="出版日期">
          <el-date-picker v-model="bookDialog.form.publishDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="总册数" required>
          <el-input-number v-model="bookDialog.form.totalCopies" :min="1" />
        </el-form-item>
      </div>
      <el-form-item label="简介">
        <el-input v-model="bookDialog.form.description" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="bookDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="submitBook">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="readerDialog.visible" :title="readerDialog.mode === 'edit' ? '编辑读者' : '新增读者'" width="720px">
    <el-form :model="readerDialog.form" label-width="92px">
      <div class="form-grid">
        <el-form-item label="姓名" required>
          <el-input v-model="readerDialog.form.name" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="readerDialog.form.gender" clearable>
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="借阅证号" required>
          <el-input v-model="readerDialog.form.cardNumber" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="readerDialog.form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="readerDialog.form.email" />
        </el-form-item>
        <el-form-item label="院系班级">
          <el-input v-model="readerDialog.form.department" />
        </el-form-item>
        <el-form-item label="登记日期">
          <el-date-picker v-model="readerDialog.form.registeredDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="readerDialog.form.status">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="readerDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="submitReader">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="borrowDialog.visible" title="登记借阅" width="640px">
    <el-form :model="borrowDialog.form" label-width="92px">
      <el-form-item label="图书" required>
        <el-select v-model="borrowDialog.form.bookId" filterable placeholder="选择可借图书">
          <el-option
            v-for="book in availableBooks"
            :key="book.id"
            :label="`${book.title}（余 ${book.availableCopies}）`"
            :value="book.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="读者" required>
        <el-select v-model="borrowDialog.form.readerId" filterable placeholder="选择正常读者">
          <el-option
            v-for="reader in activeReaders"
            :key="reader.id"
            :label="`${reader.name}（${reader.cardNumber}）`"
            :value="reader.id"
          />
        </el-select>
      </el-form-item>
      <div class="form-grid">
        <el-form-item label="借阅日期">
          <el-date-picker v-model="borrowDialog.form.borrowDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="应还日期">
          <el-date-picker v-model="borrowDialog.form.dueDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="borrowDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="submitBorrow">登记</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="returnDialog.visible" title="归还图书" width="480px">
    <div v-if="returnDialog.record" class="return-summary">
      <strong>{{ returnDialog.record.book?.title }}</strong>
      <span>{{ returnDialog.record.reader?.name }}，应还日期 {{ returnDialog.record.dueDate }}</span>
    </div>
    <el-form :model="returnDialog.form" label-width="92px">
      <el-form-item label="归还日期">
        <el-date-picker v-model="returnDialog.form.returnDate" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="returnDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="submitReturn">确认归还</el-button>
    </template>
  </el-dialog>
</template>
