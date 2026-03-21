<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { getUsers, updateUser, deleteUser } from '@/api/user'
import dayjs from 'dayjs'

/* ---------- Types ---------- */
interface UserRecord {
  id: number
  username: string
  realName: string
  phone: string
  email: string
  role: string
  status: number
  createdAt: string
}

/* ---------- Search ---------- */
const searchParams = reactive({
  keyword: '',
  role: undefined as string | undefined,
})

/* ---------- Pagination ---------- */
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

/* ---------- Table ---------- */
const loading = ref(false)
const dataSource = ref<UserRecord[]>([])

const columns = [
  { title: '用户名', dataIndex: 'username', width: 120 },
  { title: '真实姓名', dataIndex: 'realName', width: 120 },
  { title: '手机号', dataIndex: 'phone', width: 140 },
  { title: '邮箱', dataIndex: 'email', width: 180 },
  { title: '角色', dataIndex: 'role', width: 100, key: 'role' },
  { title: '状态', dataIndex: 'status', width: 90, key: 'status' },
  { title: '创建时间', dataIndex: 'createdAt', width: 180, key: 'createdAt' },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const },
]

const roleColorMap: Record<string, string> = {
  ADMIN: 'red',
  OPERATOR: 'blue',
  USER: 'green',
}

const roleLabelMap: Record<string, string> = {
  ADMIN: '管理员',
  OPERATOR: '操作员',
  USER: '普通用户',
}

/* ---------- Fetch ---------- */
async function fetchData() {
  loading.value = true
  try {
    const res: any = await getUsers({
      page: pagination.current,
      size: pagination.pageSize,
      keyword: searchParams.keyword || undefined,
      role: searchParams.role || undefined,
    })
    dataSource.value = res.data.records
    pagination.total = res.data.total
  } catch {
    message.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function handleSearch() {
  pagination.current = 1
  fetchData()
}

function handleReset() {
  searchParams.keyword = ''
  searchParams.role = undefined
  pagination.current = 1
  fetchData()
}

/* ---------- Edit Modal ---------- */
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  id: 0,
  realName: '',
  phone: '',
  email: '',
  role: '',
  status: 1,
})

function handleEdit(record: UserRecord) {
  editForm.id = record.id
  editForm.realName = record.realName || ''
  editForm.phone = record.phone || ''
  editForm.email = record.email || ''
  editForm.role = record.role || 'USER'
  editForm.status = record.status ?? 1
  editVisible.value = true
}

async function handleEditSubmit() {
  editLoading.value = true
  try {
    await updateUser(editForm.id, {
      realName: editForm.realName,
      phone: editForm.phone,
      email: editForm.email,
      role: editForm.role,
      status: editForm.status,
    })
    message.success('更新成功')
    editVisible.value = false
    fetchData()
  } catch {
    message.error('更新失败')
  } finally {
    editLoading.value = false
  }
}

/* ---------- Delete ---------- */
async function handleDelete(id: number) {
  try {
    await deleteUser(id)
    message.success('删除成功')
    fetchData()
  } catch {
    message.error('删除失败')
  }
}

/* ---------- Format ---------- */
function formatDate(val: string) {
  return val ? dayjs(val).format('YYYY-MM-DD HH:mm:ss') : '-'
}

/* ---------- Init ---------- */
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <!-- Search Bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-input
          v-model:value="searchParams.keyword"
          placeholder="搜索用户名/姓名/手机号"
          allow-clear
          style="width: 220px"
          @press-enter="handleSearch"
        />
        <a-select
          v-model:value="searchParams.role"
          placeholder="选择角色"
          allow-clear
          style="width: 140px"
        >
          <a-select-option value="ADMIN">管理员</a-select-option>
          <a-select-option value="OPERATOR">操作员</a-select-option>
          <a-select-option value="USER">普通用户</a-select-option>
        </a-select>
        <a-button type="primary" @click="handleSearch">
          <template #icon><SearchOutlined /></template>
          搜索
        </a-button>
        <a-button @click="handleReset">
          <template #icon><ReloadOutlined /></template>
          重置
        </a-button>
      </div>
    </div>

    <!-- Table -->
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      :scroll="{ x: 1200 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'role'">
          <a-tag :color="roleColorMap[record.role] || 'default'">
            {{ roleLabelMap[record.role] || record.role }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-popconfirm
              title="确定删除该用户？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- Edit Modal -->
    <a-modal
      v-model:open="editVisible"
      title="编辑用户"
      :confirm-loading="editLoading"
      @ok="handleEditSubmit"
    >
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }" style="margin-top: 16px">
        <a-form-item label="真实姓名">
          <a-input v-model:value="editForm.realName" placeholder="请输入真实姓名" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="editForm.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="editForm.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="editForm.role">
            <a-select-option value="ADMIN">管理员</a-select-option>
            <a-select-option value="OPERATOR">操作员</a-select-option>
            <a-select-option value="USER">普通用户</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="editForm.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
