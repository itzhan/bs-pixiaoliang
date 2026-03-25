<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getLogs, deleteLog } from '@/api/auditLog'

/* ---------- Options ---------- */
const moduleOptions = [
  { label: '认证', value: 'AUTH' },
  { label: '用户', value: 'USER' },
  { label: '车辆', value: 'VEHICLE' },
  { label: '停车', value: 'PARKING' },
  { label: '订单', value: 'ORDER' },
  { label: '支付', value: 'PAYMENT' },
  { label: '预约', value: 'RESERVATION' },
  { label: '公告', value: 'ANNOUNCEMENT' },
  { label: '评价', value: 'REVIEW' },
  { label: '系统', value: 'SYSTEM' },
]

const actionOptions = [
  { label: '登录', value: 'LOGIN' },
  { label: '创建', value: 'CREATE' },
  { label: '更新', value: 'UPDATE' },
  { label: '删除', value: 'DELETE' },
  { label: '查询', value: 'QUERY' },
  { label: '导出', value: 'EXPORT' },
]

/* ---------- Search ---------- */
const searchForm = reactive({
  module: undefined as string | undefined,
  action: undefined as string | undefined,
})

/* ---------- Table ---------- */
const loading = ref(false)
const dataSource = ref<any[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const columns = [
  { title: '用户名', dataIndex: 'username', width: 120 },
  { title: '模块', dataIndex: 'module', width: 100 },
  { title: '操作', dataIndex: 'action', width: 100 },
  { title: '详情', dataIndex: 'detail', ellipsis: true },
  { title: 'IP地址', dataIndex: 'ipAddress', width: 140 },
  { title: '记录时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' as const },
]

async function fetchData() {
  loading.value = true
  try {
    const res = await getLogs({
      page: pagination.current,
      size: pagination.pageSize,
      module: searchForm.module,
      action: searchForm.action,
    })
    dataSource.value = res.data.records
    pagination.total = res.data.total
  } catch {
    /* handled by interceptor */
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
  searchForm.module = undefined
  searchForm.action = undefined
  pagination.current = 1
  fetchData()
}

/* ---------- Helpers ---------- */
function formatDate(val: string) {
  return val ? dayjs(val).format('YYYY-MM-DD HH:mm:ss') : '-'
}

onMounted(fetchData)

/* ---------- Delete ---------- */
async function handleDeleteLog(id: number) {
  try {
    await deleteLog(id)
    message.success('删除成功')
    fetchData()
  } catch {
    message.error('删除失败')
  }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>审计日志</h2>
    </div>

    <!-- Search bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-select
          v-model:value="searchForm.module"
          placeholder="模块筛选"
          allow-clear
          style="width: 140px"
        >
          <a-select-option v-for="opt in moduleOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </a-select-option>
        </a-select>
        <a-select
          v-model:value="searchForm.action"
          placeholder="操作筛选"
          allow-clear
          style="width: 140px"
        >
          <a-select-option v-for="opt in actionOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </a-select-option>
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
      :pagination="{
        current: pagination.current,
        pageSize: pagination.pageSize,
        total: pagination.total,
        showSizeChanger: true,
        showTotal: (t: number) => `共 ${t} 条`,
      }"
      row-key="id"
      :scroll="{ x: 1000 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'createTime'">
          {{ formatDate(record.createTime) }}
        </template>

        <template v-else-if="column.key === 'action'">
          <a-popconfirm
            title="确定删除该日志？"
            ok-text="确定"
            cancel-text="取消"
            @confirm="handleDeleteLog(record.id)"
          >
            <a-button type="link" size="small" danger>删除</a-button>
          </a-popconfirm>
        </template>
      </template>
    </a-table>
  </div>
</template>
