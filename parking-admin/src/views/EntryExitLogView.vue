<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getLogs } from '@/api/entryExitLog'

/* ---------- Log type map ---------- */
const logTypeMap: Record<string, { label: string; color: string }> = {
  ENTRY: { label: '入场', color: 'green' },
  EXIT: { label: '出场', color: 'blue' },
}

/* ---------- Search ---------- */
const searchForm = reactive({
  plateNumber: '',
  logType: undefined as string | undefined,
})

/* ---------- Table ---------- */
const loading = ref(false)
const dataSource = ref<any[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '车牌号', dataIndex: 'plateNumber', width: 130 },
  { title: '日志类型', dataIndex: 'logType', width: 110 },
  { title: '闸机名称', dataIndex: 'gateName', ellipsis: true },
  { title: '关联订单ID', dataIndex: 'orderId', width: 120 },
  { title: '记录时间', dataIndex: 'recordTime', width: 180 },
]

async function fetchData() {
  loading.value = true
  try {
    const res = await getLogs({
      page: pagination.current,
      size: pagination.pageSize,
      plateNumber: searchForm.plateNumber || undefined,
      logType: searchForm.logType,
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
  searchForm.plateNumber = ''
  searchForm.logType = undefined
  pagination.current = 1
  fetchData()
}

/* ---------- Helpers ---------- */
function formatDate(val: string) {
  return val ? dayjs(val).format('YYYY-MM-DD HH:mm:ss') : '-'
}

onMounted(fetchData)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>出入日志</h2>
    </div>

    <!-- Search bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-input
          v-model:value="searchForm.plateNumber"
          placeholder="车牌号搜索"
          allow-clear
          style="width: 160px"
          @press-enter="handleSearch"
        />
        <a-select
          v-model:value="searchForm.logType"
          placeholder="日志类型"
          allow-clear
          style="width: 140px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="ENTRY">入场</a-select-option>
          <a-select-option value="EXIT">出场</a-select-option>
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
      :scroll="{ x: 800 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'logType'">
          <a-tag :color="logTypeMap[record.logType]?.color ?? 'default'">
            {{ logTypeMap[record.logType]?.label ?? record.logType }}
          </a-tag>
        </template>

        <template v-else-if="column.dataIndex === 'recordTime'">
          {{ formatDate(record.recordTime) }}
        </template>
      </template>
    </a-table>
  </div>
</template>
